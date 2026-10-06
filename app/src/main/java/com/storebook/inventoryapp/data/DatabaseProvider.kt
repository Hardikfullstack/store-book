package com.storebook.inventoryapp.data

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase

object DatabaseProvider {
    @Volatile
    private var currentStoreId: String? = null

    @Volatile
    private var databaseInstance: StoreBookDatabase? = null

    @Volatile
    private var driverInstance: AndroidSqliteDriver? = null

    @Synchronized
    fun getDatabase(
        context: Context,
        storeId: String,
    ): StoreBookDatabase {
        val existing = databaseInstance
        if (existing != null && currentStoreId == storeId) {
            return existing
        }

        try {
            driverInstance?.close()
        } catch (_: Exception) {
            // Best effort close
        }

        currentStoreId = storeId
        val driver =
            AndroidSqliteDriver(
                schema = StoreBookDatabase.Schema,
                context = context.applicationContext,
                name = "storebook_$storeId.db",
                callback = DbMigrationCallback,
            )
        driverInstance = driver
        val db = StoreBookDatabase(driver)
        databaseInstance = db
        return db
    }
}
