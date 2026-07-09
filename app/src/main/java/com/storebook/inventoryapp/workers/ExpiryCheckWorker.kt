package com.storebook.inventoryapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storebook.inventoryapp.R
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.repository.BatchRepository

class ExpiryCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(applicationContext)
        val activeStoreId = prefs.getString("active_store_id", "default") ?: "default"
        val callback = object : app.cash.sqldelight.driver.android.AndroidSqliteDriver.Callback(StoreBookDatabase.Schema) {
            override fun onDowngrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        }
        val driver = AndroidSqliteDriver(StoreBookDatabase.Schema, applicationContext, "storebook_${activeStoreId}.db", callback = callback)
        val database = StoreBookDatabase(driver)
        val repository = BatchRepository(database)

        val threshold = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        val nearExpiryBatches = repository.getNearExpiryBatches(threshold)

        if (nearExpiryBatches.isNotEmpty()) {
            showNotification(nearExpiryBatches.size)
        }

        return Result.success()
    }

    private fun showNotification(count: Int) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "expiry_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Expiry Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Stock Expiry Alert")
            .setContentText("$count items are nearing expiry. Check inventory batches.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            manager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }
    }
}
