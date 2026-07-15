package com.storebook.inventoryapp.ui.screens.storebook
import com.storebook.inventoryapp.utils.autoMarquee

import com.storebook.inventoryapp.R

import androidx.compose.foundation.background
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.storebook.inventoryapp.data.billing.BillingEngine
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.viewmodel.SalesViewModel
import com.storebook.inventoryapp.utils.toBigDecimal
import com.storebook.inventoryapp.utils.sumOfBigDecimal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.storebook.inventoryapp.ui.theme.PrimaryButton

enum class GSTReportType(val title: String, val subtitle: String) {
    GSTR1("GSTR-1", "Sales / Outward Supplies"),
    GSTR2("GSTR-2", "Purchases / Inward Supplies"),
    GSTR3B("GSTR-3B", "Consolidated Summary"),
    DETAILED("Detailed GST", "Item-wise Breakup")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GSTReportScreen(navController: NavController, viewModel: com.storebook.inventoryapp.ui.viewmodel.DashboardViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Month/Year picker state
    val now = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }

    val monthNames = remember {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }

    // Selected Report Type State
    var activeReportType by remember { mutableStateOf(GSTReportType.GSTR1) }

    // Computed date range for selected month
    val (startTs, endTs) = remember(selectedMonth, selectedYear) {
        val cal = Calendar.getInstance()
        cal.set(selectedYear, selectedMonth, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        Pair(start, cal.timeInMillis)
    }

    var sales by remember { mutableStateOf<List<Sale>>(emptyList()) }
    var purchases by remember { mutableStateOf<List<Purchase>>(emptyList()) }
    var suppliersMap by remember { mutableStateOf<Map<Long, Supplier>>(emptyMap()) }
    var allItemsMap by remember { mutableStateOf<Map<Long, Item>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(startTs, endTs) {
        isLoading = true
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val fetchedSales = viewModel.getSalesByDateRange(startTs, endTs)
            val fetchedPurchases = viewModel.getPurchasesByDateRange(startTs, endTs)
            val fetchedSuppliers = viewModel.getAllSuppliersMap()
            val fetchedItems = viewModel.getAllItemsMap()
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                sales = fetchedSales
                purchases = fetchedPurchases
                suppliersMap = fetchedSuppliers
                allItemsMap = fetchedItems
                isLoading = false
            }
        }
    }

    // Business details
    val businessGstin = viewModel.businessGstin

    // Calculate active tax summaries for Sales
    val salesSummaries = remember(sales, businessGstin, allItemsMap) {
        sales.map { sale ->
            val cartItems = sale.items.map { saleItem ->
                val actualItem = allItemsMap[saleItem.itemId] ?: Item(
                    id = saleItem.itemId,
                    name = saleItem.itemName,
                    quantity = 0.0,
                    unit = saleItem.unit,
                    buyPrice = saleItem.buyPrice,
                    sellPrice = saleItem.sellPrice,
                    lowStockThreshold = 0.0,
                    category = ""
                )
                CartItem(item = actualItem.copy(sellPrice = saleItem.sellPrice), quantity = saleItem.quantity)
            }
            BillingEngine.calculateInvoiceTaxes(
                cartItems = cartItems,
                totalDiscount = sale.discountAmount,
                businessGstin = businessGstin,
                customerGstin = sale.customerGstin
            )
        }
    }

    // Calculate active tax summaries for Purchases
    val purchasesSummaries = remember(purchases, businessGstin, suppliersMap, allItemsMap) {
        purchases.map { purchase ->
            val supplier = suppliersMap[purchase.supplierId]
            val supplierGstin = supplier?.gstin ?: ""
            val cartItems = purchase.items.map { pi ->
                val actualItem = allItemsMap[pi.itemId] ?: Item(
                    id = pi.itemId,
                    name = pi.itemName,
                    quantity = 0.0,
                    unit = pi.unit,
                    buyPrice = pi.buyPrice,
                    sellPrice = pi.buyPrice,
                    lowStockThreshold = 0.0,
                    category = ""
                )
                CartItem(item = actualItem.copy(sellPrice = pi.buyPrice), quantity = pi.quantity)
            }
            BillingEngine.calculateInvoiceTaxes(
                cartItems = cartItems,
                totalDiscount = 0.0,
                businessGstin = businessGstin,
                customerGstin = supplierGstin
            )
        }
    }

