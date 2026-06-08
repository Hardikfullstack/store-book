package com.pdfscanner.editorapp.ui.screens.storebook

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdfscanner.editorapp.R
import com.pdfscanner.editorapp.data.repository.Item
import com.pdfscanner.editorapp.ui.theme.Coral100
import com.pdfscanner.editorapp.ui.theme.Coral500
import com.pdfscanner.editorapp.ui.theme.Emerald500
import com.pdfscanner.editorapp.ui.viewmodels.StoreBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: StoreBookViewModel) {
    val allItems by viewModel.allItems.collectAsState()
    val context = LocalContext.current

    var searchQ by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Name") }

    // Add/Edit Bottom Sheet
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
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val categoriesList = listOf("Groceries", "Dairy", "Beverages", "Stationery", "Household", "Others")
    val unitsList = listOf("pcs", "kg", "g", "litre", "ml", "dozen", "box", "packet")

    // Derived filtered + sorted list for perf
    val filteredItems by remember(allItems, searchQ, selectedCategory, sortBy) {
        derivedStateOf {
            var items = allItems.filter { it.name.contains(searchQ, ignoreCase = true) }
            if (selectedCategory != "All") {
                items = if (selectedCategory == "Low Stock")
                    items.filter { it.quantity <= it.lowStockThreshold }
                else items.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            }
            when (sortBy) {
                "Qty" -> items.sortedBy { it.quantity }
                "Price" -> items.sortedBy { it.sellPrice }
                else -> items.sortedBy { it.name.lowercase() }
            }
        }
    }

    fun openAddSheet() {
        editingItem = null
        inputName = ""; inputQty = ""; inputUnit = "pcs"
        inputBuyPrice = ""; inputSellPrice = ""; inputThreshold = "5"
        inputCategory = "Groceries"; nameError = false; priceError = false
        showSheet = true
    }

    fun openEditSheet(item: Item) {
        editingItem = item
        inputName = item.name; inputQty = item.quantity.toString()
        inputUnit = item.unit; inputBuyPrice = item.buyPrice.toString()
        inputSellPrice = item.sellPrice.toString()
        inputThreshold = item.lowStockThreshold.toString()
        inputCategory = item.category; nameError = false; priceError = false
        showSheet = true
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.tab_inventory),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${allItems.size} items",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Sort toggle chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                sortBy = when (sortBy) {
                                    "Name" -> "Qty"; "Qty" -> "Price"; else -> "Name"
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Sort: ${when (sortBy) { "Qty" -> "Stock"; "Price" -> "Price"; else -> "Name" }}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQ,
                    onValueChange = { searchQ = it },
                    placeholder = { Text(stringResource(id = R.string.inv_search_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    item {
                        FilterChip(
                            label = stringResource(id = R.string.inv_filter_all),
                            isSelected = selectedCategory == "All",
                            onClick = { selectedCategory = "All" }
                        )
                    }
                    item {
                        FilterChip(
                            label = "⚠️ " + stringResource(id = R.string.inv_filter_low_stock),
                            isSelected = selectedCategory == "Low Stock",
                            onClick = { selectedCategory = "Low Stock" }
                        )
                    }
                    items(categoriesList) { cat ->
                        FilterChip(
                            label = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
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
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(26.dp))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQ.isBlank()) "No items yet.\nTap + to add your first item."
                            else stringResource(id = R.string.search_no_results),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val isLowStock = item.quantity <= item.lowStockThreshold

                        // Swipe-to-delete
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteItem(item.id)
                                    android.widget.Toast.makeText(
                                        context,
                                        context.getString(R.string.inv_delete_success),
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                // Red delete background revealed on swipe
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Coral500),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        modifier = Modifier.padding(end = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        ) {
                            InventoryItemCard(
                                item = item,
                                isLowStock = isLowStock,
                                onClick = { openEditSheet(item) }
                            )
                        }
                    }
                }
            }

            // Add / Edit Bottom Sheet
            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (editingItem == null) stringResource(id = R.string.inv_add_title)
                            else stringResource(id = R.string.inv_edit_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it; nameError = false },
                            label = { Text(stringResource(id = R.string.inv_name_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = nameError,
                            supportingText = if (nameError) {{ Text("Name cannot be empty") }} else null
                        )

                        // Quantity + Unit chip row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = inputQty,
                                onValueChange = { inputQty = it },
                                label = { Text(stringResource(id = R.string.inv_qty_label)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Unit picker chips
                        Column {
                            Text(stringResource(id = R.string.inv_unit_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(unitsList) { u ->
                                    FilterChip(
                                        label = u,
                                        isSelected = inputUnit == u,
                                        onClick = { inputUnit = u }
                                    )
                                }
                            }
                        }

                        // Buy + Sell price
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = inputBuyPrice,
                                onValueChange = { inputBuyPrice = it; priceError = false },
                                label = { Text(stringResource(id = R.string.inv_buy_price_label)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = priceError
                            )
                            OutlinedTextField(
                                value = inputSellPrice,
                                onValueChange = { inputSellPrice = it; priceError = false },
                                label = { Text(stringResource(id = R.string.inv_sell_price_label)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = priceError
                            )
                        }
                        if (priceError) {
                            Text("Please fill in buy and sell price.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }

                        // Category picker chips
                        Column {
                            Text(stringResource(id = R.string.inv_category_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categoriesList) { c ->
                                    FilterChip(
                                        label = c,
                                        isSelected = inputCategory == c,
                                        onClick = { inputCategory = c }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = inputThreshold,
                            onValueChange = { inputThreshold = it },
                            label = { Text(stringResource(id = R.string.inv_threshold_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Save button
                        Button(
                            onClick = {
                                val name = inputName.trim()
                                val qty = inputQty.toDoubleOrNull()
                                val buy = inputBuyPrice.toDoubleOrNull()
                                val sell = inputSellPrice.toDoubleOrNull()
                                val threshold = inputThreshold.toDoubleOrNull() ?: 5.0

                                nameError = name.isBlank()
                                priceError = buy == null || sell == null
                                if (nameError || priceError || qty == null) return@Button

                                if (editingItem == null) {
                                    viewModel.addItem(name, qty, inputUnit, buy!!, sell!!, threshold, inputCategory)
                                } else {
                                    viewModel.updateItem(editingItem!!.id, name, qty, inputUnit, buy!!, sell!!, threshold, inputCategory)
                                }
                                showSheet = false
                                android.widget.Toast.makeText(context, context.getString(R.string.inv_save_success), android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(stringResource(id = R.string.btn_save), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(item: Item, isLowStock: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) Coral100.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color accent
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isLowStock) Coral500
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(item.category, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sell ₹${String.format("%.0f", item.sellPrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Buy ₹${String.format("%.0f", item.buyPrice)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) Coral500 else MaterialTheme.colorScheme.onSurface
                    )
                    if (isLowStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Coral500)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Low Stock", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Profit margin indicator
            val margin = if (item.buyPrice > 0)
                ((item.sellPrice - item.buyPrice) / item.buyPrice * 100).toInt()
            else 0

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${margin}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = if (margin >= 15) Emerald500 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text = "margin",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label = "chip_color"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "chip_text"
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}
