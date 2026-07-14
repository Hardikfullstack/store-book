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

    suspend fun getUdhaarBalances(): List<CustomerBalance> = withContext(Dispatchers.IO) {
        val allUdhaar = queries.getAllUdhaar().executeAsList()
        val balances = mutableMapOf<String, Double>()
        val lastTimes = mutableMapOf<String, Long>()
        
        allUdhaar.forEach { entity ->
            val amt = if (entity.type == "CREDIT") entity.amount else -entity.amount
            balances[entity.customer_name] = (balances[entity.customer_name] ?: 0.0) + amt
            val currentLastTime = lastTimes[entity.customer_name] ?: 0L
            if (entity.timestamp > currentLastTime) {
                lastTimes[entity.customer_name] = entity.timestamp
            }
        }
        
        balances.map { (name, netBalance) ->
            CustomerBalance(name, netBalance, lastTimes[name] ?: 0L)
        }
    }

    // E03-S2: Detailed balance breakdown — totalOutstanding + totalPaid per customer
    suspend fun getUdhaarBalancesWithBreakdown(): List<CustomerDetailedBalance> = withContext(Dispatchers.IO) {
        val allUdhaar = queries.getAllUdhaar().executeAsList()
        val outstandingMap = mutableMapOf<String, Double>()
        val paidMap = mutableMapOf<String, Double>()
        val lastTimes = mutableMapOf<String, Long>()

        allUdhaar.forEach { entity ->
            val name = entity.customer_name
            if (entity.type == "CREDIT") {
                outstandingMap[name] = (outstandingMap[name] ?: 0.0) + entity.amount
            } else {
                paidMap[name] = (paidMap[name] ?: 0.0) + entity.amount
            }
            val currentLastTime = lastTimes[name] ?: 0L
            if (entity.timestamp > currentLastTime) {
                lastTimes[name] = entity.timestamp
            }
        }

        // Dedupe customer names from both maps
        val allNames = mutableSetOf<String>()
        allNames.addAll(outstandingMap.keys)
        allNames.addAll(paidMap.keys)

        allNames.map { name ->
            val totalOut = outstandingMap[name] ?: 0.0
            val totalPd = paidMap[name] ?: 0.0
            CustomerDetailedBalance(
                customerName = name,
                totalOutstanding = totalOut,
                totalPaid = totalPd,
                currentBalance = totalOut - totalPd,
                lastTransactionTime = lastTimes[name] ?: 0L
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
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertUdhaar(customerName, amount, type, timestamp, notes, timestamp)
        }
        -1L
    }

    // RP-A0: Push-sync methods
    suspend fun getUnsyncedUdhaars(): List<Udhaar> = withContext(Dispatchers.IO) {
        queries.getUnsyncedUdhaars().executeAsList()
    }

    suspend fun markUdhaarSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markUdhaarSynced(cloudId, id)
    }
}
