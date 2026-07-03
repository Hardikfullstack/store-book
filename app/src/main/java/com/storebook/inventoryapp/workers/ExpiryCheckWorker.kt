package com.storebook.inventoryapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.data.repository.StoreBookRepository

class ExpiryCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(applicationContext)
        val activeStoreId = prefs.getString("active_store_id", "default") ?: "default"
        val repository = StoreBookRepository(applicationContext, activeStoreId)
        
        val nearExpiryBatches = repository.getNearExpiryBatches(30)
        
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
