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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.storebook.inventoryapp.data.repository.Supplier
import com.storebook.inventoryapp.data.repository.Purchase
import com.storebook.inventoryapp.data.repository.PurchaseItemDetail
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.window.PopupProperties
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel
import com.storebook.inventoryapp.utils.toRupee
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

private const val PAGE_SIZE = 50

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun InventoryScreen(viewModel: StoreBookViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Filter state — rememberSaveable survives config changes ──────────────
    var searchQ by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var sortBy by rememberSaveable { mutableStateOf("Name") }

    // ── Infinite-scroll page state ────────────────────────────────────────────
    var displayedItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var hasMoreItems by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
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
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var qtyError by remember { mutableStateOf(false) }

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

    // ── Update displayedItems when first page arrives from VM ─────────────────
    LaunchedEffect(filteredItems) {
        displayedItems = filteredItems
        hasMoreItems = filteredItems.size >= PAGE_SIZE
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
        nameError = false
        priceError = false
        qtyError = false
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
        nameError = false
        priceError = false
        qtyError = false
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
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
                                modifier = Modifier.fillMaxWidth(),
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
                    val addedQty = addQtyInput.toDoubleOrNull() ?: 0.0
                    val finalBuyPrice = buyPriceInput.toDoubleOrNull() ?: refillItem.buyPrice
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
                        if (isLoadingItems) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                        focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
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
                            onClick = { selectedCategory = cat },
                            onPrimaryBg = true,
                        )
                    }
                }
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📦", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text =
                                    if (searchQ.isBlank() && selectedCategory == "All") {
                                        "No items yet.\nTap + to add your first item."
                                    } else {
                                        stringResource(id = R.string.search_no_results)
                                    },
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                            )
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

                            // Swipe-to-delete — triggers dialog or direct delete
                            val dismissState = rememberSwipeToDismissBoxState()

                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                    if (shouldSkipInventoryDeleteConfirm(context)) {
                                        performDelete(item)
                                    } else {
                                        pendingDeleteItem = item
                                    }
                                    // Snap back so it visually resets
                                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                }
                            }

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
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
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = nameError,
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
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
                                        priceError = false
                                    },
                                    label = { Text(stringResource(id = R.string.inv_buy_price_label)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    isError = priceError,
                                )
                            }
                            OutlinedTextField(
                                value = inputSellPrice,
                                onValueChange = {
                                    inputSellPrice = it
                                    priceError = false
                                },
                                label = { Text(stringResource(id = R.string.inv_sell_price_label)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = priceError,
                            )
                        }
                        if (priceError) {
                            Text(
                                stringResource(id = R.string.inv_err_prices),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                            )
                        }

                        // Category picker chips
                        Column {
                            Text(
                                stringResource(id = R.string.inv_category_label),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categoriesList, key = { it }) { c ->
                                    FilterChip(
                                        label = c,
                                        isSelected = inputCategory == c,
                                        onClick = { inputCategory = c },
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = inputThreshold,
                            onValueChange = { inputThreshold = it },
                            label = { Text(stringResource(id = R.string.inv_threshold_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        // Taxes & HSN
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = inputHsnCode,
                                onValueChange = { inputHsnCode = it },
                                label = { Text("HSN/SAC Code") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = inputTaxRate,
                                onValueChange = { inputTaxRate = it },
                                label = { Text("Tax Rate (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }

                        Button(
                            onClick = {
                                val name = inputName.trim()
                                val qty = inputQty.toDoubleOrNull()
                                val buy = inputBuyPrice.toDoubleOrNull()
                                val sell = inputSellPrice.toDoubleOrNull()
                                val threshold = inputThreshold.toDoubleOrNull() ?: 5.0
                                val hsn = inputHsnCode.trim().takeIf { it.isNotBlank() }
                                val tax = inputTaxRate.toDoubleOrNull() ?: 0.0

                                nameError = name.isBlank()
                                priceError = buy == null || sell == null
                                qtyError = qty == null || qty < 0.0
                                if (nameError || priceError || qtyError) return@Button

                                if (editingItem == null) {
                                    viewModel.addItem(
                                        name,
                                        qty!!,
                                        inputUnit,
                                        buy!!,
                                        sell!!,
                                        threshold,
                                        inputCategory,
                                        hsn,
                                        tax,
                                    )
                                } else {
                                    viewModel.updateItem(
                                        editingItem!!.id,
                                        name,
                                        qty!!,
                                        inputUnit,
                                        buy!!,
                                        sell!!,
                                        threshold,
                                        inputCategory,
                                        hsn,
                                        tax,
                                    )
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
                containerColor = if (isLowStock) Color(0xFFe47a77) else MaterialTheme.colorScheme.surface,
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
                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Buy Price and Sell Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    if (userRole != "staff") {
                        Text(
                            text = "Buy Price",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(id = R.string.inv_buy_prefix, item.buyPrice.toRupee()),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Sell Price",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(id = R.string.inv_sell_prefix, item.sellPrice.toRupee()),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
                    if (margin >= 15) {
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            if (onPrimaryBg) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
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
