package com.storebook.inventoryapp.data.repository

import com.storebook.inventoryapp.shared.domain.models.*
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.storebook.inventoryapp.data.local.StoreBookDbHelper
import java.io.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Repository ---
class StoreBookRepository(
        private val context: Context,
        val storeId: String,
) {
    val dbHelper = StoreBookDbHelper(context, storeId)

    // --- Inventory Operations ---

    suspend fun getActiveItems(): List<Item> =
            withContext(Dispatchers.IO) {
                val list = mutableListOf<Item>()
                val db = dbHelper.readableDatabase
                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE ${StoreBookDbHelper.KEY_ITEM_IS_DELETED} = 0 ORDER BY ${StoreBookDbHelper.KEY_ITEM_NAME} ASC",
                                null,
                        )
                cursor.use { c ->
                    while (c.moveToNext()) {
                        list.add(cursorToItem(c))
                    }
                }
                list
            }

    suspend fun getItemById(id: Long): Item? =
            withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE ${StoreBookDbHelper.KEY_ID} = ?",
                                arrayOf(id.toString()),
                        )
                cursor.use { c ->
                    if (c.moveToFirst()) {
                        cursorToItem(c)
                    } else {
                        null
                    }
                }
            }

    suspend fun insertItem(item: Item): Long =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val values =
                        ContentValues().apply {
                            put(StoreBookDbHelper.KEY_ITEM_NAME, item.name)
                            put(StoreBookDbHelper.KEY_ITEM_QTY, item.quantity)
                            put(StoreBookDbHelper.KEY_ITEM_UNIT, item.unit)
                            put(StoreBookDbHelper.KEY_ITEM_BUY_PRICE, item.buyPrice)
                            put(StoreBookDbHelper.KEY_ITEM_SELL_PRICE, item.sellPrice)
                            put(StoreBookDbHelper.KEY_ITEM_THRESHOLD, item.lowStockThreshold)
                            put(StoreBookDbHelper.KEY_ITEM_CATEGORY, item.category)
                            put(StoreBookDbHelper.KEY_ITEM_PHOTO, item.photoPath)
                            put(StoreBookDbHelper.KEY_ITEM_HSN, item.hsnCode)
                            put(StoreBookDbHelper.KEY_ITEM_TAX_RATE, item.taxRate)
                            put(StoreBookDbHelper.KEY_ITEM_IS_DELETED, 0)
                            put(StoreBookDbHelper.KEY_ITEM_DELETED_TIME, 0)
                            put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                            put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                        }
                db.insertWithOnConflict(
                        StoreBookDbHelper.TABLE_ITEMS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }

    suspend fun updateItem(item: Item): Int =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val values =
                        ContentValues().apply {
                            put(StoreBookDbHelper.KEY_ITEM_NAME, item.name)
                            put(StoreBookDbHelper.KEY_ITEM_QTY, item.quantity)
                            put(StoreBookDbHelper.KEY_ITEM_UNIT, item.unit)
                            put(StoreBookDbHelper.KEY_ITEM_BUY_PRICE, item.buyPrice)
                            put(StoreBookDbHelper.KEY_ITEM_SELL_PRICE, item.sellPrice)
                            put(StoreBookDbHelper.KEY_ITEM_THRESHOLD, item.lowStockThreshold)
                            put(StoreBookDbHelper.KEY_ITEM_CATEGORY, item.category)
                            put(StoreBookDbHelper.KEY_ITEM_PHOTO, item.photoPath)
                            put(StoreBookDbHelper.KEY_ITEM_HSN, item.hsnCode)
                            put(StoreBookDbHelper.KEY_ITEM_TAX_RATE, item.taxRate)
                            put(StoreBookDbHelper.KEY_ITEM_IS_DELETED, item.isDeleted)
                            put(StoreBookDbHelper.KEY_ITEM_DELETED_TIME, item.deletedTimestamp)
                            put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                            put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                        }
                db.update(
                        StoreBookDbHelper.TABLE_ITEMS,
                        values,
                        "${StoreBookDbHelper.KEY_ID} = ?",
                        arrayOf(item.id.toString()),
                )
            }

    suspend fun softDeleteItem(id: Long): Int =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val values =
                        ContentValues().apply {
                            put(StoreBookDbHelper.KEY_ITEM_IS_DELETED, 1)
                            put(StoreBookDbHelper.KEY_ITEM_DELETED_TIME, System.currentTimeMillis())
                            put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                            put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                        }
                db.update(
                        StoreBookDbHelper.TABLE_ITEMS,
                        values,
                        "${StoreBookDbHelper.KEY_ID} = ?",
                        arrayOf(id.toString())
                )
            }

    suspend fun recoverSoftDeletedItems(): List<Item> =
            withContext(Dispatchers.IO) {
                val list = mutableListOf<Item>()
                val db = dbHelper.readableDatabase
                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE ${StoreBookDbHelper.KEY_ITEM_IS_DELETED} = 1 ORDER BY ${StoreBookDbHelper.KEY_ITEM_DELETED_TIME} DESC",
                                null,
                        )
                cursor.use { c ->
                    while (c.moveToNext()) {
                        list.add(cursorToItem(c))
                    }
                }
                list
            }

    suspend fun restoreItem(id: Long): Int =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val values =
                        ContentValues().apply {
                            put(StoreBookDbHelper.KEY_ITEM_IS_DELETED, 0)
                            put(StoreBookDbHelper.KEY_ITEM_DELETED_TIME, 0)
                            put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                            put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                        }
                db.update(
                        StoreBookDbHelper.TABLE_ITEMS,
                        values,
                        "${StoreBookDbHelper.KEY_ID} = ?",
                        arrayOf(id.toString())
                )
            }

    // --- Sales Operations ---

    suspend fun recordSale(
            itemsInCart: List<CartItem>,
            discount: Double,
            customerName: String?,
            customerGstin: String? = null,
            customerAddress: String? = null,
            businessGstin: String? = null,
            businessAddress: String? = null,
            notes: String?,
            paymentMode: String,
            type: String = "SALE",
    ): Long =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                db.beginTransaction()
                try {
                    val taxSummary =
                            com.storebook.inventoryapp.data.billing.BillingEngine
                                    .calculateInvoiceTaxes(
                                            cartItems = itemsInCart,
                                            totalDiscount = discount,
                                            businessGstin = businessGstin,
                                            customerGstin = customerGstin,
                                    )
                    val total = taxSummary.grandTotal

                    // 2. Insert into Sales table
                    val saleTime = System.currentTimeMillis()
                    val saleValues =
                            ContentValues().apply {
                                put(StoreBookDbHelper.KEY_TIMESTAMP, saleTime)
                                put(StoreBookDbHelper.KEY_SALE_TOTAL, total)
                                put(StoreBookDbHelper.KEY_SALE_DISCOUNT, taxSummary.totalDiscount)
                                put(StoreBookDbHelper.KEY_SALE_CUSTOMER, customerName)
                                put(StoreBookDbHelper.KEY_SALE_CUSTOMER_GSTIN, customerGstin)
                                put(StoreBookDbHelper.KEY_SALE_BUSINESS_GSTIN, businessGstin)
                                put(StoreBookDbHelper.KEY_SALE_CUSTOMER_ADDRESS, customerAddress)
                                put(StoreBookDbHelper.KEY_SALE_BUSINESS_ADDRESS, businessAddress)
                                put(StoreBookDbHelper.KEY_SALE_TYPE, type)
                                put(StoreBookDbHelper.KEY_NOTES, notes)
                                put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                            }
                    val saleId = db.insert(StoreBookDbHelper.TABLE_SALES, null, saleValues)

                    if (saleId == -1L) {
                        throw Exception("Failed to insert sale record")
                    }

                    // 3. Process each item in cart
                    for (cartItem in itemsInCart) {
                        // Insert into sale_items table
                        val saleItemValues =
                                ContentValues().apply {
                                    put(StoreBookDbHelper.KEY_SI_SALE_ID, saleId)
                                    put(StoreBookDbHelper.KEY_SI_ITEM_ID, cartItem.item.id)
                                    put(StoreBookDbHelper.KEY_SI_ITEM_NAME, cartItem.item.name)
                                    put(StoreBookDbHelper.KEY_SI_UNIT, cartItem.item.unit)
                                    put(StoreBookDbHelper.KEY_SI_QTY, cartItem.quantity)
                                    put(
                                            StoreBookDbHelper.KEY_SI_SELL_PRICE,
                                            cartItem.item.sellPrice
                                    )
                                    put(StoreBookDbHelper.KEY_SI_BUY_PRICE, cartItem.item.buyPrice)
                                    put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                    put(
                                            StoreBookDbHelper.KEY_UPDATED_AT,
                                            System.currentTimeMillis()
                                    )
                                }
                        db.insert(StoreBookDbHelper.TABLE_SALE_ITEMS, null, saleItemValues)

                        // Update quantity in items table only if it's a real sale
                        if (type != "ESTIMATE" && cartItem.item.id != 0L) {
                            val qtyCursor = db.rawQuery("SELECT ${StoreBookDbHelper.KEY_ITEM_QTY} FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE ${StoreBookDbHelper.KEY_ID} = ?", arrayOf(cartItem.item.id.toString()))
                            var currentQty = 0.0
                            qtyCursor.use {
                                if (it.moveToFirst()) currentQty = it.getDouble(0)
                            }
                            if (currentQty < cartItem.quantity) {
                                throw Exception("Stock oversell error: Not enough stock for ${cartItem.item.name}")
                            }
                            val newQty = currentQty - cartItem.quantity
                            val itemUpdateValues =
                                    ContentValues().apply {
                                        put(StoreBookDbHelper.KEY_ITEM_QTY, newQty)
                                        put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                        put(
                                                StoreBookDbHelper.KEY_UPDATED_AT,
                                                System.currentTimeMillis()
                                        )
                                    }
                            db.update(
                                    StoreBookDbHelper.TABLE_ITEMS,
                                    itemUpdateValues,
                                    "${StoreBookDbHelper.KEY_ID} = ?",
                                    arrayOf(cartItem.item.id.toString()),
                            )
                        }
                    }

                    // 4. If customer name is provided, total > 0, and payment mode is Udhaar,
                    // record a credit entry automatically (only if it's a real sale)
                    if (type != "ESTIMATE" && !customerName.isNullOrBlank() &&
                                    paymentMode.equals("Udhaar", ignoreCase = true)
                    ) {
                        val udhaarValues =
                                ContentValues().apply {
                                    put(StoreBookDbHelper.KEY_UDHAAR_CUSTOMER, customerName)
                                    put(StoreBookDbHelper.KEY_UDHAAR_AMOUNT, total)
                                    put(StoreBookDbHelper.KEY_UDHAAR_TYPE, "CREDIT")
                                    put(StoreBookDbHelper.KEY_TIMESTAMP, saleTime)
                                    put(
                                            StoreBookDbHelper.KEY_NOTES,
                                            "Sale bill #$saleId" +
                                                    (if (notes != null) " - $notes" else ""),
                                    )
                                    put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                    put(
                                            StoreBookDbHelper.KEY_UPDATED_AT,
                                            System.currentTimeMillis()
                                    )
                                }
                        db.insert(StoreBookDbHelper.TABLE_UDHAAR, null, udhaarValues)
                    }

                    db.setTransactionSuccessful()
                    saleId
                } finally {
                    db.endTransaction()
                }
            }

    suspend fun undoSale(saleId: Long): Boolean =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                db.beginTransaction()
                try {
                    // 1. Fetch sale details
                    val cursor =
                            db.rawQuery(
                                    "SELECT * FROM ${StoreBookDbHelper.TABLE_SALE_ITEMS} WHERE ${StoreBookDbHelper.KEY_SI_SALE_ID} = ?",
                                    arrayOf(saleId.toString()),
                            )
                    val saleItems = mutableListOf<Pair<Long, Double>>() // Pair(ItemId, Quantity)
                    cursor.use { c ->
                        while (c.moveToNext()) {
                            val itemId =
                                    c.getLong(
                                            c.getColumnIndexOrThrow(
                                                    StoreBookDbHelper.KEY_SI_ITEM_ID
                                            )
                                    )
                            val qty =
                                    c.getDouble(
                                            c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_QTY)
                                    )
                            saleItems.add(Pair(itemId, qty))
                        }
                    }

                    // Check if it was an estimate
                    var isEstimate = false
                    val typeCursor = db.rawQuery("SELECT ${StoreBookDbHelper.KEY_SALE_TYPE} FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_ID} = ?", arrayOf(saleId.toString()))
                    typeCursor.use {
                        if (it.moveToFirst()) {
                            isEstimate = it.getString(0) == "ESTIMATE"
                        }
                    }

                    // 2. Add back stock to items (only if it was a real sale)
                    val timeNow = System.currentTimeMillis()
                    if (!isEstimate) {
                        for (pair in saleItems) {
                            db.execSQL(
                                    "UPDATE ${StoreBookDbHelper.TABLE_ITEMS} SET ${StoreBookDbHelper.KEY_ITEM_QTY} = ${StoreBookDbHelper.KEY_ITEM_QTY} + ?, ${StoreBookDbHelper.KEY_IS_SYNCED} = 0, ${StoreBookDbHelper.KEY_UPDATED_AT} = ? WHERE ${StoreBookDbHelper.KEY_ID} = ?",
                                    arrayOf(pair.second, timeNow, pair.first),
                            )
                        }
                    }

                    // 3. Delete from Udhaar associated with this sale (Soft Delete)
                    val softDelValues =
                            ContentValues().apply {
                                put(StoreBookDbHelper.KEY_IS_DELETED, 1)
                                put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                put(StoreBookDbHelper.KEY_UPDATED_AT, timeNow)
                            }
                    db.update(
                            StoreBookDbHelper.TABLE_UDHAAR,
                            softDelValues,
                            "${StoreBookDbHelper.KEY_NOTES} LIKE ?",
                            arrayOf("Sale bill #$saleId%"),
                    )

                    // 4. Soft delete sale and sale_items
                    db.update(
                            StoreBookDbHelper.TABLE_SALE_ITEMS,
                            softDelValues,
                            "${StoreBookDbHelper.KEY_SI_SALE_ID} = ?",
                            arrayOf(saleId.toString()),
                    )
                    db.update(
                            StoreBookDbHelper.TABLE_SALES,
                            softDelValues,
                            "${StoreBookDbHelper.KEY_ID} = ?",
                            arrayOf(saleId.toString()),
                    )

                    db.setTransactionSuccessful()
                    true
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    false
                } finally {
                    db.endTransaction()
                }
            }

    suspend fun markQuotationAsConverted(quotationId: Long): Boolean =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put(StoreBookDbHelper.KEY_SALE_TYPE, "CONVERTED")
                    put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                    put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                }
                val count = db.update(
                        StoreBookDbHelper.TABLE_SALES,
                        values,
                        "${StoreBookDbHelper.KEY_ID} = ?",
                        arrayOf(quotationId.toString())
                )
                count > 0
            }

    suspend fun getSaleById(saleId: Long): Sale? =
            withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_ID} = ?",
                                arrayOf(saleId.toString()),
                        )
                val sales = fetchSalesFromCursor(cursor)
                sales.firstOrNull()
            }

    suspend fun getSales(): List<Sale> =
            withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND ${StoreBookDbHelper.KEY_SALE_TYPE} = 'SALE' ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC",
                                null,
                        )
                fetchSalesFromCursor(cursor)
            }

    suspend fun getSalesPage(
            limit: Int = 50,
            offset: Int = 0,
    ): List<Sale> =
            withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND ${StoreBookDbHelper.KEY_SALE_TYPE} = 'SALE' ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC LIMIT ? OFFSET ?",
                                arrayOf(limit.toString(), offset.toString()),
                        )
                fetchSalesFromCursor(cursor)
            }

    suspend fun getSalesByDateRange(
            startTs: Long,
            endTs: Long,
    ): List<Sale> =
            withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND ${StoreBookDbHelper.KEY_SALE_TYPE} = 'SALE' AND ${StoreBookDbHelper.KEY_TIMESTAMP} BETWEEN ? AND ? ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC",
                                arrayOf(startTs.toString(), endTs.toString()),
                        )
                fetchSalesFromCursor(cursor)
            }

    suspend fun getQuotations(): List<Sale> =
            withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND (${StoreBookDbHelper.KEY_SALE_TYPE} = 'ESTIMATE' OR ${StoreBookDbHelper.KEY_SALE_TYPE} = 'CONVERTED') ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC",
                                null,
                        )
                fetchSalesFromCursor(cursor)
            }

    private suspend fun fetchSalesFromCursor(cursor: android.database.Cursor): List<Sale> =
            withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val salesList = mutableListOf<Sale>()
                cursor.use { c ->
                    val colId = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ID)
                    val ts = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_TIMESTAMP)
                    val total = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_TOTAL)
                    val disc = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_DISCOUNT)
                    val cust = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_CUSTOMER)
                    val cGstin = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_CUSTOMER_GSTIN)
                    val bGstin = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_BUSINESS_GSTIN)
                    val cAddr = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_CUSTOMER_ADDRESS)
                    val bAddr = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_BUSINESS_ADDRESS)
                    val typeIdx = c.getColumnIndex(StoreBookDbHelper.KEY_SALE_TYPE) // Might not exist if DB not upgraded in some query
                    val notes = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_NOTES)
                    while (c.moveToNext()) {
                        salesList.add(
                                Sale(
                                        id = c.getLong(colId),
                                        timestamp = c.getLong(ts),
                                        totalAmount = c.getDouble(total),
                                        discountAmount = c.getDouble(disc),
                                        customerName = c.getString(cust),
                                        customerGstin = c.getString(cGstin),
                                        businessGstin = c.getString(bGstin),
                                        customerAddress = c.getString(cAddr),
                                        businessAddress = c.getString(bAddr),
                                        type = if (typeIdx >= 0) c.getString(typeIdx) else "SALE",
                                        notes = c.getString(notes),
                                        items = emptyList(),
                                ),
                        )
                    }
                }
                if (salesList.isEmpty()) return@withContext emptyList()
                val saleIds = salesList.map { it.id }
                val itemsBySaleId = mutableMapOf<Long, MutableList<SaleItemDetail>>()

                saleIds.chunked(500).forEach { chunk ->
                    val placeholders = chunk.joinToString(",") { "?" }
                    val itemsCursor =
                            db.rawQuery(
                                    "SELECT * FROM ${StoreBookDbHelper.TABLE_SALE_ITEMS} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND ${StoreBookDbHelper.KEY_SI_SALE_ID} IN ($placeholders)",
                                    chunk.map { it.toString() }.toTypedArray(),
                            )
                    itemsCursor.use { ic ->
                        val siSaleId = ic.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_SALE_ID)
                        val siItemId = ic.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_ITEM_ID)
                        val siItemName = ic.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_ITEM_NAME)
                        val siUnit = ic.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_UNIT)
                        val siQty = ic.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_QTY)
                        val siSell = ic.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_SELL_PRICE)
                        val siBuy = ic.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_BUY_PRICE)
                        while (ic.moveToNext()) {
                            val saleId = ic.getLong(siSaleId)
                            itemsBySaleId
                                    .computeIfAbsent(saleId) { mutableListOf() }
                                    .add(
                                            SaleItemDetail(
                                                    id =
                                                            ic.getLong(
                                                                    ic.getColumnIndexOrThrow(
                                                                            StoreBookDbHelper.KEY_ID
                                                                    )
                                                            ),
                                                    itemId = ic.getLong(siItemId),
                                                    itemName = ic.getString(siItemName),
                                                    quantity = ic.getDouble(siQty),
                                                    unit = ic.getString(siUnit),
                                                    sellPrice = ic.getDouble(siSell),
                                                    buyPrice = ic.getDouble(siBuy),
                                            ),
                                    )
                        }
                    }
                }
                salesList.map { sale -> sale.copy(items = itemsBySaleId[sale.id] ?: emptyList()) }
            }

    suspend fun getActiveItemsFiltered(
            search: String? = null,
            category: String? = null,
            sortBy: String? = null,
            limit: Int = Int.MAX_VALUE,
            offset: Int = 0,
    ): List<Item> =
            withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val conditions = mutableListOf("${StoreBookDbHelper.KEY_ITEM_IS_DELETED} = 0")
                val args = mutableListOf<String>()

                if (!search.isNullOrBlank()) {
                    conditions.add("LOWER(${StoreBookDbHelper.KEY_ITEM_NAME}) LIKE ?")
                    args.add("%${search.lowercase()}%")
                }

                if (category != null && category != "All") {
                    if (category == "Low Stock") {
                        conditions.add("${StoreBookDbHelper.KEY_ITEM_QTY} <= ${StoreBookDbHelper.KEY_ITEM_THRESHOLD}")
                    } else {
                        conditions.add("${StoreBookDbHelper.KEY_ITEM_CATEGORY} = ?")
                        args.add(category)
                    }
                }

                val orderClause = when (sortBy?.lowercase()) {
                    "name_asc" -> "LOWER(${StoreBookDbHelper.KEY_ITEM_NAME}) ASC"
                    "name_desc" -> "LOWER(${StoreBookDbHelper.KEY_ITEM_NAME}) DESC"
                    "price_asc" -> "${StoreBookDbHelper.KEY_ITEM_SELL_PRICE} ASC"
                    "price_desc" -> "${StoreBookDbHelper.KEY_ITEM_SELL_PRICE} DESC"
                    "qty_asc" -> "${StoreBookDbHelper.KEY_ITEM_QTY} ASC"
                    "qty_desc" -> "${StoreBookDbHelper.KEY_ITEM_QTY} DESC"
                    "name" -> "LOWER(${StoreBookDbHelper.KEY_ITEM_NAME}) ASC"
                    "price" -> "${StoreBookDbHelper.KEY_ITEM_SELL_PRICE} DESC"
                    "qty" -> "${StoreBookDbHelper.KEY_ITEM_QTY} ASC"
                    else -> "${StoreBookDbHelper.KEY_ID} DESC"
                }

                val whereClause = conditions.joinToString(" AND ")
                val limitClause = if (limit == Int.MAX_VALUE) "" else " LIMIT $limit OFFSET $offset"

                val cursor = db.rawQuery(
                    "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE $whereClause ORDER BY $orderClause$limitClause",
                    args.toTypedArray()
                )

                val items = mutableListOf<Item>()
                cursor.use { c ->
                    while (c.moveToNext()) {
                        items.add(cursorToItem(c))
                    }
                }
                items
            }

    // --- Udhaar Operations ---

    suspend fun insertUdhaarEntry(entry: UdhaarEntry): Long =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val values =
                        ContentValues().apply {
                            put(StoreBookDbHelper.KEY_UDHAAR_CUSTOMER, entry.customerName)
                            put(StoreBookDbHelper.KEY_UDHAAR_AMOUNT, entry.amount.coerceAtLeast(0.0))
                            put(StoreBookDbHelper.KEY_UDHAAR_TYPE, entry.type)
                            put(StoreBookDbHelper.KEY_TIMESTAMP, entry.timestamp)
                            put(StoreBookDbHelper.KEY_NOTES, entry.notes)
                            put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                            put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                        }
                db.insert(StoreBookDbHelper.TABLE_UDHAAR, null, values)
            }

    suspend fun getUdhaarBalances(): List<CustomerBalance> =
            withContext(Dispatchers.IO) {
                val balances = mutableListOf<CustomerBalance>()
                val db = dbHelper.readableDatabase

                // Group outstanding credit vs payments by customer name
                val cursor =
                        db.rawQuery(
                                "SELECT ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER}, " +
                                        "SUM(CASE WHEN ${StoreBookDbHelper.KEY_UDHAAR_TYPE} = 'CREDIT' THEN " +
                                        "${StoreBookDbHelper.KEY_UDHAAR_AMOUNT} ELSE -" +
                                        "${StoreBookDbHelper.KEY_UDHAAR_AMOUNT} END) as balance, " +
                                        "MAX(${StoreBookDbHelper.KEY_TIMESTAMP}) as last_time " +
                                        "FROM ${StoreBookDbHelper.TABLE_UDHAAR} " +
                                        "WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 " +
                                        "GROUP BY ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} " +
                                        "ORDER BY last_time DESC",
                                null,
                        )
                cursor.use { c ->
                    while (c.moveToNext()) {
                        val customer =
                                c.getString(
                                        c.getColumnIndexOrThrow(
                                                StoreBookDbHelper.KEY_UDHAAR_CUSTOMER
                                        )
                                )
                        val bal = c.getDouble(c.getColumnIndexOrThrow("balance"))
                        val lastTime = c.getLong(c.getColumnIndexOrThrow("last_time"))
                        balances.add(CustomerBalance(customer, bal, lastTime))
                    }
                }
                balances
            }

    suspend fun searchCustomers(
            query: String,
            limit: Int = 50,
    ): List<String> =
            withContext(Dispatchers.IO) {
                val names = mutableListOf<String>()
                val db = dbHelper.readableDatabase
                val q = query.trim()

                val sql =
                        if (q.isEmpty()) {
                            """
                    SELECT customer_name FROM (
                        SELECT ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} AS customer_name,
                               SUM(CASE WHEN ${StoreBookDbHelper.KEY_UDHAAR_TYPE} = 'CREDIT' THEN ${StoreBookDbHelper.KEY_UDHAAR_AMOUNT} ELSE -${StoreBookDbHelper.KEY_UDHAAR_AMOUNT} END) as balance,
                               MAX(${StoreBookDbHelper.KEY_TIMESTAMP}) as ts
                        FROM ${StoreBookDbHelper.TABLE_UDHAAR}
                        WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} IS NOT NULL AND ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} != ''
                        GROUP BY ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER}
                    ) WHERE ABS(balance) > 0.01
                    ORDER BY ts DESC
                    LIMIT ?
                    """.trimIndent()
                        } else {
                            """
                    SELECT customer_name FROM (
                        SELECT ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} AS customer_name,
                               SUM(CASE WHEN ${StoreBookDbHelper.KEY_UDHAAR_TYPE} = 'CREDIT' THEN ${StoreBookDbHelper.KEY_UDHAAR_AMOUNT} ELSE -${StoreBookDbHelper.KEY_UDHAAR_AMOUNT} END) as balance,
                               MAX(${StoreBookDbHelper.KEY_TIMESTAMP}) as ts
                        FROM ${StoreBookDbHelper.TABLE_UDHAAR}
                        WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} LIKE ?
                        GROUP BY ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER}
                    ) WHERE ABS(balance) > 0.01
                    ORDER BY ts DESC
                    LIMIT ?
                    """.trimIndent()
                        }

                val args =
                        if (q.isEmpty()) arrayOf(limit.toString())
                        else arrayOf("%$q%", limit.toString())

                val cursor = db.rawQuery(sql, args)
                cursor.use { c ->
                    while (c.moveToNext()) {
                        names.add(c.getString(0))
                    }
                }
                names
            }

    suspend fun getCustomerDetails(customerName: String): Pair<String?, String?> =
            withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                var gstin: String? = null
                var address: String? = null
                try {
                    val cursor =
                            db.rawQuery(
                                    """
                        SELECT ${StoreBookDbHelper.KEY_SALE_CUSTOMER_GSTIN}, ${StoreBookDbHelper.KEY_SALE_CUSTOMER_ADDRESS}
                        FROM ${StoreBookDbHelper.TABLE_SALES}
                        WHERE ${StoreBookDbHelper.KEY_SALE_CUSTOMER} = ?
                        AND (${StoreBookDbHelper.KEY_SALE_CUSTOMER_GSTIN} IS NOT NULL OR ${StoreBookDbHelper.KEY_SALE_CUSTOMER_ADDRESS} IS NOT NULL)
                        ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC
                        LIMIT 1
                        """.trimIndent(),
                                    arrayOf(customerName),
                            )
                    cursor.use { c ->
                        if (c.moveToFirst()) {
                            gstin = c.getString(0)
                            address = c.getString(1)
                        }
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    e.printStackTrace()
                }
                Pair(gstin, address)
            }

    suspend fun getCustomerLedger(customerName: String): List<UdhaarEntry> =
            withContext(Dispatchers.IO) {
                val entries = mutableListOf<UdhaarEntry>()
                val db = dbHelper.readableDatabase

                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_UDHAAR} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} = ? ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} ASC",
                                arrayOf(customerName),
                        )
                cursor.use { c ->
                    while (c.moveToNext()) {
                        entries.add(
                                UdhaarEntry(
                                        id =
                                                c.getLong(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_ID
                                                        )
                                                ),
                                        customerName =
                                                c.getString(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper
                                                                        .KEY_UDHAAR_CUSTOMER
                                                        )
                                                ),
                                        amount =
                                                c.getDouble(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_UDHAAR_AMOUNT
                                                        )
                                                ),
                                        type =
                                                c.getString(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_UDHAAR_TYPE
                                                        )
                                                ),
                                        timestamp =
                                                c.getLong(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_TIMESTAMP
                                                        )
                                                ),
                                        notes =
                                                c.getString(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_NOTES
                                                        )
                                                ),
                                ),
                        )
                    }
                }
                entries
            }

    // --- Expense Operations ---

    suspend fun insertExpense(entry: ExpenseEntry): Long =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val values =
                        ContentValues().apply {
                            put(StoreBookDbHelper.KEY_EXPENSE_TYPE, entry.type)
                            put(StoreBookDbHelper.KEY_EXPENSE_DESC, entry.description)
                            put(StoreBookDbHelper.KEY_EXPENSE_AMOUNT, entry.amount)
                            put(StoreBookDbHelper.KEY_TIMESTAMP, entry.timestamp)
                            put(StoreBookDbHelper.KEY_EXPENSE_SUPPLIER, entry.supplierName)
                            put(StoreBookDbHelper.KEY_EXPENSE_PHONE, entry.supplierPhone)
                            put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                            put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                        }
                db.insert(StoreBookDbHelper.TABLE_EXPENSES, null, values)
            }

    suspend fun getExpenses(): List<ExpenseEntry> =
            withContext(Dispatchers.IO) {
                val expensesList = mutableListOf<ExpenseEntry>()
                val db = dbHelper.readableDatabase

                val cursor =
                        db.rawQuery(
                                "SELECT * FROM ${StoreBookDbHelper.TABLE_EXPENSES} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC",
                                null,
                        )
                cursor.use { c ->
                    while (c.moveToNext()) {
                        expensesList.add(
                                ExpenseEntry(
                                        id =
                                                c.getLong(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_ID
                                                        )
                                                ),
                                        type =
                                                c.getString(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_EXPENSE_TYPE
                                                        )
                                                ),
                                        description =
                                                c.getString(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_EXPENSE_DESC
                                                        )
                                                ),
                                        amount =
                                                c.getDouble(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_EXPENSE_AMOUNT
                                                        )
                                                ),
                                        timestamp =
                                                c.getLong(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_TIMESTAMP
                                                        )
                                                ),
                                        supplierName =
                                                c.getString(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper
                                                                        .KEY_EXPENSE_SUPPLIER
                                                        )
                                                ),
                                        supplierPhone =
                                                c.getString(
                                                        c.getColumnIndexOrThrow(
                                                                StoreBookDbHelper.KEY_EXPENSE_PHONE
                                                        )
                                                ),
                                ),
                        )
                    }
                }
                expensesList
            }

    suspend fun restockItem(
            itemId: Long,
            quantityToAdd: Double,
            costPrice: Double,
            supplierName: String?,
            supplierPhone: String?,
    ): Boolean =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                db.beginTransaction()
                try {
                    // 1. Fetch item
                    val itemCursor =
                            db.rawQuery(
                                    "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE ${StoreBookDbHelper.KEY_ID} = ?",
                                    arrayOf(itemId.toString()),
                            )
                    if (!itemCursor.moveToFirst()) {
                        itemCursor.close()
                        return@withContext false
                    }
                    val currentItem = cursorToItem(itemCursor)
                    itemCursor.close()

                    // 2. Calculate new quantity and buy price (update buy price if changed)
                    val timeNow = System.currentTimeMillis()
                    val newQty = currentItem.quantity + quantityToAdd
                    val itemValues =
                            ContentValues().apply {
                                put(StoreBookDbHelper.KEY_ITEM_QTY, newQty)
                                put(
                                        StoreBookDbHelper.KEY_ITEM_BUY_PRICE,
                                        costPrice
                                ) // update buy price to latest cost
                                put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                put(StoreBookDbHelper.KEY_UPDATED_AT, timeNow)
                            }
                    db.update(
                            StoreBookDbHelper.TABLE_ITEMS,
                            itemValues,
                            "${StoreBookDbHelper.KEY_ID} = ?",
                            arrayOf(itemId.toString()),
                    )

                    // 3. Log purchase in expenses table
                    val expenseValues =
                            ContentValues().apply {
                                put(StoreBookDbHelper.KEY_EXPENSE_TYPE, "RESTOCK")
                                put(
                                        StoreBookDbHelper.KEY_EXPENSE_DESC,
                                        "Restocked ${currentItem.name} ($quantityToAdd ${currentItem.unit})",
                                )
                                put(StoreBookDbHelper.KEY_EXPENSE_AMOUNT, costPrice * quantityToAdd)
                                put(StoreBookDbHelper.KEY_TIMESTAMP, timeNow)
                                put(StoreBookDbHelper.KEY_EXPENSE_SUPPLIER, supplierName)
                                put(StoreBookDbHelper.KEY_EXPENSE_PHONE, supplierPhone)
                                put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                put(StoreBookDbHelper.KEY_UPDATED_AT, timeNow)
                            }
                    db.insert(StoreBookDbHelper.TABLE_EXPENSES, null, expenseValues)

                    db.setTransactionSuccessful()
                    true
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    false
                } finally {
                    db.endTransaction()
                }
            }

    // --- Utility Methods ---

    private fun cursorToItem(c: Cursor): Item =
            Item(
                    id = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ID)),
                    name = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_NAME)),
                    quantity = c.getDouble(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_QTY)),
                    unit = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_UNIT)),
                    buyPrice =
                            c.getDouble(
                                    c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_BUY_PRICE)
                            ),
                    sellPrice =
                            c.getDouble(
                                    c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_SELL_PRICE)
                            ),
                    lowStockThreshold =
                            c.getDouble(
                                    c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_THRESHOLD)
                            ),
                    category =
                            c.getString(
                                    c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_CATEGORY)
                            ),
                    photoPath =
                            c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_PHOTO)),
                    hsnCode =
                            c.getColumnIndex(StoreBookDbHelper.KEY_ITEM_HSN).let {
                                if (it != -1) c.getString(it) else null
                            },
                    taxRate =
                            c.getColumnIndex(StoreBookDbHelper.KEY_ITEM_TAX_RATE).let {
                                if (it != -1) {
                                    c.getDouble(it)
                                } else {
                                    0.0
                                }
                            },
                    isDeleted =
                            c.getInt(
                                    c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_IS_DELETED)
                            ),
                    deletedTimestamp =
                            c.getLong(
                                    c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_DELETED_TIME)
                            ),
            )

    private fun String.formatName(): String =
            this.trim().split(Regex("\\s+")).joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

    suspend fun standardizeCustomerNames() =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                db.beginTransaction()
                try {
                    val cursor =
                            db.rawQuery(
                                    "SELECT ${StoreBookDbHelper.KEY_ID}, ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} FROM ${StoreBookDbHelper.TABLE_UDHAAR}",
                                    null,
                            )
                    cursor.use { c ->
                        while (c.moveToNext()) {
                            val id = c.getLong(0)
                            val oldName = c.getString(1) ?: continue
                            val newName = oldName.formatName()
                            if (oldName != newName) {
                                val values =
                                        ContentValues().apply {
                                            put(StoreBookDbHelper.KEY_UDHAAR_CUSTOMER, newName)
                                            put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                            put(
                                                    StoreBookDbHelper.KEY_UPDATED_AT,
                                                    System.currentTimeMillis()
                                            )
                                        }
                                db.update(
                                        StoreBookDbHelper.TABLE_UDHAAR,
                                        values,
                                        "${StoreBookDbHelper.KEY_ID} = ?",
                                        arrayOf(id.toString()),
                                )
                            }
                        }
                    }

                    val cursor2 =
                            db.rawQuery(
                                    "SELECT ${StoreBookDbHelper.KEY_ID}, ${StoreBookDbHelper.KEY_SALE_CUSTOMER} FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_SALE_CUSTOMER} IS NOT NULL",
                                    null,
                            )
                    cursor2.use { c ->
                        while (c.moveToNext()) {
                            val id = c.getLong(0)
                            val oldName = c.getString(1) ?: continue
                            val newName = oldName.formatName()
                            if (oldName != newName) {
                                val values =
                                        ContentValues().apply {
                                            put(StoreBookDbHelper.KEY_SALE_CUSTOMER, newName)
                                            put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                            put(
                                                    StoreBookDbHelper.KEY_UPDATED_AT,
                                                    System.currentTimeMillis()
                                            )
                                        }
                                db.update(
                                        StoreBookDbHelper.TABLE_SALES,
                                        values,
                                        "${StoreBookDbHelper.KEY_ID} = ?",
                                        arrayOf(id.toString()),
                                )
                            }
                        }
                    }

                    // Insert sample tax rates for existing items
                    db.execSQL(
                            "UPDATE ${StoreBookDbHelper.TABLE_ITEMS} SET ${StoreBookDbHelper.KEY_ITEM_TAX_RATE} = 18.0 WHERE ${StoreBookDbHelper.KEY_ITEM_TAX_RATE} = 0.0",
                    )

                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    e.printStackTrace()
                } finally {
                    db.endTransaction()
                }
            }

    suspend fun seedDummyData() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            context.assets.open("dummy_data.sql").bufferedReader().use { reader ->
                val sqlContent = reader.readText()
                val statements = sqlContent.split(";")
                for (statement in statements) {
                    val trimmed = statement.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("--")) {
                        db.execSQL(trimmed)
                    }
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    // --- Supplier & Purchase Operations ---

    suspend fun insertSupplier(supplier: Supplier): Long =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put("name", supplier.name)
                    put("phone", supplier.phone)
                    put("gstin", supplier.gstin)
                    put("address", supplier.address)
                    put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                    put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                }
                db.insert(StoreBookDbHelper.TABLE_SUPPLIERS, null, values)
            }

    suspend fun getSuppliers(): List<Supplier> =
            withContext(Dispatchers.IO) {
                val list = mutableListOf<Supplier>()
                val db = dbHelper.readableDatabase
                val cursor = db.rawQuery(
                    "SELECT * FROM ${StoreBookDbHelper.TABLE_SUPPLIERS} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 ORDER BY name ASC",
                    null
                )
                cursor.use { c ->
                    val colId = c.getColumnIndexOrThrow("id")
                    val colName = c.getColumnIndexOrThrow("name")
                    val colPhone = c.getColumnIndexOrThrow("phone")
                    val colGstin = c.getColumnIndexOrThrow("gstin")
                    val colAddr = c.getColumnIndexOrThrow("address")
                    while (c.moveToNext()) {
                        list.add(
                            Supplier(
                                id = c.getLong(colId),
                                name = c.getString(colName),
                                phone = c.getString(colPhone),
                                gstin = c.getString(colGstin),
                                address = c.getString(colAddr)
                            )
                        )
                    }
                }
                list
            }

    suspend fun deleteSupplier(id: Long): Boolean =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put(StoreBookDbHelper.KEY_IS_DELETED, 1)
                    put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                    put(StoreBookDbHelper.KEY_UPDATED_AT, System.currentTimeMillis())
                }
                db.update(StoreBookDbHelper.TABLE_SUPPLIERS, values, "id = ?", arrayOf(id.toString())) > 0
            }

    suspend fun insertPurchase(purchase: Purchase): Long =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                db.beginTransaction()
                try {
                    val timeNow = System.currentTimeMillis()
                    val values = ContentValues().apply {
                        put("supplier_id", purchase.supplierId)
                        put("supplier_name", purchase.supplierName)
                        put("total_amount", purchase.totalAmount)
                        put("tax_amount", purchase.taxAmount)
                        put("type", purchase.type)
                        put(StoreBookDbHelper.KEY_TIMESTAMP, purchase.timestamp)
                        put(StoreBookDbHelper.KEY_NOTES, purchase.notes)
                        put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                        put(StoreBookDbHelper.KEY_UPDATED_AT, timeNow)
                    }
                    val purchaseId = db.insert(StoreBookDbHelper.TABLE_PURCHASES, null, values)

                    if (purchaseId != -1L) {
                        for (pi in purchase.items) {
                            val piValues = ContentValues().apply {
                                put("purchase_id", purchaseId)
                                put("item_id", pi.itemId)
                                put("item_name", pi.itemName)
                                put("quantity", pi.quantity)
                                put("unit", pi.unit)
                                put("buy_price", pi.buyPrice)
                                put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                                put(StoreBookDbHelper.KEY_UPDATED_AT, timeNow)
                            }
                            db.insert(StoreBookDbHelper.TABLE_PURCHASE_ITEMS, null, piValues)

                            // Update item stock quantity and buy price
                            db.execSQL(
                                "UPDATE ${StoreBookDbHelper.TABLE_ITEMS} SET ${StoreBookDbHelper.KEY_ITEM_QTY} = ${StoreBookDbHelper.KEY_ITEM_QTY} + ?, ${StoreBookDbHelper.KEY_ITEM_BUY_PRICE} = ?, ${StoreBookDbHelper.KEY_IS_SYNCED} = 0, ${StoreBookDbHelper.KEY_UPDATED_AT} = ? WHERE ${StoreBookDbHelper.KEY_ID} = ?",
                                arrayOf(pi.quantity, pi.buyPrice, timeNow, pi.itemId)
                            )
                        }
                    }

                    db.setTransactionSuccessful()
                    purchaseId
                } finally {
                    db.endTransaction()
                }
            }

    suspend fun getPurchasesByDateRange(startTs: Long, endTs: Long): List<Purchase> =
            withContext(Dispatchers.IO) {
                val purchasesList = mutableListOf<Purchase>()
                val db = dbHelper.readableDatabase
                val cursor = db.rawQuery(
                    "SELECT * FROM ${StoreBookDbHelper.TABLE_PURCHASES} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND ${StoreBookDbHelper.KEY_TIMESTAMP} BETWEEN ? AND ? ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC",
                    arrayOf(startTs.toString(), endTs.toString())
                )
                cursor.use { c ->
                    val colId = c.getColumnIndexOrThrow("id")
                    val colSuppId = c.getColumnIndexOrThrow("supplier_id")
                    val colSuppName = c.getColumnIndexOrThrow("supplier_name")
                    val colTotal = c.getColumnIndexOrThrow("total_amount")
                    val colTax = c.getColumnIndexOrThrow("tax_amount")
                    val colType = c.getColumnIndexOrThrow("type")
                    val colTs = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_TIMESTAMP)
                    val colNotes = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_NOTES)

                    while (c.moveToNext()) {
                        purchasesList.add(
                            Purchase(
                                id = c.getLong(colId),
                                supplierId = c.getLong(colSuppId),
                                supplierName = c.getString(colSuppName),
                                totalAmount = c.getDouble(colTotal),
                                taxAmount = c.getDouble(colTax),
                                type = c.getString(colType),
                                timestamp = c.getLong(colTs),
                                notes = c.getString(colNotes),
                                items = emptyList()
                            )
                        )
                    }
                }

                if (purchasesList.isEmpty()) return@withContext emptyList()

                val purchaseIds = purchasesList.map { it.id }
                val placeholders = purchaseIds.joinToString(",") { "?" }
                val itemsCursor = db.rawQuery(
                    "SELECT * FROM ${StoreBookDbHelper.TABLE_PURCHASE_ITEMS} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND purchase_id IN ($placeholders)",
                    purchaseIds.map { it.toString() }.toTypedArray()
                )

                val itemsByPurchaseId = mutableMapOf<Long, MutableList<PurchaseItemDetail>>()
                itemsCursor.use { ic ->
                    val piId = ic.getColumnIndexOrThrow("id")
                    val piPurchaseId = ic.getColumnIndexOrThrow("purchase_id")
                    val piItemId = ic.getColumnIndexOrThrow("item_id")
                    val piItemName = ic.getColumnIndexOrThrow("item_name")
                    val piQty = ic.getColumnIndexOrThrow("quantity")
                    val piUnit = ic.getColumnIndexOrThrow("unit")
                    val piPrice = ic.getColumnIndexOrThrow("buy_price")

                    while (ic.moveToNext()) {
                        val pId = ic.getLong(piPurchaseId)
                        itemsByPurchaseId.computeIfAbsent(pId) { mutableListOf() }.add(
                            PurchaseItemDetail(
                                id = ic.getLong(piId),
                                purchaseId = pId,
                                itemId = ic.getLong(piItemId),
                                itemName = ic.getString(piItemName),
                                quantity = ic.getDouble(piQty),
                                unit = ic.getString(piUnit),
                                buyPrice = ic.getDouble(piPrice)
                            )
                        )
                    }
                }

                purchasesList.map { p -> p.copy(items = itemsByPurchaseId[p.id] ?: emptyList()) }
            }

    suspend fun getAllSuppliersMap(): Map<Long, Supplier> =
            withContext(Dispatchers.IO) {
                val map = mutableMapOf<Long, Supplier>()
                val db = dbHelper.readableDatabase
                val cursor = db.rawQuery("SELECT * FROM ${StoreBookDbHelper.TABLE_SUPPLIERS}", null)
                cursor.use { c ->
                    val colId = c.getColumnIndexOrThrow("id")
                    val colName = c.getColumnIndexOrThrow("name")
                    val colPhone = c.getColumnIndexOrThrow("phone")
                    val colGstin = c.getColumnIndexOrThrow("gstin")
                    val colAddr = c.getColumnIndexOrThrow("address")
                    while (c.moveToNext()) {
                        val id = c.getLong(colId)
                        map[id] = Supplier(
                            id = id,
                            name = c.getString(colName),
                            phone = c.getString(colPhone),
                            gstin = c.getString(colGstin),
                            address = c.getString(colAddr)
                        )
                    }
                }
                map
            }

    suspend fun getAllItemsMap(): Map<Long, Item> =
            withContext(Dispatchers.IO) {
                val map = mutableMapOf<Long, Item>()
                val db = dbHelper.readableDatabase
                val cursor = db.rawQuery("SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS}", null)
                cursor.use { c ->
                    val colId = c.getColumnIndexOrThrow("id")
                    val colName = c.getColumnIndexOrThrow("name")
                    val colQty = c.getColumnIndexOrThrow("quantity")
                    val colUnit = c.getColumnIndexOrThrow("unit")
                    val colBuy = c.getColumnIndexOrThrow("buy_price")
                    val colSell = c.getColumnIndexOrThrow("sell_price")
                    val colLow = c.getColumnIndexOrThrow("low_stock_threshold")
                    val colCat = c.getColumnIndexOrThrow("category")
                    val colPhoto = c.getColumnIndexOrThrow("photo_path")
                    val colHsn = c.getColumnIndexOrThrow("hsn_code")
                    val colTax = c.getColumnIndexOrThrow("tax_rate")
                    while (c.moveToNext()) {
                        val id = c.getLong(colId)
                        map[id] = Item(
                            id = id,
                            name = c.getString(colName),
                            quantity = c.getDouble(colQty),
                            unit = c.getString(colUnit),
                            buyPrice = c.getDouble(colBuy),
                            sellPrice = c.getDouble(colSell),
                            lowStockThreshold = c.getDouble(colLow),
                            category = c.getString(colCat) ?: "",
                            photoPath = c.getString(colPhoto),
                            hsnCode = c.getString(colHsn),
                            taxRate = c.getDouble(colTax)
                        )
                    }
                }
                map
            }

    suspend fun getPurchases(): List<Purchase> =
            withContext(Dispatchers.IO) {
                val purchasesList = mutableListOf<Purchase>()
                val db = dbHelper.readableDatabase
                val cursor = db.rawQuery(
                    "SELECT * FROM ${StoreBookDbHelper.TABLE_PURCHASES} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC",
                    null
                )
                cursor.use { c ->
                    val colId = c.getColumnIndexOrThrow("id")
                    val colSuppId = c.getColumnIndexOrThrow("supplier_id")
                    val colSuppName = c.getColumnIndexOrThrow("supplier_name")
                    val colTotal = c.getColumnIndexOrThrow("total_amount")
                    val colTax = c.getColumnIndexOrThrow("tax_amount")
                    val colType = c.getColumnIndexOrThrow("type")
                    val colTs = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_TIMESTAMP)
                    val colNotes = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_NOTES)

                    while (c.moveToNext()) {
                        purchasesList.add(
                            Purchase(
                                id = c.getLong(colId),
                                supplierId = c.getLong(colSuppId),
                                supplierName = c.getString(colSuppName),
                                totalAmount = c.getDouble(colTotal),
                                taxAmount = c.getDouble(colTax),
                                type = c.getString(colType),
                                timestamp = c.getLong(colTs),
                                notes = c.getString(colNotes),
                                items = emptyList()
                            )
                        )
                    }
                }

                if (purchasesList.isEmpty()) return@withContext emptyList()

                val purchaseIds = purchasesList.map { it.id }
                val placeholders = purchaseIds.joinToString(",") { "?" }
                val itemsCursor = db.rawQuery(
                    "SELECT * FROM ${StoreBookDbHelper.TABLE_PURCHASE_ITEMS} WHERE ${StoreBookDbHelper.KEY_IS_DELETED} = 0 AND purchase_id IN ($placeholders)",
                    purchaseIds.map { it.toString() }.toTypedArray()
                )

                val itemsByPurchaseId = mutableMapOf<Long, MutableList<PurchaseItemDetail>>()
                itemsCursor.use { ic ->
                    val piId = ic.getColumnIndexOrThrow("id")
                    val piPurchaseId = ic.getColumnIndexOrThrow("purchase_id")
                    val piItemId = ic.getColumnIndexOrThrow("item_id")
                    val piItemName = ic.getColumnIndexOrThrow("item_name")
                    val piQty = ic.getColumnIndexOrThrow("quantity")
                    val piUnit = ic.getColumnIndexOrThrow("unit")
                    val piPrice = ic.getColumnIndexOrThrow("buy_price")

                    while (ic.moveToNext()) {
                        val pId = ic.getLong(piPurchaseId)
                        itemsByPurchaseId.computeIfAbsent(pId) { mutableListOf() }.add(
                            PurchaseItemDetail(
                                id = ic.getLong(piId),
                                purchaseId = pId,
                                itemId = ic.getLong(piItemId),
                                itemName = ic.getString(piItemName),
                                quantity = ic.getDouble(piQty),
                                unit = ic.getString(piUnit),
                                buyPrice = ic.getDouble(piPrice)
                            )
                        )
                    }
                }

                purchasesList.map { p -> p.copy(items = itemsByPurchaseId[p.id] ?: emptyList()) }
            }

    suspend fun getSupplierBalances(): List<SupplierBalance> =
            withContext(Dispatchers.IO) {
                val list = mutableListOf<SupplierBalance>()
                val db = dbHelper.readableDatabase

                val cursor = db.rawQuery(
                    """
                    SELECT s.id, s.name, s.phone,
                           SUM(CASE WHEN p.type = 'BILL' THEN p.total_amount ELSE -p.total_amount END) as balance,
                           MAX(p.timestamp) as last_time
                    FROM ${StoreBookDbHelper.TABLE_SUPPLIERS} s
                    LEFT JOIN ${StoreBookDbHelper.TABLE_PURCHASES} p ON s.id = p.supplier_id AND p.is_deleted = 0
                    WHERE s.is_deleted = 0
                    GROUP BY s.id, s.name, s.phone
                    ORDER BY last_time DESC, s.name ASC
                    """.trimIndent(),
                    null
                )

                cursor.use { c ->
                    while (c.moveToNext()) {
                        val id = c.getLong(0)
                        val name = c.getString(1)
                        val phone = c.getString(2)
                        val bal = c.getDouble(3)
                        val lastTime = c.getLong(4)
                        list.add(
                            SupplierBalance(
                                supplierId = id,
                                supplierName = name,
                                phone = phone,
                                netBalance = bal,
                                lastTransactionTime = if (lastTime == 0L) System.currentTimeMillis() else lastTime
                            )
                        )
                    }
                }
                list
            }

    suspend fun insertSupplierPayment(supplierId: Long, supplierName: String, amount: Double, notes: String?, timestamp: Long): Long =
            withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                val timeNow = System.currentTimeMillis()
                val values = ContentValues().apply {
                    put("supplier_id", supplierId)
                    put("supplier_name", supplierName)
                    put("total_amount", amount)
                    put("tax_amount", 0.0)
                    put("type", "PAYMENT")
                    put(StoreBookDbHelper.KEY_TIMESTAMP, timestamp)
                    put(StoreBookDbHelper.KEY_NOTES, notes)
                    put(StoreBookDbHelper.KEY_IS_SYNCED, 0)
                    put(StoreBookDbHelper.KEY_UPDATED_AT, timeNow)
                }
                db.insert(StoreBookDbHelper.TABLE_PURCHASES, null, values)
            }

    suspend fun clearLocalDatabase() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM ${StoreBookDbHelper.TABLE_ITEMS}")
            db.execSQL("DELETE FROM ${StoreBookDbHelper.TABLE_SALES}")
            db.execSQL("DELETE FROM ${StoreBookDbHelper.TABLE_SALE_ITEMS}")
            db.execSQL("DELETE FROM ${StoreBookDbHelper.TABLE_UDHAAR}")
            db.execSQL("DELETE FROM ${StoreBookDbHelper.TABLE_EXPENSES}")
            db.execSQL("DELETE FROM ${StoreBookDbHelper.TABLE_SUPPLIERS}")
            db.execSQL("DELETE FROM ${StoreBookDbHelper.TABLE_PURCHASES}")
            db.execSQL("DELETE FROM ${StoreBookDbHelper.TABLE_PURCHASE_ITEMS}")
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    // --- Item Batch Operations (Phase 4) ---

    suspend fun insertItemBatch(batch: ItemBatch): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("item_id", batch.itemId)
            put("batch_number", batch.batchNumber)
            put("expiry_date", batch.expiryDate)
            put("quantity", batch.quantity)
            put("cost_price", batch.costPrice)
            put("timestamp", batch.timestamp)
            put("notes", batch.notes)
            put("is_synced", 0)
            put("updated_at", System.currentTimeMillis())
            put("is_deleted", 0)
        }
        db.insertWithOnConflict(StoreBookDbHelper.TABLE_ITEM_BATCHES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun getBatchesForItem(itemId: Long): List<ItemBatch> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEM_BATCHES} WHERE item_id = ? AND is_deleted = 0 ORDER BY expiry_date ASC",
            arrayOf(itemId.toString())
        )
        val list = mutableListOf<ItemBatch>()
        cursor.use { c ->
            val colId = c.getColumnIndexOrThrow("id")
            val colItemId = c.getColumnIndexOrThrow("item_id")
            val colBatch = c.getColumnIndexOrThrow("batch_number")
            val colExpiry = c.getColumnIndexOrThrow("expiry_date")
            val colQty = c.getColumnIndexOrThrow("quantity")
            val colCost = c.getColumnIndexOrThrow("cost_price")
            val colTs = c.getColumnIndexOrThrow("timestamp")
            val colNotes = c.getColumnIndexOrThrow("notes")
            while (c.moveToNext()) {
                list.add(ItemBatch(
                    id = c.getLong(colId),
                    itemId = c.getLong(colItemId),
                    batchNumber = c.getString(colBatch),
                    expiryDate = if (c.isNull(colExpiry)) null else c.getLong(colExpiry),
                    quantity = c.getDouble(colQty),
                    costPrice = c.getDouble(colCost),
                    timestamp = c.getLong(colTs),
                    notes = c.getString(colNotes),
                ))
            }
        }
        list
    }

    /** Returns batches expiring within the next [daysAhead] days */
    suspend fun getNearExpiryBatches(daysAhead: Int = 30): List<ItemBatch> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cutoff = now + daysAhead.toLong() * 24 * 60 * 60 * 1000
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEM_BATCHES} WHERE is_deleted = 0 AND expiry_date IS NOT NULL AND expiry_date BETWEEN ? AND ? ORDER BY expiry_date ASC",
            arrayOf(now.toString(), cutoff.toString())
        )
        val list = mutableListOf<ItemBatch>()
        cursor.use { c ->
            val colId = c.getColumnIndexOrThrow("id")
            val colItemId = c.getColumnIndexOrThrow("item_id")
            val colBatch = c.getColumnIndexOrThrow("batch_number")
            val colExpiry = c.getColumnIndexOrThrow("expiry_date")
            val colQty = c.getColumnIndexOrThrow("quantity")
            val colCost = c.getColumnIndexOrThrow("cost_price")
            val colTs = c.getColumnIndexOrThrow("timestamp")
            val colNotes = c.getColumnIndexOrThrow("notes")
            while (c.moveToNext()) {
                list.add(ItemBatch(
                    id = c.getLong(colId),
                    itemId = c.getLong(colItemId),
                    batchNumber = c.getString(colBatch),
                    expiryDate = if (c.isNull(colExpiry)) null else c.getLong(colExpiry),
                    quantity = c.getDouble(colQty),
                    costPrice = c.getDouble(colCost),
                    timestamp = c.getLong(colTs),
                    notes = c.getString(colNotes),
                ))
            }
        }
        list
    }

    suspend fun deleteItemBatch(batchId: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("is_deleted", 1)
            put("updated_at", System.currentTimeMillis())
        }
        db.update(StoreBookDbHelper.TABLE_ITEM_BATCHES, cv, "id = ?", arrayOf(batchId.toString()))
    }

    // --- Sync Operations (Offline to Cloud) ---
    fun getUnsyncedItems(): List<SyncItem> {
        val list = mutableListOf<SyncItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM items WHERE is_synced = 0", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SyncItem(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                        unit = cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                        buyPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("buy_price")),
                        sellPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("sell_price")),
                        lowStockThreshold = cursor.getDouble(cursor.getColumnIndexOrThrow("low_stock_threshold")),
                        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        photoPath = cursor.getString(cursor.getColumnIndexOrThrow("photo_path")),
                        hsnCode = cursor.getString(cursor.getColumnIndexOrThrow("hsn_code")),
                        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")) == 1,
                        updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun markItemSynced(id: Long, cloudId: String) {
        val db = dbHelper.writableDatabase
        val cv = android.content.ContentValues().apply {
            put("is_synced", 1)
            put("cloud_id", cloudId)
        }
        db.update("items", cv, "id = ?", arrayOf(id.toString()))
    }

    fun getUnsyncedSales(): List<SyncSale> {
        val list = mutableListOf<SyncSale>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM sales WHERE is_synced = 0", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SyncSale(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                        totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")),
                        discountAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("discount_amount")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                        customerGstin = cursor.getString(cursor.getColumnIndexOrThrow("customer_gstin")),
                        businessGstin = cursor.getString(cursor.getColumnIndexOrThrow("business_gstin")),
                        customerAddress = cursor.getString(cursor.getColumnIndexOrThrow("customer_address")),
                        businessAddress = cursor.getString(cursor.getColumnIndexOrThrow("business_address")),
                        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")) == 1,
                        updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun markSaleSynced(id: Long, cloudId: String) {
        val db = dbHelper.writableDatabase
        val cv = android.content.ContentValues().apply {
            put("is_synced", 1)
            put("cloud_id", cloudId)
        }
        db.update("sales", cv, "id = ?", arrayOf(id.toString()))
    }

    fun getUnsyncedSaleItems(): List<SyncSaleItem> {
        val list = mutableListOf<SyncSaleItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM sale_items WHERE is_synced = 0", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SyncSaleItem(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        saleId = cursor.getLong(cursor.getColumnIndexOrThrow("sale_id")),
                        itemId = cursor.getLong(cursor.getColumnIndexOrThrow("item_id")),
                        itemName = cursor.getString(cursor.getColumnIndexOrThrow("item_name")),
                        unit = cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                        quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                        sellPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("sell_price")),
                        buyPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("buy_price")),
                        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")) == 1,
                        updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun markSaleItemSynced(id: Long, cloudId: String) {
        val db = dbHelper.writableDatabase
        val cv = android.content.ContentValues().apply {
            put("is_synced", 1)
            put("cloud_id", cloudId)
        }
        db.update("sale_items", cv, "id = ?", arrayOf(id.toString()))
    }


