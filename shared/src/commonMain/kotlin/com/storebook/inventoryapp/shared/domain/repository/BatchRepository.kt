package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.Item_batches
import com.storebook.inventoryapp.shared.data.local.GetNearExpiryBatches
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BatchRepository(
    private val database: StoreBookDatabase
) {
    private val queries = database.storeBookQueries

    suspend fun getBatchesForItem(itemId: Long): List<Item_batches> = withContext(Dispatchers.IO) {
        queries.getBatchesForItem(itemId).executeAsList()
    }

    suspend fun getNearExpiryBatches(expiryThresholdMs: Long): List<GetNearExpiryBatches> = withContext(Dispatchers.IO) {
        queries.getNearExpiryBatches(expiryThresholdMs).executeAsList()
    }

    // E02-S1: Transaction-wrapped insert — auto-rollback on failure
    suspend fun insertItemBatch(
        itemId: Long, batchNumber: String?, expiryDate: Long?,
        quantity: Double, costPrice: Double, notes: String?
    ): Long = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertItemBatch(itemId, batchNumber, expiryDate, quantity, costPrice, timestamp, notes, timestamp)
        }
        queries.getLastInsertRowId().executeAsOne()
    }

    // RP-A0: Push-sync methods for item_batches
    suspend fun getUnsyncedItemBatches(): List<Item_batches> = withContext(Dispatchers.IO) {
        queries.getUnsyncedItemBatches().executeAsList()
    }

    suspend fun markItemBatchSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markItemBatchSynced(cloudId, id)
    }
}
