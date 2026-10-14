package com.storebook.inventoryapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.data.sync.SyncManager
import com.storebook.inventoryapp.data.sync.SyncWorker
import com.storebook.inventoryapp.shared.domain.models.ExpenseEntry
import com.storebook.inventoryapp.shared.domain.repository.ExpenseRepository
import com.storebook.inventoryapp.utils.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class ExpenseFilterParams(
    val search: String,
    val minAmt: Double?,
    val maxAmt: Double?,
    val start: Long?,
    val end: Long?,
)

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val context: Context? = null,
) : ViewModel() {
    private val _expensesList = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expensesList: StateFlow<List<ExpenseEntry>> = _expensesList

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _minAmount = MutableStateFlow<Double?>(null)
    val minAmount: StateFlow<Double?> = _minAmount

    private val _maxAmount = MutableStateFlow<Double?>(null)
    val maxAmount: StateFlow<Double?> = _maxAmount

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate: StateFlow<Long?> = _endDate

    // E11-S5: Error StateFlow for per-VM toast/Snackbar error feedback
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val filterParams =
        combine(
            _searchQuery,
            _minAmount,
            _maxAmount,
            _startDate,
            _endDate,
        ) { search, minAmt, maxAmt, start, end ->
            ExpenseFilterParams(search, minAmt, maxAmt, start, end)
        }

    val filteredExpenses: StateFlow<List<ExpenseEntry>> =
        combine(_expensesList, filterParams) { expenses, filters ->
            expenses.filter { entry ->
                // Text search matching description, type, or supplier
                if (filters.search.isNotBlank()) {
                    val q = filters.search.trim().lowercase()
                    val matchesDesc = entry.description.lowercase().contains(q)
                    val matchesType = entry.type.lowercase().contains(q)
                    val matchesSupplier = entry.supplierName?.lowercase()?.contains(q) == true
                    if (!matchesDesc && !matchesType && !matchesSupplier) return@filter false
                }

                // Min amount
                if (filters.minAmt != null && entry.amount < filters.minAmt) return@filter false

                // Max amount
                if (filters.maxAmt != null && entry.amount > filters.maxAmt) return@filter false

                // Date range (start and end timestamps in milliseconds)
                if (filters.start != null && entry.timestamp < filters.start) return@filter false
                if (filters.end != null && entry.timestamp > filters.end) return@filter false

                true
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val totalAmount: StateFlow<Double> =
        filteredExpenses
            .map { list ->
                list.sumOf { it.amount }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0,
            )

    val totalCount: StateFlow<Int> =
        filteredExpenses
            .map { it.size }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0,
            )

    init {
        loadData()
        syncExpensesFromCloud()
    }

    fun loadData() {
        viewModelScope.launch {
            _expensesList.value = repository.getAllExpenses()
        }
    }

    fun syncExpensesFromCloud(onComplete: () -> Unit = {}) {
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
                        android.util.Log.w("ExpenseViewModel", "Sync failed: ${e.message}")
                    }
                }
            }
            loadData()
            onComplete()
        }
    }

    fun refresh(onComplete: () -> Unit = {}) {
        syncExpensesFromCloud(onComplete)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAmountRange(
        min: Double?,
        max: Double?,
    ) {
        _minAmount.value = min
        _maxAmount.value = max
    }

    fun setDateRange(
        start: Long?,
        end: Long?,
    ) {
        _startDate.value = start
        _endDate.value = end
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _minAmount.value = null
        _maxAmount.value = null
        _startDate.value = null
        _endDate.value = null
    }

    fun addExpense(
        type: String,
        amount: Double,
        notes: String,
        supplierName: String? = null,
        supplierPhone: String? = null,
        onResult: () -> Unit = {},
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                type = type,
                description = notes,
                amount = amount,
                supplierName = supplierName?.takeIf { it.isNotBlank() },
                supplierPhone = supplierPhone?.takeIf { it.isNotBlank() },
            )
            loadData()
            context?.let { ctx ->
                val storeId =
                    SecurityUtils
                        .getEncryptedPrefs(ctx)
                        .getString("active_store_id", "default") ?: "default"
                SyncManager.onRecordCreatedOrUpdated(ctx, storeId)
            }
            onResult()
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.softDeleteExpense(id)
            loadData()
            context?.let { ctx ->
                val storeId =
                    SecurityUtils
                        .getEncryptedPrefs(ctx)
                        .getString("active_store_id", "default") ?: "default"
                SyncManager.onRecordCreatedOrUpdated(ctx, storeId)
            }
        }
    }
}
