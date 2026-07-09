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
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        queries.insertExpense(type, description, amount, timestamp, supplierName, supplierPhone, timestamp)
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
}
