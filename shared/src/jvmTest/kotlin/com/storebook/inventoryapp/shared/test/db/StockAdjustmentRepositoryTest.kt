package com.storebook.inventoryapp.shared.test.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.repository.StockAdjustmentRepository
import com.storebook.inventoryapp.shared.test.DatabaseTestHelper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StockAdjustmentRepositoryTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: JdbcSqliteDriver
    private lateinit var repository: StockAdjustmentRepository

    @BeforeEach
    fun setup() {
        val (db, d) = DatabaseTestHelper.createDatabase()
        database = db
        driver = d as JdbcSqliteDriver
        repository = StockAdjustmentRepository(database)
    }

    @Test
    fun `insertStockAdjustment logs adjustment with is_synced 0 and correct delta`() = runBlocking {
        val id = repository.insertStockAdjustment(
            itemId = 101L,
            itemName = "Basmati Rice 5kg",
            reason = "Count Correction",
            delta = -2.0,
        )

        assertTrue(id > 0)
        val all = repository.getAllStockAdjustments()
        assertEquals(1, all.size)
        val entry = all[0]
        assertEquals("Basmati Rice 5kg", entry.item_name)
        assertEquals("Count Correction", entry.reason)
        assertEquals(-2.0, entry.delta)
        assertEquals(0L, entry.is_synced)
    }

    @Test
    fun `getStockAdjustmentsFiltered filters by search, reason and date range`() = runBlocking {
        val t1 = 1000L
        val t2 = 2000L
        val t3 = 3000L

        database.storeBookQueries.insertStockAdjustment(1L, "Sugar 1kg", "Damage", -3.0, t1, 0, null, 0, t1)
        database.storeBookQueries.insertStockAdjustment(2L, "Wheat Flour", "Restock", 10.0, t2, 0, null, 0, t2)
        database.storeBookQueries.insertStockAdjustment(3L, "Sugar Brown", "Expiry", -1.0, t3, 0, null, 0, t3)

        // Filter by search "Sugar"
        val sugarResults = repository.getStockAdjustmentsFiltered(searchQuery = "Sugar", limit = 10, offset = 0)
        assertEquals(2, sugarResults.size)

        // Filter by reason "Restock"
        val restockResults = repository.getStockAdjustmentsFiltered(reason = "Restock", limit = 10, offset = 0)
        assertEquals(1, restockResults.size)
        assertEquals("Wheat Flour", restockResults[0].item_name)

        // Filter by date range t1..t2
        val dateResults = repository.getStockAdjustmentsFiltered(startDate = t1, endDate = t2, limit = 10, offset = 0)
        assertEquals(2, dateResults.size)

        // Total count matches filtered
        val count = repository.getStockAdjustmentsFilteredCount(searchQuery = "Sugar")
        assertEquals(2L, count)
    }

    @Test
    fun `markStockAdjustmentSynced updates sync flag and cloudId`() = runBlocking {
        val id = repository.insertStockAdjustment(
            itemId = 10L,
            itemName = "Milk 1L",
            reason = "Loss",
            delta = -1.0,
        )

        val unsyncedBefore = repository.getUnsyncedStockAdjustments()
        assertEquals(1, unsyncedBefore.size)

        repository.markStockAdjustmentSynced(id, "cloud-sa-123")

        val unsyncedAfter = repository.getUnsyncedStockAdjustments()
        assertEquals(0, unsyncedAfter.size)

        val all = repository.getAllStockAdjustments()
        assertEquals("cloud-sa-123", all[0].cloud_id)
        assertEquals(1L, all[0].is_synced)
    }

    @Test
    fun `upsertStockAdjustmentRemote handles inserts and updates idempotently`() = runBlocking {
        repository.upsertStockAdjustmentRemote(
            itemId = 42L,
            itemName = "Green Tea",
            reason = "Restock",
            delta = 20.0,
            timestamp = 5000L,
            isDeleted = 0L,
            cloudId = "cloud-remote-999",
            updatedAt = 5000L,
        )

        val all = repository.getAllStockAdjustments()
        assertEquals(1, all.size)
        assertEquals("Green Tea", all[0].item_name)
        assertEquals(20.0, all[0].delta)
        assertEquals("cloud-remote-999", all[0].cloud_id)

        // Remote update with changed delta
        repository.upsertStockAdjustmentRemote(
            itemId = 42L,
            itemName = "Green Tea",
            reason = "Restock",
            delta = 25.0,
            timestamp = 5000L,
            isDeleted = 0L,
            cloudId = "cloud-remote-999",
            updatedAt = 6000L,
        )

        val allAfter = repository.getAllStockAdjustments()
        assertEquals(1, allAfter.size)
        assertEquals(25.0, allAfter[0].delta)
    }
}
