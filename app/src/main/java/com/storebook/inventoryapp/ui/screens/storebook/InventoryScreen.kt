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
                Column {
                    Text(
                        text = "Current Stock: ${formatQty(refillItem.quantity)} ${refillItem.unit}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = addQtyInput,
                        onValueChange = { addQtyInput = it },
                        label = { Text("Add Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))

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
                }
            },
            confirmButton = {
                Button(onClick = {
                    val addedQty = addQtyInput.toDoubleOrNull() ?: 0.0
                    if (addedQty > 0) {
                        viewModel.updateItem(
                            id = refillItem.id,
                            name = refillItem.name,
                            quantity = refillItem.quantity + addedQty,
                            unit = refillItem.unit,
                            buyPrice = refillItem.buyPrice,
                            sellPrice = refillItem.sellPrice,
                            threshold = refillItem.lowStockThreshold,
                            category = refillItem.category,
                        )
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
                        .background(MaterialTheme.colorScheme.surface)
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
                        )
                        Text(
                            text = "${displayedItems.size}${if (hasMoreItems) "+" else ""} items",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Sort toggle chip
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
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
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
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
                        )
                    }
                    item {
                        FilterChip(
                            label = "⚠️ " + stringResource(id = R.string.inv_filter_low_stock),
                            isSelected = selectedCategory == "Low Stock",
                            onClick = { selectedCategory = "Low Stock" },
                        )
                    }
                    items(categoriesList, key = { it }) { cat ->
                        FilterChip(
                            label = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddSheet() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
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
                                                .background(Coral500),
                                        contentAlignment = Alignment.CenterEnd,
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(end = 20.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                stringResource(id = R.string.btn_delete),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                        }
                                    }
                                },
                            ) {
                                InventoryItemCard(
                                    item = item,
                                    isLowStock = isLowStock,
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
                                onValueChange = { inputQty = it },
                                label = { Text(stringResource(id = R.string.inv_qty_label)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
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
                                if (nameError || priceError || qty == null) return@Button

                                if (editingItem == null) {
                                    viewModel.addItem(
                                        name,
                                        qty,
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
                                        qty,
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
                containerColor = if (isLowStock) Coral100.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${formatQty(item.quantity)} ${item.unit}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (isLowStock) Coral500 else MaterialTheme.colorScheme.onSurface,
                    )

                    // Quick Refill Plus Icon
                    Box(
                        modifier =
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onRefillClick() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Refill",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        "${item.category} • " +
                            stringResource(
                                id = R.string.inv_buy_prefix,
                                item.buyPrice.toRupee(),
                            ),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )

                val margin =
                    if (item.buyPrice >
                        0
                    ) {
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
                val marginColor =
                    if (margin >=
                        15
                    ) {
                        Emerald500
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.inv_sell_prefix, item.sellPrice.toRupee()),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = marginStr,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = marginColor,
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
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label = "chip_color",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
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
