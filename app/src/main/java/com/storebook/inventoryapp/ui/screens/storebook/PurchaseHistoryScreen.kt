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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.PurchaseItemDetail
import com.storebook.inventoryapp.ui.theme.Emerald500
import com.storebook.inventoryapp.ui.theme.primaryGradient
import com.storebook.inventoryapp.ui.viewmodel.PurchaseViewModel
import com.storebook.inventoryapp.utils.autoMarquee
import com.storebook.inventoryapp.utils.toRupee
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseHistoryScreen(
    navController: NavController,
    viewModel: PurchaseViewModel,
) {
    val scope = rememberCoroutineScope()
    val allPurchases by viewModel.purchases.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var minAmountFilter by remember { mutableStateOf("") }
    var maxAmountFilter by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.syncPurchasesFromCloud()
    }

    val displayDateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    // Filter purchases: BILL type, search by supplier or notes, amount range
    val filteredPurchases by remember(
        allPurchases,
        searchQuery,
        minAmountFilter,
        maxAmountFilter,
    ) {
        derivedStateOf {
            allPurchases.filter { p ->
                // Type filter: show bills / restocks
                if (p.type == "PAYMENT") return@filter false

                // Text search by supplier or notes
                if (searchQuery.isNotBlank()) {
                    val q = searchQuery.trim().lowercase()
                    val matchesSupplier = p.supplierName.lowercase().contains(q)
                    val matchesNotes = p.notes?.lowercase()?.contains(q) == true
                    if (!matchesSupplier && !matchesNotes) return@filter false
                }

                // Amount range filter
                val minAmt = minAmountFilter.toDoubleOrNull()
                if (minAmt != null && p.totalAmount < minAmt) return@filter false

                val maxAmt = maxAmountFilter.toDoubleOrNull()
                if (maxAmt != null && p.totalAmount > maxAmt) return@filter false

                true
            }
        }
    }

    val totalPurchaseAmount by remember(filteredPurchases) {
        derivedStateOf { filteredPurchases.sumOf { it.totalAmount } }
    }

    val totalItemsCount by remember(filteredPurchases) {
        derivedStateOf { filteredPurchases.sumOf { it.items.size } }
    }

    Scaffold(
        topBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.primaryGradient)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Purchase History",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text(
                                text = "Wholesale purchases & restock history",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                isRefreshing = true
                                viewModel.syncPurchasesFromCloud {
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar by Supplier or Notes
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search by supplier or notes...",
                            modifier = Modifier.autoMarquee(),
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
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Rounded.Cancel,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
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
                    viewModel.syncPurchasesFromCloud {
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Amount Range Filter
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Amount:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            OutlinedTextField(
                                value = minAmountFilter,
                                onValueChange = { minAmountFilter = it },
                                placeholder = { Text("Min ₹", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(10.dp),
                            )
                            Text("-", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(
                                value = maxAmountFilter,
                                onValueChange = { maxAmountFilter = it },
                                placeholder = { Text("Max ₹", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(10.dp),
                            )
                            if (minAmountFilter.isNotEmpty() || maxAmountFilter.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        minAmountFilter = ""
                                        maxAmountFilter = ""
                                    },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Cancel,
                                        contentDescription = "Clear amount filter",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // Summary Metric Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "Total Wholesale Purchases",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = totalPurchaseAmount.toRupee(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${filteredPurchases.size} Bills",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "$totalItemsCount Items",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Empty State
                if (filteredPurchases.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShoppingBag,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No purchase history found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Wholesale purchases & restocks will appear here.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                } else {
                    items(filteredPurchases, key = { it.id }) { purchase ->
                        PurchaseHistoryCard(
                            purchase = purchase,
                            formattedDate = displayDateFmt.format(Date(purchase.timestamp)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseHistoryCard(
    purchase: Purchase,
    formattedDate: String,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val initial =
        purchase.supplierName
            .take(2)
            .uppercase()
            .ifBlank { "SU" }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Top Row: Avatar, Supplier Name, Date, Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initial,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = purchase.supplierName.ifBlank { "Direct Supplier" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = purchase.totalAmount.toRupee(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "${purchase.items.size} item${if (purchase.items.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = Emerald500,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Notes
            val notes = purchase.notes
            if (!notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: $notes",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Items Toggle / Preview
            if (purchase.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isExpanded) "Hide items" else "View ${purchase.items.size} items",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector =
                            if (isExpanded) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        purchase.items.forEach { item ->
                            PurchaseItemRow(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseItemRow(item: PurchaseItemDetail) {
    val lineTotal = item.buyPrice * item.quantity
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.itemName,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${item.quantity} ${item.unit} @ ${item.buyPrice.toRupee()}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = lineTotal.toRupee(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
