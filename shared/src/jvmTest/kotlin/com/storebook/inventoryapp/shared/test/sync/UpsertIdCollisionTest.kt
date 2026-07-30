package com.storebook.inventoryapp.shared.test.sync

import app.cash.sqldelight.db.SqlDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * e37-s2 — INSERT OR REPLACE upsert must not overwrite unrelated entities on ID collision.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpsertIdCollisionTest {

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
    fun two_distinct_cloud_ids_upsert_as_separate_rows_no_overwrite() {
        database.storeBookQueries.upsertItemRemote(
            cloudId = "cloud-A", name = "Tea A", quantity = 10.0, unit = "Box",
            buyPrice = 50.0, sellPrice = 100.0, lowStockThreshold = 3.0, category = "Beverage",
            photoPath = null, barcode = null, hsnCode = null, taxRate = 18.0, isDeleted = 0L, updatedAt = 1000
        )

        database.storeBookQueries.upsertItemRemote(
            cloudId = "cloud-B", name = "Coffee B", quantity = 20.0, unit = "Pack",
            buyPrice = 80.0, sellPrice = 150.0, lowStockThreshold = 5.0, category = "Beverage",
            photoPath = null, barcode = null, hsnCode = null, taxRate = 5.0, isDeleted = 0L, updatedAt = 2000
        )

        val allItems = database.storeBookQueries.getAllItems().executeAsList()
        assertEquals(2, allItems.size, "Must have exactly 2 distinct items")

        val itemA = allItems.find { it.cloud_id == "cloud-A" }
        assertNotNull(itemA, "cloud-A must exist after both upserts")
        assertEquals("Tea A", itemA!!.name)

        val itemB = allItems.find { it.cloud_id == "cloud-B" }
        assertNotNull(itemB, "cloud-B must exist after both upserts")
        assertEquals("Coffee B", itemB!!.name)
    }

    @Test
    fun second_upsert_with_same_cloud_id_updates_existing_row_no_duplicate() {
        database.storeBookQueries.upsertItemRemote(
            cloudId = "same-cloud", name = "Tea Original", quantity = 10.0, unit = "Box",
            buyPrice = 50.0, sellPrice = 100.0, lowStockThreshold = 3.0, category = "Beverage",
            photoPath = null, barcode = null, hsnCode = null, taxRate = 18.0, isDeleted = 0L, updatedAt = 1000
        )

        database.storeBookQueries.upsertItemRemote(
            cloudId = "same-cloud", name = "Tea Updated", quantity = 25.0, unit = "Box",
            buyPrice = 55.0, sellPrice = 110.0, lowStockThreshold = 3.0, category = "Beverage",
            photoPath = null, barcode = null, hsnCode = null, taxRate = 18.0, isDeleted = 0L, updatedAt = 2000
        )

        val allItems = database.storeBookQueries.getAllItems().executeAsList()
        assertEquals(1, allItems.size, "Must have exactly 1 item - upsert updated existing row")
        assertEquals("Tea Updated", allItems[0].name)
        assertEquals(25.0, allItems[0].quantity)
    }

    @Test
    fun new_cloud_id_does_not_match_or_corrupt_existing_rows() {
        database.storeBookQueries.insertItem(
            "Existing Item", 100.0, "Kg", 40.0, 80.0, 5.0, "Food", null, null, null, 5.0, 500L
        )

        database.storeBookQueries.upsertItemRemote(
            cloudId = "far-away-id", name = "New Remote", quantity = 5.0, unit = "Pack",
            buyPrice = 10.0, sellPrice = 20.0, lowStockThreshold = 1.0, category = "Misc",
            photoPath = null, barcode = null, hsnCode = null, taxRate = 0.0, isDeleted = 0L, updatedAt = 999
        )

        val allItems = database.storeBookQueries.getAllItems().executeAsList()
        assertEquals(2, allItems.size, "Original item must still exist, remote added separately")

        val orig = allItems.find { it.cloud_id == null }
        assertNotNull(orig, "Existing item without cloud_id must survive upsert")
        assertEquals("Existing Item", orig!!.name)
    }
}
