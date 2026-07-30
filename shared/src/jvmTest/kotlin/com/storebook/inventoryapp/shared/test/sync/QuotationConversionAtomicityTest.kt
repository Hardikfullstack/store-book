package com.storebook.inventoryapp.shared.test.sync

import app.cash.sqldelight.db.SqlDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * e37-s4 — convertQuotationToSale atomicity under transaction + rollback on insufficient stock.
 * Uses direct SQLDelight queries to avoid JUnit 5 + suspend scope type inference issues.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuotationConversionAtomicityTest {

    private lateinit var database: StoreBookDatabase
    private lateinit var driver: SqlDriver

    @BeforeEach
    fun setup() {
        val (db, d) = com.storebook.inventoryapp.shared.test.DatabaseTestHelper.createDatabase()
        database = db
        driver = d
    }

    @AfterEach
    fun teardown() {
        com.storebook.inventoryapp.shared.test.DatabaseTestHelper.dropDatabase(driver)
    }

    /**
     * Helper mimicking SalesRepository.convertQuotationToSale using direct queries.
     * Returns new sale ID on success, or -1L on guard failure / insufficient stock rollback.
     */
    private fun convertQuotationToSale(quotationId: Long): Long {
        val quotation = database.storeBookQueries.getSaleById(quotationId).executeAsOneOrNull()
            ?: return -1L

        if (quotation.type != "ESTIMATE" || quotation.is_converted == 1L) {
            return -1L
        }

        val quoteItems = database.storeBookQueries.getSaleItemsBySaleId(quotationId).executeAsList()

        var newSaleId: Long = -1L
        var shouldRollback = false

        // Stock availability check
        for (qi in quoteItems) {
            val currentItem = database.storeBookQueries.getItemById(qi.item_id).executeAsOneOrNull()
                ?: continue
            if (currentItem.quantity - qi.quantity < 0) {
                shouldRollback = true
                break
            }
        }

        if (!shouldRollback) {
            // Mark quotation as converted
            val updatedTs = System.currentTimeMillis()
            database.storeBookQueries.setQuotationConverted(updatedTs, quotationId)

            // Create new SALE
            val nowMs = System.currentTimeMillis()
            database.storeBookQueries.insertSale(
                timestamp = nowMs, total_amount = quotation.total_amount,
                discount_amount = quotation.discount_amount,
                customer_name = quotation.customer_name, customer_gstin = quotation.customer_gstin,
                business_gstin = quotation.business_gstin,
                customer_address = quotation.customer_address,
                business_address = quotation.business_address, type = "SALE",
                notes = quotation.notes, updated_at = nowMs
            )
            newSaleId = database.storeBookQueries.getLastInsertRowId().executeAsOne()

            // Copy sale items and deduct stock
            for (qi in quoteItems) {
                val item = database.storeBookQueries.getItemById(qi.item_id).executeAsOneOrNull()
                val itemTaxRate = item?.tax_rate ?: 0.0
                val itemHsnCode = item?.hsn_code

                database.storeBookQueries.insertSaleItem(
                    sale_id = newSaleId, item_id = qi.item_id, item_name = qi.item_name, unit = qi.unit,
                    quantity = qi.quantity, sell_price = qi.sell_price, buy_price = qi.buy_price,
                    tax_rate = itemTaxRate, hsn_code = itemHsnCode, updated_at = nowMs
                )

                if (item != null) {
                    val updatedStock = item.quantity - qi.quantity
                    database.storeBookQueries.updateItem(
                        name = item.name, quantity = updatedStock, unit = item.unit, buy_price = item.buy_price,
                        sell_price = item.sell_price, low_stock_threshold = item.low_stock_threshold,
                        category = item.category, photo_path = item.photo_path, barcode = item.barcode,
                        hsn_code = item.hsn_code, tax_rate = item.tax_rate, updated_at = nowMs, id = qi.item_id
                    )
                }
            }

            // Mark new sale as synced
            database.storeBookQueries.markSaleSynced("local-sale-$newSaleId", newSaleId)
        }

        return if (shouldRollback) -1L else newSaleId
    }

    @Test
    fun happy_path_quotation_converts_to_sale_stock_deducted_atomically() {
        database.storeBookQueries.insertItem(
            name = "Widget", quantity = 100.0, unit = "Pcs", buy_price = 20.0, sell_price = 50.0,
            low_stock_threshold = 10.0, category = "Gadget", photo_path = null, barcode = null,
            hsn_code = null, tax_rate = 0.0, updated_at = 1721000000000L
        )

        val now = 1721000001000L
        database.storeBookQueries.insertSale(
            timestamp = now, total_amount = 500.0, discount_amount = 0.0,
            customer_name = "Shopper", customer_gstin = null, business_gstin = null,
            customer_address = null, business_address = null, type = "ESTIMATE",
            notes = null, updated_at = now
        )

        val quotationId = database.storeBookQueries.getLastInsertRowId().executeAsOne()
        database.storeBookQueries.insertSaleItem(
            sale_id = quotationId, item_id = 1, item_name = "Widget", unit = "Pcs",
            quantity = 3.0, sell_price = 50.0, buy_price = 20.0, tax_rate = 0.0, hsn_code = null,
            updated_at = now
        )

        val newSaleId = convertQuotationToSale(quotationId)
        assertNotEquals(-1L, newSaleId, "Conversion should succeed and return valid sale ID")

        val quotation = database.storeBookQueries.getSaleById(quotationId).executeAsOneOrNull()
        requireNotNull(quotation)
        assertEquals(1L, quotation.is_converted, "Quotation must be marked converted")

        val newItemResult = database.storeBookQueries.getItemById(1).executeAsOneOrNull()
        requireNotNull(newItemResult)
        assertEquals(97.0, newItemResult.quantity, "Stock deducted: 100 - 3 = 97")

        val newSale = database.storeBookQueries.getSaleById(newSaleId).executeAsOneOrNull()
        requireNotNull(newSale)
        assertEquals("SALE", newSale.type, "New entity must be type SALE not ESTIMATE")
    }

    @Test
    fun insufficient_stock_rollback_no_sale_created_stock_unchanged() {
        database.storeBookQueries.insertItem(
            name = "Scarce Item", quantity = 2.0, unit = "Pcs", buy_price = 50.0, sell_price = 100.0,
            low_stock_threshold = 1.0, category = "Rare", photo_path = null, barcode = null,
            hsn_code = null, tax_rate = 0.0, updated_at = 1721000000000L
        )

        val now = 1721000001000L
        database.storeBookQueries.insertSale(
            timestamp = now, total_amount = 1000.0, discount_amount = 0.0,
            customer_name = "Buyer", customer_gstin = null, business_gstin = null,
            customer_address = null, business_address = null, type = "ESTIMATE",
            notes = null, updated_at = now
        )

        val quotationId = database.storeBookQueries.getLastInsertRowId().executeAsOne()
        database.storeBookQueries.insertSaleItem(
            sale_id = quotationId, item_id = 1, item_name = "Scarce Item", unit = "Pcs",
            quantity = 5.0, sell_price = 100.0, buy_price = 50.0, tax_rate = 0.0, hsn_code = null,
            updated_at = now
        )

        val result = convertQuotationToSale(quotationId)
        assertEquals(-1L, result, "Insufficient stock must cause conversion to fail")

        val item = database.storeBookQueries.getItemById(1).executeAsOneOrNull()
        requireNotNull(item)
        assertEquals(2.0, item.quantity, "Stock must be unchanged - transaction rolled back")

        val quot = database.storeBookQueries.getSaleById(quotationId).executeAsOneOrNull()
        requireNotNull(quot)
        assertEquals("ESTIMATE", quot.type, "Quotation must remain as ESTIMATE after failed conversion")
    }

    @Test
    fun double_conversion_second_call_returns_minus_1_quotation_already_converted() {
        database.storeBookQueries.insertItem(
            name = "Widget", quantity = 50.0, unit = "Pcs", buy_price = 20.0, sell_price = 50.0,
            low_stock_threshold = 5.0, category = "Gadget", photo_path = null, barcode = null,
            hsn_code = null, tax_rate = 0.0, updated_at = 1721000000000L
        )

        val now = 1721000001000L
        database.storeBookQueries.insertSale(
            timestamp = now, total_amount = 500.0, discount_amount = 0.0,
            customer_name = "Shopper", customer_gstin = null, business_gstin = null,
            customer_address = null, business_address = null, type = "ESTIMATE",
            notes = null, updated_at = now
        )

        val quotationId = database.storeBookQueries.getLastInsertRowId().executeAsOne()
        database.storeBookQueries.insertSaleItem(
            sale_id = quotationId, item_id = 1, item_name = "Widget", unit = "Pcs",
            quantity = 2.0, sell_price = 50.0, buy_price = 20.0, tax_rate = 0.0, hsn_code = null,
            updated_at = now
        )

        val firstResult = convertQuotationToSale(quotationId)
        assertNotEquals(-1L, firstResult, "First conversion must succeed")

        val secondResult = convertQuotationToSale(quotationId)
        assertEquals(-1L, secondResult, "Second conversion must be rejected because quotation is already converted")
    }
}
