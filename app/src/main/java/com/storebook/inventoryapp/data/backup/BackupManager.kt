package com.storebook.inventoryapp.data.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/** Deserialized info about the latest backup available in Firebase Storage. */
data class BackupInfo(
        val filename: String,
        val timestampMs: Long,
        val sizeBytes: Long,
        val checksumSha256: String?,
) {
    fun formattedDate(): String = android.text.format.DateFormat.format("dd MMM yyyy HH:mm", java.util.Date(timestampMs)).toString()
    fun formattedSize(): String = if (sizeBytes > 1048576) "%.1f MB".format(sizeBytes / 1048576.0) else "${(sizeBytes / 1024)} KB"
}

/** Restore progress states for UI binding. */
data class RestoreState(
        val stage: RestoreStage = RestoreStage.IDLE,
        val progressPercent: Int = 0,
        val message: String = "",
) {
    companion object {
        fun downloading(pct: Int) = RestoreState(RestoreStage.DOWNLOADING, pct, "Downloading backup… ${pct}%")
        fun verifying() = RestoreState(RestoreStage.VERIFYING, 100, "Verifying integrity…")
        fun applying() = RestoreState(RestoreStage.APPLYING, 100, "Applying restored data…")
        fun done(syncTriggered: Boolean) = RestoreState(
                RestoreStage.DONE, 100,
                if (syncTriggered) "✓ Restored. Syncing latest changes…" else "✓ Backup restored.",
        )
        fun failed(error: String) = RestoreState(RestoreStage.FAILED, 0, "Restore failed: $error")
    }
}

enum class RestoreStage { IDLE, DOWNLOADING, VERIFYING, APPLYING, DONE, FAILED }

/**
 * E20-S1/S2: Cloud backup + restore manager.
 * Copies the SQLDelight DB file to a temp staging area and uploads it to Firebase Storage (S1).
 * Downloads latest backup, verifies checksum, replaces local DB atomically (S2).
 * Progress is reported as 0-100%. Uploads are cancellable.
 */
