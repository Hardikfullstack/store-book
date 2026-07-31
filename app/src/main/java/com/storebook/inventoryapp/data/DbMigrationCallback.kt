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
    override fun onOpen(holdable: SupportSQLiteDatabase) {
        val columns = pragmaTableInfo(holdable, "sale_items")
        if (!columns.contains("tax_rate")) {
            holdable.execSQL("ALTER TABLE sale_items ADD COLUMN tax_rate REAL NOT NULL DEFAULT 0.0")
        }
        if (!columns.contains("hsn_code")) {
            holdable.execSQL("ALTER TABLE sale_items ADD COLUMN hsn_code TEXT")
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
