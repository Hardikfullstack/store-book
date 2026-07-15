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
import com.storebook.inventoryapp.shared.domain.repository.SyncRepository
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.ExpenseEntry
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.ui.viewmodels.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import com.storebook.inventoryapp.utils.SecurityUtils
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class DashboardViewModel(
    private val inventoryRepository: InventoryRepository,
    private val salesRepository: SalesRepository,
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val expenseRepository: ExpenseRepository,
    private val syncRepository: SyncRepository,
    private val context: Context
) : ViewModel() {
    private val prefs = SecurityUtils.getEncryptedPrefs(context)

    var userRole: String by mutableStateOf(prefs.getString("user_role", "owner") ?: "owner")
    var userRoleType: UserRole by mutableStateOf(UserRole.fromString(userRole))
    var isPremiumUser by mutableStateOf(prefs.getBoolean("is_premium", false))

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

    // ==========================================================================
    // E01-S4: Sync Status — observable sync state for UI badge + retry trigger
    // ==========================================================================
    data class UiSyncStatus(
        val status: String,              // IDLE, PUSHING, PULLING, DONE, FAILED, DONE_WITH_WARNINGS
        val lastSyncAt: Long,            // timestamp of last full sync
        val failedCount: Int,           // count of permanently failed mutations
        val isSyncing: Boolean          // true when status == PUSHING or PULLING
    ) { companion object { val initial = UiSyncStatus("IDLE", 0L, 0, false) } }

    private val _uiSyncStatus = MutableStateFlow(UiSyncStatus.initial)
    val uiSyncStatus: StateFlow<UiSyncStatus> = _uiSyncStatus

    // E11-S5: Error StateFlow for per-VM toast/Snackbar error feedback
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ==========================================================================
    // E03-S3: Today's Snapshot — aggregate from sale_items price snapshots (accurate profit)
    // Uses sell_price/buy_price captured AT TIME OF SALE, not current item prices
    // ==========================================================================
    data class TodaySnapshot(
        val todayRevenue: Double = 0.0,
        val todayCOG: Double = 0.0,
        val todayExpenses: Double = 0.0,
        val todayProfit: Double = 0.0
    ) { companion object { val initial = TodaySnapshot() } }

    private val _todaySnapshot = MutableStateFlow(TodaySnapshot.initial)
    val todaySnapshot: StateFlow<TodaySnapshot> = _todaySnapshot

    val last7DaysData: StateFlow<Triple<List<Double>, List<Double>, List<Double>>> = 
        combine(salesList, purchases, expensesList) { sales, purcs, expenses ->
            val format = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
            val days = (0..6).map { offset ->
                val cal = java.util.Calendar.getInstance()
                cal.add(java.util.Calendar.DAY_OF_YEAR, -offset)
                format.format(cal.time)
            }.reversed()

            val salesPerDay = days.associateWith { dayStr ->
                sales.filter { format.format(java.util.Date(it.timestamp)) == dayStr }.sumOf { it.totalAmount }
            }
            val purchasesPerDay = days.associateWith { dayStr ->
                purcs.filter { format.format(java.util.Date(it.timestamp)) == dayStr }.sumOf { it.totalAmount }
            }
            val expensesPerDay = days.associateWith { dayStr ->
                expenses.filter { format.format(java.util.Date(it.timestamp)) == dayStr }.sumOf { it.amount }
            }
            Triple(
                days.map { salesPerDay[it] ?: 0.0 },
                days.map { purchasesPerDay[it] ?: 0.0 },
                days.map { expensesPerDay[it] ?: 0.0 }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple<List<Double>, List<Double>, List<Double>>(emptyList(), emptyList(), emptyList()))

    init {
        loadAllData()
        refreshSyncStatus()
        loadTodaySnapshot()

    }

    fun loadAllData() {
        isPremiumUser = prefs.getBoolean("is_premium", false)
        viewModelScope.launch {
            _allItems.value = inventoryRepository.getActiveItems().map { i ->
                Item(id = i.id, name = i.name, quantity = i.quantity, unit = i.unit, buyPrice = i.buy_price, sellPrice = i.sell_price, lowStockThreshold = i.low_stock_threshold, category = i.category)
            }
            val items = _allItems.value
            _lowStockItems.value = items.filter { it.quantity <= it.lowStockThreshold }
            val todayStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
            _salesList.value = salesRepository.getAllSales().map { s ->
                val sDateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date(s.timestamp))
                val items = if (sDateStr == todayStr) {
                    salesRepository.getSaleItems(s.id).map { saleItem ->
                        com.storebook.inventoryapp.shared.domain.models.SaleItemDetail(
                            itemId = saleItem.item_id, itemName = saleItem.item_name, quantity = saleItem.quantity, unit = saleItem.unit, buyPrice = saleItem.buy_price, sellPrice = saleItem.sell_price
                        )
                    }
                } else emptyList()
                Sale(id = s.id, timestamp = s.timestamp, totalAmount = s.total_amount, discountAmount = s.discount_amount, customerName = s.customer_name, customerGstin = s.customer_gstin, businessGstin = s.business_gstin, customerAddress = s.customer_address, businessAddress = s.business_address, type = s.type, notes = s.notes, items = items)
            }
            _expensesList.value = expenseRepository.getAllExpenses()
            _purchases.value = purchaseRepository.getAllPurchases().map { p ->
                Purchase(id = p.id, supplierId = p.supplier_id, supplierName = p.supplier_name, totalAmount = p.total_amount, taxAmount = p.tax_amount, type = p.type, timestamp = p.timestamp, notes = p.notes, items = emptyList())
            }
        }
    }

    // ==========================================================================
    // E03-S3: Load Today's financial snapshot from SQL aggregates
    // ==========================================================================
    private fun loadTodaySnapshot() {
        viewModelScope.launch {
            val todayKey = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
                .format(java.util.Date())
            val snap = salesRepository.getTodaySnapshot(todayKey)
            if (snap != null) {
                _todaySnapshot.value = TodaySnapshot(
                    todayRevenue = snap.today_revenue,
                    todayCOG = snap.today_cog,
                    todayExpenses = snap.today_expenses,
                    todayProfit = (snap.today_revenue - snap.today_cog - snap.today_expenses)
                )
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

    // ==========================================================================
    // E01-S4: Sync Status UI — load + expose + retry
    // ==========================================================================

    /** Reload sync state from DB into UiSyncStatus StateFlow */
    private fun refreshSyncStatus() {
        viewModelScope.launch {
            val st = syncRepository.getSyncState()
            _uiSyncStatus.value = UiSyncStatus(
                status = st?.status ?: "IDLE",
                lastSyncAt = st?.last_full_sync_at ?: 0L,
                failedCount = (st?.total_failed_mutations ?: 0L).toInt(),
                isSyncing = st?.status in listOf("PUSHING", "PULLING")
            )
        }
    }

    /** Trigger a one-time sync job (on user tap of retry button) */
    fun retrySync() {
        val storeId = prefs.getString("active_store_id", "default_store") ?: "default_store"
        val workReq = OneTimeWorkRequestBuilder<com.storebook.inventoryapp.data.sync.SyncWorker>()
            .addTag("sync_manual_retry")
            .setInputData(
                androidx.work.Data.Builder()
                    .putString("STORE_ID", storeId)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueue(workReq)

        // Start listening for sync completion by polling refresh
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            refreshSyncStatus() 
        }
    }
}
