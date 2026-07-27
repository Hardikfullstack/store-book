package com.storebook.inventoryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.PurchaseItemDetail
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.repository.PurchaseRepository
import com.storebook.inventoryapp.shared.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val inventoryRepository: InventoryRepository,
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
                                com.storebook.inventoryapp.shared.domain.models.PurchaseItemDetail(
                                    itemId = pi.item_id,
                                    itemName = pi.item_name,
                                    quantity = pi.quantity,
                                    unit = pi.unit,
                                    buyPrice = pi.buy_price,
                                )
                            },
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
            val pId =
                purchaseRepository.insertPurchase(
                    supplierId = supplierId,
                    supplierName = supplierName,
                    totalAmount = totalAmount,
                    taxAmount = 0.0,
                    type = paymentMode,
                    notes = notes,
                )
            itemsInCart.forEach { item ->
                purchaseRepository
                    .insertPurchaseItem(pId, item.itemId, item.itemName, item.quantity, item.unit, item.buyPrice)
                inventoryRepository.updateItemStock(item.itemId, item.quantity)
            }
            loadData()
            onResult(pId)
        }
    }
}
