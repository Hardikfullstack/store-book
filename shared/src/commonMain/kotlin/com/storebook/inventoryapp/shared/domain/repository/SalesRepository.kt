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

    // E03-S4: Convert quotation to sale — atomically copies all line items + deducts stock
    suspend fun convertQuotationToSale(quotationId: Long): Long = withContext(Dispatchers.IO) {
        val quotation = queries.getSaleById(quotationId).executeAsOneOrNull()
        if (quotation == null || quotation.type != "ESTIMATE" || quotation.is_converted == 1L) {
            return@withContext -1L // Not a valid unconverted quotation
        }

        var newSaleId = -1L
        database.transaction {
            // Step 0: Check stock availability before committing to conversion (BUG-STOCK-INT guard)
            val quoteItems = queries.getSaleItemsBySaleId(quotationId).executeAsList()
            for (qi in quoteItems) {
                val currentItem = queries.getItemById(qi.item_id).executeAsOneOrNull()
                if (currentItem != null) {
                    val newQty = currentItem.quantity - qi.quantity
                    if (newQty < 0) {
                        throw com.storebook.inventoryapp.shared.domain.repository.InventoryRepository.InsufficientStockException(
                            itemId = qi.item_id,
                            itemName = currentItem.name,
                            currentQuantity = currentItem.quantity,
                            requestedChange = -qi.quantity
                        )
                    }
                }
            }

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

                // BUG-STOCK-INT: Deduct stock when converting ESTIMATE to actual SALE
                val currentItem = queries.getItemById(qi.item_id).executeAsOneOrNull()
                if (currentItem != null) {
                    val delta = -qi.quantity
                    val curTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    queries.updateItemStock(delta, curTime, qi.item_id)
                }
            }
        }
        newSaleId
    }


    // ==========================================================================
    // BUG-01 FIX: Atomic checkout — sale header + line items + stock deduction
    //             ALL inside a single database.transaction{...} so partial writes
    //             are impossible on crash/OOM/constraint violation.
    // ==========================================================================

    /**
     * Atomically inserts the sale header, all line items, and deducts inventory.
     * Returns -1 if stock is insufficient for any item (BUG-07 enforcement).
     * ESTIMATE type: does NOT deduct stock — only actual SALE deducts.
     * All or nothing: on ANY failure the entire block rolls back via SQLDelight transaction.
     */
    suspend fun atomicCheckout(
        cartItemsData: List<Map<String, Any>>, // each map: itemId, itemName, unit, quantity, buyPrice, sellPrice
        totalAmount: Double,
        discountAmount: Double,
        customerName: String,
        type: String
    ): Long = withContext(Dispatchers.IO) {
        val shouldDeductStock = type != "ESTIMATE"
        var newSaleId = -1L
        database.transaction {
            // Step 1: Insert sale header
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            queries.insertSale(
                timestamp, totalAmount, discountAmount, customerName,
                null, null, null, null, type, null, timestamp
            )
            newSaleId = queries.getLastInsertRowId().executeAsOne()

            // Step 2: Insert all sale items + deduct stock (only for SALE, not ESTIMATE)
            for (ci in cartItemsData) {
                val itemId = ci["itemId"] as Long
                val itemName = ci["itemName"] as String
                val unit = ci["unit"] as String
                val quantity = ci["quantity"] as Double
                val buyPrice = ci["buyPrice"] as Double
                val sellPrice = ci["sellPrice"] as Double

                // BUG-STOCK-INT: Only deduct stock for actual sales, never for estimates/quotations
                if (shouldDeductStock) {
                    val currentItem = queries.getItemById(itemId).executeAsOneOrNull()
                    if (currentItem != null) {
                        val newQty = currentItem.quantity - quantity
                        if (newQty < 0) {
                            throw com.storebook.inventoryapp.shared.domain.repository.InventoryRepository.InsufficientStockException(
                                itemId = itemId,
                                itemName = currentItem.name,
                                currentQuantity = currentItem.quantity,
                                requestedChange = -quantity
                            )
                        }
                        // BUG-13 FIX: updateItemStock expects a signed delta; pass negative quantity to subtract
                        val curTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                        queries.updateItemStock(-quantity, curTime, itemId)
                    }
                }

                // Insert sale item (always inserted regardless of type)
                queries.insertSaleItem(
                    newSaleId, itemId, itemName, unit, quantity, sellPrice, buyPrice, timestamp
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

    // ==========================================================================
    // E27-S2: Server-side search — paginate sales by customer name + date range
    // Case-insensitive partial match, LIMIT/OFFSET for server-side pagination
    // ==========================================================================

    /** Search sales by customer name with date range + pagination */
    suspend fun getSalesSearch(
        searchQuery: String,
        startDate: Long,
        endDate: Long,
        limit: Long = 50,
        offset: Long = 0
    ) : List<Sales> = withContext(Dispatchers.IO) {
        val query = if (searchQuery.isBlank()) "%" else searchQuery.lowercase()
        queries.getSalesSearch(query, startDate, endDate, limit, offset).executeAsList()
    }
}
