package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.Purchases
import com.storebook.inventoryapp.shared.data.local.Purchase_items
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PurchaseRepository(
    private val database: StoreBookDatabase
) {
    private val queries = database.storeBookQueries

    suspend fun getAllPurchases(): List<Purchases> = withContext(Dispatchers.IO) {
        queries.getAllPurchases().executeAsList()
    }

    suspend fun getPurchasesByDateRange(startTs: Long, endTs: Long): List<Purchases> = withContext(Dispatchers.IO) {
        queries.getPurchasesByDateRange(startTs, endTs).executeAsList()
    }

    suspend fun insertPurchase(
        supplierId: Long, supplierName: String, totalAmount: Double,
        taxAmount: Double, type: String, notes: String?
    ): Long = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertPurchase(supplierId, supplierName, totalAmount, taxAmount, type, timestamp, notes, timestamp)
        }
        queries.getLastInsertRowId().executeAsOne()
    }

    suspend fun insertPurchaseItem(
        purchaseId: Long, itemId: Long, itemName: String, quantity: Double, unit: String, buyPrice: Double
    ) = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertPurchaseItem(purchaseId, itemId, itemName, quantity, unit, buyPrice, timestamp)
        }
    }

    // RP-A0: Push-sync methods for purchases + purchase_items
    suspend fun getUnsyncedPurchases(): List<Purchases> = withContext(Dispatchers.IO) {
        queries.getUnsyncedPurchases().executeAsList()
    }

    suspend fun markPurchaseSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markPurchaseSynced(cloudId, id)
    }

    suspend fun getUnsyncedPurchaseItems(): List<Purchase_items> = withContext(Dispatchers.IO) {
        queries.getUnsyncedPurchaseItems().executeAsList()
    }

    suspend fun markPurchaseItemSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markPurchaseItemSynced(cloudId, id)
    }

    suspend fun getPurchaseItems(purchaseId: Long): List<Purchase_items> = withContext(Dispatchers.IO) {
        queries.getPurchaseItemsByPurchaseId(purchaseId).executeAsList()
    }
}
