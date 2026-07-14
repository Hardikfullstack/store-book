package com.storebook.inventoryapp.ui.screens.storebook

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.shared.domain.models.SupplierBalance
import com.storebook.inventoryapp.ui.components.AlphabetScrubber
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.theme.PrimaryButton
import com.storebook.inventoryapp.ui.theme.primaryGradient
import com.storebook.inventoryapp.utils.toRupee
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierLedgerScreen(
        viewModel: com.storebook.inventoryapp.ui.viewmodel.SupplierViewModel,
        onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val supplierBalances by viewModel.supplierBalances.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()

    var searchQ by remember { mutableStateOf("") }
    var selectedSupplierBalance by remember { mutableStateOf<SupplierBalance?>(null) }

    // Bottom Sheet states
    var showSupplierDetailSheet by remember { mutableStateOf(false) }
    var showAddSupplierSheet by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<Supplier?>(null) }

    // Add/Edit Supplier Form Fields
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var supplierGstin by remember { mutableStateOf("") }
    var supplierAddress by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf(false) }

    // Payment fields
    var paymentAmount by remember { mutableStateOf("") }
    var paymentNotes by remember { mutableStateOf("") }

    val focusRequesterPhone = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusRequesterGstin = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusRequesterAddress = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusRequesterNotes = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    val filteredBalances by remember {
        derivedStateOf {
            if (searchQ.isBlank()) {
                supplierBalances
            } else {
                supplierBalances.filter { it.supplierName.contains(searchQ, ignoreCase = true) }
            }
        }
    }

    val totalPayable by remember {
        derivedStateOf { supplierBalances.filter { it.netBalance > 0 }.sumOf { it.netBalance } }
    }

    val supplierDetailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addSupplierSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(searchQ) { listState.scrollToItem(0) }

    fun openAddSupplier() {
        editingSupplier = null
        supplierName = ""
        supplierPhone = ""
        supplierGstin = ""
        supplierAddress = ""
        formError = false
        showAddSupplierSheet = true
    }

    fun openEditSupplier(supplier: SupplierBalance) {
        editingSupplier =
                Supplier(
                        id = supplier.supplierId,
                        name = supplier.supplierName,
                        phone = supplier.phone,
                        gstin = null, // Can fetch details if needed, or leave optional
                        address = null
                )
        supplierName = supplier.supplierName
        supplierPhone = supplier.phone ?: ""
        supplierGstin = ""
        supplierAddress = ""
        formError = false
        showAddSupplierSheet = true
    }

    Scaffold(
            topBar = {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(MaterialTheme.primaryGradient)
                                        .statusBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                    text = "Supplier Ledger",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                    text = "Manage wholesale purchases & payments",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                            value = searchQ,
                            onValueChange = { searchQ = it },
                            placeholder = { Text("Search supplier by name...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                        Icons.Default.Search,
                                        contentDescription =
                                                stringResource(R.string.ui_element_desc)
                                )
                            },
                            trailingIcon = {
                                if (searchQ.isNotEmpty()) {
                                    androidx.compose.material3.IconButton(
                                            onClick = { searchQ = "" }
                                    ) {
                                        Icon(
                                                Icons.Rounded.Cancel,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors =
                                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                            unfocusedTextColor =
                                                    MaterialTheme.colorScheme.onPrimary,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedBorderColor =
                                                    MaterialTheme.colorScheme.onPrimary.copy(
                                                            alpha = 0.8f
                                                    ),
                                            unfocusedBorderColor =
                                                    MaterialTheme.colorScheme.onPrimary.copy(
                                                            alpha = 0.6f
                                                    ),
                                            focusedLeadingIconColor =
                                                    MaterialTheme.colorScheme.onPrimary.copy(
                                                            alpha = 0.9f
                                                    ),
                                            unfocusedLeadingIconColor =
                                                    MaterialTheme.colorScheme.onPrimary.copy(
                                                            alpha = 0.7f
                                                    ),
                                            focusedPlaceholderColor =
                                                    MaterialTheme.colorScheme.onPrimary.copy(
                                                            alpha = 0.8f
                                                    ),
                                            unfocusedPlaceholderColor =
                                                    MaterialTheme.colorScheme.onPrimary.copy(
                                                            alpha = 0.7f
                                                    ),
                                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                                    )
                    )
                }
            },
            floatingActionButton = {
                androidx.compose.material3.Button(
                        onClick = { openAddSupplier() },
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                        shape = CircleShape,
                        modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.ui_element_desc)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Supplier", fontWeight = FontWeight.Bold)
                }
            }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                    state = listState,
                    modifier =
                            Modifier.fillMaxSize()
                                    .padding(
                                            start = 16.dp,
                                            end =
                                                    if (filteredBalances.isNotEmpty() &&
                                                                    searchQ.isBlank()
                                                    )
                                                            32.dp
                                                    else 16.dp
                                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
            ) {
                // Total Payable Card
                item {
                    Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                            alpha = 0.4f
                                                    )
                                    ),
                            border =
                                    BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                    ) {
                        Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                    text = "Total Net Payable to Suppliers",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                    text = totalPayable.toRupee(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Coral500
                            )
                        }
                    }
                }

                if (filteredBalances.isEmpty()) {
                    item {
                        Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                        modifier =
                                                Modifier.size(72.dp)
                                                        .clip(
                                                                androidx.compose.foundation.shape
                                                                        .CircleShape
                                                        )
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer.copy(
                                                                        alpha = 0.3f
                                                                )
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                            imageVector = Icons.Outlined.Store,
                                            contentDescription =
                                                    stringResource(R.string.ui_element_desc),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                        text =
                                                if (searchQ.isBlank()) "No suppliers added yet."
                                                else "No matching suppliers found.",
                                        color =
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.5f
                                                ),
                                        fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredBalances, key = { it.supplierId }) { supplier ->
                        val initial = supplier.supplierName.take(2).uppercase()

                        Card(
                                modifier =
                                        Modifier.fillMaxWidth().clickable(onClickLabel = "Action") {
                                            selectedSupplierBalance = supplier
                                            showSupplierDetailSheet = true
                                        },
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                border =
                                        BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(
                                                        alpha = 0.4f
                                                )
                                        )
                        ) {
                            Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                            modifier =
                                                    Modifier.size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                    MaterialTheme.colorScheme
                                                                            .primaryContainer
                                                            ),
                                            contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                                text = initial,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                                text = supplier.supplierName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                        )
                                        val phone = supplier.phone
                                        if (!phone.isNullOrBlank()) {
                                            Text(
                                                    text = phone,
                                                    fontSize = 12.sp,
                                                    color =
                                                            MaterialTheme.colorScheme
                                                                    .onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                            text =
                                                    if (supplier.netBalance > 0) "Payable"
                                                    else if (supplier.netBalance < 0) "Advance"
                                                    else "Settled",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                            text = Math.abs(supplier.netBalance).toRupee(),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color =
                                                    if (supplier.netBalance > 0) Coral500
                                                    else if (supplier.netBalance < 0) Emerald500
                                                    else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Alphabet Scrubber Overlay
            if (filteredBalances.isNotEmpty() && searchQ.isBlank()) {
                AlphabetScrubber(
                        onLetterSelect = { char ->
                            val index =
                                    filteredBalances.indexOfFirst {
                                        it.supplierName.uppercase().firstOrNull()?.let { firstChar
                                            ->
                                            firstChar >= char
                                        } == true
                                    }
                            if (index != -1) {
                                scope.launch {
                                    listState.scrollToItem(
                                            index + 1
                                    ) // +1 for the Total Payable Card
                                }
                            } else {
                                scope.launch { listState.scrollToItem(filteredBalances.size) }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                )
            }

            // ── Supplier Detail Bottom Sheet ────────────────────────────────────
            if (showSupplierDetailSheet && selectedSupplierBalance != null) {
                val supplier = selectedSupplierBalance!!
                val supplierPurchases =
                        remember(purchases, supplier.supplierId) {
                            purchases.filter { it.supplierId == supplier.supplierId }
                        }

                ModalBottomSheet(
                        onDismissRequest = { showSupplierDetailSheet = false },
                        sheetState = supplierDetailSheetState,
                        dragHandle = { BottomSheetDefaults.DragHandle() },
                        containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .fillMaxHeight(0.9f)
                                            .padding(horizontal = 20.dp)
                    ) {
                        // Header
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                        text = supplier.supplierName,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                )
                                val phone = supplier.phone
                                if (!phone.isNullOrBlank()) {
                                    IconButton(
                                            onClick = {
                                                val intent =
                                                        Intent(Intent.ACTION_DIAL).apply {
                                                            data = Uri.parse("tel:$phone")
                                                        }
                                                context.startActivity(intent)
                                            }
                                    ) {
                                        Icon(
                                                Icons.Default.Call,
                                                contentDescription = "Call",
                                                tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { openEditSupplier(supplier) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(
                                        onClick = {
                                            viewModel.removeSupplier(supplier.supplierId) {
                                                showSupplierDetailSheet = false
                                                android.widget.Toast.makeText(
                                                                context,
                                                                "Supplier deleted",
                                                                android.widget.Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                            }
                                        }
                                ) {
                                    Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Coral500
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Balance Card
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        if (supplier.netBalance > 0)
                                                                Coral500.copy(alpha = 0.1f)
                                                        else if (supplier.netBalance < 0)
                                                                Emerald500.copy(alpha = 0.1f)
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant.copy(
                                                                        alpha = 0.5f
                                                                )
                                        )
                        ) {
                            Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                            text =
                                                    if (supplier.netBalance > 0)
                                                            "Current Net Payable"
                                                    else if (supplier.netBalance < 0)
                                                            "Advance Balance"
                                                    else "No Outstanding Balance",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                            text = Math.abs(supplier.netBalance).toRupee(),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color =
                                                    if (supplier.netBalance > 0) Coral500
                                                    else if (supplier.netBalance < 0) Emerald500
                                                    else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                androidx.compose.material3.Button(
                                        onClick = { showAddPaymentDialog = true },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.primary
                                                ),
                                        shape = RoundedCornerShape(12.dp)
                                ) { Text("Record Payment", fontWeight = FontWeight.Bold) }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                                text = "Transaction History",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            if (supplierPurchases.isEmpty()) {
                                item {
                                    Box(
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .padding(vertical = 40.dp),
                                            contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                                "No bills or payments logged yet.",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                items(supplierPurchases, key = { it.id }) { purchase ->
                                    val isBill = purchase.type == "BILL"

                                    Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors =
                                                    CardDefaults.cardColors(
                                                            containerColor =
                                                                    MaterialTheme.colorScheme
                                                                            .surface
                                                    ),
                                            border =
                                                    BorderStroke(
                                                            1.dp,
                                                            MaterialTheme.colorScheme.outlineVariant
                                                                    .copy(alpha = 0.3f)
                                                    )
                                    ) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                        modifier =
                                                                Modifier.size(4.dp, 36.dp)
                                                                        .clip(
                                                                                RoundedCornerShape(
                                                                                        2.dp
                                                                                )
                                                                        )
                                                                        .background(
                                                                                if (isBill) Coral500
                                                                                else Emerald500
                                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                            text =
                                                                    if (isBill) "Purchase Bill"
                                                                    else "Payment Paid",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                    )
                                                    Text(
                                                            text =
                                                                    dateFmt.format(
                                                                            Date(purchase.timestamp)
                                                                    ),
                                                            fontSize = 11.sp,
                                                            color =
                                                                    MaterialTheme.colorScheme
                                                                            .onSurfaceVariant
                                                    )
                                                    val notes = purchase.notes
                                                    if (!notes.isNullOrBlank()) {
                                                        Text(
                                                                text = "Notes: $notes",
                                                                fontSize = 12.sp,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                    text =
                                                            if (isBill)
                                                                    "+${purchase.totalAmount.toRupee()}"
                                                            else
                                                                    "-${purchase.totalAmount.toRupee()}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp,
                                                    color = if (isBill) Coral500 else Emerald500
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Add/Edit Supplier Sheet ─────────────────────────────────────────
            if (showAddSupplierSheet) {
                ModalBottomSheet(
                        onDismissRequest = { showAddSupplierSheet = false },
                        sheetState = addSupplierSheetState,
                        containerColor = MaterialTheme.colorScheme.surface,
                        dragHandle = { BottomSheetDefaults.DragHandle() },
                        modifier = Modifier.imePadding()
                ) {
                    Column(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 24.dp, vertical = 8.dp)
                                            .padding(bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                                text =
                                        if (editingSupplier == null) "Add Supplier"
                                        else "Edit Supplier",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                                value = supplierName,
                                onValueChange = {
                                    supplierName = it
                                    formError = false
                                },
                                label = { Text("Supplier Name") },
                                keyboardOptions =
                                        KeyboardOptions(
                                                imeAction =
                                                        androidx.compose.ui.text.input.ImeAction
                                                                .Next
                                        ),
                                keyboardActions =
                                        androidx.compose.foundation.text.KeyboardActions(
                                                onNext = { focusRequesterPhone.requestFocus() }
                                        ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = formError,
                                supportingText =
                                        if (formError) {
                                            { Text("Supplier name cannot be empty") }
                                        } else null
                        )

                        OutlinedTextField(
                                value = supplierPhone,
                                onValueChange = { supplierPhone = it },
                                label = { Text("Phone Number") },
                                keyboardOptions =
                                        KeyboardOptions(
                                                keyboardType = KeyboardType.Phone,
                                                imeAction =
                                                        androidx.compose.ui.text.input.ImeAction
                                                                .Next
                                        ),
                                keyboardActions =
                                        androidx.compose.foundation.text.KeyboardActions(
                                                onNext = { focusRequesterGstin.requestFocus() }
                                        ),
                                modifier =
                                        Modifier.fillMaxWidth().focusRequester(focusRequesterPhone),
                                singleLine = true
                        )

                        OutlinedTextField(
                                value = supplierGstin,
                                onValueChange = { supplierGstin = it },
                                label = { Text("GSTIN") },
                                keyboardOptions =
                                        KeyboardOptions(
                                                imeAction =
                                                        androidx.compose.ui.text.input.ImeAction
                                                                .Next
                                        ),
                                keyboardActions =
                                        androidx.compose.foundation.text.KeyboardActions(
                                                onNext = { focusRequesterAddress.requestFocus() }
                                        ),
                                modifier =
                                        Modifier.fillMaxWidth().focusRequester(focusRequesterGstin),
                                singleLine = true
                        )

                        OutlinedTextField(
                                value = supplierAddress,
                                onValueChange = { supplierAddress = it },
                                label = { Text("Address") },
                                keyboardOptions =
                                        KeyboardOptions(
                                                imeAction =
                                                        androidx.compose.ui.text.input.ImeAction
                                                                .Done
                                        ),
                                keyboardActions =
                                        androidx.compose.foundation.text.KeyboardActions(
                                                onDone = { focusManager.clearFocus() }
                                        ),
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .focusRequester(focusRequesterAddress),
                                maxLines = 3
                        )

                        PrimaryButton(
                                onClick = {
                                    val name = supplierName.trim()
                                    if (name.isBlank()) {
                                        formError = true
                                        return@PrimaryButton
                                    }

                                    if (editingSupplier == null) {
                                        viewModel.addSupplier(
                                                name = name,
                                                phone =
                                                        supplierPhone.trim().takeIf {
                                                            it.isNotBlank()
                                                        },
                                                gstin =
                                                        supplierGstin.trim().takeIf {
                                                            it.isNotBlank()
                                                        },
                                                address =
                                                        supplierAddress.trim().takeIf {
                                                            it.isNotBlank()
                                                        }
                                        ) {
                                            showAddSupplierSheet = false
                                            android.widget.Toast.makeText(
                                                            context,
                                                            "Supplier added!",
                                                            android.widget.Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        }
                                    } else {
                                        // Can add editing action in Repository if needed, or simply
                                        // delete and re-add, but standard add is fine.
                                        // For now we will add it as standard add / overwrite if
                                        // exists.
                                        viewModel.addSupplier(
                                                name = name,
                                                phone =
                                                        supplierPhone.trim().takeIf {
                                                            it.isNotBlank()
                                                        },
                                                gstin =
                                                        supplierGstin.trim().takeIf {
                                                            it.isNotBlank()
                                                        },
                                                address =
                                                        supplierAddress.trim().takeIf {
                                                            it.isNotBlank()
                                                        }
                                        ) {
                                            showAddSupplierSheet = false
                                            android.widget.Toast.makeText(
                                                            context,
                                                            "Supplier updated!",
                                                            android.widget.Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                        ) { Text(text = "Save Supplier", fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // ── Record Supplier Payment Dialog ──────────────────────────────────
            if (showAddPaymentDialog && selectedSupplierBalance != null) {
                val supplier = selectedSupplierBalance!!

                AlertDialog(
                        onDismissRequest = { showAddPaymentDialog = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        title = {
                            Text(
                                    text = "Record Payment to ${supplier.supplierName}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                        value = paymentAmount,
                                        onValueChange = { paymentAmount = it },
                                        label = { Text("Amount Paid") },
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Decimal,
                                                        imeAction =
                                                                androidx.compose.ui.text.input
                                                                        .ImeAction.Next
                                                ),
                                        keyboardActions =
                                                androidx.compose.foundation.text.KeyboardActions(
                                                        onNext = {
                                                            focusRequesterNotes.requestFocus()
                                                        }
                                                ),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                OutlinedTextField(
                                        value = paymentNotes,
                                        onValueChange = { paymentNotes = it },
                                        label = { Text("Notes (e.g. Bank Ref, Cash, etc.)") },
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        imeAction =
                                                                androidx.compose.ui.text.input
                                                                        .ImeAction.Done
                                                ),
                                        keyboardActions =
                                                androidx.compose.foundation.text.KeyboardActions(
                                                        onDone = { focusManager.clearFocus() }
                                                ),
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .focusRequester(focusRequesterNotes),
                                        singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            PrimaryButton(
                                    onClick = {
                                        val amt = paymentAmount.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            viewModel.addSupplierPayment(
                                                    supplierId = supplier.supplierId,
                                                    supplierName = supplier.supplierName,
                                                    amount = amt,
                                                    notes =
                                                            paymentNotes.trim().takeIf {
                                                                it.isNotBlank()
                                                            }
                                            ) {
                                                showAddPaymentDialog = false
                                                // Refresh sheet details
                                                val updatedBalance =
                                                        supplierBalances.find {
                                                            it.supplierId == supplier.supplierId
                                                        }
                                                if (updatedBalance != null) {
                                                    selectedSupplierBalance = updatedBalance
                                                }
                                                android.widget.Toast.makeText(
                                                                context,
                                                                "Payment logged!",
                                                                android.widget.Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                            }
                                        }
                                    }
                            ) { Text("Save Payment") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddPaymentDialog = false }) {
                                Text("Cancel")
                            }
                        }
                )
            }
        }
    }
}
