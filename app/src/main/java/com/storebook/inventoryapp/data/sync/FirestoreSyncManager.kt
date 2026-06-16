package com.storebook.inventoryapp.data.sync

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.storebook.inventoryapp.data.local.StoreBookDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class FirestoreSyncManager(
    context: Context,
) {
    private val dbHelper = StoreBookDbHelper(context.applicationContext)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun syncAllData() =
        withContext(Dispatchers.IO) {
            val user = auth.currentUser
            if (user == null) {
                Log.w("SyncManager", "User not logged in. Aborting sync.")
                return@withContext
            }
            val userId = user.uid

            try {
                // Sync Items Table
                syncTable(
                    userId,
                    StoreBookDbHelper.TABLE_ITEMS,
                    listOf(
                        StoreBookDbHelper.KEY_ITEM_NAME,
                        StoreBookDbHelper.KEY_ITEM_QTY,
                        StoreBookDbHelper.KEY_ITEM_UNIT,
                        StoreBookDbHelper.KEY_ITEM_BUY_PRICE,
                        StoreBookDbHelper.KEY_ITEM_SELL_PRICE,
                        StoreBookDbHelper.KEY_ITEM_CATEGORY,
                    ),
                )

                // Sync Sales Table
                syncTable(
                    userId,
                    StoreBookDbHelper.TABLE_SALES,
                    listOf(
                        StoreBookDbHelper.KEY_TIMESTAMP,
                        StoreBookDbHelper.KEY_SALE_TOTAL,
                        StoreBookDbHelper.KEY_SALE_CUSTOMER,
                        StoreBookDbHelper.KEY_NOTES,
                    ),
                )

                // Sync Udhaar Table
                syncTable(
                    userId,
                    StoreBookDbHelper.TABLE_UDHAAR,
                    listOf(
                        StoreBookDbHelper.KEY_UDHAAR_CUSTOMER,
                        StoreBookDbHelper.KEY_UDHAAR_AMOUNT,
                        StoreBookDbHelper.KEY_UDHAAR_TYPE,
                        StoreBookDbHelper.KEY_TIMESTAMP,
                    ),
                )

                // Further implementations can sync SaleItems and Expenses similarly.
                Log.i("SyncManager", "Sync complete for user $userId")
            } catch (e: Exception) {
                Log.e("SyncManager", "Sync failed: ${e.message}", e)
            }
        }

    private suspend fun syncTable(
        userId: String,
        tableName: String,
        columnsToMap: List<String>,
    ) {
        val db = dbHelper.writableDatabase

        // 1. PUSH local unsynced records to Firestore
        val cursor =
            db.rawQuery(
                "SELECT * FROM $tableName WHERE ${StoreBookDbHelper.KEY_IS_SYNCED} = 0",
                null,
            )

        cursor.use { c ->
            while (c.moveToNext()) {
                val localId = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_ID))

                // Get or generate cloud_id
                val cloudIdIndex = c.getColumnIndex(StoreBookDbHelper.KEY_CLOUD_ID)
                var cloudId = if (cloudIdIndex >= 0) c.getString(cloudIdIndex) else null

                if (cloudId == null || cloudId.isBlank()) {
                    cloudId = UUID.randomUUID().toString()
                    // Save new cloudId locally
                    val cv = ContentValues().apply { put(StoreBookDbHelper.KEY_CLOUD_ID, cloudId) }
                    db.update(tableName, cv, "${StoreBookDbHelper.KEY_ID} = ?", arrayOf(localId.toString()))
                }

                // Map row to HashMap
                val dataMap = hashMapOf<String, Any>()
                dataMap["cloud_id"] = cloudId
                dataMap["is_deleted"] = c.getInt(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_IS_DELETED))
                dataMap["updated_at"] = c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_UPDATED_AT))

                for (col in columnsToMap) {
                    val idx = c.getColumnIndex(col)
                    if (idx >= 0) {
                        when (c.getType(idx)) {
                            android.database.Cursor.FIELD_TYPE_INTEGER -> dataMap[col] = c.getLong(idx)
                            android.database.Cursor.FIELD_TYPE_FLOAT -> dataMap[col] = c.getDouble(idx)
                            android.database.Cursor.FIELD_TYPE_STRING -> dataMap[col] = c.getString(idx) ?: ""
                        }
                    }
                }

                // Push to Firestore
                firestore
                    .collection("users")
                    .document(userId)
                    .collection(tableName)
                    .document(cloudId)
                    .set(dataMap, SetOptions.merge())
                    .await()

                // Mark as synced locally
                val cvSync = ContentValues().apply { put(StoreBookDbHelper.KEY_IS_SYNCED, 1) }
                db.update(tableName, cvSync, "${StoreBookDbHelper.KEY_ID} = ?", arrayOf(localId.toString()))
            }
        }

        // 2. PULL newer records from Firestore (Last-Write-Wins)
        // Note: For a robust sync, we should persist a 'lastSyncTimestamp' in SharedPreferences per table
        // and only pull records where 'updated_at' > 'lastSyncTimestamp'.
        val remoteDocs =
            firestore
                .collection("users")
                .document(userId)
                .collection(tableName)
                // .whereGreaterThan("updated_at", lastSyncTimestamp)
                .get()
                .await()

        for (doc in remoteDocs.documents) {
            val remoteCloudId = doc.getString("cloud_id") ?: continue
            val remoteUpdatedAt = doc.getLong("updated_at") ?: 0L

            // Check if exists locally
            val localCursor =
                db.rawQuery(
                    "SELECT ${StoreBookDbHelper.KEY_ID}, ${StoreBookDbHelper.KEY_UPDATED_AT} FROM $tableName WHERE ${StoreBookDbHelper.KEY_CLOUD_ID} = ?",
                    arrayOf(remoteCloudId),
                )

            var shouldInsert = false
            var shouldUpdate = false
            var localIdToUpdate = -1L

            if (localCursor.moveToFirst()) {
                val localUpdatedAt = localCursor.getLong(1)
                if (remoteUpdatedAt > localUpdatedAt) {
                    shouldUpdate = true
                    localIdToUpdate = localCursor.getLong(0)
                }
            } else {
                shouldInsert = true
            }
            localCursor.close()

            if (shouldInsert || shouldUpdate) {
                val cv =
                    ContentValues().apply {
                        put(StoreBookDbHelper.KEY_CLOUD_ID, remoteCloudId)
                        put(StoreBookDbHelper.KEY_UPDATED_AT, remoteUpdatedAt)
                        put(StoreBookDbHelper.KEY_IS_SYNCED, 1)
                        put(StoreBookDbHelper.KEY_IS_DELETED, doc.getLong("is_deleted")?.toInt() ?: 0)

                        for (col in columnsToMap) {
                            val value = doc.get(col)
                            when (value) {
                                is Long -> put(col, value)
                                is Double -> put(col, value)
                                is String -> put(col, value)
                            }
                        }
                    }

                if (shouldInsert) {
                    db.insert(tableName, null, cv)
                } else if (shouldUpdate) {
                    db.update(tableName, cv, "${StoreBookDbHelper.KEY_ID} = ?", arrayOf(localIdToUpdate.toString()))
                }
            }
        }
    }
}
