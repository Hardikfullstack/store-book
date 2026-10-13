package com.storebook.inventoryapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storebook.inventoryapp.data.sync.SyncManager
import com.storebook.inventoryapp.utils.SecurityUtils

class DailyBatchWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        private const val TAG = "DailyBatchWorker"
    }

    override suspend fun doWork(): Result {
        val today = SyncManager.getTodayDateString()
        val prefs = SecurityUtils.getEncryptedPrefs(applicationContext)
        val lastRun = SyncManager.getLastBatchRunDate(applicationContext)

        // Rule: Once processed for today, do NOT run again
        if (lastRun == today) {
            Log.d(TAG, "Daily batch has already been processed today ($today). Skipping.")
            return Result.success()
        }

        // Rule: If offline, keep pending and retry
        if (!SyncManager.isOnline(applicationContext)) {
            Log.w(TAG, "Device is offline at scheduled batch time. Marking batch as pending.")
            SyncManager.setBatchPending(applicationContext, true)
            return Result.retry()
        }

        Log.d(TAG, "Starting daily batch sync for $today...")
        return try {
            val allSucceeded = SyncManager.executeDailyBatch(applicationContext)
            if (allSucceeded) {
                Log.d(TAG, "Daily batch sync completed successfully for $today.")
                prefs
                    .edit()
                    .putString(SyncManager.PREF_LAST_BATCH_RUN_DATE, today)
                    .putBoolean(SyncManager.PREF_IS_BATCH_PENDING, false)
                    .apply()
                Result.success()
            } else {
                Log.w(TAG, "Daily batch sync had partial failures. Keeping batch pending.")
                SyncManager.setBatchPending(applicationContext, true)
                Result.retry()
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Daily batch worker exception: ${e.message}", e)
            SyncManager.setBatchPending(applicationContext, true)
            Result.retry()
        }
    }
}
