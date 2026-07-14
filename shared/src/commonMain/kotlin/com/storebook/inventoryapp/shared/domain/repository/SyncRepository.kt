package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.Items
import com.storebook.inventoryapp.shared.data.local.Sales
import com.storebook.inventoryapp.shared.data.local.Sale_items
import com.storebook.inventoryapp.shared.data.local.Udhaar
import com.storebook.inventoryapp.shared.data.local.Expenses
import com.storebook.inventoryapp.shared.data.local.Suppliers
import com.storebook.inventoryapp.shared.data.local.Purchases
import com.storebook.inventoryapp.shared.data.local.Purchase_items
import com.storebook.inventoryapp.shared.data.local.Item_batches

/**
 * RP-A0: SQLDelight sync repository.
 * Replaces LegacySyncHelper — all push/pull operations now go through generated DAO methods.
 * Compile-time type safety for every query via .sq schema definitions.
 */
class SyncRepository(
    private val database: StoreBookDatabase,
) {
    private val queries = database.storeBookQueries

    // ========================================================================
    // PUSH PHASE — getUnsynced + markSynced per entity (8 entities total)
    // ========================================================================

    suspend fun pushUnsyncedItems(
        batchSize: Int = 100,
        syncToCloud: (List<Items>) -> List<Pair<Long, String>>, // (localId → cloudId)
    ): Int {
        val unsynced = queries.getActiveItems().executeAsList()
            .filter { it.is_synced == 0L }
        if (unsynced.isEmpty()) return 0

        val results = syncToCloud(unsynced)
        results.forEach { (localId, cloudId) ->
            queries.markItemSynced(cloudId, localId)
        }
        return results.size
    }

    suspend fun getUnsyncedItems(): List<Items> =
        queries.getUnsyncedItems().executeAsList()

    suspend fun markItemSynced(localId: Long, cloudId: String) =
        queries.markItemSynced(cloudId, localId)

    // --- Sales ---
    suspend fun getUnsyncedSales(): List<Sales> =
        queries.getUnsyncedSales().executeAsList()

    suspend fun markSaleSynced(localId: Long, cloudId: String) =
        queries.markSaleSynced(cloudId, localId)

    // --- Sale Items ---
    suspend fun getUnsyncedSaleItems(): List<Sale_items> =
        queries.getUnsyncedSaleItems().executeAsList()

    suspend fun markSaleItemSynced(localId: Long, cloudId: String) =
        queries.markSaleItemSynced(cloudId, localId)

    // --- Udhaar ---
    suspend fun getUnsyncedUdhaars(): List<Udhaar> =
        queries.getUnsyncedUdhaars().executeAsList()

    suspend fun markUdhaarSynced(localId: Long, cloudId: String) =
        queries.markUdhaarSynced(cloudId, localId)

    // --- Expenses ---
    suspend fun getUnsyncedExpenses(): List<Expenses> =
        queries.getUnsyncedExpenses().executeAsList()

    suspend fun markExpenseSynced(localId: Long, cloudId: String) =
        queries.markExpenseSynced(cloudId, localId)

    // --- Suppliers ---
    suspend fun getUnsyncedSuppliers(): List<Suppliers> =
        queries.getUnsyncedSuppliers().executeAsList()

    suspend fun markSupplierSynced(localId: Long, cloudId: String) =
        queries.markSupplierSynced(cloudId, localId)

    // --- Purchases ---
    suspend fun getUnsyncedPurchases(): List<Purchases> =
        queries.getUnsyncedPurchases().executeAsList()

    suspend fun markPurchaseSynced(localId: Long, cloudId: String) =
        queries.markPurchaseSynced(cloudId, localId)

    // --- Purchase Items ---
    suspend fun getUnsyncedPurchaseItems(): List<Purchase_items> =
        queries.getUnsyncedPurchaseItems().executeAsList()

    suspend fun markPurchaseItemSynced(localId: Long, cloudId: String) =
        queries.markPurchaseItemSynced(cloudId, localId)

    // --- Item Batches ---
    suspend fun getUnsyncedItemBatches(): List<Item_batches> =
        queries.getUnsyncedItemBatches().executeAsList()

    suspend fun markItemBatchSynced(localId: Long, cloudId: String) =
        queries.markItemBatchSynced(cloudId, localId)

    // ========================================================================
    // PULL PHASE — upsert (INSERT OR REPLACE) per entity from cloud data
    // ========================================================================

    suspend fun upsertItem(
        id: Long, name: String, quantity: Double, unit: String, buyPrice: Double,
        sellPrice: Double, lowStockThreshold: Double, category: String,
        photoPath: String?, hsnCode: String?, taxRate: Double,
        isDeleted: Long, cloudId: String?, updatedAt: Long,
    ) = queries.insertOrUpdateItem(
        id = id, name = name, quantity = quantity, unit = unit, buyPrice = buyPrice,
        sellPrice = sellPrice, lowStockThreshold = lowStockThreshold, category = category,
        photoPath = photoPath, hsnCode = hsnCode, taxRate = taxRate,
        isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
    )

    suspend fun upsertSale(
        id: Long, timestamp: Long, totalAmount: Double, discountAmount: Double,
        customerName: String?, customerGstin: String?, businessGstin: String?,
        customerAddress: String?, businessAddress: String?, type: String,
        notes: String?, isDeleted: Long, cloudId: String?, updatedAt: Long,
    ) = queries.insertOrUpdateSale(
        id = id, timestamp = timestamp, totalAmount = totalAmount, discountAmount = discountAmount,
        customerName = customerName, customerGstin = customerGstin, businessGstin = businessGstin,
        customerAddress = customerAddress, businessAddress = businessAddress, type = type,
        notes = notes, isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
    )

    suspend fun upsertSaleItem(
        id: Long, saleId: Long, itemId: Long, itemName: String, unit: String,
        quantity: Double, sellPrice: Double, buyPrice: Double,
        isDeleted: Long, cloudId: String?, updatedAt: Long,
    ) = queries.insertOrUpdateSaleItem(
        id = id, saleId = saleId, itemId = itemId, itemName = itemName, unit = unit,
        quantity = quantity, sellPrice = sellPrice, buyPrice = buyPrice,
        isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
    )

    suspend fun upsertUdhaar(
        id: Long, customerName: String, amount: Double, type: String,
        timestamp: Long, notes: String?, isDeleted: Long,
        cloudId: String?, updatedAt: Long,
    ) = queries.insertOrUpdateUdhaar(
        id = id, customerName = customerName, amount = amount, type = type,
        timestamp = timestamp, notes = notes, isDeleted = isDeleted,
        cloudId = cloudId, updatedAt = updatedAt,
    )

    suspend fun upsertExpense(
        id: Long, type: String, description: String, amount: Double,
        timestamp: Long, supplierName: String?, supplierPhone: String?,
        isDeleted: Long, cloudId: String?, updatedAt: Long,
    ) = queries.insertOrUpdateExpense(
        id = id, type = type, description = description, amount = amount,
        timestamp = timestamp, supplierName = supplierName, supplierPhone = supplierPhone,
        isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
    )

    suspend fun upsertSupplier(
        id: Long, name: String, phone: String?, gstin: String?, address: String?,
        isDeleted: Long, cloudId: String?, updatedAt: Long,
    ) = queries.insertOrUpdateSupplier(
        id = id, name = name, phone = phone, gstin = gstin, address = address,
        isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
    )

    suspend fun upsertPurchase(
        id: Long, supplierId: Long, supplierName: String, totalAmount: Double,
        taxAmount: Double, type: String, timestamp: Long, notes: String?,
        isDeleted: Long, cloudId: String?, updatedAt: Long,
    ) = queries.insertOrUpdatePurchase(
        id = id, supplierId = supplierId, supplierName = supplierName, totalAmount = totalAmount,
        taxAmount = taxAmount, type = type, timestamp = timestamp, notes = notes,
        isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
    )

    suspend fun upsertPurchaseItem(
        id: Long, purchaseId: Long, itemId: Long, itemName: String,
        quantity: Double, unit: String, buyPrice: Double,
        isDeleted: Long, cloudId: String?, updatedAt: Long,
    ) = queries.insertOrUpdatePurchaseItem(
        id = id, purchaseId = purchaseId, itemId = itemId, itemName = itemName,
        quantity = quantity, unit = unit, buyPrice = buyPrice,
        isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
    )

    suspend fun upsertItemBatch(
        id: Long, itemId: Long, batchNumber: String?, expiryDate: Long?,
        quantity: Double, costPrice: Double, timestamp: Long, notes: String?,
        isDeleted: Long, cloudId: String?, updatedAt: Long,
    ) = queries.insertOrUpdateItemBatch(
        id = id, itemId = itemId, batchNumber = batchNumber, expiryDate = expiryDate,
        quantity = quantity, costPrice = costPrice, timestamp = timestamp, notes = notes,
        isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
    )

    // ========================================================================
    // UPSERT REMOTE — assign a guaranteed-unique local id, then upsert by cloud_id
    // Solves the bug where non-numeric Firestore IDs ("abc123") all converted to 0L
    // and overwrote each other via INSERT OR REPLACE at id=0
    // ========================================================================

    suspend fun upsertItemWithCloudId(
        name: String, quantity: Double, unit: String, buyPrice: Double,
        sellPrice: Double, lowStockThreshold: Double, category: String,
        photoPath: String?, hsnCode: String?, taxRate: Double,
        isDeleted: Long, cloudId: String, updatedAt: Long,
    ) {
        database.transaction { queries.upsertItemRemote(
            name = name, quantity = quantity, unit = unit, buyPrice = buyPrice,
            sellPrice = sellPrice, lowStockThreshold = lowStockThreshold, category = category,
            photoPath = photoPath, hsnCode = hsnCode, taxRate = taxRate,
            isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
        ) }
    }

    suspend fun upsertSaleWithCloudId(
        timestamp: Long, totalAmount: Double, discountAmount: Double,
        customerName: String?, type: String, notes: String?,
        isDeleted: Long, cloudId: String, updatedAt: Long,
    ) {
        database.transaction { queries.upsertSaleRemote(
            timestamp = timestamp, totalAmount = totalAmount, discountAmount = discountAmount,
            customerName = customerName, customerGstin = null, businessGstin = null,
            customerAddress = null, businessAddress = null, type = type, notes = notes,
            isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
        ) }
    }

    suspend fun upsertSaleItemWithCloudId(
        saleId: Long, itemId: Long, itemName: String, unit: String,
        quantity: Double, sellPrice: Double, buyPrice: Double,
        isDeleted: Long, cloudId: String, updatedAt: Long,
    ) {
        database.transaction { queries.upsertSaleItemRemote(
            saleId = saleId, itemId = itemId, itemName = itemName, unit = unit,
            quantity = quantity, sellPrice = sellPrice, buyPrice = buyPrice,
            isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
        ) }
    }

    suspend fun upsertUdhaarWithCloudId(
        customerName: String, amount: Double, type: String,
        timestamp: Long, notes: String?, isDeleted: Long,
        cloudId: String, updatedAt: Long,
    ) {
        database.transaction { queries.upsertUdhaarRemote(
            customerName = customerName, amount = amount, type = type,
            timestamp = timestamp, notes = notes, isDeleted = isDeleted,
            cloudId = cloudId, updatedAt = updatedAt,
        ) }
    }

    suspend fun upsertExpenseWithCloudId(
        type: String, description: String, amount: Double,
        timestamp: Long, supplierName: String?, supplierPhone: String?,
        isDeleted: Long, cloudId: String, updatedAt: Long,
    ) {
        database.transaction { queries.upsertExpenseRemote(
            type = type, description = description, amount = amount,
            timestamp = timestamp, supplierName = supplierName, supplierPhone = supplierPhone,
            isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
        ) }
    }

    suspend fun upsertSupplierWithCloudId(
        name: String, phone: String?, gstin: String?, address: String?,
        isDeleted: Long, cloudId: String, updatedAt: Long,
    ) {
        database.transaction { queries.upsertSupplierRemote(
            name = name, phone = phone, gstin = gstin, address = address,
            isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
        ) }
    }

    suspend fun upsertPurchaseWithCloudId(
        supplierId: Long, supplierName: String, totalAmount: Double, taxAmount: Double,
        type: String, timestamp: Long, notes: String?,
        isDeleted: Long, cloudId: String, updatedAt: Long,
    ) {
        database.transaction { queries.upsertPurchaseRemote(
            supplierId = supplierId, supplierName = supplierName, totalAmount = totalAmount,
            taxAmount = taxAmount, type = type, timestamp = timestamp, notes = notes,
            isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
        ) }
    }

    suspend fun upsertPurchaseItemWithCloudId(
        purchaseId: Long, itemId: Long, itemName: String,
        quantity: Double, unit: String, buyPrice: Double,
        isDeleted: Long, cloudId: String, updatedAt: Long,
    ) {
        database.transaction { queries.upsertPurchaseItemRemote(
            purchaseId = purchaseId, itemId = itemId, itemName = itemName,
            quantity = quantity, unit = unit, buyPrice = buyPrice,
            isDeleted = isDeleted, cloudId = cloudId, updatedAt = updatedAt,
        ) }
    }

    // ========================================================================
    // CLOUD ID RESOLVERS — map cloud entity IDs to local auto-increment IDs
    // ========================================================================

    suspend fun resolveItemIdByCloudId(cloudId: String): Long? =
        queries.resolveItemIdByCloudId(cloudId).executeAsOneOrNull()

    suspend fun resolveSaleIdByCloudId(cloudId: String): Long? =
        queries.resolveSaleIdByCloudId(cloudId).executeAsOneOrNull()

    suspend fun resolveSupplierIdByCloudId(cloudId: String): Long? =
        queries.resolveSupplierIdByCloudId(cloudId).executeAsOneOrNull()

    suspend fun resolvePurchaseIdByCloudId(cloudId: String): Long? =
        queries.resolvePurchaseIdByCloudId(cloudId).executeAsOneOrNull()

    // ========================================================================
    // UTILITY — clear all entities (used during full re-sync)
    // ========================================================================

    suspend fun clearAllData() {
        queries.clearItems()
        queries.clearSales()
        queries.clearSaleItems()
        queries.clearUdhaar()
        queries.clearExpenses()
        queries.clearSuppliers()
        queries.clearPurchases()
        queries.clearPurchaseItems()
        queries.clearItemBatches()
    }

    // ========================================================================
    // FAILED SYNC QUEUE — E01-S2: Retry persistence for failed mutations
    // ========================================================================

    /** Enqueue a failed mutation for retry with exponential backoff */
    suspend fun enqueueSyncFailure(
        entityType: String,
        localId: Long,
        cloudId: String?,
        nextRetryAt: Long,
        errorMessage: String
    ) {
        queries.enqueueSyncFailure(entityType, localId, cloudId, nextRetryAt, errorMessage)
    }

    /** Get all failed mutations that are overdue for retry */
    suspend fun getOverdueForRetry(): List<com.storebook.inventoryapp.shared.data.local.Failed_sync_queue> {
        return queries.getOverdueForRetry(System.currentTimeMillis()).executeAsList()
    }

    /** Update retry state — increment count and schedule next attempt (or mark permanent) */
    suspend fun updateRetryState(id: Long, nextRetryAt: Long, errorMessage: String) {
        queries.updateRetryState(nextRetryAt, errorMessage, id)
    }

    /** Mark a failed mutation as permanently failed after max retries exhausted */
    suspend fun markPermanentFailure(id: Long) {
        queries.markPermanentFailure(id)
    }

    /** Remove entry after successful retry */
    suspend fun dequeueFailedSyncById(id: Long) {
        queries.dequeueFailedSyncById(id)
    }

    /** Get all permanent failures (for Settings > Sync Issues screen — E01-S2) */
    suspend fun getPermanentFailures(): List<com.storebook.inventoryapp.shared.data.local.Failed_sync_queue> {
        return queries.getPermanentFailures().executeAsList()
    }

    /** Count of pending retries */
    suspend fun getPendingFailureCount(): Long? {
        return queries.getPendingFailureCount().executeAsOneOrNull()
    }

    // ========================================================================
    // SYNC STATE — E01-S1: Track overall sync health (updated only on full success)
    // ========================================================================

    /** Get current sync state */
    suspend fun getSyncState(): com.storebook.inventoryapp.shared.data.local.Sync_state? {
        return queries.getSyncState().executeAsOneOrNull()
    }

    /** Update sync state — only called after FULL successful push+pull batch (E01-S1) */
    suspend fun updateSyncState(
        lastFullSyncAt: Long = System.currentTimeMillis(),
        lastPushBatchCount: Int,
        lastPullBatchCount: Int,
        totalFailedMutations: Long = 0L,
        status: String = "DONE"
    ) {
        queries.upsertSyncState(lastFullSyncAt, lastPushBatchCount.toLong(), lastPullBatchCount.toLong(), totalFailedMutations, status)
    }

    /** Set sync status indicator (IDLE, PUSHING, PULLING, DONE, FAILED) */
    suspend fun setSyncStatus(status: String) {
        queries.setSyncStatus(status)
    }

    /** Increment failure count on partial batch failures */
    suspend fun incrementFailedMutationCount() {
        queries.incrementFailedMutationCount()
    }

    // ========================================================================
    // E01-S3: CONFLICT RESOLUTION — Last-Write-Wins (LWW) strategy
    // Compare remote updatedAt vs local updatedAt. If remote >= local → accept.
    // Else keep local (will be re-pushed on next run).
    // Sales are append-only by ID uniqueness — no LWW needed.
    // ========================================================================

    /** @return true if we should accept the remote entity (remote is newer or doesn't exist locally) */
    suspend fun shouldAcceptRemote(remoteUpdatedAt: Long, cloudId: String): Boolean {
        val local = queries.getLocalItemByCloudId(cloudId).executeAsOneOrNull()
            ?: return true // no local record → always accept
        return remoteUpdatedAt >= (local.updated_at ?: 0L)
    }

    /** Check if a sale already exists locally (append-only — reject duplicates) */
    suspend fun saleExistsLocally(cloudId: String): Boolean {
        return queries.resolveSaleIdByCloudId(cloudId).executeAsOneOrNull() != null
    }

    suspend fun saleItemExistsLocally(cloudId: String): Boolean {
        return queries.resolveSaleItemIdByCloudId(cloudId).executeAsOneOrNull() != null
    }

    suspend fun purchaseItemExistsLocally(cloudId: String): Boolean {
        return queries.resolvePurchaseItemIdByCloudId(cloudId).executeAsOneOrNull() != null
    }

    fun getQueries() = queries
}
