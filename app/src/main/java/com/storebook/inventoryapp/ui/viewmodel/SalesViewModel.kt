package com.storebook.inventoryapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.shared.domain.repository.SalesRepository
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.repository.UdhaarRepository
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.shared.domain.models.CustomerBalance
import com.storebook.inventoryapp.shared.domain.models.UdhaarEntry
import com.storebook.inventoryapp.shared.domain.models.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SalesViewModel(
    private val salesRepository: SalesRepository,
    private val inventoryRepository: InventoryRepository,
    private val udhaarRepository: UdhaarRepository,
    private val context: android.content.Context
) : ViewModel() {
    
    private val prefs = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(context)

    private fun triggerSync() {
        val storeId = prefs.getString("active_store_id", "default_store") ?: "default_store"
        val data = androidx.work.Data.Builder().putString("STORE_ID", storeId).build()
        val request = androidx.work.OneTimeWorkRequestBuilder<com.storebook.inventoryapp.data.sync.SyncWorker>().setInputData(data).build()
        androidx.work.WorkManager.getInstance(context).enqueue(request)
    }

    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _allItems

    private val _udhaarBalances = MutableStateFlow<List<CustomerBalance>>(emptyList())
    val udhaarBalances: StateFlow<List<CustomerBalance>> = _udhaarBalances

    private val _customerSuggestions = MutableStateFlow<List<String>>(emptyList())
    val customerSuggestions: StateFlow<List<String>> = _customerSuggestions

    private val _salesHistoryList = MutableStateFlow<List<Sale>>(emptyList())
    val salesHistoryList: StateFlow<List<Sale>> = _salesHistoryList

    // Cart state
    var cartItems by mutableStateOf<List<CartItem>>(emptyList())
    var cartDiscount by mutableStateOf(0.0)
    var cartCustomerName by mutableStateOf("")
    var cartCustomerAddress by mutableStateOf("")
    var cartCustomerGstin by mutableStateOf("")
    var cartPaymentMode by mutableStateOf("Cash")

    var lastSaleId by mutableStateOf<Long?>(null)
    var lastSaleTime by mutableStateOf(0L)

    var businessGstin by mutableStateOf("") // from prefs if needed
    var isHapticFeedbackEnabled by mutableStateOf(true)

    fun loadAllData(force: Boolean = true) {
        viewModelScope.launch {
            _allItems.value = inventoryRepository.getActiveItems().map { i ->
                Item(id = i.id, name = i.name, quantity = i.quantity, unit = i.unit, buyPrice = i.buy_price, sellPrice = i.sell_price, lowStockThreshold = i.low_stock_threshold, category = i.category)
            }
            val balances = udhaarRepository.getUdhaarBalances()
            _udhaarBalances.value = balances
            _customerSuggestions.value = balances.map { it.customerName }.distinct()
        }
    }

    fun clearLastSaleId() {
        lastSaleId = null
    }

    fun clearCart() {
        cartItems = emptyList()
        cartDiscount = 0.0
        cartCustomerName = ""
        cartCustomerAddress = ""
        cartCustomerGstin = ""
    }

    fun addToCart(item: Item, quantity: Double = 1.0) {
        val existing = cartItems.find { it.item.id == item.id }
        if (existing != null) {
            cartItems = cartItems.map { if (it.item.id == item.id) it.copy(quantity = it.quantity + quantity) else it }
        } else {
            cartItems = cartItems + CartItem(item, quantity)
        }
    }

    fun updateCartQty(item: Item, qty: Double) {
        if (qty <= 0.0) {
            cartItems = cartItems.filter { it.item.id != item.id }
        } else {
            cartItems = cartItems.map { if (it.item.id == item.id) it.copy(quantity = qty) else it }
        }
    }

    fun changeCartQtyRelative(item: Item, step: Double) {
        val existing = cartItems.find { it.item.id == item.id }
        val current = existing?.quantity ?: 0.0
        updateCartQty(item, current + step)
    }

    fun updateCustomerSearch(name: String) {
        cartCustomerName = name
    }

    fun selectCustomer(name: String) {
        cartCustomerName = name
    }

    private var isCheckoutProcessing = false

    fun checkout(paymentMode: String, type: String, onResult: (Long, Double) -> Unit) {
        if (isCheckoutProcessing || cartItems.isEmpty()) return
        isCheckoutProcessing = true
        viewModelScope.launch {
            val total = cartItems.sumOf { it.item.sellPrice * it.quantity } - cartDiscount
            val saleId = salesRepository.insertSale(
                totalAmount = total,
                discountAmount = cartDiscount,
                customerName = cartCustomerName.ifBlank { "Cash / Anonymous" },
                customerGstin = cartCustomerGstin,
                businessGstin = "",
                customerAddress = cartCustomerAddress,
                businessAddress = "",
                type = type,
                notes = ""
            )
            cartItems.forEach { cartItem ->
                salesRepository.insertSaleItem(saleId, cartItem.item.id, cartItem.item.name, cartItem.item.unit, cartItem.quantity, cartItem.item.buyPrice, cartItem.item.sellPrice)
                inventoryRepository.updateItemStock(cartItem.item.id, -cartItem.quantity)
            }

            lastSaleId = saleId
            lastSaleTime = System.currentTimeMillis()
            
            // if Udhaar, log entry
            if (paymentMode == "Udhaar" && type == "SALE") {
                udhaarRepository.insertUdhaar(
                    customerName = cartCustomerName.trim(),
                    amount = total,
                    type = "CREDIT",
                    notes = "Credit Sale #$saleId"
                )
            }
            
            clearCart()
            triggerSync()
            isCheckoutProcessing = false
            onResult(saleId, total)
        }
    }

    fun undoLastSale(onComplete: () -> Unit) {
        viewModelScope.launch {
            lastSaleId?.let { id ->
                val items = salesRepository.getSaleItems(id)
                salesRepository.softDeleteSale(id)
                items.forEach { item ->
                    inventoryRepository.updateItemStock(item.item_id, item.quantity)
                }
                lastSaleId = null
                lastSaleTime = 0L
                loadAllData(true)
                triggerSync()
            }
            onComplete()
        }
    }

    fun loadSalesHistory(startTs: Long, endTs: Long) {
        viewModelScope.launch {
            _salesHistoryList.value = salesRepository.getSalesByDateRange(startTs, endTs).map { s ->
                Sale(id = s.id, timestamp = s.timestamp, totalAmount = s.total_amount, discountAmount = s.discount_amount, customerName = s.customer_name, customerGstin = s.customer_gstin, businessGstin = s.business_gstin, customerAddress = s.customer_address, businessAddress = s.business_address, type = s.type, notes = s.notes, items = emptyList())
            }
        }
    }

    suspend fun getSalesWithItems(limit: Long, offset: Long): List<Sale> {
        val rawSales = salesRepository.getAllSales().drop(offset.toInt()).take(limit.toInt())
        return rawSales.map { s ->
            val items = salesRepository.getSaleItems(s.id).map { saleItem ->
                com.storebook.inventoryapp.shared.domain.models.SaleItemDetail(itemId = saleItem.item_id, itemName = saleItem.item_name, quantity = saleItem.quantity, unit = saleItem.unit, buyPrice = saleItem.buy_price, sellPrice = saleItem.sell_price)
            }
            Sale(id = s.id, timestamp = s.timestamp, totalAmount = s.total_amount, discountAmount = s.discount_amount, customerName = s.customer_name, customerGstin = s.customer_gstin, businessGstin = s.business_gstin, customerAddress = s.customer_address, businessAddress = s.business_address, type = s.type, notes = s.notes, isConverted = (s.is_converted == 1L), items = items)
        }
    }

    // E03-S4: Atomically convert quotation to sale — preserves all line items + prices exactly
    fun convertQuotation(saleId: Long, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val newSaleId = salesRepository.convertQuotationToSale(saleId)
            if (newSaleId > 0) {
                triggerSync()
                // Reload data to reflect the new sale + converted status
                _salesHistoryList.value = getSalesWithItems(100, 0)
            }
            onComplete(newSaleId)
        }
    }
    fun shareInvoice(context: android.content.Context, saleId: Long) {
        val businessName = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(context).getString("business_name", "Store") ?: "Store"
        val businessAddress = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(context).getString("business_address", "") ?: ""
        val businessGstin = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(context).getString("business_gstin", "") ?: ""
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val saleObj = salesRepository.getSaleById(saleId)
            if (saleObj != null) {
                val items = salesRepository.getSaleItems(saleId).map { saleItem ->
                    com.storebook.inventoryapp.shared.domain.models.SaleItemDetail(itemId = saleItem.item_id, itemName = saleItem.item_name, quantity = saleItem.quantity, unit = saleItem.unit, buyPrice = saleItem.buy_price, sellPrice = saleItem.sell_price)
                }
                val sale = Sale(id = saleObj.id, timestamp = saleObj.timestamp, totalAmount = saleObj.total_amount, discountAmount = saleObj.discount_amount, customerName = saleObj.customer_name, customerGstin = saleObj.customer_gstin, businessGstin = saleObj.business_gstin, customerAddress = saleObj.customer_address, businessAddress = saleObj.business_address, type = saleObj.type, notes = saleObj.notes, isConverted = (saleObj.is_converted == 1L), items = items)
                
                val mappedCartItems = sale.items.map { saleItem ->
                    val actualItemObj = inventoryRepository.getItemById(saleItem.itemId)
                    val actualItem = actualItemObj?.let { Item(id = it.id, name = it.name, quantity = it.quantity, unit = it.unit, buyPrice = it.buy_price, sellPrice = it.sell_price, lowStockThreshold = it.low_stock_threshold, category = it.category) } ?: Item(id = saleItem.itemId, name = saleItem.itemName, quantity = 0.0, unit = saleItem.unit, buyPrice = saleItem.buyPrice, sellPrice = saleItem.sellPrice, lowStockThreshold = 0.0, category = "")
                    CartItem(item = actualItem, quantity = saleItem.quantity)
                }
                val file = com.storebook.inventoryapp.utils.InvoicePdfGenerator.generateInvoicePdf(context, sale, mappedCartItems, businessName, businessAddress, businessGstin)
                if (file != null) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share Invoice"))
                    }
                }
            }
        }
    }

    // E03-S1 — expose the price-drift audit report from the repository
    suspend fun getPriceDriftReport(): List<com.storebook.inventoryapp.shared.data.local.GeneratePriceDriftReport> {
        return salesRepository.getPriceDriftReport()
    }
}
