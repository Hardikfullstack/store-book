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

    suspend fun insertItem(
        name: String, quantity: Double, unit: String, buyPrice: Double, 
        sellPrice: Double, threshold: Double, category: String, 
        photoPath: String?, hsnCode: String?, taxRate: Double
    ) = withContext(Dispatchers.IO) {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        queries.insertItem(
            name, quantity, unit, buyPrice, sellPrice, threshold, 
            category, photoPath, hsnCode, taxRate, timestamp
        )
        queries.getLastInsertRowId().executeAsOne()
    }

    suspend fun updateItem(
        id: Long, name: String, quantity: Double, unit: String, buyPrice: Double, 
        sellPrice: Double, threshold: Double, category: String, 
        photoPath: String?, hsnCode: String?, taxRate: Double
    ) = withContext(Dispatchers.IO) {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        queries.updateItem(
            name, quantity, unit, buyPrice, sellPrice, threshold, 
            category, photoPath, hsnCode, taxRate, timestamp, id
        )
    }

    suspend fun updateItemStock(id: Long, addedQuantity: Double) = withContext(Dispatchers.IO) {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val currentItem = queries.getItemById(id).executeAsOneOrNull()
        if (currentItem != null) {
            val newQty = currentItem.quantity + addedQuantity
            queries.updateItemStock(newQty, timestamp, id)
        }
    }

    suspend fun softDeleteItem(id: Long) = withContext(Dispatchers.IO) {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        queries.softDeleteItem(timestamp, timestamp, id)
    }
}
