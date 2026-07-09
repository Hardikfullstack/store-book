package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemRepository(
    private val database: StoreBookDatabase
) {
    private val queries = database.storeBookQueries

    suspend fun clearLocalDatabase() = withContext(Dispatchers.IO) {
        queries.deleteAllItems()
        queries.deleteAllSales()
        queries.deleteAllSaleItems()
        queries.deleteAllUdhaar()
        queries.deleteAllExpenses()
        queries.deleteAllSuppliers()
        queries.deleteAllPurchases()
        queries.deleteAllPurchaseItems()
        queries.deleteAllItemBatches()
    }

    suspend fun seedDummyData() = withContext(Dispatchers.IO) {
        // Just empty implementation for now
        // A complete migration would add dummy objects via specific repositories
    }
}
