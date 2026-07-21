package com.storebook.inventoryapp.shared.test

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase

/**
 * e31-s2: In-memory JdbcSqliteDriver for real SQLDelight tests on JVM.
 *
 * Creates a fresh database with full schema, runs all CREATE INDEX / DEFAULT data from queries,
 * returns fully-initialised StoreBookDatabase ready for assertions.
 */
object DatabaseTestHelper {

    private val SCHEMA_SQL = javaClass.getResource("/sqldelight/com/storebook/inventoryapp/shared/data/local/StoreBook.sq")
        ?.readText()
        .orEmpty()

    /** Build a brand-new in-memory database with every table + index from StoreBook.sq. */
    fun createDatabase(): Pair<StoreBookDatabase, SqlDriver> {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        // Extract CREATE TABLE / CREATE INDEX statements and execute them raw so schema exists
        createSchema(driver)

        val database = StoreBookDatabase(driver)

        // Seed default sync_state row so later queries (getSyncState) don't return null
        driver.execute(
            identifier = null,
            sql = "INSERT OR REPLACE INTO sync_state(id, status) VALUES(1, 'IDLE')",
            parameters = 0
        )

        return Pair(database, driver)
    }

    /** Run all CREATE / INSERT from the .sq file as raw SQL (schema bootstrap). */
    private fun createSchema(driver: SqlDriver) {
        val lines = SCHEMA_SQL.split("\n")
        var buffer = ""
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("--")) continue

            buffer += "$line\n"
            if (trimmed.endsWith(";")) {
                try {
                    driver.execute(identifier = null, sql = buffer.trim(), parameters = 0)
                } catch (_: Exception) {
                    // Some SQLDelight queries have named params that raw execution can't bind — skip gracefully
                }
                buffer = ""
            }
        }
    }

    fun dropDatabase(driver: SqlDriver) {
        (driver as? JdbcSqliteDriver)?.close()
    }
}
