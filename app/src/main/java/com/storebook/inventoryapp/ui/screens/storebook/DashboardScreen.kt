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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.data.repository.Item
import com.storebook.inventoryapp.ui.navigation.Routes
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel
import com.storebook.inventoryapp.utils.toRupee
import com.storebook.inventoryapp.utils.toRupeeWithDecimals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: StoreBookViewModel,
) {
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val salesList by viewModel.salesList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Greeting based on time of day
    val hourOfDay = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingStr =
        when {
            hourOfDay < 12 -> stringResource(R.string.dash_greeting_morning)
            hourOfDay < 17 -> stringResource(R.string.dash_greeting_afternoon)
            else -> stringResource(R.string.dash_greeting_evening)
        }
    val greeting =
        remember(greetingStr) {
            val prefix =
                when {
                    hourOfDay < 12 -> "🌅 "
                    hourOfDay < 17 -> "☀️ "
                    else -> "🌙 "
                }
            "$prefix$greetingStr!"
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
    val todayProfit =
        remember(todaySales) {
            todaySales.sumOf { sale ->
                sale.items.sumOf { (it.sellPrice - it.buyPrice) * it.quantity } - sale.discountAmount
            }
        }

    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    // Undo last sale countdown
    var undoSecondsLeft by remember { mutableStateOf(0) }
    var quickRefillItem by remember { mutableStateOf<Item?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(viewModel.lastSaleId, viewModel.lastSaleTime) {
        if (viewModel.lastSaleId != null) {
            val elapsed = (System.currentTimeMillis() - viewModel.lastSaleTime) / 1000
            undoSecondsLeft = (30 - elapsed).toInt().coerceAtLeast(0)
            while (undoSecondsLeft > 0) {
                delay(1000)
                undoSecondsLeft--
            }
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
                Button(onClick = {
                    val addedQty = addQtyInput.toDoubleOrNull() ?: 0.0
                    if (addedQty > 0) {
                        viewModel.updateItem(
                            id = refillItem.id,
                            name = refillItem.name,
                            quantity = refillItem.quantity + addedQty,
                            unit = refillItem.unit,
                            buyPrice = refillItem.buyPrice,
                            sellPrice = refillItem.sellPrice,
                            threshold = refillItem.lowStockThreshold,
                            category = refillItem.category,
                        )
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
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(InkBlue900, InkBlue700, InkBlue500),
                            ),
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 22.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = greeting,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style =
                                    MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp,
                                        fontFamily = Inter,
                                    ),
                                color = Color.White,
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.dash_subtitle),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f),
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
                                            Gold200.copy(alpha = 0.25f)
                                        } else {
                                            Color.White.copy(alpha = 0.15f)
                                        },
                                    ).clickable { navController.navigate(Routes.PremiumPlans) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                        ) {
                            Text(
                                text = if (viewModel.isPremiumUser) "★ PRO" else "FREE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (viewModel.isPremiumUser) Gold400 else Color.White,
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
                                    phone
                                        .replace("+91", "")
                                        .trim()
                                        .take(1)
                                        .uppercase()
                                } else {
                                    "S"
                                }
                            }

                        Box(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable {
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
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shadowElevation = 4.dp,
                            ) {
                                Text(
                                    text = "Add Product",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(Routes.Inventory) {
                                        popUpTo<Routes.Dashboard> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    fabExpanded = false
                                },
                                modifier = Modifier.size(48.dp),
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                            ) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp)) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shadowElevation = 4.dp,
                            ) {
                                Text(
                                    text = "Record Sale",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(Routes.Sales) {
                                        popUpTo<Routes.Dashboard> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    fabExpanded = false
                                },
                                modifier = Modifier.size(48.dp),
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shadowElevation = 4.dp,
                            ) {
                                Text(
                                    text = "Give Udhaar",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(Routes.Udhaar) {
                                        popUpTo<Routes.Dashboard> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    fabExpanded = false
                                },
                                modifier = Modifier.size(48.dp),
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ) {
                    Icon(
                        if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Quick Actions",
                    )
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
                            Modifier.fillMaxWidth().clickable {
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
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
                                    .background(Coral500.copy(alpha = 0.05f))
                                    .border(1.dp, Coral500.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        ) {
                            // Header
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .clickable {
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
                                    tint = Coral500,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.dash_alert_banner, lowStockItems.size),
                                    color = Coral500,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f),
                                )
                                Text("View All", color = Coral500, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Coral500,
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
                                            color = Coral500,
                                        )
                                    }
                                    Button(
                                        onClick = { quickRefillItem = item },
                                        colors = ButtonDefaults.buttonColors(containerColor = Coral500),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp),
                                    ) {
                                        Text("Restock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        visible = viewModel.lastSaleId != null && undoSecondsLeft > 0,
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
                                        contentDescription = null,
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
                                Button(
                                    onClick = {
                                        viewModel.undoLastSale {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_today_revenue),
                                    value = todayRevenue.toRupee(),
                                    gradient = Brush.linearGradient(listOf(InkBlue700, InkBlue500)),
                                    emoji = "💰",
                                    modifier = Modifier.weight(1f),
                                )
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_today_profit),
                                    value = todayProfit.toRupee(),
                                    gradient = Brush.linearGradient(listOf(Color(0xFF059669), Emerald500)),
                                    emoji = "📈",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_total_items),
                                    value = "${allItems.size}",
                                    gradient = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))),
                                    emoji = "📦",
                                    modifier = Modifier.weight(1f),
                                )
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_low_stock),
                                    value = "${lowStockItems.size}",
                                    gradient =
                                        if (lowStockItems.isNotEmpty()) {
                                            Brush.linearGradient(listOf(Color(0xFFDC2626), Coral500))
                                        } else {
                                            Brush.linearGradient(listOf(SlateGray400, SlateGray400))
                                        },
                                    emoji = if (lowStockItems.isNotEmpty()) "⚠️" else "✅",
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
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(InkBlue700, InkBlue500),
                                    ),
                                ).clickable {
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
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(id = R.string.btn_quick_sale),
                                color = Color.White,
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
                                Text("🛒", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = R.string.dash_no_sales_today),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    fontSize = 13.sp,
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
fun AnimatedMetricCard(
    title: String,
    value: String,
    gradient: Brush, // Keep for signature compatibility
    emoji: String,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val iconBgColor =
        remember(emoji, isDark) {
            when (emoji) {
                "💰" -> if (isDark) Color(0xFF312E81) else Color(0xFFEEF2FF)
                "📈" -> if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5)
                "📦" -> if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)
                else -> if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)
            }
        }
    val iconColor =
        remember(emoji, isDark) {
            when (emoji) {
                "💰" -> if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                "📈" -> if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                "📦" -> if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                else -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
            }
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    letterSpacing = 0.2.sp,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
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
    sale: com.storebook.inventoryapp.data.repository.Sale,
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
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = customerName.firstOrNull()?.uppercase() ?: "W",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
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
                        modifier = Modifier.clickable { showPopup = true },
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
                Button(onClick = { showPopup = false }) {
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
    AnimatedMetricCard(title = title, value = value, gradient = gradient, emoji = "📊", modifier = modifier)
}
