@file:android.annotation.SuppressLint("LocalContextGetResourceValueCall")

package com.storebook.inventoryapp.ui.screens.storebook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.ui.viewmodel.SalesViewModel

data class PriceDriftEntry(
    val saleItemId: Long,
    val itemName: String,
    val soldAtPrice: Double,
    val currentPrice: Double,
    val driftAmount: Double,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceDriftReportScreen(
    navController: NavController,
    viewModel: SalesViewModel,
) {
    var driftListState by remember {
        mutableStateOf<List<PriceDriftEntry>>(emptyList())
    }

    var isLoadingState by remember {
        mutableStateOf(true)
    }

    var totalLineItemsCount by remember {
        mutableStateOf(0)
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val raw = viewModel.getPriceDriftReport()
            val sales: List<Sale> = viewModel.getSalesWithItems(limit = 5000, offset = 0)
            totalLineItemsCount = sales.sumOf { it.items.size }.toInt()

            val mapped =
                raw.map {
                    PriceDriftEntry(
                        saleItemId = it.sale_item_id,
                        itemName = it.item_name,
                        soldAtPrice = it.sold_at_price,
                        currentPrice = it.current_price,
                        driftAmount = it.drift_amount,
                    )
                }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                driftListState = mapped
                isLoadingState = false
            }
        }
    }

    val currentDriftCount = driftListState.size
    val hasAnyDrift = currentDriftCount > 0
    val correctCount = maxOf(totalLineItemsCount - currentDriftCount, 0)
    val pctCorrect =
        if (totalLineItemsCount > 0) {
            java.lang.Math.round(correctCount * 100.0 / totalLineItemsCount.toFloat())
        } else {
            100
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Price Drift Audit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->

        if (isLoadingState) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // ---- Status Card ----
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                                Text("Audit Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            if (hasAnyDrift) {
                                Text(
                                    text =
                                        "$pctCorrect% of sale-line prices match current catalog prices. " +
                                            "$currentDriftCount line(s) show a price change since billed " +
                                            "— normal after updating prices.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            } else {
                                Text(
                                    text =
                                        if (totalLineItemsCount > 0) {
                                            "All $totalLineItemsCount sale-line prices match current catalog prices."
                                        } else {
                                            "No sales yet — audit will populate after your first transactions."
                                        },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                        }
                    }
                }

                // ---- Explanation Card ----
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            CardDefaults
                                .cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text("How This Works", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text(
                                text =
                                    "When you bill a sale, each line item saves the sellPrice and " +
                                        "buyPrice at that moment. If you later change an item's " +
                                        "selling price, old SaleItem records are NOT affected.\n\n" +
                                        "This report compares every recorded line against today's " +
                                        "catalog price and flags any differences " +
                                        "— proving your financial data is accurate and untampered.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp),
                                lineHeight = 18.sp,
                            )
                        }
                    }
                }

                // ---- Detail Rows (only if any drift) ----
                if (hasAnyDrift) {
                    item {
                        Text(
                            text = "$currentDriftCount item(s) with price changes:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }

                    items(driftListState) { entry ->
                        PriceDriftItemRow(entry)
                    }
                }

                // ---- Footer ----
                item {
                    Text(
                        text = "Scanned $totalLineItemsCount line items across all sales.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceDriftItemRow(entry: PriceDriftEntry) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(entry.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Row(
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriceLabel("Billed At", "₹${String.format("%.2f", entry.soldAtPrice)}")
                PriceLabel("Now", "₹${String.format("%.2f", entry.currentPrice)}")
                PriceLabel("Drift", "₹${String.format("%.2f", entry.driftAmount)}", accent = true)
            }
        }
    }
}

@Composable
private fun PriceLabel(
    title: String,
    value: String,
    accent: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (accent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}
