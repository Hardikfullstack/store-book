package com.storebook.inventoryapp.data.sync

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.storebook.inventoryapp.data.local.StoreBookDbHelper
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncManager(
        private val context: Context,
) {
    private val prefs =
            context.applicationContext.getSharedPreferences("storebook_prefs", Context.MODE_PRIVATE)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var syncJob = kotlinx.coroutines.SupervisorJob()
    private var syncScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + syncJob)

    suspend fun syncAllData(onProgress: (Int, String) -> Unit = { _, _ -> }) =
            withContext(Dispatchers.IO) {
                val user = auth.currentUser
                if (user == null) {
                    Log.w("SyncManager", "User not logged in. Aborting sync.")
                    return@withContext
                }
                val userId =
                        user.phoneNumber
                                ?: user.uid // Web uses phone number as user id if available

                try {
                    // PHASE 1: Provisioning & Store Mapping
                    onProgress(5, "Authenticating user...")
                    val oldActiveStoreId = prefs.getString("active_store_id", "default")
                    var storeId = prefs.getString("current_store_id", null) ?: oldActiveStoreId

                    if (storeId == "default" || storeId == null) {
                        // Check if user exists in cloud and has a store
                        val userDoc = firestore.collection("users").document(userId).get().await()
                        if (userDoc.exists() && userDoc.getString("storeId") != null) {
                            storeId = userDoc.getString("storeId")
                        } else {
                            // Provision a new SaaS store for the mobile user
                            storeId = UUID.randomUUID().toString()

                            // Create Store
                            val newStoreData =
                                    hashMapOf(
                                            "name" to "My Mobile Store",
                                            "is_active" to true,
                                            "created_at" to System.currentTimeMillis()
                                    )
                            firestore
                                    .collection("stores")
                                    .document(storeId!!)
                                    .set(newStoreData)
                                    .await()

                            // Link User to Store
                            val newUserData =
                                    hashMapOf(
                                            "phone" to (user.phoneNumber ?: ""),
                                            "role" to "admin",
                                            "storeId" to storeId,
                                            "created_at" to System.currentTimeMillis()
                                    )
                            firestore
                                    .collection("users")
                                    .document(userId)
                                    .set(newUserData, SetOptions.merge())
                                    .await()
                        }

                        if (storeId != null && storeId != "default" && oldActiveStoreId == "default"
                        ) {
                            val appContext = context.applicationContext
                            val oldDbFile = appContext.getDatabasePath("storebook_default.db")
                            val newDbFile = appContext.getDatabasePath("storebook_${storeId}.db")
                            if (oldDbFile.exists() && !newDbFile.exists()) {
                                oldDbFile.renameTo(newDbFile)
                                val oldWal = java.io.File(oldDbFile.path + "-wal")
                                if (oldWal.exists())
                                        oldWal.renameTo(java.io.File(newDbFile.path + "-wal"))
                                val oldShm = java.io.File(oldDbFile.path + "-shm")
                                if (oldShm.exists())
                                        oldShm.renameTo(java.io.File(newDbFile.path + "-shm"))
                            }
                        }

                        prefs.edit()
                                .putString("current_store_id", storeId)
                                .putString("active_store_id", storeId)
                                .apply()
                        val stores =
                                prefs.getStringSet("user_stores", setOf("default"))?.toMutableSet()
                                        ?: mutableSetOf()
                        stores.remove("default")
                        stores.add(storeId!!)
                        prefs.edit().putStringSet("user_stores", stores).apply()
                    }

                    if (storeId == null) {
                        throw Exception("Failed to provision or retrieve storeId")
                    }

                    val activeDbHelper = StoreBookDbHelper(context.applicationContext, storeId)

                    // Sync Items Table
                    onProgress(15, "Syncing Inventory Items...")
                    syncTable(
                            activeDbHelper,
                            storeId,
                            StoreBookDbHelper.TABLE_ITEMS,
                            listOf(
                                    StoreBookDbHelper.KEY_ITEM_NAME,
                                    StoreBookDbHelper.KEY_ITEM_QTY,
                                    StoreBookDbHelper.KEY_ITEM_UNIT,
                                    StoreBookDbHelper.KEY_ITEM_BUY_PRICE,
                                    StoreBookDbHelper.KEY_ITEM_SELL_PRICE,
                                    StoreBookDbHelper.KEY_ITEM_CATEGORY,
                                    StoreBookDbHelper.KEY_ITEM_THRESHOLD,
                                    StoreBookDbHelper.KEY_ITEM_PHOTO,
                                    StoreBookDbHelper.KEY_ITEM_HSN,
                                    StoreBookDbHelper.KEY_ITEM_TAX_RATE,
                            ),
                    )

                    onProgress(30, "Syncing Sales Records...")
                    // Sync Sales Table
                    syncTable(
                            activeDbHelper,
                            storeId,
                            StoreBookDbHelper.TABLE_SALES,
                            listOf(
                                    StoreBookDbHelper.KEY_TIMESTAMP,
                                    StoreBookDbHelper.KEY_SALE_TOTAL,
                                    StoreBookDbHelper.KEY_SALE_DISCOUNT,
                                    StoreBookDbHelper.KEY_SALE_CUSTOMER,
                                    StoreBookDbHelper.KEY_SALE_CUSTOMER_GSTIN,
                                    StoreBookDbHelper.KEY_SALE_BUSINESS_GSTIN,
                                    StoreBookDbHelper.KEY_SALE_CUSTOMER_ADDRESS,
                                    StoreBookDbHelper.KEY_SALE_BUSINESS_ADDRESS,
                                    StoreBookDbHelper.KEY_SALE_TYPE,
                                    StoreBookDbHelper.KEY_NOTES,
                            ),
                    )

                    onProgress(45, "Syncing Sale Details...")
                    // Sync Sale Items Table
                    syncTable(
                            activeDbHelper,
                            storeId,
                            StoreBookDbHelper.TABLE_SALE_ITEMS,
                            listOf(
                                    StoreBookDbHelper.KEY_SI_SALE_ID,
                                    StoreBookDbHelper.KEY_SI_ITEM_ID,
                                    StoreBookDbHelper.KEY_SI_ITEM_NAME,
                                    StoreBookDbHelper.KEY_SI_UNIT,
                                    StoreBookDbHelper.KEY_SI_QTY,
                                    StoreBookDbHelper.KEY_SI_SELL_PRICE,
                                    StoreBookDbHelper.KEY_SI_BUY_PRICE,
                            ),
                    )

                    onProgress(60, "Syncing Udhaar/Credit...")
                    // Sync Udhaar Table
                    syncTable(
                            activeDbHelper,
                            storeId,
                            StoreBookDbHelper.TABLE_UDHAAR,
                            listOf(
                                    StoreBookDbHelper.KEY_UDHAAR_CUSTOMER,
                                    StoreBookDbHelper.KEY_UDHAAR_AMOUNT,
                                    StoreBookDbHelper.KEY_UDHAAR_TYPE,
                                    StoreBookDbHelper.KEY_TIMESTAMP,
                                    StoreBookDbHelper.KEY_NOTES,
                            ),
                    )

                    onProgress(70, "Syncing Expenses...")
                    // Sync Expenses Table
                    syncTable(
                            activeDbHelper,
                            storeId,
                            StoreBookDbHelper.TABLE_EXPENSES,
                            listOf(
                                    StoreBookDbHelper.KEY_EXPENSE_TYPE,
                                    StoreBookDbHelper.KEY_EXPENSE_DESC,
                                    StoreBookDbHelper.KEY_EXPENSE_AMOUNT,
                                    StoreBookDbHelper.KEY_TIMESTAMP,
                                    StoreBookDbHelper.KEY_EXPENSE_SUPPLIER,
                                    StoreBookDbHelper.KEY_EXPENSE_PHONE,
                            ),
                    )

                    onProgress(80, "Syncing Suppliers...")
                    // Sync Suppliers Table
                    syncTable(
                            activeDbHelper,
                            storeId,
                            StoreBookDbHelper.TABLE_SUPPLIERS,
                            listOf(
                                    "name",
                                    "phone",
                                    "gstin",
                                    "address",
                            ),
                    )

                    onProgress(90, "Syncing Purchases...")
                    // Sync Purchases Table
                    syncTable(
                            activeDbHelper,
                            storeId,
                            StoreBookDbHelper.TABLE_PURCHASES,
                            listOf(
                                    "supplier_id",
                                    "supplier_name",
                                    "total_amount",
                                    "tax_amount",
                                    "type",
                                    StoreBookDbHelper.KEY_TIMESTAMP,
                                    StoreBookDbHelper.KEY_NOTES,
                            ),
                    )

                    onProgress(95, "Syncing Purchase Details...")
                    // Sync Purchase Items Table
                    syncTable(
                            activeDbHelper,
                            storeId,
                            StoreBookDbHelper.TABLE_PURCHASE_ITEMS,
                            listOf(
                                    "purchase_id",
                                    "item_id",
                                    "item_name",
                                    "quantity",
                                    "unit",
                                    "buy_price",
                            ),
                    )

                    onProgress(100, "Sync Complete!")
                    Log.i("SyncManager", "Sync complete for store $storeId")
                } catch (e: Exception) {
                    Log.e("SyncManager", "Sync failed: ${e.message}", e)
                }
            }

    private suspend fun syncTable(
            dbHelper: StoreBookDbHelper,
            storeId: String,
            tableName: String,
            columnsToMap: List<String>,
    ) {
        val db = dbHelper.writableDatabase

        // 1. Fetch remote records first to resolve conflicts
        val remoteDocs =
                firestore.collection("stores").document(storeId).collection(tableName).get().await()

        // 2. PUSH local unsynced records to Firestore
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
                    db.update(
                            tableName,
                            cv,
                            "${StoreBookDbHelper.KEY_ID} = ?",
                            arrayOf(localId.toString())
                    )
                } else {
                    // Conflict resolution: Check if remote is newer
                    val localUpdatedAt =
                            c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_UPDATED_AT))
                    val remoteDoc =
                            remoteDocs.documents.find {
                                it.getString("cloud_id") == cloudId || it.id == cloudId
                            }
                    if (remoteDoc != null) {
                        val remoteUpdatedAt = remoteDoc.getLong("updated_at") ?: 0L
                        if (remoteUpdatedAt > localUpdatedAt) {
                            // Web/Cloud has a newer version. Do not push. PULL phase will overwrite
                            // local.
                            continue
                        }
                    }
                }

                // Map row to HashMap
                val dataMap = hashMapOf<String, Any>()
                dataMap["cloud_id"] = cloudId
                dataMap["is_deleted"] =
                        c.getInt(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_IS_DELETED))
                dataMap["updated_at"] =
                        c.getLong(c.getColumnIndexOrThrow(StoreBookDbHelper.KEY_UPDATED_AT))

                for (col in columnsToMap) {
                    val idx = c.getColumnIndex(col)
                    if (idx >= 0) {
                        when (c.getType(idx)) {
                            android.database.Cursor.FIELD_TYPE_INTEGER ->
                                    dataMap[col] = c.getLong(idx)
                            android.database.Cursor.FIELD_TYPE_FLOAT ->
                                    dataMap[col] = c.getDouble(idx)
                            android.database.Cursor.FIELD_TYPE_STRING ->
                                    dataMap[col] = c.getString(idx) ?: ""
                        }
                    }
                }

                // Add server timestamp to mitigate local clock drift issues
                dataMap["cloud_updated_at"] =
                        com.google.firebase.firestore.FieldValue.serverTimestamp()

                // Push to Firestore (MULTI-TENANT PATH)
                firestore
                        .collection("stores")
                        .document(storeId)
                        .collection(tableName)
                        .document(cloudId)
                        .set(dataMap, com.google.firebase.firestore.SetOptions.merge())
                        .await()

                // Mark as synced locally
                val cvSync = ContentValues().apply { put(StoreBookDbHelper.KEY_IS_SYNCED, 1) }
                db.update(
                        tableName,
                        cvSync,
                        "${StoreBookDbHelper.KEY_ID} = ?",
                        arrayOf(localId.toString())
                )
            }
        }

        // 3. PULL newer records from Firestore to Local (using already fetched remoteDocs)

        for (doc in remoteDocs.documents) {
            val remoteCloudId = doc.getString("cloud_id") ?: doc.id
            val remoteUpdatedAt = doc.getLong("updated_at") ?: 0L

            // Check if exists locally
            val localCursor =
                    db.rawQuery(
                            "SELECT ${StoreBookDbHelper.KEY_ID}, ${StoreBookDbHelper.KEY_UPDATED_AT}, ${StoreBookDbHelper.KEY_IS_SYNCED} FROM $tableName WHERE ${StoreBookDbHelper.KEY_CLOUD_ID} = ?",
                            arrayOf(remoteCloudId),
                    )

            var shouldInsert = false
            var shouldUpdate = false
            var localIdToUpdate = -1L

            if (localCursor.moveToFirst()) {
                val localUpdatedAt = localCursor.getLong(1)
                val isSynced = localCursor.getInt(2)
                if (isSynced == 1 && remoteUpdatedAt > localUpdatedAt) {
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
                            put(
                                    StoreBookDbHelper.KEY_IS_DELETED,
                                    doc.getLong("is_deleted")?.toInt() ?: 0
                            )

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
                    db.update(
                            tableName,
                            cv,
                            "${StoreBookDbHelper.KEY_ID} = ?",
                            arrayOf(localIdToUpdate.toString())
                    )
                }
            }
        }
    }

    private val listeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
    private var onDataChangedCallback: (() -> Unit)? = null

    fun registerDataChangedCallback(callback: () -> Unit) {
        onDataChangedCallback = callback
    }

    fun startRealtimeSync(storeId: String) {
        stopRealtimeSync()

        val tables =
                listOf(
                        StoreBookDbHelper.TABLE_ITEMS to
                                listOf(
                                        StoreBookDbHelper.KEY_ITEM_NAME,
                                        StoreBookDbHelper.KEY_ITEM_QTY,
                                        StoreBookDbHelper.KEY_ITEM_UNIT,
                                        StoreBookDbHelper.KEY_ITEM_BUY_PRICE,
                                        StoreBookDbHelper.KEY_ITEM_SELL_PRICE,
                                        StoreBookDbHelper.KEY_ITEM_CATEGORY,
                                        StoreBookDbHelper.KEY_ITEM_THRESHOLD,
                                        StoreBookDbHelper.KEY_ITEM_PHOTO,
                                        StoreBookDbHelper.KEY_ITEM_HSN,
                                        StoreBookDbHelper.KEY_ITEM_TAX_RATE,
                                ),
                        StoreBookDbHelper.TABLE_SALES to
                                listOf(
                                        StoreBookDbHelper.KEY_TIMESTAMP,
                                        StoreBookDbHelper.KEY_SALE_TOTAL,
                                        StoreBookDbHelper.KEY_SALE_DISCOUNT,
                                        StoreBookDbHelper.KEY_SALE_CUSTOMER,
                                        StoreBookDbHelper.KEY_SALE_CUSTOMER_GSTIN,
                                        StoreBookDbHelper.KEY_SALE_BUSINESS_GSTIN,
                                        StoreBookDbHelper.KEY_SALE_CUSTOMER_ADDRESS,
                                        StoreBookDbHelper.KEY_SALE_BUSINESS_ADDRESS,
                                        StoreBookDbHelper.KEY_SALE_TYPE,
                                        StoreBookDbHelper.KEY_NOTES,
                                ),
                        StoreBookDbHelper.TABLE_SALE_ITEMS to
                                listOf(
                                        StoreBookDbHelper.KEY_SI_SALE_ID,
                                        StoreBookDbHelper.KEY_SI_ITEM_ID,
                                        StoreBookDbHelper.KEY_SI_ITEM_NAME,
                                        StoreBookDbHelper.KEY_SI_UNIT,
                                        StoreBookDbHelper.KEY_SI_QTY,
                                        StoreBookDbHelper.KEY_SI_SELL_PRICE,
                                        StoreBookDbHelper.KEY_SI_BUY_PRICE,
                                ),
                        StoreBookDbHelper.TABLE_UDHAAR to
                                listOf(
                                        StoreBookDbHelper.KEY_UDHAAR_CUSTOMER,
                                        StoreBookDbHelper.KEY_UDHAAR_AMOUNT,
                                        StoreBookDbHelper.KEY_UDHAAR_TYPE,
                                        StoreBookDbHelper.KEY_TIMESTAMP,
                                        StoreBookDbHelper.KEY_NOTES,
                                ),
                        StoreBookDbHelper.TABLE_EXPENSES to
                                listOf(
                                        StoreBookDbHelper.KEY_EXPENSE_TYPE,
                                        StoreBookDbHelper.KEY_EXPENSE_DESC,
                                        StoreBookDbHelper.KEY_EXPENSE_AMOUNT,
                                        StoreBookDbHelper.KEY_TIMESTAMP,
                                        StoreBookDbHelper.KEY_EXPENSE_SUPPLIER,
                                        StoreBookDbHelper.KEY_EXPENSE_PHONE,
                                ),
                        StoreBookDbHelper.TABLE_SUPPLIERS to
                                listOf(
                                        "name",
                                        "phone",
                                        "gstin",
                                        "address",
                                ),
                        StoreBookDbHelper.TABLE_PURCHASES to
                                listOf(
                                        "supplier_id",
                                        "supplier_name",
                                        "total_amount",
                                        "tax_amount",
                                        "type",
                                        StoreBookDbHelper.KEY_TIMESTAMP,
                                        StoreBookDbHelper.KEY_NOTES,
                                ),
                        StoreBookDbHelper.TABLE_PURCHASE_ITEMS to
                                listOf(
                                        "purchase_id",
                                        "item_id",
                                        "item_name",
                                        "quantity",
                                        "unit",
                                        "buy_price",
                                )
                )

        val dbHelper = StoreBookDbHelper(context.applicationContext, storeId)
        val db = dbHelper.writableDatabase

        for ((tableName, columns) in tables) {
            val listener =
                    firestore
                            .collection("stores")
                            .document(storeId)
                            .collection(tableName)
                            .addSnapshotListener { snapshots, e ->
                                if (e != null) {
                                    Log.w("SyncManager", "Listen failed for table $tableName", e)
                                    return@addSnapshotListener
                                }

                                if (snapshots != null) {
                                    syncScope.launch {
                                        var localDbUpdated = false

                                        for (change in snapshots.documentChanges) {
                                            if (change.type ==
                                                            com.google.firebase.firestore
                                                                    .DocumentChange.Type.ADDED ||
                                                            change.type ==
                                                                    com.google.firebase.firestore
                                                                            .DocumentChange.Type
                                                                            .MODIFIED
                                            ) {
                                                val doc = change.document
                                                val remoteCloudId =
                                                        doc.getString("cloud_id") ?: doc.id
                                                val remoteUpdatedAt =
                                                        doc.getLong("updated_at") ?: 0L

                                                val localCursor =
                                                        db.rawQuery(
                                                                "SELECT ${StoreBookDbHelper.KEY_ID}, ${StoreBookDbHelper.KEY_UPDATED_AT}, ${StoreBookDbHelper.KEY_IS_SYNCED} FROM $tableName WHERE ${StoreBookDbHelper.KEY_CLOUD_ID} = ?",
                                                                arrayOf(remoteCloudId)
                                                        )

                                                var shouldInsert = false
                                                var shouldUpdate = false
                                                var localIdToUpdate = -1L

                                                if (localCursor.moveToFirst()) {
                                                    val localUpdatedAt = localCursor.getLong(1)
                                                    val isSynced = localCursor.getInt(2)
                                                    if (isSynced == 1 &&
                                                                    remoteUpdatedAt > localUpdatedAt
                                                    ) {
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
                                                                put(
                                                                        StoreBookDbHelper
                                                                                .KEY_CLOUD_ID,
                                                                        remoteCloudId
                                                                )
                                                                put(
                                                                        StoreBookDbHelper
                                                                                .KEY_UPDATED_AT,
                                                                        remoteUpdatedAt
                                                                )
                                                                put(
                                                                        StoreBookDbHelper
                                                                                .KEY_IS_SYNCED,
                                                                        1
                                                                )
                                                                put(
                                                                        StoreBookDbHelper
                                                                                .KEY_IS_DELETED,
                                                                        doc.getLong("is_deleted")
                                                                                ?.toInt()
                                                                                ?: 0
                                                                )

                                                                for (col in columns) {
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
                                                        localDbUpdated = true
                                                    } else if (shouldUpdate) {
                                                        db.update(
                                                                tableName,
                                                                cv,
                                                                "${StoreBookDbHelper.KEY_ID} = ?",
                                                                arrayOf(localIdToUpdate.toString())
                                                        )
                                                        localDbUpdated = true
                                                    }
                                                }
                                            }
                                        }

                                        if (localDbUpdated) {
                                            Log.i(
                                                    "SyncManager",
                                                    "Local database updated from real-time changes on $tableName"
                                            )
                                            withContext(Dispatchers.Main) {
                                                onDataChangedCallback?.invoke()
                                            }
                                        }
                                    }
                                }
                            }
            listeners.add(listener)
        }
    }

    fun stopRealtimeSync() {
        syncJob.cancel()
        syncJob = kotlinx.coroutines.SupervisorJob()
        syncScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + syncJob)
        for (listener in listeners) {
            listener.remove()
        }
        listeners.clear()
    }
}
