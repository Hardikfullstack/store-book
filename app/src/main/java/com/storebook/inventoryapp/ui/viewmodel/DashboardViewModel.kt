package com.storebook.inventoryapp.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.repository.SalesRepository
import com.storebook.inventoryapp.shared.domain.repository.PurchaseRepository
import com.storebook.inventoryapp.shared.domain.repository.SupplierRepository
import com.storebook.inventoryapp.shared.domain.repository.ExpenseRepository
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.ExpenseEntry
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.ui.viewmodels.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.storebook.inventoryapp.utils.SecurityUtils

class DashboardViewModel(
    private val inventoryRepository: InventoryRepository,
    private val salesRepository: SalesRepository,
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val expenseRepository: ExpenseRepository,
    context: Context
) : ViewModel() {
    private val prefs = SecurityUtils.getEncryptedPrefs(context)

    var userRole: String by mutableStateOf(prefs.getString("user_role", "owner") ?: "owner")
    var userRoleType: UserRole by mutableStateOf(UserRole.fromString(userRole))
    var isPremiumUser by mutableStateOf(false) // Simplified for facade

    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _allItems

    private val _lowStockItems = MutableStateFlow<List<Item>>(emptyList())
    val lowStockItems: StateFlow<List<Item>> = _lowStockItems

    private val _salesList = MutableStateFlow<List<Sale>>(emptyList())
    val salesList: StateFlow<List<Sale>> = _salesList

    private val _expensesList = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expensesList: StateFlow<List<ExpenseEntry>> = _expensesList

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _allItems.value = inventoryRepository.getActiveItems().map { i ->
                Item(id = i.id, name = i.name, quantity = i.quantity, unit = i.unit, buyPrice = i.buy_price, sellPrice = i.sell_price, lowStockThreshold = i.low_stock_threshold, category = i.category)
            }
            val items = _allItems.value
            _lowStockItems.value = items.filter { it.quantity <= it.lowStockThreshold }
            _salesList.value = salesRepository.getAllSales().map { s ->
                Sale(id = s.id, timestamp = s.timestamp, totalAmount = s.total_amount, discountAmount = s.discount_amount, customerName = s.customer_name, customerGstin = s.customer_gstin, businessGstin = s.business_gstin, customerAddress = s.customer_address, businessAddress = s.business_address, type = s.type, notes = s.notes, items = emptyList())
            }
            _expensesList.value = expenseRepository.getAllExpenses()
            _purchases.value = purchaseRepository.getAllPurchases().map { p ->
                Purchase(id = p.id, supplierId = p.supplier_id, supplierName = p.supplier_name, totalAmount = p.total_amount, taxAmount = p.tax_amount, type = p.type, timestamp = p.timestamp, notes = p.notes, items = emptyList())
            }
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch {
            inventoryRepository.updateItem(
                id = item.id, name = item.name, quantity = item.quantity, unit = item.unit, 
                buyPrice = item.buyPrice, sellPrice = item.sellPrice, 
                threshold = item.lowStockThreshold, category = item.category, 
                photoPath = item.photoPath, hsnCode = item.hsnCode, taxRate = item.taxRate
            )
            loadAllData()
        }
    }

    val businessName: String
        get() = prefs.getString("business_name", "Store") ?: "Store"

    val businessGstin: String
        get() = prefs.getString("business_gstin", "") ?: ""

    fun exportGSTR1Excel(context: Context, startTs: Long, endTs: Long, fileName: String) {
        viewModelScope.launch {
            val sales = salesRepository.getSalesByDateRange(startTs, endTs).map { s ->
                Sale(id = s.id, timestamp = s.timestamp, totalAmount = s.total_amount, discountAmount = s.discount_amount, customerName = s.customer_name, customerGstin = s.customer_gstin, businessGstin = s.business_gstin, customerAddress = s.customer_address, businessAddress = s.business_address, type = s.type, notes = s.notes, items = emptyList())
            }
            val allItemsMap = inventoryRepository.getActiveItems().associate { it.id to Item(id = it.id, name = it.name, quantity = it.quantity, unit = it.unit, buyPrice = it.buy_price, sellPrice = it.sell_price, lowStockThreshold = it.low_stock_threshold, category = it.category) }
            com.storebook.inventoryapp.utils.ExcelExporter.exportGstr1(
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
            val purchases = purchaseRepository.getPurchasesByDateRange(startTs, endTs).map { p ->
                Purchase(id = p.id, supplierId = p.supplier_id, supplierName = p.supplier_name, totalAmount = p.total_amount, taxAmount = p.tax_amount, type = p.type, timestamp = p.timestamp, notes = p.notes, items = emptyList())
            }
            val suppliersMap = supplierRepository.getAllSuppliers().associate { it.id to com.storebook.inventoryapp.shared.domain.models.Supplier(id = it.id, name = it.name, phone = it.phone, gstin = it.gstin, address = it.address) }
            val allItemsMap = inventoryRepository.getActiveItems().associate { it.id to Item(id = it.id, name = it.name, quantity = it.quantity, unit = it.unit, buyPrice = it.buy_price, sellPrice = it.sell_price, lowStockThreshold = it.low_stock_threshold, category = it.category) }
            com.storebook.inventoryapp.utils.ExcelExporter.exportGstr2(
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
            val sales = salesRepository.getSalesByDateRange(startTs, endTs).map { s ->
                Sale(id = s.id, timestamp = s.timestamp, totalAmount = s.total_amount, discountAmount = s.discount_amount, customerName = s.customer_name, customerGstin = s.customer_gstin, businessGstin = s.business_gstin, customerAddress = s.customer_address, businessAddress = s.business_address, type = s.type, notes = s.notes, items = emptyList())
            }
            val purchases = purchaseRepository.getPurchasesByDateRange(startTs, endTs).map { p ->
                Purchase(id = p.id, supplierId = p.supplier_id, supplierName = p.supplier_name, totalAmount = p.total_amount, taxAmount = p.tax_amount, type = p.type, timestamp = p.timestamp, notes = p.notes, items = emptyList())
            }
            val suppliersMap = supplierRepository.getAllSuppliers().associate { it.id to com.storebook.inventoryapp.shared.domain.models.Supplier(id = it.id, name = it.name, phone = it.phone, gstin = it.gstin, address = it.address) }
            val allItemsMap = inventoryRepository.getActiveItems().associate { it.id to Item(id = it.id, name = it.name, quantity = it.quantity, unit = it.unit, buyPrice = it.buy_price, sellPrice = it.sell_price, lowStockThreshold = it.low_stock_threshold, category = it.category) }
            com.storebook.inventoryapp.utils.ExcelExporter.exportGstr3B(
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
            val sales = salesRepository.getSalesByDateRange(startTs, endTs).map { s ->
                Sale(id = s.id, timestamp = s.timestamp, totalAmount = s.total_amount, discountAmount = s.discount_amount, customerName = s.customer_name, customerGstin = s.customer_gstin, businessGstin = s.business_gstin, customerAddress = s.customer_address, businessAddress = s.business_address, type = s.type, notes = s.notes, items = emptyList())
            }
            val purchases = purchaseRepository.getPurchasesByDateRange(startTs, endTs).map { p ->
                Purchase(id = p.id, supplierId = p.supplier_id, supplierName = p.supplier_name, totalAmount = p.total_amount, taxAmount = p.tax_amount, type = p.type, timestamp = p.timestamp, notes = p.notes, items = emptyList())
            }
            val suppliersMap = supplierRepository.getAllSuppliers().associate { it.id to com.storebook.inventoryapp.shared.domain.models.Supplier(id = it.id, name = it.name, phone = it.phone, gstin = it.gstin, address = it.address) }
            val allItemsMap = inventoryRepository.getActiveItems().associate { it.id to Item(id = it.id, name = it.name, quantity = it.quantity, unit = it.unit, buyPrice = it.buy_price, sellPrice = it.sell_price, lowStockThreshold = it.low_stock_threshold, category = it.category) }
            com.storebook.inventoryapp.utils.ExcelExporter.exportGstDetailed(
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

    suspend fun getSalesByDateRange(startTs: Long, endTs: Long): List<Sale> {
        return salesRepository.getSalesByDateRange(startTs, endTs).map { s ->
            Sale(id = s.id, timestamp = s.timestamp, totalAmount = s.total_amount, discountAmount = s.discount_amount, customerName = s.customer_name, customerGstin = s.customer_gstin, businessGstin = s.business_gstin, customerAddress = s.customer_address, businessAddress = s.business_address, type = s.type, notes = s.notes, items = emptyList())
        }
    }

    suspend fun getPurchasesByDateRange(startTs: Long, endTs: Long): List<Purchase> {
        return purchaseRepository.getPurchasesByDateRange(startTs, endTs).map { p ->
            Purchase(id = p.id, supplierId = p.supplier_id, supplierName = p.supplier_name, totalAmount = p.total_amount, taxAmount = p.tax_amount, type = p.type, timestamp = p.timestamp, notes = p.notes, items = emptyList())
        }
    }

    suspend fun getAllSuppliersMap(): Map<Long, com.storebook.inventoryapp.shared.domain.models.Supplier> {
        return supplierRepository.getAllSuppliers().associate { it.id to com.storebook.inventoryapp.shared.domain.models.Supplier(id = it.id, name = it.name, phone = it.phone, gstin = it.gstin, address = it.address) }
    }

    suspend fun getAllItemsMap(): Map<Long, Item> {
        return inventoryRepository.getActiveItems().associate { it.id to Item(id = it.id, name = it.name, quantity = it.quantity, unit = it.unit, buyPrice = it.buy_price, sellPrice = it.sell_price, lowStockThreshold = it.low_stock_threshold, category = it.category) }
    }
}