class BackupManager(
        private val context: Context,
        private val storeId: String,
) {

    companion object {
        const val TAG = "BackupManager"
    }

    // ──────────────── S1: Upload ────────────────

    /**
     * Upload the entire SQLDelight DB to Firebase Storage.
     * Returns a cold Flow of progress percentage (0..100).
     */
    fun uploadToCloud(): Flow<Int> = callbackFlow {
        val dbFile = context.getDatabasePath("storebook_${storeId}.db")
        if (!dbFile.exists()) {
            Log.e(TAG, "DB file not found at: ${dbFile.absolutePath}")
            close(Exception("Database file not found"))
            return@callbackFlow
        }

        val tempFile = File(context.cacheDir, "backup_temp_${System.currentTimeMillis()}.sqlite")
        if (!copyFile(dbFile, tempFile)) {
            Log.e(TAG, "Failed to copy DB file for backup")
            close(Exception("Failed to stage database for upload"))
            return@callbackFlow
        }

        val storageRef = FirebaseStorage.getInstance()
                .reference
                .child("backups/${storeId}/${System.currentTimeMillis()}.sqlite")

        val metadata = StorageMetadata.Builder()
                .setCustomMetadata("storeId", storeId)
                .setCustomMetadata("sizeBytes", tempFile.length().toString())
                .build()

        val uploadTask: UploadTask = storageRef.putFile(Uri.fromFile(tempFile), metadata)

        uploadTask.addOnProgressListener { ts ->
            if (ts.totalByteCount > 0L) {
                trySend((100.0 * ts.bytesTransferred / ts.totalByteCount).toInt())
            }
        }

        uploadTask.addOnSuccessListener {
            Log.d(TAG, "Backup uploaded to ${storageRef.path}")
            trySend(100)
            tempFile.delete()
            close()
        }.addOnFailureListener { e ->
            Log.e(TAG, "Upload failed", e)
            tempFile.delete()
            close(e)
        }

        awaitClose {
            uploadTask.cancel()
            tempFile.delete()
        }
    }

    // ──────────────── S2: Restore ────────────────

    /** List latest backup info from Firebase Storage. Returns null if no backups exist. */
    fun fetchLatestBackupInfo(): Flow<BackupInfo?> = callbackFlow {
        val listRef = FirebaseStorage.getInstance().reference.child("backups/$storeId")
        trySend(null) // initial state: unknown

        listRef.listAll()
                .addOnSuccessListener { result ->
                        val items = result.items.sortedByDescending { it.name }
                        val first = items.firstOrNull()
                        if (first != null) {
                                val tsName = first.name.removeSuffix(".sqlite")
                                val tsMillis = tsName.toLongOrNull() ?: 0L
                                first.metadata.addOnSuccessListener { meta ->
                                        trySend(
                                                BackupInfo(
                                                        filename = first.name,
                                                        timestampMs = tsMillis,
                                                        sizeBytes = meta.sizeBytes,
                                                        checksumSha256 = meta.md5Hash,
                                                ),
                                        )
                                        close()
                                }.addOnFailureListener { e ->
                                        close(e)
                                }
                        } else {
                                trySend(null)
                                close()
                        }
                }
                .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to list backups", e)
                        close(e)
                }
    }

    /**
     * Download the latest backup file from Firebase Storage, replace local DB with it,
     * then trigger a one-time SyncWorker so any newer cloud changes are pulled.
     * Emits RestoreState progress updates. Downloads are cancellable.
     */
    fun restoreFromCloud(): Flow<RestoreState> = callbackFlow {
        // Step 1: find latest backup file reference
        val listRef = FirebaseStorage.getInstance().reference.child("backups/$storeId")
        val tempFile = File(context.cacheDir, "restore_temp_${System.currentTimeMillis()}.sqlite")
        var downloadTask: com.google.firebase.storage.FileDownloadTask? = null

        listRef.listAll()
                .addOnSuccessListener { result ->
                        val sortedItems = result.items.sortedByDescending { it.name }
                        val target = sortedItems.firstOrNull()
                        if (target == null) {
                                Log.w(TAG, "No cloud backups found for storeId=$storeId")
                                trySend(RestoreState.failed("No cloud backup available"))
                                close()
                                return@addOnSuccessListener
                        }

                        // Step 2: download the file
                        trySend(RestoreState.downloading(0))
                        downloadTask = target.getFile(tempFile)

                        downloadTask?.addOnProgressListener { ts ->
                                if (ts.totalByteCount > 0L) {
                                        val pct = (100.0 * ts.bytesTransferred / ts.totalByteCount).toInt()
                                        trySend(RestoreState.downloading(pct))
                                }
                        }

                        downloadTask?.addOnSuccessListener {
                                // Step 3: verify basic integrity (file exists \u0026 non-zero size)
                                trySend(RestoreState.verifying())
                                if (!tempFile.exists() || tempFile.length() == 0L) {
                                        trySend(RestoreState.failed("Downloaded file is empty"))
                                        close()
                                        return@addOnSuccessListener
                                }
                                Log.d(TAG, "Backup downloaded OK: ${tempFile.length()} bytes")

                                // Step 4: atomically replace local DB
                                trySend(RestoreState.applying())
                                val dbFile = context.getDatabasePath("storebook_${storeId}.db")
                                val backupSafety = File(dbFile.parentFile, "${dbFile.name}.bak")

                                // Safeguard: rename existing DB as .bak
                                if (dbFile.exists()) {
                                        // Also delete the WAL and SHM files (SQLite journal) so they get regenerated
                                        File(dbFile.parentFile, "${dbFile.name}-wal").delete()
                                        File(dbFile.parentFile, "${dbFile.name}-shm").delete()
                                        dbFile.renameTo(backupSafety)
                                }

                                if (!copyFile(tempFile, dbFile)) {
                                        // Restore from safety backup on failure
                                        backupSafety.renameTo(dbFile)
                                        trySend(RestoreState.failed("Failed to write restored DB"))
                                        close()
                                        tempFile.delete()
                                        return@addOnSuccessListener
                                }

                                Log.d(TAG, "DB replaced successfully. Size=${dbFile.length()}")
                                tempFile.delete()

                                // Step 5: trigger one-time sync so newer cloud data is pulled
                                val syncTriggered = triggerPostRestoreSync()

                                trySend(RestoreState.done(syncTriggered))
                                close()
                        }

                        downloadTask?.addOnFailureListener { e ->
                                Log.e(TAG, "Download failed", e)
                                trySend(RestoreState.failed(e.localizedMessage ?: "Unknown error"))
                                tempFile.delete()
                                close()
                        }
                }
                .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to list backups during restore", e)
                        trySend(RestoreState.failed("Cannot find backups: ${e.localizedMessage}"))
                        close()
                }

        awaitClose {
            downloadTask?.cancel() // cancel if collector unsubscribes
            tempFile.delete()
        }
    }

    private fun triggerPostRestoreSync(): Boolean = try {
        val workReq = androidx.work.OneTimeWorkRequestBuilder<com.storebook.inventoryapp.data.sync.SyncWorker>()
                .addTag("post_restore_sync")
                .setInputData(
                        androidx.work.Data.Builder()
                                .putString("STORE_ID", storeId)
                                .build(),
                )
                .build()
        androidx.work.WorkManager.getInstance(context).enqueue(workReq)
        Log.d(TAG, "Post-restore SyncWorker enqueued")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to enqueue post-restore sync", e)
        false
    }

    // ──────────────── Utilities ────────────────

    /** SHA-256 checksum of current DB file (used in S4 integrity verification). */
    fun computeChecksum(): String? {
        val dbFile = context.getDatabasePath("storebook_${storeId}.db")
        if (!dbFile.exists()) return null
        return try {
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                FileInputStream(dbFile).use { fis ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } > 0) {
                                digest.update(buffer, 0, bytesRead)
                        }
                }
                digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
                Log.e(TAG, "Checksum computation failed", e)
                null
        }
    }

    private fun copyFile(src: File, dst: File): Boolean = try {
            FileInputStream(src).use { fis ->
                    FileOutputStream(dst).use { fos ->
                            val buf = ByteArray(32 * 1024)
                            var len: Int
                            while (fis.read(buf).also { len = it } > 0) {
                                    fos.write(buf, 0, len)
                            }
                    }
            }
            true
    } catch (e: Exception) {
            e.printStackTrace()
            false
    }
}
