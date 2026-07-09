package com.storebook.inventoryapp.shared.domain.repository

import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.data.local.Sales
import com.storebook.inventoryapp.shared.data.local.Sale_items
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

    suspend fun insertSale(
        totalAmount: Double, discountAmount: Double, customerName: String?, 
        customerGstin: String?, businessGstin: String?, customerAddress: String?, 
        businessAddress: String?, type: String, notes: String?
    ): Long = withContext(Dispatchers.IO) {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        queries.insertSale(
            timestamp, totalAmount, discountAmount, customerName, 
            customerGstin, businessGstin, customerAddress, businessAddress, 
            type, notes, timestamp
        )
        queries.getLastInsertRowId().executeAsOne()
    }

    suspend fun insertSaleItem(
        saleId: Long, itemId: Long, itemName: String, unit: String, 
        quantity: Double, buyPrice: Double, sellPrice: Double
    ) = withContext(Dispatchers.IO) {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        queries.insertSaleItem(saleId, itemId, itemName, unit, quantity, sellPrice, buyPrice, timestamp)
    }

    suspend fun getSaleById(id: Long): Sales? = withContext(Dispatchers.IO) {
        queries.getSaleById(id).executeAsOneOrNull()
    }

    suspend fun getSaleItems(saleId: Long): List<Sale_items> = withContext(Dispatchers.IO) {
        queries.getSaleItemsBySaleId(saleId).executeAsList()
    }

    suspend fun softDeleteSale(id: Long) = withContext(Dispatchers.IO) {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        queries.softDeleteSale(timestamp, id)
    }
}
