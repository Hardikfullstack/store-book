package com.storebook.inventoryapp.data.sync

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.storebook.inventoryapp.utils.NetworkMonitor
import com.storebook.inventoryapp.utils.SecurityUtils
import com.storebook.inventoryapp.workers.DailyBatchWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object SyncManager {
    private const val TAG = "SyncManager"

    const val PREF_ENABLE_BATCH_UPLOAD = true
    const val PREF_DAILY_BATCH_HOUR = "daily_batch_hour"
    const val PREF_DAILY_BATCH_MINUTE = "daily_batch_minute"
    const val PREF_LAST_BATCH_RUN_DATE = "last_batch_run_date"
    const val PREF_IS_BATCH_PENDING = "is_batch_pending"

    const val DEFAULT_BATCH_HOUR = 23 // 11:00 PM
    const val DEFAULT_BATCH_MINUTE = 0

    const val WORK_NAME_DAILY_BATCH = "storebook_daily_batch_work"
    const val WORK_NAME_IMMEDIATE_SYNC = "storebook_immediate_sync"

    private val scope = CoroutineScope(Dispatchers.IO)

    fun isBatchUploadEnabled(context: Context? = null): Boolean = PREF_ENABLE_BATCH_UPLOAD

    fun getDailyBatchTime(context: Context): Pair<Int, Int> {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        val hour = prefs.getInt(PREF_DAILY_BATCH_HOUR, DEFAULT_BATCH_HOUR)
        val minute = prefs.getInt(PREF_DAILY_BATCH_MINUTE, DEFAULT_BATCH_MINUTE)
        return Pair(hour, minute)
    }

    fun setDailyBatchTime(
        context: Context,
        hour: Int,
        minute: Int,
    ) {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        prefs
            .edit()
            .putInt(PREF_DAILY_BATCH_HOUR, hour)
            .putInt(PREF_DAILY_BATCH_MINUTE, minute)
            .apply()
        scheduleDailyBatch(context)
    }

    fun getLastBatchRunDate(context: Context): String? {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        return prefs.getString(PREF_LAST_BATCH_RUN_DATE, null)
    }

    fun isBatchPending(context: Context): Boolean {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        return prefs.getBoolean(PREF_IS_BATCH_PENDING, false)
    }

    fun setBatchPending(
        context: Context,
        pending: Boolean,
    ) {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        prefs.edit().putBoolean(PREF_IS_BATCH_PENDING, pending).apply()
    }

    fun getTodayDateString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun isPastScheduledBatchTime(context: Context): Boolean {
        val (targetHour, targetMinute) = getDailyBatchTime(context)
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        return currentHour > targetHour || (currentHour == targetHour && currentMinute >= targetMinute)
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Called at Application startup in StoreBookApplication.onCreate()
     */
    fun init(application: Application) {
        scheduleDailyBatch(application)
        observeNetworkReconnect(application)
    }

    /**
     * Schedules a recurring 24-hour job at the fixed scheduled daily batch time (e.g. 11:00 PM).
     */
    fun scheduleDailyBatch(context: Context) {
        val (targetHour, targetMinute) = getDailyBatchTime(context)
        val now = Calendar.getInstance()
        val target =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        val initialDelayMs = (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)

        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val workRequest =
            PeriodicWorkRequestBuilder<DailyBatchWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("daily_batch_upload")
                .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            WORK_NAME_DAILY_BATCH,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest,
        )
        Log.d(TAG, "Scheduled daily batch in ${initialDelayMs / 1000 / 60} minutes ($targetHour:${"%02d".format(targetMinute)})")
    }

    /**
     * Called whenever a record is created or updated in local database.
     * Enforces the user rules:
     * - If enable_batch_upload == true: Keep in local DB (is_synced = 0); sync via daily batch.
     * - If enable_batch_upload == false:
     *     - If online: Trigger immediate sync to Live DB. If it fails, stays in local DB for next batch.
     *     - If offline: Stays in local DB (is_synced = 0) for next batch.
     */
    fun onRecordCreatedOrUpdated(
        context: Context,
        storeId: String,
    ) {
        if (isBatchUploadEnabled(context)) {
            Log.d(TAG, "Record saved locally with is_synced=0 (Batch mode active: waiting for daily batch)")
            return
        }

        if (!isOnline(context)) {
            Log.d(TAG, "Record saved locally with is_synced=0 (Offline: queued for scheduled batch)")
            return
        }

        // Real-time mode and online: Trigger immediate sync
        Log.d(TAG, "Real-time mode active & online: Enqueuing immediate sync for store $storeId")
        triggerImmediateSync(context, storeId)
    }

    /**
     * Trigger immediate one-time sync for a specific store.
     */
    fun triggerImmediateSync(
        context: Context,
        storeId: String,
    ) {
        val data = Data.Builder().putString("STORE_ID", storeId).build()
        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .addTag("immediate_sync")
                .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            "${WORK_NAME_IMMEDIATE_SYNC}_$storeId",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    /**
     * Observes network transitions.
     * Only triggers when network transitions from offline to online.
     */
    private fun observeNetworkReconnect(application: Application) {
        scope.launch {
            var wasOnline: Boolean? = null
            NetworkMonitor(application).isOnline.collectLatest { isOnline ->
                if (isOnline && wasOnline == false) {
                    Log.d(TAG, "Network restored from offline! Checking if daily batch is pending...")
                    onNetworkRestored(application)
                }
                wasOnline = isOnline
            }
        }
    }

    /**
     * Executes when network connection becomes available.
     * Enforces the rule:
     * - "The batch must run only once per day, not every time the internet connection becomes available."
     * - "If the scheduled batch time occurs while the device is offline, execute that pending batch once."
     * - "Once the daily batch has been successfully processed, it must not run again until the next scheduled batch time."
     */
    fun onNetworkRestored(context: Context) {
        val today = getTodayDateString()
        val lastRunDate = getLastBatchRunDate(context)

        // Rule: If already run for today, NEVER run again!
        if (lastRunDate == today) {
            Log.d(TAG, "Daily batch has already been processed today ($today). Skipping reconnect trigger.")
            return
        }

        val isPending = isBatchPending(context)
        val pastScheduledTime = isPastScheduledBatchTime(context)

        if (isPending || pastScheduledTime) {
            Log.d(TAG, "Pending daily batch detected (pending=$isPending, pastScheduledTime=$pastScheduledTime). Running once now.")
            triggerPendingDailyBatch(context)
        } else {
            Log.d(TAG, "Not yet time for today's daily batch. Skipping reconnect sync.")
        }
    }

    /**
     * Runs the pending daily batch as a one-time work request.
     */
    fun triggerPendingDailyBatch(context: Context) {
        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            OneTimeWorkRequestBuilder<DailyBatchWorker>()
                .setConstraints(constraints)
                .addTag("pending_daily_batch")
                .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            "pending_daily_batch_execution",
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    /**
     * Executes the daily batch across all user stores.
     * Returns true if all stores synced successfully.
     */
    suspend fun executeDailyBatch(context: Context): Boolean {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        val activeStoreId = prefs.getString("active_store_id", "default") ?: "default"
        val userStoresStr = prefs.getString("user_stores", activeStoreId) ?: activeStoreId
        val stores = userStoresStr.split(",").filter { it.isNotBlank() }.distinct()

        var allSuccessful = true
        for (storeId in stores) {
            try {
                Log.d(TAG, "Daily batch processing store: $storeId")
                SyncWorker.performSync(context, storeId) { _, _ -> }
            } catch (e: Exception) {
                Log.e(TAG, "Daily batch failed for store $storeId: ${e.message}", e)
                allSuccessful = false
            }
        }
        return allSuccessful
    }
}
