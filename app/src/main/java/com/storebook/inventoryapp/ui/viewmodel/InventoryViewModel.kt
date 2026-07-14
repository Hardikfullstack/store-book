package com.storebook.inventoryapp.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.ItemBatch
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.shared.domain.models.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.storebook.inventoryapp.utils.SecurityUtils
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import com.storebook.inventoryapp.data.sync.SyncWorker

import com.storebook.inventoryapp.shared.domain.repository.SupplierRepository
import com.storebook.inventoryapp.shared.domain.repository.PurchaseRepository
import com.storebook.inventoryapp.shared.domain.repository.BatchRepository

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val supplierRepository: SupplierRepository,
    private val purchaseRepository: PurchaseRepository,
    private val batchRepository: BatchRepository,
    private val context: Context
) : ViewModel() {

    private val prefs = SecurityUtils.getEncryptedPrefs(context)

    var userRole: String by mutableStateOf(prefs.getString("user_role", "owner") ?: "owner")
    private var _lastRestockSupplierName by mutableStateOf(prefs.getString("last_restock_supplier", "") ?: "")
    var lastRestockSupplierName: String
        get() = _lastRestockSupplierName
        set(value) {
            _lastRestockSupplierName = value
            prefs.edit().putString("last_restock_supplier", value).apply()
        }

    private val _filteredItems = MutableStateFlow<List<Item>>(emptyList())
    val filteredItems: StateFlow<List<Item>> = _filteredItems

    private val _nearExpiryItems = MutableStateFlow<List<Item>>(emptyList())
    val nearExpiryItems: StateFlow<List<Item>> = _nearExpiryItems

    private val _isLoadingItems = MutableStateFlow(false)
    val isLoadingItems: StateFlow<Boolean> = _isLoadingItems

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers

    private var currentSearch = ""
    private var currentCategory = "All"
    private var currentSortBy = "Name"

    private fun triggerSync() {
        val storeId = prefs.getString("active_store_id", "default_store") ?: "default_store"
        val data = Data.Builder().putString("STORE_ID", storeId).build()
        val request = OneTimeWorkRequestBuilder<SyncWorker>().setInputData(data).build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun loadFilteredItems(search: String = currentSearch, category: String = currentCategory, sortBy: String = currentSortBy) {
        currentSearch = search
        currentCategory = category
        currentSortBy = sortBy

        viewModelScope.launch {
            _isLoadingItems.value = true
            try {
                _filteredItems.value = inventoryRepository.getActiveItemsFiltered(search, category, sortBy.lowercase(), 50, 0).map { i ->
                    Item(id = i.id, name = i.name, quantity = i.quantity, unit = i.unit, buyPrice = i.buy_price, sellPrice = i.sell_price, lowStockThreshold = i.low_stock_threshold, category = i.category, hsnCode = i.hsn_code, taxRate = i.tax_rate, photoPath = i.photo_path)
                }
                _suppliers.value = supplierRepository.getAllSuppliers().map { s ->
                    Supplier(id = s.id, name = s.name, phone = s.phone, gstin = s.gstin, address = s.address)
                }
            } finally {
                _isLoadingItems.value = false
            }
        }
    }

    fun loadMoreItems(search: String = "", category: String = "All", sortBy: String = "Name", currentSize: Int, pageSize: Int = 50, onResult: (List<Item>) -> Unit = {}) {
        viewModelScope.launch {
            val more = inventoryRepository.getActiveItemsFiltered(search, category, sortBy, pageSize.toLong(), currentSize.toLong()).map { i ->
                Item(id = i.id, name = i.name, quantity = i.quantity, unit = i.unit, buyPrice = i.buy_price, sellPrice = i.sell_price, lowStockThreshold = i.low_stock_threshold, category = i.category, hsnCode = i.hsn_code, taxRate = i.tax_rate, photoPath = i.photo_path)
            }
            onResult(more)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            inventoryRepository.softDeleteItem(id)
            loadFilteredItems()
            triggerSync()
        }
    }

    fun addSupplier(name: String, phone: String?, gstin: String?, address: String?, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = supplierRepository.insertSupplier(name, phone, gstin, address)
            _suppliers.value = supplierRepository.getAllSuppliers().map { s ->
                Supplier(id = s.id, name = s.name, phone = s.phone, gstin = s.gstin, address = s.address)
            }
            onResult(id)
        }
    }

    fun addPurchase(purchase: Purchase, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = purchaseRepository.insertPurchase(purchase.supplierId, purchase.supplierName, purchase.totalAmount, purchase.taxAmount, purchase.type, purchase.notes)
            purchase.items.forEach { item ->
                purchaseRepository.insertPurchaseItem(id, item.itemId, item.itemName, item.quantity, item.unit, item.buyPrice)
                inventoryRepository.updateItemStock(item.itemId, item.quantity)
            }
            loadFilteredItems()
            triggerSync()
            onResult(id)
        }
    }

    fun addItemBatch(batch: ItemBatch, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = batchRepository.insertItemBatch(batch.itemId, batch.batchNumber, batch.expiryDate, batch.quantity, batch.costPrice, batch.notes)
            onResult(id)
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
        onResult: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val item = Item(
                id = 0L,
                name = name,
                quantity = quantity,
                unit = unit,
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                lowStockThreshold = threshold,
                category = category,
                photoPath = null,
                hsnCode = hsnCode,
                taxRate = taxRate
            )
            val id = inventoryRepository.insertItem(
                name = item.name, quantity = item.quantity, unit = item.unit,
                buyPrice = item.buyPrice, sellPrice = item.sellPrice,
                threshold = item.lowStockThreshold, category = item.category,
                photoPath = item.photoPath, hsnCode = item.hsnCode, taxRate = item.taxRate
            ) ?: return@launch
            triggerSync()
            onResult(id)
        }
    }

    fun restockItem(itemId: Long, quantityToAdd: Double, costPrice: Double, supplierName: String?, supplierPhone: String?) {
        viewModelScope.launch {
            inventoryRepository.updateItemStock(itemId, quantityToAdd)
            loadFilteredItems()
            triggerSync()
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
        photoPath: String? = null
    ) {
        viewModelScope.launch {
            val item = Item(
                id = id,
                name = name,
                quantity = quantity,
                unit = unit,
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                lowStockThreshold = threshold,
                category = category,
                photoPath = photoPath,
                hsnCode = hsnCode,
                taxRate = taxRate
            )
            inventoryRepository.updateItem(
                id = item.id, name = item.name, quantity = item.quantity, unit = item.unit, 
                buyPrice = item.buyPrice, sellPrice = item.sellPrice, 
                threshold = item.lowStockThreshold, category = item.category, 
                photoPath = item.photoPath, hsnCode = item.hsnCode, taxRate = item.taxRate
            )
            loadFilteredItems()
            triggerSync()
        }
    }
}
