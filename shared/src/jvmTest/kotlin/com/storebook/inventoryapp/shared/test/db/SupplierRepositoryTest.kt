package com.storebook.inventoryapp.shared.test.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.repository.PurchaseRepository
import com.storebook.inventoryapp.shared.domain.repository.SupplierRepository
import com.storebook.inventoryapp.shared.test.DatabaseTestHelper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SupplierRepositoryTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: JdbcSqliteDriver
    private lateinit var supplierRepository: SupplierRepository
    private lateinit var purchaseRepository: PurchaseRepository

    @BeforeEach
    fun setup() {
        val (db, d) = DatabaseTestHelper.createDatabase()
        database = db
        driver = d as JdbcSqliteDriver
        supplierRepository = SupplierRepository(database)
        purchaseRepository = PurchaseRepository(database)
    }

    @AfterEach
    fun teardown() {
        DatabaseTestHelper.dropDatabase(driver)
    }

    @Test
    fun `getSupplierBalances returns all suppliers even with zero purchases`() = runBlocking {
        // Insert 4 suppliers (simulating 4 suppliers in store)
        val s1 = supplierRepository.insertSupplier("Supplier A", "1111111111", null, null)
        val s2 = supplierRepository.insertSupplier("Supplier B", "2222222222", null, null)
        val s3 = supplierRepository.insertSupplier("Supplier C", "3333333333", null, null)
        val s4 = supplierRepository.insertSupplier("Supplier D", "4444444444", null, null)

        // Only 3 of the 4 suppliers have purchases
        // s1: Bill 1000, Payment 200 -> netBalance = 800 (Payable)
        purchaseRepository.insertPurchase(s1, "Supplier A", 1000.0, 0.0, "BILL", "Invoice #1")
        purchaseRepository.insertPurchase(s1, "Supplier A", 200.0, 0.0, "PAYMENT", "Payment #1")

        // s2: Bill 500, Payment 500 -> netBalance = 0 (Settled)
        purchaseRepository.insertPurchase(s2, "Supplier B", 500.0, 0.0, "BILL", "Invoice #2")
        purchaseRepository.insertPurchase(s2, "Supplier B", 500.0, 0.0, "PAYMENT", "Payment #2")

        // s3: Bill 300, Payment 500 -> netBalance = -200 (Advance)
        purchaseRepository.insertPurchase(s3, "Supplier C", 300.0, 0.0, "BILL", "Invoice #3")
        purchaseRepository.insertPurchase(s3, "Supplier C", 500.0, 0.0, "PAYMENT", "Payment #3")

        // s4 has NO purchases at all

        val balances = supplierRepository.getSupplierBalances(purchaseRepository)

        // Must return ALL 4 suppliers!
        assertEquals(4, balances.size)

        val balanceMap = balances.associateBy { it.supplierId }
        assertNotNull(balanceMap[s1])
        assertNotNull(balanceMap[s2])
        assertNotNull(balanceMap[s3])
        assertNotNull(balanceMap[s4])

        assertEquals(800.0, balanceMap[s1]?.netBalance)
        assertEquals(0.0, balanceMap[s2]?.netBalance)
        assertEquals(-200.0, balanceMap[s3]?.netBalance)
        assertEquals(0.0, balanceMap[s4]?.netBalance)
    }

    @Test
    fun `getSupplierBalances returns all suppliers with 0 balance when purchases empty`() = runBlocking {
        supplierRepository.insertSupplier("Vendor 1", null, null, null)
        supplierRepository.insertSupplier("Vendor 2", null, null, null)

        val balances = supplierRepository.getSupplierBalances(purchaseRepository)

        assertEquals(2, balances.size)
        assertEquals(0.0, balances[0].netBalance)
        assertEquals(0.0, balances[1].netBalance)
    }
}
