package com.storebook.inventoryapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.storebook.inventoryapp.data.local.StoreBookDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Serializable

// --- Models ---
data class Item(
    val id: Long = 0,
    val name: String,
    val quantity: Double,
    val unit: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val lowStockThreshold: Double,
    val category: String,
    val photoPath: String? = null,
    val isDeleted: Int = 0,
    val deletedTimestamp: Long = 0
) : Serializable

data class CartItem(
    val item: Item,
    var quantity: Double
) : Serializable

data class Sale(
    val id: Long = 0,
    val timestamp: Long,
    val totalAmount: Double,
    val discountAmount: Double,
    val customerName: String? = null,
    val notes: String? = null,
    val items: List<SaleItemDetail> = emptyList()
) : Serializable

data class SaleItemDetail(
    val id: Long = 0,
    val itemId: Long,
    val itemName: String,
    val quantity: Double,
    val unit: String,
    val sellPrice: Double,
    val buyPrice: Double
) : Serializable

data class UdhaarEntry(
    val id: Long = 0,
    val customerName: String,
    val amount: Double,
    val type: String, // 'CREDIT' or 'PAYMENT'
    val timestamp: Long,
    val notes: String? = null
) : Serializable

data class CustomerBalance(
    val customerName: String,
    val netBalance: Double, // positive = customer owes shop, negative = shop owes customer
    val lastTransactionTime: Long
) : Serializable

data class ExpenseEntry(
    val id: Long = 0,
    val type: String, // 'RESTOCK' or 'OVERHEAD'
    val description: String,
    val amount: Double,
    val timestamp: Long,
    val supplierName: String? = null,
    val supplierPhone: String? = null
) : Serializable

// --- Repository ---
class StoreBookRepository(context: Context) {
    private val dbHelper = StoreBookDbHelper(context)

    // --- Inventory Operations ---

