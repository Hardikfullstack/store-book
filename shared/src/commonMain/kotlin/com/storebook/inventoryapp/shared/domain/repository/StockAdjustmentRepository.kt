package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.Stock_adjustments
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockAdjustmentRepository(
    private val database: StoreBookDatabase,
) {
    private val queries = database.storeBookQueries

    suspend fun getAllStockAdjustments(): List<Stock_adjustments> = withContext(Dispatchers.IO) {
        queries.getAllStockAdjustments().executeAsList()
    }

    suspend fun getStockAdjustmentsFiltered(
        searchQuery: String = "",
        reason: String = "",
        startDate: Long = 0L,
        endDate: Long = 0L,
        limit: Long = 50L,
        offset: Long = 0L,
    ): List<Stock_adjustments> = withContext(Dispatchers.IO) {
        queries.getStockAdjustmentsFiltered(
            search = searchQuery,
            reason = reason,
            startDate = startDate,
            endDate = endDate,
            limit = limit,
            offset = offset,
        ).executeAsList()
    }

    suspend fun getStockAdjustmentsFilteredCount(
        searchQuery: String = "",
        reason: String = "",
        startDate: Long = 0L,
        endDate: Long = 0L,
    ): Long = withContext(Dispatchers.IO) {
        queries.getStockAdjustmentsFilteredCount(
            search = searchQuery,
            reason = reason,
            startDate = startDate,
            endDate = endDate,
        ).executeAsOne()
    }

    suspend fun insertStockAdjustment(
        itemId: Long,
        itemName: String,
        reason: String,
        delta: Double,
        timestamp: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
    ): Long = withContext(Dispatchers.IO) {
        database.transactionWithResult {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertStockAdjustment(
                item_id = itemId,
                item_name = itemName,
                reason = reason,
                delta = delta,
                timestamp = timestamp,
                is_deleted = 0L,
                cloud_id = null,
                is_synced = 0L,
                updated_at = now,
            )
            queries.getLastInsertRowId().executeAsOne()
        }
    }

    suspend fun getUnsyncedStockAdjustments(): List<Stock_adjustments> = withContext(Dispatchers.IO) {
        queries.getUnsyncedStockAdjustments().executeAsList()
    }

    suspend fun markStockAdjustmentSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markStockAdjustmentSynced(cloudId, id)
    }

    suspend fun upsertStockAdjustmentRemote(
        itemId: Long,
        itemName: String,
        reason: String,
        delta: Double,
        timestamp: Long,
        isDeleted: Long,
        cloudId: String,
        updatedAt: Long,
    ) = withContext(Dispatchers.IO) {
        database.transaction {
            queries.upsertStockAdjustmentRemote(
                itemId = itemId,
                itemName = itemName,
                reason = reason,
                delta = delta,
                timestamp = timestamp,
                isDeleted = isDeleted,
                cloudId = cloudId,
                updatedAt = updatedAt,
            )
        }
    }
}
