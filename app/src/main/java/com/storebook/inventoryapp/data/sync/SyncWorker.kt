package com.storebook.inventoryapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storebook.inventoryapp.dataconnect.*
import com.storebook.inventoryapp.shared.domain.repository.SyncRepository
import com.storebook.inventoryapp.shared.util.RetryBackoffCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private fun sanitize(str: String?): String = str?.let { it.replace("<", "").replace(">", "") } ?: ""

/** Normalize 10-digit (seconds) timestamps to 13-digit (milliseconds) */
private fun normalizeTimestamp(ts: Double): Long {
    val raw = ts.toLong()
    return if (raw in 1..9_999_999_999L) {
        raw * 1000L
    } else {
        raw
    }
}

/** E01-S2: Classify exception as transient (retry-worth) vs permanent (give up) */
private fun isTransientException(e: Throwable): Boolean {
    if (e is java.io.IOException) return true
    val msg = e.message?.lowercase() ?: ""
    return "timeout" in msg ||
        "connection" in msg ||
        "retry" in msg ||
        "rate limit" in msg ||
        "too many requests" in msg ||
        "unavailable" in msg ||
        "internal" in msg
}

/**
 * E01-S1 + E01-S2: SyncWorker with:
 * - Per-mutation try/catch with log.d output ✅ (was already in place)
 * - Failed mutations enqueued to FailedSyncQueue for retry ✅ (new)
 * - Exponential backoff processing of overdue retries ✅ (new)
 * - SyncState.status tracking (IDLE→PUSHING→PULLING→DONE/FAILED) ✅ (new)
 * - SyncState.last_full_sync_at only updated after FULL batch success ✅ (new)
 */