data class SyncItem(
    val id: Long, val name: String, val quantity: Double, val unit: String,
    val buyPrice: Double, val sellPrice: Double, val lowStockThreshold: Double,
    val category: String, val isDeleted: Boolean, val updatedAt: Long,
    val photoPath: String?, val hsnCode: String?
)

data class SyncSale(
    val id: Long, val timestamp: Long, val totalAmount: Double, val discountAmount: Double,
    val type: String, val isDeleted: Boolean, val updatedAt: Long,
    val customerName: String?, val customerGstin: String?, val businessGstin: String?,
    val customerAddress: String?, val businessAddress: String?, val notes: String?
)

data class SyncSaleItem(
    val id: Long, val saleId: Long, val itemId: Long, val itemName: String,
    val unit: String, val quantity: Double, val sellPrice: Double, val buyPrice: Double,
    val isDeleted: Boolean, val updatedAt: Long
)

    fun getUnsyncedUdhaars(): List<SyncUdhaar> {
        val list = mutableListOf<SyncUdhaar>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM udhaar WHERE is_synced = 0", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SyncUdhaar(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")) == 1,
                        updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun markUdhaarSynced(id: Long, cloudId: String) {
        val db = dbHelper.writableDatabase
        val cv = android.content.ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }
        db.update("udhaar", cv, "id = ?", arrayOf(id.toString()))
    }

    fun getUnsyncedExpenses(): List<SyncExpense> {
        val list = mutableListOf<SyncExpense>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM expenses WHERE is_synced = 0", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SyncExpense(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                        supplierName = cursor.getString(cursor.getColumnIndexOrThrow("supplier")),
                        supplierPhone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")) == 1,
                        updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun markExpenseSynced(id: Long, cloudId: String) {
        val db = dbHelper.writableDatabase
        val cv = android.content.ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }
        db.update("expenses", cv, "id = ?", arrayOf(id.toString()))
    }

    fun getUnsyncedSuppliers(): List<SyncSupplier> {
        val list = mutableListOf<SyncSupplier>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM suppliers WHERE is_synced = 0", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SyncSupplier(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        gstin = cursor.getString(cursor.getColumnIndexOrThrow("gstin")),
                        address = cursor.getString(cursor.getColumnIndexOrThrow("address")),
                        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")) == 1,
                        updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun markSupplierSynced(id: Long, cloudId: String) {
        val db = dbHelper.writableDatabase
        val cv = android.content.ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }
        db.update("suppliers", cv, "id = ?", arrayOf(id.toString()))
    }

    fun getUnsyncedPurchases(): List<SyncPurchase> {
        val list = mutableListOf<SyncPurchase>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM purchases WHERE is_synced = 0", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SyncPurchase(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        supplierId = cursor.getLong(cursor.getColumnIndexOrThrow("supplier_id")),
                        supplierName = cursor.getString(cursor.getColumnIndexOrThrow("supplier_name")),
                        totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")),
                        taxAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("tax_amount")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")) == 1,
                        updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun markPurchaseSynced(id: Long, cloudId: String) {
        val db = dbHelper.writableDatabase
        val cv = android.content.ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }
        db.update("purchases", cv, "id = ?", arrayOf(id.toString()))
    }
    
    fun getUnsyncedPurchaseItems(): List<SyncPurchaseItem> {
        val list = mutableListOf<SyncPurchaseItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM purchase_items WHERE is_synced = 0", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    SyncPurchaseItem(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        purchaseId = cursor.getLong(cursor.getColumnIndexOrThrow("purchase_id")),
                        itemId = cursor.getLong(cursor.getColumnIndexOrThrow("item_id")),
                        itemName = cursor.getString(cursor.getColumnIndexOrThrow("item_name")),
                        quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                        unit = cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                        buyPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("buy_price")),
                        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")) == 1,
                        updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun markPurchaseItemSynced(id: Long, cloudId: String) {
        val db = dbHelper.writableDatabase
        val cv = android.content.ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }
        db.update("purchase_items", cv, "id = ?", arrayOf(id.toString()))
    }
}

data class SyncUdhaar(val id: Long, val customerName: String, val amount: Double, val type: String, val timestamp: Long, val notes: String?, val isDeleted: Boolean, val updatedAt: Long)
data class SyncExpense(val id: Long, val type: String, val description: String, val amount: Double, val timestamp: Long, val supplierName: String?, val supplierPhone: String?, val isDeleted: Boolean, val updatedAt: Long)
data class SyncSupplier(val id: Long, val name: String, val phone: String?, val gstin: String?, val address: String?, val isDeleted: Boolean, val updatedAt: Long)
data class SyncPurchase(val id: Long, val supplierId: Long, val supplierName: String, val totalAmount: Double, val taxAmount: Double, val type: String, val timestamp: Long, val notes: String?, val isDeleted: Boolean, val updatedAt: Long)
data class SyncPurchaseItem(val id: Long, val purchaseId: Long, val itemId: Long, val itemName: String, val quantity: Double, val unit: String, val buyPrice: Double, val isDeleted: Boolean, val updatedAt: Long)
