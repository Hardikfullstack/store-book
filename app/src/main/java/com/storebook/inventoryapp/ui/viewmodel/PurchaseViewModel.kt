package com.storebook.inventoryapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.data.sync.SyncManager
import com.storebook.inventoryapp.data.sync.SyncWorker
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.PurchaseItemDetail
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.repository.PurchaseRepository
import com.storebook.inventoryapp.shared.domain.repository.SupplierRepository
import com.storebook.inventoryapp.utils.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val inventoryRepository: InventoryRepository,
    private val context: Context? = null,
) : ViewModel() {
    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers

    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _allItems

    // E11-S5: Error StateFlow for per-VM toast/Snackbar error feedback
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadData()
        syncPurchasesFromCloud()
    }

    fun syncPurchasesFromCloud(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            context?.let { ctx ->
                val storeId =
                    SecurityUtils
                        .getEncryptedPrefs(ctx)
                        .getString("active_store_id", "default") ?: "default"
                if (SyncManager.isOnline(ctx)) {
                    try {
                        SyncWorker.performSync(ctx, storeId, forceFull = true) { _, _ -> }
                    } catch (e: Exception) {
                        android.util.Log.w("PurchaseViewModel", "Sync failed: ${e.message}")
                    }
                }
            }
            loadData()
            onComplete()
        }
    }

    fun refresh(onComplete: () -> Unit = {}) {
        syncPurchasesFromCloud(onComplete)
    }

    fun loadData() {
        viewModelScope.launch {
            _purchases.value =
                purchaseRepository.getAllPurchases().map { p ->
                    Purchase(
                        id = p.id,
                        supplierId = p.supplier_id,
                        supplierName = p.supplier_name,
                        totalAmount = p.total_amount,
                        taxAmount = p.tax_amount,
                        type = p.type,
                        timestamp = p.timestamp,
                        notes = p.notes,
                        items =
                            purchaseRepository.getPurchaseItems(p.id).map { pi ->
                                PurchaseItemDetail(
                                    id = pi.id,
                                    purchaseId = p.id,
                                    itemId = pi.item_id,
                                    itemName = pi.item_name,
                                    quantity = pi.quantity,
                                    unit = pi.unit,
                                    buyPrice = pi.buy_price,
                                    cloudId = pi.cloud_id,
                                )
                            },
                        cloudId = p.cloud_id,
                    )
                }
            _suppliers.value =
                supplierRepository.getAllSuppliers().map { s ->
                    Supplier(id = s.id, name = s.name, phone = s.phone, gstin = s.gstin, address = s.address)
                }
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

    suspend fun getPurchaseItems(purchaseId: Long): List<PurchaseItemDetail> =
        _purchases.value
            .find {
                it.id == purchaseId
            }?.items ?: emptyList()

    fun addPurchase(
        supplierId: Long,
        supplierName: String,
        itemsInCart: List<PurchaseItemDetail>,
        totalAmount: Double,
        paymentMode: String,
        notes: String?,
        onResult: (Long) -> Unit = {},
    ) {
        viewModelScope.launch {
            val purchaseUuid =
                java.util.UUID
                    .randomUUID()
                    .toString()
            val pId =
                purchaseRepository.insertPurchase(
                    supplierId = supplierId,
                    supplierName = supplierName,
                    totalAmount = totalAmount,
                    taxAmount = 0.0,
                    type = paymentMode,
                    notes = notes,
                    cloudId = purchaseUuid,
                )
            itemsInCart.forEach { item ->
                val itemUuid =
                    item.cloudId?.takeIf { it.isNotBlank() } ?: java.util.UUID
                        .randomUUID()
                        .toString()
                purchaseRepository
                    .insertPurchaseItem(
                        pId,
                        item.itemId,
                        item.itemName,
                        item.quantity,
                        item.unit,
                        item.buyPrice,
                        cloudId = itemUuid,
                    )
                inventoryRepository.updateItemStock(item.itemId, item.quantity)
            }
            loadData()
            context?.let { ctx ->
                val storeId =
                    SecurityUtils
                        .getEncryptedPrefs(ctx)
                        .getString("active_store_id", "default") ?: "default"
                SyncManager.onRecordCreatedOrUpdated(ctx, storeId)
            }
            onResult(pId)
        }
    }
}
