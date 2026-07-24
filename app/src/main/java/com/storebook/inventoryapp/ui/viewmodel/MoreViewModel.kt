package com.storebook.inventoryapp.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.storebook.inventoryapp.data.backup.BackupManager
import com.storebook.inventoryapp.data.backup.RestoreStage
import com.storebook.inventoryapp.data.play.PlayBillingManager
import com.storebook.inventoryapp.dataconnect.*
import com.storebook.inventoryapp.shared.domain.models.ExpenseEntry
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.shared.domain.repository.ExpenseRepository
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.repository.SalesRepository
import com.storebook.inventoryapp.shared.domain.repository.SystemRepository
import com.storebook.inventoryapp.ui.viewmodels.UserRole
import com.storebook.inventoryapp.utils.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class MoreViewModel(
    private val salesRepository: SalesRepository,
    private val expenseRepository: ExpenseRepository,
    private val inventoryRepository: InventoryRepository,
    private val systemRepository: SystemRepository,
    private val context: Context,
) : ViewModel() {
    private val prefs = SecurityUtils.getEncryptedPrefs(context)

    // Play Billing: observe for real-time subscription state changes
    private val billingManager by lazy { PlayBillingManager(context) }
    var userRole: String by mutableStateOf(prefs.getString("user_role", "owner") ?: "owner")
    var userRoleType: UserRole by mutableStateOf(UserRole.fromString(userRole))
    var isPremiumUser by mutableStateOf(false)
    var activeStoreId: String by
        mutableStateOf(prefs.getString("active_store_id", "default") ?: "default")
    private var _businessName by
        mutableStateOf(
            prefs.getString(
                "business_name_$activeStoreId",
                prefs.getString("business_name", ""),
            )
                ?: "",
        )
    var businessName: String
        get() = _businessName
        set(value) {
            _businessName = value
            prefs
                .edit()
                .putString("business_name_$activeStoreId", value)
                .putString("business_name", value)
                .apply()
        }
    var userStores: List<String> by
        mutableStateOf(
            prefs.getString("user_stores", "default")?.split(",")?.filter {
                it.isNotBlank()
            }
                ?: listOf("default"),
        )

    var businessGstin by
        mutableStateOf(
            prefs.getString(
                "business_gstin_$activeStoreId",
                prefs.getString("business_gstin", ""),
            )
                ?: "",
        )
    var businessAddress by
        mutableStateOf(
            prefs.getString(
                "business_address_$activeStoreId",
                prefs.getString("business_address", ""),
            )
                ?: "",
        )
    var businessCurrency by
        mutableStateOf(
            prefs.getString(
                "business_currency_$activeStoreId",
                prefs.getString("business_currency", "INR"),
            )
                ?: "INR",
        )

    // other settings
    var isHapticFeedbackEnabled by mutableStateOf(prefs.getBoolean("haptic_feedback", true))
    var lowStockThreshold by
        mutableStateOf(prefs.getString("default_low_stock_threshold", "5") ?: "5")

    // E20-S1: Cloud Backup state
    var backupProgress by mutableStateOf(-1) // -1 = idle, 0-99 = uploading, 100 = done
    var backupError by mutableStateOf<String?>(null)
    val lastBackupMillis: Long
        get() = prefs.getLong("last_backup_timestamp", 0)

    /** Trigger a full DB upload to Firebase Storage. */
    fun triggerBackup() {
        viewModelScope.launch {
            backupProgress = 0
            backupError = null
            val manager = BackupManager(context, activeStoreId)
            try {
                manager.uploadToCloud().collect { progress ->
                    backupProgress = progress
                    if (progress == 100) {
                        prefs
                            .edit()
                            .putLong("last_backup_timestamp", System.currentTimeMillis())
                            .apply()
                        // After successful backup, re-check cloud availability for restore info
                        checkForCloudBackup()
                    }
                }
            } catch (e: Exception) {
                backupError = "Backup failed: ${e.localizedMessage}"
                backupProgress = -1
            }
        }
    }

    // E20-S2: Cloud Restore state
    var restoreAvailable by mutableStateOf(false)
    var restoreTimestampMs by mutableStateOf(0L)
    var restoreSizeBytes by mutableStateOf(0L)
    var restoreProgress by mutableStateOf(-1) // -1 = idle, 0-99 = restoring, 100 = done
    var restoreError by mutableStateOf<String?>(null)
    var restoreStageMsg by mutableStateOf("")

    /** Check if a cloud backup exists for this storeId. */
    fun checkForCloudBackup() {
        viewModelScope.launch {
            val manager = BackupManager(context, activeStoreId)
            try {
                manager.fetchLatestBackupInfo().collect { info ->
                    if (info != null) {
                        restoreAvailable = true
                        restoreTimestampMs = info.timestampMs
                        restoreSizeBytes = info.sizeBytes
                    } else {
                        restoreAvailable = false
                    }
                }
            } catch (_: Exception) {
                restoreAvailable = false
            }
        }
    }

    /** Trigger download + apply of latest cloud backup. */
    fun triggerRestore() {
        viewModelScope.launch {
            restoreProgress = 0
            restoreError = null
            val manager = BackupManager(context, activeStoreId)
            try {
                manager.restoreFromCloud().collect { state ->
                    restoreProgress =
                        when (state.stage) {
                            RestoreStage.DOWNLOADING -> state.progressPercent
                            RestoreStage.VERIFYING, RestoreStage.APPLYING -> 99
                            RestoreStage.DONE -> 100
                            else -> 0
                        }
                    restoreStageMsg = state.message
                    if (state.stage == RestoreStage.FAILED) {
                        restoreError = state.message
                        restoreProgress = -1
                    } else if (state.stage == RestoreStage.DONE) {
                        // After restore: re-check backup info (timestamp updated)
                        checkForCloudBackup()
                        // Reload local data from restored DB
                        loadData()
                    }
                }
            } catch (_: Exception) {
                restoreError = "Restore encountered an error"
                restoreProgress = -1
            }
        }
    }

    private val _storeNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val storeNames: StateFlow<Map<String, String>> = _storeNames

    fun getStoreName(id: String): String =
        _storeNames.value[id]
            ?: prefs.getString("business_name_$id", null)
            ?: prefs.getString("business_name", null) ?: "My Store"

    fun switchStore(
        newStoreId: String,
        onProgress: ((Int, String) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newDbFile = context.getDatabasePath("storebook_$newStoreId.db")
            if (newDbFile.exists()) {
                context.deleteDatabase("storebook_$newStoreId.db")
            }
            prefs.edit().putLong("last_sync_timestamp_$newStoreId", 0L).apply()

            withContext(Dispatchers.Main) {
                activeStoreId = newStoreId
                prefs.edit().putString("active_store_id", newStoreId).apply()
                businessName =
                    prefs.getString(
                        "business_name_$newStoreId",
                        prefs.getString("business_name", "StoreBook Kirana") ?: "StoreBook Kirana",
                    )
                        ?: "StoreBook Kirana"
                businessGstin =
                    prefs.getString("business_gstin_$newStoreId", prefs.getString("business_gstin", ""))
                        ?: ""
                businessAddress =
                    prefs.getString(
                        "business_address_$newStoreId",
                        prefs.getString("business_address", ""),
                    )
                        ?: ""
                businessCurrency =
                    prefs.getString(
                        "business_currency_$newStoreId",
                        prefs.getString("business_currency", "INR"),
                    )
                        ?: "INR"
                val stores =
                    prefs
                        .getString("user_stores", "default")
                        ?.split(",")
                        ?.filter { it.isNotBlank() }
                        ?.toMutableList()
                        ?: mutableListOf("default")
                if (!stores.contains(newStoreId)) {
                    stores.add(newStoreId)
                    prefs.edit().putString("user_stores", stores.joinToString(",")).apply()
                }
                userStores =
                    prefs.getString("user_stores", "default")?.split(",")?.filter { it.isNotBlank() }
                        ?: listOf("default")
            }

            try {
                com.storebook.inventoryapp.data.sync.SyncWorker.performSync(context, newStoreId) { progress, message ->
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onProgress?.invoke(progress, message)
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }

            withContext(Dispatchers.Main) {
                loadData()
                onComplete?.invoke()
            }
        }
    }

    fun createLocalStore(
        name: String,
        onProgress: ((Int, String) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
    ) {
        val newStoreId =
            java.util.UUID
                .randomUUID()
                .toString()
        prefs.edit().putString("business_name_$newStoreId", name).apply()
        switchStore(newStoreId, onProgress, onComplete)
    }

    fun updateBusinessName(value: String) {
        businessName = value
    }

    fun updateBusinessGstin(value: String) {
        businessGstin = value
        prefs.edit().putString("business_gstin_$activeStoreId", value).apply()
    }

    fun updateBusinessAddress(value: String) {
        businessAddress = value
        prefs.edit().putString("business_address_$activeStoreId", value).apply()
    }

    fun updateBusinessCurrency(value: String) {
        businessCurrency = value
        prefs.edit().putString("business_currency_$activeStoreId", value).apply()
    }

    fun updateHapticFeedbackEnabled(value: Boolean) {
        updateHapticFeedback(value)
    }

    fun logOverheadExpense(
        desc: String,
        amount: Double,
    ) {
        viewModelScope.launch {
            expenseRepository.insertExpense(
                type = "OVERHEAD",
                description = desc.trim(),
                amount = amount,
                supplierName = null,
                supplierPhone = null,
            )
            loadData()
        }
    }

    fun logRestockItem(
        itemId: Long,
        quantity: Double,
        costPrice: Double,
        supplier: String?,
        phone: String?,
    ) {
        viewModelScope.launch {
            inventoryRepository.updateItemStock(itemId, quantity)
            loadData()
        }
    }

    private val _salesList = MutableStateFlow<List<Sale>>(emptyList())
    val salesList: StateFlow<List<Sale>> = _salesList

    private val _expensesList = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expensesList: StateFlow<List<ExpenseEntry>> = _expensesList

    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _allItems

    init {
        // Connect billing client & observe for real-time subscription state
        billingManager.connect()
        viewModelScope.launch {
            billingManager.state.collect { billingState ->
                // Play Billing reports pro is unlocked → sync to SharedPrefs + set local state
                if (billingState.isProUnlocked != isPremiumUser) {
                    isPremiumUser = billingState.isProUnlocked
                    if (isPremiumUser) {
                        prefs.edit().putBoolean("is_premium", true).apply()
                    }
                }
            }
        }
        // Fetch subscription from DataConnect (DB single source of truth)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val connector = StorebookConnectorConnector.instance
                    val userRes = connector.getUser.execute(uid)
                    val user = userRes.data.user
                    if (user != null) {
                        val dbPremium =
                            user.subscriptionPlan == "pro" &&
                                user.subscriptionStatus == "active"
                        if (dbPremium) {
                            prefs.edit().putBoolean("is_premium", true).apply()
                        } else if (!prefs.getBoolean("is_premium", false)) {
                            prefs.edit().putBoolean("is_premium", false).apply()
                        }
                        isPremiumUser = prefs.getBoolean("is_premium", false) || billingManager.isProUnlocked()
                    }
                }
            } catch (_: Exception) {
                // Fallback to existing SharedPrefs value
            }
        }
        loadData()
        // E20-S2: on startup, check if cloud backup is available for restore
        checkForCloudBackup()
        fetchStoreNames()
    }

    /** Refresh subscription from all known sources: DataConnect, Play Billing, SharedPrefs. */
    fun refreshUserState() {
        userRole = prefs.getString("user_role", "owner") ?: "owner"
        userRoleType = UserRole.fromString(userRole)
        // Check DB + billing state (no expiry validation — that's handled by BillingClient)
        isPremiumUser = prefs.getBoolean("is_premium", false) || billingManager.isProUnlocked()
    }

    private fun fetchStoreNames() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val connector =
                    com.storebook.inventoryapp.dataconnect.StorebookConnectorConnector.instance
                val newNames = mutableMapOf<String, String>()
                userStores.forEach { sId ->
                    try {
                        val storeRes = connector.getStore.execute(sId)
                        val sName = storeRes.data.store?.name
                        if (!sName.isNullOrBlank()) {
                            prefs.edit().putString("business_name_$sId", sName).apply()
                            newNames[sId] = sName
                            if (sId == activeStoreId) {
                                businessName = sName
                            }
                        } else {
                            newNames[sId] =
                                prefs.getString("business_name_$sId", "Store (${sId.take(8)})")
                                    ?: "Store (${sId.take(8)})"
                        }
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        newNames[sId] =
                            prefs.getString("business_name_$sId", "Store (${sId.take(8)})")
                                ?: "Store (${sId.take(8)})"
                    }
                }
                _storeNames.value = newNames
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _salesList.value =
                salesRepository.getSalesByDateRange(0, Long.MAX_VALUE).map { s ->
                    Sale(
                        id = s.id,
                        timestamp = s.timestamp,
                        totalAmount = s.total_amount,
                        discountAmount = s.discount_amount,
                        customerName = s.customer_name,
                        customerGstin = s.customer_gstin,
                        businessGstin = s.business_gstin,
                        customerAddress = s.customer_address,
                        businessAddress = s.business_address,
                        type = s.type,
                        notes = s.notes,
                        items = emptyList(),
                    )
                }
            _expensesList.value = expenseRepository.getAllExpenses()
            _allItems.value =
                inventoryRepository.getActiveItems().map { i ->
                    Item(
                        id = i.id,
                        name = i.name,
                        quantity = i.quantity,
                        unit = i.unit,
                        buyPrice = i.buy_price,
                        sellPrice = i.sell_price,
                        lowStockThreshold = i.low_stock_threshold,
                        category = i.category,
                    )
                }
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        isHapticFeedbackEnabled = enabled
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
    }

    fun updateLowStockThreshold(threshold: String) {
        lowStockThreshold = threshold
        prefs.edit().putString("default_low_stock_threshold", threshold).apply()
    }

    fun clearAllLocalData(onComplete: () -> Unit) {
        viewModelScope.launch {
            systemRepository.clearLocalDatabase()
            onComplete()
        }
    }

    fun signOut(onComplete: () -> Unit) {
        prefs.edit().clear().apply()
        onComplete()
    }

    fun seedDummyData() {
        viewModelScope.launch {
            systemRepository.seedDummyData()
            loadData()
        }
    }

    fun importInventoryFromCSV(
        context: Context,
        fileUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream =
                    context.contentResolver.openInputStream(fileUri)
                        ?: throw Exception("Could not open CSV file")
                var importedCount = 0

                inputStream.bufferedReader().use { reader ->
                    val header = reader.readLine() ?: throw Exception("CSV file is empty")

                    // Detect if file has new 10-column format (with HSN + Tax) or legacy 8-column
                    val headerTokens = parseCsvLine(header)
                    if (headerTokens.size < 8) {
                        throw Exception("Invalid CSV structure")
                    }
                    val hasExtendedFields = headerTokens.size >= 10

                    var line = reader.readLine()
                    while (line != null) {
                        val tokens = parseCsvLine(line)
                        if (tokens.size >= 8) {
                            val name = tokens[1].trim()
                            val qty = tokens[2].toDoubleOrNull() ?: 0.0
                            val unit = tokens[3].trim()
                            val buyPrice = tokens[4].toDoubleOrNull() ?: 0.0
                            val sellPrice = tokens[5].toDoubleOrNull() ?: 0.0
                            val threshold = tokens[6].toDoubleOrNull() ?: 0.0
                            val category = tokens[7].trim()
                            val hsnCode =
                                if (hasExtendedFields && tokens.size > 8) {
                                    tokens[8].trim().ifBlank { null }
                                } else {
                                    null
                                }
                            val taxRate =
                                if (hasExtendedFields && tokens.size > 9) {
                                    tokens[9].toDoubleOrNull() ?: 0.0
                                } else {
                                    0.0
                                }

                            if (name.isNotBlank() && qty >= 0.0 && sellPrice > 0.0) {
                                inventoryRepository.insertItem(
                                    name = name,
                                    quantity = qty,
                                    unit = unit,
                                    buyPrice = buyPrice,
                                    sellPrice = sellPrice,
                                    threshold = threshold,
                                    category = category,
                                    photoPath = null,
                                    barcode = null,
                                    hsnCode = hsnCode,
                                    taxRate = taxRate,
                                )
                                importedCount++
                            }
                        }
                        line = reader.readLine()
                    }
                }
                withContext(Dispatchers.Main) {
                    if (importedCount > 0) {
                        loadData()
                        onSuccess()
                    } else {
                        onError("No valid records found in CSV file")
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                withContext(Dispatchers.Main) { onError(e.message ?: "CSV Import failed") }
            }
        }
    }

    fun exportInventoryToCSV(
        context: Context,
        fileUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = inventoryRepository.getActiveItems()
                val csvContent = StringBuilder()
                csvContent.append(
                    "ID,Item Name,Stock Quantity,Unit,Buy Price,Sell Price,Alert Threshold,Category,HSN Code,Tax Rate\n",
                )
                for (item in items) {
                    csvContent.append(
                        "${item.id},${csvEscape(item.name)},${item.quantity},${csvEscape(item.unit)}," +
                            "${item.buy_price},${item.sell_price},${item.low_stock_threshold}," +
                            "${csvEscape(item.category)},${csvEscape(item.hsn_code ?: "")},${item.tax_rate}\n",
                    )
                }

                context.contentResolver.openOutputStream(fileUri)?.use { out ->
                    out.write(csvContent.toString().toByteArray(Charsets.UTF_8))
                }
                    ?: throw Exception("Failed to open file for writing")

                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                withContext(Dispatchers.Main) { onError(e.message ?: "Unknown Error") }
            }
        }
    }

    /** RFC 4180 compliant: wraps in quotes if value contains comma, quote, or newline */
    private fun csvEscape(value: String): String =
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }

    /** Parses a single CSV line respecting quoted fields that may contain commas */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    suspend fun createStaffAccount(
        username: String,
        pin: String,
    ): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient()
                val ownerId =
                    com.google.firebase.auth.FirebaseAuth
                        .getInstance()
                        .currentUser
                        ?.uid
                        ?: return@withContext false
                val json =
                    """
                    {
                        "username": "$username",
                        "password": "$pin",
                        "storeId": "$activeStoreId",
                        "ownerId": "$ownerId"
                    }
                    """.trimIndent()

                val body =
                    okhttp3.RequestBody.create(
                        "application/json; charset=utf-8".toMediaTypeOrNull(),
                        json,
                    )
                val request =
                    okhttp3.Request
                        .Builder()
                        .url("http://10.0.2.2:3000/api/staff/invite")
                        .post(body)
                        .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }
    }
}
