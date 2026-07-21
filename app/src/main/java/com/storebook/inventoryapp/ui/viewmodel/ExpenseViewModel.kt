package com.storebook.inventoryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storebook.inventoryapp.shared.domain.models.ExpenseEntry
import com.storebook.inventoryapp.shared.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository,
) : ViewModel() {
    private val _expensesList = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expensesList: StateFlow<List<ExpenseEntry>> = _expensesList

    // E11-S5: Error StateFlow for per-VM toast/Snackbar error feedback
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _expensesList.value = repository.getAllExpenses()
        }
    }

    fun addExpense(
        type: String,
        amount: Double,
        notes: String,
        supplierName: String? = null,
        supplierPhone: String? = null,
        onResult: () -> Unit = {
        },
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                type = type,
                description = notes,
                amount = amount,
                supplierName = supplierName,
                supplierPhone = supplierPhone,
            )
            loadData()
            onResult()
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            // legacyRepository.deleteExpense(id)
            // loadData()
        }
    }
}
