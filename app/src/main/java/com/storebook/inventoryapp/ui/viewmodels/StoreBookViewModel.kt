package com.storebook.inventoryapp.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.storebook.inventoryapp.data.billing.BillingEngine
import com.storebook.inventoryapp.data.play.PlayBillingManager
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.CustomerBalance
import com.storebook.inventoryapp.shared.domain.models.ExpenseEntry
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.ItemBatch
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.data.repository.StoreBookRepository
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.shared.domain.models.SupplierBalance
import com.storebook.inventoryapp.shared.domain.models.UdhaarEntry
import com.storebook.inventoryapp.data.sync.FirestoreSyncManager
import com.storebook.inventoryapp.data.sync.SyncWorker
import com.storebook.inventoryapp.utils.InvoicePdfGenerator
import com.storebook.inventoryapp.utils.ExcelExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppPermission {
    VIEW_FINANCIALS,
    MANAGE_STAFF,
    MANAGE_BUSINESS_SETTINGS,
    VIEW_REPORTS,
    MANAGE_INVENTORY,
    RECORD_SALES,
    MANAGE_PREMIUM
}

enum class UserRole(val permissions: Set<AppPermission>) {
    OWNER(AppPermission.entries.toSet()),
    MANAGER(setOf(
        AppPermission.VIEW_FINANCIALS,
        AppPermission.VIEW_REPORTS,
        AppPermission.MANAGE_INVENTORY,
        AppPermission.RECORD_SALES
    )),
    BILLER(setOf(
        AppPermission.RECORD_SALES
    ));

    fun hasPermission(permission: AppPermission): Boolean = permissions.contains(permission)

    companion object {
        fun fromString(role: String): UserRole {
            return try {
                valueOf(role.uppercase())
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (role.lowercase() == "staff") BILLER else OWNER
            }
        }
    }
}

class StoreBookViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val prefs = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(application)

    var activeStoreId: String by mutableStateOf(prefs.getString("active_store_id", "default") ?: "default")
        private set

    var userStores: List<String> by mutableStateOf(prefs.getStringSet("user_stores", setOf("default"))?.toList() ?: listOf("default"))
        private set

    var userRole: String by mutableStateOf(prefs.getString("user_role", "owner") ?: "owner")
        private set

    var userRoleType: UserRole by mutableStateOf(UserRole.fromString(prefs.getString("user_role", "owner") ?: "owner"))
        private set

    var repository = StoreBookRepository(application.applicationContext, activeStoreId)
        private set

    var isHapticFeedbackEnabled: Boolean by mutableStateOf(prefs.getBoolean("haptic_feedback_enabled", true))
        private set

    fun updateHapticFeedbackEnabled(enabled: Boolean) {
        isHapticFeedbackEnabled = enabled
        prefs.edit().putBoolean("haptic_feedback_enabled", enabled).apply()
    }

    val billingManager = PlayBillingManager(application.applicationContext)
    private val syncManager = FirestoreSyncManager(application.applicationContext)

    // Data Flows
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _allItems

    private val _filteredItems = MutableStateFlow<List<Item>>(emptyList())
    val filteredItems: StateFlow<List<Item>> = _filteredItems
    // Near expiry items derived from batches
    private val _nearExpiryItems = MutableStateFlow<List<Item>>(emptyList())
    val nearExpiryItems: StateFlow<List<Item>> = _nearExpiryItems

    private val _isLoadingItems = MutableStateFlow(false)
    val isLoadingItems: StateFlow<Boolean> = _isLoadingItems

    private val _lowStockItems = MutableStateFlow<List<Item>>(emptyList())
    val lowStockItems: StateFlow<List<Item>> = _lowStockItems

    private val _salesList = MutableStateFlow<List<Sale>>(emptyList())
    val salesList: StateFlow<List<Sale>> = _salesList

    private val _salesHistoryList = MutableStateFlow<List<Sale>>(emptyList())
    val salesHistoryList: StateFlow<List<Sale>> = _salesHistoryList

    private val _quotationsList = MutableStateFlow<List<Sale>>(emptyList())
    val quotationsList: StateFlow<List<Sale>> = _quotationsList

    private val _udhaarBalances = MutableStateFlow<List<CustomerBalance>>(emptyList())
    val udhaarBalances: StateFlow<List<CustomerBalance>> = _udhaarBalances

    private val _customerSuggestions = MutableStateFlow<List<String>>(emptyList())
    val customerSuggestions: StateFlow<List<String>> = _customerSuggestions

    private var customerSearchJob: Job? = null

    fun updateCustomerSearch(query: String) {
        customerSearchJob?.cancel()
        customerSearchJob =
            viewModelScope.launch {
                delay(200) // debounce
                _customerSuggestions.value = repository.searchCustomers(query, 50)
            }
    }

    fun selectCustomer(name: String) {
        cartCustomerName = name
        viewModelScope.launch {
            val (gstin, address) = repository.getCustomerDetails(name)
            if (!gstin.isNullOrBlank()) {
                cartCustomerGstin = gstin
            }
            if (!address.isNullOrBlank()) {
                cartCustomerAddress = address
            }
        }
    }

    private val _expensesList = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expensesList: StateFlow<List<ExpenseEntry>> = _expensesList

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases

    private val _supplierBalances = MutableStateFlow<List<SupplierBalance>>(emptyList())
    val supplierBalances: StateFlow<List<SupplierBalance>> = _supplierBalances

    private val _nearExpiryBatches = MutableStateFlow<List<ItemBatch>>(emptyList())
    val nearExpiryBatches: StateFlow<List<ItemBatch>> = _nearExpiryBatches

    // Cart / Checkout State
    var cartItems by mutableStateOf<List<CartItem>>(emptyList())
        private set

    var cartDiscount by mutableDoubleStateOf(0.0)
    var cartCustomerName by mutableStateOf("")
    var cartCustomerGstin by mutableStateOf("")
    var cartCustomerAddress by mutableStateOf("")
    var cartNotes by mutableStateOf("")
    // Smart default: restore last used payment mode across sessions
    var cartPaymentMode by mutableStateOf(prefs.getString("last_payment_mode", "Cash") ?: "Cash")
    // Smart default: restore last restock supplier
    var lastRestockSupplierName by mutableStateOf(prefs.getString("last_restock_supplier", "") ?: "")
        private set

    fun saveLastPaymentMode(mode: String) {
        cartPaymentMode = mode
        prefs.edit { putString("last_payment_mode", mode) }
    }
    fun saveLastRestockSupplier(name: String) {
        lastRestockSupplierName = name
        prefs.edit { putString("last_restock_supplier", name) }
    }

    var convertingQuotationId by mutableStateOf<Long?>(null)
        private set

    var businessName by
    mutableStateOf(
        prefs.getString("business_name", "StoreBook Kirana") ?: "StoreBook Kirana"
    )
        private set
    var businessGstin by mutableStateOf(prefs.getString("business_gstin", "") ?: "")
        private set
    var businessAddress by mutableStateOf(prefs.getString("business_address", "") ?: "")
        private set
    var businessCurrency by mutableStateOf(prefs.getString("business_currency", "INR") ?: "INR")
        private set

    fun updateBusinessName(name: String) {
        businessName = name
        prefs.edit { putString("business_name", name) }
    }

    fun updateBusinessGstin(gstin: String) {
        businessGstin = gstin
        prefs.edit { putString("business_gstin", gstin) }
    }

    fun updateBusinessAddress(address: String) {
        businessAddress = address
        prefs.edit { putString("business_address", address) }
    }

    fun updateBusinessCurrency(currencyCode: String) {
        businessCurrency = currencyCode
        prefs.edit { putString("business_currency", currencyCode) }
        com.storebook.inventoryapp.utils.updateCurrencyConfig(currencyCode)
    }

    // Billing state (reactive from Play Billing client)
    var isPremiumUser: Boolean by mutableStateOf(prefs.getBoolean("is_premium", false))

    var errorMessage by mutableStateOf<String?>(null)

    // Undo mechanism
    var lastSaleId by mutableStateOf<Long?>(null)
        private set
    var lastSaleTime by mutableLongStateOf(0L)
        private set

    init {
        com.storebook.inventoryapp.utils.updateCurrencyConfig(prefs.getString("business_currency", "INR") ?: "INR")
        viewModelScope.launch {
            repository.standardizeCustomerNames()
            loadAllData()
        }

        viewModelScope.launch {
            billingManager.state.collect { billingState ->
                // Staff members inherit the Owner's cloud sync capability.
                // Their personal Google Play accounts won't have the subscription.
                val playPremium = billingState.isProUnlocked
                val webPremium = prefs.getBoolean("is_premium", false)
                val isLogged = FirebaseAuth.getInstance().currentUser != null
                isPremiumUser = isLogged && (playPremium || webPremium || userRole == "staff")
                if (isPremiumUser) {
                    triggerSync()
                    startSyncManagerRealtime()
                } else {
                    syncManager.stopRealtimeSync()
                }
            }
        }
    }

    private fun startSyncManagerRealtime() {
        if (isPremiumUser && activeStoreId != "default") {
            syncManager.registerDataChangedCallback {
                // When other users edit the data, reload it locally (without triggering sync cycle)
                loadAllData(triggerSync = false)
            }
            syncManager.startRealtimeSync(activeStoreId)
        }
    }

    fun refreshUserState() {
        userRole = prefs.getString("user_role", "owner") ?: "owner"
        userRoleType = UserRole.fromString(userRole)
        val playPremium = billingManager.state.value.isProUnlocked
        val webPremium = prefs.getBoolean("is_premium", false)
        val isLogged = FirebaseAuth.getInstance().currentUser != null
        isPremiumUser = isLogged && (playPremium || webPremium || userRole == "staff" || userRoleType == UserRole.BILLER)

        val newStoreId = prefs.getString("active_store_id", "default") ?: "default"
        if (newStoreId != activeStoreId) {
            switchStore(newStoreId)
        } else {
            if (isPremiumUser) {
                startSyncManagerRealtime()
            } else {
                syncManager.stopRealtimeSync()
            }
        }
    }

    fun switchStore(newStoreId: String) {
        activeStoreId = newStoreId
        prefs.edit { putString("active_store_id", newStoreId) }

        val stores = prefs.getStringSet("user_stores", setOf("default"))?.toMutableSet() ?: mutableSetOf("default")
        if (!stores.contains(newStoreId)) {
            stores.add(newStoreId)
            prefs.edit { putStringSet("user_stores", stores) }
        }

        userStores = prefs.getStringSet("user_stores", setOf("default"))?.toList() ?: listOf("default")
        repository = StoreBookRepository(getApplication(), newStoreId)
        viewModelScope.launch {
            repository.standardizeCustomerNames()
            loadAllData()
            if (isPremiumUser) {
                startSyncManagerRealtime()
            } else {
                syncManager.stopRealtimeSync()
            }
        }
    }

    fun triggerSync() {
        if (isPremiumUser) {
            val data = androidx.work.Data.Builder()
                .putString("STORE_ID", activeStoreId)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(getApplication()).enqueueUniqueWork(
                "StoreBookSync",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    fun loadAllData(triggerSync: Boolean = true) {
        viewModelScope.launch {
            val items = repository.getActiveItems()
            _allItems.value = items
            _filteredItems.value = items
            _lowStockItems.value = items.filter { it.quantity <= it.lowStockThreshold }
            _salesList.value = repository.getSalesPage(limit = 50, offset = 0)
            _udhaarBalances.value = repository.getUdhaarBalances()
            _customerSuggestions.value = repository.searchCustomers("", 50)
            _expensesList.value = repository.getExpenses()
            _quotationsList.value = repository.getQuotations()
            _suppliers.value = repository.getSuppliers()
            _purchases.value = repository.getPurchases()
            _supplierBalances.value = repository.getSupplierBalances()
            _nearExpiryBatches.value = repository.getNearExpiryBatches(30)
            // Map batches to items for UI filter
            val nearItems = _nearExpiryBatches.value.mapNotNull { batch ->
                // Synchronously fetch item (could be optimized)
                repository.getItemById(batch.itemId)?.takeIf { it.isDeleted == 0 }
            }.distinctBy { it.id }
            _nearExpiryItems.value = nearItems

            if (triggerSync) {
                triggerSync()
            }
        }
    }

    fun loadSalesHistory(startTs: Long, endTs: Long) {
        viewModelScope.launch {
            _salesHistoryList.value = repository.getSalesByDateRange(startTs, endTs)
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.stopRealtimeSync()
        billingManager.endConnection()
    }

    /**
     * Load items from the DB with server-side filtering, search, and sorting. Called from the
     * Inventory UI with debounced search input. This avoids loading all 1000+ items into memory and
     * filtering in Kotlin.
     */
    fun loadFilteredItems(
        search: String = "",
        category: String = "All",
        sortBy: String = "Name",
    ) {
        viewModelScope.launch {
            _isLoadingItems.value = true
            try {
                _filteredItems.value =
                    repository.getActiveItemsFiltered(
                        search = search,
                        category = category,
                        sortBy = sortBy,
                        limit = 50 // Added limit to fix loading too many items and making UI sluggish
                    )
            } finally {
                _isLoadingItems.value = false
            }
        }
    }

    fun loadMoreItems(
        search: String,
        category: String,
        sortBy: String,
        currentSize: Int,
        pageSize: Int = 50,
        onResult: (List<Item>) -> Unit,
    ) {
        viewModelScope.launch {
            val more =
                repository.getActiveItemsFiltered(
                    search = search,
                    category = category,
                    sortBy = sortBy,
                    limit = pageSize,
                    offset = currentSize,
                )
            onResult(more)
        }
    }

    // --- Inventory Actions ---

    fun addItem(
        name: String,
        quantity: Double,
        unit: String,
        buyPrice: Double,
        sellPrice: Double,
        threshold: Double,
        category: String,
        hsnCode: String? = null,
        taxRate: Double = 0.0,
        onResult: (Long) -> Unit = {},
    ) {
        viewModelScope.launch {
            val item =
                Item(
                    name = name.trim(),
                    quantity = quantity,
                    unit = unit,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    lowStockThreshold = threshold,
                    category = category,
                    hsnCode = hsnCode,
                    taxRate = taxRate,
                )
            val newId = repository.insertItem(item)
            loadAllData()
            onResult(newId)
        }
    }

    fun updateItem(
        id: Long,
        name: String,
        quantity: Double,
        unit: String,
        buyPrice: Double,
        sellPrice: Double,
        threshold: Double,
        category: String,
        hsnCode: String? = null,
        taxRate: Double = 0.0,
    ) {
        viewModelScope.launch {
            val item =
                Item(
                    id = id,
                    name = name.trim(),
                    quantity = quantity,
                    unit = unit,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    lowStockThreshold = threshold,
                    category = category,
                    hsnCode = hsnCode,
                    taxRate = taxRate,
                )
            repository.updateItem(item)
            loadAllData()
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.softDeleteItem(id)
            loadAllData()
        }
    }

    // --- Cart Actions ---

    fun addToCart(
        item: Item,
        qty: Double,
    ) {
        if (cartItems.any { it.item.id == item.id }) {
            cartItems = cartItems.map { ci ->
                if (ci.item.id == item.id) ci.copy(quantity = ci.quantity + qty) else ci
            }
        } else {
            cartItems = cartItems + CartItem(item, qty)
        }
    }

    fun changeCartQtyRelative(
        item: Item,
        delta: Double,
    ) {
        val existing = cartItems.find { it.item.id == item.id }
        if (existing != null) {
            val newQty = existing.quantity + delta
            if (newQty <= 0.0) {
                cartItems = cartItems.filterNot { it.item.id == item.id }
            } else {
                cartItems = cartItems.map { ci ->
                    if (ci.item.id == item.id) ci.copy(quantity = newQty) else ci
                }
            }
        } else if (delta > 0.0) {
            cartItems = cartItems + CartItem(item, delta)
        }
    }

    fun updateCartQty(
        item: Item,
        qty: Double,
    ) {
        if (cartItems.any { it.item.id == item.id }) {
            if (qty <= 0.0) {
                removeFromCart(item)
            } else {
                cartItems =
                    cartItems.map { ci ->
                        if (ci.item.id == item.id) ci.copy(quantity = qty) else ci
                    }
            }
        }
    }

    fun removeFromCart(item: Item) {
        cartItems = cartItems.filterNot { it.item.id == item.id }
    }

    fun clearCart() {
        cartItems = emptyList()
        cartDiscount = 0.0
        cartCustomerName = ""
        cartCustomerGstin = ""
        cartCustomerAddress = ""
        cartNotes = ""
        cartPaymentMode = "Cash"
        convertingQuotationId = null
    }

    fun loadQuotationToCart(quotation: Sale) {
        clearCart()
        val convertedCartItems = quotation.items.map { saleItem ->
            val actualItem = allItems.value.find { it.id == saleItem.itemId } ?: Item(
                id = saleItem.itemId,
                name = saleItem.itemName,
                quantity = 0.0,
                unit = saleItem.unit,
                buyPrice = saleItem.buyPrice,
                sellPrice = saleItem.sellPrice,
                lowStockThreshold = 0.0,
                category = "Imported"
            )
            CartItem(item = actualItem, quantity = saleItem.quantity)
        }
        cartItems = convertedCartItems
        cartDiscount = quotation.discountAmount
        cartCustomerName = quotation.customerName ?: ""
        cartCustomerGstin = quotation.customerGstin ?: ""
        cartCustomerAddress = quotation.customerAddress ?: ""
        cartNotes = quotation.notes ?: ""
        cartPaymentMode = "Cash"
        convertingQuotationId = quotation.id
    }

    // --- Sales Actions ---

    fun clearLastSaleId() {
        lastSaleId = null
    }

    private fun String.formatName(): String =
        this.trim().split(Regex("\\s+")).joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }

    fun checkout(
        paymentMode: String = cartPaymentMode,
        type: String = "SALE",
        onSuccess: (Long, Double) -> Unit,
    ) {
        if (cartItems.isEmpty()) return
        val customerNameForSale = cartCustomerName.takeIf { it.isNotBlank() }?.formatName()
        val notesForSale = cartNotes.trim().takeIf { it.isNotBlank() }

        viewModelScope.launch {
            try {
                val saleId =
                    repository.recordSale(
                        itemsInCart = cartItems,
                        discount = cartDiscount,
                        customerName = customerNameForSale,
                        customerGstin = cartCustomerGstin.takeIf { it.isNotBlank() },
                        customerAddress = cartCustomerAddress.takeIf { it.isNotBlank() },
                        businessGstin = businessGstin.takeIf { it.isNotBlank() },
                        businessAddress = businessAddress.takeIf { it.isNotBlank() },
                        notes = notesForSale,
                        paymentMode = paymentMode,
                        type = type,
                    )
                if (saleId != -1L) {
                    if (type != "ESTIMATE") {
                        lastSaleId = saleId
                        lastSaleTime = System.currentTimeMillis()
                    }

                    // Calculate grand total with taxes
                    val taxSummary =
                        BillingEngine.calculateInvoiceTaxes(
                            cartItems = cartItems,
                            totalDiscount = cartDiscount,
                            businessGstin = businessGstin.takeIf { it.isNotBlank() },
                            customerGstin = cartCustomerGstin.takeIf { it.isNotBlank() },
                        )
                    val total = taxSummary.grandTotal

                    // If this sale was converted from an estimate, mark the estimate as CONVERTED
                    convertingQuotationId?.let { oldQuoteId ->
                        repository.markQuotationAsConverted(oldQuoteId)
                    }

                    clearCart()
                    loadAllData()
                    onSuccess(saleId, total)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                errorMessage = e.message ?: "An error occurred during checkout"
            }
        }
    }

    fun undoLastSale(onSuccess: () -> Unit) {
        val saleId = lastSaleId ?: return
        if (System.currentTimeMillis() - lastSaleTime > 30000) {
            lastSaleId = null
            return
        }
        viewModelScope.launch {
            val success = repository.undoSale(saleId)
            if (success) {
                lastSaleId = null
                loadAllData()
                onSuccess()
            }
        }
    }

    // --- Udhaar Credit Ledger Actions ---

    fun recordUdhaarEntry(
        customerName: String,
        amount: Double,
        type: String,
        notes: String?,
    ) {
        viewModelScope.launch {
            val entry =
                UdhaarEntry(
                    customerName = customerName.formatName(),
                    amount = amount,
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    notes = notes?.trim(),
                )
            repository.insertUdhaarEntry(entry)
            loadAllData()
        }
    }

    // --- Expenses Logger Actions ---

    fun logOverheadExpense(
        desc: String,
        amount: Double,
    ) {
        viewModelScope.launch {
            val entry =
                ExpenseEntry(
                    type = "OVERHEAD",
                    description = desc.trim(),
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                )
            repository.insertExpense(entry)
            loadAllData()
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
            repository.restockItem(
                itemId = itemId,
                quantityToAdd = quantity,
                costPrice = costPrice,
                supplierName = supplier?.trim()?.takeIf { it.isNotBlank() },
                supplierPhone = phone?.trim()?.takeIf { it.isNotBlank() },
            )
            loadAllData()
        }
    }

    // --- CSV Operations ---

    fun shareInvoice(
        context: Context,
        saleId: Long,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sale = repository.getSaleById(saleId)
            if (sale != null) {
                // Fetch actual items from DB to get the correct taxRate and HSN
                val mappedCartItems =
                    sale.items.map { saleItem ->
                        val actualItem =
                            repository.getItemById(saleItem.itemId)
                                ?: Item(
                                    id = saleItem.itemId,
                                    name = saleItem.itemName,
                                    quantity = 0.0,
                                    unit = saleItem.unit,
                                    buyPrice = saleItem.buyPrice,
                                    sellPrice = saleItem.sellPrice,
                                    lowStockThreshold = 0.0,
                                    category = "",
                                )
                        CartItem(item = actualItem, quantity = saleItem.quantity)
                    }

                val file =
                    InvoicePdfGenerator.generateInvoicePdf(
                        context = context,
                        sale = sale,
                        cartItems = mappedCartItems,
                        shopName = businessName,
                        shopAddress = businessAddress,
                        shopGstin = businessGstin,
                    )
                if (file != null) {
                    withContext(Dispatchers.Main) {
                        val uri =
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file,
                            )
                        val intent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        context.startActivity(
                            Intent.createChooser(intent, "Share Invoice")
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Failed to generate PDF",
                            Toast.LENGTH_SHORT,
                        )
                            .show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Sale not found",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
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
                val items = repository.getActiveItems()
                val csvContent = StringBuilder()
                csvContent.append(
                    "ID,Item Name,Stock Quantity,Unit,Buy Price,Sell Price,Alert Threshold,Category,HSN Code,Tax Rate\n"
                )
                for (item in items) {
                    csvContent.append(
                        "${item.id},${csvEscape(item.name)},${item.quantity},${csvEscape(item.unit)},${item.buyPrice},${item.sellPrice},${item.lowStockThreshold},${csvEscape(item.category)},${csvEscape(item.hsnCode ?: "")},${item.taxRate}\n",
                    )
                }

                // Write directly to the URI provided by the Storage Access Framework
                // (CreateDocument)
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
                        throw Exception("Invalid CSV structure: Expected at least 8 columns (ID, Name, Qty, Unit, Buy Price, Sell Price, Alert Threshold, Category). Got ${headerTokens.size}")
                    }
                    val normalizedTokens = headerTokens.map { it.trim().lowercase().replace(" ", "").replace("_", "") }
                    val hasName = normalizedTokens.contains("name")
                    val hasQty = normalizedTokens.contains("quantity") || normalizedTokens.contains("qty")
                    val hasUnit = normalizedTokens.contains("unit")
                    val hasBuyPrice = normalizedTokens.contains("buyprice") || normalizedTokens.contains("purchaseprice")
                    val hasSellPrice = normalizedTokens.contains("sellprice") || normalizedTokens.contains("price")

                    if (!hasName || !hasQty || !hasUnit || !hasBuyPrice || !hasSellPrice) {
                        throw Exception("Invalid CSV template. Please use the exported CSV template with correct headers (Name, Quantity, Unit, Buy Price, Sell Price).")
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
                                if (hasExtendedFields && tokens.size > 8)
                                    tokens[8].trim().ifBlank { null }
                                else null
                            val taxRate =
                                if (hasExtendedFields && tokens.size > 9)
                                    tokens[9].toDoubleOrNull() ?: 0.0
                                else 0.0

                            if (name.isNotBlank() && qty >= 0.0 && sellPrice > 0.0) {
                                val item =
                                    Item(
                                        name = name,
                                        quantity = qty,
                                        unit = unit.ifBlank { "pcs" },
                                        buyPrice = buyPrice,
                                        sellPrice = sellPrice,
                                        lowStockThreshold = threshold,
                                        category = category.ifBlank { "Others" },
                                        hsnCode = hsnCode,
                                        taxRate = taxRate,
                                    )
                                repository.insertItem(item)
                                importedCount++
                            }
                        }
                        line = reader.readLine()
                    }
                }
                withContext(Dispatchers.Main) {
                    if (importedCount > 0) {
                        loadAllData()
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

    /** RFC 4180 compliant: wraps in quotes if value contains comma, quote, or newline */
    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
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

    fun seedDummyData() {
        viewModelScope.launch {
            repository.seedDummyData()
            loadAllData()
        }
    }

    // --- Supplier & Purchase Operations ---
    fun addSupplier(name: String, phone: String?, gstin: String?, address: String?, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertSupplier(Supplier(name = name, phone = phone, gstin = gstin, address = address))
            loadAllData()
            onResult(id)
        }
    }

    fun removeSupplier(id: Long, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.deleteSupplier(id)
            loadAllData()
            onResult(success)
        }
    }

    fun addPurchase(purchase: Purchase, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertPurchase(purchase)
            loadAllData()
            onResult(id)
        }
    }

    fun addSupplierPayment(supplierId: Long, supplierName: String, amount: Double, notes: String?, timestamp: Long = System.currentTimeMillis(), onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertSupplierPayment(supplierId, supplierName, amount, notes, timestamp)
            loadAllData()
            onResult(id)
        }
    }

    fun clearLocalDatabase() {
        viewModelScope.launch {
            repository.clearLocalDatabase()
            loadAllData()
        }
    }

    // --- GST Report Generation ---
    suspend fun generateGSTR1Csv(startTs: Long, endTs: Long): String =
        withContext(Dispatchers.IO) {
            val sales = repository.getSalesByDateRange(startTs, endTs)
            val sb = StringBuilder()
            sb.appendLine("GSTIN of Recipient,Recipient Name,Invoice No,Invoice Date,Invoice Value (₹),Place of Supply,Taxable Value (₹),Rate (%),IGST (₹),CGST (₹),SGST (₹)")
            var inv = 1
            for (sale in sales) {
                val taxable = sale.totalAmount / 1.18 // approximate if no per-item tax stored
                val taxAmt  = sale.totalAmount - taxable
                val cgst    = taxAmt / 2
                val sgst    = taxAmt / 2
                val date    = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(sale.timestamp))
                val gstin   = sale.customerGstin ?: ""
                val cust    = (sale.customerName ?: "Consumer").replace(",", " ")
                sb.appendLine("$gstin,$cust,INV${inv.toString().padStart(4,'0')},$date,${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)},29,${String.format(Locale.getDefault(), "%.2f", taxable)},18,0.00,${String.format(Locale.getDefault(), "%.2f", cgst)},${String.format(Locale.getDefault(), "%.2f", sgst)}")
                inv++
            }
            sb.toString()
        }

    fun exportGSTR1Excel(context: Context, startTs: Long, endTs: Long, fileName: String) {
        viewModelScope.launch {
            val sales = repository.getSalesByDateRange(startTs, endTs)
            val allItemsMap = repository.getAllItemsMap()
            ExcelExporter.exportGstr1(
                context = context,
                fileName = fileName,
                sales = sales,
                businessName = businessName,
                businessGstin = businessGstin,
                allItemsMap = allItemsMap
            )
        }
    }

    fun exportGSTR2Excel(context: Context, startTs: Long, endTs: Long, fileName: String) {
        viewModelScope.launch {
            val purchases = repository.getPurchasesByDateRange(startTs, endTs)
            val suppliersMap = repository.getAllSuppliersMap()
            val allItemsMap = repository.getAllItemsMap()
            ExcelExporter.exportGstr2(
                context = context,
                fileName = fileName,
                purchases = purchases,
                businessName = businessName,
                businessGstin = businessGstin,
                suppliersMap = suppliersMap,
                allItemsMap = allItemsMap
            )
        }
    }

    fun exportGSTR3BExcel(context: Context, startTs: Long, endTs: Long, fileName: String) {
        viewModelScope.launch {
            val sales = repository.getSalesByDateRange(startTs, endTs)
            val purchases = repository.getPurchasesByDateRange(startTs, endTs)
            val suppliersMap = repository.getAllSuppliersMap()
            val allItemsMap = repository.getAllItemsMap()
            ExcelExporter.exportGstr3B(
                context = context,
                fileName = fileName,
                sales = sales,
                purchases = purchases,
                businessName = businessName,
                businessGstin = businessGstin,
                suppliersMap = suppliersMap,
                allItemsMap = allItemsMap
            )
        }
    }

    fun exportGstdetailedExcel(context: Context, startTs: Long, endTs: Long, fileName: String) {
        viewModelScope.launch {
            val sales = repository.getSalesByDateRange(startTs, endTs)
            val purchases = repository.getPurchasesByDateRange(startTs, endTs)
            val suppliersMap = repository.getAllSuppliersMap()
            val allItemsMap = repository.getAllItemsMap()
            ExcelExporter.exportGstDetailed(
                context = context,
                fileName = fileName,
                sales = sales,
                purchases = purchases,
                businessName = businessName,
                businessGstin = businessGstin,
                suppliersMap = suppliersMap,
                allItemsMap = allItemsMap
            )
        }
    }

    // --- Item Batch Operations (Phase 4) ---
    fun addItemBatch(batch: ItemBatch, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertItemBatch(batch)
            loadAllData()
            onResult(id)
        }
    }

    suspend fun createStaffAccount(username: String, pin: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val ownerId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext false
                val json = """
                    {
                        "username": "$username",
                        "password": "$pin",
                        "storeId": "$activeStoreId",
                        "ownerId": "$ownerId"
                    }
                """.trimIndent()

                val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                val request = Request.Builder()
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
