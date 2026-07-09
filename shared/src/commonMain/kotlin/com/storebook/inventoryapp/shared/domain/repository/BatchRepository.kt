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

    suspend fun insertItemBatch(
        itemId: Long, batchNumber: String?, expiryDate: Long?, 
        quantity: Double, costPrice: Double, notes: String?
    ): Long = withContext(Dispatchers.IO) {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        queries.insertItemBatch(itemId, batchNumber, expiryDate, quantity, costPrice, timestamp, notes, timestamp)
        queries.getLastInsertRowId().executeAsOne()
    }
}