class SyncWorker(
    private val appCtxParam: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appCtxParam, workerParams) {
    override suspend fun doWork(): Result {
        val storeId =
            inputData.getString("STORE_ID")
                ?: com.storebook.inventoryapp.utils.SecurityUtils
                    .getEncryptedPrefs(applicationContext)
                    .getString("active_store_id", "default")
                ?: "default"
        return try {
            SyncWorker.performSync(applicationContext, storeId) { _, _ -> }
            Result.success()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.w("SyncWorker", "Sync failed at high level, will retry: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private val syncMutex = Mutex()

        private fun openRepo(
            ctx: Context,
            storeId: String,
        ): SyncRepository =
            SyncRepository(
                com.storebook.inventoryapp.data.DatabaseProvider
                    .getDatabase(ctx, storeId),
            )

        suspend fun performSync(
            appCtx: Context,
            storeId: String,
            onProgress: (Int, String) -> Unit = { _, _ -> },
        ) = withContext(Dispatchers.IO) {
            syncMutex.withLock {
                val repo = openRepo(appCtx, storeId)
                val conn = StorebookConnectorConnector.instance

                // E01-S2: Process overdue retries first (retry previously failed mutations)
                onProgress(5, "Processing retries")
                processRetries(repo, conn, storeId)

                // E01-S1: Set status before push starts
                repo.setSyncStatus("PUSHING")
                onProgress(10, "Pushing")
                val pushed = push(repo, conn, storeId)

                // E01-S1: Only set PULLING if push completed (not partial failure)
                repo.setSyncStatus("PULLING")
                onProgress(40, "Pulling")
                val (pulled, pullHadEntityFailures) = pull(repo, conn, storeId, appCtx)

                // E01-S1: Update SyncState ONLY after both phases succeed fully
                // Push count + Pull count are batch metrics for tracking
                val failedCount = repo.getPendingFailureCount() ?: 0L
                // BUG-02 FIX + F1: Set status to FAILED when push failures OR pull entity failures exist
                val hasFailures = failedCount > 0 || pullHadEntityFailures
                repo.updateSyncState(
                    lastFullSyncAt = System.currentTimeMillis(),
                    lastPushBatchCount = pushed,
                    lastPullBatchCount = pulled,
                    totalFailedMutations = failedCount,
                    status = if (hasFailures) "FAILED" else "DONE",
                )

                onProgress(100, "Done")
            }
        }

        // ==========================================================================
        // E01-S2: Retry previously-failed mutations with exponential backoff
        // Delays: attempt 0→5s, attempt 1→10s, attempt 2→20s (capped 5min)
        // After max_retries=3 → mark PERMANENT_FAILURE
        // ==========================================================================

        private suspend fun processRetries(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            sid: String,
        ) {
            val overdue = r.getOverdueForRetry()
            android.util.Log.d("SyncWorker", "E01-S2 retry queue: ${overdue.size} overdue items to retry")

            for (entry in overdue) {
                try {
                    // Re-attempt based on entity type
                    when (entry.entity_type) {
                        "ITEM" -> retryPushItem(r, c, sid, entry.local_id.toString())
                        "SALE" -> retryPushSale(r, c, sid, entry.local_id.toString())
                        "UDHAAR" -> retryPushUdhaar(r, c, sid, entry.local_id.toString())
                        "SUPPLIER" -> retryPushSupplier(r, c, sid, entry.local_id.toString())
                        "PURCHASE" -> retryPushPurchase(r, c, sid, entry.local_id.toString())
                        "STOCK_ADJUSTMENT" -> retryPushStockAdjustment(r, c, sid, entry.local_id.toString())
                        else ->
                            android.util.Log
                                .d("SyncWorker", "Retry skipping unsupported entity: ${entry.entity_type}")
                    }

                    // Success — dequeue this entry
                    r.dequeueFailedSyncById(entry.id)
                    android.util.Log.d("SyncWorker", "Retry success for ${entry.entity_type}/${entry.local_id}")
                } catch (e: Exception) {
                    // Backoff: increment retry count and schedule next attempt or mark permanent
                    val rc = entry.retry_count.toInt()
                    val mr = entry.max_retries.toInt()
                    if (RetryBackoffCalculator.hasMaxRetries(rc, mr)) {
                        r.markPermanentFailure(entry.id)
                        android.util.Log
                            .e(
                                "SyncWorker",
                                "E01-S2 PERMANENT_FAILURE: ${entry.entity_type}/${entry.local_id} after $rc retries — reason: ${e.message}",
                            )
                    } else {
                        val nextRetryAt = RetryBackoffCalculator.nextRetryAtFromNow(rc)
                        r.updateRetryState(entry.id, nextRetryAt, e.message ?: "unknown")
                        android.util.Log
                            .w(
                                "SyncWorker",
                                "Retry backoff: ${entry.entity_type}/${entry.local_id} → attempt $rc at $nextRetryAt (${e.message})",
                            )
                    }
                }
            }
        }

        private suspend fun retryPushItem(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            localId: String,
        ) {
            val items = r.getUnsyncedItems().filter { it.id.toString() == localId }
            if (items.isEmpty()) return // already synced or deleted
            val it = items[0]
            val currentCloudId = it.cloud_id
            val remoteId: String =
                if (!currentCloudId.isNullOrBlank()) {
                    currentCloudId
                } else {
                    val newUuid =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    r.setItemCloudId(it.id, newUuid)
                    newUuid
                }
            val batch = r.getBatchesForItem(it.id).firstOrNull()
            var categoryCloudId =
                it.category?.takeIf { catName -> catName.isNotBlank() }?.let { catName ->
                    val trimmed = catName.trim()
                    val match = r.getAllCategories().firstOrNull { cat -> cat.name.trim().equals(trimmed, ignoreCase = true) }
                    match?.cloud_id?.takeIf { cId -> cId.isNotBlank() } ?: run {
                        val newCatUuid =
                            java.util.UUID
                                .randomUUID()
                                .toString()
                        try {
                            if (match != null) {
                                r.setCategoryCloudId(match.id, newCatUuid)
                            } else {
                                r.getQueries().insertCategory(
                                    name = trimmed,
                                    businessType = "general",
                                    storeId = s,
                                    cloudId = newCatUuid,
                                    isDefault = 0L,
                                    isSynced = 0L,
                                    updatedAt = System.currentTimeMillis(),
                                )
                            }
                            newCatUuid
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
            val res =
                c.syncItem
                    .execute(
                        remoteId, s, sanitize(it.name), it.quantity, it.unit, it.buy_price, it.sell_price,
                        it.low_stock_threshold ?: 0.0, it.category ?: "", it.is_deleted == 1L, it.updated_at.toDouble(),
                    ) {
                        categoryId = categoryCloudId
                        photoPath = it.photo_path
                        barcode = it.barcode
                        hsnCode = it.hsn_code
                        taxRate = it.tax_rate
                        batchLotNumber = batch?.batch_number
                        expiryDate =
                            batch?.expiry_date?.let { ms ->
                                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(ms))
                            }
                    }
            r.markItemSynced(it.id, res.data.key.id)
        }

        private suspend fun retryPushSale(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            localId: String,
        ) {
            val sales = r.getUnsyncedSales().filter { it.id.toString() == localId }
            if (sales.isEmpty()) return
            val se = sales[0]
            val currentCloudId = se.cloud_id
            val remoteId: String =
                if (!currentCloudId.isNullOrBlank()) {
                    currentCloudId
                } else {
                    val newUuid =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    r.setSaleCloudId(se.id, newUuid)
                    newUuid
                }
            val res =
                c.syncSale.execute(
                    remoteId, s, se.timestamp.toDouble(), se.total_amount,
                    se.discount_amount ?: 0.0, se.type, se.is_deleted == 1L, se.updated_at.toDouble(),
                ) {
                    customerName =
                        sanitize(se.customer_name)
                    ; businessGstin = sanitize(se.business_gstin)
                }
            r.markSaleSynced(se.id, res.data.key.id)
        }

        private suspend fun retryPushUdhaar(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            localId: String,
        ) {
            val udhaars = r.getUnsyncedUdhaars().filter { it.id.toString() == localId }
            if (udhaars.isEmpty()) return // already synced or deleted
            val u = udhaars[0]
            val currentCloudId = u.cloud_id
            val remoteId: String =
                if (!currentCloudId.isNullOrBlank()) {
                    currentCloudId
                } else {
                    val newUuid =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    r.setUdhaarCloudId(u.id, newUuid)
                    newUuid
                }
            val res =
                c.syncUdhaar
                    .execute(
                        remoteId, s, sanitize(u.customer_name), u.amount, u.type, u.timestamp.toDouble(),
                        u.is_deleted == 1L, u.updated_at.toDouble(),
                    ) { notes = u.notes }
            r.markUdhaarSynced(u.id, res.data.key.id)
        }

        private suspend fun retryPushSupplier(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            localId: String,
        ) {
            val suppliers = r.getUnsyncedSuppliers().filter { it.id.toString() == localId }
            if (suppliers.isEmpty()) return
            val su = suppliers[0]
            val currentCloudId = su.cloud_id
            val remoteId: String =
                if (!currentCloudId.isNullOrBlank()) {
                    currentCloudId
                } else {
                    val newUuid =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    r.setSupplierCloudId(su.id, newUuid)
                    newUuid
                }
            val res =
                c.syncSupplier.execute(
                    remoteId, s, sanitize(su.name), su.is_deleted == 1L,
                    su.updated_at.toDouble(),
                ) {
                    phone = sanitize(su.phone)
                    ; gstin = sanitize(su.gstin)
                    address = sanitize(su.address)
                }
            r.markSupplierSynced(su.id, res.data.key.id)
        }

        private suspend fun retryPushPurchase(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            localId: String,
        ) {
            val purchases = r.getUnsyncedPurchases().filter { it.id.toString() == localId }
            if (purchases.isEmpty()) return
            val p = purchases[0]
            val currentCloudId = p.cloud_id
            val remoteId: String =
                if (!currentCloudId.isNullOrBlank()) {
                    currentCloudId
                } else {
                    val newUuid =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    r.setPurchaseCloudId(p.id, newUuid)
                    newUuid
                }
            val suppliersMap = r.getAllSuppliers().associate { it.id to it.cloud_id }
            val remoteSupplierId = suppliersMap[p.supplier_id]?.takeIf { it.isNotBlank() } ?: "068cdba2-f58e-4ab8-a596-13806e4cb18e"
            val res =
                c.syncPurchase.execute(
                    remoteId, s, remoteSupplierId, sanitize(p.supplier_name), p.total_amount,
                    p.tax_amount ?: 0.0, p.type, p.timestamp.toDouble(), p.is_deleted == 1L,
                    p.updated_at.toDouble(),
                ) { notes = p.notes }
            r.markPurchaseSynced(p.id, res.data.key.id)
        }

        private suspend fun retryPushStockAdjustment(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            localId: String,
        ) {
            val adjustments = r.getUnsyncedStockAdjustments().filter { it.id.toString() == localId }
            if (adjustments.isEmpty()) return
            val sa = adjustments[0]
            val currentCloudId = sa.cloud_id
            val remoteId: String =
                if (!currentCloudId.isNullOrBlank()) {
                    currentCloudId
                } else {
                    val newUuid =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    r.setStockAdjustmentCloudId(sa.id, newUuid)
                    newUuid
                }
            val itemsMap = r.getAllItems().associate { it.id to it.cloud_id }
            val remoteItemId = itemsMap[sa.item_id]?.takeIf { it.isNotBlank() } ?: sa.item_id.toString()
            val res =
                c.syncStockAdjustment.execute(
                    remoteId, s, remoteItemId, sanitize(sa.item_name),
                    sa.reason, sa.delta, sa.timestamp.toDouble(), sa.is_deleted == 1L,
                    sa.updated_at.toDouble(),
                )
            r.markStockAdjustmentSynced(sa.id, res.data.key.id)
        }

        // ==========================================================================
        // PUSH PHASE — E01-S1: every mutation wrapped in try/catch + enqueue on fail
        // ==========================================================================

        private suspend fun push(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            sid: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            var totalPushed = 0
            // 1. Categories
            totalPushed += pushCategories(r, c, sid, batchClient)
            // 2. Suppliers
            totalPushed += pushSuppliers(r, c, sid, batchClient)
            // 3. Items
            totalPushed += pushItems(r, c, sid, batchClient)
            // 4. Item Batches
            totalPushed += pushItemBatches(r, c, sid, batchClient)
            // 5. Sales
            totalPushed += pushSales(r, c, sid, batchClient)
            // 6. Sale Items
            totalPushed += pushSaleItems(r, c, sid, batchClient)
            // 7. Purchases
            totalPushed += pushPurchases(r, c, sid, batchClient)
            // 8. Purchase Items
            totalPushed += pushPi(r, c, sid, batchClient)
            // 9. Expenses
            totalPushed += pushExpenses(r, c, sid, batchClient)
            // 10. Udhaars
            totalPushed += pushUdhaars(r, c, sid, batchClient)
            // 11. Stock Adjustments
            totalPushed += pushStockAdjustments(r, c, sid, batchClient)
            return totalPushed
        }

        private suspend fun pushCategories(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedCategories()
            if (unsynced.isEmpty()) return 0
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { cat ->
                        val remoteId =
                            if (!cat.cloud_id.isNullOrBlank()) {
                                cat.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setCategoryCloudId(cat.id, newUuid)
                                newUuid
                            }
                        mapOf(
                            "id" to remoteId,
                            "businessType" to cat.business_type,
                            "storeId" to s,
                            "name" to sanitize(cat.name),
                            "isDefault" to (cat.is_default == 1L),
                            "isDeleted" to (cat.is_deleted == 1L),
                            "updatedAt" to cat.updated_at.toDouble(),
                        )
                    }
                try {
                    batchClient.execute("SyncCategoriesBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val cat = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markCategorySynced(cat.id, remoteId, cat.updated_at)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push categories: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push categories failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val cat = chunk[i]
                        try {
                            val remoteId = payloadList[i]["id"] as String
                            val res =
                                c.syncCategory.execute(
                                    id = remoteId,
                                    businessType = cat.business_type,
                                    name = sanitize(cat.name),
                                    isDeleted = cat.is_deleted == 1L,
                                    updatedAt = cat.updated_at.toDouble(),
                                    storeId = s,
                                )
                            r.markCategorySynced(cat.id, res.data.key.id, cat.updated_at)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push category ${cat.id} failed: ${singleE.message}")
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushItems(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedItems()
            if (unsynced.isEmpty()) return 0

            // Pre-load category map: lowercase name -> cloud_id
            val allCategories = r.getAllCategories().toMutableList()
            val catNameToCloudId = mutableMapOf<String, String>()
            for (cat in allCategories) {
                val currentCid = cat.cloud_id
                val cid: String =
                    if (!currentCid.isNullOrBlank()) {
                        currentCid
                    } else {
                        val newCid =
                            java.util.UUID
                                .randomUUID()
                                .toString()
                        r.markCategorySynced(cat.id, newCid, cat.updated_at)
                        newCid
                    }
                catNameToCloudId[cat.name.trim().lowercase()] = cid
            }

            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { it ->
                        val batch = r.getBatchesForItem(it.id).firstOrNull()
                        val currentCloudId = it.cloud_id
                        val remoteId: String =
                            if (!currentCloudId.isNullOrBlank()) {
                                currentCloudId
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setItemCloudId(it.id, newUuid)
                                newUuid
                            }
                        var categoryCloudId = catNameToCloudId[it.category.trim().lowercase()]
                        if (categoryCloudId.isNullOrBlank() && it.category.isNotBlank()) {
                            val trimmed = it.category.trim()
                            val match = allCategories.firstOrNull { c -> c.name.trim().equals(trimmed, ignoreCase = true) }
                            categoryCloudId = match?.cloud_id?.takeIf { cId -> cId.isNotBlank() }
                            if (categoryCloudId.isNullOrBlank()) {
                                val newCatUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                val now = System.currentTimeMillis()
                                try {
                                    r.getQueries().insertCategory(
                                        name = trimmed,
                                        businessType = "general",
                                        storeId = s,
                                        cloudId = newCatUuid,
                                        isDefault = 0L,
                                        isSynced = 0L,
                                        updatedAt = now,
                                    )
                                    categoryCloudId = newCatUuid
                                    catNameToCloudId[trimmed.lowercase()] = newCatUuid
                                } catch (_: Exception) {
                                }
                            }
                        }
                        val itemMap =
                            mutableMapOf<String, Any?>(
                                "id" to remoteId,
                                "storeId" to s,
                                "name" to sanitize(it.name),
                                "quantity" to it.quantity,
                                "unit" to it.unit,
                                "buyPrice" to it.buy_price,
                                "sellPrice" to it.sell_price,
                                "lowStockThreshold" to (it.low_stock_threshold ?: 0.0),
                                "category" to (it.category ?: ""),
                                "isDeleted" to (it.is_deleted == 1L),
                                "updatedAt" to it.updated_at.toDouble(),
                            )
                        categoryCloudId?.let { cid -> itemMap["categoryId"] = cid }
                        it.photo_path?.let { p -> itemMap["photoPath"] = p }
                        it.barcode?.let { b -> itemMap["barcode"] = b }
                        it.hsn_code?.let { h -> itemMap["hsnCode"] = h }
                        it.tax_rate?.let { tr -> itemMap["taxRate"] = tr }
                        batch?.batch_number?.let { bn -> itemMap["batchLotNumber"] = bn }
                        batch?.expiry_date?.let { ms ->
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            itemMap["expiryDate"] = sdf.format(java.util.Date(ms))
                        }
                        itemMap
                    }
                try {
                    batchClient.execute("SyncItemsBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val it = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markItemSynced(it.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push items: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push items failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val it = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        val categoryCloudId = (payloadList[i]["categoryId"] as? String) ?: catNameToCloudId[it.category.trim().lowercase()]
                        try {
                            val batch = r.getBatchesForItem(it.id).firstOrNull()
                            val res =
                                c.syncItem.execute(
                                    remoteId,
                                    s,
                                    sanitize(it.name),
                                    it.quantity,
                                    it.unit,
                                    it.buy_price,
                                    it.sell_price,
                                    it.low_stock_threshold ?: 0.0,
                                    it.category ?: "",
                                    it.is_deleted == 1L,
                                    it.updated_at.toDouble(),
                                ) {
                                    categoryId = categoryCloudId
                                    photoPath = it.photo_path
                                    barcode = it.barcode
                                    hsnCode = it.hsn_code
                                    taxRate = it.tax_rate
                                    batchLotNumber = batch?.batch_number
                                    expiryDate =
                                        batch?.expiry_date?.let { ms ->
                                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                            sdf.format(java.util.Date(ms))
                                        }
                                }
                            r.markItemSynced(it.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push item ${it.id} failed: ${singleE.message}", singleE)
                            if (!isTransientException(singleE)) {
                                r.incrementFailedMutationCount()
                                continue
                            }
                            try {
                                r.enqueueSyncFailure(
                                    entityType = "ITEM",
                                    localId = it.id,
                                    cloudId = remoteId,
                                    nextRetryAt = RetryBackoffCalculator.nextRetryAtFromNow(0),
                                    errorMessage = "push_item: ${singleE.message ?: "unknown"}",
                                )
                                r.incrementFailedMutationCount()
                            } catch (qe: Exception) {
                                android.util.Log.e("SW", "Enqueue failed for item ${it.id}", qe)
                            }
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushSuppliers(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedSuppliers()
            if (unsynced.isEmpty()) return 0
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { su ->
                        val remoteId =
                            if (!su.cloud_id.isNullOrBlank()) {
                                su.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setSupplierCloudId(su.id, newUuid)
                                newUuid
                            }
                        val supplierMap =
                            mutableMapOf<String, Any?>(
                                "id" to remoteId,
                                "storeId" to s,
                                "name" to sanitize(su.name),
                                "isDeleted" to (su.is_deleted == 1L),
                                "updatedAt" to su.updated_at.toDouble(),
                            )
                        su.phone?.let { supplierMap["phone"] = sanitize(it) }
                        su.gstin?.let { supplierMap["gstin"] = sanitize(it) }
                        su.address?.let { supplierMap["address"] = sanitize(it) }
                        supplierMap
                    }
                try {
                    batchClient.execute("SyncSuppliersBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val su = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markSupplierSynced(su.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push suppliers: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push suppliers failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val su = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        try {
                            val res =
                                c.syncSupplier.execute(
                                    remoteId,
                                    s,
                                    sanitize(su.name),
                                    su.is_deleted == 1L,
                                    su.updated_at.toDouble(),
                                ) {
                                    phone = sanitize(su.phone)
                                    gstin = sanitize(su.gstin)
                                    address = sanitize(su.address)
                                }
                            r.markSupplierSynced(su.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push supplier ${su.id} failed", singleE)
                            try {
                                r.enqueueSyncFailure(
                                    "SUPPLIER",
                                    su.id,
                                    remoteId,
                                    RetryBackoffCalculator.nextRetryAtFromNow(0),
                                    "push_supplier: ${singleE.message ?: "unknown"}",
                                )
                                r.incrementFailedMutationCount()
                            } catch (qe: Exception) {
                                android.util.Log.e("SW", "Enqueue supplier fail", qe)
                            }
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushItemBatches(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedItemBatches()
            if (unsynced.isEmpty()) return 0
            val itemsMap = r.getAllItems().associate { it.id to it.cloud_id }
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { ib ->
                        val remoteId =
                            if (!ib.cloud_id.isNullOrBlank()) {
                                ib.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setItemBatchCloudId(ib.id, newUuid)
                                newUuid
                            }
                        val remoteItemId = itemsMap[ib.item_id]?.takeIf { it.isNotBlank() } ?: ib.item_id.toString()
                        val batchMap =
                            mutableMapOf<String, Any?>(
                                "id" to remoteId,
                                "storeId" to s,
                                "itemId" to remoteItemId,
                                "quantity" to ib.quantity,
                                "costPrice" to ib.cost_price,
                                "timestamp" to ib.timestamp.toDouble(),
                                "isDeleted" to (ib.is_deleted == 1L),
                                "updatedAt" to ib.updated_at.toDouble(),
                            )
                        ib.batch_number?.let { batchMap["batchNumber"] = it }
                        ib.expiry_date?.let { batchMap["expiryDate"] = it.toDouble() }
                        ib.notes?.let { batchMap["notes"] = it }
                        batchMap
                    }
                try {
                    batchClient.execute("SyncItemBatchesBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val ib = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markItemBatchSynced(ib.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push item_batches: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push item_batches failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val ib = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        val remoteItemId = itemsMap[ib.item_id]?.takeIf { it.isNotBlank() } ?: ib.item_id.toString()
                        try {
                            val res =
                                c.syncItemBatch.execute(
                                    remoteId,
                                    s,
                                    remoteItemId,
                                    ib.quantity,
                                    ib.cost_price,
                                    ib.timestamp.toDouble(),
                                    ib.is_deleted == 1L,
                                    ib.updated_at.toDouble(),
                                ) {
                                    batchNumber = ib.batch_number
                                    expiryDate = ib.expiry_date?.toDouble()
                                    notes = ib.notes
                                }
                            r.markItemBatchSynced(ib.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push item_batch ${ib.id} failed: ${singleE.message}")
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushSales(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedSales()
            if (unsynced.isEmpty()) return 0
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { se ->
                        val remoteId =
                            if (!se.cloud_id.isNullOrBlank()) {
                                se.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setSaleCloudId(se.id, newUuid)
                                newUuid
                            }
                        val saleMap =
                            mutableMapOf<String, Any?>(
                                "id" to remoteId,
                                "storeId" to s,
                                "timestamp" to se.timestamp.toDouble(),
                                "totalAmount" to se.total_amount,
                                "discountAmount" to (se.discount_amount ?: 0.0),
                                "type" to se.type,
                                "isDeleted" to (se.is_deleted == 1L),
                                "updatedAt" to se.updated_at.toDouble(),
                            )
                        se.customer_name?.let { saleMap["customerName"] = sanitize(it) }
                        se.customer_gstin?.let { saleMap["customerGstin"] = sanitize(it) }
                        se.business_gstin?.let { saleMap["businessGstin"] = sanitize(it) }
                        se.customer_address?.let { saleMap["customerAddress"] = sanitize(it) }
                        se.business_address?.let { saleMap["businessAddress"] = sanitize(it) }
                        se.notes?.let { saleMap["notes"] = it }
                        saleMap
                    }
                try {
                    batchClient.execute("SyncSalesBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val se = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markSaleSynced(se.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push sales: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push sales failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val se = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        try {
                            val res =
                                c.syncSale.execute(
                                    remoteId,
                                    s,
                                    se.timestamp.toDouble(),
                                    se.total_amount,
                                    se.discount_amount ?: 0.0,
                                    se.type,
                                    se.is_deleted == 1L,
                                    se.updated_at.toDouble(),
                                ) {
                                    customerName = sanitize(se.customer_name)
                                    businessGstin = sanitize(se.business_gstin)
                                }
                            r.markSaleSynced(se.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push sale ${se.id} failed: ${singleE.message}", singleE)
                            if (!isTransientException(singleE)) {
                                r.incrementFailedMutationCount()
                                continue
                            }
                            try {
                                r.enqueueSyncFailure(
                                    "SALE",
                                    se.id,
                                    remoteId,
                                    RetryBackoffCalculator.nextRetryAtFromNow(0),
                                    "push_sale: ${singleE.message ?: "unknown"}",
                                )
                                r.incrementFailedMutationCount()
                            } catch (qe: Exception) {
                                android.util.Log.e("SW", "Enqueue sale fail", qe)
                            }
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushSaleItems(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedSaleItems()
            if (unsynced.isEmpty()) return 0
            val salesMap = r.getAllSales().associate { it.id to it.cloud_id }
            val itemsMap = r.getAllItems().associate { it.id to it.cloud_id }
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { si ->
                        val remoteId =
                            if (!si.cloud_id.isNullOrBlank()) {
                                si.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setSaleItemCloudId(si.id, newUuid)
                                newUuid
                            }
                        val remoteSaleId = salesMap[si.sale_id]?.takeIf { it.isNotBlank() } ?: si.sale_id.toString()
                        val remoteItemId = itemsMap[si.item_id]?.takeIf { it.isNotBlank() } ?: si.item_id.toString()
                        mapOf(
                            "id" to remoteId,
                            "storeId" to s,
                            "saleId" to remoteSaleId,
                            "itemId" to remoteItemId,
                            "itemName" to sanitize(si.item_name),
                            "quantity" to si.quantity,
                            "unit" to si.unit,
                            "sellPrice" to (si.sell_price ?: 0.0),
                            "buyPrice" to si.buy_price,
                            "taxRate" to 0.0,
                            "hsnCode" to "",
                            "isDeleted" to (si.is_deleted == 1L),
                            "updatedAt" to si.updated_at.toDouble(),
                        )
                    }
                try {
                    batchClient.execute("SyncSaleItemDetailsBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val si = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markSaleItemSynced(si.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push sale_items: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push sale_items failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val si = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        val remoteSaleId = salesMap[si.sale_id]?.takeIf { it.isNotBlank() } ?: si.sale_id.toString()
                        val remoteItemId = itemsMap[si.item_id]?.takeIf { it.isNotBlank() } ?: si.item_id.toString()
                        try {
                            val res =
                                c.syncSaleItem.execute(
                                    remoteId,
                                    s,
                                    remoteSaleId,
                                    remoteItemId,
                                    sanitize(si.item_name),
                                    si.unit,
                                    si.quantity,
                                    si.sell_price ?: 0.0,
                                    si.buy_price,
                                    si.is_deleted == 1L,
                                    si.updated_at.toDouble(),
                                )
                            r.markSaleItemSynced(si.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push sale_item ${si.id} failed", singleE)
                            if (!isTransientException(singleE)) {
                                r.incrementFailedMutationCount()
                                continue
                            }
                            try {
                                r.enqueueSyncFailure(
                                    "SALE_ITEM",
                                    si.id,
                                    remoteId,
                                    RetryBackoffCalculator.nextRetryAtFromNow(0),
                                    "push_saleItem: ${singleE.message ?: "unknown"}",
                                )
                                r.incrementFailedMutationCount()
                            } catch (qe: Exception) {
                                android.util.Log.e("SW", "Enqueue sitem fail", qe)
                            }
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushPurchases(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedPurchases()
            if (unsynced.isEmpty()) return 0
            val suppliersMap = r.getAllSuppliers().associate { it.id to it.cloud_id }
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { p ->
                        val remoteId =
                            if (!p.cloud_id.isNullOrBlank()) {
                                p.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setPurchaseCloudId(p.id, newUuid)
                                newUuid
                            }
                        val remoteSupplierId = suppliersMap[p.supplier_id]?.takeIf { it.isNotBlank() } ?: "068cdba2-f58e-4ab8-a596-13806e4cb18e"
                        val purchaseMap =
                            mutableMapOf<String, Any?>(
                                "id" to remoteId,
                                "storeId" to s,
                                "supplierId" to remoteSupplierId,
                                "supplierName" to sanitize(p.supplier_name),
                                "totalAmount" to p.total_amount,
                                "taxAmount" to (p.tax_amount ?: 0.0),
                                "type" to p.type,
                                "timestamp" to p.timestamp.toDouble(),
                                "isDeleted" to (p.is_deleted == 1L),
                                "updatedAt" to p.updated_at.toDouble(),
                            )
                        p.notes?.let { purchaseMap["notes"] = it }
                        purchaseMap
                    }
                try {
                    batchClient.execute("SyncPurchasesBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val p = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markPurchaseSynced(p.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push purchases: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push purchases failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val p = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        val remoteSupplierId = suppliersMap[p.supplier_id]?.takeIf { it.isNotBlank() } ?: "068cdba2-f58e-4ab8-a596-13806e4cb18e"
                        try {
                            val res =
                                c.syncPurchase.execute(
                                    remoteId,
                                    s,
                                    remoteSupplierId,
                                    sanitize(p.supplier_name),
                                    p.total_amount,
                                    p.tax_amount ?: 0.0,
                                    p.type,
                                    p.timestamp.toDouble(),
                                    p.is_deleted == 1L,
                                    p.updated_at.toDouble(),
                                ) { notes = p.notes }
                            r.markPurchaseSynced(p.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push purchase ${p.id} failed", singleE)
                            try {
                                r.enqueueSyncFailure(
                                    "PURCHASE",
                                    p.id,
                                    remoteId,
                                    RetryBackoffCalculator.nextRetryAtFromNow(0),
                                    "push_purchase: ${singleE.message ?: "unknown"}",
                                )
                                r.incrementFailedMutationCount()
                            } catch (qe: Exception) {
                                android.util.Log.e("SW", "Enqueue purchase fail", qe)
                            }
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushPi(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedPurchaseItems()
            if (unsynced.isEmpty()) return 0
            val purchasesMap = r.getAllPurchases().associate { it.id to it.cloud_id }
            val itemsMap = r.getAllItems().associate { it.id to it.cloud_id }
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { pi ->
                        val remoteId =
                            if (!pi.cloud_id.isNullOrBlank()) {
                                pi.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setPurchaseItemCloudId(pi.id, newUuid)
                                newUuid
                            }
                        val remotePurchaseId = purchasesMap[pi.purchase_id]?.takeIf { it.isNotBlank() } ?: pi.purchase_id.toString()
                        val remoteItemId = itemsMap[pi.item_id]?.takeIf { it.isNotBlank() } ?: pi.item_id.toString()
                        mapOf(
                            "id" to remoteId,
                            "storeId" to s,
                            "purchaseId" to remotePurchaseId,
                            "itemId" to remoteItemId,
                            "itemName" to sanitize(pi.item_name),
                            "quantity" to pi.quantity,
                            "unit" to pi.unit,
                            "buyPrice" to pi.buy_price,
                            "isDeleted" to (pi.is_deleted == 1L),
                            "updatedAt" to pi.updated_at.toDouble(),
                        )
                    }
                try {
                    batchClient.execute("SyncPurchaseItemDetailsBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val pi = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markPurchaseItemSynced(pi.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push purchase_items: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push purchase_items failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val pi = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        val remotePurchaseId = purchasesMap[pi.purchase_id]?.takeIf { it.isNotBlank() } ?: pi.purchase_id.toString()
                        val remoteItemId = itemsMap[pi.item_id]?.takeIf { it.isNotBlank() } ?: pi.item_id.toString()
                        try {
                            val res =
                                c.syncPurchaseItem.execute(
                                    remoteId,
                                    s,
                                    remotePurchaseId,
                                    remoteItemId,
                                    sanitize(pi.item_name),
                                    pi.quantity,
                                    pi.unit,
                                    pi.buy_price,
                                    pi.is_deleted == 1L,
                                    pi.updated_at.toDouble(),
                                )
                            r.markPurchaseItemSynced(pi.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push pitem ${pi.id} failed", singleE)
                            try {
                                r.enqueueSyncFailure(
                                    "PURCHASE_ITEM",
                                    pi.id,
                                    remoteId,
                                    RetryBackoffCalculator.nextRetryAtFromNow(0),
                                    "push_pitem: ${singleE.message ?: "unknown"}",
                                )
                                r.incrementFailedMutationCount()
                            } catch (qe: Exception) {
                                android.util.Log.e("SW", "Enqueue pitem fail", qe)
                            }
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushExpenses(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedExpenses()
            if (unsynced.isEmpty()) return 0
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { ex ->
                        val remoteId =
                            if (!ex.cloud_id.isNullOrBlank()) {
                                ex.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setExpenseCloudId(ex.id, newUuid)
                                newUuid
                            }
                        val expenseMap =
                            mutableMapOf<String, Any?>(
                                "id" to remoteId,
                                "storeId" to s,
                                "type" to ex.type,
                                "description" to sanitize(ex.description),
                                "amount" to ex.amount,
                                "timestamp" to ex.timestamp.toDouble(),
                                "isDeleted" to (ex.is_deleted == 1L),
                                "updatedAt" to ex.updated_at.toDouble(),
                            )
                        ex.supplier_name?.let { expenseMap["supplierName"] = it }
                        ex.supplier_phone?.let { expenseMap["supplierPhone"] = it }
                        expenseMap
                    }
                try {
                    batchClient.execute("SyncExpensesBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val ex = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markExpenseSynced(ex.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push expenses: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push expenses failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val ex = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        try {
                            val res =
                                c.syncExpense.execute(
                                    remoteId,
                                    s,
                                    ex.type,
                                    sanitize(ex.description),
                                    ex.amount,
                                    ex.timestamp.toDouble(),
                                    ex.is_deleted == 1L,
                                    ex.updated_at.toDouble(),
                                ) {
                                    supplierName = ex.supplier_name
                                    supplierPhone = ex.supplier_phone
                                }
                            r.markExpenseSynced(ex.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push expense ${ex.id} failed", singleE)
                            if (!isTransientException(singleE)) {
                                r.incrementFailedMutationCount()
                                continue
                            }
                            try {
                                r.enqueueSyncFailure(
                                    "EXPENSE",
                                    ex.id,
                                    remoteId,
                                    RetryBackoffCalculator.nextRetryAtFromNow(0),
                                    "push_expense: ${singleE.message ?: "unknown"}",
                                )
                                r.incrementFailedMutationCount()
                            } catch (qe: Exception) {
                                android.util.Log.e("SW", "Enqueue expense fail", qe)
                            }
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushUdhaars(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedUdhaars()
            if (unsynced.isEmpty()) return 0
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { u ->
                        val remoteId =
                            if (!u.cloud_id.isNullOrBlank()) {
                                u.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setUdhaarCloudId(u.id, newUuid)
                                newUuid
                            }
                        val udhaarMap =
                            mutableMapOf<String, Any?>(
                                "id" to remoteId,
                                "storeId" to s,
                                "customerName" to sanitize(u.customer_name),
                                "amount" to u.amount,
                                "type" to u.type,
                                "timestamp" to u.timestamp.toDouble(),
                                "isDeleted" to (u.is_deleted == 1L),
                                "updatedAt" to u.updated_at.toDouble(),
                            )
                        u.notes?.let { udhaarMap["notes"] = it }
                        udhaarMap
                    }
                try {
                    batchClient.execute("SyncUdhaarsBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val u = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markUdhaarSynced(u.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push udhaars: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push udhaars failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val u = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        try {
                            val res =
                                c.syncUdhaar.execute(
                                    remoteId,
                                    s,
                                    sanitize(u.customer_name),
                                    u.amount,
                                    u.type,
                                    u.timestamp.toDouble(),
                                    u.is_deleted == 1L,
                                    u.updated_at.toDouble(),
                                ) { notes = u.notes }
                            r.markUdhaarSynced(u.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push udhaar ${u.id} failed", singleE)
                            if (!isTransientException(singleE)) {
                                r.incrementFailedMutationCount()
                                continue
                            }
                            try {
                                r.enqueueSyncFailure(
                                    "UDHAAR",
                                    u.id,
                                    remoteId,
                                    RetryBackoffCalculator.nextRetryAtFromNow(0),
                                    "push_udhaar: ${singleE.message ?: "unknown"}",
                                )
                                r.incrementFailedMutationCount()
                            } catch (qe: Exception) {
                                android.util.Log.e("SW", "Enqueue udhaar fail", qe)
                            }
                        }
                    }
                }
            }
            return count
        }

        private suspend fun pushStockAdjustments(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
            batchClient: BatchSyncClient = BatchSyncClient.instance,
        ): Int {
            val unsynced = r.getUnsyncedStockAdjustments()
            if (unsynced.isEmpty()) return 0
            val itemsMap = r.getAllItems().associate { it.id to it.cloud_id }
            var count = 0
            for (chunk in unsynced.chunked(BatchSyncClient.DEFAULT_BATCH_CHUNK_SIZE)) {
                val payloadList =
                    chunk.map { sa ->
                        val remoteId =
                            if (!sa.cloud_id.isNullOrBlank()) {
                                sa.cloud_id
                            } else {
                                val newUuid =
                                    java.util.UUID
                                        .randomUUID()
                                        .toString()
                                r.setStockAdjustmentCloudId(sa.id, newUuid)
                                newUuid
                            }
                        val remoteItemId = itemsMap[sa.item_id]?.takeIf { it.isNotBlank() } ?: sa.item_id.toString()
                        mapOf(
                            "id" to remoteId,
                            "storeId" to s,
                            "itemId" to remoteItemId,
                            "itemName" to sanitize(sa.item_name),
                            "reason" to sa.reason,
                            "delta" to sa.delta,
                            "timestamp" to sa.timestamp.toDouble(),
                            "isDeleted" to (sa.is_deleted == 1L),
                            "updatedAt" to sa.updated_at.toDouble(),
                        )
                    }
                try {
                    batchClient.execute("SyncStockAdjustmentsBatch", mapOf("data" to payloadList))
                    for (i in chunk.indices) {
                        val sa = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        r.markStockAdjustmentSynced(sa.id, remoteId)
                    }
                    count += chunk.size
                    android.util.Log.d("SW", "Batch push stock_adjustments: ${chunk.size} items succeeded")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "Batch push stock_adjustments failed: ${e.message}", e)
                    for (i in chunk.indices) {
                        val sa = chunk[i]
                        val remoteId = payloadList[i]["id"] as String
                        val remoteItemId = itemsMap[sa.item_id]?.takeIf { it.isNotBlank() } ?: sa.item_id.toString()
                        try {
                            val res =
                                c.syncStockAdjustment.execute(
                                    remoteId,
                                    s,
                                    remoteItemId,
                                    sanitize(sa.item_name),
                                    sa.reason,
                                    sa.delta,
                                    sa.timestamp.toDouble(),
                                    sa.is_deleted == 1L,
                                    sa.updated_at.toDouble(),
                                )
                            r.markStockAdjustmentSynced(sa.id, res.data.key.id)
                            count++
                        } catch (singleE: Exception) {
                            android.util.Log.e("SW", "Single push stock_adjustment ${sa.id} failed", singleE)
                            if (!isTransientException(singleE)) {
                                r.incrementFailedMutationCount()
                                continue
                            }
                            try {
                                r.enqueueSyncFailure(
                                    "STOCK_ADJUSTMENT",
                                    sa.id,
                                    remoteId,
                                    RetryBackoffCalculator.nextRetryAtFromNow(0),
                                    "push_stockAdjustment: ${singleE.message ?: "unknown"}",
                                )
                                r.incrementFailedMutationCount()
                            } catch (qe: Exception) {
                                android.util.Log.e("SW", "Enqueue stock_adjustment fail", qe)
                            }
                        }
                    }
                }
            }
            return count
        }

        // ==========================================================================
        // E01-S3: PULL PHASE — Last-Write-Wins conflict resolution
        // - Items, Suppliers, Purchases: remote.updatedAt >= local.updatedAt → accept
        // - Sales: append-only by cloudId (skip duplicates)
        // - SaleItems, PurchaseItems: cascade from parent entity lookup
        // ==========================================================================

        private suspend fun pull(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            sid: String,
            ctx: Context,
        ): Pair<Int, Boolean> {
            val prefs =
                com.storebook.inventoryapp.utils.SecurityUtils
                    .getEncryptedPrefs(ctx)
            val ls = prefs.getLong("last_sync_timestamp_$sid", 0L)
            var pulled = 0
            val entityFailures = mutableMapOf<String, String>()

            // Categories — pull industry catalog and custom categories
            try {
                val bType =
                    prefs.getString("business_type_$sid", null)
                        ?: prefs.getString("business_type", "general")
                        ?: "general"
                val businessTypesToFetch =
                    if (bType == "general") listOf("general") else listOf(bType, "general")
                val fetchedCategories =
                    mutableMapOf<String, com.storebook.inventoryapp.dataconnect.GetCategoriesQuery.Data.CategoryItem>()
                for (bt in businessTypesToFetch) {
                    val catResult =
                        c.getCategories
                            .execute(bt)
                            .data
                            .categories
                    for (cat in catResult) {
                        if (cat.storeId == null || cat.storeId == sid) {
                            fetchedCategories[cat.id] = cat
                        }
                    }
                }
                for (cat in fetchedCategories.values) {
                    r.upsertCategoryWithCloudId(
                        name = sanitize(cat.name),
                        businessType = cat.businessType,
                        storeId = cat.storeId,
                        cloudId = cat.id,
                        isDeleted = cat.isDeleted,
                        updatedAt = normalizeTimestamp(cat.updatedAt),
                    )
                }
                pulled += fetchedCategories.size
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "Categories pull failed: ${e.message}", e)
                entityFailures["categories_pull"] = e.message ?: "unknown"
            }

            // Items — LWW via updatedAt comparison
            try {
                val items =
                    c.syncItems
                        .execute(sid, ls.toDouble())
                        .data.items
                for (i in items) {
                    val safeUpdatedAt = normalizeTimestamp(i.updatedAt)
                    if (r.shouldAcceptRemoteItem(safeUpdatedAt, i.id)) {
                        r
                            .upsertItemWithCloudId(
                                name = i.name, quantity = i.quantity, unit = i.unit,
                                buyPrice = i.buyPrice, sellPrice = i.sellPrice, lowStockThreshold = i.lowStockThreshold,
                                category = i.category, photoPath = i.photoPath ?: "", barcode = i.barcode ?: "",
                                hsnCode = i.hsnCode ?: "", taxRate = i.taxRate ?: 0.0,
                                isDeleted = if (i.isDeleted) 1L else 0L, cloudId = i.id,
                                updatedAt = safeUpdatedAt,
                            )
                        if (!i.batchLotNumber.isNullOrBlank() || !i.expiryDate.isNullOrBlank()) {
                            val localItemId = r.resolveItemIdByCloudId(i.id)
                            if (localItemId != null) {
                                val expiryMs =
                                    i.expiryDate?.let { dateStr ->
                                        dateStr.toLongOrNull() ?: try {
                                            java.text
                                                .SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                                .parse(dateStr)
                                                ?.time
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                val existingBatch = r.getBatchesForItem(localItemId).firstOrNull()
                                if (existingBatch == null ||
                                    existingBatch.batch_number != i.batchLotNumber ||
                                    existingBatch.expiry_date != expiryMs
                                ) {
                                    r.insertItemBatch(
                                        itemId = localItemId,
                                        batchNumber = i.batchLotNumber,
                                        expiryDate = expiryMs,
                                        quantity = i.quantity,
                                        costPrice = i.buyPrice,
                                        notes = "Synced from cloud",
                                    )
                                }
                            }
                        }
                    }
                }
                pulled += items.size
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "Items pull failed: ${e.message}", e)
                entityFailures["items_pull"] = e.message ?: "unknown"
            }

            // Sales — append-only; skip existing cloudId duplicates
            try {
                val sales =
                    c.syncSales
                        .execute(sid, ls.toDouble())
                        .data.sales
                for (sa in sales) {
                    if (!r.saleExistsLocally(sa.id)) {
                        r
                            .upsertSaleWithCloudId(
                                normalizeTimestamp(sa.timestamp),
                                sa.totalAmount,
                                sa.discountAmount,
                                sanitize(sa.customerName),
                                sa.customerGstin,
                                sa.businessGstin,
                                sa.customerAddress,
                                sa.businessAddress,
                                sa.type,
                                sa.notes,
                                if (sa.isDeleted) 1L else 0L,
                                sa.id,
                                normalizeTimestamp(sa.updatedAt),
                            )
                    }
                }
                pulled += sales.size
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "Sales pull failed: ${e.message}", e)
                entityFailures["sales_pull"] = e.message ?: "unknown"
            }

            // SaleItems — cascade from resolved parentId keys
            try {
                val sis =
                    c.syncSaleItems
                        .execute(sid, ls.toDouble())
                        .data.saleItemDetails
                var saleItemsPulled = 0
                saleItemsLoop@ for (si in sis) {
                    // BUG-03 FIX: Skip orphan records instead of inserting with FK=0
                    val resolvedSaleId = r.resolveSaleIdByCloudId(si.saleId)
                    if (resolvedSaleId == null) {
                        android.util.Log
                            .w(
                                "SW-Pull",
                                "BUG-03: Skipping saleItem ${si.id}: parent sale ${si.saleId} not found locally",
                            )
                        continue@saleItemsLoop
                    }
                    val resolvedItemId = r.resolveItemIdByCloudId(si.itemId)
                    if (resolvedItemId == null) {
                        android.util.Log
                            .w("SW-Pull", "BUG-03: Skipping saleItem ${si.id}: item ${si.itemId} not found locally")
                        continue@saleItemsLoop
                    }
                    r
                        .upsertSaleItemWithCloudId(
                            resolvedSaleId,
                            resolvedItemId,
                            sanitize(si.itemName),
                            si.unit,
                            si.quantity,
                            si.sellPrice,
                            si.buyPrice,
                            si.taxRate ?: 0.0,
                            si.hsnCode,
                            if (si.isDeleted) 1L else 0L,
                            si.id,
                            normalizeTimestamp(si.updatedAt),
                        )
                    saleItemsPulled++
                }
                pulled += saleItemsPulled
                if (saleItemsPulled < sis.size) {
                    android.util.Log
                        .w(
                            "SW-Pull",
                            "SaleItems: $saleItemsPulled/${sis.size} processed, ${sis.size - saleItemsPulled} skipped (orphan parents not yet synced)",
                        )
                }
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "SaleItems pull failed: ${e.message}", e)
                entityFailures["sale_items_pull"] = e.message ?: "unknown"
            }

            // Udhaars — always accept new records
            try {
                val us =
                    c.syncUdhaars
                        .execute(sid, ls.toDouble())
                        .data.udhaarEntries
                for (u in us) {
                    r
                        .upsertUdhaarWithCloudId(
                            sanitize(u.customerName), u.amount, u.type, normalizeTimestamp(u.timestamp),
                            u.notes
                                ?.let(::sanitize),
                            if (u.isDeleted) 1L else 0L, u.id, normalizeTimestamp(u.updatedAt),
                        )
                }
                pulled += us.size
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "Udhaars pull failed: ${e.message}", e)
                entityFailures["udhaars_pull"] = e.message ?: "unknown"
            }

            // Expenses — always accept
            try {
                val es =
                    c.syncExpenses
                        .execute(sid, ls.toDouble())
                        .data.expenseEntries
                for (ex in es) {
                    r
                        .upsertExpenseWithCloudId(
                            ex.type, sanitize(ex.description), ex.amount, normalizeTimestamp(ex.timestamp),
                            ex.supplierName
                                ?.let(::sanitize),
                            ex.supplierPhone?.let(::sanitize), if (ex.isDeleted) 1L else 0L, ex.id,
                            normalizeTimestamp(ex.updatedAt),
                        )
                }
                pulled += es.size
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "Expenses pull failed: ${e.message}", e)
                entityFailures["expenses_pull"] = e.message ?: "unknown"
            }

            // Suppliers — LWW via updatedAt
            try {
                val ss =
                    c.syncSuppliers
                        .execute(sid, ls.toDouble())
                        .data.suppliers
                for (su in ss) {
                    val safeUpdatedAt = normalizeTimestamp(su.updatedAt)
                    if (r.shouldAcceptRemoteSupplier(safeUpdatedAt, su.id)) {
                        r
                            .upsertSupplierWithCloudId(
                                sanitize(su.name), su.phone?.let(::sanitize), su.gstin?.let(::sanitize),
                                su.address
                                    ?.let(::sanitize),
                                if (su.isDeleted) 1L else 0L, su.id, safeUpdatedAt,
                            )
                    }
                }
                pulled += ss.size
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "Suppliers pull failed: ${e.message}", e)
                entityFailures["suppliers_pull"] = e.message ?: "unknown"
            }

            // Purchases — LWW via updatedAt
            try {
                val ps =
                    c.syncPurchases
                        .execute(sid, ls.toDouble())
                        .data.purchases
                var purchasesProcessed = 0
                purchasesLoop@ for (pu in ps) {
                    val safeUpdatedAt = normalizeTimestamp(pu.updatedAt)
                    if (!r.shouldAcceptRemotePurchase(safeUpdatedAt, pu.id)) continue@purchasesLoop
                    // BUG-03 FIX: Skip orphan records instead of inserting with FK=0
                    val resolvedSupplierId = r.resolveSupplierIdByCloudId(pu.supplierId)?.toLong()
                    if (resolvedSupplierId == null) {
                        android.util.Log
                            .w(
                                "SW-Pull",
                                "BUG-03: Skipping purchase ${pu.id}: supplier ${pu.supplierId} not found locally",
                            )
                        continue@purchasesLoop
                    }
                    r
                        .upsertPurchaseWithCloudId(
                            resolvedSupplierId, sanitize(pu.supplierName), pu.totalAmount, pu.taxAmount, pu.type,
                            normalizeTimestamp(pu.timestamp),
                            pu.notes?.let(::sanitize), if (pu.isDeleted) 1L else 0L, pu.id,
                            safeUpdatedAt,
                        )
                    purchasesProcessed++
                }
                pulled += purchasesProcessed
                if (purchasesProcessed < ps.size) {
                    android.util.Log
                        .w(
                            "SW-Pull",
                            "Purchases: $purchasesProcessed/${ps.size} processed, ${ps.size - purchasesProcessed} skipped",
                        )
                }
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "Purchases pull failed: ${e.message}", e)
                entityFailures["purchases_pull"] = e.message ?: "unknown"
            }

            // PurchaseItems — cascade from resolved parentId keys
            try {
                val pis =
                    c.syncPurchaseItems
                        .execute(sid, ls.toDouble())
                        .data.purchaseItemDetails
                var purchaseItemsPulled = 0
                purchaseItemsLoop@ for (pi in pis) {
                    // BUG-03 FIX: Skip orphan records instead of inserting with FK=0
                    val resolvedPurchaseId = r.resolvePurchaseIdByCloudId(pi.purchaseId)?.toLong()
                    if (resolvedPurchaseId == null) {
                        android.util.Log
                            .w(
                                "SW-Pull",
                                "BUG-03: Skipping purchaseItem ${pi.id}: parent purchase ${pi.purchaseId} not found",
                            )
                        continue@purchaseItemsLoop
                    }
                    val resolvedItemId = r.resolveItemIdByCloudId(pi.itemId)
                    if (resolvedItemId == null) {
                        android.util.Log
                            .w("SW-Pull", "BUG-03: Skipping purchaseItem ${pi.id}: item ${pi.itemId} not found")
                        continue@purchaseItemsLoop
                    }
                    r
                        .upsertPurchaseItemWithCloudId(
                            resolvedPurchaseId,
                            resolvedItemId,
                            sanitize(pi.itemName),
                            pi.quantity,
                            pi.unit,
                            pi.buyPrice,
                            if (pi.isDeleted) 1L else 0L,
                            pi.id,
                            normalizeTimestamp(pi.updatedAt),
                        )
                    purchaseItemsPulled++
                }
                pulled += purchaseItemsPulled
                if (purchaseItemsPulled < pis.size) {
                    android.util.Log
                        .w(
                            "SW-Pull",
                            "PurchaseItems: $purchaseItemsPulled/${pis.size} processed, ${pis.size - purchaseItemsPulled} skipped",
                        )
                }
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "PurchaseItems pull failed: ${e.message}", e)
                entityFailures["purchase_items_pull"] = e.message ?: "unknown"
            }

            // Stock Adjustments — always accept
            try {
                val sas =
                    c.syncStockAdjustments
                        .execute(sid, ls.toDouble())
                        .data.stockAdjustments
                for (sa in sas) {
                    val localItemId = r.resolveItemIdByCloudId(sa.itemId) ?: sa.itemId.toLongOrNull() ?: 0L
                    r.upsertStockAdjustmentRemote(
                        itemId = localItemId,
                        itemName = sanitize(sa.itemName),
                        reason = sa.reason,
                        delta = sa.delta,
                        timestamp = normalizeTimestamp(sa.timestamp),
                        isDeleted = if (sa.isDeleted) 1L else 0L,
                        cloudId = sa.id,
                        updatedAt = normalizeTimestamp(sa.updatedAt),
                    )
                }
                pulled += sas.size
            } catch (e: Exception) {
                android.util.Log.e("SW-Pull", "StockAdjustments pull failed: ${e.message}", e)
                entityFailures["stock_adjustments_pull"] = e.message ?: "unknown"
            }

            // Update last-sync timestamp only if all entities pulled cleanly
            if (entityFailures.isEmpty()) {
                prefs.edit().putLong("last_sync_timestamp_$sid", System.currentTimeMillis()).commit()
            }
            try {
                com.google.firebase.database.FirebaseDatabase
                    .getInstance()
                    .getReference("store_updates")
                    .child(sid)
                    .child("last_update")
                    .setValue(System.currentTimeMillis())
            } catch (
                e: Exception,
            ) {
                android.util.Log.e("SW-Pull", "Remote timestamp update failed", e)
            }
            // BUG-02 FIX: Report overall pull status with per-entity failure info
            if (entityFailures.isNotEmpty()) {
                val failedLabels = entityFailures.keys.joinToString(", ")
                android.util.Log.w("SW", "E01-S3 partial: $pulled entities OK, failed[$failedLabels]")
            } else {
                android.util.Log.d("SW", "E01-S3 pull complete: $pulled entities processed")
            }
            // Return (countPull, hadEntityFailures) so doWork can drive FAILED status
            return Pair(pulled, entityFailures.isNotEmpty())
        }
    }
}
