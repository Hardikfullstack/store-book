package com.storebook.inventoryapp.shared.test

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase

/**
 * In-memory JdbcSqliteDriver for real SQLDelight tests on JVM.
 * Uses SQLDelight's generated Schema object for proper DDL bootstrap.
 */
object DatabaseTestHelper {

    /** Build a brand-new in-memory database with every table + index from StoreBook.sq. */
    fun createDatabase(): Pair<StoreBookDatabase, SqlDriver> {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        // Use SQLDelight's generated Schema to create all tables and indexes
        StoreBookDatabase.Schema.create(driver)

        val database = StoreBookDatabase(driver)

        // Seed default sync_state row so later queries (getSyncState) don't return null
        driver.execute(
            identifier = null,
            sql = "INSERT OR REPLACE INTO sync_state(id, status) VALUES(1, 'IDLE')",
            parameters = 0
        )

        return Pair(database, driver)
    }

    fun dropDatabase(driver: SqlDriver) {
        (driver as? JdbcSqliteDriver)?.close()
    }
}
