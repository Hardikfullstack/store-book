package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.Expenses
import com.storebook.inventoryapp.shared.domain.models.ExpenseEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val database: StoreBookDatabase
) {
    private val queries = database.storeBookQueries

    suspend fun getAllExpenses(): List<ExpenseEntry> = withContext(Dispatchers.IO) {
        queries.getAllExpenses().executeAsList().map { it.toDomain() }
    }

    suspend fun insertExpense(
        type: String, description: String, amount: Double,
        supplierName: String?, supplierPhone: String?
    ): Long = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertExpense(type, description, amount, timestamp, supplierName, supplierPhone, timestamp)
        }
        -1L
    }

    private fun Expenses.toDomain() = ExpenseEntry(
        id = id,
        type = type,
        description = description,
        amount = amount,
        timestamp = timestamp,
        supplierName = supplier_name,
        supplierPhone = supplier_phone
    )

    // RP-A0: Push-sync methods
    suspend fun getUnsyncedExpenses(): List<Expenses> = withContext(Dispatchers.IO) {
        queries.getUnsyncedExpenses().executeAsList()
    }

    suspend fun markExpenseSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markExpenseSynced(cloudId, id)
    }

    // E03-S3: Daily expense aggregates by date range
    suspend fun getDailyExpensesByDateRange(startTs: Long, endTs: Long): List<com.storebook.inventoryapp.shared.data.local.GetDailyExpensesByDateRange> =
        withContext(Dispatchers.IO) {
            queries.getDailyExpensesByDateRange(startTs, endTs).executeAsList()
        }
}
