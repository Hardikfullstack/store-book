package com.storebook.inventoryapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log

class StoreBookSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i("StoreBookSyncWorker", "Starting background sync...")
        val syncManager = FirestoreSyncManager(applicationContext)
        
        return try {
            syncManager.syncAllData()
            Log.i("StoreBookSyncWorker", "Background sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("StoreBookSyncWorker", "Background sync failed: ${e.message}", e)
            Result.retry()
        }
    }
}
