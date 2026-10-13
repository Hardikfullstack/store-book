package com.storebook.inventoryapp.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.dataconnect.StorebookConnectorConnector
import com.storebook.inventoryapp.dataconnect.execute
import com.storebook.inventoryapp.dataconnect.instance
import com.storebook.inventoryapp.shared.domain.models.Category
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.ItemBatch
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.shared.domain.repository.BatchRepository
import com.storebook.inventoryapp.shared.domain.repository.CategoryRepository
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.repository.PurchaseRepository
import com.storebook.inventoryapp.shared.domain.repository.StockAdjustmentRepository
import com.storebook.inventoryapp.shared.domain.repository.SupplierRepository
import com.storebook.inventoryapp.utils.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val supplierRepository: SupplierRepository,
    private val purchaseRepository: PurchaseRepository,
    private val batchRepository: BatchRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository,
    private val categoryRepository: CategoryRepository,
    private val context: Context,
) : ViewModel() {
    private val prefs = SecurityUtils.getEncryptedPrefs(context)

    var userRole: String by mutableStateOf(prefs.getString("user_role", "owner") ?: "owner")
    private var _lastRestockSupplierName by mutableStateOf(prefs.getString("last_restock_supplier", "") ?: "")

    // E11-S5: Error StateFlow for per-VM toast/Snackbar error feedback
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
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

    val currentBusinessType: String
        get() {
            val storeId = prefs.getString("active_store_id", "default") ?: "default"
            return prefs.getString("business_type_$storeId", null)
                ?: prefs.getString("business_type", "general")
                ?: "general"
        }

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private var currentSearch = ""
    private var currentCategory = "All"
    private var currentSortBy = "Name"

    init {
        viewModelScope.launch {
            CategoryRepository.categoriesUpdated.collect {
                try {
                    _categories.value = categoryRepository.getCategoriesByBusinessType(currentBusinessType)
                } catch (e: Exception) {
                    android.util.Log.w("InventoryViewModel", "Failed to reload categories: ${e.message}")
                }
            }
        }
        loadCategories()
    }

    private fun triggerSync() {
        val storeId = prefs.getString("active_store_id", null) ?: SecurityUtils.DEFAULT_STORE_ID
        com.storebook.inventoryapp.data.sync.SyncManager
            .onRecordCreatedOrUpdated(context, storeId)
    }

    fun loadFilteredItems(
        search: String = currentSearch,
        category: String = currentCategory,
        sortBy: String = currentSortBy,
    ) {
        currentSearch = search
        currentCategory = category
        currentSortBy = sortBy

        viewModelScope.launch {
            _isLoadingItems.value = true
            try {
                _filteredItems.value =
                    inventoryRepository.getActiveItemsFiltered(search, category, sortBy.lowercase(), 50, 0).map { i ->
                        Item(
                            id = i.id,
                            name = i.name,
                            quantity = i.quantity,
                            unit = i.unit,
                            buyPrice = i.buy_price,
                            sellPrice = i.sell_price,
                            lowStockThreshold = i.low_stock_threshold,
                            category = i.category,
                            hsnCode = i.hsn_code,
                            taxRate = i.tax_rate,
                            photoPath = i.photo_path,
                            barcode = i.barcode,
                        )
                    }
                val threshold = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
                val nearBatches = batchRepository.getNearExpiryBatches(threshold)
                val nearItemIds = nearBatches.map { it.item_id }.toSet()
                _nearExpiryItems.value =
                    if (nearItemIds.isNotEmpty()) {
                        inventoryRepository.getActiveItems().filter { it.id in nearItemIds }.map { i ->
                            Item(
                                id = i.id,
                                name = i.name,
                                quantity = i.quantity,
                                unit = i.unit,
                                buyPrice = i.buy_price,
                                sellPrice = i.sell_price,
                                lowStockThreshold = i.low_stock_threshold,
                                category = i.category,
                                hsnCode = i.hsn_code,
                                taxRate = i.tax_rate,
                                photoPath = i.photo_path,
                                barcode = i.barcode,
                            )
                        }
                    } else {
                        emptyList()
                    }
                _suppliers.value =
                    supplierRepository.getAllSuppliers().map { s ->
                        Supplier(id = s.id, name = s.name, phone = s.phone, gstin = s.gstin, address = s.address)
                    }
            } finally {
                _isLoadingItems.value = false
            }
        }
    }

    fun loadMoreItems(
        search: String = "",
        category: String = "All",
        sortBy: String = "Name",
        currentSize: Int,
        pageSize: Int = 50,
        onResult: (List<Item>) -> Unit = {
        },
    ) {
        viewModelScope.launch {
            val more =
                inventoryRepository
                    .getActiveItemsFiltered(search, category, sortBy, pageSize.toLong(), currentSize.toLong())
                    .map { i ->
                        Item(
                            id = i.id,
                            name = i.name,
                            quantity = i.quantity,
                            unit = i.unit,
                            buyPrice = i.buy_price,
                            sellPrice = i.sell_price,
                            lowStockThreshold = i.low_stock_threshold,
                            category = i.category,
                            hsnCode = i.hsn_code,
                            taxRate = i.tax_rate,
                            photoPath = i.photo_path,
                            barcode = i.barcode,
                        )
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

    fun addSupplier(
        name: String,
        phone: String?,
        gstin: String?,
        address: String?,
        onResult: (Long) -> Unit = {},
    ) {
        viewModelScope.launch {
            val id = supplierRepository.insertSupplier(name, phone, gstin, address)
            _suppliers.value =
                supplierRepository.getAllSuppliers().map { s ->
                    Supplier(id = s.id, name = s.name, phone = s.phone, gstin = s.gstin, address = s.address)
                }
            onResult(id)
        }
    }

    fun addPurchase(
        purchase: Purchase,
        onResult: (Long) -> Unit = {},
    ) {
        viewModelScope.launch {
            val purchaseUuid =
                purchase.cloudId?.takeIf { it.isNotBlank() } ?: java.util.UUID
                    .randomUUID()
                    .toString()
            val id =
                purchaseRepository
                    .insertPurchase(
                        purchase.supplierId,
                        purchase.supplierName,
                        purchase.totalAmount,
                        purchase.taxAmount,
                        purchase.type,
                        purchase.notes,
                        cloudId = purchaseUuid,
                    )
            purchase.items.forEach { item ->
                val purchaseItemUuid =
                    item.cloudId?.takeIf { it.isNotBlank() } ?: java.util.UUID
                        .randomUUID()
                        .toString()
                purchaseRepository
                    .insertPurchaseItem(
                        id,
                        item.itemId,
                        item.itemName,
                        item.quantity,
                        item.unit,
                        item.buyPrice,
                        cloudId = purchaseItemUuid,
                    )
                inventoryRepository.updateItemStock(item.itemId, item.quantity)
                stockAdjustmentRepository.insertStockAdjustment(
                    itemId = item.itemId,
                    itemName = item.itemName,
                    reason = "Restock",
                    delta = item.quantity,
                    cloudId =
                        java.util.UUID
                            .randomUUID()
                            .toString(),
                )
            }
            loadFilteredItems()
            triggerSync()
            onResult(id)
        }
    }

    fun addItemBatch(
        batch: ItemBatch,
        onResult: (Long) -> Unit = {},
    ) {
        viewModelScope.launch {
            val id =
                batchRepository
                    .insertItemBatch(
                        batch.itemId,
                        batch.batchNumber,
                        batch.expiryDate,
                        batch.quantity,
                        batch.costPrice,
                        batch.notes,
                        cloudId =
                            java.util.UUID
                                .randomUUID()
                                .toString(),
                    )
            onResult(id)
        }
    }

    suspend fun getLatestBatchForItem(itemId: Long): ItemBatch? {
        val b = batchRepository.getBatchesForItem(itemId).firstOrNull() ?: return null
        return ItemBatch(
            id = b.id,
            itemId = b.item_id,
            batchNumber = b.batch_number,
            expiryDate = b.expiry_date,
            quantity = b.quantity,
            costPrice = b.cost_price,
            timestamp = b.timestamp,
            notes = b.notes,
        )
    }

    fun addItem(
        name: String,
        quantity: Double,
        unit: String,
        buyPrice: Double,
        sellPrice: Double,
        threshold: Double,
        category: String,
        categoryId: String? = null,
        photoPath: String? = null,
        barcode: String? = null,
        hsnCode: String? = null,
        taxRate: Double = 0.0,
        supplierId: Long? = null,
        supplierName: String? = null,
        onResult: (Long) -> Unit = {},
    ) {
        viewModelScope.launch {
            val trimmedCategory = category.trim()
            val allCategories = categoryRepository.getAllCategories()
            val matchedCat = allCategories.find { it.name.trim().equals(trimmedCategory, ignoreCase = true) }
            val effectiveCategoryId =
                categoryId?.takeIf { it.isNotBlank() }
                    ?: matchedCat?.cloudId
                    ?: run {
                        val newCatUuid =
                            java.util.UUID
                                .randomUUID()
                                .toString()
                        try {
                            val storeId = prefs.getString("active_store_id", null) ?: SecurityUtils.DEFAULT_STORE_ID
                            categoryRepository.insertCategory(
                                name = trimmedCategory,
                                businessType = currentBusinessType,
                                storeId = storeId,
                                cloudId = newCatUuid,
                            )
                        } catch (_: Exception) {
                        }
                        newCatUuid
                    }
            if (matchedCat != null && matchedCat.cloudId.isNullOrBlank()) {
                categoryRepository.markCategorySynced(matchedCat.id, effectiveCategoryId, matchedCat.updatedAt)
            }

            val itemUuid =
                java.util.UUID
                    .randomUUID()
                    .toString()
            val item =
                Item(
                    id = 0L,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    lowStockThreshold = threshold,
                    category = category,
                    photoPath = photoPath,
                    barcode = barcode,
                    hsnCode = hsnCode,
                    taxRate = taxRate,
                )
            val id =
                inventoryRepository.insertItem(
                    name = item.name, quantity = item.quantity, unit = item.unit,
                    buyPrice = item.buyPrice, sellPrice = item.sellPrice,
                    threshold = item.lowStockThreshold, category = item.category,
                    photoPath = item.photoPath, barcode = item.barcode, hsnCode = item.hsnCode, taxRate = item.taxRate,
                    cloudId = itemUuid,
                ) ?: return@launch

            // Case 3: Initial Stock Purchase & Purchase Item (Mirrors web logic: ItemsClient.tsx:671-705)
            if (quantity > 0) {
                try {
                    val purchaseUuid =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    val purchaseItemUuid =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    val totalAmount = quantity * buyPrice
                    val taxAmount = totalAmount * (taxRate / 100.0)

                    val allSuppliers = supplierRepository.getAllSuppliers()
                    val targetSupplier =
                        if (supplierId != null && supplierId > 0) {
                            allSuppliers.find { it.id == supplierId }
                        } else {
                            allSuppliers.find {
                                it.name.trim().equals("Cash / Anonymous", ignoreCase = true) ||
                                    it.cloud_id == "068cdba2-f58e-4ab8-a596-13806e4cb18e"
                            }
                        }

                    val effectiveSupplierId =
                        targetSupplier?.id ?: try {
                            val newSuppId = supplierRepository.insertSupplier("Cash / Anonymous", null, null, null)
                            supplierRepository.markSupplierSynced(newSuppId, "068cdba2-f58e-4ab8-a596-13806e4cb18e")
                            newSuppId
                        } catch (_: Exception) {
                            0L
                        }
                    val effectiveSupplierName = targetSupplier?.name ?: (supplierName?.takeIf { it.isNotBlank() } ?: "Cash / Anonymous")

                    val newPurchaseId =
                        purchaseRepository.insertPurchase(
                            supplierId = effectiveSupplierId,
                            supplierName = effectiveSupplierName,
                            totalAmount = totalAmount,
                            taxAmount = taxAmount,
                            type = "BILL",
                            notes = "Initial stock for $name",
                            cloudId = purchaseUuid,
                        )

                    purchaseRepository.insertPurchaseItem(
                        purchaseId = newPurchaseId,
                        itemId = id,
                        itemName = name,
                        quantity = quantity,
                        unit = unit,
                        buyPrice = buyPrice,
                        cloudId = purchaseItemUuid,
                    )

                    stockAdjustmentRepository.insertStockAdjustment(
                        itemId = id,
                        itemName = name,
                        reason = "Initial Stock",
                        delta = quantity,
                        cloudId =
                            java.util.UUID
                                .randomUUID()
                                .toString(),
                    )
                } catch (e: Exception) {
                    android.util.Log.e("InventoryViewModel", "Failed to create initial stock purchase: ${e.message}", e)
                }
            }

            triggerSync()
            // e11-s2: Reactively refresh filtered items so new item appears immediately
            loadFilteredItems()
            onResult(id)
        }
    }

    fun restockItem(
        itemId: Long,
        quantityToAdd: Double,
        costPrice: Double,
        supplierName: String? = null,
        supplierPhone: String? = null,
    ) {
        viewModelScope.launch {
            val item = inventoryRepository.getItemById(itemId)
            val itemName = item?.name ?: "Item #$itemId"
            inventoryRepository.updateItemStock(itemId, quantityToAdd)
            stockAdjustmentRepository.insertStockAdjustment(
                itemId = itemId,
                itemName = itemName,
                reason = "Restock",
                delta = quantityToAdd,
                cloudId =
                    java.util.UUID
                        .randomUUID()
                        .toString(),
            )
            // Log Purchase & PurchaseItemDetail to match Web & Quick Refill
            try {
                val purchaseUuid =
                    java.util.UUID
                        .randomUUID()
                        .toString()
                val purchaseItemUuid =
                    java.util.UUID
                        .randomUUID()
                        .toString()
                val totalAmount = quantityToAdd * costPrice
                val taxAmount = totalAmount * ((item?.tax_rate ?: 0.0) / 100.0)

                val allSuppliers = supplierRepository.getAllSuppliers()
                val targetSupplier =
                    allSuppliers.find {
                        it.name.trim().equals(supplierName?.trim(), ignoreCase = true) ||
                            it.name.trim().equals("Cash / Anonymous", ignoreCase = true) ||
                            it.cloud_id == "068cdba2-f58e-4ab8-a596-13806e4cb18e"
                    }
                val effectiveSupplierId =
                    targetSupplier?.id ?: try {
                        val newSuppId = supplierRepository.insertSupplier("Cash / Anonymous", null, null, null)
                        supplierRepository.markSupplierSynced(newSuppId, "068cdba2-f58e-4ab8-a596-13806e4cb18e")
                        newSuppId
                    } catch (_: Exception) {
                        0L
                    }
                val effectiveSupplierName = targetSupplier?.name ?: (supplierName?.takeIf { it.isNotBlank() } ?: "Cash / Anonymous")

                val newPurchaseId =
                    purchaseRepository.insertPurchase(
                        supplierId = effectiveSupplierId,
                        supplierName = effectiveSupplierName,
                        totalAmount = totalAmount,
                        taxAmount = taxAmount,
                        type = "BILL",
                        notes = "Refill stock for $itemName",
                        cloudId = purchaseUuid,
                    )
                purchaseRepository.insertPurchaseItem(
                    purchaseId = newPurchaseId,
                    itemId = itemId,
                    itemName = itemName,
                    quantity = quantityToAdd,
                    unit = item?.unit ?: "pcs",
                    buyPrice = costPrice,
                    cloudId = purchaseItemUuid,
                )
            } catch (e: Exception) {
                android.util.Log.e("InventoryViewModel", "Failed to log restock purchase: ${e.message}", e)
            }
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
        photoPath: String? = null,
        barcode: String? = null,
        hsnCode: String? = null,
        taxRate: Double = 0.0,
        adjustmentReason: String = "Count Correction",
    ) {
        viewModelScope.launch {
            val existingItem = inventoryRepository.getItemById(id)
            val delta = if (existingItem != null) quantity - existingItem.quantity else 0.0
            if (delta != 0.0) {
                stockAdjustmentRepository.insertStockAdjustment(
                    itemId = id,
                    itemName = name,
                    reason = adjustmentReason,
                    delta = delta,
                )
            }
            val item =
                Item(
                    id = id,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    lowStockThreshold = threshold,
                    category = category,
                    photoPath = photoPath,
                    barcode = barcode,
                    hsnCode = hsnCode,
                    taxRate = taxRate,
                )
            inventoryRepository.updateItem(
                id = item.id, name = item.name, quantity = item.quantity, unit = item.unit,
                buyPrice = item.buyPrice, sellPrice = item.sellPrice,
                threshold = item.lowStockThreshold, category = item.category,
                photoPath = item.photoPath, barcode = item.barcode, hsnCode = item.hsnCode, taxRate = item.taxRate,
            )
            loadFilteredItems()
            triggerSync()
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            val bType = currentBusinessType
            val storeId = prefs.getString("active_store_id", null) ?: SecurityUtils.DEFAULT_STORE_ID
            categoryRepository.seedDefaultCategoriesIfEmpty(bType, storeId)
            _categories.value = categoryRepository.getCategoriesByBusinessType(bType)
            syncCategoriesWithServer(bType, storeId)
        }
    }

    fun syncCategoriesWithServer(
        businessType: String = currentBusinessType,
        storeId: String = prefs.getString("active_store_id", null) ?: SecurityUtils.DEFAULT_STORE_ID,
    ) {
        viewModelScope.launch {
            try {
                val connector = StorebookConnectorConnector.instance
                val result = connector.getCategories.execute(businessType)
                val remoteList = result.data.categories
                for (remoteCat in remoteList) {
                    categoryRepository.upsertCategoryRemote(
                        name = remoteCat.name,
                        businessType = remoteCat.businessType,
                        storeId = remoteCat.storeId,
                        cloudId = remoteCat.id,
                        isDeleted = remoteCat.isDeleted,
                        updatedAt = remoteCat.updatedAt.toLong(),
                    )
                }
                _categories.value = categoryRepository.getCategoriesByBusinessType(businessType)
            } catch (e: Exception) {
                // If offline or network unavailable, local DB remains functional
                android.util.Log.d("InventoryViewModel", "Sync categories failed (offline/transient): ${e.message}")
            }
        }
    }

    fun createCategory(
        name: String,
        onResult: (Category) -> Unit,
    ) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isBlank()) return@launch
            val bType = currentBusinessType
            val storeId = prefs.getString("active_store_id", null) ?: SecurityUtils.DEFAULT_STORE_ID
            val uuid =
                java.util.UUID
                    .randomUUID()
                    .toString()
            var isSynced = false
            val now = System.currentTimeMillis()

            val isBatchMode =
                com.storebook.inventoryapp.data.sync.SyncManager
                    .isBatchUploadEnabled(context)
            if (!isBatchMode &&
                com.storebook.inventoryapp.data.sync.SyncManager
                    .isOnline(context)
            ) {
                try {
                    val connector = StorebookConnectorConnector.instance
                    connector.syncCategory.execute(
                        id = uuid,
                        businessType = bType,
                        name = trimmed,
                        isDeleted = false,
                        updatedAt = now.toDouble(),
                        storeId = storeId,
                    )
                    isSynced = true
                } catch (e: Exception) {
                    android.util.Log.w("InventoryViewModel", "Live DB category creation failed (offline/error): ${e.message}")
                }
            }

            val saved =
                categoryRepository.insertCategory(
                    name = trimmed,
                    businessType = bType,
                    storeId = storeId,
                    cloudId = uuid,
                    isSynced = isSynced,
                    updatedAt = now,
                )

            _categories.value = categoryRepository.getCategoriesByBusinessType(bType)
            onResult(saved)

            // Trigger global sync of categories in background so other categories are up to date
            syncCategoriesWithServer(bType, storeId)
        }
    }
}
