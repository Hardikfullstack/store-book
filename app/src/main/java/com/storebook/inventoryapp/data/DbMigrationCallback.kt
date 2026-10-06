package com.storebook.inventoryapp.data

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase

/**
 * DbMigrationCallback — runs ALTER TABLE ADD COLUMN for missing sale_items fields.
 *
 * sale_items.tax_rate and sale_items.hsn_code were added in the .sq schema later but
 * existing on-device databases were created before those columns, so SQLite throws
 * "no such column" at runtime.  This callback safely adds the columns via pragma introspection
 * so it is idempotent: safe-start on both old and new DBs.
 */
object DbMigrationCallback : AndroidSqliteDriver.Callback(StoreBookDatabase.Schema) {
    override fun onConfigure(db: SupportSQLiteDatabase) {
        super.onConfigure(db)
        db.enableWriteAheadLogging()
    }

    override fun onOpen(holdable: SupportSQLiteDatabase) {
        val columns = pragmaTableInfo(holdable, "sale_items")
        if (!columns.contains("tax_rate")) {
            holdable.execSQL("ALTER TABLE sale_items ADD COLUMN tax_rate REAL NOT NULL DEFAULT 0.0")
        }
        if (!columns.contains("hsn_code")) {
            holdable.execSQL("ALTER TABLE sale_items ADD COLUMN hsn_code TEXT")
        }

        holdable.execSQL(
            """
            CREATE TABLE IF NOT EXISTS stock_adjustments (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              item_id INTEGER NOT NULL,
              item_name TEXT NOT NULL,
              reason TEXT NOT NULL,
              delta REAL NOT NULL,
              timestamp INTEGER NOT NULL,
              is_deleted INTEGER NOT NULL DEFAULT 0,
              cloud_id TEXT,
              is_synced INTEGER NOT NULL DEFAULT 0,
              updated_at INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        holdable.execSQL("CREATE INDEX IF NOT EXISTS idx_stock_adjustments_timestamp ON stock_adjustments(timestamp)")
        holdable.execSQL("CREATE INDEX IF NOT EXISTS idx_stock_adjustments_item_id ON stock_adjustments(item_id)")

        val categoryColumns = pragmaTableInfo(holdable, "categories")
        if (categoryColumns.isEmpty()) {
            holdable.execSQL(
                """
                CREATE TABLE IF NOT EXISTS categories (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT NOT NULL,
                  business_type TEXT NOT NULL,
                  store_id TEXT,
                  cloud_id TEXT,
                  is_default INTEGER NOT NULL DEFAULT 0,
                  is_synced INTEGER NOT NULL DEFAULT 0,
                  is_deleted INTEGER NOT NULL DEFAULT 0,
                  updated_at INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            holdable.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_business_type ON categories(business_type)")
            holdable.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_cloud_id ON categories(cloud_id)")
        } else if (!categoryColumns.contains("cloud_id")) {
            holdable.execSQL(
                """
                CREATE TABLE IF NOT EXISTS categories_new (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT NOT NULL,
                  business_type TEXT NOT NULL,
                  store_id TEXT,
                  cloud_id TEXT,
                  is_default INTEGER NOT NULL DEFAULT 0,
                  is_synced INTEGER NOT NULL DEFAULT 0,
                  is_deleted INTEGER NOT NULL DEFAULT 0,
                  updated_at INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            holdable.execSQL(
                """
                INSERT INTO categories_new (name, business_type, store_id, cloud_id, is_default, is_synced, is_deleted, updated_at)
                SELECT name, business_type, store_id, id, 0, is_synced, is_deleted, updated_at FROM categories
                """.trimIndent(),
            )
            holdable.execSQL("DROP TABLE categories")
            holdable.execSQL("ALTER TABLE categories_new RENAME TO categories")
            holdable.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_business_type ON categories(business_type)")
            holdable.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_cloud_id ON categories(cloud_id)")
        } else {
            if (!categoryColumns.contains("is_default")) {
                holdable.execSQL("ALTER TABLE categories ADD COLUMN is_default INTEGER NOT NULL DEFAULT 0")
            }
            holdable.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_business_type ON categories(business_type)")
            holdable.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_cloud_id ON categories(cloud_id)")
        }
    }

    override fun onDowngrade(
        db: SupportSQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        // Ignore downgrade to prevent crash when switching schema versions
    }

    /** Returns the set of column names for a given table via PRAGMA table_info */
    private fun pragmaTableInfo(
        db: SupportSQLiteDatabase,
        tableName: String,
    ): Set<String> {
        val result = mutableSetOf<String>()
        db.query("PRAGMA table_info($tableName)").use { cursor ->
            val nameIdx = cursor.getColumnIndex("name")
            if (nameIdx >= 0) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(nameIdx))
                }
            }
        }
        return result
    }
}
