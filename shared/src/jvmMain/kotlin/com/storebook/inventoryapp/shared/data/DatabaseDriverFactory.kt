package com.storebook.inventoryapp.shared.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // e31-s2: In-memory JDBC SQLite driver for JVM tests
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        StoreBookDatabase.Schema.create(driver)
        return driver
    }
}
