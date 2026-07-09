@file:android.annotation.SuppressLint("LocalContextGetResourceValueCall")
package com.storebook.inventoryapp.ui.screens.storebook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.storebook.inventoryapp.ui.viewmodels.UserRole
import com.storebook.inventoryapp.ui.viewmodels.AppPermission
import com.storebook.inventoryapp.ui.viewmodels.hasPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.ui.navigation.Routes
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.viewmodel.DashboardViewModel
import com.storebook.inventoryapp.ui.viewmodel.SalesViewModel
import com.storebook.inventoryapp.utils.toRupee
import com.storebook.inventoryapp.utils.toRupeeWithDecimals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import com.storebook.inventoryapp.ui.theme.primaryGradient
import com.storebook.inventoryapp.ui.theme.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel,
    salesViewModel: SalesViewModel
) {
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val salesList by viewModel.salesList.collectAsStateWithLifecycle()
    val expensesList by viewModel.expensesList.collectAsStateWithLifecycle()
    val purchasesList by viewModel.purchases.collectAsStateWithLifecycle()
    val currentLastSaleId = salesViewModel.lastSaleId
    val currentLastSaleTime = salesViewModel.lastSaleTime
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Greeting based on time of day
    val hourOfDay = remember(System.currentTimeMillis() / (3600 * 1000)) { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingStr =
        when {
            hourOfDay < 12 -> stringResource(R.string.dash_greeting_morning)
            hourOfDay < 17 -> stringResource(R.string.dash_greeting_afternoon)
            else -> stringResource(R.string.dash_greeting_evening)
        }
    // Today's stats using derivedStateOf for performance
    val todayDateStr = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    val saleDateFmt = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()) }
    val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val todaySales =
        remember(salesList) {
            salesList.filter { saleDateFmt.format(Date(it.timestamp)) == todayDateStr }
        }
    val todayRevenue =
        remember(todaySales) {
            todaySales.sumOf { it.totalAmount }
        }
    val todayExpenses =
        remember(expensesList) {
            expensesList.filter { saleDateFmt.format(Date(it.timestamp)) == todayDateStr }
                .sumOf { it.amount }
        }
    val todayProfit =
        remember(todaySales, todayExpenses) {
            val grossProfit = todaySales.sumOf { sale ->
                sale.items.sumOf { (it.sellPrice - it.buyPrice) * it.quantity } - sale.discountAmount
            }
            grossProfit - todayExpenses
        }

    // 7-day trend calculations for Net Sales, Purchases, and Expenses
    val last7DaysData = remember(salesList, purchasesList, expensesList) {
        val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val days = (0..6).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            format.format(cal.time)
        }.reversed() // [day-6, day-5, ..., today]

        // Sales per day
        val salesPerDay = days.associateWith { dayStr ->
            salesList.filter { saleDateFmt.format(Date(it.timestamp)) == dayStr }.sumOf { it.totalAmount }
        }

        // Purchases per day
        val purchasesPerDay = days.associateWith { dayStr ->
            purchasesList.filter { saleDateFmt.format(Date(it.timestamp)) == dayStr }.sumOf { it.totalAmount }
        }

        // Expenses per day
        val expensesPerDay = days.associateWith { dayStr ->
            expensesList.filter { saleDateFmt.format(Date(it.timestamp)) == dayStr }.sumOf { it.amount }
        }

        Triple(
            days.map { salesPerDay[it] ?: 0.0 },
            days.map { purchasesPerDay[it] ?: 0.0 },
            days.map { expensesPerDay[it] ?: 0.0 }
        )
    }

    val salesTrend = last7DaysData.first
    val salesSumFirstHalf = salesTrend.take(3).sum()
    val salesSumSecondHalf = salesTrend.takeLast(3).sum()
    val salesIndicatorColor = when {
        salesSumSecondHalf > salesSumFirstHalf -> MaterialTheme.colorScheme.tertiary // Positive Growth (Green)
        salesSumSecondHalf < salesSumFirstHalf -> MaterialTheme.colorScheme.error // Declining (Red)
        else -> MaterialTheme.colorScheme.secondary // Stable/Neutral (Yellow)
    }
    val salesIndicatorLabel = when {
        salesSumSecondHalf > salesSumFirstHalf -> "Strong Sales Growth"
        salesSumSecondHalf < salesSumFirstHalf -> "Declining Sales Volume"
        else -> "Stable Sales Performance"
    }

    val purchasesIndicatorColor = MaterialTheme.colorScheme.secondary // Stable (Yellow)
    val purchasesIndicatorLabel = "Stock Inflow: Normal"

    val expensesSum = last7DaysData.third.sum()
    val salesSum = last7DaysData.first.sum()
    val ratio = if (salesSum > 0) expensesSum / salesSum else 0.0
    val expensesIndicatorColor = when {
        ratio > 0.20 -> MaterialTheme.colorScheme.error // High Expenses (Red)
        ratio > 0.05 -> MaterialTheme.colorScheme.secondary // Moderate Expenses (Yellow)
        else -> MaterialTheme.colorScheme.tertiary // Low Expenses (Green)
    }
    val expensesIndicatorLabel = when {
        ratio > 0.20 -> "High Overhead Expenses"
        ratio > 0.05 -> "Moderate Overhead Expenses"
        else -> "Optimal Low Expenses"
    }



    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    // Undo last sale countdown
    var undoSecondsLeft by remember { mutableStateOf(0) }
    var undoJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var quickRefillItem by remember { mutableStateOf<Item?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(currentLastSaleId, currentLastSaleTime) {
        if (currentLastSaleId != null) {
            val elapsed = (System.currentTimeMillis() - currentLastSaleTime) / 1000
            undoSecondsLeft = (30 - elapsed).toInt().coerceAtLeast(0)
            while (undoSecondsLeft > 0) {
                kotlinx.coroutines.delay(1000)
                undoSecondsLeft--
            }
            if (undoSecondsLeft == 0) {
                salesViewModel.clearLastSaleId()
            }
        } else {
            undoSecondsLeft = 0
        }
    }

    // ── Quick Refill Dialog ──────────────────────────────────────────────────
    if (quickRefillItem != null) {
        var addQtyInput by remember { mutableStateOf("") }
        val refillItem = quickRefillItem!!

        AlertDialog(
            onDismissRequest = { quickRefillItem = null },
            title = {
                Text(
                    text = "Refill Stock: ${refillItem.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column {
                    Text(
                        text = "Current Stock: ${formatQty(refillItem.quantity)} ${refillItem.unit}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = addQtyInput,
                        onValueChange = { addQtyInput = it },
                        label = { Text("Add Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Presets
                    val presets =
                        if (refillItem.unit in listOf("pcs", "dozen", "box", "packet")) {
                            listOf(5, 10, 50, 100)
                        } else {
                            listOf(5, 10, 25, 50)
                        }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets, key = { it }) { preset ->
                            FilterChip(
                                label = "+$preset",
                                isSelected = false,
                                onClick = {
                                    val currentVal = addQtyInput.toDoubleOrNull() ?: 0.0
                                    val formatted =
                                        if ((currentVal + preset) % 1.0 == 0.0) {
                                            (currentVal + preset).toInt().toString()
                                        } else {
                                            (currentVal + preset).toString()
                                        }
                                    addQtyInput = formatted
                                },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryButton(onClick = {
                    val addedQty = addQtyInput.toDoubleOrNull() ?: 0.0
                    if (addedQty > 0) {
                        viewModel.updateItem(refillItem.copy(quantity = refillItem.quantity + addedQty))
                    }
                    quickRefillItem = null
                }) {
                    Text("Add Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { quickRefillItem = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            // Modern gradient header with rounded bottom corners
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.primaryGradient)
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 22.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val greetingIcon = when {
                                hourOfDay < 12 -> Icons.Filled.WbTwilight
                                hourOfDay < 17 -> Icons.Filled.WbSunny
                                else -> Icons.Filled.ModeNight
                            }
                            Icon(greetingIcon, contentDescription = stringResource(R.string.ui_element_desc), modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "$greetingStr!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style =
                                    MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp,
                                    ),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.dash_subtitle),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Premium pill badge
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (viewModel.isPremiumUser) {
                                            Gold400
                                        } else {
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                                        },
                                    ).clickable(onClickLabel = "Action") { navController.navigate(Routes.PremiumPlans) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                        ) {
                            Text(
                                text = if (viewModel.isPremiumUser) "★ PRO" else "FREE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (viewModel.isPremiumUser) Color(0xFF452E00) else MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                        // Profile Avatar
                        val auth =
                            remember {
                                com.google.firebase.auth.FirebaseAuth
                                    .getInstance()
                            }
                        val currentUser = auth.currentUser
                        val avatarLetter =
                            remember(currentUser) {
                                val phone = currentUser?.phoneNumber
                                if (phone != null && phone.length > 3) {
                                    val digits = phone.filter { it.isDigit() }
                                    if (digits.isNotEmpty()) {
                                        // Skip the country code if possible by taking last 10, then first
                                        val mainNumber = if (digits.length >= 10) digits.takeLast(10) else digits
                                        mainNumber.take(1).uppercase()
                                    } else {
                                        "S"
                                    }
                                } else {
                                    "S"
                                }
                            }

                        Box(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                                    .clickable(onClickLabel = "Action") {
                                        if (currentUser == null) {
                                            navController.navigate(Routes.Auth)
                                        } else {
                                            android.widget.Toast
                                                .makeText(
                                                    context,
                                                    "Logged in: ${currentUser.phoneNumber}",
                                                    android.widget.Toast.LENGTH_SHORT,
                                                ).show()
                                        }
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = avatarLetter,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.loadAllData()
                    delay(600)
                    isRefreshing = false
                }
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Search or Sell Omnibox
                item {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth().clickable(onClickLabel = "Action") {
                                navController.navigate(Routes.Sales) {
                                    popUpTo<Routes.Dashboard> { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {},
                            placeholder = { Text("Search or scan barcode to sell...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.ui_element_desc)) },
                            enabled = false,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                ),
                            shape = CircleShape,
                        )
                    }
                }

                // Command Center - Quick Action Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { navController.navigate(Routes.Sales) },
                                label = { Text("New Sale", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = stringResource(R.string.ui_element_desc), modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(16.dp),
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { navController.navigate(Routes.Udhaar) },
                                label = { Text("Give Udhaar", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = stringResource(R.string.ui_element_desc), modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(16.dp),
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { navController.navigate(Routes.Inventory) },
                                label = { Text("Add Stock", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.ui_element_desc), modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(16.dp),
                            )
                        }
                    }
                }

                // Low-stock alert panel
                if (lowStockItems.isNotEmpty()) {
                    item {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.05f))
                                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        ) {
                            // Header
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .clickable(onClickLabel = "Action") {
                                            navController.navigate(Routes.Inventory) {
                                                popUpTo<Routes.Dashboard> { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = stringResource(R.string.ui_element_desc),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.dash_alert_banner, lowStockItems.size),
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f),
                                )
                                Text("View All", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = stringResource(R.string.ui_element_desc),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            // Items list (up to 3)
                            lowStockItems.take(3).forEach { item ->
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Stock: ${formatQty(item.quantity)} ${item.unit}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    androidx.compose.material3.Button(
                                        onClick = { quickRefillItem = item },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp),
                                    ) {
                                        Text("Restock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // Undo last sale card
                item {
                    AnimatedVisibility(
                        visible = currentLastSaleId != null && undoSecondsLeft > 0,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = stringResource(R.string.ui_element_desc),
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "बिक्री रद्द करें? (${undoSecondsLeft}s)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                androidx.compose.material3.Button(
                                    onClick = {
                                        salesViewModel.undoLastSale {
                                            android.widget.Toast
                                                .makeText(
                                                    context,
                                                    context.getString(R.string.toast_undo_success),
                                                    android.widget.Toast.LENGTH_SHORT,
                                                ).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                        ),
                                ) {
                                    Text(
                                        stringResource(id = R.string.btn_undo),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }

                // Today's Summary Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(id = R.string.dash_today_summary),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        // 2x2 Grid layout for stat cards to fit without scroll
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (viewModel.userRoleType.hasPermission(com.storebook.inventoryapp.ui.viewmodels.AppPermission.VIEW_FINANCIALS)) {
                                SparklineMetricCard(
                                    title = "Net Sales (Last 7 Days)",
                                    totalValue = last7DaysData.first.sum().toRupee(),
                                    trendData = last7DaysData.first,
                                    indicatorColor = salesIndicatorColor,
                                    indicatorLabel = salesIndicatorLabel,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                SparklineMetricCard(
                                    title = "Purchases (Last 7 Days)",
                                    totalValue = last7DaysData.second.sum().toRupee(),
                                    trendData = last7DaysData.second,
                                    indicatorColor = purchasesIndicatorColor,
                                    indicatorLabel = purchasesIndicatorLabel,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                SparklineMetricCard(
                                    title = "Expenses (Last 7 Days)",
                                    totalValue = last7DaysData.third.sum().toRupee(),
                                    trendData = last7DaysData.third,
                                    indicatorColor = expensesIndicatorColor,
                                    indicatorLabel = expensesIndicatorLabel,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_total_items),
                                    value = "${allItems.size}",
                                    gradient = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                                    iconContent = {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_total_items),
                                            contentDescription = stringResource(R.string.ui_element_desc),
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_low_stock),
                                    value = "${lowStockItems.size}",
                                    gradient =
                                        if (lowStockItems.isNotEmpty()) {
                                            Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
                                        } else {
                                            Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                                        },
                                    iconContent = {
                                        Icon(
                                            imageVector = if (lowStockItems.isNotEmpty()) Icons.Outlined.WarningAmber else Icons.Outlined.CheckCircleOutline,
                                            contentDescription = stringResource(R.string.ui_element_desc),
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                // Quick Sale CTA
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.primaryGradient)
                                .clickable(onClickLabel = "Action") {
                                    navController.navigate(Routes.Sales) {
                                        popUpTo<Routes.Dashboard> {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                Icons.Outlined.ShoppingCart,
                                contentDescription = stringResource(R.string.ui_element_desc),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Record Sale",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = Inter,
                            )
                        }
                    }
                }

                // Recent Sales header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = R.string.dash_recent_sales),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${todaySales.size} sales",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            androidx.compose.material3.TextButton(
                                onClick = { navController.navigate(Routes.SalesHistory) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp),
                            ) {
                                Text(
                                    text = stringResource(id = R.string.btn_view_all),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                if (todaySales.isEmpty()) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShoppingCart,
                                        contentDescription = stringResource(R.string.ui_element_desc),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(id = R.string.dash_no_sales_today),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                } else {
                    items(todaySales, key = { it.id }) { sale ->
                        val saleTime = remember(sale.timestamp) { timeFmt.format(Date(sale.timestamp)) }
                        val profit =
                            remember(sale) {
                                sale.items.sumOf { (it.sellPrice - it.buyPrice) * it.quantity } - sale.discountAmount
                            }
                        SaleTimelineCard(
                            sale = sale,
                            customerName = sale.customerName ?: stringResource(id = R.string.customer_walk_in),
                            saleTime = saleTime,
                            profit = profit,
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun SparklineMetricCard(
    title: String,
    totalValue: String,
    trendData: List<Double>,
    indicatorColor: Color,
    indicatorLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Total Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = totalValue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Sparkline (Mini Line Chart)
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(36.dp)
                        .padding(top = 4.dp)
                ) {
                    if (trendData.size >= 2) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxVal = trendData.maxOrNull()?.toFloat() ?: 0f
                            val minVal = trendData.minOrNull()?.toFloat() ?: 0f
                            val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

                            val width = size.width
                            val height = size.height
                            val path = androidx.compose.ui.graphics.Path()

                            trendData.forEachIndexed { index, value ->
                                val x = index * (width / (trendData.size - 1))
                                // Invert Y because canvas (0,0) is top-left
                                val y = height - ((value.toFloat() - minVal) / range) * height
                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = indicatorColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = null,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                )
                            )
                        }
                    } else {
                        // Empty/stable straight line sparkline
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(
                                color = indicatorColor,
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            // Traffic Light Indicator below
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Traffic light indicator: a solid circle/dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Text(
                    text = indicatorLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = indicatorColor
                )
            }
        }
    }
}

@Composable
fun AnimatedMetricCard(
    title: String,
    value: String,
    gradient: Brush,
    iconContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .height(76.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Premium Icon Box with Gradient
            Box(
                modifier =
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(gradient),
                contentAlignment = Alignment.Center,
            ) {
                iconContent()
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    letterSpacing = 0.3.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = Inter,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleTimelineCard(
    sale: com.storebook.inventoryapp.shared.domain.models.Sale,
    customerName: String,
    saleTime: String,
    profit: Double,
) {
    var showPopup by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent dot
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.primaryGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = customerName.firstOrNull()?.uppercase() ?: "W",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (sale.items.size == 1) {
                    val item = sale.items.first()
                    val priceText = item.sellPrice.toRupeeWithDecimals()
                    Text(
                        text = "${item.itemName} (${item.quantity} ${item.unit} x ${priceText})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = saleTime,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                } else {
                    Text(
                        text = "${sale.items.size} items (View details)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClickLabel = "Action") { showPopup = true },
                    )
                    Text(
                        text = saleTime,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${sale.totalAmount.toRupee()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
                val isLoss = profit < 0
                val absProfit = abs(profit)
                Text(
                    text = if (isLoss) "-${absProfit.toRupee()} नुकसान" else "+${profit.toRupee()} लाभ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLoss) Coral500 else Emerald500,
                )
            }
        }
    }

    if (showPopup) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPopup = false },
            title = { Text(stringResource(id = R.string.dash_sale_details), fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sale.items) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = item.itemName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    "${item.quantity} ${item.unit} x ${item.sellPrice.toRupeeWithDecimals()}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "${(item.quantity * item.sellPrice).toRupee()}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                PrimaryButton(onClick = { showPopup = false }) {
                    Text(stringResource(id = R.string.btn_close))
                }
            },
        )
    }
}

// Legacy MetricCard kept for compatibility
@Composable
fun MetricCard(
    title: String,
    value: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
) {
    AnimatedMetricCard(
        title = title,
        value = value,
        gradient = gradient,
        iconContent = {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Outlined.CheckCircleOutline,
                contentDescription = stringResource(R.string.ui_element_desc),
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(22.dp)
            )
        },
        modifier = modifier
    )
}
