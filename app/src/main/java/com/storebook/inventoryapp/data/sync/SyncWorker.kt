package com.storebook.inventoryapp.data.sync

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storebook.inventoryapp.data.sync.LegacySyncHelper
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

    override suspend fun doWork(): Result {
        val storeId = inputData.getString("STORE_ID") ?: return Result.failure()
        return try {
            performSync(applicationContext, storeId) { _, _ -> }
            Result.success()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.retry()
        }
    }

    companion object {
        suspend fun performSync(
            appContext: Context,
            storeId: String,
            onProgress: (Int, String) -> Unit
        ) = withContext(Dispatchers.IO) {
            val repository = LegacySyncHelper(appContext, storeId)
            val connector = StorebookConnectorConnector.instance

            if (com.storebook.inventoryapp.BuildConfig.DEBUG) Log.d("SyncWorker", "Starting sync for store: $storeId")

            // === PUSH PHASE (Local to Cloud) ===
            onProgress(10, "Pushing local changes...")

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
            onProgress(40, "Pulling cloud updates...")
            val prefs = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(appContext)
            val lastSync = prefs.getLong("last_sync_timestamp_$storeId", 0L)



            // 1. Fetch data from network outside transaction
            val itemsRes = try { connector.syncItems.execute(storeId, lastSync.toDouble()) } catch (e: Exception) { android.util.Log.e("SyncWorker", "Items failed", e); null }
            val salesRes = try { connector.syncSales.execute(storeId, lastSync.toDouble()) } catch (e: Exception) { android.util.Log.e("SyncWorker", "Sales failed", e); null }
            val saleItemsRes = try { connector.syncSaleItems.execute(storeId, lastSync.toDouble()) } catch (e: Exception) { android.util.Log.e("SyncWorker", "SaleItems failed", e); null }
            val udhaarsRes = try { connector.syncUdhaars.execute(storeId, lastSync.toDouble()) } catch (e: Exception) { android.util.Log.e("SyncWorker", "Udhaars failed", e); null }
            val expensesRes = try { connector.syncExpenses.execute(storeId, lastSync.toDouble()) } catch (e: Exception) { android.util.Log.e("SyncWorker", "Expenses failed", e); null }
            val suppliersRes = try { connector.syncSuppliers.execute(storeId, lastSync.toDouble()) } catch (e: Exception) { android.util.Log.e("SyncWorker", "Suppliers failed", e); null }
            val purchasesRes = try { connector.syncPurchases.execute(storeId, lastSync.toDouble()) } catch (e: Exception) { android.util.Log.e("SyncWorker", "Purchases failed", e); null }
            val purchaseItemsRes = try { connector.syncPurchaseItems.execute(storeId, lastSync.toDouble()) } catch (e: Exception) { android.util.Log.e("SyncWorker", "PurchaseItems failed", e); null }
            val batchesRes = try { connector.syncItemBatches.execute(storeId, lastSync.toDouble()) } catch (e: Exception) { android.util.Log.e("SyncWorker", "Batches failed", e); null }

            val db = repository.dbHelper.writableDatabase
            db.beginTransaction()
            try {
                // 2. Insert Items
                itemsRes?.data?.items?.let { items ->
                    android.util.Log.d("SyncWorker", "Items fetched from cloud: ${items.size}")
                    for (item in items) {
                        val cv = ContentValues().apply {
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
                        }
                        upsertEntity(db, "items", item.id, cv, "name", item.name)
                    }
                }

                // 3. Pull Sales
                salesRes?.data?.sales?.let { sales ->
                    for (sale in sales) {
                        val cv = ContentValues().apply {
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
                        }
                        upsertEntity(db, "sales", sale.id, cv)
                    }
                }

                // 4. Pull Sale Items
                saleItemsRes?.data?.saleItemDetails?.let { saleItemDetails ->
                    for (si in saleItemDetails) {
                        val localSaleId = resolveLocalId(db, "sales", si.saleId)
                        val localItemId = resolveLocalId(db, "items", si.itemId)
                        val cv = ContentValues().apply {
                            put("sale_id", localSaleId)
                            put("item_id", localItemId)
                            put("item_name", si.itemName)
                            put("quantity", si.quantity)
                            put("unit", si.unit)
                            put("buy_price", si.buyPrice)
                            put("sell_price", si.sellPrice)
                            put("is_deleted", if (si.isDeleted) 1 else 0)
                            put("updated_at", si.updatedAt.toLong())
                        }
                        upsertEntity(db, "sale_items", si.id, cv)
                    }
                }

                // 5. Pull Udhaars
                udhaarsRes?.data?.udhaarEntries?.let { udhaarEntries ->
                    for (u in udhaarEntries) {
                        val cv = ContentValues().apply {
                            put("customer_name", u.customerName)
                            put("amount", u.amount)
                            put("type", u.type)
                            put("timestamp", u.timestamp.toLong())
                            put("notes", u.notes)
                            put("is_deleted", if (u.isDeleted) 1 else 0)
                            put("updated_at", u.updatedAt.toLong())
                        }
                        upsertEntity(db, "udhaar", u.id, cv)
                    }
                }

                // 6. Pull Expenses
                expensesRes?.data?.expenseEntries?.let { expenseEntries ->
                    for (e in expenseEntries) {
                        val cv = ContentValues().apply {
                            put("type", e.type)
                            put("description", e.description)
                            put("amount", e.amount)
                            put("timestamp", e.timestamp.toLong())
                            put("supplier_name", e.supplierName)
                            put("supplier_phone", e.supplierPhone)
                            put("is_deleted", if (e.isDeleted) 1 else 0)
                            put("updated_at", e.updatedAt.toLong())
                        }
                        upsertEntity(db, "expenses", e.id, cv)
                    }
                }

                // 7. Pull Suppliers
                suppliersRes?.data?.suppliers?.let { suppliers ->
                    for (s in suppliers) {
                        val cv = ContentValues().apply {
                            put("name", s.name)
                            put("phone", s.phone)
                            put("gstin", s.gstin)
                            put("address", s.address)
                            put("is_deleted", if (s.isDeleted) 1 else 0)
                            put("updated_at", s.updatedAt.toLong())
                        }
                        upsertEntity(db, "suppliers", s.id, cv, "name", s.name)
                    }
                }

                // 8. Pull Purchases
                purchasesRes?.data?.purchases?.let { purchases ->
                    for (p in purchases) {
                        val localSupplierId = resolveLocalId(db, "suppliers", p.supplierId)
                        val cv = ContentValues().apply {
                            put("supplier_id", localSupplierId)
                            put("supplier_name", p.supplierName)
                            put("total_amount", p.totalAmount)
                            put("tax_amount", p.taxAmount)
                            put("type", p.type)
                            put("timestamp", p.timestamp.toLong())
                            put("notes", p.notes)
                            put("is_deleted", if (p.isDeleted) 1 else 0)
                            put("updated_at", p.updatedAt.toLong())
                        }
                        upsertEntity(db, "purchases", p.id, cv)
                    }
                }

                // 9. Pull Purchase Items
                purchaseItemsRes?.data?.purchaseItemDetails?.let { purchaseItemDetails ->
                    for (pi in purchaseItemDetails) {
                        val localPurchaseId = resolveLocalId(db, "purchases", pi.purchaseId)
                        val localItemId = resolveLocalId(db, "items", pi.itemId)
                        val cv = ContentValues().apply {
                            put("purchase_id", localPurchaseId)
                            put("item_id", localItemId)
                            put("item_name", pi.itemName)
                            put("quantity", pi.quantity)
                            put("unit", pi.unit)
                            put("buy_price", pi.buyPrice)
                            put("is_deleted", if (pi.isDeleted) 1 else 0)
                            put("updated_at", pi.updatedAt.toLong())
                        }
                        upsertEntity(db, "purchase_items", pi.id, cv)
                    }
                }

                // 10. Pull Item Batches
                batchesRes?.data?.itemBatches?.let { itemBatches ->
                    for (b in itemBatches) {
                        val localItemId = resolveLocalId(db, "items", b.itemId)
                        val cv = ContentValues().apply {
                            put("item_id", localItemId)
                            put("batch_number", b.batchNumber)
                            put("expiry_date", b.expiryDate?.toLong())
                            put("quantity", b.quantity)
                            put("cost_price", b.costPrice)
                            put("timestamp", b.timestamp.toLong())
                            put("notes", b.notes)
                            put("is_deleted", if (b.isDeleted) 1 else 0)
                            put("updated_at", b.updatedAt.toLong())
                        }
                        upsertEntity(db, "item_batches", b.id, cv)
                    }
                }
                db.setTransactionSuccessful()
                prefs.edit().putLong("last_sync_timestamp_$storeId", System.currentTimeMillis()).apply()
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
            } catch (e: Exception) { android.util.Log.e("SyncWorker", "Error in pull phase", e) }

            // Check items in DB
            val c = repository.dbHelper.readableDatabase.rawQuery("SELECT COUNT(*) FROM items", null)
            if (c.moveToFirst()) {
                android.util.Log.d("SyncWorker", "Items in local DB after sync: ${c.getInt(0)}")
            }
            c.close()

            onProgress(100, "Sync complete!")
        }

        private fun resolveLocalId(db: android.database.sqlite.SQLiteDatabase, table: String, cloudId: String): Long {
            val asLong = cloudId.toLongOrNull()
            if (asLong != null) return asLong
            var localId = 0L
            db.rawQuery("SELECT id FROM $table WHERE cloud_id = ?", arrayOf(cloudId)).use {
                if (it.moveToFirst()) localId = it.getLong(0)
            }
            return localId
        }

        private fun upsertEntity(
            db: android.database.sqlite.SQLiteDatabase, table: String, cloudId: String, cv: android.content.ContentValues,
            uniqueColumn: String? = null, uniqueValue: String? = null
        ) {
            var localId = resolveLocalId(db, table, cloudId)
            if (localId == 0L && uniqueColumn != null && uniqueValue != null) {
                db.rawQuery("SELECT id FROM $table WHERE $uniqueColumn = ?", arrayOf(uniqueValue)).use {
                    if (it.moveToFirst()) localId = it.getLong(0)
                }
            }
            cv.put("cloud_id", cloudId)
            cv.put("is_synced", 1)
            if (localId != 0L) {
                val rows = db.update(table, cv, "id = ?", arrayOf(localId.toString()))
                if (rows == 0) {
                    cv.put("id", localId)
                    db.insertWithOnConflict(table, null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                }
            } else {
                db.insert(table, null, cv)
            }
        }
}
}
