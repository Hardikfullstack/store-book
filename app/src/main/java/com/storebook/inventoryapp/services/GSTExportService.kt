package com.storebook.inventoryapp.services

import android.content.Context
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.repository.PurchaseRepository
import com.storebook.inventoryapp.shared.domain.repository.SalesRepository
import com.storebook.inventoryapp.shared.domain.repository.SupplierRepository
import com.storebook.inventoryapp.utils.ExcelExporter
import com.storebook.inventoryapp.utils.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GSTExportService(
    private val salesRepository: SalesRepository,
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val inventoryRepository: InventoryRepository,
) {
    suspend fun exportGSTR1Excel(
        context: Context,
        startTs: Long,
        endTs: Long,
        fileName: String,
    ) = withContext(Dispatchers.IO) {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        val activeStoreId = prefs.getString("active_store_id", "default_store") ?: "default_store"
        val businessName =
            prefs.getString("business_name_$activeStoreId", prefs.getString("business_name", "Store")) ?: "Store"
        val businessGstin =
            prefs.getString("business_gstin_$activeStoreId", prefs.getString("business_gstin", "")) ?: ""

        val sales =
            salesRepository
                .getSalesByDateRange(startTs, endTs)
                .filter { it.type != "ESTIMATE" }
                .map { s ->
                    Sale(
                        id = s.id,
                        timestamp = s.timestamp,
                        totalAmount = s.total_amount,
                        discountAmount = s.discount_amount,
                        customerName = s.customer_name,
                        customerGstin = s.customer_gstin,
                        businessGstin = s.business_gstin,
                        customerAddress = s.customer_address,
                        businessAddress = s.business_address,
                        type = s.type,
                        notes = s.notes,
                        items =
                            salesRepository.getSaleItems(s.id).map { saleItem ->
                                com.storebook.inventoryapp.shared.domain.models.SaleItemDetail(
                                    itemId = saleItem.item_id,
                                    itemName = saleItem.item_name,
                                    quantity = saleItem.quantity,
                                    unit = saleItem.unit,
                                    buyPrice = saleItem.buy_price,
                                    sellPrice = saleItem.sell_price,
                                    taxRate = saleItem.tax_rate ?: 0.0,
                                    hsnCode = saleItem.hsn_code,
                                )
                            },
                    )
                }
        val allItemsMap =
            inventoryRepository.getActiveItems().associate {
                it.id to
                    Item(
                        id = it.id,
                        name = it.name,
                        quantity = it.quantity,
                        unit = it.unit,
                        buyPrice = it.buy_price,
                        sellPrice = it.sell_price,
                        lowStockThreshold = it.low_stock_threshold,
                        category = it.category,
                        hsnCode = it.hsn_code,
                        taxRate = it.tax_rate,
                    )
            }
        ExcelExporter.exportGstr1(
            context = context,
            fileName = fileName,
            sales = sales,
            businessName = businessName,
            businessGstin = businessGstin,
            allItemsMap = allItemsMap,
        )
    }

    suspend fun exportGSTR2Excel(
        context: Context,
        startTs: Long,
        endTs: Long,
        fileName: String,
    ) = withContext(Dispatchers.IO) {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        val activeStoreId = prefs.getString("active_store_id", "default_store") ?: "default_store"
        val businessName =
            prefs.getString("business_name_$activeStoreId", prefs.getString("business_name", "Store")) ?: "Store"
        val businessGstin =
            prefs.getString("business_gstin_$activeStoreId", prefs.getString("business_gstin", "")) ?: ""

        val purchases =
            purchaseRepository.getPurchasesByDateRange(startTs, endTs).map { p ->
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
        val suppliersMap =
            supplierRepository.getAllSuppliers().associate {
                it.id to
                    com.storebook.inventoryapp.shared.domain.models.Supplier(
                        id = it.id,
                        name = it.name,
                        phone = it.phone,
                        gstin = it.gstin,
                        address = it.address,
                    )
            }
        val allItemsMap =
            inventoryRepository.getActiveItems().associate {
                it.id to
                    Item(
                        id = it.id,
                        name = it.name,
                        quantity = it.quantity,
                        unit = it.unit,
                        buyPrice = it.buy_price,
                        sellPrice = it.sell_price,
                        lowStockThreshold = it.low_stock_threshold,
                        category = it.category,
                    )
            }
        ExcelExporter.exportGstr2(
            context = context,
            fileName = fileName,
            purchases = purchases,
            businessName = businessName,
            businessGstin = businessGstin,
            suppliersMap = suppliersMap,
            allItemsMap = allItemsMap,
        )
    }

    suspend fun exportGSTR3BExcel(
        context: Context,
        startTs: Long,
        endTs: Long,
        fileName: String,
    ) = withContext(Dispatchers.IO) {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        val activeStoreId = prefs.getString("active_store_id", "default_store") ?: "default_store"
        val businessName =
            prefs.getString("business_name_$activeStoreId", prefs.getString("business_name", "Store")) ?: "Store"
        val businessGstin =
            prefs.getString("business_gstin_$activeStoreId", prefs.getString("business_gstin", "")) ?: ""

        val sales =
            salesRepository.getSalesByDateRange(startTs, endTs).map { s ->
                Sale(
                    id = s.id,
                    timestamp = s.timestamp,
                    totalAmount = s.total_amount,
                    discountAmount = s.discount_amount,
                    customerName = s.customer_name,
                    customerGstin = s.customer_gstin,
                    businessGstin = s.business_gstin,
                    customerAddress = s.customer_address,
                    businessAddress = s.business_address,
                    type = s.type,
                    notes = s.notes,
                    items =
                        salesRepository.getSaleItems(s.id).map { saleItem ->
                            com.storebook.inventoryapp.shared.domain.models.SaleItemDetail(
                                itemId = saleItem.item_id,
                                itemName = saleItem.item_name,
                                quantity = saleItem.quantity,
                                unit = saleItem.unit,
                                buyPrice = saleItem.buy_price,
                                sellPrice = saleItem.sell_price,
                                taxRate = saleItem.tax_rate ?: 0.0,
                                hsnCode = saleItem.hsn_code,
                            )
                        },
                )
            }
        val purchases =
            purchaseRepository.getPurchasesByDateRange(startTs, endTs).map { p ->
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
        val suppliersMap =
            supplierRepository.getAllSuppliers().associate {
                it.id to
                    com.storebook.inventoryapp.shared.domain.models.Supplier(
                        id = it.id,
                        name = it.name,
                        phone = it.phone,
                        gstin = it.gstin,
                        address = it.address,
                    )
            }
        val allItemsMap =
            inventoryRepository.getActiveItems().associate {
                it.id to
                    Item(
                        id = it.id,
                        name = it.name,
                        quantity = it.quantity,
                        unit = it.unit,
                        buyPrice = it.buy_price,
                        sellPrice = it.sell_price,
                        lowStockThreshold = it.low_stock_threshold,
                        category = it.category,
                        hsnCode = it.hsn_code,
                        taxRate = it.tax_rate,
                    )
            }
        ExcelExporter.exportGstr3B(
            context = context,
            fileName = fileName,
            sales = sales,
            purchases = purchases,
            businessName = businessName,
            businessGstin = businessGstin,
            suppliersMap = suppliersMap,
            allItemsMap = allItemsMap,
        )
    }

    suspend fun exportGstdetailedExcel(
        context: Context,
        startTs: Long,
        endTs: Long,
        fileName: String,
    ) = withContext(Dispatchers.IO) {
        val prefs = SecurityUtils.getEncryptedPrefs(context)
        val activeStoreId = prefs.getString("active_store_id", "default_store") ?: "default_store"
        val businessName =
            prefs.getString("business_name_$activeStoreId", prefs.getString("business_name", "Store")) ?: "Store"
        val businessGstin =
            prefs.getString("business_gstin_$activeStoreId", prefs.getString("business_gstin", "")) ?: ""

        val sales =
            salesRepository.getSalesByDateRange(startTs, endTs).map { s ->
                Sale(
                    id = s.id,
                    timestamp = s.timestamp,
                    totalAmount = s.total_amount,
                    discountAmount = s.discount_amount,
                    customerName = s.customer_name,
                    customerGstin = s.customer_gstin,
                    businessGstin = s.business_gstin,
                    customerAddress = s.customer_address,
                    businessAddress = s.business_address,
                    type = s.type,
                    notes = s.notes,
                    items =
                        salesRepository.getSaleItems(s.id).map { saleItem ->
                            com.storebook.inventoryapp.shared.domain.models.SaleItemDetail(
                                itemId = saleItem.item_id,
                                itemName = saleItem.item_name,
                                quantity = saleItem.quantity,
                                unit = saleItem.unit,
                                buyPrice = saleItem.buy_price,
                                sellPrice = saleItem.sell_price,
                                taxRate = saleItem.tax_rate ?: 0.0,
                                hsnCode = saleItem.hsn_code,
                            )
                        },
                )
            }
        val purchases =
            purchaseRepository.getPurchasesByDateRange(startTs, endTs).map { p ->
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
        val suppliersMap =
            supplierRepository.getAllSuppliers().associate {
                it.id to
                    com.storebook.inventoryapp.shared.domain.models.Supplier(
                        id = it.id,
                        name = it.name,
                        phone = it.phone,
                        gstin = it.gstin,
                        address = it.address,
                    )
            }
        val allItemsMap =
            inventoryRepository.getActiveItems().associate {
                it.id to
                    Item(
                        id = it.id,
                        name = it.name,
                        quantity = it.quantity,
                        unit = it.unit,
                        buyPrice = it.buy_price,
                        sellPrice = it.sell_price,
                        lowStockThreshold = it.low_stock_threshold,
                        category = it.category,
                    )
            }
        ExcelExporter.exportGstDetailed(
            context = context,
            fileName = fileName,
            sales = sales,
            purchases = purchases,
            businessName = businessName,
            businessGstin = businessGstin,
            suppliersMap = suppliersMap,
            allItemsMap = allItemsMap,
        )
    }
}
