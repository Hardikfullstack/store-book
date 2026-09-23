@file:android.annotation.SuppressLint("LocalContextGetResourceValueCall")

package com.storebook.inventoryapp.ui.screens.storebook
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.domain.models.CustomerBalance
import com.storebook.inventoryapp.shared.domain.models.UdhaarEntry
import com.storebook.inventoryapp.ui.components.AlphabetScrubber
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.theme.primaryGradient
import com.storebook.inventoryapp.ui.viewmodel.UdhaarViewModel
import com.storebook.inventoryapp.utils.autoMarquee
import com.storebook.inventoryapp.utils.toRupee
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
private fun LedgerStat(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    alignEnd: Boolean = false,
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1)
    }
}

@Composable
private fun LedgerEntryCard(
    entry: com.storebook.inventoryapp.shared.domain.models.UdhaarEntry, // adjust to your actual entry type
    dateFmt: java.text.DateFormat,
) {
    val dateStr = dateFmt.format(Date(entry.timestamp))
    val isCredit = entry.type == "CREDIT"
    val accentColor = if (isCredit) Coral500 else Emerald500

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isCredit) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                            .copy(alpha = 0.25f)
                    },
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Directional icon instead of a plain color bar — quicker to parse at a glance
                Box(
                    modifier =
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isCredit) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCredit) "Credit Given (उधार दिया)" else "Payment Received (जमा किया)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = accentColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    val notes = entry.notes
                    if (!notes.isNullOrBlank()) {
                        Text(
                            notes,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                    }
                    Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
            Text(
                text = entry.amount.toRupee(),
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = accentColor,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UdhaarScreen(viewModel: UdhaarViewModel) {
    val balances by viewModel.udhaarBalances.collectAsStateWithLifecycle()

    // E03-S2: Detailed breakdown with outstanding + paid separation
    val detailedBals by viewModel.detailedBalances.collectAsStateWithLifecycle()
    val detailLookup =
        remember(detailedBals) {
            detailedBals.associateBy { it.customerName }
        }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var searchQ by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerBalance?>(null) }
    var ledgerEntries by remember { mutableStateOf<List<UdhaarEntry>>(emptyList()) }

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("CREDIT") }
    var inputAmount by remember { mutableStateOf("") }
    var inputNotes by remember { mutableStateOf("") }
    var inputCustomerName by remember { mutableStateOf("") }

    val filteredBalances by remember {
        derivedStateOf {
            // E03-S2: Sort by highest current balance descending
            val sorted = balances.sortedByDescending { it.netBalance }
            if (searchQ.isBlank()) {
                sorted
            } else {
                sorted.filter { it.customerName.contains(searchQ, ignoreCase = true) }
            }
        }
    }

    val totalOutstanding by remember {
        derivedStateOf { balances.filter { it.netBalance > 0 }.sumOf { it.netBalance } }
    }

    var showCustomerLedgerSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val lastDateFmt = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    val fetchLedger: (String) -> Unit = { name ->
        coroutineScope.launch {
            ledgerEntries = viewModel.repository.getCustomerLedger(name)
        }
    }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val listState =
        androidx.compose.foundation.lazy
            .rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(searchQ) {
        listState.scrollToItem(0)
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.primaryGradient)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                // Title row — no ambiguous "+ Add" button anymore
                Text(
                    text = stringResource(id = R.string.udh_title),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "Customer Credit Ledger",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Total outstanding card — now also houses the two primary actions,
                // so this one surface does identity + summary + action instead of three separate blocks
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter =
                                            androidx.compose.ui.res
                                                .painterResource(id = R.drawable.ic_rupee),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(id = R.string.udh_total_outstanding),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.1.sp,
                                    )
                                    Text(
                                        text = totalOutstanding.toRupee(),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontFamily = Poppins,
                                    )
                                }
                            }
                            Box(
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f))
                                        .padding(horizontal = 9.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text = "${balances.size} ${if (balances.size == 1) "Customer" else "Customers"}",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Two explicit actions — replaces the ambiguous generic "+ Add" button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            androidx.compose.material3.Button(
                                onClick = {
                                    selectedCustomer = null
                                    inputCustomerName = ""
                                    inputAmount = ""
                                    inputNotes = ""
                                    dialogType = "CREDIT"
                                    showDialog = true
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(11.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                                    modifier =
                                        Modifier
                                            .size(14.dp),
                                    tint = MaterialTheme.colorScheme.onError,
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    stringResource(id = R.string.udh_btn_give_credit),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onError,
                                    maxLines = 1,
                                )
                            }
                            androidx.compose.material3.Button(
                                onClick = {
                                    selectedCustomer = null
                                    inputCustomerName = ""
                                    inputAmount = ""
                                    inputNotes = ""
                                    dialogType = "PAYMENT"
                                    showDialog = true
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(11.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                .copy(alpha = 0.12f),
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null,
                                    modifier =
                                        Modifier
                                            .size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    stringResource(id = R.string.udh_btn_receive_payment),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQ,
                    onValueChange = { searchQ = it },
                    placeholder = {
                        Text(
                            stringResource(id = R.string.udh_search_hint), fontSize = 14.sp,
                            modifier =
                                Modifier
                                    .autoMarquee(),
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchQ.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut(),
                        ) {
                            IconButton(onClick = { searchQ = "" }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.Rounded.Cancel,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    colors =
                        androidx.compose.material3.OutlinedTextFieldDefaults.colors(
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
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            if (balances.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Book,
                                contentDescription = stringResource(R.string.ui_element_desc),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(50.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No udhaar accounts yet?\nYour first customer is just a tap away!",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Add Party ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.ui_element_desc),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp).rotate(45f),
                            )
                        }
                    }
                }
            } else if (filteredBalances.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier =
                                Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.ui_element_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.udh_no_results, searchQ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier =
                        Modifier.fillMaxSize().padding(
                            start = 16.dp,
                            end =
                                if (filteredBalances.isNotEmpty() &&
                                    searchQ.isBlank()
                                ) {
                                    32.dp
                                } else {
                                    16.dp
                                },
                        ),
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
                                                .clickable(onClickLabel = "Action") {
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
                                                        .background(MaterialTheme.primaryGradient),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    bal.customerName.take(1).uppercase(),
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                bal.customerName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                modifier = Modifier.autoMarquee(),
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
                                    if (viewModel.businessName.isNotBlank() &&
                                        viewModel.businessName != "StoreBook Kirana"
                                    ) {
                                        context.getString(
                                            R.string.udh_reminder_template_with_shop,
                                            bal.customerName,
                                            bal.netBalance,
                                            viewModel.businessName,
                                        )
                                    } else {
                                        context.getString(
                                            R.string.udh_reminder_template,
                                            bal.customerName,
                                            bal.netBalance,
                                        )
                                    }
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
                                                    offsetX > 0 -> MaterialTheme.colorScheme.primary
                                                    offsetX < 0 -> MaterialTheme.colorScheme.tertiary
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
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = stringResource(R.string.ui_element_desc),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                stringResource(id = R.string.udh_mark_paid),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                        offsetX < 0 -> {
                                            Text(
                                                stringResource(id = R.string.udh_whatsapp_remind),
                                                color = MaterialTheme.colorScheme.onTertiary,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                Icons.AutoMirrored.Filled.Send,
                                                contentDescription = stringResource(R.string.ui_element_desc),
                                                tint = MaterialTheme.colorScheme.onTertiary,
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

            // Alphabet Scrubber Overlay
            if (filteredBalances.isNotEmpty() && searchQ.isBlank()) {
                AlphabetScrubber(
                    onLetterSelect = { char ->
                        val hasHeader = balances.filter { it.netBalance > 0 }.take(4).isNotEmpty()
                        val offset = if (hasHeader) 1 else 0

                        val index =
                            filteredBalances.indexOfFirst {
                                it.customerName.uppercase().firstOrNull()?.let { firstChar ->
                                    firstChar >=
                                        char
                                } ==
                                    true
                            }
                        if (index != -1) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index + offset)
                            }
                        } else {
                            coroutineScope.launch {
                                listState.animateScrollToItem(filteredBalances.size + offset - 1)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
                )
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
                                .fillMaxHeight(0.9f)
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 24.dp),
                    ) {
                        // Identity row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.primaryGradient),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    customer.customerName.firstOrNull()?.uppercase() ?: "?",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = customer.customerName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).autoMarquee(),
                            )

                            // Print + Share — trailing, icon-only but with a shared circular tray background
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .clickable(onClickLabel = "Print PDF statement") {
                                                val pdfFile =
                                                    com.storebook.inventoryapp.utils.UdhaarPdfGenerator
                                                        .generateUdhaarStatement(
                                                            context,
                                                            customer.customerName,
                                                            customer.netBalance,
                                                            ledgerEntries,
                                                            viewModel.businessName,
                                                        )
                                                if (pdfFile != null) {
                                                    com.storebook.inventoryapp.utils.ShareUtils
                                                        .openPdf(context, pdfFile)
                                                } else {
                                                    android.widget.Toast
                                                        .makeText(context, "Failed to generate statement PDF", android.widget.Toast.LENGTH_SHORT)
                                                        .show()
                                                }
                                            },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Print,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Box(
                                    modifier =
                                        Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(WhatsAppGreen.copy(alpha = 0.12f))
                                            .clickable(onClickLabel = "Share statement on WhatsApp") {
                                                val template =
                                                    if (viewModel.businessName.isNotBlank() &&
                                                        viewModel.businessName != "StoreBook Kirana"
                                                    ) {
                                                        context.getString(
                                                            R.string.udh_reminder_template_with_shop,
                                                            customer.customerName,
                                                            customer.netBalance,
                                                            viewModel.businessName,
                                                        )
                                                    } else {
                                                        context
                                                            .getString(R.string.udh_reminder_template, customer.customerName, customer.netBalance)
                                                    }
                                                val pdfFile =
                                                    com.storebook.inventoryapp.utils.UdhaarPdfGenerator
                                                        .generateUdhaarStatement(
                                                            context,
                                                            customer.customerName,
                                                            customer.netBalance,
                                                            ledgerEntries,
                                                            viewModel.businessName,
                                                        )
                                                if (pdfFile != null) {
                                                    val uri =
                                                        androidx.core.content.FileProvider.getUriForFile(
                                                            context,
                                                            "${context.packageName}.fileprovider",
                                                            pdfFile,
                                                        )
                                                    val intent =
                                                        Intent(Intent.ACTION_SEND).apply {
                                                            type = "application/pdf"
                                                            putExtra(Intent.EXTRA_STREAM, uri)
                                                            putExtra(Intent.EXTRA_TEXT, template)
                                                            setPackage("com.whatsapp")
                                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        }
                                                    try {
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                                        val fallbackIntent =
                                                            Intent(Intent.ACTION_VIEW).apply {
                                                                data =
                                                                    Uri.parse("https://api.whatsapp.com/send?text=${URLEncoder.encode(template, "UTF-8")}")
                                                            }
                                                        context.startActivity(fallbackIntent)
                                                    }
                                                } else {
                                                    val fallbackIntent =
                                                        Intent(Intent.ACTION_VIEW).apply {
                                                            data =
                                                                Uri.parse("https://api.whatsapp.com/send?text=${URLEncoder.encode(template, "UTF-8")}")
                                                        }
                                                    context.startActivity(fallbackIntent)
                                                }
                                            },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = null,
                                        tint = WhatsAppGreen,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Balance summary — promoted to its own full-width card, no longer squeezed next to the avatar
                        val detail = detailLookup[customer.customerName]
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            if (detail != null) {
                                LedgerStat(label = "Outstanding", value = detail.totalOutstanding.toRupee())
                                LedgerStat(label = "Paid", value = detail.totalPaid.toRupee(), valueColor = MaterialTheme.colorScheme.secondary)
                                val isZero = kotlin.math.abs(detail.currentBalance) < 0.01
                                LedgerStat(
                                    label = "Balance",
                                    value = if (isZero) "₹0 Settled" else detail.currentBalance.toRupee(),
                                    valueColor =
                                        when {
                                            isZero -> MaterialTheme.colorScheme.onSurfaceVariant
                                            detail.currentBalance > 0 -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.secondary
                                        },
                                    alignEnd = true,
                                )
                            } else {
                                val isZero = kotlin.math.abs(customer.netBalance) < 0.01
                                LedgerStat(
                                    label = "Current Balance",
                                    value = if (isZero) "₹0 Settled" else customer.netBalance.toRupee(),
                                    valueColor =
                                        when {
                                            isZero -> MaterialTheme.colorScheme.onSurfaceVariant
                                            customer.netBalance > 0 -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.secondary
                                        },
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Transaction History",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (ledgerEntries.isNotEmpty()) {
                                Text(
                                    "${ledgerEntries.size} entries",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Ledger timeline
                        if (ledgerEntries.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Outlined.ReceiptLong,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        "No transactions yet",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(ledgerEntries, key = { it.id }) { entry ->
                                    LedgerEntryCard(entry = entry, dateFmt = dateFmt)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Give credit / Receive payment — icons added for faster recognition
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            androidx.compose.material3.Button(
                                onClick = {
                                    inputCustomerName = customer.customerName
                                    inputAmount = ""
                                    inputNotes = ""
                                    dialogType = "CREDIT"
                                    showDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                                    modifier =
                                        Modifier
                                            .size(16.dp),
                                    tint = MaterialTheme.colorScheme.onError,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    stringResource(id = R.string.udh_btn_give_credit),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 13.sp,
                                )
                            }
                            androidx.compose.material3.Button(
                                onClick = {
                                    inputCustomerName = customer.customerName
                                    inputAmount = ""
                                    inputNotes = ""
                                    dialogType = "PAYMENT"
                                    showDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null,
                                    modifier =
                                        Modifier
                                            .size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    stringResource(id = R.string.udh_btn_receive_payment),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 13.sp,
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

                val focusRequesterName = remember { FocusRequester() }
                val focusRequesterAmount = remember { FocusRequester() }
                val focusRequesterNotes = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current

                LaunchedEffect(Unit) {
                    if (selectedCustomer != null) {
                        focusRequesterAmount.requestFocus()
                    } else {
                        focusRequesterName.requestFocus()
                    }
                }

                androidx.compose.ui.window.Dialog(onDismissRequest = { if (!isSubmitting) showDialog = false }) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 480.dp)
                                    .imePadding()
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            val accentColor = if (dialogType == "CREDIT") Coral500 else Emerald500

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(accentColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        if (dialogType == "CREDIT") "−" else "+",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = accentColor,
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (dialogType == "CREDIT") "Give Credit" else "Receive Payment",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                    )
                                    Text(
                                        text =
                                            if (dialogType ==
                                                "CREDIT"
                                            ) {
                                                "Money going out to customer"
                                            } else {
                                                "Money coming in from customer"
                                            },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            if (selectedCustomer == null) {
                                OutlinedTextField(
                                    value = inputCustomerName,
                                    onValueChange = {
                                        inputCustomerName = it
                                        nameError = false
                                    },
                                    label = {
                                        Text(
                                            "Customer Name",
                                            modifier =
                                                androidx.compose.ui.Modifier
                                                    .autoMarquee(),
                                        )
                                    },
                                    isError = nameError,
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterName),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person, contentDescription = null,
                                            modifier =
                                                Modifier
                                                    .size(18.dp),
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusRequesterAmount.requestFocus() }),
                                    supportingText =
                                        if (nameError) {
                                            { Text("Enter a valid name", fontSize = 11.sp) }
                                        } else {
                                            null
                                        },
                                )
                            } else {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(
                                            id = R.string.udh_customer_prefix,
                                            selectedCustomer?.customerName ?: "Customer",
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        modifier = Modifier.autoMarquee(),
                                    )
                                }
                            }

                            // Amount — hero input, tinted to the transaction direction
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(accentColor.copy(alpha = 0.08f))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(id = R.string.udh_amount_label),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                OutlinedTextField(
                                    value = inputAmount,
                                    onValueChange = {
                                        inputAmount = it
                                        amountError = false
                                    },
                                    placeholder = {
                                        Text(
                                            "0",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    },
                                    prefix = {
                                        Text("₹", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                    },
                                    textStyle =
                                        LocalTextStyle.current.copy(
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = accentColor,
                                        ),
                                    isError = amountError,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusRequesterNotes.requestFocus() }),
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterAmount),
                                    singleLine = true,
                                    colors =
                                        OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedContainerColor = Color.Transparent,
                                        ),
                                    supportingText =
                                        if (amountError) {
                                            {
                                                Text(
                                                    "Enter a valid amount",
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.fillMaxWidth(),
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                )
                            }

                            OutlinedTextField(
                                value = inputNotes,
                                onValueChange = { inputNotes = it },
                                label = {
                                    Text(
                                        stringResource(id = R.string.udh_desc_note),
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                placeholder = {
                                    Text(
                                        "e.g. Grocery purchase",
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterNotes),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                            )

                            // Plain-language confirmation — same safeguard as the other party-balance dialog,
                            // since a mixed-up direction here has the same real-world consequence
                            val previewAmt = inputAmount.toDoubleOrNull()
                            val previewName =
                                selectedCustomer?.customerName?.ifBlank { null }
                                    ?: inputCustomerName.trim().ifBlank { null }
                            if (previewAmt != null && previewAmt > 0 && previewName != null) {
                                Text(
                                    text =
                                        if (dialogType == "CREDIT") {
                                            "$previewName now owes you ₹${"%.2f".format(previewAmt)} more"
                                        } else {
                                            "$previewName's balance reduces by ₹${"%.2f".format(previewAmt)}"
                                        },
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                androidx.compose.material3.Button(
                                    onClick = { showDialog = false },
                                    enabled = !isSubmitting,
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

                                androidx.compose.material3.Button(
                                    onClick = {
                                        val name = inputCustomerName.trim()
                                        val amt = inputAmount.toDoubleOrNull()

                                        var isValid = true
                                        if (selectedCustomer == null && name.isBlank()) {
                                            nameError = true
                                            isValid = false
                                        }
                                        if (amt == null || amt <= 0.0) {
                                            amountError = true
                                            isValid = false
                                        }
                                        if (!isValid) return@Button

                                        val parsedAmount = amt!!
                                        val finalName = selectedCustomer?.customerName ?: name
                                        isSubmitting = true
                                        viewModel.recordUdhaarEntry(finalName, parsedAmount, dialogType, inputNotes)
                                        showDialog = false
                                        isSubmitting = false

                                        if (showCustomerLedgerSheet && selectedCustomer != null) {
                                            val currentCustomerName = selectedCustomer?.customerName ?: "Unknown"
                                            fetchLedger(currentCustomerName)
                                            coroutineScope.launch {
                                                val updatedList = viewModel.repository.getUdhaarBalances()
                                                selectedCustomer =
                                                    updatedList.find { it.customerName == currentCustomerName }
                                            }
                                        }

                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Entry saved successfully")
                                        }
                                    },
                                    enabled = !isSubmitting,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                if (dialogType ==
                                                    "CREDIT"
                                                ) {
                                                    MaterialTheme.colorScheme.error
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                        ),
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color =
                                                if (dialogType ==
                                                    "CREDIT"
                                                ) {
                                                    MaterialTheme.colorScheme.onError
                                                } else {
                                                    MaterialTheme.colorScheme.onPrimary
                                                },
                                        )
                                    } else {
                                        Text(
                                            stringResource(id = R.string.btn_save),
                                            color =
                                                if (dialogType ==
                                                    "CREDIT"
                                                ) {
                                                    MaterialTheme.colorScheme.onError
                                                } else {
                                                    MaterialTheme.colorScheme.onPrimary
                                                },
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
}

@Composable
fun UdhaarCustomerCard(
    bal: CustomerBalance,
    lastDateFmt: SimpleDateFormat,
    owesMoney: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Action") { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        val isZero = kotlin.math.abs(bal.netBalance) < 0.01

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
                            when {
                                isZero -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                                owesMoney -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = bal.customerName.firstOrNull()?.uppercase() ?: "?",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color =
                        when {
                            isZero -> MaterialTheme.colorScheme.onSurfaceVariant
                            owesMoney -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        },
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bal.customerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    modifier = Modifier.autoMarquee(),
                )
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
                    text = if (isZero) "₹0" else "${bal.netBalance.toRupee()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color =
                        when {
                            isZero -> MaterialTheme.colorScheme.onSurfaceVariant
                            owesMoney -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        },
                )
                Text(
                    text =
                        when {
                            isZero -> "Settled"
                            owesMoney -> "Due"
                            else -> "Advance"
                        },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        when {
                            isZero -> MaterialTheme.colorScheme.onSurfaceVariant
                            owesMoney -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        },
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
