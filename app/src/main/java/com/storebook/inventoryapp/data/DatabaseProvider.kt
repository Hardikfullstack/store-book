package com.storebook.inventoryapp.data

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import java.util.concurrent.ConcurrentHashMap

object DatabaseProvider {
    private val databases = ConcurrentHashMap<String, StoreBookDatabase>()
    private val drivers = ConcurrentHashMap<String, AndroidSqliteDriver>()

    @Synchronized
    fun getDatabase(
        context: Context,
        storeId: String,
    ): StoreBookDatabase =
        databases.getOrPut(storeId) {
            val driver =
                AndroidSqliteDriver(
                    schema = StoreBookDatabase.Schema,
                    context = context.applicationContext,
                    name = "storebook_$storeId.db",
                    callback = DbMigrationCallback,
                )
            drivers[storeId] = driver
            StoreBookDatabase(driver)
        }

    @Synchronized
    fun closeDatabase(storeId: String) {
        try {
            drivers.remove(storeId)?.close()
        } catch (_: Exception) {
            // Best effort close
        }
        databases.remove(storeId)
    }

    @Synchronized
    fun closeAll() {
        drivers.values.forEach {
            try {
                it.close()
            } catch (_: Exception) {
                // Best effort close
            }
        }
        drivers.clear()
        databases.clear()
    }
}
