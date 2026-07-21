package com.storebook.inventoryapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.repository.BatchRepository
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository

class ExpiryCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val prefs =
            com.storebook.inventoryapp.utils.SecurityUtils
                .getEncryptedPrefs(applicationContext)
        val activeStoreId = prefs.getString("active_store_id", "default") ?: "default"
        val callback =
            object : app.cash.sqldelight.driver.android.AndroidSqliteDriver.Callback(StoreBookDatabase.Schema) {
                override fun onDowngrade(
                    db: androidx.sqlite.db.SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int,
                ) {}
            }
        val driver =
            AndroidSqliteDriver(
                StoreBookDatabase.Schema,
                applicationContext,
                "storebook_$activeStoreId.db",
                callback = callback,
            )
        val database = StoreBookDatabase(driver)
        val batchRepo = BatchRepository(database)
        val inventoryRepo = InventoryRepository(database)

        // 1) Expiry check (existing logic)
        val threshold = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        val nearExpiryBatches = batchRepo.getNearExpiryBatches(threshold)

        if (nearExpiryBatches.isNotEmpty()) {
            showExpiryNotification(nearExpiryBatches.size)
        }

        // 2) E02-S2: Low-stock threshold check
        val lowStockItems = inventoryRepo.getLowStockAlertPending()
        if (lowStockItems.isNotEmpty()) {
            showLowStockNotification(lowStockItems)
            // Mark each alerted so we don't spam until restocked above threshold
            for (item in lowStockItems) {
                inventoryRepo.markItemLowStockAlertSent(item.id)
            }
        }

        // 3) Reset flags for items that crossed back above threshold
        inventoryRepo.resetLowStockFlagsAboveThreshold()

        return Result.success()
    }

    // E02-S2: Separate notification channels for expiry vs low-stock
    private fun showExpiryNotification(count: Int) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "expiry_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    channelId,
                    "Expiry Alerts",
                    NotificationManager.IMPORTANCE_HIGH,
                )
            manager.createNotificationChannel(channel)
        }

        val notification =
            NotificationCompat
                .Builder(applicationContext, channelId)
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

    /** E02-S2: Show low-stock alerts with item names listed */
    private fun showLowStockNotification(items: List<com.storebook.inventoryapp.shared.data.local.Items>) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "low_stock_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    channelId,
                    "Low Stock Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            manager.createNotificationChannel(channel)
        }

        // Up to 5 item names + quantities for readability in notification body
        val itemNames =
            items.take(5).joinToString(", ") { item ->
                "${item.name} (${String.format("%.1f", item.quantity)})"
            }
        val contentText = if (items.size > 5) "$itemNames ... (+${items.size - 5} more)" else itemNames

        val notification =
            NotificationCompat
                .Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Low Stock Alert")
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        try {
            val notifId = (System.currentTimeMillis() / 1000).toInt()
            manager.notify(notifId + 5001, notification) // offset ID to avoid collision with expiry notifs
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }
    }
}
