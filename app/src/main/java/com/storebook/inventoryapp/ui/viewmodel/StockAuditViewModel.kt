package com.storebook.inventoryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.shared.domain.models.StockAdjustment
import com.storebook.inventoryapp.shared.domain.repository.StockAdjustmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockAuditViewModel(
    private val stockAdjustmentRepository: StockAdjustmentRepository,
) : ViewModel() {

    private val _adjustments = MutableStateFlow<List<StockAdjustment>>(emptyList())
    val adjustments: StateFlow<List<StockAdjustment>> = _adjustments.asStateFlow()

    private val _totalCount = MutableStateFlow(0L)
    val totalCount: StateFlow<Long> = _totalCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedReason = MutableStateFlow("All Reasons")
    val selectedReason: StateFlow<String> = _selectedReason.asStateFlow()

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate: StateFlow<Long?> = _endDate.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    val pageSize = 15

    init {
        loadAdjustments()
    }

    fun loadAdjustments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val search = _searchQuery.value.trim()
                val reason = _selectedReason.value
                val start = _startDate.value ?: 0L
                val end = _endDate.value ?: 0L
                val page = _currentPage.value
                val offset = ((page - 1) * pageSize).toLong()

                val items = stockAdjustmentRepository.getStockAdjustmentsFiltered(
                    searchQuery = search,
                    reason = reason,
                    startDate = start,
                    endDate = end,
                    limit = pageSize.toLong(),
                    offset = offset,
                )
                val count = stockAdjustmentRepository.getStockAdjustmentsFilteredCount(
                    searchQuery = search,
                    reason = reason,
                    startDate = start,
                    endDate = end,
                )
                _adjustments.value = items.map {
                    StockAdjustment(
                        id = it.id,
                        itemId = it.item_id,
                        itemName = it.item_name,
                        reason = it.reason,
                        delta = it.delta,
                        timestamp = it.timestamp,
                        isDeleted = it.is_deleted.toInt(),
                        cloudId = it.cloud_id,
                        isSynced = it.is_synced.toInt(),
                        updatedAt = it.updated_at,
                    )
                }
                _totalCount.value = count
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _currentPage.value = 1
        loadAdjustments()
    }

    fun setSelectedReason(reason: String) {
        _selectedReason.value = reason
        _currentPage.value = 1
        loadAdjustments()
    }

    fun setDateRange(start: Long?, end: Long?) {
        _startDate.value = start
        _endDate.value = end
        _currentPage.value = 1
        loadAdjustments()
    }

    fun goToPage(page: Int) {
        val totalPages = maxOf(1, ((_totalCount.value + pageSize - 1) / pageSize).toInt())
        if (page in 1..totalPages) {
            _currentPage.value = page
            loadAdjustments()
        }
    }

    fun refresh() {
        loadAdjustments()
    }
}
