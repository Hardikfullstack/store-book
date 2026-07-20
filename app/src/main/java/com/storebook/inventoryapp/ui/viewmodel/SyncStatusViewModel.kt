/*
 * BP-3: Centralized Sync Status Hub
 *
 * Single source of truth for sync state across the entire app.
 * All ViewModels and UI screens observe THIS instead of creating
 * their own NetworkMonitor / sync polling loops.
 *
 * Observes:
 *  - Network connectivity via NetworkMonitor
 *  - Sync progress (PUSHING → PULLING → DONE/FAILED)
 *  - Last full sync timestamp from SyncRepository.getSyncState()
 *
 * Provides:
 *  - retrySync() — enqueue a one-time sync WorkManager job
 *  - StateFlow<UiSyncStatus> — unified data class for UI consumption
 */

package com.storebook.inventoryapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.storebook.inventoryapp.data.sync.SyncWorker
import com.storebook.inventoryapp.shared.domain.repository.SyncRepository
import com.storebook.inventoryapp.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Unified sync status data class.
 * Replaces DashboardViewModel.UiSyncStatus to serve as the single
 * observable for all screens (Dashboard, Navigation drawer, More settings).
 */
data class UiSyncStatus(
    val status: String,              // "IDLE", "PUSHING", "PULLING", "DONE", "FAILED"
    val lastSyncAt: Long,            // timestamp of last full sync epoch (ms)
    val failedCount: Int,            // number of pending failures in failed_sync_queue
    val isOnline: Boolean            // current network connectivity state
) {
    val isSyncing: Boolean
        get() = status in listOf("PUSHING", "PULLING")

    companion object {
        val initial = UiSyncStatus(
            status = "IDLE",
            lastSyncAt = 0L,
            failedCount = 0,
            isOnline = false
        )
    }
}

/**
 * Centralized ViewModel that exposes:
 * - syncState as a StateFlow<UiSyncStatus> (combines network + DB sync progress)
 * - retrySync() to trigger an on-demand full sync via WorkManager
 *
 * Polls SyncRepository.getSyncState() periodically. Listens for
 * NetworkMonitor connectivity changes in real-time.
 */
class SyncStatusViewModel(
    private val context: Context,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    private val _rawDbSyncStatus = MutableStateFlow<UiSyncStatus>(UiSyncStatus.initial)

    /** Unified StateFlow — combines network status + DB-level sync progress. */
    val syncState: StateFlow<UiSyncStatus> =
        NetworkMonitor(context.applicationContext).isOnline
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )
            .let { isOnlineFlow ->
                kotlinx.coroutines.flow.combine(
                    _rawDbSyncStatus.asStateFlow(),
                    isOnlineFlow
                ) { dbStatus, isOnline ->
                    UiSyncStatus(
                        status = if (!isOnline) "FAILED" else dbStatus.status,
                        lastSyncAt = dbStatus.lastSyncAt,
                        failedCount = dbStatus.failedCount,
                        isOnline = isOnline
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiSyncStatus.initial
            )

    // Convenience accessors for screens that prefer named StateFlows over a single data class
    val isOnline: StateFlow<Boolean> = syncState.map { it.isOnline }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    /** Trigger a one-time full sync job (user-tap retry). */
    fun retrySync() {
        val workReq = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag("sync_manual_retry")
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(
                "immediate_sync",
                ExistingWorkPolicy.REPLACE,
                workReq
            )

        // Start listening for sync completion by polling refresh
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000L)
            refreshSyncFromDb()

            // Keep polling for up to 6 cycles (12 seconds total) while syncing is in progress
            var cycleCount = 0
            while (_rawDbSyncStatus.value.isSyncing && cycleCount < 6) {
                kotlinx.coroutines.delay(2000L)
                refreshSyncFromDb()
                cycleCount++
            }
        }
    }

    /** Reload sync state from DB into internal StateFlow. */
    private fun refreshSyncFromDb() {
        viewModelScope.launch {
            val st = try {
                syncRepository.getSyncState()
            } catch (_: Exception) {
                // If the query throws (unlikely), don't crash. Return null-safe defaults.
                null
            }
            val failedCount = try {
                (syncRepository.getPendingFailureCount() ?: 0L).toInt()
            } catch (_: Exception) {
                0
            }

            _rawDbSyncStatus.value = UiSyncStatus(
                status = st?.status ?: "IDLE",
                lastSyncAt = st?.last_full_sync_at ?: 0L,
                failedCount = failedCount,
                isOnline = _rawDbSyncStatus.value.isOnline // keep as-is; merged later
            )
        }
    }

    init {
        // Initial load + periodic refresh every 5 seconds
        refreshSyncFromDb()
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5_000L)
                refreshSyncFromDb()
            }
        }

        // Observe sync state changes from SyncWorker via DB polling — covered above
    }
}

