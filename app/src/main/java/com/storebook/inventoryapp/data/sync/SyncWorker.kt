package com.storebook.inventoryapp.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storebook.inventoryapp.data.repository.StoreBookRepository
import com.storebook.inventoryapp.dataconnect.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.Firebase

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val storeId = inputData.getString("STORE_ID") ?: return@withContext Result.failure()
            val repository = StoreBookRepository(applicationContext, storeId)
            
            val connector = StorebookConnectorConnector.instance

            Log.d("SyncWorker", "Starting sync for store: $storeId")

            // 1. Sync Items
            val unsyncedItems = repository.getUnsyncedItems()
            for (item in unsyncedItems) {
                try {
                    val result = connector.syncItem.execute(
                        id = item.id.toString(), // Using local ID as cloud ID temporarily, or cloudId if assigned
                        storeId = storeId,
                        name = item.name,
                        quantity = item.quantity,
                        unit = item.unit,
                        buyPrice = item.buyPrice,
                        sellPrice = item.sellPrice,
                        lowStockThreshold = item.lowStockThreshold,
                        category = item.category ?: "",
                        isDeleted = item.isDeleted,
                        updatedAt = item.updatedAt.toInt(),
                    ) {
                        photoPath = item.photoPath
                        hsnCode = item.hsnCode
                    }
                    
                    val cloudId = result.data.key.id
                    repository.markItemSynced(item.id, cloudId)
                    Log.d("SyncWorker", "Synced Item: ${item.name}")
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Failed to sync item: ${item.name}", e)
                }
            }

            // 2. Sync Sales
            val unsyncedSales = repository.getUnsyncedSales()
            for (sale in unsyncedSales) {
                try {
                    val result = connector.syncSale.execute(
                        id = sale.id.toString(),
                        storeId = storeId,
                        timestamp = sale.timestamp.toInt(),
                        totalAmount = sale.totalAmount,
                        discountAmount = sale.discountAmount,
                        type = sale.type,
                        isDeleted = sale.isDeleted,
                        updatedAt = sale.updatedAt.toInt()
                    ) {
                        customerName = sale.customerName
                        customerGstin = sale.customerGstin
                        businessGstin = sale.businessGstin
                        customerAddress = sale.customerAddress
                        businessAddress = sale.businessAddress
                        notes = sale.notes
                    }
                    
                    val cloudId = result.data.key.id
                    repository.markSaleSynced(sale.id, cloudId)
                    Log.d("SyncWorker", "Synced Sale: ${sale.id}")
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Failed to sync sale: ${sale.id}", e)
                }
            }

            // 3. Sync Sale Items
            val unsyncedSaleItems = repository.getUnsyncedSaleItems()
            for (saleItem in unsyncedSaleItems) {
                try {
                    val result = connector.syncSaleItem.execute(
                        id = saleItem.id.toString(),
                        storeId = storeId,
                        saleId = saleItem.saleId.toString(),
                        itemId = saleItem.itemId.toString(),
                        itemName = saleItem.itemName,
                        unit = saleItem.unit,
                        quantity = saleItem.quantity,
                        sellPrice = saleItem.sellPrice,
                        buyPrice = saleItem.buyPrice,
                        isDeleted = saleItem.isDeleted,
                        updatedAt = saleItem.updatedAt.toInt()
                    )
                    
                    val cloudId = result.data.key.id
                    repository.markSaleItemSynced(saleItem.id, cloudId)
                    Log.d("SyncWorker", "Synced Sale Item: ${saleItem.itemName}")
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Failed to sync sale item: ${saleItem.itemName}", e)
                }
            }

            Log.d("SyncWorker", "Sync completed successfully")
            Result.success()
            
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed critically", e)
            Result.retry()
        }
    }
}
