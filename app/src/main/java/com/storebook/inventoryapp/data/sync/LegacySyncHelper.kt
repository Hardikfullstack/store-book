package com.storebook.inventoryapp.data.sync

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class SyncItem(val id: Long, val name: String, val quantity: Double, val unit: String, val buyPrice: Double, val sellPrice: Double, val lowStockThreshold: Double, val category: String?, val photoPath: String?, val hsnCode: String?, val taxRate: Double, val isDeleted: Boolean, val cloudId: String?, val isSynced: Boolean, val updatedAt: Long)
data class SyncSale(val id: Long, val timestamp: Long, val totalAmount: Double, val discountAmount: Double, val customerName: String?, val customerGstin: String?, val businessGstin: String?, val customerAddress: String?, val businessAddress: String?, val type: String, val notes: String?, val isDeleted: Boolean, val cloudId: String?, val isSynced: Boolean, val updatedAt: Long)
data class SyncSaleItem(val id: Long, val saleId: Long, val itemId: Long, val itemName: String, val quantity: Double, val unit: String, val buyPrice: Double, val sellPrice: Double, val isDeleted: Boolean, val cloudId: String?, val isSynced: Boolean, val updatedAt: Long)
data class SyncUdhaar(val id: Long, val customerName: String, val amount: Double, val type: String, val timestamp: Long, val notes: String?, val isDeleted: Boolean, val cloudId: String?, val isSynced: Boolean, val updatedAt: Long)
data class SyncExpense(val id: Long, val type: String, val description: String, val amount: Double, val timestamp: Long, val supplierName: String?, val supplierPhone: String?, val isDeleted: Boolean, val cloudId: String?, val isSynced: Boolean, val updatedAt: Long)
data class SyncSupplier(val id: Long, val name: String, val phone: String?, val gstin: String?, val address: String?, val isDeleted: Boolean, val cloudId: String?, val isSynced: Boolean, val updatedAt: Long)
data class SyncPurchase(val id: Long, val supplierId: Long, val supplierName: String, val totalAmount: Double, val taxAmount: Double, val type: String, val timestamp: Long, val notes: String?, val isDeleted: Boolean, val cloudId: String?, val isSynced: Boolean, val updatedAt: Long)
data class SyncPurchaseItem(val id: Long, val purchaseId: Long, val itemId: Long, val itemName: String, val quantity: Double, val unit: String, val buyPrice: Double, val isDeleted: Boolean, val cloudId: String?, val isSynced: Boolean, val updatedAt: Long)

class LegacySyncHelper(context: Context, val storeId: String) {
    val dbHelper = object : SQLiteOpenHelper(context, "storebook_${storeId}.db", null, 9) {
        override fun onCreate(db: SQLiteDatabase) {}
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }

    val db get() = dbHelper.writableDatabase

    fun getUnsyncedItems(): List<SyncItem> {
        val list = mutableListOf<SyncItem>()
        db.rawQuery("SELECT * FROM items WHERE is_synced = 0", null).use { c ->
            while (c.moveToNext()) {
                list.add(SyncItem(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    name = c.getString(c.getColumnIndexOrThrow("name")),
                    quantity = c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    unit = c.getString(c.getColumnIndexOrThrow("unit")),
                    buyPrice = c.getDouble(c.getColumnIndexOrThrow("buy_price")),
                    sellPrice = c.getDouble(c.getColumnIndexOrThrow("sell_price")),
                    lowStockThreshold = c.getDouble(c.getColumnIndexOrThrow("low_stock_threshold")),
                    category = c.getString(c.getColumnIndexOrThrow("category")),
                    photoPath = c.getString(c.getColumnIndexOrThrow("photo_path")),
                    hsnCode = c.getString(c.getColumnIndexOrThrow("hsn_code")),
                    taxRate = c.getDouble(c.getColumnIndexOrThrow("tax_rate")),
                    isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
                    cloudId = c.getString(c.getColumnIndexOrThrow("cloud_id")),
                    isSynced = c.getInt(c.getColumnIndexOrThrow("is_synced")) == 1,
                    updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at"))
                ))
            }
        }
        return list
    }

