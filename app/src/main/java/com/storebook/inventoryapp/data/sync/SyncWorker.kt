package com.storebook.inventoryapp.data.sync

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storebook.inventoryapp.data.repository.StoreBookRepository
import com.storebook.inventoryapp.dataconnect.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import com.google.firebase.Firebase
import com.storebook.inventoryapp.utils.StringUtils

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val storeId = inputData.getString("STORE_ID") ?: return@withContext Result.failure()
            val repository = StoreBookRepository(applicationContext, storeId)
            val connector = StorebookConnectorConnector.instance

            if (com.storebook.inventoryapp.BuildConfig.DEBUG) Log.d("SyncWorker", "Starting sync for store: $storeId")

            // === PUSH PHASE (Local to Cloud) ===

            val unsyncedItems = repository.getUnsyncedItems()
            for (item in unsyncedItems) {
                try {
                    val result = connector.syncItem.execute(
                        id = item.id.toString(), storeId = storeId, name = StringUtils.sanitize(item.name),
                        quantity = item.quantity, unit = item.unit, buyPrice = item.buyPrice, sellPrice = item.sellPrice,
                        lowStockThreshold = item.lowStockThreshold, category = item.category ?: "", isDeleted = item.isDeleted,
                        updatedAt = item.updatedAt.toDouble()
                    ) { photoPath = item.photoPath; hsnCode = item.hsnCode }
                    repository.markItemSynced(item.id, result.data.key.id)
                } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }
            }

            val unsyncedSales = repository.getUnsyncedSales()
            for (sale in unsyncedSales) {
                try {
                    val result = connector.syncSale.execute(
                        id = sale.id.toString(), storeId = storeId, timestamp = sale.timestamp.toDouble(),
                        totalAmount = sale.totalAmount, discountAmount = sale.discountAmount, type = sale.type,
                        isDeleted = sale.isDeleted, updatedAt = sale.updatedAt.toDouble()
                    ) {
                        customerName = StringUtils.sanitize(sale.customerName)
                        customerGstin = StringUtils.sanitize(sale.customerGstin)
                        businessGstin = StringUtils.sanitize(sale.businessGstin)
                        customerAddress = StringUtils.sanitize(sale.customerAddress)
                        businessAddress = StringUtils.sanitize(sale.businessAddress)
                        notes = StringUtils.sanitize(sale.notes)
                    }
                    repository.markSaleSynced(sale.id, result.data.key.id)
                } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }
            }

            val unsyncedSaleItems = repository.getUnsyncedSaleItems()
            for (saleItem in unsyncedSaleItems) {
                try {
                    val result = connector.syncSaleItem.execute(
                        id = saleItem.id.toString(), storeId = storeId, saleId = saleItem.saleId.toString(),
                        itemId = saleItem.itemId.toString(), itemName = StringUtils.sanitize(saleItem.itemName),
                        unit = saleItem.unit, quantity = saleItem.quantity, sellPrice = saleItem.sellPrice,
                        buyPrice = saleItem.buyPrice, isDeleted = saleItem.isDeleted, updatedAt = saleItem.updatedAt.toDouble()
                    )
                    repository.markSaleItemSynced(saleItem.id, result.data.key.id)
                } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }
            }

            val unsyncedUdhaars = repository.getUnsyncedUdhaars()
            for (udhaar in unsyncedUdhaars) {
                try {
                    val result = connector.syncUdhaar.execute(
                        id = udhaar.id.toString(), storeId = storeId, customerName = StringUtils.sanitize(udhaar.customerName),
                        amount = udhaar.amount, type = udhaar.type, timestamp = udhaar.timestamp.toDouble(),
                        isDeleted = udhaar.isDeleted, updatedAt = udhaar.updatedAt.toDouble()
                    ) { notes = StringUtils.sanitize(udhaar.notes) }
                    repository.markUdhaarSynced(udhaar.id, result.data.key.id)
                } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }
            }

            val unsyncedExpenses = repository.getUnsyncedExpenses()
            for (expense in unsyncedExpenses) {
                try {
                    val result = connector.syncExpense.execute(
                        id = expense.id.toString(), storeId = storeId, type = expense.type,
                        description = StringUtils.sanitize(expense.description), amount = expense.amount,
                        timestamp = expense.timestamp.toDouble(), isDeleted = expense.isDeleted, updatedAt = expense.updatedAt.toDouble()
                    ) {
                        supplierName = StringUtils.sanitize(expense.supplierName)
                        supplierPhone = StringUtils.sanitize(expense.supplierPhone)
                    }
                    repository.markExpenseSynced(expense.id, result.data.key.id)
                } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }
            }

            val unsyncedSuppliers = repository.getUnsyncedSuppliers()
            for (supplier in unsyncedSuppliers) {
                try {
                    val result = connector.syncSupplier.execute(
                        id = supplier.id.toString(), storeId = storeId, name = StringUtils.sanitize(supplier.name),
                        isDeleted = supplier.isDeleted, updatedAt = supplier.updatedAt.toDouble()
                    ) {
                        phone = StringUtils.sanitize(supplier.phone)
                        gstin = StringUtils.sanitize(supplier.gstin)
                        address = StringUtils.sanitize(supplier.address)
                    }
                    repository.markSupplierSynced(supplier.id, result.data.key.id)
                } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }
            }

            val unsyncedPurchases = repository.getUnsyncedPurchases()
            for (purchase in unsyncedPurchases) {
                try {
                    val result = connector.syncPurchase.execute(
                        id = purchase.id.toString(), storeId = storeId, supplierId = purchase.supplierId.toString(),
                        supplierName = StringUtils.sanitize(purchase.supplierName), totalAmount = purchase.totalAmount,
                        taxAmount = purchase.taxAmount, type = purchase.type, timestamp = purchase.timestamp.toDouble(),
                        isDeleted = purchase.isDeleted, updatedAt = purchase.updatedAt.toDouble()
                    ) { notes = StringUtils.sanitize(purchase.notes) }
                    repository.markPurchaseSynced(purchase.id, result.data.key.id)
                } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }
            }

            val unsyncedPurchaseItems = repository.getUnsyncedPurchaseItems()
            for (pi in unsyncedPurchaseItems) {
                try {
                    val result = connector.syncPurchaseItem.execute(
                        id = pi.id.toString(), storeId = storeId, purchaseId = pi.purchaseId.toString(),
                        itemId = pi.itemId.toString(), itemName = StringUtils.sanitize(pi.itemName),
                        quantity = pi.quantity, unit = pi.unit, buyPrice = pi.buyPrice,
                        isDeleted = pi.isDeleted, updatedAt = pi.updatedAt.toDouble()
                    )
                    repository.markPurchaseItemSynced(pi.id, result.data.key.id)
                } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e }
            }

            // === PULL PHASE (Cloud to Local) ===
            val prefs = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(applicationContext)
            val lastSync = prefs.getLong("last_sync_timestamp", 0L)
            
            val db = repository.dbHelper.writableDatabase
            db.beginTransaction()
            try {
                // 1. Pull Items
                val itemsRes = connector.syncItems.execute(storeId, lastSync.toDouble())
                for (item in itemsRes.data.items) {
                    val pId = item.id.toLongOrNull(); if (pId == null) continue
                    val cv = ContentValues().apply {
                        put("id", pId)
                        put("name", item.name)
                        put("quantity", item.quantity)
                        put("unit", item.unit)
                        put("buy_price", item.buyPrice)
                        put("sell_price", item.sellPrice)
                        put("low_stock_threshold", item.lowStockThreshold)
                        put("category", item.category)
                        put("photo_path", item.photoPath)
                        put("hsn_code", item.hsnCode)
                        put("is_deleted", if (item.isDeleted) 1 else 0)
                        put("updated_at", item.updatedAt.toLong())
                        put("is_synced", 1)
                    }
                    db.insertWithOnConflict("items", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }

                // 2. Pull Sales
                val salesRes = connector.syncSales.execute(storeId, lastSync.toDouble())
                for (sale in salesRes.data.sales) {
                    val pId = sale.id.toLongOrNull(); if (pId == null) continue
                    val cv = ContentValues().apply {
                        put("id", pId)
                        put("timestamp", sale.timestamp.toLong())
                        put("total_amount", sale.totalAmount)
                        put("discount_amount", sale.discountAmount)
                        put("type", sale.type)
                        put("customer_name", sale.customerName)
                        put("customer_gstin", sale.customerGstin)
                        put("business_gstin", sale.businessGstin)
                        put("customer_address", sale.customerAddress)
                        put("business_address", sale.businessAddress)
                        put("notes", sale.notes)
                        put("is_deleted", if (sale.isDeleted) 1 else 0)
                        put("updated_at", sale.updatedAt.toLong())
                        put("is_synced", 1)
                    }
                    db.insertWithOnConflict("sales", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                
                // 3. Pull Sale Items
                val saleItemsRes = connector.syncSaleItems.execute(storeId, lastSync.toDouble())
                for (si in saleItemsRes.data.saleItemDetails) {
                    val pId = si.id.toLongOrNull(); if (pId == null) continue
                    val cv = ContentValues().apply {
                        put("id", pId)
                        put("sale_id", si.saleId.toLongOrNull() ?: 0L)
                        put("item_id", si.itemId.toLongOrNull() ?: 0L)
                        put("item_name", si.itemName)
                        put("quantity", si.quantity)
                        put("unit", si.unit)
                        put("buy_price", si.buyPrice)
                        put("sell_price", si.sellPrice)
                        put("is_deleted", if (si.isDeleted) 1 else 0)
                        put("updated_at", si.updatedAt.toLong())
                        put("is_synced", 1)
                    }
                    db.insertWithOnConflict("sale_items", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                
                // 4. Pull Udhaars
                val udhaarsRes = connector.syncUdhaars.execute(storeId, lastSync.toDouble())
                for (u in udhaarsRes.data.udhaarEntries) {
                    val pId = u.id.toLongOrNull(); if (pId == null) continue
                    val cv = ContentValues().apply {
                        put("id", pId)
                        put("customer_name", u.customerName)
                        put("amount", u.amount)
                        put("type", u.type)
                        put("timestamp", u.timestamp.toLong())
                        put("notes", u.notes)
                        put("is_deleted", if (u.isDeleted) 1 else 0)
                        put("updated_at", u.updatedAt.toLong())
                        put("is_synced", 1)
                    }
                    db.insertWithOnConflict("udhaar", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }

                // 5. Pull Expenses
                val expensesRes = connector.syncExpenses.execute(storeId, lastSync.toDouble())
                for (e in expensesRes.data.expenseEntries) {
                    val pId = e.id.toLongOrNull(); if (pId == null) continue
                    val cv = ContentValues().apply {
                        put("id", pId)
                        put("type", e.type)
                        put("description", e.description)
                        put("amount", e.amount)
                        put("timestamp", e.timestamp.toLong())
                        put("supplier", e.supplierName)
                        put("phone", e.supplierPhone)
                        put("is_deleted", if (e.isDeleted) 1 else 0)
                        put("updated_at", e.updatedAt.toLong())
                        put("is_synced", 1)
                    }
                    db.insertWithOnConflict("expenses", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                
                // 6. Pull Suppliers
                val suppliersRes = connector.syncSuppliers.execute(storeId, lastSync.toDouble())
                for (s in suppliersRes.data.suppliers) {
                    val pId = s.id.toLongOrNull(); if (pId == null) continue
                    val cv = ContentValues().apply {
                        put("id", pId)
                        put("name", s.name)
                        put("phone", s.phone)
                        put("gstin", s.gstin)
                        put("address", s.address)
                        put("is_deleted", if (s.isDeleted) 1 else 0)
                        put("updated_at", s.updatedAt.toLong())
                        put("is_synced", 1)
                    }
                    db.insertWithOnConflict("suppliers", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                
                // 7. Pull Purchases
                val purchasesRes = connector.syncPurchases.execute(storeId, lastSync.toDouble())
                for (p in purchasesRes.data.purchases) {
                    val pId = p.id.toLongOrNull(); if (pId == null) continue
                    val cv = ContentValues().apply {
                        put("id", pId)
                        put("supplier_id", p.supplierId.toLongOrNull() ?: 0L)
                        put("supplier_name", p.supplierName)
                        put("total_amount", p.totalAmount)
                        put("tax_amount", p.taxAmount)
                        put("type", p.type)
                        put("timestamp", p.timestamp.toLong())
                        put("notes", p.notes)
                        put("is_deleted", if (p.isDeleted) 1 else 0)
                        put("updated_at", p.updatedAt.toLong())
                        put("is_synced", 1)
                    }
                    db.insertWithOnConflict("purchases", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                
                // 8. Pull Purchase Items
                val purchaseItemsRes = connector.syncPurchaseItems.execute(storeId, lastSync.toDouble())
                for (pi in purchaseItemsRes.data.purchaseItemDetails) {
                    val pId = pi.id.toLongOrNull(); if (pId == null) continue
                    val cv = ContentValues().apply {
                        put("id", pId)
                        put("purchase_id", pi.purchaseId.toLongOrNull() ?: 0L)
                        put("item_id", pi.itemId.toLongOrNull() ?: 0L)
                        put("item_name", pi.itemName)
                        put("quantity", pi.quantity)
                        put("unit", pi.unit)
                        put("buy_price", pi.buyPrice)
                        put("is_deleted", if (pi.isDeleted) 1 else 0)
                        put("updated_at", pi.updatedAt.toLong())
                        put("is_synced", 1)
                    }
                    db.insertWithOnConflict("purchase_items", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                
                // 9. Pull Item Batches
                val batchesRes = connector.syncItemBatches.execute(storeId, lastSync.toDouble())
                for (b in batchesRes.data.itemBatches) {
                    val pId = b.id.toLongOrNull(); if (pId == null) continue
                    val cv = ContentValues().apply {
                        put("id", pId)
                        put("item_id", b.itemId.toLongOrNull() ?: 0L)
                        put("batch_number", b.batchNumber)
                        put("expiry_date", b.expiryDate?.toLong())
                        put("quantity", b.quantity)
                        put("cost_price", b.costPrice)
                        put("timestamp", b.timestamp.toLong())
                        put("notes", b.notes)
                        put("is_deleted", if (b.isDeleted) 1 else 0)
                        put("updated_at", b.updatedAt.toLong())
                        put("is_synced", 1)
                    }
                    db.insertWithOnConflict("item_batches", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }

                db.setTransactionSuccessful()
                prefs.edit().putLong("last_sync_timestamp", System.currentTimeMillis()).apply()
            } catch (e: Exception) {
                if (com.storebook.inventoryapp.BuildConfig.DEBUG) Log.e("SyncWorker", "Failed to pull data", e)
            } finally {
                db.endTransaction()
            }

            if (com.storebook.inventoryapp.BuildConfig.DEBUG) Log.d("SyncWorker", "Sync completed successfully")
            
            try {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("store_updates")
                    .child(storeId)
                    .child("last_update")
                    .setValue(System.currentTimeMillis())
            } catch (e: Exception) { }
            
            Result.success()
            
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.retry()
        }
    }
}
