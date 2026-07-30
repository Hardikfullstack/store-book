package com.storebook.inventoryapp.shared.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Item(
        val id: Long = 0,
        val name: String,
        val quantity: Double,
        val unit: String,
        val buyPrice: Double,
        val sellPrice: Double,
        val lowStockThreshold: Double,
        val category: String,
        val photoPath: String? = null,
        val barcode: String? = null,
        val hsnCode: String? = null,
        val taxRate: Double = 0.0,
        val isDeleted: Int = 0,
        val deletedTimestamp: Long = 0,
)

@Serializable
data class CartItem(
        val item: Item,
        var quantity: Double,
)

@Serializable
data class Sale(
        val id: Long = 0,
        val timestamp: Long,
        val totalAmount: Double,
        val discountAmount: Double,
        val customerName: String? = null,
        val customerGstin: String? = null,
        val businessGstin: String? = null,
        val customerAddress: String? = null,
        val businessAddress: String? = null,
        val type: String = "SALE", // 'SALE' or 'ESTIMATE'
        val notes: String? = null,
        val isConverted: Boolean = false,
        val items: List<SaleItemDetail> = emptyList(),
)

@Serializable
data class SaleItemDetail(
        val id: Long = 0,
        val itemId: Long,
        val itemName: String,
        val quantity: Double,
        val unit: String,
        val sellPrice: Double,
        val buyPrice: Double,
        val taxRate: Double = 0.0,
        val hsnCode: String? = null,
)

@Serializable
data class UdhaarEntry(
        val id: Long = 0,
        val customerName: String,
        val amount: Double,
        val type: String, // 'CREDIT' or 'PAYMENT'
        val timestamp: Long,
        val notes: String? = null,
)

@Serializable
data class CustomerBalance(
        val customerName: String,
        val netBalance: Double, // positive = customer owes shop, negative = shop owes customer
        val lastTransactionTime: Long,
)

/** E03-S2: Detailed breakdown of outstanding vs paid amounts per customer */
@Serializable
data class CustomerDetailedBalance(
        val customerName: String,
        val totalOutstanding: Double, // SUM of all CREDIT entries
        val totalPaid: Double,       // SUM of all PAYMENT entries
        val currentBalance: Double,  // outstanding minus paid (positive = owes money)
        val lastTransactionTime: Long,
)

@Serializable
data class ExpenseEntry(
        val id: Long = 0,
        val type: String, // 'RESTOCK' or 'OVERHEAD'
        val description: String,
        val amount: Double,
        val timestamp: Long,
        val supplierName: String? = null,
        val supplierPhone: String? = null,
)

@Serializable
data class Supplier(
        val id: Long = 0,
        val name: String,
        val phone: String? = null,
        val gstin: String? = null,
        val address: String? = null,
)

@Serializable
data class Purchase(
        val id: Long = 0,
        val supplierId: Long,
        val supplierName: String,
        val totalAmount: Double,
        val taxAmount: Double = 0.0,
        val type: String = "BILL",
        val timestamp: Long,
        val notes: String? = null,
        val items: List<PurchaseItemDetail> = emptyList(),
)

@Serializable
data class PurchaseItemDetail(
        val id: Long = 0,
        val purchaseId: Long,
        val itemId: Long,
        val itemName: String,
        val quantity: Double,
        val unit: String,
        val buyPrice: Double,
)

@Serializable
data class SupplierBalance(
        val supplierId: Long,
        val supplierName: String,
        val phone: String? = null,
        val netBalance: Double,
        val lastTransactionTime: Long,
)

@Serializable
data class ItemBatch(
        val id: Long = 0,
        val itemId: Long,
        val batchNumber: String? = null,
        val expiryDate: Long? = null,   // epoch millis; null = no expiry
        val quantity: Double,
        val costPrice: Double,
        val timestamp: Long,
        val notes: String? = null,
)
