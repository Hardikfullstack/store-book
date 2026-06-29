package com.storebook.inventoryapp.shared.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase

actual class DatabaseDriverFactory(private val storeId: String) {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(StoreBookDatabase.Schema, "storebook_$storeId.db")
    }
}
