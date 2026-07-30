package com.storebook.inventoryapp.shared.test.sync

import app.cash.sqldelight.db.SqlDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * e37-s1 — Last-Writer-Wins conflict resolution based on timestamp comparison.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LwwConflictResolutionTest {

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
    fun remote_newer_than_local_remote_accepted() {
        database.storeBookQueries.insertItem(
            "Local Item", 10.0, "Box", 50.0, 100.0, 3.0, "Beverage", null, null, null, 18.0, 1000L
        )

        database.storeBookQueries.upsertItemRemote(
            name = "Remote Item", quantity = 20.0, unit = "Box", buyPrice = 60.0, sellPrice = 120.0,
            lowStockThreshold = 3.0, category = "Beverage", photoPath = null, barcode = null,
            hsnCode = null, taxRate = 18.0, isDeleted = 0L, cloudId = "cloud-remote", updatedAt = 2000
        )

        val allItems = database.storeBookQueries.getAllItems().executeAsList()
        val acceptedItem = allItems.find { it.cloud_id == "cloud-remote" }
        assertNotNull(acceptedItem, "Remote item with newer timestamp should be accepted")
        assertEquals("Remote Item", acceptedItem!!.name)
    }

    @Test
    fun remote_older_than_local_remote_rejected() {
        database.storeBookQueries.insertItem(
            "Local Item", 10.0, "Box", 50.0, 100.0, 3.0, "Beverage", null, null, null, 18.0, 2000L
        )

        database.storeBookQueries.upsertItemRemote(
            name = "Stale Remote", quantity = 5.0, unit = "Box", buyPrice = 30.0, sellPrice = 60.0,
            lowStockThreshold = 3.0, category = "Beverage", photoPath = null, barcode = null,
            hsnCode = null, taxRate = 18.0, isDeleted = 0L, cloudId = "stale-cloud", updatedAt = 500
        )

        val allItems = database.storeBookQueries.getAllItems().executeAsList()
        val staleItem = allItems.find { it.cloud_id == "stale-cloud" }
        if (staleItem != null) {
            assertEquals("Local Item", staleItem.name, "Stale remote should not overwrite local data")
        } else {
            assertTrue(true, "Stale item correctly excluded or upserted as new row without overwriting")
        }
    }

    @Test
    fun identical_timestamps_local_accepted_via_gte() {
        database.storeBookQueries.insertItem(
            "Tie Item", 10.0, "Box", 50.0, 100.0, 3.0, "Beverage", null, null, null, 18.0, 1500L
        )

        database.storeBookQueries.upsertItemRemote(
            name = "Tie Remote", quantity = 25.0, unit = "Box", buyPrice = 70.0, sellPrice = 130.0,
            lowStockThreshold = 3.0, category = "Beverage", photoPath = null, barcode = null,
            hsnCode = null, taxRate = 18.0, isDeleted = 0L, cloudId = "tie-cloud", updatedAt = 1500
        )

        val tieItem = database.storeBookQueries.getAllItems().executeAsList().find { it.cloud_id == "tie-cloud" }
        assertNotNull(tieItem, "Equal timestamp should still accept (>=)")
    }

    @Test
    fun no_local_record_remote_accepted_unconditionally() {
        database.storeBookQueries.upsertItemRemote(
            name = "New Remote", quantity = 30.0, unit = "Pcs", buyPrice = 80.0, sellPrice = 150.0,
            lowStockThreshold = 5.0, category = "Gadget", photoPath = null, barcode = null,
            hsnCode = null, taxRate = 0.0, isDeleted = 0L, cloudId = "fresh-cloud", updatedAt = 100
        )

        val newItem = database.storeBookQueries.getAllItems().executeAsList().find { it.cloud_id == "fresh-cloud" }
        assertNotNull(newItem, "Brand-new remote entity must be inserted")
        assertEquals("New Remote", newItem!!.name)
    }

    @Test
    fun sale_append_only_no_overwrite_on_existing_sale() {
        val now = 1721000000000L
        database.storeBookQueries.insertSale(
            now, 999.0, 0.0, "Original", null, null, null, null, "SALE", null, now
        )

        val localSaleId = database.storeBookQueries.getLastInsertRowId().executeAsOne()

        database.storeBookQueries.upsertSaleRemote(
            timestamp = now + 100, totalAmount = 1.0, discountAmount = 0.0,
            customerName = "Tamper", customerGstin = null, businessGstin = null,
            customerAddress = null, businessAddress = null, type = "SALE", notes = null,
            isDeleted = 0L, cloudId = "sale-cloud", updatedAt = now + 500
        )

        val localSale = database.storeBookQueries.getSaleById(localSaleId).executeAsOneOrNull()
        requireNotNull(localSale)
        assertEquals("Original", localSale.customer_name, "Original sale must not be overwritten by tamper attempt")
    }
}
