package com.storebook.inventoryapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StoreBookDbHelper(
    context: Context,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "storebook.db"
        const val DATABASE_VERSION = 6

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

        // Sync & Cloud Columns
        const val KEY_CLOUD_ID = "cloud_id"
        const val KEY_IS_SYNCED = "is_synced"
        const val KEY_UPDATED_AT = "updated_at"
        const val KEY_IS_DELETED = "is_deleted"

        // Items Table Columns
        const val KEY_ITEM_NAME = "name"
        const val KEY_ITEM_QTY = "quantity"
        const val KEY_ITEM_UNIT = "unit"
        const val KEY_ITEM_BUY_PRICE = "buy_price"
        const val KEY_ITEM_SELL_PRICE = "sell_price"
        const val KEY_ITEM_THRESHOLD = "low_stock_threshold"
        const val KEY_ITEM_CATEGORY = "category"
        const val KEY_ITEM_PHOTO = "photo_path"
        const val KEY_ITEM_HSN = "hsn_code"
        const val KEY_ITEM_TAX_RATE = "tax_rate"
        const val KEY_ITEM_IS_DELETED = "is_deleted"
        const val KEY_ITEM_DELETED_TIME = "deleted_timestamp"

        // Sales Table Columns
        const val KEY_SALE_TOTAL = "total_amount"
        const val KEY_SALE_DISCOUNT = "discount_amount"
        const val KEY_SALE_CUSTOMER = "customer_name"
        const val KEY_SALE_CUSTOMER_GSTIN = "customer_gstin"
        const val KEY_SALE_BUSINESS_GSTIN = "business_gstin"
        const val KEY_SALE_CUSTOMER_ADDRESS = "customer_address"
        const val KEY_SALE_BUSINESS_ADDRESS = "business_address"

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
        val createItemsTable = (
            "CREATE TABLE " + TABLE_ITEMS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ITEM_NAME + " TEXT NOT NULL UNIQUE," +
                KEY_ITEM_QTY + " REAL NOT NULL DEFAULT 0.0," +
                KEY_ITEM_UNIT + " TEXT NOT NULL," +
                KEY_ITEM_BUY_PRICE + " REAL NOT NULL DEFAULT 0.0," +
                KEY_ITEM_SELL_PRICE + " REAL NOT NULL DEFAULT 0.0," +
                KEY_ITEM_THRESHOLD + " REAL NOT NULL DEFAULT 0.0," +
                KEY_ITEM_CATEGORY + " TEXT NOT NULL," +
                KEY_ITEM_PHOTO + " TEXT," +
                KEY_ITEM_HSN + " TEXT," +
                KEY_ITEM_TAX_RATE + " REAL NOT NULL DEFAULT 0.0," +
                KEY_ITEM_IS_DELETED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_ITEM_DELETED_TIME + " INTEGER DEFAULT 0," +
                KEY_CLOUD_ID + " TEXT," +
                KEY_IS_SYNCED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_UPDATED_AT + " INTEGER NOT NULL DEFAULT 0" + ")"
        )

        val createSalesTable = (
            "CREATE TABLE " + TABLE_SALES + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_TIMESTAMP + " INTEGER NOT NULL," +
                KEY_SALE_TOTAL + " REAL NOT NULL," +
                KEY_SALE_DISCOUNT + " REAL NOT NULL DEFAULT 0.0," +
                KEY_SALE_CUSTOMER + " TEXT," +
                KEY_SALE_CUSTOMER_GSTIN + " TEXT," +
                KEY_SALE_BUSINESS_GSTIN + " TEXT," +
                KEY_SALE_CUSTOMER_ADDRESS + " TEXT," +
                KEY_SALE_BUSINESS_ADDRESS + " TEXT," +
                KEY_NOTES + " TEXT," +
                KEY_IS_DELETED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_CLOUD_ID + " TEXT," +
                KEY_IS_SYNCED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_UPDATED_AT + " INTEGER NOT NULL DEFAULT 0" + ")"
        )

        val createSaleItemsTable = (
            "CREATE TABLE " + TABLE_SALE_ITEMS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_SI_SALE_ID + " INTEGER NOT NULL," +
                KEY_SI_ITEM_ID + " INTEGER NOT NULL," +
                KEY_SI_ITEM_NAME + " TEXT NOT NULL," +
                KEY_SI_UNIT + " TEXT NOT NULL," +
                KEY_SI_QTY + " REAL NOT NULL," +
                KEY_SI_SELL_PRICE + " REAL NOT NULL," +
                KEY_SI_BUY_PRICE + " REAL NOT NULL," +
                KEY_IS_DELETED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_CLOUD_ID + " TEXT," +
                KEY_IS_SYNCED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_UPDATED_AT + " INTEGER NOT NULL DEFAULT 0" + ")"
        )

        val createUdhaarTable = (
            "CREATE TABLE " + TABLE_UDHAAR + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_UDHAAR_CUSTOMER + " TEXT NOT NULL," +
                KEY_UDHAAR_AMOUNT + " REAL NOT NULL," +
                KEY_UDHAAR_TYPE + " TEXT NOT NULL," + // 'CREDIT' or 'PAYMENT'
                KEY_TIMESTAMP + " INTEGER NOT NULL," +
                KEY_NOTES + " TEXT," +
                KEY_IS_DELETED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_CLOUD_ID + " TEXT," +
                KEY_IS_SYNCED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_UPDATED_AT + " INTEGER NOT NULL DEFAULT 0" + ")"
        )

        val createExpensesTable = (
            "CREATE TABLE " + TABLE_EXPENSES + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_EXPENSE_TYPE + " TEXT NOT NULL," + // 'RESTOCK' or 'OVERHEAD'
                KEY_EXPENSE_DESC + " TEXT NOT NULL," +
                KEY_EXPENSE_AMOUNT + " REAL NOT NULL," +
                KEY_TIMESTAMP + " INTEGER NOT NULL," +
                KEY_EXPENSE_SUPPLIER + " TEXT," +
                KEY_EXPENSE_PHONE + " TEXT," +
                KEY_IS_DELETED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_CLOUD_ID + " TEXT," +
                KEY_IS_SYNCED + " INTEGER NOT NULL DEFAULT 0," +
                KEY_UPDATED_AT + " INTEGER NOT NULL DEFAULT 0" + ")"
        )

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

        // Sync indexes
        val tables = listOf(TABLE_ITEMS, TABLE_SALES, TABLE_SALE_ITEMS, TABLE_UDHAAR, TABLE_EXPENSES)
        for (table in tables) {
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_${table}_cloud_id ON $table($KEY_CLOUD_ID) WHERE $KEY_CLOUD_ID IS NOT NULL",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_${table}_is_synced ON $table($KEY_IS_SYNCED)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_${table}_updated_at ON $table($KEY_UPDATED_AT)")
        }
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        if (oldVersion < 3) {
            // Upgrade to version 3: Add sync tracking columns
            val tables = listOf(TABLE_ITEMS, TABLE_SALES, TABLE_SALE_ITEMS, TABLE_UDHAAR, TABLE_EXPENSES)
            for (table in tables) {
                db.execSQL("ALTER TABLE $table ADD COLUMN $KEY_CLOUD_ID TEXT")
                db.execSQL("ALTER TABLE $table ADD COLUMN $KEY_IS_SYNCED INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE $table ADD COLUMN $KEY_UPDATED_AT INTEGER NOT NULL DEFAULT 0")

                // Add is_deleted to tables that don't have it
                if (table != TABLE_ITEMS) {
                    db.execSQL("ALTER TABLE $table ADD COLUMN $KEY_IS_DELETED INTEGER NOT NULL DEFAULT 0")
                }

                // Create indexes for sync columns
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_${table}_cloud_id ON $table($KEY_CLOUD_ID) WHERE $KEY_CLOUD_ID IS NOT NULL",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_${table}_is_synced ON $table($KEY_IS_SYNCED)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_${table}_updated_at ON $table($KEY_UPDATED_AT)")
            }
        }
        if (oldVersion < 4) {
            // Upgrade to version 4: Add HSN and Tax Rate to Items
            db.execSQL("ALTER TABLE $TABLE_ITEMS ADD COLUMN $KEY_ITEM_HSN TEXT")
            db.execSQL("ALTER TABLE $TABLE_ITEMS ADD COLUMN $KEY_ITEM_TAX_RATE REAL NOT NULL DEFAULT 0.0")
        }
        if (oldVersion < 5) {
            // Upgrade to version 5: Add GSTIN columns to Sales
            db.execSQL("ALTER TABLE $TABLE_SALES ADD COLUMN $KEY_SALE_CUSTOMER_GSTIN TEXT")
            db.execSQL("ALTER TABLE $TABLE_SALES ADD COLUMN $KEY_SALE_BUSINESS_GSTIN TEXT")
        }
        if (oldVersion < 6) {
            // Upgrade to version 6: Add Address columns to Sales
            db.execSQL("ALTER TABLE $TABLE_SALES ADD COLUMN $KEY_SALE_CUSTOMER_ADDRESS TEXT")
            db.execSQL("ALTER TABLE $TABLE_SALES ADD COLUMN $KEY_SALE_BUSINESS_ADDRESS TEXT")
        }
    }
}
