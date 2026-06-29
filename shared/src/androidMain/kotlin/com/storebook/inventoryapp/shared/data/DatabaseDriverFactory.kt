package com.storebook.inventoryapp.shared.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase

actual class DatabaseDriverFactory(private val context: Context, private val storeId: String) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(StoreBookDatabase.Schema, context, "storebook_$storeId.db")
    }
}
