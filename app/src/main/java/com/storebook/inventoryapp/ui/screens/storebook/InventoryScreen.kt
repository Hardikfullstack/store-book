@file:android.annotation.SuppressLint("LocalContextGetResourceValueCall")

package com.storebook.inventoryapp.ui.screens.storebook
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.ItemBatch
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.PurchaseItemDetail
import com.storebook.inventoryapp.shared.domain.models.Supplier
import com.storebook.inventoryapp.ui.components.DynamicFastScroller
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.theme.PrimaryButton
import com.storebook.inventoryapp.ui.theme.primaryGradient
import com.storebook.inventoryapp.ui.viewmodel.InventoryViewModel
import com.storebook.inventoryapp.utils.autoMarquee
import com.storebook.inventoryapp.utils.toRupee
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PAGE_SIZE = 50

@OptIn(ExperimentalMaterial3Api::class)
object FutureSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val todayStart = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        return utcTimeMillis >= todayStart
    }

    override fun isSelectableYear(year: Int): Boolean =
        year >=
            java.util.Calendar
                .getInstance()
                .get(java.util.Calendar.YEAR)
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
    )
}

@Composable
private fun ErrorText(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Filter state — rememberSaveable survives config changes ──────────────
    var searchQ by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var sortBy by rememberSaveable { mutableStateOf("Name") }
    var sortDescending by rememberSaveable { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // ── Infinite-scroll page state ────────────────────────────────────────────
    var displayedItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var filterMode by rememberSaveable { mutableStateOf("All") } // "All" or "NearExpiry"
    var hasMoreItems by remember { mutableStateOf(true) }
    var shouldScrollToTop by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val nearExpiryItems by viewModel.nearExpiryItems.collectAsStateWithLifecycle()
    val isLoadingItems by viewModel.isLoadingItems.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()

    // ── Delete confirmation dialog state ─────────────────────────────────────
    // ── Delete confirmation dialog state ─────────────────────────────────────
    var pendingDeleteItem by remember { mutableStateOf<Item?>(null) }
    var quickRefillItem by remember { mutableStateOf<Item?>(null) }
    var isQuickRefillSubmitting by remember { mutableStateOf(false) }
    // ── Add/Edit Bottom Sheet ─────────────────────────────────────────────────
    var showSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form inputs
    var inputName by remember { mutableStateOf("") }
    var inputQty by remember { mutableStateOf("") }
    var inputUnit by remember { mutableStateOf("pcs") }
    var inputBuyPrice by remember { mutableStateOf("") }
    var inputSellPrice by remember { mutableStateOf("") }
    var inputThreshold by remember { mutableStateOf("5") }
    var inputCategory by remember { mutableStateOf("Groceries") }
    var inputHsnCode by remember { mutableStateOf("") }
    var inputTaxRate by remember { mutableStateOf("") }
    var inputBarcode by remember { mutableStateOf("") }
    var inputBatchNumber by remember { mutableStateOf("") }
    var inputExpiryDateMs by remember { mutableStateOf<Long?>(null) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    var inputAdjustmentReason by remember { mutableStateOf("Count Correction") }
    var showAdvancedOptions by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var qtyError by remember { mutableStateOf(false) }
    var buyPriceError by remember { mutableStateOf(false) }
    var sellPriceError by remember { mutableStateOf(false) }

    val focusRequesterName = remember { FocusRequester() }
    val focusRequesterBarcode = remember { FocusRequester() }
    val focusRequesterQty = remember { FocusRequester() }
    val focusRequesterBuyPrice = remember { FocusRequester() }
    val focusRequesterSellPrice = remember { FocusRequester() }
    val focusRequesterThreshold = remember { FocusRequester() }
    val focusRequesterHsn = remember { FocusRequester() }
    val focusRequesterTax = remember { FocusRequester() }
    val focusRequesterBatch = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val barcodeScanner =
        remember {
            val options =
                com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
                    .Builder()
                    .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS)
                    .enableAutoZoom()
                    .build()
            com.google.mlkit.vision.codescanner.GmsBarcodeScanning
                .getClient(context, options)
        }

    val categoriesList =
        listOf("Groceries", "Dairy", "Beverages", "Stationery", "Household", "Others")
    val unitsList = listOf("pcs", "kg", "g", "litre", "ml", "dozen", "box", "packet")

    // ── Initial load ──────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.loadFilteredItems(
            searchQ,
            selectedCategory,
            sortBy + if (sortDescending) "_DESC" else "_ASC",
        )
    }

    // ── Debounce search + category/sort changes → trigger DB query ────────────
    LaunchedEffect(searchQ, selectedCategory, sortBy, sortDescending) {
        snapshotFlow { "$searchQ|$selectedCategory|$sortBy|$sortDescending" }
            .debounce(300L)
            .distinctUntilChanged()
            .collect { _ ->
                hasMoreItems = true
                shouldScrollToTop = true
                val actualSortBy = sortBy + if (sortDescending) "_DESC" else "_ASC"
                viewModel.loadFilteredItems(searchQ, selectedCategory, actualSortBy)
            }
    }

    // ── Update displayedItems based on filter mode ────────────────────────────────
    LaunchedEffect(filterMode, filteredItems, nearExpiryItems) {
        displayedItems = if (filterMode == "NearExpiry") nearExpiryItems else filteredItems
        // NearExpiry view shows all items, no pagination
        hasMoreItems = filterMode != "NearExpiry" && displayedItems.size >= PAGE_SIZE
        if (shouldScrollToTop) {
            listState.scrollToItem(0)
            shouldScrollToTop = false
        }
    }

    // ── Infinite scroll trigger — load next page when near bottom ─────────────
    val nearBottom by remember {
        derivedStateOf {
            val lastVisible =
                listState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 8
        }
    }

    LaunchedEffect(nearBottom) {
        if (nearBottom && hasMoreItems && !isLoadingMore) {
            isLoadingMore = true
            viewModel.loadMoreItems(
                search = searchQ,
                category = selectedCategory,
                sortBy = sortBy + if (sortDescending) "_DESC" else "_ASC",
                currentSize = displayedItems.size,
                pageSize = PAGE_SIZE,
            ) { more ->
                if (more.isEmpty()) {
                    hasMoreItems = false
                } else {
                    displayedItems = displayedItems + more
                    hasMoreItems = more.size >= PAGE_SIZE
                }
                isLoadingMore = false
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    fun openAddSheet() {
        editingItem = null
        inputName = ""
        inputQty = ""
        inputUnit = "pcs"
        inputBuyPrice = ""
        inputSellPrice = ""
        inputThreshold = "5"
        inputCategory = "Groceries"
        inputBarcode = ""
        inputHsnCode = ""
        inputTaxRate = ""
        inputBatchNumber = ""
        inputExpiryDateMs = null
        showAdvancedOptions = false
        nameError = false
        priceError = false
        qtyError = false
        buyPriceError = false
        sellPriceError = false
        showSheet = true
    }

    fun openEditSheet(item: Item) {
        editingItem = item
        inputName = item.name
        inputQty = formatQty(item.quantity)
        inputUnit = item.unit
        inputBuyPrice = formatQty(item.buyPrice)
        inputSellPrice = formatQty(item.sellPrice)
        inputThreshold = formatQty(item.lowStockThreshold)
        inputCategory = item.category
        inputBarcode = item.barcode ?: ""
        inputHsnCode = item.hsnCode ?: ""
        inputTaxRate = if (item.taxRate > 0) item.taxRate.toString() else ""
        inputBatchNumber = ""
        inputExpiryDateMs = null
        showAdvancedOptions = item.hsnCode != null || item.taxRate > 0
        nameError = false
        qtyError = false
        buyPriceError = false
        sellPriceError = false
        priceError = false
        showSheet = true

        scope.launch {
            val latestBatch = viewModel.getLatestBatchForItem(item.id)
            if (latestBatch != null) {
                inputBatchNumber = latestBatch.batchNumber ?: ""
                inputExpiryDateMs = latestBatch.expiryDate
                if (inputBatchNumber.isNotBlank() || inputExpiryDateMs != null) {
                    showAdvancedOptions = true
                }
            }
        }
    }

    fun performDelete(item: Item) {
        viewModel.deleteItem(item.id)
        android.widget.Toast
            .makeText(
                context,
                context.getString(R.string.inv_delete_success),
                android.widget.Toast.LENGTH_SHORT,
            ).show()
        // Optimistic UI update — remove immediately from local list
        displayedItems = displayedItems.filterNot { it.id == item.id }
    }

    // ── Delete confirmation dialog ─────────────────────────────────────────────
    DeleteConfirmationDialog(
        visible = pendingDeleteItem != null,
        itemName = pendingDeleteItem?.name ?: "",
        entityLabel = "item",
        onConfirm = {
            pendingDeleteItem?.let { performDelete(it) }
            pendingDeleteItem = null
        },
        onDismiss = { pendingDeleteItem = null },
        context = context,
    )

    // ── Quick Refill Dialog ──────────────────────────────────────────────────
    if (quickRefillItem != null) {
        var addQtyInput by remember { mutableStateOf("") }
        val refillItem = quickRefillItem!!
        var buyPriceInput by remember { mutableStateOf(formatQty(refillItem.buyPrice)) }
        var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
        var supplierSearchText by remember { mutableStateOf("") }
        var showSupplierDropdown by remember { mutableStateOf(false) }
        var refillBatchNumber by remember { mutableStateOf("") }
        var refillExpiryDateMs by remember { mutableStateOf<Long?>(null) }
        var showRefillDatePicker by remember { mutableStateOf(false) }
        val focusRequesterBuyPrice = remember { FocusRequester() }
        val focusRequesterSupplier = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        // Expiry Date Picker for Refill
        if (showRefillDatePicker) {
            val datePickerState =
                rememberDatePickerState(
                    initialSelectedDateMillis =
                        refillExpiryDateMs
                            ?: System.currentTimeMillis(),
                    selectableDates = FutureSelectableDates,
                )
            DatePickerDialog(
                onDismissRequest = { showRefillDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selected = datePickerState.selectedDateMillis
                            if (selected != null &&
                                selected <
                                System.currentTimeMillis() -
                                24 * 60 * 60 * 1000
                            ) {
                                android.widget.Toast
                                    .makeText(
                                        context,
                                        "Expiry date cannot be in the past",
                                        android.widget.Toast.LENGTH_SHORT,
                                    ).show()
                            } else {
                                refillExpiryDateMs = selected
                                showRefillDatePicker = false
                            }
                        },
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showRefillDatePicker = false }) { Text("Cancel") }
                },
            ) { DatePicker(state = datePickerState) }
        }

        val filteredSuppliers =
            remember(suppliers, supplierSearchText) {
                if (supplierSearchText.isBlank()) {
                    suppliers
                } else {
                    suppliers.filter { it.name.contains(supplierSearchText, ignoreCase = true) }
                }
            }

        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { if (!isQuickRefillSubmitting) quickRefillItem = null },
            title = {
                Column {
                    Text(
                        text = "Refill Stock",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = refillItem.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.autoMarquee(),
                    )
                }
            },
            text = {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Current → New stock preview strip
                    val addedQtyVal = addQtyInput.toDoubleOrNull() ?: 0.0
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Current",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${formatQty(refillItem.quantity)} ${refillItem.unit}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "After refill",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${formatQty(refillItem.quantity + addedQtyVal)} ${refillItem.unit}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

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
                            KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions =
                            KeyboardActions(
                                onNext = {
                                    if (viewModel.userRole != "staff") {
                                        focusRequesterBuyPrice.requestFocus()
                                    } else {
                                        focusRequesterSupplier.requestFocus()
                                    }
                                },
                            ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                            ),
                    )

                    // Presets — now show a checked state when they match the current input
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
                                isSelected = addQtyInput.toDoubleOrNull() == preset.toDouble(),
                                onClick = { addQtyInput = preset.toString() },
                            )
                        }
                    }

                    if (viewModel.userRole != "staff") {
                        OutlinedTextField(
                            value = buyPriceInput,
                            onValueChange = { buyPriceInput = it },
                            label = {
                                Text(
                                    "Buy Price (Per ${refillItem.unit})",
                                    modifier =
                                        androidx.compose.ui.Modifier
                                            .autoMarquee(),
                                )
                            },
                            prefix = { Text("₹ ") },
                            keyboardOptions =
                                KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusRequesterSupplier.requestFocus() }),
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterBuyPrice),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                ),
                        )

                        // Live total cost of this refill
                        val buyPriceVal = buyPriceInput.toDoubleOrNull()
                        if (addedQtyVal > 0 && buyPriceVal != null && buyPriceVal >= 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Total cost",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "₹${"%.2f".format(addedQtyVal * buyPriceVal)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // Supplier Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Supplier",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value =
                                    if (selectedSupplier !=
                                        null
                                    ) {
                                        selectedSupplier?.name ?: "N/A"
                                    } else {
                                        supplierSearchText
                                    },
                                onValueChange = {
                                    selectedSupplier = null
                                    supplierSearchText = it
                                    showSupplierDropdown = true
                                },
                                placeholder = {
                                    Text(
                                        "Search or type new supplier...",
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterSupplier),
                                singleLine = true,
                                leadingIcon =
                                    if (selectedSupplier != null) {
                                        {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Emerald500,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                trailingIcon = {
                                    IconButton(onClick = { showSupplierDropdown = !showSupplierDropdown }) {
                                        Icon(
                                            if (showSupplierDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle supplier list",
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    ),
                            )

                            DropdownMenu(
                                expanded =
                                    showSupplierDropdown &&
                                        (filteredSuppliers.isNotEmpty() || supplierSearchText.isNotBlank()),
                                onDismissRequest = { showSupplierDropdown = false },
                                properties = PopupProperties(focusable = false),
                                modifier = Modifier.fillMaxWidth(0.85f).heightIn(max = 200.dp),
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Cash Purchase / No Supplier") },
                                    leadingIcon = { Icon(Icons.Default.MoneyOff, contentDescription = null) },
                                    onClick = {
                                        selectedSupplier = null
                                        supplierSearchText = ""
                                        showSupplierDropdown = false
                                    },
                                )

                                filteredSuppliers.forEach { supplier ->
                                    DropdownMenuItem(
                                        text = { Text(supplier.name) },
                                        onClick = {
                                            selectedSupplier = supplier
                                            supplierSearchText = supplier.name
                                            showSupplierDropdown = false
                                        },
                                    )
                                }

                                if (supplierSearchText.isNotBlank() &&
                                    filteredSuppliers.none { it.name.equals(supplierSearchText, ignoreCase = true) }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Create supplier: \"$supplierSearchText\"") },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                        onClick = {
                                            viewModel.addSupplier(
                                                name = supplierSearchText,
                                                phone = null,
                                                gstin = null,
                                                address = null,
                                            ) { newId ->
                                                selectedSupplier = Supplier(id = newId, name = supplierSearchText)
                                            }
                                            showSupplierDropdown = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Batch & Expiry — collapsed by default to reduce clutter in a "quick" refill flow
                    var showBatchSection by remember {
                        mutableStateOf(refillBatchNumber.isNotBlank() || refillExpiryDateMs != null)
                    }
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(onClickLabel = "Toggle batch and expiry fields") {
                                    showBatchSection = !showBatchSection
                                }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Batch & Expiry (Optional)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Icon(
                            imageVector = if (showBatchSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    AnimatedVisibility(visible = showBatchSection) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = refillBatchNumber,
                                onValueChange = { refillBatchNumber = it },
                                label = {
                                    Text(
                                        "Batch / Lot Number",
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                placeholder = {
                                    Text(
                                        "e.g. MFG-2024-B1",
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    ),
                            )

                            OutlinedTextField(
                                value =
                                    refillExpiryDateMs?.let {
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Text(
                                        "Expiry Date",
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                placeholder = {
                                    Text(
                                        "Tap calendar icon to set",
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    ),
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (refillExpiryDateMs != null) {
                                            TextButton(onClick = { refillExpiryDateMs = null }) {
                                                Text("Clear", fontSize = 11.sp)
                                            }
                                        }
                                        IconButton(onClick = { showRefillDatePicker = true }) {
                                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Expiry Date")
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                val addedQtyCheck = addQtyInput.toDoubleOrNull()
                val buyPriceCheck =
                    if (viewModel.userRole == "staff") refillItem.buyPrice else buyPriceInput.toDoubleOrNull()
                val isValid =
                    addedQtyCheck != null && addedQtyCheck > 0.0 && buyPriceCheck != null && buyPriceCheck >= 0.0

                PrimaryButton(
                    enabled = !isQuickRefillSubmitting && isValid,
                    onClick = {
                        if (isQuickRefillSubmitting) return@PrimaryButton
                        val addedQty = addQtyInput.toDoubleOrNull()
                        val finalBuyPrice =
                            if (viewModel.userRole == "staff") refillItem.buyPrice else buyPriceInput.toDoubleOrNull()

                        if (addedQty == null || addedQty.isNaN() || addedQty <= 0.0) {
                            Toast.makeText(context, "Please enter a valid positive quantity", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        if (finalBuyPrice == null || finalBuyPrice < 0.0) {
                            Toast.makeText(context, "Please enter a valid buy price", Toast.LENGTH_SHORT).show()
                            return@PrimaryButton
                        }
                        if (addedQty > 0) {
                            val purchase =
                                Purchase(
                                    supplierId = selectedSupplier?.id ?: 0L,
                                    supplierName = selectedSupplier?.name ?: "Cash / Anonymous",
                                    totalAmount = addedQty * finalBuyPrice,
                                    taxAmount = (addedQty * finalBuyPrice) * (refillItem.taxRate / 100.0),
                                    type = "BILL",
                                    timestamp = System.currentTimeMillis(),
                                    notes = "Refill stock for ${refillItem.name}",
                                    items =
                                        listOf(
                                            PurchaseItemDetail(
                                                purchaseId = 0L,
                                                itemId = refillItem.id,
                                                itemName = refillItem.name,
                                                quantity = addedQty,
                                                unit = refillItem.unit,
                                                buyPrice = finalBuyPrice,
                                            ),
                                        ),
                                )
                            isQuickRefillSubmitting = true
                            viewModel.addPurchase(purchase) {
                                if (refillBatchNumber.isNotBlank() || refillExpiryDateMs != null) {
                                    viewModel.addItemBatch(
                                        ItemBatch(
                                            itemId = refillItem.id,
                                            batchNumber = refillBatchNumber.trim().takeIf { it.isNotBlank() },
                                            expiryDate = refillExpiryDateMs,
                                            quantity = addedQty,
                                            costPrice = finalBuyPrice,
                                            timestamp = System.currentTimeMillis(),
                                            notes = "Refill for ${refillItem.name}",
                                        ),
                                    )
                                }
                                Toast.makeText(context, "Stock refilled & purchase logged!", Toast.LENGTH_SHORT).show()
                                quickRefillItem = null
                            }
                        } else {
                            quickRefillItem = null
                        }
                    },
                ) {
                    if (isQuickRefillSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adding...")
                    } else {
                        Text("Add Stock")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isQuickRefillSubmitting,
                    onClick = { quickRefillItem = null },
                ) { Text("Cancel") }
            },
        )
    }

    // ── Main UI ──────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.primaryGradient)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.tab_inventory),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text =
                                "${displayedItems.size}${if (hasMoreItems) "+" else ""} items",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        )
                    }

                    // Sort toggle chip
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.onPrimary),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .clickable(onClickLabel = "Action") {
                                        sortBy =
                                            when (sortBy) {
                                                "Name" -> "Qty"
                                                "Qty" -> "Price"
                                                else -> "Name"
                                            }
                                    }.padding(
                                        start = 12.dp,
                                        top = 6.dp,
                                        bottom = 6.dp,
                                        end = 6.dp,
                                    ),
                        ) {
                            Text(
                                text =
                                    stringResource(
                                        id = R.string.inv_sort_dynamic,
                                        when (sortBy) {
                                            "Qty" -> "Stock"
                                            "Price" -> "Price"
                                            else -> "Name"
                                        },
                                    ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Box(
                            modifier =
                                Modifier
                                    .clickable(onClickLabel = "Action") {
                                        sortDescending = !sortDescending
                                    }.padding(
                                        start = 6.dp,
                                        top = 6.dp,
                                        bottom = 6.dp,
                                        end = 12.dp,
                                    ),
                        ) {
                            Icon(
                                imageVector =
                                    if (sortDescending) {
                                        Icons.Default.KeyboardArrowDown
                                    } else {
                                        Icons.Default.KeyboardArrowUp
                                    },
                                contentDescription = "Sort Order",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar — debounced via LaunchedEffect above
                OutlinedTextField(
                    value = searchQ,
                    onValueChange = { searchQ = it },
                    placeholder = {
                        Text(
                            stringResource(id = R.string.inv_search_hint),
                            fontSize = 14.sp,
                            modifier = Modifier.autoMarquee(),
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.ui_element_desc),
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            AnimatedVisibility(visible = isLoadingItems) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }
                            AnimatedVisibility(
                                visible = searchQ.isNotEmpty(),
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut(),
                            ) {
                                IconButton(
                                    onClick = { searchQ = "" },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Cancel,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            val isFilterActive = selectedCategory != "All" || filterMode != "All"
                            Box(
                                modifier =
                                    Modifier
                                        .padding(start = 2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isFilterActive) {
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                            } else {
                                                Color.Transparent
                                            },
                                        ).clickable(onClickLabel = "Open filters") {
                                            showFilterSheet = true
                                        }.padding(6.dp),
                            ) {
                                Box {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription =
                                            if (isFilterActive) "Filters (active)" else "Filters",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    if (isFilterActive) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(6.dp)
                                                    .align(Alignment.TopEnd)
                                                    .clip(CircleShape)
                                                    .background(Coral500),
                                        )
                                    }
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions =
                        KeyboardActions(
                            onSearch = { focusManager.clearFocus() },
                        ),
                    colors =
                        OutlinedTextFieldDefaults.colors(
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddSheet() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.size(26.dp),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            when {
                isLoadingItems && displayedItems.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                stringResource(id = R.string.inv_loading),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
                displayedItems.isEmpty() -> {
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
                                        .background(
                                            MaterialTheme.colorScheme
                                                .primaryContainer
                                                .copy(
                                                    alpha = 0.3f,
                                                ),
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector =
                                        androidx.compose.material.icons.Icons.Outlined
                                            .Inventory,
                                    contentDescription =
                                        stringResource(R.string.ui_element_desc),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(50.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text =
                                    if (searchQ.isBlank() &&
                                        selectedCategory == "All" &&
                                        filterMode == "All"
                                    ) {
                                        "No stock yet?\nYour first item is just a tap away!"
                                    } else {
                                        stringResource(id = R.string.search_no_results)
                                    },
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp,
                            )
                            if (searchQ.isBlank() &&
                                selectedCategory == "All" &&
                                filterMode == "All"
                            ) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Add here ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription =
                                            stringResource(R.string.ui_element_desc),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp).rotate(45f),
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                    ) {
                        items(
                            items = displayedItems,
                            key = { it.id },
                            contentType = {
                                "inventory_item"
                            }, // stable content type for Compose recycling
                        ) { item ->
                            val isLowStock = item.quantity <= item.lowStockThreshold

                            // Both swipe directions: right=quick restock options, left=delete
                            val dismissState = rememberSwipeToDismissBoxState()

                            LaunchedEffect(dismissState.currentValue) {
                                when (dismissState.currentValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        // Do nothing in LaunchedEffect to let the swipe box stay
                                        // open
                                        // so the user can interact with the presets in
                                        // backgroundContent
                                    }
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        if (shouldSkipInventoryDeleteConfirm(context)) {
                                            performDelete(item)
                                        } else {
                                            pendingDeleteItem = item
                                        }
                                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                    }
                                    else -> {}
                                }
                            }

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = true,
                                backgroundContent = {
                                    val direction = dismissState.dismissDirection
                                    if (direction == SwipeToDismissBoxValue.StartToEnd) {
                                        // Swipe right = quick restock presets (green)
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(
                                                        MaterialTheme
                                                            .colorScheme
                                                            .error,
                                                    ),
                                            contentAlignment = Alignment.CenterEnd,
                                        ) {
                                            Row(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = 16.dp,
                                                        ),
                                                verticalAlignment =
                                                    Alignment.CenterVertically,
                                                horizontalArrangement =
                                                    Arrangement.SpaceBetween,
                                            ) {
                                                Row(
                                                    verticalAlignment =
                                                        Alignment.CenterVertically,
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Restock",
                                                        tint =
                                                            MaterialTheme.colorScheme
                                                                .onPrimary,
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "Restock",
                                                        color =
                                                            MaterialTheme.colorScheme
                                                                .onPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                    )
                                                }

                                                // Presets
                                                Row(
                                                    verticalAlignment =
                                                        Alignment.CenterVertically,
                                                    horizontalArrangement =
                                                        Arrangement.spacedBy(8.dp),
                                                ) {
                                                    val presets = listOf(5, 10, 50)
                                                    presets.forEach { preset ->
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .clip(
                                                                        RoundedCornerShape(
                                                                            12.dp,
                                                                        ),
                                                                    ).background(
                                                                        MaterialTheme
                                                                            .colorScheme
                                                                            .onPrimary
                                                                            .copy(
                                                                                alpha =
                                                                                0.2f,
                                                                            ),
                                                                    ).clickable(
                                                                        onClickLabel =
                                                                            "Action",
                                                                    ) {
                                                                        scope
                                                                            .launch {
                                                                                val purchase =
                                                                                    Purchase(
                                                                                        supplierId =
                                                                                        0L,
                                                                                        supplierName =
                                                                                            viewModel
                                                                                                .lastRestockSupplierName
                                                                                                .ifBlank {
                                                                                                    "Cash / Anonymous"
                                                                                                },
                                                                                        totalAmount =
                                                                                            item.buyPrice *
                                                                                                preset,
                                                                                        taxAmount =
                                                                                            (
                                                                                                item.buyPrice *
                                                                                                    preset
                                                                                            ) *
                                                                                                (
                                                                                                    item.taxRate /
                                                                                                        100.0
                                                                                                ),
                                                                                        type =
                                                                                            "BILL",
                                                                                        timestamp =
                                                                                            System
                                                                                                .currentTimeMillis(),
                                                                                        notes =
                                                                                            "Quick +$preset restock",
                                                                                        items =
                                                                                            listOf(
                                                                                                PurchaseItemDetail(
                                                                                                    purchaseId =
                                                                                                    0L,
                                                                                                    itemId =
                                                                                                        item.id,
                                                                                                    itemName =
                                                                                                        item.name,
                                                                                                    quantity =
                                                                                                        preset
                                                                                                            .toDouble(),
                                                                                                    unit =
                                                                                                        item.unit,
                                                                                                    buyPrice =
                                                                                                        item.buyPrice,
                                                                                                ),
                                                                                            ),
                                                                                    )
                                                                                viewModel
                                                                                    .addPurchase(
                                                                                        purchase,
                                                                                    ) {
                                                                                    }
                                                                                android.widget
                                                                                    .Toast
                                                                                    .makeText(
                                                                                        context,
                                                                                        "+$preset " +
                                                                                            "${item.name} restocked",
                                                                                        android.widget
                                                                                            .Toast
                                                                                            .LENGTH_SHORT,
                                                                                    ).show()
                                                                                dismissState
                                                                                    .snapTo(
                                                                                        SwipeToDismissBoxValue
                                                                                            .Settled,
                                                                                    )
                                                                            }
                                                                    }.padding(
                                                                        horizontal =
                                                                            10.dp,
                                                                        vertical =
                                                                            6.dp,
                                                                    ),
                                                            contentAlignment =
                                                                Alignment.Center,
                                                        ) {
                                                            Text(
                                                                "+$preset",
                                                                color =
                                                                    MaterialTheme
                                                                        .colorScheme
                                                                        .onPrimary,
                                                                fontWeight =
                                                                    FontWeight.Black,
                                                                fontSize = 12.sp,
                                                            )
                                                        }
                                                    }

                                                    // Custom button
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .clip(
                                                                    RoundedCornerShape(
                                                                        12.dp,
                                                                    ),
                                                                ).background(
                                                                    MaterialTheme
                                                                        .colorScheme
                                                                        .onPrimary
                                                                        .copy(
                                                                            alpha =
                                                                            0.2f,
                                                                        ),
                                                                ).clickable(
                                                                    onClickLabel =
                                                                        "Action",
                                                                ) {
                                                                    quickRefillItem =
                                                                        item
                                                                    scope.launch {
                                                                        dismissState
                                                                            .snapTo(
                                                                                SwipeToDismissBoxValue
                                                                                    .Settled,
                                                                            )
                                                                    }
                                                                }.padding(
                                                                    horizontal =
                                                                        10.dp,
                                                                    vertical =
                                                                        6.dp,
                                                                ),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Text(
                                                            "Custom",
                                                            color =
                                                                MaterialTheme
                                                                    .colorScheme
                                                                    .onPrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                        )
                                                    }

                                                    // Cancel button
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .clip(CircleShape)
                                                                .background(
                                                                    MaterialTheme
                                                                        .colorScheme
                                                                        .onPrimary
                                                                        .copy(
                                                                            alpha =
                                                                            0.15f,
                                                                        ),
                                                                ).clickable(
                                                                    onClickLabel =
                                                                        "Action",
                                                                ) {
                                                                    scope.launch {
                                                                        dismissState
                                                                            .snapTo(
                                                                                SwipeToDismissBoxValue
                                                                                    .Settled,
                                                                            )
                                                                    }
                                                                }.padding(6.dp),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Close,
                                                            contentDescription = "Cancel",
                                                            tint =
                                                                MaterialTheme
                                                                    .colorScheme
                                                                    .onPrimary,
                                                            modifier = Modifier.size(16.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Swipe left = delete (red)
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(
                                                        MaterialTheme
                                                            .colorScheme
                                                            .error,
                                                    ),
                                            contentAlignment = Alignment.CenterEnd,
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(end = 20.dp),
                                                verticalAlignment =
                                                    Alignment.CenterVertically,
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint =
                                                        MaterialTheme.colorScheme
                                                            .onError,
                                                )
                                            }
                                        }
                                    }
                                },
                            ) {
                                InventoryItemCard(
                                    item = item,
                                    isLowStock = isLowStock,
                                    userRole = viewModel.userRole,
                                    onClick = { openEditSheet(item) },
                                    onRefillClick = { quickRefillItem = item },
                                )
                            }
                        }

                        // Loading footer for infinite scroll
                        if (isLoadingMore) {
                            item(contentType = "loading_footer") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        stringResource(id = R.string.inv_loading_more),
                                        fontSize = 12.sp,
                                        color =
                                            MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.5f,
                                            ),
                                    )
                                }
                            }
                        }

                        // End of list indicator
                        if (!hasMoreItems && displayedItems.size > PAGE_SIZE) {
                            item(contentType = "end_of_list") {
                                Text(
                                    text =
                                        stringResource(
                                            id = R.string.inv_all_items_loaded,
                                            displayedItems.size,
                                        ),
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    color =
                                        MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.35f,
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            // Premium Dynamic Fast Scroller Overlay
            if (displayedItems.isNotEmpty() && !isLoadingItems && filterMode != "NearExpiry") {
                DynamicFastScroller(
                    listState = listState,
                    itemsCount = displayedItems.size,
                    thumbLabel = { index ->
                        val item = displayedItems.getOrNull(index)
                        item?.let { "${it.category} • ${it.name.take(1).uppercase()}" } ?: ""
                    },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
                )
            }

            // ── Premium Filter Bottom Sheet ──────────────────────────────────────────
            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterSheet = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Filters & Categories",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (selectedCategory != "All" || filterMode != "All") {
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        selectedCategory = "All"
                                        filterMode = "All"
                                    },
                                    contentPadding =
                                        PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp),
                                ) {
                                    Text(
                                        "Reset",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }

                        Text(
                            "Quick Filters",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                label = stringResource(id = R.string.inv_filter_all),
                                isSelected = selectedCategory == "All" && filterMode == "All",
                                onClick = {
                                    selectedCategory = "All"
                                    filterMode = "All"
                                    showFilterSheet = false
                                },
                            )
                            FilterChip(
                                label = stringResource(id = R.string.inv_filter_low_stock),
                                icon = Icons.Outlined.WarningAmber,
                                isSelected = selectedCategory == "Low Stock",
                                onClick = {
                                    selectedCategory = "Low Stock"
                                    showFilterSheet = false
                                },
                            )
                            FilterChip(
                                label =
                                    "Expiring ≤30d" +
                                        "${if (nearExpiryItems.isNotEmpty()) " (${nearExpiryItems.size})" else ""}",
                                icon = Icons.Outlined.Schedule,
                                isSelected = filterMode == "NearExpiry",
                                onClick = {
                                    filterMode =
                                        if (filterMode == "NearExpiry") {
                                            "All"
                                        } else {
                                            "NearExpiry"
                                        }
                                    showFilterSheet = false
                                },
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            "Sort By",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilterChip(
                                label = "Name",
                                isSelected = sortBy == "Name",
                                onClick = { sortBy = "Name" },
                            )
                            FilterChip(
                                label = "Stock",
                                isSelected = sortBy == "Qty",
                                onClick = { sortBy = "Qty" },
                            )
                            FilterChip(
                                label = "Price",
                                isSelected = sortBy == "Price",
                                onClick = { sortBy = "Price" },
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Asc/Desc toggle
                            IconButton(
                                onClick = { sortDescending = !sortDescending },
                                modifier =
                                    Modifier
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme
                                                .primaryContainer,
                                        ).size(36.dp),
                            ) {
                                Icon(
                                    imageVector =
                                        if (sortDescending) {
                                            Icons.Default.KeyboardArrowDown
                                        } else {
                                            Icons.Default.KeyboardArrowUp
                                        },
                                    contentDescription =
                                        if (sortDescending) "Descending" else "Ascending",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            "All Categories",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            categoriesList.forEach { cat ->
                                FilterChip(
                                    label = cat,
                                    isSelected = selectedCategory == cat,
                                    onClick = {
                                        selectedCategory = cat
                                        filterMode = "All"
                                        showFilterSheet = false // Auto-close on selection
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // ── Add / Edit Bottom Sheet ──────────────────────────────────────────
            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .imePadding()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        LaunchedEffect(Unit) { focusRequesterName.requestFocus() }

                        // ── Header: title + explicit close button ─────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text =
                                    if (editingItem == null) {
                                        stringResource(id = R.string.inv_add_title)
                                    } else {
                                        stringResource(id = R.string.inv_edit_title)
                                    },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            IconButton(onClick = { showSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        // ── Section: Basic Details ─────────────────────────────────────────
                        SectionLabel("Basic Details")
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = inputName,
                                onValueChange = {
                                    inputName = it
                                    nameError = false
                                },
                                label = {
                                    Text(
                                        stringResource(id = R.string.inv_name_label),
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterName),
                                singleLine = true,
                                isError = nameError,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusRequesterQty.requestFocus() }),
                                supportingText =
                                    if (nameError) {
                                        { ErrorText(stringResource(id = R.string.inv_err_empty_name)) }
                                    } else {
                                        null
                                    },
                            )

                            OutlinedTextField(
                                value = inputBarcode,
                                onValueChange = { inputBarcode = it },
                                label = {
                                    Text(
                                        "Barcode (Optional)",
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                placeholder = {
                                    Text(
                                        "e.g. 890123456789",
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusRequesterQty.requestFocus() }),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterBarcode),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        barcodeScanner
                                            .startScan()
                                            .addOnSuccessListener { barcode ->
                                                val code = barcode.rawValue
                                                if (!code.isNullOrBlank()) {
                                                    inputBarcode = code
                                                    Toast.makeText(context, "Scanned: $code", Toast.LENGTH_SHORT).show()
                                                }
                                            }.addOnFailureListener { e: Exception ->
                                                Toast
                                                    .makeText(context, "Scan failed: ${e.message}", Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                    }) {
                                        Icon(
                                            Icons.Default.QrCodeScanner,
                                            contentDescription = "Scan Barcode",
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                },
                            )
                        }

                        // ── Section: Stock ───────────────────────────────────────────────
                        SectionLabel("Stock")
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = inputQty,
                                onValueChange = {
                                    inputQty = it
                                    qtyError = false
                                },
                                label = {
                                    Text(
                                        stringResource(id = R.string.inv_qty_label),
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                suffix = { Text(inputUnit) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions =
                                    KeyboardActions(
                                        onNext = {
                                            if (viewModel.userRole != "staff") {
                                                focusRequesterBuyPrice.requestFocus()
                                            } else {
                                                focusRequesterSellPrice.requestFocus()
                                            }
                                        },
                                    ),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterQty),
                                singleLine = true,
                                isError = qtyError,
                                supportingText =
                                    if (qtyError) {
                                        { ErrorText(stringResource(id = R.string.inv_err_qty)) }
                                    } else {
                                        null
                                    },
                            )

                            // Reason for stock adjustment when editing and qty differs
                            AnimatedVisibility(
                                visible =
                                    editingItem != null &&
                                        inputQty.toDoubleOrNull() != null &&
                                        inputQty.toDoubleOrNull() != editingItem?.quantity,
                            ) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        "Reason for Stock Adjustment",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val reasons = listOf("Count Correction", "Damage", "Expiry", "Loss")
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(reasons, key = { it }) { r ->
                                            FilterChip(
                                                label = r,
                                                isSelected = inputAdjustmentReason == r,
                                                onClick = { inputAdjustmentReason = r },
                                            )
                                        }
                                    }
                                }
                            }

                            // Unit picker chips
                            Column {
                                Text(
                                    stringResource(id = R.string.inv_unit_label),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(unitsList, key = { it }) { u ->
                                        FilterChip(
                                            label = u,
                                            isSelected = inputUnit == u,
                                            onClick = { inputUnit = u },
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = inputThreshold,
                                onValueChange = { inputThreshold = it },
                                label = {
                                    Text(
                                        stringResource(id = R.string.inv_threshold_label),
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                suffix = { Text(inputUnit) },
                                supportingText = {
                                    Text(
                                        "Alert shown when stock falls to or below this",
                                        fontSize =
                                            11
                                                .sp,
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions =
                                    KeyboardActions(
                                        onNext = {
                                            if (showAdvancedOptions) {
                                                focusRequesterHsn.requestFocus()
                                            } else {
                                                focusManager
                                                    .clearFocus()
                                            }
                                        },
                                    ),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterThreshold),
                                singleLine = true,
                            )
                        }

                        // ── Section: Pricing ─────────────────────────────────────────────
                        SectionLabel("Pricing")
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                if (viewModel.userRole != "staff") {
                                    OutlinedTextField(
                                        value = inputBuyPrice,
                                        onValueChange = {
                                            inputBuyPrice = it
                                            buyPriceError = false
                                        },
                                        label = {
                                            Text(
                                                stringResource(id = R.string.inv_buy_price_label),
                                                modifier =
                                                    androidx.compose.ui.Modifier
                                                        .autoMarquee(),
                                            )
                                        },
                                        prefix = { Text("₹ ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                        keyboardActions =
                                            KeyboardActions(onNext = {
                                                focusRequesterSellPrice
                                                    .requestFocus()
                                            }),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesterBuyPrice),
                                        singleLine = true,
                                        isError = buyPriceError,
                                        supportingText =
                                            if (buyPriceError) {
                                                { ErrorText("Enter valid buy price") }
                                            } else {
                                                null
                                            },
                                    )
                                }
                                OutlinedTextField(
                                    value = inputSellPrice,
                                    onValueChange = {
                                        inputSellPrice = it
                                        sellPriceError = false
                                    },
                                    label = {
                                        Text(
                                            stringResource(id = R.string.inv_sell_price_label),
                                            modifier =
                                                androidx.compose.ui.Modifier
                                                    .autoMarquee(),
                                        )
                                    },
                                    prefix = { Text("₹ ") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                    keyboardActions =
                                        KeyboardActions(onNext = {
                                            focusRequesterThreshold.requestFocus()
                                        }),
                                    modifier = Modifier.weight(1f).focusRequester(focusRequesterSellPrice),
                                    singleLine = true,
                                    isError = sellPriceError,
                                    supportingText =
                                        if (sellPriceError) {
                                            { ErrorText("Enter valid sell price") }
                                        } else {
                                            null
                                        },
                                )
                            }

                            // Live margin preview — pure UI feedback, no side effects
                            if (viewModel.userRole != "staff") {
                                val buyVal = inputBuyPrice.toDoubleOrNull()
                                val sellVal = inputSellPrice.toDoubleOrNull()
                                if (buyVal != null && sellVal != null && buyVal > 0) {
                                    val marginPct = ((sellVal - buyVal) / buyVal * 100).toInt()
                                    val profitPerUnit = sellVal - buyVal
                                    val marginColor =
                                        when {
                                            marginPct < 0 -> MaterialTheme.colorScheme.error
                                            marginPct < 15 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            else -> Emerald500
                                        }
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(marginColor.copy(alpha = 0.1f))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = if (marginPct < 0) "Selling at a loss" else "Margin on this item",
                                            fontSize = 12.sp,
                                            color = marginColor,
                                        )
                                        Text(
                                            text =
                                                "${if (marginPct >= 0) "+" else ""}$marginPct% (₹${"%.2f".format(profitPerUnit)}/$inputUnit)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = marginColor,
                                        )
                                    }
                                }
                            }
                        }

                        // ── Advanced options: expandable card, not a bare row ─────────────
                        val chevronRotation by
                            androidx.compose.animation.core.animateFloatAsState(
                                targetValue = if (showAdvancedOptions) 180f else 0f,
                                label = "chevron_rotation",
                            )
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable(onClickLabel = "Toggle advanced options") {
                                            showAdvancedOptions = !showAdvancedOptions
                                        }.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Advanced (HSN, Tax, Batch & Expiry)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (showAdvancedOptions) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.rotate(chevronRotation),
                                )
                            }

                            val sheetDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

                            if (showExpiryDatePicker) {
                                val dpState =
                                    rememberDatePickerState(
                                        initialSelectedDateMillis = inputExpiryDateMs ?: System.currentTimeMillis(),
                                        selectableDates = FutureSelectableDates,
                                    )
                                DatePickerDialog(
                                    onDismissRequest = { showExpiryDatePicker = false },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                val selected = dpState.selectedDateMillis
                                                if (selected != null &&
                                                    selected < System.currentTimeMillis() - 24 * 60 * 60 * 1000
                                                ) {
                                                    Toast
                                                        .makeText(context, "Expiry date cannot be in the past", Toast.LENGTH_SHORT)
                                                        .show()
                                                } else {
                                                    inputExpiryDateMs = selected
                                                    showExpiryDatePicker = false
                                                }
                                            },
                                        ) { Text("OK") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showExpiryDatePicker = false }) { Text("Cancel") }
                                    },
                                ) { DatePicker(state = dpState) }
                            }

                            AnimatedVisibility(visible = showAdvancedOptions) {
                                Column(
                                    modifier =
                                        Modifier
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                            .padding(bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        OutlinedTextField(
                                            value = inputHsnCode,
                                            onValueChange = { newValue ->
                                                inputHsnCode = newValue
                                                val rate =
                                                    com.storebook.inventoryapp.utils.HsnTaxLookup
                                                        .getTaxRate(newValue)
                                                if (rate != null) {
                                                    inputTaxRate =
                                                        if (rate % 1.0 ==
                                                            0.0
                                                        ) {
                                                            rate.toInt().toString()
                                                        } else {
                                                            rate.toString()
                                                        }
                                                } else if (newValue.isBlank()) {
                                                    inputTaxRate = "0"
                                                }
                                            },
                                            label = {
                                                Text(
                                                    "HSN/SAC Code",
                                                    modifier =
                                                        androidx.compose.ui.Modifier
                                                            .autoMarquee(),
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                            keyboardActions =
                                                KeyboardActions(onNext = {
                                                    focusRequesterTax
                                                        .requestFocus()
                                                }),
                                            modifier = Modifier.weight(1f).focusRequester(focusRequesterHsn),
                                            singleLine = true,
                                        )
                                        OutlinedTextField(
                                            value = inputTaxRate,
                                            onValueChange = { inputTaxRate = it },
                                            label = {
                                                Text(
                                                    "Tax Rate (%)",
                                                    modifier =
                                                        androidx.compose.ui.Modifier
                                                            .autoMarquee(),
                                                )
                                            },
                                            suffix = { Text("%") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                            keyboardActions =
                                                KeyboardActions(onNext = {
                                                    focusRequesterBatch
                                                        .requestFocus()
                                                }),
                                            modifier = Modifier.weight(1f).focusRequester(focusRequesterTax),
                                            singleLine = true,
                                        )
                                    }

                                    HorizontalDivider(
                                        color =
                                            MaterialTheme.colorScheme.outlineVariant
                                                .copy(alpha = 0.4f),
                                    )

                                    Text(
                                        text = "Batch & Expiry Tracking (Optional)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    OutlinedTextField(
                                        value = inputBatchNumber,
                                        onValueChange = { inputBatchNumber = it },
                                        label = {
                                            Text(
                                                "Batch / Lot Number",
                                                modifier =
                                                    androidx.compose.ui.Modifier
                                                        .autoMarquee(),
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                "e.g. MFG-2024-B1",
                                                modifier =
                                                    androidx.compose.ui.Modifier
                                                        .autoMarquee(),
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterBatch),
                                        singleLine = true,
                                    )

                                    OutlinedTextField(
                                        value = inputExpiryDateMs?.let { sheetDateFormatter.format(Date(it)) } ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = {
                                            Text(
                                                "Expiry Date",
                                                modifier =
                                                    androidx.compose.ui.Modifier
                                                        .autoMarquee(),
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                "Tap calendar icon to set",
                                                modifier =
                                                    androidx.compose.ui.Modifier
                                                        .autoMarquee(),
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = false,
                                        colors =
                                            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            ),
                                        trailingIcon = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (inputExpiryDateMs != null) {
                                                    TextButton(onClick = { inputExpiryDateMs = null }) {
                                                        Text("Clear", fontSize = 11.sp)
                                                    }
                                                }
                                                IconButton(onClick = { showExpiryDatePicker = true }) {
                                                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick Expiry Date")
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        }

                        // ── Save button — unchanged logic ──────────────────────────────────
                        PrimaryButton(
                            onClick = {
                                val name = inputName.trim()
                                val qty = inputQty.toDoubleOrNull()
                                val buy =
                                    if (viewModel.userRole == "staff") {
                                        (inputSellPrice.toDoubleOrNull() ?: 0.0)
                                    } else {
                                        inputBuyPrice.toDoubleOrNull()
                                    }
                                val sell = inputSellPrice.toDoubleOrNull()
                                val threshold = inputThreshold.toDoubleOrNull() ?: 5.0
                                val hsn =
                                    if (showAdvancedOptions) {
                                        inputHsnCode.trim().takeIf { it.isNotBlank() }
                                    } else {
                                        editingItem?.hsnCode
                                    }
                                val tax =
                                    if (showAdvancedOptions) {
                                        inputTaxRate.toDoubleOrNull() ?: 0.0
                                    } else {
                                        editingItem?.taxRate ?: 0.0
                                    }
                                val barcode = inputBarcode.trim().takeIf { it.isNotBlank() }
                                val batchNum =
                                    if (showAdvancedOptions) {
                                        inputBatchNumber.trim().takeIf { it.isNotBlank() }
                                    } else {
                                        null
                                    }
                                val expiryMs = if (showAdvancedOptions) inputExpiryDateMs else null

                                nameError = name.isBlank()
                                buyPriceError = viewModel.userRole != "staff" && (buy == null || buy < 0.0)
                                sellPriceError = sell == null || sell <= 0.0
                                qtyError = qty == null || qty < 0.0

                                if (nameError || qtyError || buyPriceError || sellPriceError) {
                                    return@PrimaryButton
                                }
                                priceError = buy == null || sell == null || buy < 0.0 || sell <= 0.0
                                if (nameError || qtyError || priceError || buyPriceError || sellPriceError) {
                                    return@PrimaryButton
                                }

                                val safeQty =
                                    qty ?: run {
                                        Toast.makeText(context, "Quantity required", Toast.LENGTH_SHORT).show()
                                        return@PrimaryButton
                                    }
                                val safeBuy =
                                    buy ?: run {
                                        Toast.makeText(context, "Buy price required", Toast.LENGTH_SHORT).show()
                                        return@PrimaryButton
                                    }
                                val safeSell =
                                    sell ?: run {
                                        Toast.makeText(context, "Sell price required", Toast.LENGTH_SHORT).show()
                                        return@PrimaryButton
                                    }

                                if (editingItem == null) {
                                    viewModel.addItem(
                                        name, safeQty, inputUnit, safeBuy, safeSell, threshold, inputCategory,
                                        barcode = barcode, hsnCode = hsn, taxRate = tax,
                                    ) { newItemId ->
                                        if (batchNum != null || expiryMs != null) {
                                            viewModel.addItemBatch(
                                                ItemBatch(
                                                    itemId = newItemId,
                                                    batchNumber = batchNum,
                                                    expiryDate = expiryMs,
                                                    quantity = safeQty,
                                                    costPrice = safeBuy,
                                                    timestamp = System.currentTimeMillis(),
                                                    notes = "Initial stock batch",
                                                ),
                                            )
                                        }
                                    }
                                } else {
                                    viewModel.updateItem(
                                        editingItem?.id ?: run {
                                            Toast.makeText(context, "Invalid item", Toast.LENGTH_SHORT).show()
                                            return@PrimaryButton
                                        },
                                        name, safeQty, inputUnit, safeBuy, safeSell, threshold, inputCategory,
                                        barcode = barcode, hsnCode = hsn, taxRate = tax, adjustmentReason = inputAdjustmentReason,
                                    )
                                    if (batchNum != null || expiryMs != null) {
                                        viewModel.addItemBatch(
                                            ItemBatch(
                                                itemId =
                                                    editingItem?.id ?: run {
                                                        Toast
                                                            .makeText(context, "Invalid item", Toast.LENGTH_SHORT)
                                                            .show()
                                                        return@PrimaryButton
                                                    },
                                                batchNumber = batchNum,
                                                expiryDate = expiryMs,
                                                quantity = qty,
                                                costPrice = safeBuy,
                                                timestamp = System.currentTimeMillis(),
                                                notes = "Batch logged on edit",
                                            ),
                                        )
                                    }
                                }
                                showSheet = false
                                Toast
                                    .makeText(context, context.getString(R.string.inv_save_success), Toast.LENGTH_SHORT)
                                    .show()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(stringResource(id = R.string.btn_save), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ── Price Column ─────────────────────────────────────────────────────────────────

@Composable
private fun PriceColumn(
    label: String,
    value: String,
    valueColor: Color,
    alignEnd: Boolean = false,
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
        )
    }
}

// ── Item Card ─────────────────────────────────────────────────────────────────

@Composable
fun InventoryItemCard(
    item: Item,
    isLowStock: Boolean,
    userRole: String,
    onClick: () -> Unit,
    onRefillClick: () -> Unit,
) {
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.05f
    // In dark mode dim the white text so it doesn't glare on the dark-red card
    val lowStockTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.75f) else Color.White
    val lowStockLabelColor =
        if (isDarkTheme) Color.White.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.85f)
    val lowStockBadgeBg = if (isDarkTheme) Color.White.copy(alpha = 0.18f) else Color.White
    val lowStockBadgeText =
        if (isDarkTheme) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.error
    val lowStockBtnBg = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.White
    val lowStockBtnText =
        if (isDarkTheme) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.error
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Open item details") { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border =
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent stripe — signals low stock without drowning the card in red
            Box(
                modifier =
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            if (isLowStock) MaterialTheme.colorScheme.error else Color.Transparent,
                        ),
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
            ) {
                // Row 1: Name + category, quantity + low-stock indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            modifier = Modifier.autoMarquee(),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = item.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${formatQty(item.quantity)} ${item.unit}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color =
                                if (isLowStock) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                        if (isLowStock) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Low stock",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Buy | Sell | Margin — three-column layout, single scan line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (userRole != "staff") {
                        PriceColumn(
                            label = "Buy",
                            value = stringResource(id = R.string.inv_buy_prefix, item.buyPrice.toRupee()),
                            valueColor = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    PriceColumn(
                        label = "Sell",
                        value = stringResource(id = R.string.inv_sell_prefix, item.sellPrice.toRupee()),
                        valueColor = MaterialTheme.colorScheme.primary,
                        alignEnd = userRole == "staff",
                    )
                    if (userRole != "staff") {
                        val margin =
                            if (item.buyPrice > 0) {
                                ((item.sellPrice - item.buyPrice) / item.buyPrice * 100).toInt()
                            } else {
                                0
                            }
                        val marginStr = if (margin > 0) "+$margin%" else "$margin%"
                        val profitAbs = item.sellPrice - item.buyPrice
                        val marginColor =
                            if (margin >= 15) {
                                Emerald500
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }

                        PriceColumn(
                            label = "Margin",
                            value = "$marginStr (${profitAbs.toRupee()})",
                            valueColor = marginColor,
                            alignEnd = true,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.Button(
                    onClick = onRefillClick,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors =
                        androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor =
                                if (isLowStock) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                            contentColor =
                                if (isLowStock) {
                                    MaterialTheme.colorScheme.onError
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                        ),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLowStock) "Restock Now" else "Add Stock",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

// ── Filter Chip ───────────────────────────────────────────────────────────────

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPrimaryBg: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    val bgColor by
        animateColorAsState(
            targetValue =
                if (isSelected) {
                    if (onPrimaryBg) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                } else {
                    if (onPrimaryBg) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                },
            animationSpec = tween(200),
            label = "chip_color",
        )
    val textColor by
        animateColorAsState(
            targetValue =
                if (isSelected) {
                    if (onPrimaryBg) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                } else {
                    if (onPrimaryBg) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                },
            animationSpec = tween(200),
            label = "chip_text",
        )

    val bgModifier =
        if (isSelected && !onPrimaryBg) {
            Modifier.background(MaterialTheme.primaryGradient)
        } else {
            Modifier.background(bgColor)
        }

    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .then(bgModifier)
                .clickable(onClickLabel = "Action") { onClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = stringResource(R.string.ui_element_desc),
                    tint = textColor,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
        }
    }
}
