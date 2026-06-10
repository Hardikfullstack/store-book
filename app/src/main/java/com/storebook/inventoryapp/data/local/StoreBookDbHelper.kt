package com.storebook.inventoryapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StoreBookDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "storebook.db"
        const val DATABASE_VERSION = 2

        // Table Names
        const val TABLE_ITEMS = "items"
        const val TABLE_SALES = "sales"
        const val TABLE_SALE_ITEMS = "sale_items"
        const val TABLE_UDHAAR = "udhaar"
        const val TABLE_EXPENSES = "expenses"

        // Common Column Names
        const val KEY_ID = "id"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_NOTES = "notes"

        // Items Table Columns
        const val KEY_ITEM_NAME = "name"
        const val KEY_ITEM_QTY = "quantity"
        const val KEY_ITEM_UNIT = "unit"
        const val KEY_ITEM_BUY_PRICE = "buy_price"
        const val KEY_ITEM_SELL_PRICE = "sell_price"
        const val KEY_ITEM_THRESHOLD = "low_stock_threshold"
        const val KEY_ITEM_CATEGORY = "category"
        const val KEY_ITEM_PHOTO = "photo_path"
        const val KEY_ITEM_IS_DELETED = "is_deleted"
        const val KEY_ITEM_DELETED_TIME = "deleted_timestamp"

        // Sales Table Columns
        const val KEY_SALE_TOTAL = "total_amount"
        const val KEY_SALE_DISCOUNT = "discount_amount"
        const val KEY_SALE_CUSTOMER = "customer_name"

        // Sale Items Table Columns
        const val KEY_SI_SALE_ID = "sale_id"
        const val KEY_SI_ITEM_ID = "item_id"
        const val KEY_SI_ITEM_NAME = "item_name"
        const val KEY_SI_UNIT = "unit"
        const val KEY_SI_QTY = "quantity"
        const val KEY_SI_SELL_PRICE = "sell_price"
        const val KEY_SI_BUY_PRICE = "buy_price"

        // Udhaar Table Columns
        const val KEY_UDHAAR_CUSTOMER = "customer_name"
        const val KEY_UDHAAR_AMOUNT = "amount"
        const val KEY_UDHAAR_TYPE = "type" // 'CREDIT' or 'PAYMENT'

        // Expenses Table Columns
        const val KEY_EXPENSE_TYPE = "type" // 'RESTOCK' or 'OVERHEAD'
        const val KEY_EXPENSE_DESC = "description"
        const val KEY_EXPENSE_AMOUNT = "amount"
        const val KEY_EXPENSE_SUPPLIER = "supplier_name"
        const val KEY_EXPENSE_PHONE = "supplier_phone"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createItemsTable = ("CREATE TABLE " + TABLE_ITEMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ITEM_NAME + " TEXT NOT NULL UNIQUE,"
                + KEY_ITEM_QTY + " REAL NOT NULL DEFAULT 0.0,"
                + KEY_ITEM_UNIT + " TEXT NOT NULL,"
                + KEY_ITEM_BUY_PRICE + " REAL NOT NULL DEFAULT 0.0,"
                + KEY_ITEM_SELL_PRICE + " REAL NOT NULL DEFAULT 0.0,"
                + KEY_ITEM_THRESHOLD + " REAL NOT NULL DEFAULT 0.0,"
                + KEY_ITEM_CATEGORY + " TEXT NOT NULL,"
                + KEY_ITEM_PHOTO + " TEXT,"
                + KEY_ITEM_IS_DELETED + " INTEGER NOT NULL DEFAULT 0,"
                + KEY_ITEM_DELETED_TIME + " INTEGER DEFAULT 0" + ")")

        val createSalesTable = ("CREATE TABLE " + TABLE_SALES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TIMESTAMP + " INTEGER NOT NULL,"
                + KEY_SALE_TOTAL + " REAL NOT NULL,"
                + KEY_SALE_DISCOUNT + " REAL NOT NULL DEFAULT 0.0,"
                + KEY_SALE_CUSTOMER + " TEXT,"
                + KEY_NOTES + " TEXT" + ")")

        val createSaleItemsTable = ("CREATE TABLE " + TABLE_SALE_ITEMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SI_SALE_ID + " INTEGER NOT NULL,"
                + KEY_SI_ITEM_ID + " INTEGER NOT NULL,"
                + KEY_SI_ITEM_NAME + " TEXT NOT NULL,"
                + KEY_SI_UNIT + " TEXT NOT NULL,"
                + KEY_SI_QTY + " REAL NOT NULL,"
                + KEY_SI_SELL_PRICE + " REAL NOT NULL,"
                + KEY_SI_BUY_PRICE + " REAL NOT NULL" + ")")

        val createUdhaarTable = ("CREATE TABLE " + TABLE_UDHAAR + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_UDHAAR_CUSTOMER + " TEXT NOT NULL,"
                + KEY_UDHAAR_AMOUNT + " REAL NOT NULL,"
                + KEY_UDHAAR_TYPE + " TEXT NOT NULL," // 'CREDIT' or 'PAYMENT'
                + KEY_TIMESTAMP + " INTEGER NOT NULL,"
                + KEY_NOTES + " TEXT" + ")")

        val createExpensesTable = ("CREATE TABLE " + TABLE_EXPENSES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_EXPENSE_TYPE + " TEXT NOT NULL," // 'RESTOCK' or 'OVERHEAD'
                + KEY_EXPENSE_DESC + " TEXT NOT NULL,"
                + KEY_EXPENSE_AMOUNT + " REAL NOT NULL,"
                + KEY_TIMESTAMP + " INTEGER NOT NULL,"
                + KEY_EXPENSE_SUPPLIER + " TEXT,"
                + KEY_EXPENSE_PHONE + " TEXT" + ")")

        db.execSQL(createItemsTable)
        db.execSQL(createSalesTable)
        db.execSQL(createSaleItemsTable)
        db.execSQL(createUdhaarTable)
        db.execSQL(createExpensesTable)

        // Create indexes for frequently queried columns
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_items_deleted ON ${TABLE_ITEMS}(${KEY_ITEM_IS_DELETED})")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_items_category ON ${TABLE_ITEMS}(${KEY_ITEM_CATEGORY})")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_sale_items_sale_id ON ${TABLE_SALE_ITEMS}(${KEY_SI_SALE_ID})")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_udhaar_customer ON ${TABLE_UDHAAR}(${KEY_UDHAAR_CUSTOMER})")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_sales_timestamp ON ${TABLE_SALES}(${KEY_TIMESTAMP})")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_timestamp ON ${TABLE_EXPENSES}(${KEY_TIMESTAMP})")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALE_ITEMS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UDHAAR)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES)
        onCreate(db)
    }
}
