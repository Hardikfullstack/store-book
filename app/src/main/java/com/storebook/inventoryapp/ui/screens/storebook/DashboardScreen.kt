package com.storebook.inventoryapp.ui.screens.storebook

import com.storebook.inventoryapp.utils.toRupee
import com.storebook.inventoryapp.utils.toRupeeWithDecimals
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.ui.navigation.Routes
import com.storebook.inventoryapp.ui.theme.Coral500
import com.storebook.inventoryapp.ui.theme.Emerald500
import com.storebook.inventoryapp.ui.theme.Gold200
import com.storebook.inventoryapp.ui.theme.Gold400
import com.storebook.inventoryapp.ui.theme.InkBlue500
import com.storebook.inventoryapp.ui.theme.InkBlue700
import com.storebook.inventoryapp.ui.theme.SlateGray400
import com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: StoreBookViewModel) {
    val allItems by viewModel.allItems.collectAsState()
    val lowStockItems by viewModel.lowStockItems.collectAsState()
    val salesList by viewModel.salesList.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Greeting based on time of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "🌅 सुप्रभात!"
            hour < 17 -> "☀️ नमस्ते!"
            else -> "🌙 शुभ संध्या!"
        }
    }

    // Today's stats using derivedStateOf for performance
    val todayDateStr = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    val saleDateFmt = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()) }
    val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val todaySales by remember(salesList) {
        derivedStateOf {
            salesList.filter { saleDateFmt.format(Date(it.timestamp)) == todayDateStr }
        }
    }
    val todayRevenue by remember(todaySales) {
        derivedStateOf { todaySales.sumOf { it.totalAmount } }
    }
    val todayProfit by remember(todaySales) {
        derivedStateOf {
            todaySales.sumOf { sale ->
                sale.items.sumOf { (it.sellPrice - it.buyPrice) * it.quantity } - sale.discountAmount
            }
        }
    }

    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    // Undo last sale countdown
    var undoSecondsLeft by remember { mutableStateOf(0) }
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

    Scaffold(
        topBar = {
            // Modern gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(InkBlue700, InkBlue500)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "व्यापार खाता बही",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }

                    // Premium pill badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (viewModel.isPremiumUser) Gold200.copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.15f)
                            )
                            .clickable { navController.navigate(Routes.PremiumPlans) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = if (viewModel.isPremiumUser) "★ PRO" else "FREE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (viewModel.isPremiumUser) Gold400 else Color.White
                        )
                    }
                }
            }
        }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Low-stock alert banner
                item {
                    AnimatedVisibility(
                        visible = lowStockItems.isNotEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Coral500.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Routes.Inventory) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Coral500.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Coral500, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(id = R.string.dash_alert_banner, lowStockItems.size),
                                    color = Coral500,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Coral500)
                            }
                        }
                    }
                }

                // Undo last sale card
                item {
                    AnimatedVisibility(
                        visible = viewModel.lastSaleId != null && undoSecondsLeft > 0,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "बिक्री रद्द करें? (${undoSecondsLeft}s)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Button(
                                    onClick = {
                                        viewModel.undoLastSale {
                                            android.widget.Toast.makeText(
                                                context,
                                                context.getString(R.string.toast_undo_success),
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(stringResource(id = R.string.btn_undo), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Horizontally scrollable stat cards
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 8.dp)
                        ) {
                            item {
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_today_revenue),
                                    value = "${todayRevenue.toRupee()}",
                                    gradient = Brush.linearGradient(listOf(InkBlue700, InkBlue500)),
                                    emoji = "💰"
                                )
                            }
                            item {
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_today_profit),
                                    value = "${todayProfit.toRupee()}",
                                    gradient = Brush.linearGradient(listOf(Color(0xFF059669), Emerald500)),
                                    emoji = "📈"
                                )
                            }
                            item {
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_total_items),
                                    value = "${allItems.size}",
                                    gradient = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))),
                                    emoji = "📦"
                                )
                            }
                            item {
                                AnimatedMetricCard(
                                    title = stringResource(id = R.string.dash_low_stock),
                                    value = "${lowStockItems.size}",
                                    gradient = if (lowStockItems.isNotEmpty())
                                        Brush.linearGradient(listOf(Color(0xFFDC2626), Coral500))
                                    else Brush.linearGradient(listOf(SlateGray400, SlateGray400)),
                                    emoji = if (lowStockItems.isNotEmpty()) "⚠️" else "✅"
                                )
                            }
                        }
                    }
                }

                // Quick Sale CTA
                item {
                    Button(
                        onClick = { navController.navigate(Routes.Sales) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.btn_quick_sale),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Recent Sales header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.dash_recent_sales),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${todaySales.size} sales",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            androidx.compose.material3.TextButton(
                                onClick = { navController.navigate(Routes.SalesHistory) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "View All",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (todaySales.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🛒", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = R.string.dash_no_sales_today),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(todaySales, key = { it.id }) { sale ->
                        val saleTime = remember(sale.timestamp) { timeFmt.format(Date(sale.timestamp)) }
                        val profit = remember(sale) {
                            sale.items.sumOf { (it.sellPrice - it.buyPrice) * it.quantity } - sale.discountAmount
                        }
                        SaleTimelineCard(
                            sale = sale,
                            customerName = sale.customerName ?: "Walk-in Customer",
                            saleTime = saleTime,
                            profit = profit
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
    gradient: Brush,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .height(110.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = emoji, fontSize = 22.sp)
                Column {
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
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
    profit: Double
) {
    var showPopup by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent dot
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customerName.firstOrNull()?.uppercase() ?: "W",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (sale.items.size == 1) {
                    val item = sale.items.first()
                    Text(
                        text = "${item.itemName} (${item.quantity} ${item.unit} x ${item.sellPrice.toRupeeWithDecimals()})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = saleTime,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Text(
                        text = "${sale.items.size} items (View details)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showPopup = true }
                    )
                    Text(
                        text = saleTime,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${sale.totalAmount.toRupee()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                val isLoss = profit < 0
                val absProfit = Math.abs(profit)
                Text(
                    text = if (isLoss) "-${absProfit.toRupee()} नुकसान" else "+${profit.toRupee()} लाभ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLoss) Coral500 else Emerald500
                )
            }
        }
    }

    if (showPopup) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPopup = false },
            title = { Text("Sale Details", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sale.items) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(item.itemName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${item.quantity} ${item.unit} x ${item.sellPrice.toRupeeWithDecimals()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${(item.quantity * item.sellPrice).toRupee()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPopup = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// Legacy MetricCard kept for compatibility
@Composable
fun MetricCard(title: String, value: String, gradient: Brush, modifier: Modifier = Modifier) {
    AnimatedMetricCard(title = title, value = value, gradient = gradient, emoji = "📊", modifier = modifier)
}
