package com.storebook.inventoryapp.ui.screens.storebook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.domain.models.ExpenseEntry
import com.storebook.inventoryapp.ui.theme.primaryGradient
import com.storebook.inventoryapp.ui.viewmodel.ExpenseViewModel
import com.storebook.inventoryapp.utils.toRupeeWithDecimals
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ExpenseOrange = Color(0xFFEA580C) // Matching web bg-orange-600 / text-orange-600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    navController: NavController,
    viewModel: ExpenseViewModel,
) {
    val scope = rememberCoroutineScope()
    val allExpenses by viewModel.expensesList.collectAsStateWithLifecycle()
    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val minAmount by viewModel.minAmount.collectAsStateWithLifecycle()
    val maxAmount by viewModel.maxAmount.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntry?>(null) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var minInputText by remember(minAmount) { mutableStateOf(minAmount?.toString() ?: "") }
    var maxInputText by remember(maxAmount) { mutableStateOf(maxAmount?.toString() ?: "") }

    val datePickerDateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val cardDateTimeFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    val hasActiveFilters by remember(searchQuery, minAmount, maxAmount, startDate, endDate) {
        derivedStateOf {
            searchQuery.isNotBlank() || minAmount != null || maxAmount != null || startDate != null || endDate != null
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = startDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val sel = dpState.selectedDateMillis
                        if (sel != null) {
                            val cal =
                                Calendar.getInstance().apply {
                                    timeInMillis = sel
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                            viewModel.setDateRange(cal.timeInMillis, endDate)
                        }
                        showStartDatePicker = false
                    },
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = dpState)
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = endDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val sel = dpState.selectedDateMillis
                        if (sel != null) {
                            val cal =
                                Calendar.getInstance().apply {
                                    timeInMillis = sel
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }
                            viewModel.setDateRange(startDate, cal.timeInMillis)
                        }
                        showEndDatePicker = false
                    },
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = dpState)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.primaryGradient)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Expenses Tracker",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text(
                                text = "Keep an eye on your business spending",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isRefreshing = true
                                    viewModel.refresh {
                                        isRefreshing = false
                                    }
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = ExpenseOrange,
                                    contentColor = Color.White,
                                ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Expense",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            "Search expenses by description, type or supplier...",
                            fontSize = 13.sp,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.ui_element_desc),
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    Icons.Rounded.Cancel,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f),
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.45f),
                            focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f),
                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                )
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.refresh {
                        isRefreshing = false
                    }
                }
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Filters Card (Amount Range + Date Range)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            // Amount Range Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    "Amount:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(55.dp),
                                )
                                OutlinedTextField(
                                    value = minInputText,
                                    onValueChange = {
                                        minInputText = it
                                        viewModel.setAmountRange(it.toDoubleOrNull(), maxAmount)
                                    },
                                    placeholder = { Text("Min", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(10.dp),
                                )
                                Text(
                                    "-",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                OutlinedTextField(
                                    value = maxInputText,
                                    onValueChange = {
                                        maxInputText = it
                                        viewModel.setAmountRange(minAmount, it.toDoubleOrNull())
                                    },
                                    placeholder = { Text("Max", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(10.dp),
                                )
                                if (minAmount != null || maxAmount != null) {
                                    IconButton(
                                        onClick = {
                                            minInputText = ""
                                            maxInputText = ""
                                            viewModel.setAmountRange(null, null)
                                        },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            Icons.Rounded.Cancel,
                                            contentDescription = "Clear amount filter",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }

                            // Date Range Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    "Date:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(55.dp),
                                )

                                // Start Date Chip/Box
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                            .clickable { showStartDatePicker = true }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = startDate?.let { datePickerDateFmt.format(Date(it)) } ?: "Start Date",
                                            fontSize = 11.sp,
                                            fontWeight = if (startDate != null) FontWeight.SemiBold else FontWeight.Normal,
                                            color =
                                                if (startDate != null) {
                                                    MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                        )
                                    }
                                }

                                Text(
                                    "-",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                // End Date Chip/Box
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                            .clickable { showEndDatePicker = true }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = endDate?.let { datePickerDateFmt.format(Date(it)) } ?: "End Date",
                                            fontSize = 11.sp,
                                            fontWeight = if (endDate != null) FontWeight.SemiBold else FontWeight.Normal,
                                            color =
                                                if (endDate != null) {
                                                    MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                        )
                                    }
                                }

                                if (startDate != null || endDate != null) {
                                    IconButton(
                                        onClick = { viewModel.setDateRange(null, null) },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            Icons.Rounded.Cancel,
                                            contentDescription = "Clear date filter",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }

                            // Active Filter Chips & Reset All
                            AnimatedVisibility(visible = hasActiveFilters) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Filters applied",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    TextButton(
                                        onClick = {
                                            minInputText = ""
                                            maxInputText = ""
                                            viewModel.clearFilters()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            "Clear All Filters",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Summary Card (Matching Web summary metrics)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "Total Expenses",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "$totalCount ${if (totalCount == 1) "entry" else "entries"}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Amount",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = totalAmount.toRupeeWithDecimals(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ExpenseOrange,
                                )
                            }
                        }
                    }
                }

                // Empty State
                if (filteredExpenses.isEmpty()) {
                    item {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(ExpenseOrange.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = ExpenseOrange,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text =
                                    if (hasActiveFilters) {
                                        "No expenses found matching your search."
                                    } else {
                                        "No expenses logged yet"
                                    },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text =
                                    if (hasActiveFilters) {
                                        "Try adjusting your search terms, amount limits, or date range."
                                    } else {
                                        "Keep an eye on business spending by logging shop rent, bills, supplies, and more."
                                    },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (hasActiveFilters) {
                                OutlinedButton(
                                    onClick = {
                                        minInputText = ""
                                        maxInputText = ""
                                        viewModel.clearFilters()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text("Reset Filters")
                                }
                            } else {
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = ExpenseOrange,
                                            contentColor = Color.White,
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add First Expense")
                                }
                            }
                        }
                    }
                } else {
                    // Expenses List Items
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            dateDisplay = cardDateTimeFmt.format(Date(expense.timestamp)),
                            onDelete = { expenseToDelete = expense },
                        )
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { type, description, amount, supplierName, supplierPhone ->
                viewModel.addExpense(
                    type = type,
                    amount = amount,
                    notes = description,
                    supplierName = supplierName,
                    supplierPhone = supplierPhone,
                )
                showAddDialog = false
            },
        )
    }

    // Delete Confirmation Dialog
    if (expenseToDelete != null) {
        val toDelete = expenseToDelete!!
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            icon = {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp),
                )
            },
            title = {
                Text("Delete Expense", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    "Are you sure you want to delete this expense of ${toDelete.amount.toRupeeWithDecimals()} (${toDelete.description})?",
                    textAlign = TextAlign.Center,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(toDelete.id)
                        expenseToDelete = null
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
private fun ExpenseItemCard(
    expense: ExpenseEntry,
    dateDisplay: String,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Top Row: Date & Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = dateDisplay,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Type Badge (Matching Web pill style)
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = expense.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Middle Row: Description & Supplier
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = expense.description,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val supplier = expense.supplierName
                if (!supplier.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = supplier,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Bottom Row: Amount & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = expense.amount.toRupeeWithDecimals(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = ExpenseOrange,
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete Expense",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, description: String, amount: Double, supplierName: String?, supplierPhone: String?) -> Unit,
) {
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("supplies") }
    var amountText by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commonCategories =
        listOf(
            "supplies",
            "rent",
            "electricity",
            "salaries",
            "maintenance",
            "overhead",
            "misc",
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Expense",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Description (required)
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        errorMessage = null
                    },
                    label = { Text("Description *") },
                    placeholder = { Text("e.g. Shop electricity bill, Packaging boxes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )

                // Category Quick Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Category / Type *",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        items(commonCategories) { cat ->
                            val isSelected = type.equals(cat, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { type = cat },
                                label = {
                                    Text(
                                        text = cat.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                        fontSize = 11.sp,
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ExpenseOrange.copy(alpha = 0.15f),
                                        selectedLabelColor = ExpenseOrange,
                                    ),
                            )
                        }
                    }
                }

                // Amount (required)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (₹) *") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )

                // Supplier (optional)
                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("Supplier Name (Optional)") },
                    placeholder = { Text("e.g. Vendor name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedDesc = description.trim()
                    val amt = amountText.toDoubleOrNull()
                    if (trimmedDesc.isBlank()) {
                        errorMessage = "Please enter an expense description"
                        return@Button
                    }
                    if (amt == null || amt <= 0.0) {
                        errorMessage = "Please enter a valid amount greater than 0"
                        return@Button
                    }
                    onSave(
                        type.trim().ifBlank { "misc" },
                        trimmedDesc,
                        amt,
                        supplierName.trim().takeIf { it.isNotBlank() },
                        supplierPhone.trim().takeIf { it.isNotBlank() },
                    )
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = ExpenseOrange,
                        contentColor = Color.White,
                    ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Save Expense", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}
