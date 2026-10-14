package com.storebook.inventoryapp.shared.test.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.repository.ExpenseRepository
import com.storebook.inventoryapp.shared.test.DatabaseTestHelper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExpenseRepositoryTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: JdbcSqliteDriver
    private lateinit var repository: ExpenseRepository

    @BeforeEach
    fun setup() {
        val (db, d) = DatabaseTestHelper.createDatabase()
        database = db
        driver = d as JdbcSqliteDriver
        repository = ExpenseRepository(database)
    }

    @Test
    fun `insertExpense and getAllExpenses returns inserted expenses`() = runBlocking {
        repository.insertExpense(
            type = "supplies",
            description = "Packaging tape & boxes",
            amount = 450.0,
            supplierName = "Box Traders",
            supplierPhone = "9876543210",
        )

        val expenses = repository.getAllExpenses()
        assertEquals(1, expenses.size)
        val first = expenses[0]
        assertEquals("supplies", first.type)
        assertEquals("Packaging tape & boxes", first.description)
        assertEquals(450.0, first.amount)
        assertEquals("Box Traders", first.supplierName)
        assertEquals("9876543210", first.supplierPhone)
    }

    @Test
    fun `softDeleteExpense marks expense as deleted so it is excluded from getAllExpenses`() = runBlocking {
        repository.insertExpense(
            type = "rent",
            description = "Shop Rent October",
            amount = 12000.0,
            supplierName = null,
            supplierPhone = null,
        )

        val initial = repository.getAllExpenses()
        assertEquals(1, initial.size)
        val id = initial[0].id

        repository.softDeleteExpense(id)

        val afterDelete = repository.getAllExpenses()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `getUnsyncedExpenses and markExpenseSynced updates cloud sync state`() = runBlocking {
        repository.insertExpense(
            type = "electricity",
            description = "Electricity Bill Sept",
            amount = 2300.0,
            supplierName = "Power Corp",
            supplierPhone = null,
        )

        val unsynced = repository.getUnsyncedExpenses()
        assertEquals(1, unsynced.size)
        val id = unsynced[0].id

        repository.markExpenseSynced(id, "cloud-uuid-exp-123")

        val remainingUnsynced = repository.getUnsyncedExpenses()
        assertTrue(remainingUnsynced.isEmpty())
    }

    @Test
    fun `getDailyExpensesByDateRange aggregates expenses correctly within range`() = runBlocking {
        val t1 = 1728000000000L
        val t2 = 1728000000000L + 86400000L

        database.storeBookQueries.insertExpense("supplies", "Paper rolls", 200.0, t1, null, null, t1)
        database.storeBookQueries.insertExpense("chai", "Tea & Snacks", 50.0, t1, null, null, t1)
        database.storeBookQueries.insertExpense("rent", "Rent", 5000.0, t2, null, null, t2)

        val day1Aggregates = repository.getDailyExpensesByDateRange(t1, t1)
        assertEquals(1, day1Aggregates.size)
        assertEquals(250.0, day1Aggregates[0].expense_total ?: 0.0, 0.01)

        val bothDaysAggregates = repository.getDailyExpensesByDateRange(t1, t2)
        assertEquals(2, bothDaysAggregates.size)
    }
}
