package com.storebook.inventoryapp.ui.screens.storebook

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.data.repository.Item
import com.storebook.inventoryapp.data.repository.ItemBatch
import com.storebook.inventoryapp.data.repository.Supplier
import com.storebook.inventoryapp.data.repository.Purchase
import com.storebook.inventoryapp.data.repository.PurchaseItemDetail
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.ui.window.PopupProperties
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel
import com.storebook.inventoryapp.utils.toRupee
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Close
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.HorizontalDivider

private const val PAGE_SIZE = 50

@OptIn(ExperimentalMaterial3Api::class)
object FutureSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val todayStart = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        return utcTimeMillis >= todayStart
    }
    override fun isSelectableYear(year: Int): Boolean {
        return year >= java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun InventoryScreen(viewModel: StoreBookViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Filter state — rememberSaveable survives config changes ──────────────
    var searchQ by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var sortBy by rememberSaveable { mutableStateOf("Name") }
    var showFilterSheet by remember { mutableStateOf(false) }

    // ── Infinite-scroll page state ────────────────────────────────────────────
    var displayedItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var filterMode by rememberSaveable { mutableStateOf("All") } // "All" or "NearExpiry"
    var hasMoreItems by remember { mutableStateOf(true) }
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
    var inputBatchNumber by remember { mutableStateOf("") }
    var inputExpiryDateMs by remember { mutableStateOf<Long?>(null) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    var showAdvancedOptions by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var qtyError by remember { mutableStateOf(false) }
    var buyPriceError by remember { mutableStateOf(false) }
    var sellPriceError by remember { mutableStateOf(false) }

    val focusRequesterName = remember { FocusRequester() }
    val focusRequesterQty = remember { FocusRequester() }
    val focusRequesterBuyPrice = remember { FocusRequester() }
    val focusRequesterSellPrice = remember { FocusRequester() }
    val focusRequesterThreshold = remember { FocusRequester() }
    val focusRequesterHsn = remember { FocusRequester() }
    val focusRequesterTax = remember { FocusRequester() }
    val focusRequesterBatch = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val categoriesList = listOf("Groceries", "Dairy", "Beverages", "Stationery", "Household", "Others")
    val unitsList = listOf("pcs", "kg", "g", "litre", "ml", "dozen", "box", "packet")

    // ── Initial load ──────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.loadFilteredItems(searchQ, selectedCategory, sortBy)
    }


    // ── Debounce search + category/sort changes → trigger DB query ────────────
    LaunchedEffect(searchQ, selectedCategory, sortBy) {
        snapshotFlow { Triple(searchQ, selectedCategory, sortBy) }
            .debounce(300L)
            .distinctUntilChanged()
            .collect { (q, cat, sort) ->
                hasMoreItems = true
                viewModel.loadFilteredItems(q, cat, sort)
            }
    }

    // ── Update displayedItems based on filter mode ────────────────────────────────
    LaunchedEffect(filterMode, filteredItems, nearExpiryItems) {
        displayedItems = if (filterMode == "NearExpiry") nearExpiryItems else filteredItems
        // NearExpiry view shows all items, no pagination
        hasMoreItems = filterMode != "NearExpiry" && displayedItems.size >= PAGE_SIZE
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
                sortBy = sortBy,
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
        inputHsnCode = item.hsnCode ?: ""
        inputTaxRate =
            if (item.taxRate > 0) item.taxRate.toString() else ""
        inputBatchNumber = ""
        inputExpiryDateMs = null
        showAdvancedOptions = item.hsnCode != null || item.taxRate > 0
        nameError = false
        qtyError = false
        buyPriceError = false
        sellPriceError = false
        priceError = false
        showSheet = true
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
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = refillExpiryDateMs ?: System.currentTimeMillis(),
                selectableDates = FutureSelectableDates
            )
            DatePickerDialog(
                onDismissRequest = { showRefillDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null && selected < System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
                            android.widget.Toast.makeText(context, "Expiry date cannot be in the past", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            refillExpiryDateMs = selected
                            showRefillDatePicker = false
                        }
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showRefillDatePicker = false }) { Text("Cancel") }
                }
            ) { DatePicker(state = datePickerState) }
        }

        val filteredSuppliers = remember(suppliers, supplierSearchText) {
            if (supplierSearchText.isBlank()) {
                suppliers
            } else {
                suppliers.filter { it.name.contains(supplierSearchText, ignoreCase = true) }
            }
        }

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Current Stock: ${formatQty(refillItem.quantity)} ${refillItem.unit}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = addQtyInput,
                        onValueChange = { addQtyInput = it },
                        label = { Text("Add Quantity") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            if (viewModel.userRole != "staff") focusRequesterBuyPrice.requestFocus() else focusRequesterSupplier.requestFocus()
                        }),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

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

                    if (viewModel.userRole != "staff") {
                        OutlinedTextField(
                            value = buyPriceInput,
                            onValueChange = { buyPriceInput = it },
                            label = { Text("Buy Price (Per ${refillItem.unit})") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusRequesterSupplier.requestFocus() }),
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterBuyPrice),
                            singleLine = true
                        )
                    }

                    // Supplier Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Supplier",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = if (selectedSupplier != null) selectedSupplier!!.name else supplierSearchText,
                                onValueChange = {
                                    selectedSupplier = null
                                    supplierSearchText = it
                                    showSupplierDropdown = true
                                },
                                placeholder = { Text("Search or type new supplier...") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterSupplier),
                                singleLine = true,
                                trailingIcon = {
                                    TextButton(onClick = { showSupplierDropdown = !showSupplierDropdown }) {
                                        Text("Select")
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = showSupplierDropdown && (filteredSuppliers.isNotEmpty() || supplierSearchText.isNotBlank()),
                                onDismissRequest = { showSupplierDropdown = false },
                                properties = PopupProperties(focusable = false),
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .heightIn(max = 200.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Cash Purchase / No Supplier") },
                                    onClick = {
                                        selectedSupplier = null
                                        supplierSearchText = ""
                                        showSupplierDropdown = false
                                    }
                                )

                                filteredSuppliers.forEach { supplier ->
                                    DropdownMenuItem(
                                        text = { Text(supplier.name) },
                                        onClick = {
                                            selectedSupplier = supplier
                                            supplierSearchText = supplier.name
                                            showSupplierDropdown = false
                                        }
                                    )
                                }

                                if (supplierSearchText.isNotBlank() && filteredSuppliers.none { it.name.equals(supplierSearchText, ignoreCase = true) }) {
                                    DropdownMenuItem(
                                        text = { Text("Create supplier: \"$supplierSearchText\"") },
                                        onClick = {
                                            viewModel.addSupplier(
                                                name = supplierSearchText,
                                                phone = null,
                                                gstin = null,
                                                address = null
                                            ) { newId ->
                                                selectedSupplier = Supplier(id = newId, name = supplierSearchText)
                                            }
                                            showSupplierDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val addedQty = addQtyInput.toDoubleOrNull()
                    val finalBuyPrice = if (viewModel.userRole == "staff") refillItem.buyPrice else buyPriceInput.toDoubleOrNull()

                    if (addedQty == null || addedQty <= 0.0) {
                        android.widget.Toast.makeText(context, "Please enter a valid positive quantity", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (finalBuyPrice == null || finalBuyPrice < 0.0) {
                        android.widget.Toast.makeText(context, "Please enter a valid buy price", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (addedQty > 0) {
                        val purchase = Purchase(
                            supplierId = selectedSupplier?.id ?: 0L,
                            supplierName = selectedSupplier?.name ?: "Cash / Anonymous",
                            totalAmount = addedQty * finalBuyPrice,
                            taxAmount = addedQty * finalBuyPrice * (refillItem.taxRate / 100.0),
                            type = "BILL",
                            timestamp = System.currentTimeMillis(),
                            notes = "Refill stock for ${refillItem.name}",
                            items = listOf(
                                PurchaseItemDetail(
                                    purchaseId = 0L,
                                    itemId = refillItem.id,
                                    itemName = refillItem.name,
                                    quantity = addedQty,
                                    unit = refillItem.unit,
                                    buyPrice = finalBuyPrice
                                )
                            )
                        )
                        viewModel.addPurchase(purchase) {
                            // Log a batch record if batch number or expiry is provided
                            if (refillBatchNumber.isNotBlank() || refillExpiryDateMs != null) {
                                viewModel.addItemBatch(
                                    ItemBatch(
                                        itemId = refillItem.id,
                                        batchNumber = refillBatchNumber.trim().takeIf { it.isNotBlank() },
                                        expiryDate = refillExpiryDateMs,
                                        quantity = addedQty,
                                        costPrice = finalBuyPrice,
                                        timestamp = System.currentTimeMillis(),
                                        notes = "Refill for ${refillItem.name}"
                                    )
                                )
                            }
                            android.widget.Toast.makeText(context, "Stock refilled & purchase logged!", android.widget.Toast.LENGTH_SHORT).show()
                        }
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

    // ── Main UI ──────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
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
                            text = "${displayedItems.size}${if (hasMoreItems) "+" else ""} items",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        )
                    }

                    // Sort toggle chip
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.onPrimary)
                                .clickable {
                                    sortBy =
                                        when (sortBy) {
                                            "Name" -> "Qty"
                                            "Qty" -> "Price"
                                            else -> "Name"
                                        }
                                }.padding(horizontal = 12.dp, vertical = 6.dp),
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar — debounced via LaunchedEffect above
                OutlinedTextField(
                    value = searchQ,
                    onValueChange = { searchQ = it },
                    placeholder = { Text(stringResource(id = R.string.inv_search_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            if (isLoadingItems) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selectedCategory != "All" || filterMode != "All") MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { showFilterSheet = true }
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filters",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
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

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp),
                ) {
                    item {
                        FilterChip(
                            label = stringResource(id = R.string.inv_filter_all),
                            isSelected = selectedCategory == "All",
                            onClick = { selectedCategory = "All" },
                            onPrimaryBg = true,
                        )
                    }
                    item {
                        FilterChip(
                            label = "⚠️ " + stringResource(id = R.string.inv_filter_low_stock),
                            isSelected = selectedCategory == "Low Stock",
                            onClick = { selectedCategory = "Low Stock" },
                            onPrimaryBg = true,
                        )
                    }
                    items(categoriesList, key = { it }) { cat ->
                        FilterChip(
                            label = cat,
                            isSelected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                filterMode = "All"
                            },
                            onPrimaryBg = true,
                        )
                    }
                    item {
                        FilterChip(
                            label = "🕒 Expiring ≤30d${if (nearExpiryItems.isNotEmpty()) " (${nearExpiryItems.size})" else ""}",
                            isSelected = filterMode == "NearExpiry",
                            onClick = {
                                filterMode = if (filterMode == "NearExpiry") "All" else "NearExpiry"
                            },
                            onPrimaryBg = true,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddSheet() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(26.dp))
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("📦✨", fontSize = 72.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQ.isBlank() && selectedCategory == "All" && filterMode == "All") {
                                    "No stock yet?\nYour first item is just a tap away!"
                                } else {
                                    stringResource(id = R.string.search_no_results)
                                },
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp
                            )
                            if (searchQ.isBlank() && selectedCategory == "All" && filterMode == "All") {
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Add here ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "↘️",
                                        fontSize = 24.sp
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
                            contentType = { "inventory_item" }, // stable content type for Compose recycling
                        ) { item ->
                            val isLowStock = item.quantity <= item.lowStockThreshold

                            // Both swipe directions: right=quick restock options, left=delete
                            val dismissState = rememberSwipeToDismissBoxState()

                            LaunchedEffect(dismissState.currentValue) {
                                when (dismissState.currentValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        // Do nothing in LaunchedEffect to let the swipe box stay open
                                        // so the user can interact with the presets in backgroundContent
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
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(MaterialTheme.colorScheme.error),
                                            contentAlignment = Alignment.CenterEnd,
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Add, contentDescription = "Restock", tint = MaterialTheme.colorScheme.onPrimary)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Restock", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }

                                                // Presets
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    val presets = listOf(5, 10, 50)
                                                    presets.forEach { preset ->
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                                                .clickable {
                                                                    scope.launch {
                                                                        val purchase = Purchase(
                                                                            supplierId = 0L,
                                                                            supplierName = viewModel.lastRestockSupplierName.ifBlank { "Cash / Anonymous" },
                                                                            totalAmount = item.buyPrice * preset,
                                                                            taxAmount = (item.buyPrice * preset) * (item.taxRate / 100.0),
                                                                            type = "BILL",
                                                                            timestamp = System.currentTimeMillis(),
                                                                            notes = "Quick +$preset restock",
                                                                            items = listOf(
                                                                                PurchaseItemDetail(
                                                                                    purchaseId = 0L,
                                                                                    itemId = item.id,
                                                                                    itemName = item.name,
                                                                                    quantity = preset.toDouble(),
                                                                                    unit = item.unit,
                                                                                    buyPrice = item.buyPrice
                                                                                )
                                                                            )
                                                                        )
                                                                        viewModel.addPurchase(purchase) {}
                                                                        android.widget.Toast.makeText(context, "+$preset ${item.name} restocked", android.widget.Toast.LENGTH_SHORT).show()
                                                                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                                                    }
                                                                }
                                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Text("+$preset", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                                        }
                                                    }

                                                    // Custom button
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                                            .clickable {
                                                                quickRefillItem = item
                                                                scope.launch {
                                                                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                                                }
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Text("Custom", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }

                                                    // Cancel button
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                                                            .clickable {
                                                                scope.launch {
                                                                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                                                }
                                                            }
                                                            .padding(6.dp),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
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
                                                    .background(MaterialTheme.colorScheme.error),
                                            contentAlignment = Alignment.CenterEnd,
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(end = 20.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.onError,
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
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        stringResource(id = R.string.inv_loading_more),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }

                        // End of list indicator
                        if (!hasMoreItems && displayedItems.size > PAGE_SIZE) {
                            item(contentType = "end_of_list") {
                                Text(
                                    text = stringResource(id = R.string.inv_all_items_loaded, displayedItems.size),
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                )
                            }
                        }
                    }
                }
            }

            // ── Premium Filter Bottom Sheet ──────────────────────────────────────────
            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterSheet = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Filters & Categories",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            "Quick Filters",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                label = stringResource(id = R.string.inv_filter_all),
                                isSelected = selectedCategory == "All" && filterMode == "All",
                                onClick = {
                                    selectedCategory = "All"
                                    filterMode = "All"
                                    showFilterSheet = false
                                }
                            )
                            FilterChip(
                                label = "⚠️ " + stringResource(id = R.string.inv_filter_low_stock),
                                isSelected = selectedCategory == "Low Stock",
                                onClick = {
                                    selectedCategory = "Low Stock"
                                    showFilterSheet = false
                                }
                            )
                            FilterChip(
                                label = "🕒 Expiring ≤30d${if (nearExpiryItems.isNotEmpty()) " (${nearExpiryItems.size})" else ""}",
                                isSelected = filterMode == "NearExpiry",
                                onClick = {
                                    filterMode = if (filterMode == "NearExpiry") "All" else "NearExpiry"
                                    showFilterSheet = false
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            "All Categories",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoriesList.forEach { cat ->
                                FilterChip(
                                    label = cat,
                                    isSelected = selectedCategory == cat,
                                    onClick = {
                                        selectedCategory = cat
                                        filterMode = "All"
                                        showFilterSheet = false // Auto-close on selection
                                    }
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
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        LaunchedEffect(Unit) {
                            focusRequesterName.requestFocus()
                        }

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

                        OutlinedTextField(
                            value = inputName,
                            onValueChange = {
                                inputName = it
                                nameError = false
                            },
                            label = { Text(stringResource(id = R.string.inv_name_label)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequesterName),
                            singleLine = true,
                            isError = nameError,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusRequesterQty.requestFocus() }
                            ),
                            supportingText =
                                if (nameError) {
                                    { Text(stringResource(id = R.string.inv_err_empty_name)) }
                                } else {
                                    null
                                },
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = inputQty,
                                onValueChange = {
                                    inputQty = it
                                    qtyError = false
                                },
                                label = { Text(stringResource(id = R.string.inv_qty_label)) },
                                suffix = { Text(inputUnit) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        if (viewModel.userRole != "staff") {
                                            focusRequesterBuyPrice.requestFocus()
                                        } else {
                                            focusRequesterSellPrice.requestFocus()
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequesterQty),
                                singleLine = true,
                                isError = qtyError,
                                supportingText = if (qtyError) {
                                    { Text(stringResource(id = R.string.inv_err_qty)) }
                                } else null,
                            )
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
                                    FilterChip(label = u, isSelected = inputUnit == u, onClick = { inputUnit = u })
                                }
                            }
                        }

                        // Buy + Sell price
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (viewModel.userRole != "staff") {
                                OutlinedTextField(
                                    value = inputBuyPrice,
                                    onValueChange = {
                                        inputBuyPrice = it
                                        buyPriceError = false
                                    },
                                    label = { Text(stringResource(id = R.string.inv_buy_price_label)) },
                                    prefix = { Text("₹ ") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusRequesterSellPrice.requestFocus() }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequesterBuyPrice),
                                    singleLine = true,
                                    isError = buyPriceError,
                                    supportingText = if (buyPriceError) {
                                        { Text("Enter valid buy price") }
                                    } else null
                                )
                            }
                            OutlinedTextField(
                                value = inputSellPrice,
                                onValueChange = {
                                    inputSellPrice = it
                                    sellPriceError = false
                                },
                                label = { Text(stringResource(id = R.string.inv_sell_price_label)) },
                                prefix = { Text("₹ ") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusRequesterThreshold.requestFocus() }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequesterSellPrice),
                                singleLine = true,
                                isError = sellPriceError,
                                supportingText = if (sellPriceError) {
                                    { Text("Enter valid sell price") }
                                } else null
                            )
                        }

                        OutlinedTextField(
                            value = inputThreshold,
                            onValueChange = { inputThreshold = it },
                            label = { Text(stringResource(id = R.string.inv_threshold_label)) },
                            suffix = { Text(inputUnit) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    if (showAdvancedOptions) {
                                        focusRequesterHsn.requestFocus()
                                    } else {
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequesterThreshold),
                            singleLine = true,
                        )

                        // Advanced Options Accordion Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAdvancedOptions = !showAdvancedOptions }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (showAdvancedOptions) "Hide Advanced Options" else "Show Advanced Options (HSN, Tax, Batch)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (showAdvancedOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        val sheetDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

                        // Expiry Date Picker for Add/Edit sheet
                        if (showExpiryDatePicker) {
                            val dpState = rememberDatePickerState(
                                initialSelectedDateMillis = inputExpiryDateMs ?: System.currentTimeMillis(),
                                selectableDates = FutureSelectableDates
                            )
                            DatePickerDialog(
                                onDismissRequest = { showExpiryDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val selected = dpState.selectedDateMillis
                                        if (selected != null && selected < System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
                                            android.widget.Toast.makeText(context, "Expiry date cannot be in the past", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            inputExpiryDateMs = selected
                                            showExpiryDatePicker = false
                                        }
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showExpiryDatePicker = false }) { Text("Cancel") }
                                }
                            ) { DatePicker(state = dpState) }
                        }

                        AnimatedVisibility(visible = showAdvancedOptions) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // Taxes & HSN
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = inputHsnCode,
                                        onValueChange = { inputHsnCode = it },
                                        label = { Text("HSN/SAC Code") },
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusRequesterTax.requestFocus() }
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequesterHsn),
                                        singleLine = true,
                                    )
                                    OutlinedTextField(
                                        value = inputTaxRate,
                                        onValueChange = { inputTaxRate = it },
                                        label = { Text("Tax Rate (%)") },
                                        suffix = { Text("%") },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal,
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusRequesterBatch.requestFocus() }
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequesterTax),
                                        singleLine = true,
                                    )
                                }

                                Text(
                                    text = "Batch & Expiry Tracking (Optional)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                OutlinedTextField(
                                    value = inputBatchNumber,
                                    onValueChange = { inputBatchNumber = it },
                                    label = { Text("Batch / Lot Number") },
                                    placeholder = { Text("e.g. MFG-2024-B1") },
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequesterBatch),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = inputExpiryDateMs?.let { sheetDateFormatter.format(Date(it)) } ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Expiry Date") },
                                    placeholder = { Text("Tap calendar icon to set") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false,
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
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
                                    }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val name = inputName.trim()
                                val qty = inputQty.toDoubleOrNull()
                                val buy = if (viewModel.userRole == "staff") (inputSellPrice.toDoubleOrNull() ?: 0.0) else inputBuyPrice.toDoubleOrNull()
                                val sell = inputSellPrice.toDoubleOrNull()
                                val threshold = inputThreshold.toDoubleOrNull() ?: 5.0
                                val hsn = inputHsnCode.trim().takeIf { it.isNotBlank() }
                                val tax = inputTaxRate.toDoubleOrNull() ?: 0.0
                                val batchNum = inputBatchNumber.trim().takeIf { it.isNotBlank() }
                                val expiryMs = inputExpiryDateMs

                                nameError = name.isBlank()
                                buyPriceError = viewModel.userRole != "staff" && (buy == null || buy < 0.0)
                                sellPriceError = sell == null || sell <= 0.0
                                qtyError = qty == null || qty < 0.0

                                if (nameError || qtyError || buyPriceError || sellPriceError) return@Button
                                priceError = buy == null || sell == null || buy < 0.0 || sell <= 0.0
                                if (nameError || qtyError || priceError || buyPriceError || sellPriceError) return@Button

                                if (editingItem == null) {
                                    viewModel.addItem(
                                        name, qty!!, inputUnit, buy!!, sell!!, threshold,
                                        inputCategory, hsn, tax,
                                    ) { newItemId ->
                                        // Log a batch record if batch or expiry info was provided
                                        if (batchNum != null || expiryMs != null) {
                                            viewModel.addItemBatch(
                                                ItemBatch(
                                                    itemId = newItemId,
                                                    batchNumber = batchNum,
                                                    expiryDate = expiryMs,
                                                    quantity = qty!!,
                                                    costPrice = buy!!,
                                                    timestamp = System.currentTimeMillis(),
                                                    notes = "Initial stock batch"
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    viewModel.updateItem(
                                        editingItem!!.id, name, qty!!, inputUnit, buy!!, sell!!,
                                        threshold, inputCategory, hsn, tax,
                                    )
                                    // Also log a batch if expiry info was provided during edit
                                    if (batchNum != null || expiryMs != null) {
                                        viewModel.addItemBatch(
                                            ItemBatch(
                                                itemId = editingItem!!.id,
                                                batchNumber = batchNum,
                                                expiryDate = expiryMs,
                                                quantity = qty,
                                                costPrice = buy!!,
                                                timestamp = System.currentTimeMillis(),
                                                notes = "Batch logged on edit"
                                            )
                                        )
                                    }
                                }
                                showSheet = false
                                android.widget.Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.inv_save_success),
                                        android.widget.Toast.LENGTH_SHORT,
                                    ).show()
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

// ── Item Card ─────────────────────────────────────────────────────────────────

@Composable
fun InventoryItemCard(
    item: Item,
    isLowStock: Boolean,
    userRole: String,
    onClick: () -> Unit,
    onRefillClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Row 1: Name, Category badge, Quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
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
                Text(
                    text = "${formatQty(item.quantity)} ${item.unit}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isLowStock) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Buy Price and Sell Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val labelColor = if (isLowStock) MaterialTheme.colorScheme.onError.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                val sellValueColor = if (isLowStock) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.primary

                Column {
                    if (userRole != "staff") {
                        Text(
                            text = "Buy Price",
                            fontSize = 11.sp,
                            color = labelColor
                        )
                        Text(
                            text = stringResource(id = R.string.inv_buy_prefix, item.buyPrice.toRupee()),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isLowStock) MaterialTheme.colorScheme.onError else Color.Unspecified
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Sell Price",
                        fontSize = 11.sp,
                        color = labelColor
                    )
                    Text(
                        text = stringResource(id = R.string.inv_sell_prefix, item.sellPrice.toRupee()),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = sellValueColor
                    )
                }
            }

            if (userRole != "staff") {
                Spacer(modifier = Modifier.height(8.dp))
                // Row 3: Profit / Margin
                val margin =
                    if (item.buyPrice > 0) {
                        ((item.sellPrice - item.buyPrice) / item.buyPrice * 100).toInt()
                    } else {
                        0
                    }
                val marginStr =
                    if (margin > 0) {
                        "+$margin%"
                    } else if (margin < 0) {
                        "$margin%"
                    } else {
                        "0%"
                    }
                val profitAbs = (item.sellPrice - item.buyPrice)
                val marginColor =
                    if (isLowStock) {
                        MaterialTheme.colorScheme.onError
                    } else if (margin >= 15) {
                        Emerald500
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profit/ Margin",
                        fontSize = 11.sp,
                        color = if (isLowStock) MaterialTheme.colorScheme.onError.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$marginStr / ${profitAbs.toRupee()} Per ${item.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = marginColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Stock Button (Full width)
            Button(
                onClick = onRefillClick,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Add Stock",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
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
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (onPrimaryBg) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
        } else {
            if (onPrimaryBg) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(200),
        label = "chip_color",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (onPrimaryBg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
        } else {
            if (onPrimaryBg) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "chip_text",
    )
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}
