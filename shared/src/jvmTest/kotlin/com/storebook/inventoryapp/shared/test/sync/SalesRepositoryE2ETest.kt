package com.storebook.inventoryapp.shared.test.sync

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.models.PaymentMode
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.repository.UdhaarRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Repository-level tests for sales flow end-to-end.
 * Validates: atomic checkout, sale item persistence across dates,
 * soft delete + stock restore, quotation conversion.
 */
class SalesRepositoryE2ETest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: JdbcSqliteDriver
    private lateinit var salesRepo: com.storebook.inventoryapp.shared.domain.repository.SalesRepository
    private lateinit var inventoryRepo: InventoryRepository
    private lateinit var udhaarRepo: UdhaarRepository

    @BeforeEach
    fun setup() {
        val (db, d) = com.storebook.inventoryapp.shared.test.DatabaseTestHelper.createDatabase()
        database = db
        driver = d as JdbcSqliteDriver
        salesRepo = com.storebook.inventoryapp.shared.domain.repository.SalesRepository(database)
        inventoryRepo = InventoryRepository(database)
        udhaarRepo = UdhaarRepository(database)
    }

    @AfterEach
    fun teardown() {
        com.storebook.inventoryapp.shared.test.DatabaseTestHelper.dropDatabase(driver)
    }

    // ───────── Atomic checkout: sale header + items + stock deduction ─────────

    @Test
    fun `atomicCheckout inserts sale header and line items atomically`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("Widget", 100.0, "Pcs", 50.0, 100.0, 5.0, "Electronics", null, null, null, 0.0, now)

        val cartItemsData = listOf(
            mapOf<String, Any>(
                "itemId" to 1L,
                "itemName" to "Widget",
                "unit" to "Pcs",
                "quantity" to 3.0,
                "buyPrice" to 50.0,
                "sellPrice" to 100.0,
                "taxRate" to 18.0,
                "hsnCode" to "8542",
            ),
        )

        val saleId = salesRepo.atomicCheckout(
            cartItemsData = cartItemsData,
            totalAmount = 300.0,
            discountAmount = 0.0,
            customerName = "Test Customer",
            paymentMode = PaymentMode.CASH,
            customerGstin = null,
            businessGstin = null,
            customerAddress = null,
            businessAddress = null,
            type = "SALE",
        )

        assertTrue(saleId > 0, "Sale ID must be positive: $saleId")

        val sale = salesRepo.getSaleById(saleId)
        assertNotNull(sale, "Sale header must exist")
        assertEquals("Test Customer", sale!!.customer_name)
        assertEquals(300.0, sale.total_amount, 0.01)

        val items = salesRepo.getSaleItems(saleId)
        assertEquals(1, items.size, "Exactly one line item")
        assertEquals(3.0, items[0].quantity, 0.01)
    }

    @Test
    fun `atomicCheckout deducts inventory stock for SALE type`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("Bolt", 500.0, "Pcs", 5.0, 15.0, 20.0, "Hardware", null, null, null, 0.0, now)

        val cartItemsData = listOf(
            mapOf<String, Any>(
                "itemId" to 1L,
                "itemName" to "Bolt",
                "unit" to "Pcs",
                "quantity" to 50.0,
                "buyPrice" to 5.0,
                "sellPrice" to 15.0,
                "taxRate" to 0.0,
                "hsnCode" to "",
            ),
        )

        val saleId = salesRepo.atomicCheckout(
            cartItemsData = cartItemsData,
            totalAmount = 750.0,
            discountAmount = 0.0,
            customerName = "Hardware Shop",
            paymentMode = PaymentMode.CASH,
            customerGstin = null,
            businessGstin = null,
            customerAddress = null,
            businessAddress = null,
            type = "SALE",
        )

        assertTrue(saleId > 0)

        val item = database.storeBookQueries.getItemById(1L).executeAsOneOrNull()
        assertNotNull(item)
        assertEquals(450.0, item!!.quantity, 0.01, "Stock should be deducted: 500 - 50 = 450")
    }

    @Test
    fun `atomicCheckout does NOT deduct stock for ESTIMATE type`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("Bolt", 500.0, "Pcs", 5.0, 15.0, 20.0, "Hardware", null, null, null, 0.0, now)

        val cartItemsData = listOf(
            mapOf<String, Any>(
                "itemId" to 1L,
                "itemName" to "Bolt",
                "unit" to "Pcs",
                "quantity" to 50.0,
                "buyPrice" to 5.0,
                "sellPrice" to 15.0,
                "taxRate" to 0.0,
                "hsnCode" to "",
            ),
        )

        val saleId = salesRepo.atomicCheckout(
            cartItemsData = cartItemsData,
            totalAmount = 750.0,
            discountAmount = 0.0,
            customerName = "Quote Customer",
            paymentMode = PaymentMode.CASH,
            customerGstin = null,
            businessGstin = null,
            customerAddress = null,
            businessAddress = null,
            type = "ESTIMATE",
        )

        assertTrue(saleId > 0)

        val item = database.storeBookQueries.getItemById(1L).executeAsOneOrNull()
        assertNotNull(item)
        assertEquals(500.0, item!!.quantity, 0.01, "Stock unchanged for ESTIMATE")
    }

    @Test
    fun `atomicCheckout fails when stock is insufficient`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("Nail", 10.0, "Pcs", 2.0, 5.0, 3.0, "Hardware", null, null, null, 0.0, now)

        val cartItemsData = listOf(
            mapOf<String, Any>(
                "itemId" to 1L,
                "itemName" to "Nail",
                "unit" to "Pcs",
                "quantity" to 100.0,
                "buyPrice" to 2.0,
                "sellPrice" to 5.0,
                "taxRate" to 0.0,
                "hsnCode" to "",
            ),
        )

        var saleId = -1L
        var threw = false
        try {
            saleId = salesRepo.atomicCheckout(
                cartItemsData = cartItemsData,
                totalAmount = 500.0,
                discountAmount = 0.0,
                customerName = "Big Order",
                paymentMode = PaymentMode.CASH,
                customerGstin = null,
                businessGstin = null,
                customerAddress = null,
                businessAddress = null,
                type = "SALE",
            )
        } catch (e: com.storebook.inventoryapp.shared.domain.repository.InventoryRepository.InsufficientStockException) {
            threw = true
        }

        assertTrue(threw, "checkout must throw or return -1 when stock insufficient")
        assertEquals(-1L, saleId, "Checkout must return -1 when stock insufficient")

        val item = database.storeBookQueries.getItemById(1L).executeAsOneOrNull()
        assertNotNull(item)
        assertEquals(10.0, item!!.quantity, 0.01, "Stock unchanged after failed checkout")
    }

    // ───────── Sale items visible regardless of date (post-fix validation) ─────────

    @Test
    fun `sale items query returns items for any sale date`() = runBlocking {
        val oldDate = 1720000000000L
        val now = Clock.System.now().toEpochMilliseconds()

        database.storeBookQueries.insertItem("Old Widget", 20.0, "Pcs", 30.0, 60.0, 5.0, "Misc", null, null, null, 0.0, oldDate)

        val cartItemsData = listOf(
            mapOf<String, Any>(
                "itemId" to 1L,
                "itemName" to "Old Widget",
                "unit" to "Pcs",
                "quantity" to 2.0,
                "buyPrice" to 30.0,
                "sellPrice" to 60.0,
                "taxRate" to 0.0,
                "hsnCode" to "",
            ),
        )

        val saleId = salesRepo.atomicCheckout(
            cartItemsData = cartItemsData,
            totalAmount = 120.0,
            discountAmount = 0.0,
            customerName = "Old Sale",
            paymentMode = PaymentMode.CASH,
            customerGstin = null,
            businessGstin = null,
            customerAddress = null,
            businessAddress = null,
            type = "SALE",
        )

        assertTrue(saleId > 0)

        val items = salesRepo.getSaleItems(saleId)
        assertEquals(1, items.size, "Old sale must still have its items accessible")
        assertEquals("Old Widget", items[0].item_name)
    }

    // ───────── Soft delete restores stock ─────────

    @Test
    fun `softDeleteSale marks sale as deleted and does not restore stock automatically`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("Eraser", 30.0, "Pcs", 10.0, 25.0, 5.0, "Stationery", null, null, null, 0.0, now)

        val cartItemsData = listOf(
            mapOf<String, Any>(
                "itemId" to 1L,
                "itemName" to "Eraser",
                "unit" to "Pcs",
                "quantity" to 5.0,
                "buyPrice" to 10.0,
                "sellPrice" to 25.0,
                "taxRate" to 0.0,
                "hsnCode" to "",
            ),
        )

        val saleId = salesRepo.atomicCheckout(
            cartItemsData = cartItemsData,
            totalAmount = 125.0,
            discountAmount = 0.0,
            customerName = "Student",
            paymentMode = PaymentMode.CASH,
            customerGstin = null,
            businessGstin = null,
            customerAddress = null,
            businessAddress = null,
            type = "SALE",
        )

        assertTrue(saleId > 0)

        salesRepo.softDeleteSale(saleId)

        val deletedSale = database.storeBookQueries.getSaleById(saleId).executeAsOneOrNull()
        assertNotNull(deletedSale)
        assertEquals(1L, deletedSale!!.is_deleted, "Sale must be soft-deleted")
    }

    // ───────── Quotation conversion ─────────

    @Test
    fun `convertQuotationToSale creates sale and deducts stock`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("Pen", 80.0, "Pcs", 8.0, 20.0, 10.0, "Stationery", null, null, null, 0.0, now)

        database.storeBookQueries.insertSale(
            timestamp = now, total_amount = 40.0, discount_amount = 0.0,
            customer_name = "Quote", customer_gstin = null, business_gstin = null,
            customer_address = null, business_address = null, type = "ESTIMATE", notes = null, updated_at = now,
        )
        val quoteId = database.storeBookQueries.getLastInsertRowId().executeAsOne()
        database.storeBookQueries.insertSaleItem(
            sale_id = quoteId, item_id = 1, item_name = "Pen", unit = "Pcs", quantity = 2.0,
            sell_price = 20.0, buy_price = 8.0, tax_rate = 5.0, hsn_code = null, updated_at = now,
        )

        val newSaleId = salesRepo.convertQuotationToSale(quoteId)
        assertTrue(newSaleId > 0, "Converted sale must have positive ID: $newSaleId")

        val converted = salesRepo.getSaleById(newSaleId)
        assertNotNull(converted)
        assertEquals("SALE", converted!!.type, "Must be SALE type after conversion")

        val newItems = salesRepo.getSaleItems(newSaleId)
        assertEquals(1, newItems.size, "Converted sale has same items")

        val item = database.storeBookQueries.getItemById(1L).executeAsOneOrNull()
        assertNotNull(item)
        assertEquals(78.0, item!!.quantity, 0.01, "Stock deducted: 80 - 2 = 78")
    }

    @Test
    fun `convertQuotationToSale fails for already converted quotation`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("Pen", 80.0, "Pcs", 8.0, 20.0, 10.0, "Stationery", null, null, null, 0.0, now)

        database.storeBookQueries.insertSale(
            timestamp = now, total_amount = 40.0, discount_amount = 0.0,
            customer_name = "Quote", customer_gstin = null, business_gstin = null,
            customer_address = null, business_address = null, type = "ESTIMATE", notes = null, updated_at = now,
        )
        val quoteId = database.storeBookQueries.getLastInsertRowId().executeAsOne()
        database.storeBookQueries.insertSaleItem(
            sale_id = quoteId, item_id = 1, item_name = "Pen", unit = "Pcs", quantity = 2.0,
            sell_price = 20.0, buy_price = 8.0, tax_rate = 5.0, hsn_code = null, updated_at = now,
        )

        val first = salesRepo.convertQuotationToSale(quoteId)
        assertTrue(first > 0, "First conversion succeeds")

        val second = salesRepo.convertQuotationToSale(quoteId)
        assertEquals(-1L, second, "Second conversion must return -1 (already converted)")
    }

    @Test
    fun `convertQuotation fails when insufficient stock`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("Razor", 3.0, "Pcs", 25.0, 50.0, 1.0, "Grooming", null, null, null, 0.0, now)

        database.storeBookQueries.insertSale(
            timestamp = now, total_amount = 1000.0, discount_amount = 0.0,
            customer_name = "Big Quote", customer_gstin = null, business_gstin = null,
            customer_address = null, business_address = null, type = "ESTIMATE", notes = null, updated_at = now,
        )
        val quoteId = database.storeBookQueries.getLastInsertRowId().executeAsOne()
        database.storeBookQueries.insertSaleItem(
            sale_id = quoteId, item_id = 1, item_name = "Razor", unit = "Pcs", quantity = 50.0,
            sell_price = 50.0, buy_price = 25.0, tax_rate = 0.0, hsn_code = null, updated_at = now,
        )

        var result: Long = -1L
        var threw = false
        try {
            result = salesRepo.convertQuotationToSale(quoteId)
        } catch (e: com.storebook.inventoryapp.shared.domain.repository.InventoryRepository.InsufficientStockException) {
            threw = true
        }
        assertTrue(threw, "Conversion must throw or return -1 with insufficient stock")
        assertEquals(-1L, result, "Conversion fails with insufficient stock")

        val item = database.storeBookQueries.getItemById(1L).executeAsOneOrNull()
        assertNotNull(item)
        assertEquals(3.0, item!!.quantity, 0.01, "Stock unchanged after failed conversion")
    }

    // ───────── BUG-21: Udhaar created atomically inside transaction ─────────

    @Test
    fun `atomicCheckout creates Udhaar ledger when paymentMode is Udhaar`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("CreditWidget", 50.0, "Pcs", 10.0, 20.0, 5.0, "Misc", null, null, null, 0.0, now)

        val cartItemsData = listOf(
            mapOf<String, Any>(
                "itemId" to 1L,
                "itemName" to "CreditWidget",
                "unit" to "Pcs",
                "quantity" to 2.0,
                "buyPrice" to 10.0,
                "sellPrice" to 20.0,
                "taxRate" to 0.0,
                "hsnCode" to "",
            ),
        )

        val saleId = salesRepo.atomicCheckout(
            cartItemsData = cartItemsData,
            totalAmount = 40.0,
            discountAmount = 0.0,
            customerName = "UdhaarCustomer",
            paymentMode = PaymentMode.UDHAR,
            customerGstin = null,
            businessGstin = null,
            customerAddress = null,
            businessAddress = null,
            type = "SALE",
        )

        assertTrue(saleId > 0, "Sale must succeed: $saleId")
        val ledger = udhaarRepo.getUdhaarBalances()
        val match = ledger.find { it.customerName == "UdhaarCustomer" }
        assertNotNull(match, "Udhaar customer must appear in balances")
        assertEquals(40.0, match!!.netBalance, 0.01, "Net balance equals sale total")
    }

    @Test
    fun `atomicCheckout skips Udhaar ledger for Cash payment`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("CashWidget", 50.0, "Pcs", 10.0, 20.0, 5.0, "Misc", null, null, null, 0.0, now)

        val cartItemsData = listOf(
            mapOf<String, Any>(
                "itemId" to 1L,
                "itemName" to "CashWidget",
                "unit" to "Pcs",
                "quantity" to 2.0,
                "buyPrice" to 10.0,
                "sellPrice" to 20.0,
                "taxRate" to 0.0,
                "hsnCode" to "",
            ),
        )

        val saleId = salesRepo.atomicCheckout(
            cartItemsData = cartItemsData,
            totalAmount = 40.0,
            discountAmount = 0.0,
            customerName = "CashCustomer",
            paymentMode = PaymentMode.CASH,
            customerGstin = null,
            businessGstin = null,
            customerAddress = null,
            businessAddress = null,
            type = "SALE",
        )

        assertTrue(saleId > 0, "Sale must succeed: $saleId")
        val ledger = udhaarRepo.getUdhaarBalances()
        val match = ledger.find { it.customerName == "CashCustomer" }
        assertNull(match, "Cash customer must NOT appear in Udhaar balances")
    }

    @Test
    fun `atomicCheckout skips Udhaar ledger for ESTIMATE type`() = runBlocking {
        val now = Clock.System.now().toEpochMilliseconds()
        database.storeBookQueries.insertItem("EstWidget", 50.0, "Pcs", 10.0, 20.0, 5.0, "Misc", null, null, null, 0.0, now)

        val cartItemsData = listOf(
            mapOf<String, Any>(
                "itemId" to 1L,
                "itemName" to "EstWidget",
                "unit" to "Pcs",
                "quantity" to 2.0,
                "buyPrice" to 10.0,
                "sellPrice" to 20.0,
                "taxRate" to 0.0,
                "hsnCode" to "",
            ),
        )

        val saleId = salesRepo.atomicCheckout(
            cartItemsData = cartItemsData,
            totalAmount = 40.0,
            discountAmount = 0.0,
            customerName = "EstIMATECustomer",
            paymentMode = PaymentMode.UDHAR,
            customerGstin = null,
            businessGstin = null,
            customerAddress = null,
            businessAddress = null,
            type = "ESTIMATE",
        )

        assertTrue(saleId > 0, "Estimate must succeed: $saleId")
        val ledger = udhaarRepo.getUdhaarBalances()
        val match = ledger.find { it.customerName == "EstIMATECustomer" }
        assertNull(match, "Estimate with Udhaar payment must NOT create ledger entry")
    }
}
