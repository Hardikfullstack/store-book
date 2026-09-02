package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.Udhaar
import com.storebook.inventoryapp.shared.domain.models.UdhaarEntry
import com.storebook.inventoryapp.shared.domain.models.CustomerBalance
import com.storebook.inventoryapp.shared.domain.models.CustomerDetailedBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UdhaarRepository(
    private val database: StoreBookDatabase
) {
    private val queries = database.storeBookQueries

    suspend fun getAllUdhaar(): List<UdhaarEntry> = withContext(Dispatchers.IO) {
        queries.getAllUdhaar().executeAsList().map { it.toDomain() }
    }

    suspend fun getCustomerLedger(customerName: String): List<UdhaarEntry> = withContext(Dispatchers.IO) {
        queries.getCustomerLedger(customerName).executeAsList().map { it.toDomain() }
    }

    /**
     * BUG-10 fix: Authoritative udhaar balance computed via SQL query.
     * Both local DB and Firebase Data Connect derive balance from the same aggregation logic,
     * so multi-device sync converges to identical results.
     */
    suspend fun getUdhaarBalances(): List<CustomerBalance> = withContext(Dispatchers.IO) {
        queries.getUdhaarCustomerBalance().executeAsList().map { row ->
            val netBalance = row.totalOutstanding - row.totalPaid
            CustomerBalance(row.customer_name, netBalance, row.lastTransactionTime ?: 0L)
        }
    }

    // E03-S2: Detailed balance breakdown — totalOutstanding + totalPaid per customer (SQL-backed)
    suspend fun getUdhaarBalancesWithBreakdown(): List<CustomerDetailedBalance> = withContext(Dispatchers.IO) {
        queries.getUdhaarCustomerBalance().executeAsList().map { row ->
            CustomerDetailedBalance(
                customerName = row.customer_name,
                totalOutstanding = row.totalOutstanding,
                totalPaid = row.totalPaid,
                currentBalance = row.totalOutstanding - row.totalPaid,
                lastTransactionTime = row.lastTransactionTime ?: 0L
            )
        }.sortedByDescending { it.currentBalance }
    }

    private fun Udhaar.toDomain() = UdhaarEntry(
        id = id,
        customerName = customer_name,
        amount = amount,
        type = type,
        timestamp = timestamp,
        notes = notes
    )

    suspend fun insertUdhaar(
        customerName: String, amount: Double, type: String, notes: String?
    ): Long = withContext(Dispatchers.IO) {
        database.transactionWithResult {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertUdhaar(customerName, amount, type, timestamp, notes, timestamp)
            queries.getLastInsertRowId().executeAsOne()
        }
    }

    // RP-A0: Push-sync methods
    suspend fun getUnsyncedUdhaars(): List<Udhaar> = withContext(Dispatchers.IO) {
        queries.getUnsyncedUdhaars().executeAsList()
    }

    suspend fun markUdhaarSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markUdhaarSynced(cloudId, id)
    }
}