    // GSTR-1 Sales Totals
    val totalSalesValue = remember(sales) { sales.sumOfBigDecimal { it.totalAmount.toBigDecimal() }.toDouble() }
    val totalSalesTaxable = remember(salesSummaries) { salesSummaries.sumOfBigDecimal { it.netTaxableAmount.toBigDecimal() }.toDouble() }
    val totalSalesCgst = remember(salesSummaries) { salesSummaries.sumOfBigDecimal { it.totalCgst.toBigDecimal() }.toDouble() }
    val totalSalesSgst = remember(salesSummaries) { salesSummaries.sumOfBigDecimal { it.totalSgst.toBigDecimal() }.toDouble() }
    val totalSalesIgst = remember(salesSummaries) { salesSummaries.sumOfBigDecimal { it.totalIgst.toBigDecimal() }.toDouble() }
    val totalSalesTax = (totalSalesCgst.toBigDecimal() + totalSalesSgst.toBigDecimal() + totalSalesIgst.toBigDecimal()).toDouble()

    // GSTR-2 Purchases Totals
    val totalPurchasesValue = remember(purchases) { purchases.sumOfBigDecimal { it.totalAmount.toBigDecimal() }.toDouble() }
    val totalPurchasesTaxable = remember(purchasesSummaries) { purchasesSummaries.sumOfBigDecimal { it.netTaxableAmount.toBigDecimal() }.toDouble() }
    val totalPurchasesCgst = remember(purchasesSummaries) { purchasesSummaries.sumOfBigDecimal { it.totalCgst.toBigDecimal() }.toDouble() }
    val totalPurchasesSgst = remember(purchasesSummaries) { purchasesSummaries.sumOfBigDecimal { it.totalSgst.toBigDecimal() }.toDouble() }
    val totalPurchasesIgst = remember(purchasesSummaries) { purchasesSummaries.sumOfBigDecimal { it.totalIgst.toBigDecimal() }.toDouble() }
    val totalPurchasesTax = (totalPurchasesCgst.toBigDecimal() + totalPurchasesSgst.toBigDecimal() + totalPurchasesIgst.toBigDecimal()).toDouble()

