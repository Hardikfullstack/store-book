package com.storebook.inventoryapp.ui.screens.storebook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.data.repository.Sale
import com.storebook.inventoryapp.data.repository.StoreBookRepository
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel
import com.storebook.inventoryapp.utils.toRupee
import com.storebook.inventoryapp.utils.toRupeeWithDecimals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class GroupBy { DATE, PRODUCT, CUSTOMER }

data class LineItem(
    val saleId: Long,
    val timestamp: Long,
    val customerName: String,
    val itemId: Long,
    val itemName: String,
    val quantity: Double,
    val unit: String,
    val sellPrice: Double,
    val buyPrice: Double,
    val discountSplit: Double,
) {
    val revenue get() = (quantity * sellPrice)
    val profit get() = (sellPrice - buyPrice) * quantity - discountSplit
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesAnalyticsScreen(
    navController: NavController,
    viewModel: StoreBookViewModel,
) {
    var rawSales by remember { mutableStateOf<List<Sale>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val defaultCustName = stringResource(id = R.string.customer_walk_in)
    val repository = remember { StoreBookRepository(context) }

    // Filters
    var groupBy by remember { mutableStateOf(GroupBy.PRODUCT) }
    var customerFilter by remember { mutableStateOf<Set<String>>(emptySet()) }
    var productFilter by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Smart Quick Date Filters (1-click approach)
    var quickDateFilter by remember { mutableStateOf("All Time") } // "All Time", "Today", "This Month", "Custom"
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }

    // Dialog & Sheet States
    var activeSheet by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            rawSales = repository.getSalesPage(limit = 5000, offset = 0)
        }
        isLoading = false
    }

    val lineItems =
        remember(rawSales, defaultCustName) {
            rawSales.flatMap { sale ->
                val custName = sale.customerName ?: defaultCustName

                // Calculate total revenue of the sale to distribute discount proportionally
                val totalSaleRevenue = sale.items.sumOf { it.sellPrice * it.quantity }

                sale.items.map { item ->
                    val itemRevenue = item.sellPrice * item.quantity
                    // Pro-rata discount based on item's contribution to total revenue
                    val discountSplit =
                        if (totalSaleRevenue > 0) {
                            (itemRevenue / totalSaleRevenue) * sale.discountAmount
                        } else {
                            sale.discountAmount / (if (sale.items.isNotEmpty()) sale.items.size else 1).toDouble()
                        }

                    LineItem(
                        saleId = sale.id,
                        timestamp = sale.timestamp,
                        customerName = custName,
                        itemId = item.itemId,
                        itemName = item.itemName,
                        quantity = item.quantity,
                        unit = item.unit,
                        sellPrice = item.sellPrice,
                        buyPrice = item.buyPrice,
                        discountSplit = discountSplit,
                    )
                }
            }
        }

    val filteredItems =
        remember(lineItems, quickDateFilter, customStartDate, customEndDate, customerFilter, productFilter) {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            lineItems.filter { item ->
                val matchCustomer = customerFilter.isEmpty() || item.customerName in customerFilter
                val matchProduct = productFilter.isEmpty() || item.itemName in productFilter
                val matchDate =
                    when (quickDateFilter) {
                        "Today" -> {
                            calendar.timeInMillis = now
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            val startOfDay = calendar.timeInMillis
                            item.timestamp >= startOfDay
                        }
                        "This Month" -> {
                            calendar.timeInMillis = now
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            val startOfMonth = calendar.timeInMillis
                            item.timestamp >= startOfMonth
                        }
                        "Custom" -> {
                            if (customStartDate != null) {
                                calendar.timeInMillis = customStartDate!!
                                calendar.set(Calendar.HOUR_OF_DAY, 0)
                                calendar.set(Calendar.MINUTE, 0)
                                calendar.set(Calendar.SECOND, 0)
                                val startOfDay = calendar.timeInMillis

                                val endTs = customEndDate ?: customStartDate!!
                                calendar.timeInMillis = endTs
                                calendar.set(Calendar.HOUR_OF_DAY, 23)
                                calendar.set(Calendar.MINUTE, 59)
                                calendar.set(Calendar.SECOND, 59)
                                val endOfDay = calendar.timeInMillis
                                item.timestamp in startOfDay..endOfDay
                            } else {
                                true
                            }
                        }
                        else -> true
                    }
                matchCustomer && matchProduct && matchDate
            }
        }

    val totalRevenue = filteredItems.sumOf { it.revenue }
    val totalProfit = filteredItems.sumOf { it.profit }

    val allCustomers = remember(lineItems) { lineItems.map { it.customerName }.distinct().sorted() }
    val allProducts = remember(lineItems) { lineItems.map { it.itemName }.distinct().sorted() }

    if (showDateRangePicker) {
        val drpState =
            rememberDateRangePickerState(
                initialSelectedStartDateMillis = customStartDate ?: System.currentTimeMillis(),
                initialSelectedEndDateMillis = customEndDate,
            )
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    if (drpState.selectedStartDateMillis != null) {
                        customStartDate = drpState.selectedStartDateMillis
                        customEndDate = drpState.selectedEndDateMillis
                        quickDateFilter = "Custom"
                    }
                    showDateRangePicker = false
                }) { Text(stringResource(id = R.string.ana_apply_custom)) }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text(stringResource(id = R.string.btn_cancel)) }
            },
        ) {
            Column {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        androidx.compose.material3.OutlinedButton(onClick = {
                            quickDateFilter = "All Time"
                            showDateRangePicker =
                                false
                        }) { Text(stringResource(id = R.string.ana_all_time)) }
                    }
                    item {
                        androidx.compose.material3.OutlinedButton(onClick = {
                            quickDateFilter = "Today"
                            showDateRangePicker =
                                false
                        }) { Text(stringResource(id = R.string.ana_today)) }
                    }
                    item {
                        androidx.compose.material3.OutlinedButton(onClick = {
                            quickDateFilter = "This Month"
                            showDateRangePicker =
                                false
                        }) { Text(stringResource(id = R.string.ana_this_month)) }
                    }
                }
                HorizontalDivider()
                DateRangePicker(state = drpState, modifier = Modifier.weight(1f))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.ana_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            stringResource(id = R.string.ana_total_revenue),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "${totalRevenue.toRupee()}",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(id = R.string.ana_net_profit),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        val totalProfitAbs = abs(totalProfit)
                        Text(
                            text =
                                if (totalProfit >=
                                    0
                                ) {
                                    "${totalProfitAbs.toRupee()}"
                                } else {
                                    "-${totalProfitAbs.toRupee()}"
                                },
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = if (totalProfit >= 0) Emerald500 else Coral500,
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            // Segmented Control for View Mode (Group By)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val options =
                    listOf(
                        GroupBy.PRODUCT to stringResource(R.string.ana_group_products),
                        GroupBy.CUSTOMER to stringResource(R.string.ana_group_customers),
                        GroupBy.DATE to stringResource(R.string.ana_group_timeline),
                    )
                options.forEach { (g, label) ->
                    val isSelected = groupBy == g
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { groupBy = g },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Smart Filter Chips Row - separated from View Mode
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    val dateLabel =
                        when (quickDateFilter) {
                            "Today" -> "📅 " + stringResource(R.string.ana_date_today)
                            "This Month" -> "📅 " + stringResource(R.string.ana_date_this_month)
                            "Custom" -> {
                                if (customStartDate != null) {
                                    val sf = SimpleDateFormat("dd MMM", Locale.getDefault())
                                    if (customEndDate != null && customEndDate != customStartDate) {
                                        "📅 ${sf.format(Date(customStartDate!!))} - ${sf.format(Date(customEndDate!!))}"
                                    } else {
                                        "📅 ${sf.format(Date(customStartDate!!))}"
                                    }
                                } else {
                                    "📅 " + stringResource(R.string.ana_date_custom)
                                }
                            }
                            else -> "📅 " + stringResource(R.string.ana_date_all_time)
                        }

                    FilterChip(
                        selected = quickDateFilter != "All Time",
                        onClick = { showDateRangePicker = true },
                        label = { Text(dateLabel) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp)) },
                    )
                }
                item {
                    val custLabel =
                        if (customerFilter.isEmpty()) {
                            stringResource(R.string.ana_group_customers)
                        } else if (customerFilter.size ==
                            1
                        ) {
                            "Cust: ${customerFilter.first().take(6)}.."
                        } else {
                            "${customerFilter.size} Custs"
                        }
                    FilterChip(
                        selected = customerFilter.isNotEmpty(),
                        onClick = {
                            activeSheet = "CUSTOMER"
                            showSheet = true
                        },
                        label = { Text(custLabel) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp)) },
                    )
                }
                item {
                    val prodLabel =
                        if (productFilter.isEmpty()) {
                            stringResource(R.string.ana_group_products)
                        } else if (productFilter.size ==
                            1
                        ) {
                            "Prod: ${productFilter.first().take(6)}.."
                        } else {
                            "${productFilter.size} Prods"
                        }
                    FilterChip(
                        selected = productFilter.isNotEmpty(),
                        onClick = {
                            activeSheet = "PRODUCT"
                            showSheet = true
                        },
                        label = { Text(prodLabel) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp)) },
                    )
                }
            }

            HorizontalDivider()

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.ana_no_sales), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (groupBy) {
                        GroupBy.DATE -> {
                            items(filteredItems.sortedByDescending { it.timestamp }) { item ->
                                FlatLineItemCard(item, onShare = { viewModel.shareInvoice(context, item.saleId) })
                            }
                        }
                        GroupBy.PRODUCT -> {
                            val grouped =
                                filteredItems.groupBy { it.itemName }.toList().sortedByDescending {
                                    it.second.sumOf { i ->
                                        i.revenue
                                    }
                                }
                            items(grouped) { (prodName, itemsList) ->
                                ExpandableGroupCard(title = prodName, items = itemsList, groupBy = groupBy, onShareItem = { saleId ->
                                    viewModel.shareInvoice(context, saleId)
                                })
                            }
                        }
                        GroupBy.CUSTOMER -> {
                            val grouped =
                                filteredItems.groupBy { it.customerName }.toList().sortedByDescending {
                                    it.second.sumOf { i ->
                                        i.revenue
                                    }
                                }
                            items(grouped) { (custName, itemsList) ->
                                ExpandableGroupCard(title = custName, items = itemsList, groupBy = groupBy, onShareItem = { saleId ->
                                    viewModel.shareInvoice(context, saleId)
                                })
                            }
                        }
                    }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() },
            ) {
                Column(
                    modifier = Modifier
                        .imePadding()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    when (activeSheet) {
                        "CUSTOMER" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(id = R.string.ana_filter_customer),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (customerFilter.isNotEmpty()) {
                                    TextButton(
                                        onClick = { customerFilter = emptySet() },
                                    ) { Text(stringResource(id = R.string.ana_clear_all)) }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                                items(allCustomers) { cust ->
                                    val isSelected = cust in customerFilter
                                    SheetCheckboxOption(cust, checked = isSelected) {
                                        customerFilter =
                                            if (isSelected) customerFilter - cust else customerFilter + cust
                                    }
                                }
                            }
                        }
                        "PRODUCT" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(id = R.string.ana_filter_product),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (productFilter.isNotEmpty()) {
                                    TextButton(
                                        onClick = { productFilter = emptySet() },
                                    ) { Text(stringResource(id = R.string.ana_clear_all)) }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                                items(allProducts) { prod ->
                                    val isSelected = prod in productFilter
                                    SheetCheckboxOption(prod, checked = isSelected) {
                                        productFilter = if (isSelected) productFilter - prod else productFilter + prod
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SheetOption(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
        if (active) Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SheetCheckboxOption(
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal)
        Checkbox(checked = checked, onCheckedChange = { onClick() })
    }
}

@Composable
fun FlatLineItemCard(
    item: LineItem,
    onShare: () -> Unit,
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.itemName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${item.revenue.toRupee()}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = Poppins,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onShare, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${item.quantity} ${item.unit} x ${item.sellPrice.toRupeeWithDecimals()}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val profitAbs = abs(item.profit)
                Text(
                    text = if (item.profit >= 0) "Profit: ${profitAbs.toRupee()}" else "Loss: ${profitAbs.toRupee()}",
                    fontSize = 12.sp,
                    color = if (item.profit >= 0) Emerald500 else Coral500,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier =
                            Modifier
                                .size(
                                    16.dp,
                                ).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            item.customerName.take(1).uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        item.customerName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                Text(
                    dateFmt.format(Date(item.timestamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
fun ExpandableGroupCard(
    title: String,
    items: List<LineItem>,
    groupBy: GroupBy,
    onShareItem: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val totalRevenue = items.sumOf { it.revenue }
    val totalProfit = items.sumOf { it.profit }
    val totalQty = items.sumOf { it.quantity }
    val dateFmt = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        "${items.size} txns · $totalQty items",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${totalRevenue.toRupee()}",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val totalProfitAbs = abs(totalProfit)
                    Text(
                        text =
                            if (totalProfit >=
                                0
                            ) {
                                "Profit: ${totalProfitAbs.toRupee()}"
                            } else {
                                "Loss: ${totalProfitAbs.toRupee()}"
                            },
                        fontSize = 10.sp,
                        color = if (totalProfit >= 0) Emerald500 else Coral500,
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier =
                        Modifier.fillMaxWidth().background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        ),
                ) {
                    items.forEach { item ->
                        HorizontalDivider()
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val titleText = if (groupBy == GroupBy.PRODUCT) item.customerName else item.itemName
                                    Text(
                                        text = titleText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${item.revenue.toRupee()}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { onShareItem(item.saleId) },
                                            modifier = Modifier.size(20.dp),
                                        ) {
                                            Icon(
                                                Icons.Outlined.Share,
                                                contentDescription = "Share",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "${dateFmt.format(
                                            Date(item.timestamp),
                                        )} • ${item.quantity} ${item.unit} x ${item.sellPrice.toRupeeWithDecimals()}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    val itemProfitAbs = abs(item.profit)
                                    Text(
                                        text =
                                            if (item.profit >=
                                                0
                                            ) {
                                                "Prf: ${itemProfitAbs.toRupee()}"
                                            } else {
                                                "Loss: ${itemProfitAbs.toRupee()}"
                                            },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (item.profit >= 0) Emerald500 else Coral500,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
