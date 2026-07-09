package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.Udhaar
import com.storebook.inventoryapp.shared.domain.models.UdhaarEntry
import com.storebook.inventoryapp.shared.domain.models.CustomerBalance
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
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        queries.insertUdhaar(customerName, amount, type, timestamp, notes, timestamp)
        -1L
    }
}
