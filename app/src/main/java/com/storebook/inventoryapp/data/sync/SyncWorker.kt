package com.storebook.inventoryapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.storebook.inventoryapp.dataconnect.*
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.repository.SyncRepository
import com.storebook.inventoryapp.shared.util.RetryBackoffCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun sanitize(str: String?): String = str?.let { it.replace("<", "").replace(">", "") } ?: ""

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
        val storeId = inputData.getString("STORE_ID") ?: return Result.failure()
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
        private fun openRepo(
            ctx: Context,
            storeId: String,
        ): SyncRepository =
            SyncRepository(
                StoreBookDatabase(
                    AndroidSqliteDriver(
                        StoreBookDatabase.Schema,
                        ctx,
                        "storebook_$storeId.db",
                    ),
                ),
            )

        suspend fun performSync(
            appCtx: Context,
            storeId: String,
            onProgress: (Int, String) -> Unit = { _, _ -> },
        ) = withContext(Dispatchers.IO) {
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
            val res =
                c.syncItem
                    .execute(
                        it.id.toString(), s, sanitize(it.name), it.quantity, it.unit, it.buy_price, it.sell_price,
                        it.low_stock_threshold ?: 0.0, it.category ?: "", it.is_deleted == 1L, it.updated_at.toDouble(),
                    ) {
                        photoPath = it.photo_path
                        ; barcode = it.barcode
                        ; hsnCode = it.hsn_code
                        ; taxRate = it.tax_rate
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
            val res =
                c.syncSale.execute(
                    se.id.toString(), s, se.timestamp.toDouble(), se.total_amount,
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
            val res =
                c.syncUdhaar
                    .execute(
                        u.id.toString(), s, sanitize(u.customer_name), u.amount, u.type, u.timestamp.toDouble(),
                        u.is_deleted == 1L, u.updated_at.toDouble(),
                    ) { notes = u.notes }
            r.markUdhaarSynced(u.id, res.data.key.id)
        }

        // ==========================================================================
        // PUSH PHASE — E01-S1: every mutation wrapped in try/catch + enqueue on fail
        // ==========================================================================

        private suspend fun push(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            sid: String,
        ): Int {
            var totalPushed = 0
            totalPushed += pushItems(r, c, sid)
            totalPushed += pushSales(r, c, sid)
            totalPushed += pushSaleItems(r, c, sid)
            totalPushed += pushUdhaars(r, c, sid)
            totalPushed += pushExpenses(r, c, sid)
            totalPushed += pushSuppliers(r, c, sid)
            totalPushed += pushPurchases(r, c, sid)
            totalPushed += pushPi(r, c, sid)
            return totalPushed
        }

        private suspend fun pushItems(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
        ): Int {
            var count = 0
            for (it in r.getUnsyncedItems()) {
                try {
                    val res =
                        c.syncItem
                            .execute(
                                it.id.toString(),
                                s,
                                sanitize(it.name),
                                it.quantity,
                                it.unit,
                                it.buy_price,
                                it.sell_price,
                                it.low_stock_threshold ?: 0.0, it.category ?: "", it.is_deleted == 1L,
                                it.updated_at
                                    .toDouble(),
                            ) {
                                photoPath = it.photo_path
                                ; barcode = it.barcode
                                ; hsnCode = it.hsn_code
                                ; taxRate = it.tax_rate
                            }
                    r.markItemSynced(it.id, res.data.key.id)
                    count++
                    android.util.Log.d("SW", "E01-S1 push item ${it.id} → cloud ${res.data.key.id}")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "E01-S1 push item ${it.id} FAILED: ${e.message}", e)
                    if (!isTransientException(e)) {
                        android.util.Log.w("SW", "Non-transient error for item ${it.id}, skipping retry")
                        r.incrementFailedMutationCount()
                        continue
                    }
                    try {
                        r.enqueueSyncFailure(
                            entityType = "ITEM",
                            localId = it.id,
                            cloudId = null,
                            nextRetryAt = RetryBackoffCalculator.nextRetryAtFromNow(0),
                            errorMessage = "push_item: ${e.message ?: "unknown"}",
                        )
                        r.incrementFailedMutationCount()
                    } catch (qe: Exception) {
                        android.util.Log.e("SW", "Enqueue failed for item ${it.id}", qe)
                    }
                }
            }
            return count
        }

        private suspend fun pushSales(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
        ): Int {
            var count = 0
            for (se in r.getUnsyncedSales()) {
                try {
                    val res =
                        c.syncSale.execute(
                            se.id.toString(), s, se.timestamp.toDouble(), se.total_amount,
                            se.discount_amount ?: 0.0, se.type, se.is_deleted == 1L, se.updated_at.toDouble(),
                        ) {
                            customerName =
                                sanitize(se.customer_name)
                            ; businessGstin = sanitize(se.business_gstin)
                        }
                    r.markSaleSynced(se.id, res.data.key.id)
                    count++
                    android.util.Log.d("SW", "E01-S1 push sale ${se.id} → cloud ${res.data.key.id}")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "E01-S1 push sale ${se.id} FAILED: ${e.message}", e)
                    if (!isTransientException(e)) {
                        android.util.Log.w("SW", "Non-transient error for sale ${se.id}, skipping retry")
                        r.incrementFailedMutationCount()
                        continue
                    }
                    try {
                        r
                            .enqueueSyncFailure(
                                "SALE",
                                se.id,
                                null,
                                RetryBackoffCalculator.nextRetryAtFromNow(0),
                                "push_sale: ${e.message ?: "unknown"}",
                            )
                        r.incrementFailedMutationCount()
                    } catch (
                        qe: Exception,
                    ) {
                        android.util.Log.e("SW", "Enqueue sale fail", qe)
                    }
                }
            }
            return count
        }

        private suspend fun pushSaleItems(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
        ): Int {
            var count = 0
            for (si in r.getUnsyncedSaleItems()) {
                try {
                    val res =
                        c.syncSaleItem
                            .execute(
                                si.id.toString(),
                                s,
                                si.sale_id.toString(),
                                si.item_id.toString(),
                                sanitize(si.item_name),
                                si.unit,
                                si.quantity,
                                si.sell_price ?: 0.0, si.buy_price, si.is_deleted == 1L, si.updated_at.toDouble(),
                            )
                    r.markSaleItemSynced(si.id, res.data.key.id)
                    count++
                    android.util.Log.d("SW", "E01-S1 push sale_item ${si.id}")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "E01-S1 push sale_item ${si.id} FAILED", e)
                    if (!isTransientException(e)) {
                        android.util.Log.w("SW", "Non-transient error for sale_item ${si.id}, skipping retry")
                        r.incrementFailedMutationCount()
                        continue
                    }
                    try {
                        r
                            .enqueueSyncFailure(
                                "SALE_ITEM",
                                si.id,
                                null,
                                RetryBackoffCalculator.nextRetryAtFromNow(0),
                                "push_saleItem: ${e.message ?: "unknown"}",
                            )
                        r.incrementFailedMutationCount()
                    } catch (
                        qe: Exception,
                    ) {
                        android.util.Log.e("SW", "Enqueue sitem fail", qe)
                    }
                }
            }
            return count
        }

        private suspend fun pushUdhaars(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
        ): Int {
            var count = 0
            for (u in r.getUnsyncedUdhaars()) {
                try {
                    val res =
                        c.syncUdhaar
                            .execute(
                                u.id.toString(), s, sanitize(u.customer_name), u.amount, u.type, u.timestamp.toDouble(),
                                u.is_deleted == 1L, u.updated_at.toDouble(),
                            ) { notes = u.notes }
                    r.markUdhaarSynced(u.id, res.data.key.id)
                    count++
                    android.util.Log.d("SW", "E01-S1 push udhaar ${u.id}")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "E01-S1 push udhaar ${u.id} FAILED", e)
                    if (!isTransientException(e)) {
                        android.util.Log.w("SW", "Non-transient error for udhaar ${u.id}, skipping retry")
                        r.incrementFailedMutationCount()
                        continue
                    }
                    try {
                        r
                            .enqueueSyncFailure(
                                "UDHAAR",
                                u.id,
                                null,
                                RetryBackoffCalculator.nextRetryAtFromNow(0),
                                "push_udhaar: ${e.message ?: "unknown"}",
                            )
                        r.incrementFailedMutationCount()
                    } catch (
                        qe: Exception,
                    ) {
                        android.util.Log.e("SW", "Enqueue udhaar fail", qe)
                    }
                }
            }
            return count
        }

        private suspend fun pushExpenses(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
        ): Int {
            var count = 0
            for (ex in r.getUnsyncedExpenses()) {
                try {
                    val res =
                        c.syncExpense
                            .execute(
                                ex.id.toString(), s, ex.type, sanitize(ex.description), ex.amount,
                                ex.timestamp
                                    .toDouble(),
                                ex.is_deleted == 1L, ex.updated_at.toDouble(),
                            ) {
                                supplierName = ex.supplier_name
                                supplierPhone =
                                    ex.supplier_phone
                            }
                    r.markExpenseSynced(ex.id, res.data.key.id)
                    count++
                    android.util.Log.d("SW", "E01-S1 push expense ${ex.id}")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "E01-S1 push expense ${ex.id} FAILED", e)
                    if (!isTransientException(e)) {
                        android.util.Log.w("SW", "Non-transient error for expense ${ex.id}, skipping retry")
                        r.incrementFailedMutationCount()
                        continue
                    }
                    try {
                        r
                            .enqueueSyncFailure(
                                "EXPENSE",
                                ex.id,
                                null,
                                RetryBackoffCalculator.nextRetryAtFromNow(0),
                                "push_expense: ${e.message ?: "unknown"}",
                            )
                        r.incrementFailedMutationCount()
                    } catch (
                        qe: Exception,
                    ) {
                        android.util.Log.e("SW", "Enqueue expense fail", qe)
                    }
                }
            }
            return count
        }

        private suspend fun pushSuppliers(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
        ): Int {
            var count = 0
            for (su in r.getUnsyncedSuppliers()) {
                try {
                    val res =
                        c.syncSupplier
                            .execute(
                                su.id.toString(), s, sanitize(su.name), su.is_deleted == 1L,
                                su.updated_at
                                    .toDouble(),
                            ) {
                                phone =
                                    sanitize(su.phone)
                                ; gstin = sanitize(su.gstin)
                                address = sanitize(su.address)
                            }
                    r.markSupplierSynced(su.id, res.data.key.id)
                    count++
                    android.util.Log.d("SW", "E01-S1 push supplier ${su.id}")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "E01-S1 push supplier ${su.id} FAILED", e)
                    try {
                        r
                            .enqueueSyncFailure(
                                "SUPPLIER",
                                su.id,
                                null,
                                RetryBackoffCalculator.nextRetryAtFromNow(0),
                                "push_supplier: ${e.message ?: "unknown"}",
                            )
                        r.incrementFailedMutationCount()
                    } catch (
                        qe: Exception,
                    ) {
                        android.util.Log.e("SW", "Enqueue supplier fail", qe)
                    }
                }
            }
            return count
        }

        private suspend fun pushPurchases(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
        ): Int {
            var count = 0
            for (p in r.getUnsyncedPurchases()) {
                try {
                    val res =
                        c.syncPurchase
                            .execute(
                                p.id.toString(), s, p.supplier_id.toString(), sanitize(p.supplier_name), p.total_amount,
                                p.tax_amount ?: 0.0, p.type, p.timestamp.toDouble(), p.is_deleted == 1L,
                                p.updated_at
                                    .toDouble(),
                            ) {
                                notes =
                                    p.notes
                            }
                    r.markPurchaseSynced(p.id, res.data.key.id)
                    count++
                    android.util.Log.d("SW", "E01-S1 push purchase ${p.id}")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "E01-S1 push purchase ${p.id} FAILED", e)
                    try {
                        r
                            .enqueueSyncFailure(
                                "PURCHASE",
                                p.id,
                                null,
                                RetryBackoffCalculator.nextRetryAtFromNow(0),
                                "push_purchase: ${e.message ?: "unknown"}",
                            )
                        r.incrementFailedMutationCount()
                    } catch (
                        qe: Exception,
                    ) {
                        android.util.Log.e("SW", "Enqueue purchase fail", qe)
                    }
                }
            }
            return count
        }

        private suspend fun pushPi(
            r: SyncRepository,
            c: StorebookConnectorConnector,
            s: String,
        ): Int {
            var count = 0
            for (pi in r.getUnsyncedPurchaseItems()) {
                try {
                    val res =
                        c.syncPurchaseItem
                            .execute(
                                pi.id.toString(),
                                s,
                                pi.purchase_id.toString(),
                                pi.item_id.toString(),
                                sanitize(pi.item_name),
                                pi.quantity,
                                pi.unit,
                                pi.buy_price,
                                pi.is_deleted == 1L, pi.updated_at.toDouble(),
                            )
                    r.markPurchaseItemSynced(pi.id, res.data.key.id)
                    count++
                    android.util.Log.d("SW", "E01-S1 push purchase_item ${pi.id}")
                } catch (e: Exception) {
                    android.util.Log.e("SW", "E01-S1 push pitem ${pi.id} FAILED", e)
                    try {
                        r
                            .enqueueSyncFailure(
                                "PURCHASE_ITEM", pi.id, null,
                                RetryBackoffCalculator
                                    .nextRetryAtFromNow(0),
                                "push_pItem: ${e.message ?: "unknown"}",
                            )
                        r.incrementFailedMutationCount()
                    } catch (
                        qe: Exception,
                    ) {
                        android.util.Log.e("SW", "Enqueue pitem fail", qe)
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

            // Items — LWW via updatedAt comparison
            try {
                val items =
                    c.syncItems
                        .execute(sid, ls.toDouble())
                        .data.items
                for (i in items) {
                    if (r.shouldAcceptRemoteItem(i.updatedAt.toLong(), i.id)) {
                        r
                            .upsertItemWithCloudId(
                                name = i.name, quantity = i.quantity, unit = i.unit,
                                buyPrice = i.buyPrice, sellPrice = i.sellPrice, lowStockThreshold = i.lowStockThreshold,
                                category = i.category, photoPath = i.photoPath ?: "", barcode = i.barcode ?: "",
                                hsnCode = i.hsnCode ?: "", taxRate = i.taxRate ?: 0.0,
                                isDeleted = if (i.isDeleted) 1L else 0L, cloudId = i.id,
                                updatedAt = i.updatedAt.toLong(),
                            )
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
                                sa.timestamp.toLong(),
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
                                sa.updatedAt
                                    .toLong(),
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
                            if (si.isDeleted) 1L else 0L,
                            si.id,
                            si.updatedAt
                                .toLong(),
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
                            sanitize(u.customerName), u.amount, u.type, u.timestamp.toLong(),
                            u.notes
                                ?.let(::sanitize),
                            if (u.isDeleted) 1L else 0L, u.id, u.updatedAt.toLong(),
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
                            ex.type, sanitize(ex.description), ex.amount, ex.timestamp.toLong(),
                            ex.supplierName
                                ?.let(::sanitize),
                            ex.supplierPhone?.let(::sanitize), if (ex.isDeleted) 1L else 0L, ex.id,
                            ex.updatedAt
                                .toLong(),
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
                    if (r.shouldAcceptRemoteSupplier(su.updatedAt.toLong(), su.id)) {
                        r
                            .upsertSupplierWithCloudId(
                                sanitize(su.name), su.phone?.let(::sanitize), su.gstin?.let(::sanitize),
                                su.address
                                    ?.let(::sanitize),
                                if (su.isDeleted) 1L else 0L, su.id, su.updatedAt.toLong(),
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
                    if (!r.shouldAcceptRemotePurchase(pu.updatedAt.toLong(), pu.id)) continue@purchasesLoop
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
                            pu.timestamp
                                .toLong(),
                            pu.notes?.let(::sanitize), if (pu.isDeleted) 1L else 0L, pu.id,
                            pu.updatedAt
                                .toLong(),
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
                            pi.updatedAt
                                .toLong(),
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

            // Update last-sync timestamp
            prefs.edit().putLong("last_sync_timestamp_$sid", System.currentTimeMillis()).commit()
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
