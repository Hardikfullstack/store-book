package com.storebook.inventoryapp.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.data.play.PlayBillingManager
import com.storebook.inventoryapp.data.repository.CartItem
import com.storebook.inventoryapp.data.repository.CustomerBalance
import com.storebook.inventoryapp.data.repository.ExpenseEntry
import com.storebook.inventoryapp.data.repository.Item
import com.storebook.inventoryapp.data.repository.Sale
import com.storebook.inventoryapp.data.repository.StoreBookRepository
import com.storebook.inventoryapp.data.repository.UdhaarEntry
import com.storebook.inventoryapp.data.sync.FirestoreSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreBookViewModel(
        application: Application,
) : AndroidViewModel(application) {
    private val repository = StoreBookRepository(application.applicationContext)
    val billingManager = PlayBillingManager(application.applicationContext)
    private val syncManager = FirestoreSyncManager(application.applicationContext)

    // Data Flows
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _allItems

    private val _filteredItems = MutableStateFlow<List<Item>>(emptyList())
    val filteredItems: StateFlow<List<Item>> = _filteredItems

    private val _isLoadingItems = MutableStateFlow(false)
    val isLoadingItems: StateFlow<Boolean> = _isLoadingItems

    private val _lowStockItems = MutableStateFlow<List<Item>>(emptyList())
    val lowStockItems: StateFlow<List<Item>> = _lowStockItems

    private val _salesList = MutableStateFlow<List<Sale>>(emptyList())
    val salesList: StateFlow<List<Sale>> = _salesList

    private val _salesHistoryList = MutableStateFlow<List<Sale>>(emptyList())
    val salesHistoryList: StateFlow<List<Sale>> = _salesHistoryList

    private val _udhaarBalances = MutableStateFlow<List<CustomerBalance>>(emptyList())
    val udhaarBalances: StateFlow<List<CustomerBalance>> = _udhaarBalances

    private val _customerSuggestions = MutableStateFlow<List<String>>(emptyList())
    val customerSuggestions: StateFlow<List<String>> = _customerSuggestions

    private var customerSearchJob: kotlinx.coroutines.Job? = null

    fun updateCustomerSearch(query: String) {
        customerSearchJob?.cancel()
        customerSearchJob =
                viewModelScope.launch {
                    kotlinx.coroutines.delay(200) // debounce
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

    // Cart / Checkout State
    var cartItems by mutableStateOf<List<CartItem>>(emptyList())
        private set

    var cartDiscount by mutableStateOf(0.0)
    var cartCustomerName by mutableStateOf("")
    var cartCustomerGstin by mutableStateOf("")
    var cartCustomerAddress by mutableStateOf("")
    var cartNotes by mutableStateOf("")
    var cartPaymentMode by mutableStateOf("Cash")

    private val prefs =
            application.getSharedPreferences(
                    "storebook_prefs",
                    android.content.Context.MODE_PRIVATE
            )
    var businessName by
            mutableStateOf(
                    prefs.getString("business_name", "StoreBook Kirana") ?: "StoreBook Kirana"
            )
        private set
    var businessGstin by mutableStateOf(prefs.getString("business_gstin", "") ?: "")
        private set
    var businessAddress by mutableStateOf(prefs.getString("business_address", "") ?: "")
        private set

    fun updateBusinessName(name: String) {
        businessName = name
        prefs.edit().putString("business_name", name).apply()
    }

    fun updateBusinessGstin(gstin: String) {
        businessGstin = gstin
        prefs.edit().putString("business_gstin", gstin).apply()
    }

    fun updateBusinessAddress(address: String) {
        businessAddress = address
        prefs.edit().putString("business_address", address).apply()
    }

    // Billing state (reactive from Play Billing client)
    var isPremiumUser: Boolean by mutableStateOf(false)

    // Undo mechanism
    var lastSaleId by mutableStateOf<Long?>(null)
        private set
    var lastSaleTime by mutableStateOf(0L)
        private set

    init {
        viewModelScope.launch {
            repository.standardizeCustomerNames()
            loadAllData()
        }
        // Observe billing state changes
        viewModelScope.launch {
            billingManager.state.collect { billingState ->
                isPremiumUser = billingState.isProUnlocked
                if (isPremiumUser) {
                    triggerSync()
                }
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            if (isPremiumUser) {
                syncManager.syncAllData()
            }
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            val items = repository.getActiveItems()
            _allItems.value = items
            _filteredItems.value = items
            _lowStockItems.value = items.filter { it.quantity <= it.lowStockThreshold }
            _salesList.value = repository.getSalesPage(limit = 50, offset = 0)
            _udhaarBalances.value = repository.getUdhaarBalances()
            _customerSuggestions.value = repository.searchCustomers("", 50)
            _expensesList.value = repository.getExpenses()

            // Automatically attempt sync after updating data
            triggerSync()
        }
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
            _filteredItems.value =
                    repository.getActiveItemsFiltered(
                            search = search,
                            category = category,
                            sortBy = sortBy
                    )
            _isLoadingItems.value = false
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

    fun loadSalesHistory(
            startTs: Long,
            endTs: Long,
    ) {
        viewModelScope.launch {
            _salesHistoryList.value = repository.getSalesByDateRange(startTs, endTs)
        }
    }

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
            repository.insertItem(item)
            loadAllData()
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
        val existing = cartItems.find { it.item.id == item.id }
        if (existing != null) {
            cartItems =
                    cartItems.map { ci ->
                        if (ci.item.id == item.id) ci.copy(quantity = ci.quantity + qty) else ci
                    }
        } else {
            cartItems = cartItems + CartItem(item, qty)
        }
    }

    fun updateCartQty(
            item: Item,
            qty: Double,
    ) {
        val existing = cartItems.find { it.item.id == item.id }
        if (existing != null) {
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
    }

    // --- Sales Actions ---

    private fun String.formatName(): String =
            this.trim().split(Regex("\\s+")).joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

    fun checkout(
            paymentMode: String = cartPaymentMode,
            onSuccess: (Long, Double) -> Unit,
    ) {
        if (cartItems.isEmpty()) return
        val customerNameForSale = cartCustomerName.takeIf { it.isNotBlank() }?.formatName()
        val notesForSale = cartNotes.trim().takeIf { it.isNotBlank() }

        viewModelScope.launch {
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
                    )
            if (saleId != -1L) {
                lastSaleId = saleId
                lastSaleTime = System.currentTimeMillis()

                // Calculate grand total with taxes
                val taxSummary =
                        com.storebook.inventoryapp.data.billing.BillingEngine.calculateInvoiceTaxes(
                                cartItems = cartItems,
                                totalDiscount = cartDiscount,
                                businessGstin = businessGstin.takeIf { it.isNotBlank() },
                                customerGstin = cartCustomerGstin.takeIf { it.isNotBlank() },
                        )
                val total = taxSummary.grandTotal

                // NOTE: recordSale() ALREADY creates Udhaar CREDIT entry if customerName is present
                // So we do NOT need to create it here again. Removing the duplicate insert.

                clearCart()
                loadAllData()
                onSuccess(saleId, total)
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
            context: android.content.Context,
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
                        com.storebook.inventoryapp.utils.InvoicePdfGenerator.generateInvoicePdf(
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
                                androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file,
                                )
                        val intent =
                                android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                        context.startActivity(
                                android.content.Intent.createChooser(intent, "Share Invoice")
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                                        context,
                                        "Failed to generate PDF",
                                        android.widget.Toast.LENGTH_SHORT,
                                )
                                .show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                                    context,
                                    "Sale not found",
                                    android.widget.Toast.LENGTH_SHORT
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

                            if (name.isNotBlank()) {
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
}
