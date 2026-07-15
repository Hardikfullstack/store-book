package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.Items
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryRepository(
    private val database: StoreBookDatabase
) {
    private val queries = database.storeBookQueries

    suspend fun getActiveItems(): List<Items> = withContext(Dispatchers.IO) {
        queries.getActiveItems().executeAsList()
    }

    suspend fun getActiveItemsFiltered(
        searchQuery: String, category: String, sortBy: String, limit: Long, offset: Long
    ): List<Items> = withContext(Dispatchers.IO) {
        queries.getActiveItemsFiltered(searchQuery, category, sortBy, limit, offset).executeAsList()
    }

    suspend fun getItemById(id: Long): Items? = withContext(Dispatchers.IO) {
        queries.getItemById(id).executeAsOneOrNull()
    }

    // ==========================================================================
    // E02-S1: All Item CRUD operations wrapped in database.transaction{}
    //         SQLDelight TransacterImpl auto-rollback on uncaught exception inside
    //         block. Single-statement queries are already atomic at SQLite level.
    // ==========================================================================

    suspend fun insertItem(
        name: String, quantity: Double, unit: String, buyPrice: Double,
        sellPrice: Double, threshold: Double, category: String,
        photoPath: String?, hsnCode: String?, taxRate: Double
    ): Long? = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertItem(
                name, quantity, unit, buyPrice, sellPrice, threshold,
                category, photoPath, hsnCode, taxRate, timestamp
            )
        }
        queries.getLastInsertRowId().executeAsOneOrNull()
    }

    suspend fun updateItem(
        id: Long, name: String, quantity: Double, unit: String, buyPrice: Double,
        sellPrice: Double, threshold: Double, category: String,
        photoPath: String?, hsnCode: String?, taxRate: Double
    ) = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.updateItem(
                name, quantity, unit, buyPrice, sellPrice, threshold,
                category, photoPath, hsnCode, taxRate, timestamp, id
            )
        }
    }

    suspend fun updateItemStock(id: Long, addedQuantity: Double) = withContext(Dispatchers.IO) {
        database.transaction {
            val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val currentItem = queries.getItemById(id).executeAsOneOrNull()
            if (currentItem != null) {
                val newQty = currentItem.quantity + addedQuantity
                // BUG-07 FIX: Guard against negative stock — reject and throw
                if (newQty < 0) {
                    throw InsufficientStockException(
                        itemId = id,
                        itemName = currentItem.name,
                        currentQuantity = currentItem.quantity,
                        requestedChange = addedQuantity
                    )
                }
                queries.updateItemStock(newQty, currentTime, id)
            }
        }
    }

    /** BUG-07: Custom exception for insufficient stock — allows UI to show targeted toast + keep cart intact */
    data class InsufficientStockException(
        val itemId: Long,
        val itemName: String,
        val currentQuantity: Double,
        val requestedChange: Double
    ) : RuntimeException("Insufficient stock for '$itemName': have $currentQuantity, tried to change by $requestedChange")

    suspend fun softDeleteItem(id: Long) = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            // E02-S1: Soft-delete only — set isDeleted=1, never hard DELETE FROM
            queries.softDeleteItem(timestamp, timestamp, id)
        }
    }

    // ============================================================================
    // E02-S2: Low-stock alert helpers (used by ExpiryCheckWorker)
    // ============================================================================

    /** Get items currently below their low-stock threshold that haven't been alerted yet */
    suspend fun getLowStockAlertPending(): List<Items> = withContext(Dispatchers.IO) {
        queries.getLowStockAlertPending().executeAsList()
    }

    /** Mark a specific item's alert flag so it won't fire again until restocked above threshold */
    suspend fun markItemLowStockAlertSent(id: Long) = withContext(Dispatchers.IO) {
        queries.markItemLowStockAlertSent(id)
    }

    /** Reset alert flags for items that have been restocked above their threshold */
    suspend fun resetLowStockFlagsAboveThreshold() = withContext(Dispatchers.IO) {
        queries.resetLowStockAlertFlagAboveThreshold()
    }

    // ============================================================================
    // RP-A0: Push-sync methods (getUnsynced + markSynced for cloud sync)
    // ============================================================================

    suspend fun getUnsyncedItems(): List<Items> = withContext(Dispatchers.IO) {
        queries.getUnsyncedItems().executeAsList()
    }

    suspend fun markItemSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markItemSynced(cloudId, id)
    }
}
