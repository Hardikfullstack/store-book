@file:android.annotation.SuppressLint("LocalContextGetResourceValueCall")

package com.storebook.inventoryapp.ui.screens.storebook
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.ui.navigation.Routes
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.theme.PrimaryButton
import com.storebook.inventoryapp.ui.theme.primaryGradient
import com.storebook.inventoryapp.ui.viewmodel.DashboardViewModel
import com.storebook.inventoryapp.ui.viewmodel.SalesViewModel
import com.storebook.inventoryapp.ui.viewmodels.AppPermission
import com.storebook.inventoryapp.ui.viewmodels.hasPermission
import com.storebook.inventoryapp.utils.autoMarquee
import com.storebook.inventoryapp.utils.toRupee
import com.storebook.inventoryapp.utils.toRupeeWithDecimals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
private fun DashboardSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    )
}

@Composable
private fun SyncStatusBadge(
    uiSyncStatus: com.storebook.inventoryapp.ui.viewmodel.UiSyncStatus, // adjust to your actual type
    onRetry: () -> Unit,
) {
    val isSyncing = uiSyncStatus.isSyncing
    val isFailed = uiSyncStatus.status == "FAILED"

    val badgeColor by
        animateColorAsState(
            targetValue =
                when {
                    isSyncing -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                    uiSyncStatus.status in listOf("DONE", "IDLE") -> Color(0xFF4CAF50).copy(alpha = 0.22f)
                    isFailed -> MaterialTheme.colorScheme.error.copy(alpha = 0.22f)
                    else -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                },
            label = "sync_badge_color",
        )

    val syncLabelText =
        when (uiSyncStatus.status) {
            "PUSHING" -> "Pushing"
            "PULLING" -> "Pulling"
            "DONE", "IDLE" -> "Synced"
            "FAILED" -> if (uiSyncStatus.failedCount > 0) "Failed (${uiSyncStatus.failedCount})" else "Failed"
            else -> "Idle"
        }

    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(badgeColor)
                .clickable(
                    enabled = isFailed,
                    onClickLabel = "Retry sync",
                ) { if (isFailed) onRetry() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            when {
                isSyncing ->
                    CircularProgressIndicator(
                        modifier = Modifier.size(11.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    )
                isFailed ->
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    )
                else ->
                    Icon(
                        Icons.Outlined.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    )
            }
            Text(
                text = syncLabelText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel,
    salesViewModel: SalesViewModel,
) {
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val salesList by viewModel.salesList.collectAsStateWithLifecycle()
    val expensesList by viewModel.expensesList.collectAsStateWithLifecycle()
    // E01-S4: Observable sync status for badge + retry trigger
    val uiSyncStatus by viewModel.uiSyncStatus.collectAsStateWithLifecycle()
    val purchasesList by viewModel.purchases.collectAsStateWithLifecycle()
    val currentLastSaleId = salesViewModel.lastSaleId
    val currentLastSaleTime = salesViewModel.lastSaleTime
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadAllData()
    }

    // Greeting based on time of day
    val hourOfDay =
        remember(System.currentTimeMillis() / (3600 * 1000)) {
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        }
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

    // E03-S3: Today's profit is computed from SQL aggregate of sale_items (sell_price - buy_price
    // at time of sale)
    // NOT from current item prices — accurate even if you changed prices after the sale was
    // recorded
    val snapshot by viewModel.todaySnapshot.collectAsStateWithLifecycle()
    val todayRevenue = snapshot.todayRevenue
    val todayExpenses = snapshot.todayExpenses
    val todayProfit = snapshot.todayProfit

    val todaySales =
        remember(salesList) {
            salesList.filter { saleDateFmt.format(Date(it.timestamp)) == todayDateStr }
        }

    // 7-day trend calculations are now collected from ViewModel
    val last7DaysData by viewModel.last7DaysData.collectAsStateWithLifecycle()

    val salesTrend = last7DaysData.first
    val salesSumFirstHalf = salesTrend.take(4).sum()
    val salesSumSecondHalf = salesTrend.takeLast(4).sum()
    val salesIndicatorColor =
        when {
            salesSumSecondHalf > salesSumFirstHalf ->
                MaterialTheme.colorScheme.tertiary // Positive Growth (Green)
            salesSumSecondHalf < salesSumFirstHalf ->
                MaterialTheme.colorScheme.error // Declining (Red)
            else -> MaterialTheme.colorScheme.secondary // Stable/Neutral (Yellow)
        }
    val salesIndicatorLabel =
        when {
            salesSumSecondHalf > salesSumFirstHalf -> "Strong Sales Growth"
            salesSumSecondHalf < salesSumFirstHalf -> "Declining Sales Volume"
            else -> "Stable Sales Performance"
        }

    val purchasesIndicatorColor = MaterialTheme.colorScheme.secondary // Stable (Yellow)
    val purchasesIndicatorLabel = "Stock Inflow: Normal"

    val expensesSum = last7DaysData.third.sum()
    val salesSum = last7DaysData.first.sum()
    val ratio = if (salesSum > 0) expensesSum / salesSum else 0.0
    val expensesIndicatorColor =
        when {
            ratio > 0.20 -> MaterialTheme.colorScheme.error // High Expenses (Red)
            ratio > 0.05 -> MaterialTheme.colorScheme.secondary // Moderate Expenses (Yellow)
            else -> MaterialTheme.colorScheme.tertiary // Low Expenses (Green)
        }
    val expensesIndicatorLabel =
        when {
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
            containerColor = MaterialTheme.colorScheme.surface,
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
                        text =
                            "Current Stock: ${formatQty(refillItem.quantity)} ${refillItem.unit}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = addQtyInput,
                        onValueChange = { addQtyInput = it },
                        label = {
                            Text(
                                "Add Quantity",
                                modifier =
                                    androidx.compose.ui.Modifier
                                        .autoMarquee(),
                            )
                        },
                        keyboardOptions =
                            KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor =
                                    MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor =
                                    MaterialTheme.colorScheme.outline,
                                focusedLabelColor =
                                    MaterialTheme.colorScheme.primary,
                            ),
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
                PrimaryButton(
                    onClick = {
                        val addedQty = addQtyInput.toDoubleOrNull() ?: 0.0
                        if (addedQty > 0) {
                            // BUG-11 FIX: Use atomic delta-add instead of stale set-absolute.
                            viewModel.restockItem(refillItem.id, addedQty)
                        }
                        quickRefillItem = null
                    },
                ) { Text("Add Stock") }
            },
            dismissButton = {
                TextButton(onClick = { quickRefillItem = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.primaryGradient)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Single row: Title + PRO badge  |  Sync status + Avatar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style =
                                    MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                    ),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Box(
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (viewModel.isPremiumUser) {
                                                Gold400
                                            } else {
                                                MaterialTheme.colorScheme.onPrimary
                                                    .copy(alpha = 0.15f)
                                            },
                                        ).clickable(onClickLabel = if (viewModel.isPremiumUser) "View Pro plan" else "Upgrade to Pro") {
                                            navController.navigate(Routes.PremiumPlans)
                                        }.padding(horizontal = 9.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = if (viewModel.isPremiumUser) "★ PRO" else "FREE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (viewModel.isPremiumUser) Color(0xFF452E00) else MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SyncStatusBadge(
                                uiSyncStatus = uiSyncStatus,
                                onRetry = { viewModel.retrySync() },
                            )

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
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                                        .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), CircleShape)
                                        .clickable(
                                            onClickLabel = if (currentUser == null) "Sign in" else "View profile",
                                        ) {
                                            if (currentUser == null) {
                                                navController.navigate(Routes.Auth)
                                            } else {
                                                navController.navigate(Routes.More)
                                            }
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = avatarLetter,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            }
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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp), // base rhythm for top-level blocks
            ) {
                // Search or Sell Omnibox
                item {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth().clickable(onClickLabel = "Search or scan to sell") {
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
                            placeholder = {
                                Text(
                                    "Search or scan barcode to sell...",
                                    modifier =
                                        androidx.compose.ui.Modifier
                                            .autoMarquee(),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.ui_element_desc))
                            },
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

                // Low-stock alert panel
                if (lowStockItems.isNotEmpty()) {
                    item {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.05f))
                                    .border(
                                        1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                        RoundedCornerShape(
                                            16
                                                .dp,
                                        ),
                                    ),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                        .clickable(onClickLabel = "View all low stock items") {
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
                                    contentDescription = null,
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
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            // Item rows — grouped in their own Column with consistent internal rhythm
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                lowStockItems.take(3).forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                "Stock: ${formatQty(item.quantity)} ${item.unit}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                        androidx.compose.material3.Button(
                                            onClick = { quickRefillItem = item },
                                            colors =
                                                ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error,
                                                    contentColor = MaterialTheme.colorScheme.onError,
                                                ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp),
                                        ) {
                                            Text("Restock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
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
                                CardDefaults
                                    .cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.btn_undo) + "? (${undoSecondsLeft}s)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                androidx.compose.material3.Button(
                                    onClick = {
                                        salesViewModel.undoLastSale {
                                            android.widget.Toast
                                                .makeText(context, context.getString(R.string.toast_undo_success), android.widget.Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    colors =
                                        ButtonDefaults
                                            .buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                ) {
                                    Text(
                                        stringResource(id = R.string.btn_undo), fontWeight = FontWeight.Bold,
                                        fontSize =
                                            12
                                                .sp,
                                    )
                                }
                            }
                        }
                    }
                }

                // Today's Summary Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // major-group rhythm
                        Text(
                            text = stringResource(id = R.string.dash_today_summary),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        if (viewModel.userRoleType.hasPermission(
                                com.storebook.inventoryapp.ui.viewmodels.AppPermission.VIEW_FINANCIALS,
                            )
                        ) {
                            // "Today" group — label tightly bound to its content
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                DashboardSectionLabel("Today")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    AnimatedMetricCard(
                                        title = stringResource(id = R.string.dash_today_revenue),
                                        value = todayRevenue.toRupee(),
                                        gradient = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                                        iconContent = {
                                            Icon(
                                                Icons.Filled.Payments,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                    AnimatedMetricCard(
                                        title = stringResource(id = R.string.dash_today_expenses),
                                        value = todayExpenses.toRupee(),
                                        gradient = Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF334155))),
                                        iconContent = {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ReceiptLong,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }

                                if (viewModel.isPremiumUser) {
                                    AnimatedMetricCard(
                                        title = stringResource(id = R.string.dash_today_profit),
                                        value = todayProfit.toRupee(),
                                        gradient =
                                            if (todayProfit >= 0) {
                                                Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                                            } else {
                                                Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
                                            },
                                        iconContent = {
                                            Icon(
                                                imageVector =
                                                    if (todayProfit >=
                                                        0
                                                    ) {
                                                        Icons.AutoMirrored.Filled.TrendingUp
                                                    } else {
                                                        Icons.AutoMirrored.Filled.TrendingDown
                                                    },
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                } else {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(76.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outlineVariant
                                                        .copy(alpha = 0.4f),
                                                    RoundedCornerShape(16.dp),
                                                ).clickable(onClickLabel = "Upgrade to see today's profit") {
                                                    navController.navigate(Routes.PremiumPlans)
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary
                                                                .copy(alpha = 0.15f),
                                                        ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Lock,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = stringResource(id = R.string.dash_today_profit),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color =
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                            .copy(alpha = 0.7f),
                                                    maxLines = 1,
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Upgrade to Pro",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontFamily = Inter,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // "Last 7 Days" group
                            var showTrends by rememberSaveable { mutableStateOf(false) }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable(onClickLabel = "Toggle 7-day trends") {
                                                showTrends = !showTrends
                                            }.padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    DashboardSectionLabel("Last 7 Days")
                                    Icon(
                                        imageVector = if (showTrends) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (showTrends) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                AnimatedVisibility(visible = showTrends) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                }
                            }
                        }

                        // "Inventory Health" group
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashboardSectionLabel("Inventory Health")
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
                                            painter =
                                                androidx.compose.ui.res
                                                    .painterResource(id = R.drawable.ic_total_items),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    },
                                    modifier =
                                        Modifier.weight(1f).clickable(onClickLabel = "View all items") {
                                            navController.navigate(Routes.Inventory) {
                                                popUpTo<Routes.Dashboard> { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
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
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    },
                                    modifier =
                                        Modifier.weight(1f).clickable(
                                            onClickLabel = if (lowStockItems.isNotEmpty()) "View low stock items" else "View inventory",
                                        ) {
                                            navController.navigate(Routes.Inventory) {
                                                popUpTo<Routes.Dashboard> { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                )
                            }
                        }
                    }
                }

                // Extra breathing room before the primary action — this is the biggest semantic
                // transition on the screen (read-only info → primary CTA), so it gets more than
                // the base 12dp rhythm.
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Quick Sale CTA
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.primaryGradient)
                                .clickable(onClickLabel = "Record a new sale") {
                                    navController.navigate(Routes.Sales) {
                                        popUpTo<Routes.Dashboard> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                Icons.Outlined.ShoppingCart,
                                contentDescription = null,
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

                item { Spacer(modifier = Modifier.height(8.dp)) } // action → list transition

                // Recent Sales header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = R.string.dash_recent_sales), fontWeight = FontWeight.Bold,
                            fontSize =
                                15
                                    .sp,
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
                                Text(text = stringResource(id = R.string.btn_view_all), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (todaySales.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(32.dp),
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
                            onViewInvoice = { navController.navigate(Routes.InvoicePdfPreview(sale.id)) },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) } // bottom breathing room
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border =
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Total Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = totalValue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Sparkline (Mini Line Chart)
                Box(modifier = Modifier.width(90.dp).height(36.dp).padding(top = 4.dp)) {
                    if (trendData.size >= 2) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxVal = trendData.maxOrNull()?.toFloat() ?: 0f
                            val minVal = trendData.minOrNull()?.toFloat() ?: 0f
                            val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

                            val width = size.width
                            val height = size.height
                            val path =
                                androidx.compose.ui.graphics
                                    .Path()

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
                                style =
                                    androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = null,
                                        cap =
                                            androidx.compose.ui.graphics.StrokeCap
                                                .Round,
                                        join =
                                            androidx.compose.ui.graphics.StrokeJoin
                                                .Round,
                                    ),
                            )
                        }
                    } else {
                        // Empty/stable straight line sparkline
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(
                                color = indicatorColor,
                                start =
                                    androidx.compose.ui.geometry.Offset(
                                        0f,
                                        size.height / 2,
                                    ),
                                end =
                                    androidx.compose.ui.geometry.Offset(
                                        size.width,
                                        size.height / 2,
                                    ),
                                strokeWidth = 2.dp.toPx(),
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Traffic light indicator: a solid circle/dot
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(indicatorColor))
                Text(
                    text = indicatorLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = indicatorColor,
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
        modifier = modifier.fillMaxWidth().height(76.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border =
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Premium Icon Box with Gradient
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(gradient),
                contentAlignment = Alignment.Center,
            ) { iconContent() }
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
                    modifier = Modifier.autoMarquee(),
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
    onViewInvoice: () -> Unit = {},
) {
    var showPopup by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border =
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent dot
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.primaryGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = customerName.firstOrNull()?.uppercase() ?: "W",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier =
                            Modifier
                                .weight(1f, fill = false)
                                .autoMarquee(),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                        onClick = onViewInvoice,
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "View Invoice",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        )
                    }
                }
                if (sale.items.isNotEmpty()) {
                    if (sale.items.size == 1) {
                        val item = sale.items.first()
                        val priceText = item.sellPrice.toRupeeWithDecimals()
                        Text(
                            text =
                                "${item.itemName} (${item.quantity} ${item.unit} x $priceText)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.autoMarquee(),
                        )
                    } else {
                        val firstItem = sale.items.first()
                        val priceText = firstItem.sellPrice.toRupeeWithDecimals()
                        Text(
                            text =
                                "${firstItem.itemName} (${firstItem.quantity} ${firstItem.unit} x $priceText) +${sale.items.size - 1} more",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier =
                                Modifier
                                    .autoMarquee()
                                    .clickable(onClickLabel = "Action") {
                                        showPopup = true
                                    },
                        )
                    }
                } else {
                    val priceText = sale.totalAmount.toRupeeWithDecimals()
                    Text(
                        text = "Quick Cash Sale (1.0 pcs x $priceText)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.autoMarquee(),
                    )
                }
                Text(
                    text = saleTime,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${sale.totalAmount.toRupee()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
                val isLoss = profit < 0
                val absProfit = abs(profit)
                Text(
                    text =
                        if (isLoss) {
                            "-${absProfit.toRupee()} Loss"
                        } else {
                            "+${profit.toRupee()} Profit"
                        },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLoss) Coral500 else Emerald500,
                )
            }
        }
    }

    if (showPopup) {
        androidx.compose.material3.AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showPopup = false },
            title = {
                Text(
                    stringResource(id = R.string.dash_sale_details),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    if (sale.items.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Quick Cash Sale",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                )
                                Text(
                                    "1.0 pcs x ${sale.totalAmount.toRupeeWithDecimals()}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "${sale.totalAmount.toRupee()}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        sale.items.forEach { item ->
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
                                        overflow =
                                            androidx.compose.ui.text.style.TextOverflow
                                                .Ellipsis,
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
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
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
                imageVector =
                    androidx.compose.material.icons.Icons.Outlined.CheckCircleOutline,
                contentDescription = stringResource(R.string.ui_element_desc),
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(22.dp),
            )
        },
        modifier = modifier,
    )
}