    suspend fun getActiveItems(): List<Item> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Item>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE ${StoreBookDbHelper.KEY_ITEM_IS_DELETED} = 0 ORDER BY ${StoreBookDbHelper.KEY_ITEM_NAME} ASC",
            null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(cursorToItem(c))
            }
        }
        list
    }

    suspend fun getItemById(id: Long): Item? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE ${StoreBookDbHelper.KEY_ID} = ?",
            arrayOf(id.toString())
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                cursorToItem(c)
            } else {
                null
            }
        }
    }

    suspend fun insertItem(item: Item): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(StoreBookDbHelper.KEY_ITEM_NAME, item.name)
            put(StoreBookDbHelper.KEY_ITEM_QTY, item.quantity)
            put(StoreBookDbHelper.KEY_ITEM_UNIT, item.unit)
            put(StoreBookDbHelper.KEY_ITEM_BUY_PRICE, item.buyPrice)
            put(StoreBookDbHelper.KEY_ITEM_SELL_PRICE, item.sellPrice)
            put(StoreBookDbHelper.KEY_ITEM_THRESHOLD, item.lowStockThreshold)
            put(StoreBookDbHelper.KEY_ITEM_CATEGORY, item.category)
            put(StoreBookDbHelper.KEY_ITEM_PHOTO, item.photoPath)
            put(StoreBookDbHelper.KEY_ITEM_IS_DELETED, 0)
            put(StoreBookDbHelper.KEY_ITEM_DELETED_TIME, 0)
        }
        db.insertWithOnConflict(StoreBookDbHelper.TABLE_ITEMS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun updateItem(item: Item): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(StoreBookDbHelper.KEY_ITEM_NAME, item.name)
            put(StoreBookDbHelper.KEY_ITEM_QTY, item.quantity)
            put(StoreBookDbHelper.KEY_ITEM_UNIT, item.unit)
            put(StoreBookDbHelper.KEY_ITEM_BUY_PRICE, item.buyPrice)
            put(StoreBookDbHelper.KEY_ITEM_SELL_PRICE, item.sellPrice)
            put(StoreBookDbHelper.KEY_ITEM_THRESHOLD, item.lowStockThreshold)
            put(StoreBookDbHelper.KEY_ITEM_CATEGORY, item.category)
            put(StoreBookDbHelper.KEY_ITEM_PHOTO, item.photoPath)
            put(StoreBookDbHelper.KEY_ITEM_IS_DELETED, item.isDeleted)
            put(StoreBookDbHelper.KEY_ITEM_DELETED_TIME, item.deletedTimestamp)
        }
        db.update(StoreBookDbHelper.TABLE_ITEMS, values, "${StoreBookDbHelper.KEY_ID} = ?", arrayOf(item.id.toString()))
    }

    suspend fun softDeleteItem(id: Long): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(StoreBookDbHelper.KEY_ITEM_IS_DELETED, 1)
            put(StoreBookDbHelper.KEY_ITEM_DELETED_TIME, System.currentTimeMillis())
        }
        db.update(StoreBookDbHelper.TABLE_ITEMS, values, "${StoreBookDbHelper.KEY_ID} = ?", arrayOf(id.toString()))
    }

    suspend fun recoverSoftDeletedItems(): List<Item> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Item>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE ${StoreBookDbHelper.KEY_ITEM_IS_DELETED} = 1 ORDER BY ${StoreBookDbHelper.KEY_ITEM_DELETED_TIME} DESC",
            null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(cursorToItem(c))
            }
        }
        list
    }

    suspend fun restoreItem(id: Long): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(StoreBookDbHelper.KEY_ITEM_IS_DELETED, 0)
            put(StoreBookDbHelper.KEY_ITEM_DELETED_TIME, 0)
        }
        db.update(StoreBookDbHelper.TABLE_ITEMS, values, "${StoreBookDbHelper.KEY_ID} = ?", arrayOf(id.toString()))
    }

    // --- Sales Operations ---

    suspend fun recordSale(
        itemsInCart: List<CartItem>,
        discount: Double,
        customerName: String?,
        notes: String?,
        paymentMode: String
    ): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Calculate total sale amount
            var subtotal = 0.0
            for (cartItem in itemsInCart) {
                subtotal += cartItem.item.sellPrice * cartItem.quantity
            }
            val total = subtotal - discount

            // 2. Insert into Sales table
            val saleTime = System.currentTimeMillis()
            val saleValues = ContentValues().apply {
                put(StoreBookDbHelper.KEY_TIMESTAMP, saleTime)
                put(StoreBookDbHelper.KEY_SALE_TOTAL, total)
                put(StoreBookDbHelper.KEY_SALE_DISCOUNT, discount)
                put(StoreBookDbHelper.KEY_SALE_CUSTOMER, customerName)
                put(StoreBookDbHelper.KEY_NOTES, notes)
            }
            val saleId = db.insert(StoreBookDbHelper.TABLE_SALES, null, saleValues)

            if (saleId == -1L) {
                throw Exception("Failed to insert sale record")
            }

            // 3. Process each item in cart
            for (cartItem in itemsInCart) {
                // Insert into sale_items table
                val saleItemValues = ContentValues().apply {
                    put(StoreBookDbHelper.KEY_SI_SALE_ID, saleId)
                    put(StoreBookDbHelper.KEY_SI_ITEM_ID, cartItem.item.id)
                    put(StoreBookDbHelper.KEY_SI_ITEM_NAME, cartItem.item.name)
                    put(StoreBookDbHelper.KEY_SI_UNIT, cartItem.item.unit)
                    put(StoreBookDbHelper.KEY_SI_QTY, cartItem.quantity)
                    put(StoreBookDbHelper.KEY_SI_SELL_PRICE, cartItem.item.sellPrice)
                    put(StoreBookDbHelper.KEY_SI_BUY_PRICE, cartItem.item.buyPrice)
                }
                db.insert(StoreBookDbHelper.TABLE_SALE_ITEMS, null, saleItemValues)

                // Update quantity in items table
                val newQty = cartItem.item.quantity - cartItem.quantity
                val itemUpdateValues = ContentValues().apply {
                    put(StoreBookDbHelper.KEY_ITEM_QTY, newQty)
                }
                db.update(
                    StoreBookDbHelper.TABLE_ITEMS,
                    itemUpdateValues,
                    "${StoreBookDbHelper.KEY_ID} = ?",
                    arrayOf(cartItem.item.id.toString())
                )
            }

            // 4. If customer name is provided, total > 0, and payment mode is Udhaar, record a credit entry automatically
            if (!customerName.isNullOrBlank() && paymentMode.equals("Udhaar", ignoreCase = true)) {
                val udhaarValues = ContentValues().apply {
                    put(StoreBookDbHelper.KEY_UDHAAR_CUSTOMER, customerName)
                    put(StoreBookDbHelper.KEY_UDHAAR_AMOUNT, total)
                    put(StoreBookDbHelper.KEY_UDHAAR_TYPE, "CREDIT")
                    put(StoreBookDbHelper.KEY_TIMESTAMP, saleTime)
                    put(StoreBookDbHelper.KEY_NOTES, "Sale bill #$saleId" + (if (notes != null) " - $notes" else ""))
                }
                db.insert(StoreBookDbHelper.TABLE_UDHAAR, null, udhaarValues)
            }

            db.setTransactionSuccessful()
            saleId
        } finally {
            db.endTransaction()
        }
    }

    suspend fun undoSale(saleId: Long): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Fetch sale details
            val cursor = db.rawQuery(
                "SELECT * FROM ${StoreBookDbHelper.TABLE_SALE_ITEMS} WHERE ${StoreBookDbHelper.KEY_SI_SALE_ID} = ?",
                arrayOf(saleId.toString())
            )
            val saleItems = mutableListOf<Pair<Long, Double>>() // Pair(ItemId, Quantity)
            cursor.use { c ->
                while (c.moveToNext()) {
                    val itemId = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_ITEM_ID))
                    val qty = c.getDouble(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SI_QTY))
                    saleItems.add(Pair(itemId, qty))
                }
            }

            // 2. Add back stock to items
            for (pair in saleItems) {
                db.execSQL(
                    "UPDATE ${StoreBookDbHelper.TABLE_ITEMS} SET ${StoreBookDbHelper.KEY_ITEM_QTY} = ${StoreBookDbHelper.KEY_ITEM_QTY} + ? WHERE ${StoreBookDbHelper.KEY_ID} = ?",
                    arrayOf(pair.second, pair.first)
                )
            }

            // 3. Delete from Udhaar associated with this sale
            db.delete(
                StoreBookDbHelper.TABLE_UDHAAR,
                "${StoreBookDbHelper.KEY_NOTES} LIKE ?",
                arrayOf("Sale bill #$saleId%")
            )

            // 4. Delete sale and sale_items
            db.delete(StoreBookDbHelper.TABLE_SALE_ITEMS, "${StoreBookDbHelper.KEY_SI_SALE_ID} = ?", arrayOf(saleId.toString()))
            db.delete(StoreBookDbHelper.TABLE_SALES, "${StoreBookDbHelper.KEY_ID} = ?", arrayOf(saleId.toString()))

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    suspend fun getSales(): List<Sale> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_SALES} ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC",
            null
        )
        fetchSalesFromCursor(cursor)
    }

    suspend fun getSalesPage(limit: Int = 50, offset: Int = 0): List<Sale> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_SALES} ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC LIMIT ? OFFSET ?",
            arrayOf(limit.toString(), offset.toString())
        )
        fetchSalesFromCursor(cursor)
    }

    suspend fun getSalesByDateRange(startTs: Long, endTs: Long): List<Sale> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_TIMESTAMP} BETWEEN ? AND ? ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC",
            arrayOf(startTs.toString(), endTs.toString())
        )
        fetchSalesFromCursor(cursor)
    }

    private suspend fun fetchSalesFromCursor(cursor: android.database.Cursor): List<Sale> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val salesList = mutableListOf<Sale>()
        cursor.use { c ->
            val colId = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ID)
            val ts = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_TIMESTAMP)
            val total = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_TOTAL)
            val disc = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_DISCOUNT)
            val cust = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_SALE_CUSTOMER)
            val notes = c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_NOTES)
            while (c.moveToNext()) {
                salesList.add(Sale(
                    id = c.getLong(colId),
                    timestamp = c.getLong(ts),
                    totalAmount = c.getDouble(total),
                    discountAmount = c.getDouble(disc),
                    customerName = c.getString(cust),
                    notes = c.getString(notes),
                    items = emptyList()
                ))
            }
        }
        if (salesList.isEmpty()) return@withContext emptyList()
        val saleIds = salesList.map { it.id }
        val placeholders = saleIds.joinToString(",") { "?" }
        val itemsCursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_SALE_ITEMS} WHERE ${StoreBookDbHelper.KEY_SI_SALE_ID} IN ($placeholders)",
            saleIds.map { it.toString() }.toTypedArray()
        )
        val itemsBySaleId = mutableMapOf<Long, MutableList<SaleItemDetail>>()
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
                itemsBySaleId.computeIfAbsent(saleId) { mutableListOf() }.add(
                    SaleItemDetail(
                        id = ic.getLong(ic.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ID)),
                        itemId = ic.getLong(siItemId),
                        itemName = ic.getString(siItemName),
                        quantity = ic.getDouble(siQty),
                        unit = ic.getString(siUnit),
                        sellPrice = ic.getDouble(siSell),
                        buyPrice = ic.getDouble(siBuy)
                    )
                )
            }
        }
        salesList.map { sale -> sale.copy(items = itemsBySaleId[sale.id] ?: emptyList()) }
    }

    suspend fun getActiveItemsFiltered(
        search: String? = null,
        category: String? = null,
        sortBy: String? = null,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<Item> = withContext(Dispatchers.IO) {
        val all = getActiveItems().toMutableList()
        if (!search.isNullOrBlank()) {
            val q = search.lowercase()
            all.removeAll { !it.name.lowercase().contains(q) }
        }
        if (category != null && category != "All") {
            all.removeAll { it.category != category }
        }
        sortBy?.let {
            when (it.lowercase()) {
                "name" -> all.sortBy { item -> item.name.lowercase() }
                "price" -> all.sortByDescending { it.sellPrice }
                "qty" -> all.sortBy { it.quantity }
            }
        }
        all.subList(0, minOf(all.size.coerceAtMost(limit.coerceAtLeast(offset)), limit.coerceAtLeast(offset))).drop(offset)
    }

    // --- Udhaar Operations ---

    suspend fun insertUdhaarEntry(entry: UdhaarEntry): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(StoreBookDbHelper.KEY_UDHAAR_CUSTOMER, entry.customerName)
            put(StoreBookDbHelper.KEY_UDHAAR_AMOUNT, entry.amount)
            put(StoreBookDbHelper.KEY_UDHAAR_TYPE, entry.type)
            put(StoreBookDbHelper.KEY_TIMESTAMP, entry.timestamp)
            put(StoreBookDbHelper.KEY_NOTES, entry.notes)
        }
        db.insert(StoreBookDbHelper.TABLE_UDHAAR, null, values)
    }

    suspend fun getUdhaarBalances(): List<CustomerBalance> = withContext(Dispatchers.IO) {
        val balances = mutableListOf<CustomerBalance>()
        val db = dbHelper.readableDatabase

        // Group outstanding credit vs payments by customer name
        val cursor = db.rawQuery(
            "SELECT ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER}, " +
                    "SUM(CASE WHEN ${StoreBookDbHelper.KEY_UDHAAR_TYPE} = 'CREDIT' THEN ${StoreBookDbHelper.KEY_UDHAAR_AMOUNT} ELSE -${StoreBookDbHelper.KEY_UDHAAR_AMOUNT} END) as balance, " +
                    "MAX(${StoreBookDbHelper.KEY_TIMESTAMP}) as last_time " +
                    "FROM ${StoreBookDbHelper.TABLE_UDHAAR} " +
                    "GROUP BY ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} " +
                    "ORDER BY last_time DESC",
            null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val customer = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_UDHAAR_CUSTOMER))
                val bal = c.getDouble(c.getColumnIndexOrThrow("balance"))
                val lastTime = c.getLong(c.getColumnIndexOrThrow("last_time"))
                balances.add(CustomerBalance(customer, bal, lastTime))
            }
        }
        balances
    }

    suspend fun searchCustomers(query: String, limit: Int = 50): List<String> = withContext(Dispatchers.IO) {
        val names = mutableListOf<String>()
        val db = dbHelper.readableDatabase
        val q = query.trim()
        
        val sql = if (q.isEmpty()) {
            """
            SELECT customer_name FROM (
                SELECT ${StoreBookDbHelper.KEY_SALE_CUSTOMER} AS customer_name, MAX(${StoreBookDbHelper.KEY_TIMESTAMP}) as ts 
                FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_SALE_CUSTOMER} IS NOT NULL AND ${StoreBookDbHelper.KEY_SALE_CUSTOMER} != '' GROUP BY 1
                UNION
                SELECT ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} AS customer_name, MAX(${StoreBookDbHelper.KEY_TIMESTAMP}) as ts 
                FROM ${StoreBookDbHelper.TABLE_UDHAAR} WHERE ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} IS NOT NULL AND ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} != '' GROUP BY 1
            )
            GROUP BY customer_name
            ORDER BY MAX(ts) DESC
            LIMIT ?
            """.trimIndent()
        } else {
            """
            SELECT customer_name FROM (
                SELECT ${StoreBookDbHelper.KEY_SALE_CUSTOMER} AS customer_name, MAX(${StoreBookDbHelper.KEY_TIMESTAMP}) as ts 
                FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_SALE_CUSTOMER} LIKE ? GROUP BY 1
                UNION
                SELECT ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} AS customer_name, MAX(${StoreBookDbHelper.KEY_TIMESTAMP}) as ts 
                FROM ${StoreBookDbHelper.TABLE_UDHAAR} WHERE ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} LIKE ? GROUP BY 1
            )
            GROUP BY customer_name
            ORDER BY MAX(ts) DESC
            LIMIT ?
            """.trimIndent()
        }
        
        val cursor = if (q.isEmpty()) {
            db.rawQuery(sql, arrayOf(limit.toString()))
        } else {
            val likeArg = "%${q}%"
            db.rawQuery(sql, arrayOf(likeArg, likeArg, limit.toString()))
        }
        
        cursor.use { c ->
            while (c.moveToNext()) {
                names.add(c.getString(0))
            }
        }
        names
    }

    suspend fun getCustomerLedger(customerName: String): List<UdhaarEntry> = withContext(Dispatchers.IO) {
        val entries = mutableListOf<UdhaarEntry>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_UDHAAR} WHERE ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} = ? ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} ASC",
            arrayOf(customerName)
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                entries.add(
                    UdhaarEntry(
                        id = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ID)),
                        customerName = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_UDHAAR_CUSTOMER)),
                        amount = c.getDouble(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_UDHAAR_AMOUNT)),
                        type = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_UDHAAR_TYPE)),
                        timestamp = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_TIMESTAMP)),
                        notes = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_NOTES))
                    )
                )
            }
        }
        entries
    }

    // --- Expense Operations ---

    suspend fun insertExpense(entry: ExpenseEntry): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(StoreBookDbHelper.KEY_EXPENSE_TYPE, entry.type)
            put(StoreBookDbHelper.KEY_EXPENSE_DESC, entry.description)
            put(StoreBookDbHelper.KEY_EXPENSE_AMOUNT, entry.amount)
            put(StoreBookDbHelper.KEY_TIMESTAMP, entry.timestamp)
            put(StoreBookDbHelper.KEY_EXPENSE_SUPPLIER, entry.supplierName)
            put(StoreBookDbHelper.KEY_EXPENSE_PHONE, entry.supplierPhone)
        }
        db.insert(StoreBookDbHelper.TABLE_EXPENSES, null, values)
    }

    suspend fun getExpenses(): List<ExpenseEntry> = withContext(Dispatchers.IO) {
        val expensesList = mutableListOf<ExpenseEntry>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${StoreBookDbHelper.TABLE_EXPENSES} ORDER BY ${StoreBookDbHelper.KEY_TIMESTAMP} DESC",
            null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                expensesList.add(
                    ExpenseEntry(
                        id = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ID)),
                        type = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_EXPENSE_TYPE)),
                        description = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_EXPENSE_DESC)),
                        amount = c.getDouble(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_EXPENSE_AMOUNT)),
                        timestamp = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_TIMESTAMP)),
                        supplierName = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_EXPENSE_SUPPLIER)),
                        supplierPhone = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_EXPENSE_PHONE))
                    )
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
        supplierPhone: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Fetch item
            val itemCursor = db.rawQuery(
                "SELECT * FROM ${StoreBookDbHelper.TABLE_ITEMS} WHERE ${StoreBookDbHelper.KEY_ID} = ?",
                arrayOf(itemId.toString())
            )
            if (!itemCursor.moveToFirst()) {
                itemCursor.close()
                return@withContext false
            }
            val currentItem = cursorToItem(itemCursor)
            itemCursor.close()

            // 2. Calculate new quantity and buy price (update buy price if changed)
            val newQty = currentItem.quantity + quantityToAdd
            val itemValues = ContentValues().apply {
                put(StoreBookDbHelper.KEY_ITEM_QTY, newQty)
                put(StoreBookDbHelper.KEY_ITEM_BUY_PRICE, costPrice) // update buy price to latest cost
            }
            db.update(
                StoreBookDbHelper.TABLE_ITEMS,
                itemValues,
                "${StoreBookDbHelper.KEY_ID} = ?",
                arrayOf(itemId.toString())
            )

            // 3. Log purchase in expenses table
            val expenseValues = ContentValues().apply {
                put(StoreBookDbHelper.KEY_EXPENSE_TYPE, "RESTOCK")
                put(StoreBookDbHelper.KEY_EXPENSE_DESC, "Restocked ${currentItem.name} (${quantityToAdd} ${currentItem.unit})")
                put(StoreBookDbHelper.KEY_EXPENSE_AMOUNT, costPrice * quantityToAdd)
                put(StoreBookDbHelper.KEY_TIMESTAMP, System.currentTimeMillis())
                put(StoreBookDbHelper.KEY_EXPENSE_SUPPLIER, supplierName)
                put(StoreBookDbHelper.KEY_EXPENSE_PHONE, supplierPhone)
            }
            db.insert(StoreBookDbHelper.TABLE_EXPENSES, null, expenseValues)

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    // --- Utility Methods ---

    private fun cursorToItem(c: Cursor): Item {
        return Item(
            id = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ID)),
            name = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_NAME)),
            quantity = c.getDouble(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_QTY)),
            unit = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_UNIT)),
            buyPrice = c.getDouble(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_BUY_PRICE)),
            sellPrice = c.getDouble(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_SELL_PRICE)),
            lowStockThreshold = c.getDouble(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_THRESHOLD)),
            category = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_CATEGORY)),
            photoPath = c.getString(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_PHOTO)),
            isDeleted = c.getInt(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_IS_DELETED)),
            deletedTimestamp = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ITEM_DELETED_TIME))
        )
    }

    private fun String.formatName(): String {
        return this.trim().split(Regex("\\s+")).joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    suspend fun standardizeCustomerNames() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.rawQuery("SELECT ${StoreBookDbHelper.KEY_ID}, ${StoreBookDbHelper.KEY_UDHAAR_CUSTOMER} FROM ${StoreBookDbHelper.TABLE_UDHAAR}", null)
            cursor.use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(0)
                    val oldName = c.getString(1) ?: continue
                    val newName = oldName.formatName()
                    if (oldName != newName) {
                        val values = ContentValues().apply { put(StoreBookDbHelper.KEY_UDHAAR_CUSTOMER, newName) }
                        db.update(StoreBookDbHelper.TABLE_UDHAAR, values, "${StoreBookDbHelper.KEY_ID} = ?", arrayOf(id.toString()))
                    }
                }
            }

            val cursor2 = db.rawQuery("SELECT ${StoreBookDbHelper.KEY_ID}, ${StoreBookDbHelper.KEY_SALE_CUSTOMER} FROM ${StoreBookDbHelper.TABLE_SALES} WHERE ${StoreBookDbHelper.KEY_SALE_CUSTOMER} IS NOT NULL", null)
            cursor2.use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(0)
                    val oldName = c.getString(1) ?: continue
                    val newName = oldName.formatName()
                    if (oldName != newName) {
                        val values = ContentValues().apply { put(StoreBookDbHelper.KEY_SALE_CUSTOMER, newName) }
                        db.update(StoreBookDbHelper.TABLE_SALES, values, "${StoreBookDbHelper.KEY_ID} = ?", arrayOf(id.toString()))
                    }
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }
}
