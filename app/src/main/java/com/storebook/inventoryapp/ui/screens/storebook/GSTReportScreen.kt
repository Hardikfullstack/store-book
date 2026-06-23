package com.storebook.inventoryapp.ui.screens.storebook

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
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
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.storebook.inventoryapp.data.repository.Sale
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GSTReportScreen(navController: NavController, viewModel: StoreBookViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Month/Year picker state
    val now = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }

    val monthNames = remember {
        listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    }

    // Computed date range for selected month
    val (startTs, endTs) = remember(selectedMonth, selectedYear) {
        val cal = Calendar.getInstance()
        cal.set(selectedYear, selectedMonth, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        Pair(start, cal.timeInMillis)
    }

    var sales by remember { mutableStateOf<List<Sale>>(emptyList()) }
    var totalTaxable by remember { mutableDoubleStateOf(0.0) }
    var totalTax by remember { mutableDoubleStateOf(0.0) }
    var totalRevenue by remember { mutableDoubleStateOf(0.0) }
    var isLoading by remember { mutableStateOf(false) }
    var csvContent by remember { mutableStateOf("") }

    LaunchedEffect(startTs, endTs) {
        isLoading = true
        csvContent = viewModel.generateGSTR1Csv(startTs, endTs)
        // Parse back totals from csvContent lines (skip header)
        val lines = csvContent.lines().drop(1).filter { it.isNotBlank() }
        totalRevenue = lines.sumOf { line ->
            line.split(",").getOrNull(4)?.toDoubleOrNull() ?: 0.0
        }
        totalTaxable = lines.sumOf { line ->
            line.split(",").getOrNull(6)?.toDoubleOrNull() ?: 0.0
        }
        totalTax = totalRevenue - totalTaxable
        // Re-fetch sales for display
        sales = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            viewModel.repository.getSalesByDateRange(startTs, endTs)
        }
        isLoading = false
    }

    val gradientBrush = Brush.verticalGradient(listOf(InkBlue700, InkBlue500))

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("GST Reports", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("GSTR-1 Summary", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    IconButton(onClick = {
                        scope.launch {
                            // Write CSV to cache and share
                            val file = File(context.cacheDir, "GSTR1_${monthNames[selectedMonth]}_$selectedYear.csv")
                            file.writeText(csvContent)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "GSTR-1 ${monthNames[selectedMonth]} $selectedYear")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share GSTR-1"))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export CSV", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month navigator
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (selectedMonth == 0) { selectedMonth = 11; selectedYear-- }
                            else selectedMonth--
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev")
                        }
                        Text(
                            "${monthNames[selectedMonth]} $selectedYear",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = {
                            if (selectedMonth == 11) { selectedMonth = 0; selectedYear++ }
                            else selectedMonth++
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
                        }
                    }
                }
            }

            // Summary cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    GSTSummaryCard("Total Sales", "₹${"%,.2f".format(totalRevenue)}", InkBlue500, Modifier.weight(1f))
                    GSTSummaryCard("Taxable Value", "₹${"%,.2f".format(totalTaxable)}", Emerald500, Modifier.weight(1f))
                    GSTSummaryCard("Total Tax", "₹${"%,.2f".format(totalTax)}", Saffron500, Modifier.weight(1f))
                }
            }

            // Table header
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = InkBlue700)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Customer", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(2f))
                        Text("Date", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                        Text("Amount", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                        Text("Tax (18%)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (sales.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📋", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No sales in ${monthNames[selectedMonth]} $selectedYear", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                val dateFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                items(sales, key = { it.id }) { sale ->
                    val tax = sale.totalAmount - (sale.totalAmount / 1.18)
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                sale.customerName ?: "Consumer",
                                modifier = Modifier.weight(2f), fontSize = 13.sp, maxLines = 1
                            )
                            Text(
                                dateFmt.format(Date(sale.timestamp)),
                                modifier = Modifier.weight(1.5f), fontSize = 12.sp,
                                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "₹${"%,.2f".format(sale.totalAmount)}",
                                modifier = Modifier.weight(1.5f), fontSize = 12.sp,
                                textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "₹${"%,.2f".format(tax)}",
                                modifier = Modifier.weight(1.5f), fontSize = 12.sp,
                                textAlign = TextAlign.End, color = Saffron500
                            )
                        }
                    }
                }
            }

            // Note
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        "ℹ️ Tax calculated @18% flat rate. Use Share (↗) to export GSTR-1 CSV for filing. Per-item HSN rates are used where available.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GSTSummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
