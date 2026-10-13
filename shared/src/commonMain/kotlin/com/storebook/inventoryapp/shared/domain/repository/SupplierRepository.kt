package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.Suppliers
import com.storebook.inventoryapp.shared.domain.models.SupplierBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupplierRepository(
    private val database: StoreBookDatabase
) {
    private val queries = database.storeBookQueries

    suspend fun getAllSuppliers(): List<Suppliers> = withContext(Dispatchers.IO) {
        queries.getAllSuppliers().executeAsList()
    }

    // E02-S1: Transaction-wrapped mutations
    suspend fun insertSupplier(
        name: String, phone: String?, gstin: String?, address: String?
    ): Long = withContext(Dispatchers.IO) {
        database.transactionWithResult {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertSupplier(name, phone, gstin, address, timestamp)
            queries.getLastInsertRowId().executeAsOne()
        }
    }

    suspend fun deleteSupplier(id: Long) = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.softDeleteSupplier(timestamp, id)
        }
    }

    suspend fun getSupplierBalances(purchaseRepository: PurchaseRepository): List<SupplierBalance> = withContext(Dispatchers.IO) {
        val allPurchases = purchaseRepository.getAllPurchases()
        val balances = mutableMapOf<Long, Double>()
        val lastTimes = mutableMapOf<Long, Long>()
        allPurchases.forEach { purchase ->
            val amt = if (purchase.type == "PAYMENT") -purchase.total_amount else purchase.total_amount
            balances[purchase.supplier_id] = (balances[purchase.supplier_id] ?: 0.0) + amt
            val currentLast = lastTimes[purchase.supplier_id] ?: 0L
            if (purchase.timestamp > currentLast) {
                lastTimes[purchase.supplier_id] = purchase.timestamp
            }
        }
        val suppliers = queries.getAllSuppliers().executeAsList().sortedBy { it.name.lowercase() }
        suppliers.map { supplier ->
            SupplierBalance(
                supplierId = supplier.id,
                supplierName = supplier.name,
                phone = supplier.phone,
                netBalance = balances[supplier.id] ?: 0.0,
                lastTransactionTime = lastTimes[supplier.id] ?: 0L,
            )
        }
    }

    // RP-A0: Push-sync methods
    suspend fun getUnsyncedSuppliers(): List<Suppliers> = withContext(Dispatchers.IO) {
        queries.getUnsyncedSuppliers().executeAsList()
    }

    suspend fun markSupplierSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markSupplierSynced(cloudId, id)
    }
}