    fun markItemSynced(id: Long, cloudId: String) {
        val cv = ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }
        db.update("items", cv, "id=?", arrayOf(id.toString()))
    }

    fun getUnsyncedSales(): List<SyncSale> {
        val list = mutableListOf<SyncSale>()
        db.rawQuery("SELECT * FROM sales WHERE is_synced = 0", null).use { c ->
            while (c.moveToNext()) {
                list.add(SyncSale(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp")),
                    totalAmount = c.getDouble(c.getColumnIndexOrThrow("total_amount")),
                    discountAmount = c.getDouble(c.getColumnIndexOrThrow("discount_amount")),
                    customerName = c.getString(c.getColumnIndexOrThrow("customer_name")),
                    customerGstin = c.getString(c.getColumnIndexOrThrow("customer_gstin")),
                    businessGstin = c.getString(c.getColumnIndexOrThrow("business_gstin")),
                    customerAddress = c.getString(c.getColumnIndexOrThrow("customer_address")),
                    businessAddress = c.getString(c.getColumnIndexOrThrow("business_address")),
                    type = c.getString(c.getColumnIndexOrThrow("type")),
                    notes = c.getString(c.getColumnIndexOrThrow("notes")),
                    isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
                    cloudId = c.getString(c.getColumnIndexOrThrow("cloud_id")),
                    isSynced = c.getInt(c.getColumnIndexOrThrow("is_synced")) == 1,
                    updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at"))
                ))
            }
        }
        return list
    }

    fun markSaleSynced(id: Long, cloudId: String) {
        db.update("sales", ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }, "id=?", arrayOf(id.toString()))
    }

    fun getUnsyncedSaleItems(): List<SyncSaleItem> {
        val list = mutableListOf<SyncSaleItem>()
        db.rawQuery("SELECT * FROM sale_items WHERE is_synced = 0", null).use { c ->
            while (c.moveToNext()) {
                list.add(SyncSaleItem(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    saleId = c.getLong(c.getColumnIndexOrThrow("sale_id")),
                    itemId = c.getLong(c.getColumnIndexOrThrow("item_id")),
                    itemName = c.getString(c.getColumnIndexOrThrow("item_name")),
                    quantity = c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    unit = c.getString(c.getColumnIndexOrThrow("unit")),
                    buyPrice = c.getDouble(c.getColumnIndexOrThrow("buy_price")),
                    sellPrice = c.getDouble(c.getColumnIndexOrThrow("sell_price")),
                    isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
                    cloudId = c.getString(c.getColumnIndexOrThrow("cloud_id")),
                    isSynced = c.getInt(c.getColumnIndexOrThrow("is_synced")) == 1,
                    updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at"))
                ))
            }
        }
        return list
    }

    fun markSaleItemSynced(id: Long, cloudId: String) {
        db.update("sale_items", ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }, "id=?", arrayOf(id.toString()))
    }

    fun getUnsyncedUdhaars(): List<SyncUdhaar> {
        val list = mutableListOf<SyncUdhaar>()
        db.rawQuery("SELECT * FROM udhaar WHERE is_synced = 0", null).use { c ->
            while (c.moveToNext()) {
                list.add(SyncUdhaar(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    customerName = c.getString(c.getColumnIndexOrThrow("customer_name")),
                    amount = c.getDouble(c.getColumnIndexOrThrow("amount")),
                    type = c.getString(c.getColumnIndexOrThrow("type")),
                    timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp")),
                    notes = c.getString(c.getColumnIndexOrThrow("notes")),
                    isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
                    cloudId = c.getString(c.getColumnIndexOrThrow("cloud_id")),
                    isSynced = c.getInt(c.getColumnIndexOrThrow("is_synced")) == 1,
                    updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at"))
                ))
            }
        }
        return list
    }

    fun markUdhaarSynced(id: Long, cloudId: String) {
        db.update("udhaar", ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }, "id=?", arrayOf(id.toString()))
    }

    fun getUnsyncedExpenses(): List<SyncExpense> {
        val list = mutableListOf<SyncExpense>()
        db.rawQuery("SELECT * FROM expenses WHERE is_synced = 0", null).use { c ->
            while (c.moveToNext()) {
                list.add(SyncExpense(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    type = c.getString(c.getColumnIndexOrThrow("type")),
                    description = c.getString(c.getColumnIndexOrThrow("description")),
                    amount = c.getDouble(c.getColumnIndexOrThrow("amount")),
                    timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp")),
                    supplierName = c.getString(c.getColumnIndexOrThrow("supplier_name")),
                    supplierPhone = c.getString(c.getColumnIndexOrThrow("supplier_phone")),
                    isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
                    cloudId = c.getString(c.getColumnIndexOrThrow("cloud_id")),
                    isSynced = c.getInt(c.getColumnIndexOrThrow("is_synced")) == 1,
                    updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at"))
                ))
            }
        }
        return list
    }

    fun markExpenseSynced(id: Long, cloudId: String) {
        db.update("expenses", ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }, "id=?", arrayOf(id.toString()))
    }

    fun getUnsyncedSuppliers(): List<SyncSupplier> {
        val list = mutableListOf<SyncSupplier>()
        db.rawQuery("SELECT * FROM suppliers WHERE is_synced = 0", null).use { c ->
            while (c.moveToNext()) {
                list.add(SyncSupplier(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    name = c.getString(c.getColumnIndexOrThrow("name")),
                    phone = c.getString(c.getColumnIndexOrThrow("phone")),
                    gstin = c.getString(c.getColumnIndexOrThrow("gstin")),
                    address = c.getString(c.getColumnIndexOrThrow("address")),
                    isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
                    cloudId = c.getString(c.getColumnIndexOrThrow("cloud_id")),
                    isSynced = c.getInt(c.getColumnIndexOrThrow("is_synced")) == 1,
                    updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at"))
                ))
            }
        }
        return list
    }

    fun markSupplierSynced(id: Long, cloudId: String) {
        db.update("suppliers", ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }, "id=?", arrayOf(id.toString()))
    }

    fun getUnsyncedPurchases(): List<SyncPurchase> {
        val list = mutableListOf<SyncPurchase>()
        db.rawQuery("SELECT * FROM purchases WHERE is_synced = 0", null).use { c ->
            while (c.moveToNext()) {
                list.add(SyncPurchase(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    supplierId = c.getLong(c.getColumnIndexOrThrow("supplier_id")),
                    supplierName = c.getString(c.getColumnIndexOrThrow("supplier_name")),
                    totalAmount = c.getDouble(c.getColumnIndexOrThrow("total_amount")),
                    taxAmount = c.getDouble(c.getColumnIndexOrThrow("tax_amount")),
                    type = c.getString(c.getColumnIndexOrThrow("type")),
                    timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp")),
                    notes = c.getString(c.getColumnIndexOrThrow("notes")),
                    isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
                    cloudId = c.getString(c.getColumnIndexOrThrow("cloud_id")),
                    isSynced = c.getInt(c.getColumnIndexOrThrow("is_synced")) == 1,
                    updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at"))
                ))
            }
        }
        return list
    }

    fun markPurchaseSynced(id: Long, cloudId: String) {
        db.update("purchases", ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }, "id=?", arrayOf(id.toString()))
    }

    fun getUnsyncedPurchaseItems(): List<SyncPurchaseItem> {
        val list = mutableListOf<SyncPurchaseItem>()
        db.rawQuery("SELECT * FROM purchase_items WHERE is_synced = 0", null).use { c ->
            while (c.moveToNext()) {
                list.add(SyncPurchaseItem(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    purchaseId = c.getLong(c.getColumnIndexOrThrow("purchase_id")),
                    itemId = c.getLong(c.getColumnIndexOrThrow("item_id")),
                    itemName = c.getString(c.getColumnIndexOrThrow("item_name")),
                    quantity = c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    unit = c.getString(c.getColumnIndexOrThrow("unit")),
                    buyPrice = c.getDouble(c.getColumnIndexOrThrow("buy_price")),
                    isDeleted = c.getInt(c.getColumnIndexOrThrow("is_deleted")) == 1,
                    cloudId = c.getString(c.getColumnIndexOrThrow("cloud_id")),
                    isSynced = c.getInt(c.getColumnIndexOrThrow("is_synced")) == 1,
                    updatedAt = c.getLong(c.getColumnIndexOrThrow("updated_at"))
                ))
            }
        }
        return list
    }

    fun markPurchaseItemSynced(id: Long, cloudId: String) {
        db.update("purchase_items", ContentValues().apply { put("is_synced", 1); put("cloud_id", cloudId) }, "id=?", arrayOf(id.toString()))
    }
}
