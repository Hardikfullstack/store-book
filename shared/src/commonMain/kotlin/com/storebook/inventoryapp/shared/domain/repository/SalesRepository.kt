package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SalesRepository(
    private val database: StoreBookDatabase
) {
    private val queries = database.storeBookQueries

    suspend fun getAllSales(): List<Sales> = withContext(Dispatchers.IO) {
        queries.getAllSales().executeAsList()
    }

    suspend fun getQuotations(): List<Sales> = withContext(Dispatchers.IO) {
        queries.getQuotations().executeAsList()
    }

    suspend fun getSalesByDateRange(startTs: Long, endTs: Long): List<Sales> = withContext(Dispatchers.IO) {
        queries.getSalesByDateRange(startTs, endTs).executeAsList()
    }

    // E02-S1: Transaction-wrapped insert — auto-rollback on failure
    suspend fun insertSale(
        totalAmount: Double, discountAmount: Double, customerName: String?,
        customerGstin: String?, businessGstin: String?, customerAddress: String?,
        businessAddress: String?, type: String, notes: String?
    ): Long = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertSale(
                timestamp, totalAmount, discountAmount, customerName,
                customerGstin, businessGstin, customerAddress, businessAddress,
                type, notes, timestamp
            )
        }
        queries.getLastInsertRowId().executeAsOne()
    }

    suspend fun insertSaleItem(
        saleId: Long, itemId: Long, itemName: String, unit: String,
        quantity: Double, buyPrice: Double, sellPrice: Double
    ) = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertSaleItem(saleId, itemId, itemName, unit, quantity, sellPrice, buyPrice, timestamp)
        }
    }

    suspend fun getSaleById(id: Long): Sales? = withContext(Dispatchers.IO) {
        queries.getSaleById(id).executeAsOneOrNull()
    }

    suspend fun getSaleItems(saleId: Long): List<Sale_items> = withContext(Dispatchers.IO) {
        queries.getSaleItemsBySaleId(saleId).executeAsList()
    }

    suspend fun softDeleteSale(id: Long) = withContext(Dispatchers.IO) {
        database.transaction {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.softDeleteSale(timestamp, id)
        }
    }

    // RP-A0: Push-sync methods for sales + sale_items
    suspend fun getUnsyncedSales(): List<Sales> = withContext(Dispatchers.IO) {
        queries.getUnsyncedSales().executeAsList()
    }

    suspend fun markSaleSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markSaleSynced(cloudId, id)
    }

    suspend fun getUnsyncedSaleItems(): List<Sale_items> = withContext(Dispatchers.IO) {
        queries.getUnsyncedSaleItems().executeAsList()
    }

    suspend fun markSaleItemSynced(id: Long, cloudId: String) = withContext(Dispatchers.IO) {
        queries.markSaleItemSynced(cloudId, id)
    }

    // E03-S1: Audit query — find sale_items where snapshot sell_price differs from current item price
    suspend fun getPriceDriftReport(): List<com.storebook.inventoryapp.shared.data.local.GeneratePriceDriftReport> = withContext(Dispatchers.IO) {
        queries.generatePriceDriftReport().executeAsList()
    }

    // E03-S4: Convert quotation to sale — atomically copies all line items
    suspend fun convertQuotationToSale(quotationId: Long): Long = withContext(Dispatchers.IO) {
        val quotation = queries.getSaleById(quotationId).executeAsOneOrNull()
        if (quotation == null || quotation.type != "ESTIMATE" || quotation.is_converted == 1L) {
            return@withContext -1L // Not a valid unconverted quotation
        }

        var newSaleId = -1L
        database.transaction {
            // Step 1: Mark quotation as converted
            val updatedTs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.setQuotationConverted(updatedTs, quotationId)

            // Step 2: Create new SALE with the same header details (positional params per .sq)
            val nowMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertSale(
                nowMs,
                quotation.total_amount,
                quotation.discount_amount,
                quotation.customer_name,
                quotation.customer_gstin,
                quotation.business_gstin,
                quotation.customer_address,
                quotation.business_address,
                "SALE",
                quotation.notes,
                nowMs
            )
            newSaleId = queries.getLastInsertRowId().executeAsOne()

            // Step 3: Copy all sale_items from quotation to new sale (positional params per .sq)
            val quoteItems = queries.getSaleItemsBySaleId(quotationId).executeAsList()
            for (qi in quoteItems) {
                queries.insertSaleItem(
                    newSaleId,
                    qi.item_id,
                    qi.item_name,
                    qi.unit,
                    qi.quantity,
                    qi.sell_price,
                    qi.buy_price,
                    nowMs
                )
            }
        }
        newSaleId
    }

    // ============================================================================
    // E03-S3: Daily/Monthly aggregate queries — profit from sale_items price snapshots
    // Uses sell_price/buy_price captured AT TIME OF SALE (not current item prices)
    // ============================================================================

    /** Revenue + COGS per day for a date range */
    suspend fun getDailyRevenueByDateRange(startTs: Long, endTs: Long): List<GetDailyRevenueByDateRange> =
        withContext(Dispatchers.IO) {
            queries.getDailyRevenueByDateRange(startTs, endTs).executeAsList()
        }

    /** Today's snapshot: revenue, cost-of-goods, expenses (all 3 aggregated) */
    suspend fun getTodaySnapshot(todayKey: String): GetTodaySnapshot? =
        withContext(Dispatchers.IO) {
            queries.getTodaySnapshot(todayKey).executeAsOneOrNull()
        }

    /** Monthly summary with daily revenue vs expenses for a date range */
    suspend fun getMonthlySummaryByDateRange(startTs: Long, endTs: Long): List<GetMonthlySummaryByDateRange> =
        withContext(Dispatchers.IO) {
            queries.getMonthlySummaryByDateRange(startTs, endTs).executeAsList()
        }
}
