package com.storebook.inventoryapp.shared.test.sync

import app.cash.sqldelight.db.SqlDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * e37-s3 — Delta sync boundary: offline-created items caught by is_synced=0 filter.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeltaSyncBoundaryTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: SqlDriver

    @BeforeEach
    fun setup() {
        val (db, d) = com.storebook.inventoryapp.shared.test.DatabaseTestHelper.createDatabase()
        database = db
        driver = d
    }

    @AfterEach
    fun teardown() {
        com.storebook.inventoryapp.shared.test.DatabaseTestHelper.dropDatabase(driver)
    }

    @Test
    fun offline_created_item_appears_via_is_synced_0() {
        database.storeBookQueries.insertItem(
            "Chai", 50.0, "Box", 100.0, 180.0, 5.0, "Beverage", null, null, null, 5.0, 1721000000000L
        )

        val unsynced = database.storeBookQueries.getUnsyncedItems().executeAsList()
        assertEquals(1, unsynced.size, "Freshly inserted item must be unsynced")
        assertEquals("Chai", unsynced[0].name)
        assertEquals(0L, unsynced[0].is_synced)
    }

    @Test
    fun update_after_sync_resets_is_synced_0_second_run_re_picks_it_up() {
        database.storeBookQueries.insertItem(
            "Coffee", 30.0, "Box", 250.0, 400.0, 5.0, "Beverage", null, null, null, 18.0, 1721000000000L
        )

        val initialUnsynced = database.storeBookQueries.getUnsyncedItems().executeAsList()
        assertEquals(1, initialUnsynced.size)
        val localId = initialUnsynced[0].id

        database.storeBookQueries.markItemSynced("cloud-coffee", localId)

        val afterMark = database.storeBookQueries.getUnsyncedItems().executeAsList()
        assertTrue(afterMark.isEmpty(), "After markSynced, item should not be in unsynced list")

        // updateItem resets is_synced to 0 (per .sq definition)
        database.storeBookQueries.updateItem(
            name = "Coffee Premium", quantity = 35.0, unit = "Box", buy_price = 260.0,
            sell_price = 420.0, low_stock_threshold = 5.0, category = "Beverage", photo_path = null,
            barcode = null, hsn_code = null, tax_rate = 18.0, updated_at = 1721000001000L, id = localId
        )

        val reUnsynced = database.storeBookQueries.getUnsyncedItems().executeAsList()
        assertEquals(1, reUnsynced.size, "Update must reset is_synced=0 so second sync picks it up")
        assertEquals(0L, reUnsynced[0].is_synced)
    }

    @Test
    fun partial_push_failure_7_of_12_synced_next_unsynced_returns_only_remaining_5() {
        for (i in 0 until 12) {
            database.storeBookQueries.insertItem(
                "Batch Item $i", 10.0, "Pcs", 50.0, 100.0, 3.0, "Misc", null, null, null, 0.0, 1721000000000L
            )
        }

        val allUnsynced = database.storeBookQueries.getUnsyncedItems().executeAsList()
        assertEquals(12, allUnsynced.size)

        val idsToSync = allUnsynced.subList(0, 7).map { it.id }
        for (id in idsToSync) {
            database.storeBookQueries.markItemSynced("cloud-id-$id", id)
        }

        val remaining = database.storeBookQueries.getUnsyncedItems().executeAsList()
        assertEquals(5, remaining.size, "Only 5 unsynced items should remain after partial push")
    }
}
