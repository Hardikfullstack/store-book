package com.storebook.inventoryapp.ui.screens.storebook

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.data.repository.CustomerBalance
import com.storebook.inventoryapp.data.repository.UdhaarEntry
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel
import com.storebook.inventoryapp.utils.toRupee
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UdhaarScreen(viewModel: StoreBookViewModel) {
    val balances by viewModel.udhaarBalances.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQ by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerBalance?>(null) }
    var ledgerEntries by remember { mutableStateOf<List<UdhaarEntry>>(emptyList()) }

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("CREDIT") }
    var inputAmount by remember { mutableStateOf("") }
    var inputNotes by remember { mutableStateOf("") }
    var inputCustomerName by remember { mutableStateOf("") }

    val filteredBalances by remember(balances, searchQ) {
        derivedStateOf {
            if (searchQ.isBlank()) {
                balances
            } else {
                balances.filter { it.customerName.contains(searchQ, ignoreCase = true) }
            }
        }
    }

    val totalOutstanding by remember(balances) {
        derivedStateOf { balances.filter { it.netBalance > 0 }.sumOf { it.netBalance } }
    }

    var showCustomerLedgerSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val lastDateFmt = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    val repository =
        remember {
            com.storebook.inventoryapp.data.repository
                .StoreBookRepository(context)
        }
    val fetchLedger: (String) -> Unit = { name ->
        coroutineScope.launch {
            ledgerEntries = repository.getCustomerLedger(name)
        }
    }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.udh_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Button(
                        onClick = {
                            selectedCustomer = null
                            inputCustomerName = ""
                            inputAmount = ""
                            inputNotes = ""
                            dialogType = "CREDIT"
                            showDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.btn_add), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Total outstanding premium card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Coral100.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("📒", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = stringResource(id = R.string.udh_total_outstanding),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.1.sp,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${totalOutstanding.toRupee()}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Coral500,
                                    fontFamily = Poppins,
                                )
                            }
                        }
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "${balances.size} customers",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQ,
                    onValueChange = { searchQ = it },
                    placeholder = { Text(stringResource(id = R.string.udh_search_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            if (balances.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.udh_empty),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else if (filteredBalances.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.udh_no_results, searchQ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                ) {
                    val topCustomers =
                        balances.filter { it.netBalance > 0 }.sortedByDescending { it.netBalance }.take(
                            4,
                        )
                    if (topCustomers.isNotEmpty() && searchQ.isEmpty()) {
                        item {
                            Text(
                                "Speed Dial",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(topCustomers) { bal ->
                                    Card(
                                        modifier =
                                            Modifier
                                                .width(120.dp)
                                                .clickable {
                                                    selectedCustomer = bal
                                                    fetchLedger(bal.customerName)
                                                    showCustomerLedgerSheet = true
                                                },
                                        shape = RoundedCornerShape(12.dp),
                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor =
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.5f,
                                                    ),
                                            ),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .size(
                                                            32.dp,
                                                        ).clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    bal.customerName.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                bal.customerName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                bal.netBalance.toRupee(),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    items(filteredBalances, key = { it.customerName }) { bal ->
                        val owesMoney = bal.netBalance > 0

                        LimitedSwipeToActionBox(
                            enableStartToEnd = owesMoney,
                            enableEndToStart = owesMoney,
                            onStartToEnd = {
                                if (owesMoney) {
                                    val amountToSettle = bal.netBalance
                                    val customerToSettle = bal.customerName
                                    viewModel.recordUdhaarEntry(
                                        customerName = customerToSettle,
                                        amount = amountToSettle,
                                        type = "PAYMENT",
                                        notes = "Full settlement",
                                    )
                                    coroutineScope.launch {
                                        val result =
                                            snackbarHostState.showSnackbar(
                                                message = "Marked as paid",
                                                actionLabel = "UNDO",
                                                duration = androidx.compose.material3.SnackbarDuration.Short,
                                            )
                                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                            viewModel.recordUdhaarEntry(
                                                customerName = customerToSettle,
                                                amount = amountToSettle,
                                                type = "CREDIT",
                                                notes = "Undo settlement",
                                            )
                                        }
                                    }
                                }
                            },
                            onEndToStart = {
                                val template =
                                    context.getString(
                                        R.string.udh_reminder_template,
                                        bal.customerName,
                                        bal.netBalance,
                                    )
                                val intent =
                                    Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            Uri.parse(
                                                "https://api.whatsapp.com/send?text=${URLEncoder.encode(
                                                    template,
                                                    "UTF-8",
                                                )}",
                                            )
                                    }
                                context.startActivity(intent)
                            },
                            backgroundContent = { offsetX ->
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                when {
                                                    offsetX > 0 -> Emerald500
                                                    offsetX < 0 -> WhatsAppGreen
                                                    else -> Color.Transparent
                                                },
                                            ).padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement =
                                        when {
                                            offsetX > 0 -> Arrangement.Start
                                            else -> Arrangement.End
                                        },
                                ) {
                                    when {
                                        offsetX > 0 -> {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                stringResource(id = R.string.udh_mark_paid),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                        offsetX < 0 -> {
                                            Text(
                                                stringResource(id = R.string.udh_whatsapp_remind),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                Icons.AutoMirrored.Filled.Send,
                                                contentDescription = null,
                                                tint = Color.White,
                                            )
                                        }
                                    }
                                }
                            },
                        ) {
                            UdhaarCustomerCard(
                                bal = bal,
                                lastDateFmt = lastDateFmt,
                                owesMoney = owesMoney,
                                onClick = {
                                    selectedCustomer = bal
                                    fetchLedger(bal.customerName)
                                    showCustomerLedgerSheet = true
                                },
                            )
                        }
                    }
                }
            }

            // Customer Ledger Detail Sheet
            if (showCustomerLedgerSheet && selectedCustomer != null) {
                val customer = selectedCustomer!!
                ModalBottomSheet(
                    onDismissRequest = { showCustomerLedgerSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.88f)
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 32.dp),
                    ) {
                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Avatar circle
                                Box(
                                    modifier =
                                        Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        customer.customerName.firstOrNull()?.uppercase() ?: "?",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = customer.customerName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "${customer.netBalance.toRupee()} outstanding",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (customer.netBalance > 0) Coral500 else Emerald500,
                                    )
                                }
                            }

                            // WhatsApp share icon
                            Box(
                                modifier =
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppGreen.copy(alpha = 0.12f))
                                        .clickable {
                                            val template =
                                                context.getString(
                                                    R.string.udh_reminder_template,
                                                    customer.customerName,
                                                    customer.netBalance,
                                                )
                                            val intent =
                                                Intent(Intent.ACTION_VIEW).apply {
                                                    data =
                                                        Uri.parse(
                                                            "https://api.whatsapp.com/send?text=${URLEncoder.encode(
                                                                template,
                                                                "UTF-8",
                                                            )}",
                                                        )
                                                }
                                            context.startActivity(intent)
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                    tint = WhatsAppGreen,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Ledger timeline
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(ledgerEntries) { entry ->
                                val dateStr = dateFmt.format(Date(entry.timestamp))
                                val isCredit = entry.type == "CREDIT"
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                if (isCredit) {
                                                    Coral100.copy(alpha = 0.5f)
                                                } else {
                                                    Emerald500.copy(alpha = 0.08f)
                                                },
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .width(3.dp)
                                                        .height(40.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(if (isCredit) Coral500 else Emerald500),
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = if (isCredit) "Credit Given (उधार दिया)" else "Payment Received (जमा किया)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isCredit) Coral500 else Emerald500,
                                                )
                                                if (!entry.notes.isNullOrBlank()) {
                                                    Text(
                                                        entry.notes,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    )
                                                }
                                                Text(
                                                    dateStr,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${entry.amount.toRupee()}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = if (isCredit) Coral500 else Emerald500,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Give credit / Receive payment buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = {
                                    inputCustomerName = customer.customerName
                                    inputAmount = ""
                                    inputNotes = ""
                                    dialogType = "CREDIT"
                                    showDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Coral500),
                            ) {
                                Text(
                                    stringResource(id = R.string.udh_btn_give_credit),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                            Button(
                                onClick = {
                                    inputCustomerName = customer.customerName
                                    inputAmount = ""
                                    inputNotes = ""
                                    dialogType = "PAYMENT"
                                    showDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            ) {
                                Text(
                                    stringResource(id = R.string.udh_btn_receive_payment),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }

            // Transaction entry dialog
            if (showDialog) {
                var nameError by remember { mutableStateOf(false) }
                var amountError by remember { mutableStateOf(false) }

                androidx.compose.ui.window.Dialog(onDismissRequest = { showDialog = false }) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (dialogType == "CREDIT") {
                                                    Coral500.copy(alpha = 0.12f)
                                                } else {
                                                    Emerald500.copy(alpha = 0.12f)
                                                },
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        if (dialogType == "CREDIT") "−" else "+",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = if (dialogType == "CREDIT") Coral500 else Emerald500,
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text =
                                        if (dialogType ==
                                            "CREDIT"
                                        ) {
                                            "Give Credit (उधार दें)"
                                        } else {
                                            "Receive Payment (जमा करें)"
                                        },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dialogType == "CREDIT") Coral500 else Emerald500,
                                )
                            }

                            if (selectedCustomer == null) {
                                OutlinedTextField(
                                    value = inputCustomerName,
                                    onValueChange = {
                                        inputCustomerName = it
                                        nameError = false
                                    },
                                    label = { Text(if (nameError) "Valid Name Required" else "Customer Name") },
                                    isError = nameError,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                )
                            } else {
                                Text(
                                    stringResource(id = R.string.udh_customer_prefix, selectedCustomer!!.customerName),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }

                            OutlinedTextField(
                                value = inputAmount,
                                onValueChange = {
                                    inputAmount = it
                                    amountError = false
                                },
                                label = {
                                    Text(
                                        if (amountError) {
                                            "Valid Amount Required"
                                        } else {
                                            stringResource(
                                                id = R.string.udh_amount_label,
                                            )
                                        },
                                    )
                                },
                                isError = amountError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                            )

                            OutlinedTextField(
                                value = inputNotes,
                                onValueChange = { inputNotes = it },
                                label = { Text(stringResource(id = R.string.udh_desc_note)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Button(
                                    onClick = { showDialog = false },
                                    modifier = Modifier.weight(1f),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text(stringResource(id = R.string.btn_cancel), fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val name = inputCustomerName.trim()
                                        val amt = inputAmount.toDoubleOrNull()

                                        var isValid = true
                                        if (name.isBlank()) {
                                            nameError = true
                                            isValid = false
                                        }
                                        if (amt == null || amt <= 0.0) {
                                            amountError = true
                                            isValid = false
                                        }
                                        if (!isValid) return@Button

                                        viewModel.recordUdhaarEntry(name, amt!!, dialogType, inputNotes)
                                        showDialog = false

                                        if (showCustomerLedgerSheet && selectedCustomer != null) {
                                            fetchLedger(selectedCustomer!!.customerName)
                                            coroutineScope.launch {
                                                val updatedList = repository.getUdhaarBalances()
                                                selectedCustomer =
                                                    updatedList.find {
                                                        it.customerName ==
                                                            selectedCustomer!!.customerName
                                                    }
                                            }
                                        }

                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Entry saved successfully")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = if (dialogType == "CREDIT") Coral500 else Emerald500,
                                        ),
                                ) {
                                    Text(
                                        stringResource(id = R.string.btn_save),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
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

@Composable
fun UdhaarCustomerCard(
    bal: CustomerBalance,
    lastDateFmt: SimpleDateFormat,
    owesMoney: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (owesMoney) {
                                Coral500.copy(alpha = 0.12f)
                            } else {
                                Emerald500.copy(alpha = 0.12f)
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = bal.customerName.firstOrNull()?.uppercase() ?: "?",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = if (owesMoney) Coral500 else Emerald500,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bal.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text =
                        stringResource(
                            id = R.string.udh_last_trans,
                            lastDateFmt.format(Date(bal.lastTransactionTime)),
                        ),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${bal.netBalance.toRupee()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = if (owesMoney) Coral500 else Emerald500,
                )
                Text(
                    text = if (owesMoney) "Due" else "Advance",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (owesMoney) Coral500 else Emerald500,
                )
            }
        }
    }
}

@Composable
fun LimitedSwipeToActionBox(
    enableStartToEnd: Boolean,
    enableEndToStart: Boolean,
    onStartToEnd: () -> Unit,
    onEndToStart: () -> Unit,
    backgroundContent: @Composable (offsetX: Float) -> Unit,
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val maxDragPx = with(density) { (configuration.screenWidthDp * 0.45f).dp.toPx() } // Limit drag to 45% of screen
    val triggerPx = with(density) { (configuration.screenWidthDp * 0.25f).dp.toPx() } // Trigger at 25% of screen

    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier =
            Modifier.draggable(
                orientation = Orientation.Horizontal,
                state =
                    rememberDraggableState { delta ->
                        coroutineScope.launch {
                            var newOffset = offsetX.value + delta
                            if (!enableStartToEnd && newOffset > 0) newOffset = 0f
                            if (!enableEndToStart && newOffset < 0) newOffset = 0f

                            if (newOffset > maxDragPx) newOffset = maxDragPx
                            if (newOffset < -maxDragPx) newOffset = -maxDragPx

                            offsetX.snapTo(newOffset)
                        }
                    },
                onDragStopped = {
                    coroutineScope.launch {
                        // Check if we passed the threshold to trigger action
                        if (offsetX.value >= triggerPx && enableStartToEnd) {
                            // animate back first, then trigger
                            offsetX.animateTo(
                                0f,
                                animationSpec =
                                    androidx.compose.animation.core
                                        .tween(200),
                            )
                            onStartToEnd()
                        } else if (offsetX.value <= -triggerPx && enableEndToStart) {
                            offsetX.animateTo(
                                0f,
                                animationSpec =
                                    androidx.compose.animation.core
                                        .tween(200),
                            )
                            onEndToStart()
                        } else {
                            // didn't pass threshold, just snap back
                            offsetX.animateTo(
                                0f,
                                animationSpec =
                                    androidx.compose.animation.core
                                        .tween(200),
                            )
                        }
                    }
                },
            ),
    ) {
        // Background
        Box(modifier = Modifier.matchParentSize()) {
            backgroundContent(offsetX.value)
        }

        // Foreground
        Box(
            modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) },
        ) {
            content()
        }
    }
}
