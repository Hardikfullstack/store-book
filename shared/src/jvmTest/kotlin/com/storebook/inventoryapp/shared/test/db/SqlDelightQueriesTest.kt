package com.storebook.inventoryapp.shared.test.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * e31-s2 — Test SQLDelight Queries and Transaction Rollbacks.
 *
 * Uses real JdbcSqliteDriver (in-memory). No mocking of the database layer.
 */
class SqlDelightQueriesTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: JdbcSqliteDriver

    @BeforeEach
    fun setup() {
        val (db, d) = com.storebook.inventoryapp.shared.test.DatabaseTestHelper.createDatabase()
        database = db
        driver = d as JdbcSqliteDriver
    }



    @Test
    fun `insertItem persists and retrieves correctly`() {
        val now = 1721000000000L
        database.storeBookQueries.insertItem("Chai Tea Bags", 50.0, "Box", 120.0, 180.0, 5.0, "Beverage", null, null, null, 5.0, now)

        val item = database.storeBookQueries.getItemById(1).executeAsOneOrNull()
        requireNotNull(item)
        assertEquals("Chai Tea Bags", item.name)
        assertEquals(50.0, item.quantity)
    }

    @Test
    fun `softDelete sets is_deleted=True — row exists but excluded from active queries`() {
        database.storeBookQueries.insertItem("Tea Powder", 20.0, "Pack", 80.0, 120.0, 3.0, "Beverage", null, null, null, 5.0, 1721000000000L)
        database.storeBookQueries.softDeleteItem(1721000001000L, 1721000001000L, 1)

        val deleted = database.storeBookQueries.getDeletedItems().executeAsList()
            .find { it.name == "Tea Powder" }
        requireNotNull(deleted)
        assertEquals(1L, deleted.is_deleted)

        val allActive = database.storeBookQueries.getAllItems().executeAsList()
        assertTrue(allActive.none { it.name == "Tea Powder" }, "Soft-deleted item excluded from active list")
    }

    @Test
    fun `updateItem clears is_synced flag so SyncWorker picks up change`() {
        database.storeBookQueries.insertItem("Coffee Pack", 30.0, "Box", 250.0, 400.0, 5.0, "Beverage", null, null, null, 18.0, 1721000000000L)
        database.storeBookQueries.updateItem(name = "Coffee Pack Upgraded", quantity = 35.0, unit = "Box", buy_price = 260.0, sell_price = 420.0, low_stock_threshold = 5.0, category = "Beverage", photo_path = null, barcode = null, hsn_code = null, tax_rate = 18.0, updated_at = 1721000001000L, id = 1L)

        val updated = database.storeBookQueries.getItemById(1).executeAsOneOrNull()
        requireNotNull(updated)
        assertEquals("Coffee Pack Upgraded", updated.name)
        assertEquals(0L, updated.is_synced) // e31-s2: mutation marks dirty for sync
    }

    @Test
    fun `transaction rollback on partial failure — all writes inside block are undone`() {
        // Start with clean state
        database.storeBookQueries.insertItem("Sugar Pack A", 15.0, "Kg", 40.0, 60.0, 3.0, "Spice", null, null, null, 5.0, 1721000000000L)

        // Wrap transaction: modify TWO items; second write throws → both revert
        var threw = false
        try {
            database.transaction {
                database.storeBookQueries.updateItem(name = "Sugar Modified", quantity = 16.0, unit = "Kg", buy_price = 45.0, sell_price = 65.0, low_stock_threshold = 3.0, category = "Spice", photo_path = null, barcode = null, hsn_code = null, tax_rate = 5.0, updated_at = 1721000001000L, id = 1L)
                // Simulate a crash by deliberately failing on an invalid update of id=99999 that doesn't exist — SQLite won't throw but rowcount=0; we force exception instead:
                database.storeBookQueries.updateItem(name = "Poison", quantity = 0.0, unit = "", buy_price = 0.0, sell_price = 0.0, low_stock_threshold = 0.0, category = "X", photo_path = null, barcode = null, hsn_code = null, tax_rate = 0.0, updated_at = 0L, id = 99999L)
                // The above silently skips (no row match). Force real failure to exercise rollback:
                throw IllegalStateException("Simulated crash mid-transaction")
            }
        } catch (e: Exception) {
            threw = true
        }

        assertTrue(threw, "Transaction should have thrown / rolled back")
        val recovered = database.storeBookQueries.getItemById(1).executeAsOneOrNull()
        requireNotNull(recovered)
        assertEquals("Sugar Pack A", recovered.name) // Name restored after rollback
    }

    @Test
    fun `integer overflow safety — large monetary amounts stored as REAL do not wrap`() {
        val hugeAmount = 9_99_99_99_999.0 // ~₹99Cr — larger than Int.MAX_VALUE
        database.storeBookQueries.insertItem("Gold Bullion", hugeAmount, "Kg", 5000000.0, 6000000.0, 1.0, "Luxury", null, null, null, 3.0, 1721000000000L)

        val item = database.storeBookQueries.getItemById(1).executeAsOneOrNull()
        requireNotNull(item)
        assertEquals(hugeAmount, item.quantity, 0.1) // Quantity holds large REAL precisely
        assertFalse(item.quantity < 0, "Quantity must NOT have wrapped negative due to integer overflow")
    }

    @AfterEach
    fun teardown() {
        com.storebook.inventoryapp.shared.test.DatabaseTestHelper.dropDatabase(driver)
    }
}