    val gradientBrush = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradientBrush)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("GST Compliance Reports", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary)
                        Text(activeReportType.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    }
                    IconButton(onClick = {
                        val monthName = monthNames[selectedMonth]
                        when (activeReportType) {
                            GSTReportType.GSTR1 -> viewModel.exportGSTR1Excel(context, startTs, endTs, "GSTR1_${monthName}_$selectedYear")
                            GSTReportType.GSTR2 -> viewModel.exportGSTR2Excel(context, startTs, endTs, "GSTR2_${monthName}_$selectedYear")
                            GSTReportType.GSTR3B -> viewModel.exportGSTR3BExcel(context, startTs, endTs, "GSTR3B_${monthName}_$selectedYear")
                            GSTReportType.DETAILED -> viewModel.exportGstdetailedExcel(context, startTs, endTs, "Detailed_GST_${monthName}_$selectedYear")
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export Excel", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Report Selector Toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GSTReportType.values().forEach { type ->
                    val isSelected = activeReportType == type
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
                        modifier = Modifier
                            .clickable(onClickLabel = "Action") { activeReportType = type }
                    ) {
                        Text(
                            text = type.title,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 2. Period Navigation Calendar Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectedMonth == 0) {
                            selectedMonth = 11; selectedYear--
                        } else selectedMonth--
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${monthNames[selectedMonth]} $selectedYear",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Reporting Period",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = {
                        if (selectedMonth == 11) {
                            selectedMonth = 0; selectedYear++
                        } else selectedMonth++
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 3. Render Summaries
                    item {
                        when (activeReportType) {
                            GSTReportType.GSTR1 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SummaryBlock("Total Sales", "₹${"%,.2f".format(totalSalesValue)}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                                        SummaryBlock("Taxable Value", "₹${"%,.2f".format(totalSalesTaxable)}", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                                        SummaryBlock("Total Tax Liability", "₹${"%,.2f".format(totalSalesTax)}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                                    }
                                    TaxBreakupCard(totalSalesCgst, totalSalesSgst, totalSalesIgst)
                                }
                            }
                            GSTReportType.GSTR2 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SummaryBlock("Total Purchases", "₹${"%,.2f".format(totalPurchasesValue)}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                                        SummaryBlock("Taxable Value", "₹${"%,.2f".format(totalPurchasesTaxable)}", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                                        SummaryBlock("Total ITC", "₹${"%,.2f".format(totalPurchasesTax)}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                                    }
                                    TaxBreakupCard(totalPurchasesCgst, totalPurchasesSgst, totalPurchasesIgst)
                                }
                            }
                            GSTReportType.GSTR3B -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SummaryBlock("Outward Tax Liability", "₹${"%,.2f".format(totalSalesTax)}", MaterialTheme.colorScheme.error, Modifier.weight(1f))
                                        SummaryBlock("Eligible ITC", "₹${"%,.2f".format(totalPurchasesTax)}", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                                    }
                                    val netPayable = totalSalesTax - totalPurchasesTax
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (netPayable >= 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    if (netPayable >= 0) "Net Tax Payable" else "ITC Carry Forward",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (netPayable >= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    "Consolidated GSTR-3B balance",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                "₹${"%,.2f".format(Math.abs(netPayable))}",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 20.sp,
                                                color = if (netPayable >= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                            GSTReportType.DETAILED -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryBlock("Total Taxable Value", "₹${"%,.2f".format(totalSalesTaxable + totalPurchasesTaxable)}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                                    SummaryBlock("Consolidated GST", "₹${"%,.2f".format(totalSalesTax + totalPurchasesTax)}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // 4. Section Preview Headers
                    item {
                        Text(
                            text = when (activeReportType) {
                                GSTReportType.GSTR1 -> "Sales Invoices (${sales.size})"
                                GSTReportType.GSTR2 -> "Purchase Bills (${purchases.size})"
                                GSTReportType.GSTR3B -> "Sectional Breakdown"
                                GSTReportType.DETAILED -> "Detailed Transaction Breakdown"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 5. Render Lists / Tables
                    when (activeReportType) {
                        GSTReportType.GSTR1 -> {
                            if (sales.isEmpty()) {
                                item { EmptyReportState("No sales found for this month.") }
                            } else {
                                items(sales, key = { it.id }) { sale ->
                                    val idx = sales.indexOf(sale)
                                    val summary = salesSummaries.getOrNull(idx)
                                    TransactionItemRow(
                                        title = sale.customerName ?: "Cash Customer",
                                        subtitle = "INV${sale.id.toString().padStart(5, '0')}",
                                        date = sale.timestamp,
                                        amount = sale.totalAmount,
                                        tax = (summary?.totalCgst ?: 0.0) + (summary?.totalSgst ?: 0.0) + (summary?.totalIgst ?: 0.0),
                                        isSale = true
                                    )
                                }
                            }
                        }
                        GSTReportType.GSTR2 -> {
                            if (purchases.isEmpty()) {
                                item { EmptyReportState("No purchases found for this month.") }
                            } else {
                                items(purchases, key = { it.id }) { purchase ->
                                    val idx = purchases.indexOf(purchase)
                                    val summary = purchasesSummaries.getOrNull(idx)
                                    TransactionItemRow(
                                        title = purchase.supplierName,
                                        subtitle = "PUR${purchase.id.toString().padStart(5, '0')}",
                                        date = purchase.timestamp,
                                        amount = purchase.totalAmount,
                                        tax = (summary?.totalCgst ?: 0.0) + (summary?.totalSgst ?: 0.0) + (summary?.totalIgst ?: 0.0),
                                        isSale = false
                                    )
                                }
                            }
                        }
                        GSTReportType.GSTR3B -> {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SectionBreakdownCard("1. Outward Supplies (Sales Liability)", totalSalesTaxable, totalSalesCgst, totalSalesSgst, totalSalesIgst, totalSalesTax)
                                    SectionBreakdownCard("2. Inward Supplies (Eligible ITC)", totalPurchasesTaxable, totalPurchasesCgst, totalPurchasesSgst, totalPurchasesIgst, totalPurchasesTax)
                                    SectionBreakdownCard(
                                        "3. Net Tax Summary",
                                        totalSalesTaxable - totalPurchasesTaxable,
                                        totalSalesCgst - totalPurchasesCgst,
                                        totalSalesSgst - totalPurchasesSgst,
                                        totalSalesIgst - totalPurchasesIgst,
                                        totalSalesTax - totalPurchasesTax
                                    )
                                }
                            }
                        }
                        GSTReportType.DETAILED -> {
                            val detailedRows = mutableListOf<DetailedRowData>()
                            sales.forEachIndexed { sIdx, sale ->
                                val summary = salesSummaries.getOrNull(sIdx)
                                summary?.itemDetails?.forEach { detail ->
                                    detailedRows.add(
                                        DetailedRowData(
                                            date = sale.timestamp,
                                            txnId = "INV${sale.id.padStart(5)}",
                                            type = "Sale",
                                            party = sale.customerName ?: "Cash Customer",
                                            item = detail.cartItem.item.name,
                                            hsn = detail.cartItem.item.hsnCode ?: "-",
                                            taxRate = detail.cartItem.item.taxRate,
                                            qty = detail.cartItem.quantity,
                                            taxable = detail.netAmountBeforeTax,
                                            tax = detail.totalTaxAmount
                                        )
                                    )
                                }
                            }
                            purchases.forEachIndexed { pIdx, purchase ->
                                val summary = purchasesSummaries.getOrNull(pIdx)
                                summary?.itemDetails?.forEach { detail ->
                                    detailedRows.add(
                                        DetailedRowData(
                                            date = purchase.timestamp,
                                            txnId = "PUR${purchase.id.toString().padStart(5, '0')}",
                                            type = "Purchase",
                                            party = purchase.supplierName,
                                            item = detail.cartItem.item.name,
                                            hsn = detail.cartItem.item.hsnCode ?: "-",
                                            taxRate = detail.cartItem.item.taxRate,
                                            qty = detail.cartItem.quantity,
                                            taxable = detail.netAmountBeforeTax,
                                            tax = detail.totalTaxAmount
                                        )
                                    )
                                }
                            }

                            if (detailedRows.isEmpty()) {
                                item { EmptyReportState("No transactions found for detailed breakup.") }
                            } else {
                                items(detailedRows) { row ->
                                    DetailedBreakupRow(row)
                                }
                            }
                        }
                    }

                    // Disclaimer / Note
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = stringResource(R.string.ui_element_desc),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Styled reports are generated inside context cache. Exported files can be shared securely via standard FileProvider.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // 6. Sticky Bottom Share Action Button
            androidx.compose.material3.Button(
                onClick = {
                    val monthName = monthNames[selectedMonth]
                    when (activeReportType) {
                        GSTReportType.GSTR1 -> viewModel.exportGSTR1Excel(context, startTs, endTs, "GSTR1_${monthName}_$selectedYear")
                        GSTReportType.GSTR2 -> viewModel.exportGSTR2Excel(context, startTs, endTs, "GSTR2_${monthName}_$selectedYear")
                        GSTReportType.GSTR3B -> viewModel.exportGSTR3BExcel(context, startTs, endTs, "GSTR3B_${monthName}_$selectedYear")
                        GSTReportType.DETAILED -> viewModel.exportGstdetailedExcel(context, startTs, endTs, "Detailed_GST_${monthName}_$selectedYear")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = "Export")
                Spacer(Modifier.width(8.dp))
                Text("Export ${activeReportType.title} Excel Report", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// Sub-components

@Composable
private fun SummaryBlock(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val fontSize = when {
                value.length > 12 -> 11.sp
                value.length > 9 -> 13.sp
                else -> 15.sp
            }
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                color = color,
                maxLines = 1,
                softWrap = false
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun TaxBreakupCard(cgst: Double, sgst: Double, igst: Double) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TaxColumn("CGST", cgst)
            TaxColumn("SGST", sgst)
            TaxColumn("IGST", igst)
        }
    }
}

@Composable
private fun RowScope.TaxColumn(label: String, amount: Double) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            maxLines = 1,
            softWrap = false
        )
        val textValue = "₹${"%,.2f".format(amount)}"
        val fontSize = if (textValue.length > 10) 10.sp else 12.sp
        Text(
            text = textValue,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun TransactionItemRow(
    title: String,
    subtitle: String,
    date: Long,
    amount: Double,
    tax: Double,
    isSale: Boolean
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(subtitle, fontSize = 10.sp, maxLines = 1, softWrap = false, modifier = androidx.compose.ui.Modifier.autoMarquee()) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSale) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = null,
                        modifier = Modifier.height(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = dateFmt.format(Date(date)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${"%,.2f".format(amount)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "Tax: ₹${"%,.2f".format(tax)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun SectionBreakdownCard(
    sectionTitle: String,
    taxable: Double,
    cgst: Double,
    sgst: Double,
    igst: Double,
    totalTax: Double
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = sectionTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                softWrap = false
            )
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(8.dp))

            BreakdownRow("Taxable Value", taxable)
            BreakdownRow("CGST Amount", cgst)
            BreakdownRow("SGST Amount", sgst)
            BreakdownRow("IGST Amount", igst)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Tax Component",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "₹${"%,.2f".format(totalTax)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = "₹${"%,.2f".format(value)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun DetailedBreakupRow(row: DetailedRowData) {
    val dateFmt = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateFmt.format(Date(row.date)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = row.txnId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(Modifier.width(6.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text(row.type, fontSize = 9.sp, maxLines = 1, softWrap = false, modifier = androidx.compose.ui.Modifier.autoMarquee()) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (row.type == "Sale") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                            labelColor = if (row.type == "Sale") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondary
                        ),
                        border = null,
                        modifier = Modifier.height(18.dp)
                    )
                }
                Text(
                    text = "₹${"%,.2f".format(row.taxable + row.tax)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.background)
            Spacer(Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = row.item,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Party: ${row.party}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Taxable: ₹${"%,.2f".format(row.taxable)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Tax: ₹${"%,.2f".format(row.tax)} (${row.taxRate}%)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyReportState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.AutoMirrored.Outlined.Assignment, contentDescription = stringResource(R.string.ui_element_desc), modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
        }
    }
}

private fun Long.padStart(len: Int): String {
    return this.toString().padStart(len, '0')
}

data class DetailedRowData(
    val date: Long,
    val txnId: String,
    val type: String,
    val party: String,
    val item: String,
    val hsn: String,
    val taxRate: Double,
    val qty: Double,
    val taxable: Double,
    val tax: Double
)
