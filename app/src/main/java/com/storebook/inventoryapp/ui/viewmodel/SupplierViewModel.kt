package com.storebook.inventoryapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.data.sync.SyncManager
import com.storebook.inventoryapp.data.sync.SyncWorker
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.shared.domain.models.SupplierBalance
import com.storebook.inventoryapp.shared.domain.repository.PurchaseRepository
import com.storebook.inventoryapp.shared.domain.repository.SupplierRepository
import com.storebook.inventoryapp.utils.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SupplierViewModel(
    private val supplierRepository: SupplierRepository,
    private val purchaseRepository: PurchaseRepository,
    private val context: Context? = null,
) : ViewModel() {
    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases

    private val _suppliersMap = MutableStateFlow<Map<Long, Supplier>>(emptyMap())
    val suppliersMap: StateFlow<Map<Long, Supplier>> = _suppliersMap

    private val _supplierBalances = MutableStateFlow<List<SupplierBalance>>(emptyList())
    val supplierBalances: StateFlow<List<SupplierBalance>> = _supplierBalances

    init {
        loadData()
        syncIfInitialEmpty()
    }

    private fun syncIfInitialEmpty() {
        viewModelScope.launch {
            context?.let { ctx ->
                val localSuppliers = supplierRepository.getAllSuppliers()
                if (localSuppliers.isEmpty() && SyncManager.isOnline(ctx)) {
                    val storeId =
                        SecurityUtils
                            .getEncryptedPrefs(ctx)
                            .getString("active_store_id", "default") ?: "default"
                    try {
                        SyncWorker.performSync(ctx, storeId) { _, _ -> }
                        loadData()
                    } catch (e: Exception) {
                        android.util.Log.w("SupplierViewModel", "Initial sync failed: ${e.message}")
                    }
                }
            }
        }
    }

    fun refresh(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            context?.let { ctx ->
                val storeId =
                    SecurityUtils
                        .getEncryptedPrefs(ctx)
                        .getString("active_store_id", "default") ?: "default"
                if (SyncManager.isOnline(ctx)) {
                    try {
                        SyncWorker.performSync(ctx, storeId) { _, _ -> }
                    } catch (e: Exception) {
                        android.util.Log.w("SupplierViewModel", "Refresh sync failed: ${e.message}")
                    }
                }
            }
            loadData()
            onComplete()
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _suppliersMap.value =
                supplierRepository
                    .getAllSuppliers()
                    .map { s ->
                        Supplier(id = s.id, name = s.name, phone = s.phone, gstin = s.gstin, address = s.address)
                    }.associateBy { it.id }
            _supplierBalances.value = supplierRepository.getSupplierBalances(purchaseRepository)
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
                                com.storebook.inventoryapp.shared.domain.models.PurchaseItemDetail(
                                    purchaseId = p.id,
                                    itemId = pi.item_id,
                                    itemName = pi.item_name,
                                    quantity = pi.quantity,
                                    unit = pi.unit,
                                    buyPrice = pi.buy_price,
                                )
                            },
                    )
                }
        }
    }

    suspend fun getSupplierLedger(supplierId: Long): List<Any> {
        // Mocked or simplified wrapper around getSupplierLedger if it exists
        // Returning empty since we might just use the old method
        return emptyList()
    }

    fun addSupplierPayment(
        supplierId: Long,
        supplierName: String,
        amount: Double,
        notes: String?,
        onResult: (Long) -> Unit = {},
    ) {
        viewModelScope.launch {
            val id = purchaseRepository.insertPurchase(supplierId, supplierName, amount, 0.0, "PAYMENT", notes)
            loadData()
            context?.let { ctx ->
                val storeId =
                    SecurityUtils
                        .getEncryptedPrefs(ctx)
                        .getString("active_store_id", "default") ?: "default"
                SyncManager.onRecordCreatedOrUpdated(ctx, storeId)
            }
            onResult(id)
        }
    }

    fun addSupplier(
        name: String,
        phone: String?,
        gstin: String?,
        address: String?,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            supplierRepository.insertSupplier(name, phone, gstin, address)
            loadData()
            context?.let { ctx ->
                val storeId =
                    SecurityUtils
                        .getEncryptedPrefs(ctx)
                        .getString("active_store_id", "default") ?: "default"
                SyncManager.onRecordCreatedOrUpdated(ctx, storeId)
            }
            onComplete()
        }
    }

    fun removeSupplier(
        id: Long,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            supplierRepository.deleteSupplier(id)
            loadData()
            context?.let { ctx ->
                val storeId =
                    SecurityUtils
                        .getEncryptedPrefs(ctx)
                        .getString("active_store_id", "default") ?: "default"
                SyncManager.onRecordCreatedOrUpdated(ctx, storeId)
            }
            onComplete()
        }
    }
}
