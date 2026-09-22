@file:android.annotation.SuppressLint("LocalContextGetResourceValueCall")

package com.storebook.inventoryapp.ui.screens.storebook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.storebook.inventoryapp.shared.domain.models.StockAdjustment
import com.storebook.inventoryapp.ui.viewmodel.StockAuditViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAuditScreen(
    navController: NavController,
    viewModel: StockAuditViewModel,
) {
    val adjustments by viewModel.adjustments.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedReason by viewModel.selectedReason.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    var reasonDropdownExpanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val reasons = listOf("All Reasons", "Count Correction", "Damage", "Expiry", "Loss", "Restock")
    val dateDisplayFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeDisplayFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = startDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val sel = dpState.selectedDateMillis
                    if (sel != null) {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = sel
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        viewModel.setDateRange(cal.timeInMillis, endDate)
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
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
                TextButton(onClick = {
                    val sel = dpState.selectedDateMillis
                    if (sel != null) {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = sel
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        viewModel.setDateRange(startDate, cal.timeInMillis)
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = dpState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Audit Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            // Subtitle
            Text(
                text = "Timeline of all manual stock adjustments and discrepancies.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Search & Filter Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search items by name...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )

                    // Filters Row: Reason & Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Reason dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { reasonDropdownExpanded = true }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = selectedReason,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = reasonDropdownExpanded,
                                onDismissRequest = { reasonDropdownExpanded = false },
                            ) {
                                reasons.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r, fontSize = 13.sp) },
                                        onClick = {
                                            viewModel.setSelectedReason(r)
                                            reasonDropdownExpanded = false
                                        },
                                    )
                                }
                            }
                        }

                        // Date filter button
                        val hasDateFilter = startDate != null || endDate != null
                        val dateLabel = if (!hasDateFilter) {
                            "All Dates"
                        } else {
                            val s = startDate?.let { dateDisplayFmt.format(Date(it)) } ?: "Start"
                            val e = endDate?.let { dateDisplayFmt.format(Date(it)) } ?: "End"
                            "$s - $e"
                        }
                        Row(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (hasDateFilter) {
                                        viewModel.setDateRange(null, null)
                                    } else {
                                        showStartDatePicker = true
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = dateLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                )
                            }
                            if (hasDateFilter) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear date",
                                    modifier = Modifier.size(14.dp),
                                )
                            } else {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select date",
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }

                    // If start date is picked without end date, guide to pick end date
                    if (startDate != null && endDate == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { showEndDatePicker = true }) {
                                Text("Select End Date →", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                } else if (adjustments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                            Text(
                                text = "No stock adjustments found",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Stock adjustments from quantity edits and restocks will appear here.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(adjustments, key = { it.id }) { adjustment ->
                            StockAdjustmentCard(adjustment = adjustment, timeDisplayFmt = timeDisplayFmt)
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }

            // Pagination Footer
            val pageSize = viewModel.pageSize
            val totalPages = maxOf(1, ((totalCount + pageSize - 1) / pageSize).toInt())
            val startEntry = if (totalCount == 0L) 0 else (currentPage - 1) * pageSize + 1
            val endEntry = minOf(currentPage * pageSize, totalCount.toInt())

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Showing $startEntry to $endEntry of $totalCount entries",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { viewModel.goToPage(currentPage - 1) },
                        enabled = currentPage > 1,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Prev", fontSize = 12.sp)
                    }

                    Text(
                        text = "$currentPage / $totalPages",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )

                    OutlinedButton(
                        onClick = { viewModel.goToPage(currentPage + 1) },
                        enabled = currentPage < totalPages,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Next", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StockAdjustmentCard(
    adjustment: StockAdjustment,
    timeDisplayFmt: SimpleDateFormat,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Row 1: Item Name & Stock Delta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = adjustment.item_name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )

                val delta = adjustment.delta
                val isPositive = delta > 0
                val deltaText = if (delta % 1.0 == 0.0) {
                    if (isPositive) "+${delta.toInt()}" else "${delta.toInt()}"
                } else {
                    if (isPositive) "+%.2f".format(delta) else "%.2f".format(delta)
                }

                val deltaTextColor = if (isPositive) Color(0xFF047857) else Color(0xFFB91C1C)
                val deltaBgColor = if (isPositive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(deltaBgColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = deltaText,
                        color = deltaTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }

            // Row 2: Reason Pill & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReasonBadge(reason = adjustment.reason)

                Text(
                    text = timeDisplayFmt.format(Date(adjustment.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReasonBadge(reason: String) {
    val (textColor, bgColor) = when (reason) {
        "Damage" -> Color(0xFFB91C1C) to Color(0xFFFEE2E2)
        "Expiry" -> Color(0xFFC2410C) to Color(0xFFFFEDD5)
        "Loss" -> Color(0xFFBE123C) to Color(0xFFFFE4E6)
        "Restock" -> Color(0xFF047857) to Color(0xFFD1FAE5)
        "Count Correction" -> Color(0xFF1D4ED8) to Color(0xFFDBEAFE)
        else -> Color(0xFF4B5563) to Color(0xFFF3F4F6)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = reason,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
