package com.storebook.inventoryapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.storebook.inventoryapp.data.DbMigrationCallback
import com.storebook.inventoryapp.shared.data.local.StoreBookDatabase
import com.storebook.inventoryapp.shared.domain.repository.BatchRepository
import com.storebook.inventoryapp.shared.domain.repository.ExpenseRepository
import com.storebook.inventoryapp.shared.domain.repository.InventoryRepository
import com.storebook.inventoryapp.shared.domain.repository.PurchaseRepository
import com.storebook.inventoryapp.shared.domain.repository.SalesRepository
import com.storebook.inventoryapp.shared.domain.repository.SupplierRepository
import com.storebook.inventoryapp.shared.domain.repository.SyncRepository
import com.storebook.inventoryapp.shared.domain.repository.SystemRepository
import com.storebook.inventoryapp.shared.domain.repository.UdhaarRepository
import com.storebook.inventoryapp.utils.SecurityUtils

class AppViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    private val prefs by lazy { SecurityUtils.getEncryptedPrefs(context) }
    private val storeId by lazy { prefs.getString("active_store_id", "default_store") ?: "default_store" }

    // Lazy initialize the database so it's a singleton within the factory scope
    private val database: StoreBookDatabase by lazy {
        val driver =
            AndroidSqliteDriver(
                StoreBookDatabase.Schema,
                context,
                "storebook_$storeId.db",
                callback = DbMigrationCallback,
            )
        StoreBookDatabase(driver)
    }

    private val inventoryRepository by lazy { InventoryRepository(database) }
    private val salesRepository by lazy { SalesRepository(database) }
    private val purchaseRepository by lazy { PurchaseRepository(database) }
    private val supplierRepository by lazy { SupplierRepository(database) }
    private val udhaarRepository by lazy { UdhaarRepository(database) }
    private val batchRepository by lazy { BatchRepository(database) }
    private val expenseRepository by lazy { ExpenseRepository(database) }
    private val systemRepository by lazy { SystemRepository(database) }
    private val syncRepository by lazy { SyncRepository(database) }

    // BP-3: Centralized sync status hub — shared across all ViewModels via the factory
    private val syncStatusViewModel by lazy { SyncStatusViewModel(context, syncRepository) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(UdhaarViewModel::class.java) -> {
                UdhaarViewModel(udhaarRepository) as T
            }
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> {
                InventoryViewModel(
                    inventoryRepository,
                    supplierRepository,
                    purchaseRepository,
                    batchRepository,
                    context,
                ) as T
            }
            modelClass.isAssignableFrom(SalesViewModel::class.java) -> {
                SalesViewModel(salesRepository, inventoryRepository, udhaarRepository, context) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                val gstExportService =
                    com.storebook.inventoryapp.services.GSTExportService(
                        salesRepository,
                        purchaseRepository,
                        supplierRepository,
                        inventoryRepository,
                    )
                val vm =
                    DashboardViewModel(
                        inventoryRepository,
                        salesRepository,
                        purchaseRepository,
                        supplierRepository,
                        expenseRepository,
                        gstExportService,
                        context,
                    )
                // BP-3: Inject centralized sync hub into DashboardViewModel
                vm.syncSource = syncStatusViewModel
                vm as T
            }
            modelClass.isAssignableFrom(PurchaseViewModel::class.java) -> {
                PurchaseViewModel(purchaseRepository, supplierRepository, inventoryRepository) as T
            }
            modelClass.isAssignableFrom(SupplierViewModel::class.java) -> {
                SupplierViewModel(supplierRepository, purchaseRepository) as T
            }
            modelClass.isAssignableFrom(ExpenseViewModel::class.java) -> {
                ExpenseViewModel(expenseRepository) as T
            }
            modelClass.isAssignableFrom(MoreViewModel::class.java) -> {
                MoreViewModel(salesRepository, expenseRepository, inventoryRepository, systemRepository, context) as T
            }
            modelClass.isAssignableFrom(SyncStatusViewModel::class.java) -> {
                syncStatusViewModel as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
}
