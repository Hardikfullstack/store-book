package com.storebook.inventoryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.shared.domain.models.CustomerBalance
import com.storebook.inventoryapp.shared.domain.models.CustomerDetailedBalance
import com.storebook.inventoryapp.shared.domain.models.UdhaarEntry
import com.storebook.inventoryapp.shared.domain.repository.UdhaarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UdhaarViewModel(
    val repository: UdhaarRepository,
) : ViewModel() {
    private val _udhaarEntries = MutableStateFlow<List<UdhaarEntry>>(emptyList())
    val udhaarEntries: StateFlow<List<UdhaarEntry>> = _udhaarEntries

    private val _udhaarBalances = MutableStateFlow<List<CustomerBalance>>(emptyList())
    val udhaarBalances: StateFlow<List<CustomerBalance>> = _udhaarBalances

    // E03-S2: Detailed breakdown with separate outstanding + paid totals per customer
    private val _detailedBalances = MutableStateFlow<List<CustomerDetailedBalance>>(emptyList())
    val detailedBalances: StateFlow<List<CustomerDetailedBalance>> = _detailedBalances

    // E11-S5: Error StateFlow for per-VM toast/Snackbar error feedback
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Dummy business name for the UI fallback
    val businessName = "StoreBook Kirana"

    init {
        loadUdhaar()
    }

    fun loadUdhaar() {
        viewModelScope.launch {
            _udhaarEntries.value = repository.getAllUdhaar()
            _udhaarBalances.value = repository.getUdhaarBalances()
            _detailedBalances.value = repository.getUdhaarBalancesWithBreakdown()
        }
    }

    fun recordUdhaarEntry(
        customerName: String,
        amount: Double,
        type: String,
        notes: String?,
    ) {
        viewModelScope.launch {
            repository.insertUdhaar(customerName, amount, type, notes)
            loadUdhaar()
        }
    }
}
