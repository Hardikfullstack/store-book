@file:android.annotation.SuppressLint("LocalContextGetResourceValueCall")

package com.storebook.inventoryapp.ui.screens.storebook
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.data.billing.BillingEngine
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.ui.theme.*
import com.storebook.inventoryapp.ui.theme.PrimaryButton
import com.storebook.inventoryapp.ui.theme.primaryGradient
import com.storebook.inventoryapp.ui.viewmodel.SalesViewModel
import com.storebook.inventoryapp.utils.autoMarquee
import com.storebook.inventoryapp.utils.toRupee
import java.net.URLEncoder

// ─── Sales Screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    navController: NavController,
    viewModel: SalesViewModel,
) {
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val udhaarBalances by viewModel.udhaarBalances.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val scanner =
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

    var searchQ by remember { mutableStateOf("") }
    var showSuccessScreen by remember { mutableStateOf(false) }
    var generatedSaleId by remember { mutableStateOf(-1L) }
    var generatedTotalAmount by remember { mutableStateOf(0.0) }
    var generatedSaleType by remember { mutableStateOf("SALE") }
    var lastCartSnap by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var lastPaymentMode by remember { mutableStateOf("Cash") }

    // Payment modes — now includes "Udhaar"
    val paymentModes = listOf("Cash", "UPI", "Udhaar", "Estimate")

    // Customer name error state (mandatory for Udhaar)
    var customerNameError by remember { mutableStateOf(false) }

    // Autocomplete — show dropdown when typing customer name
    var showCustomerSuggestions by remember { mutableStateOf(false) }
    var showItemSuggestions by remember { mutableStateOf(false) }

    // Server-side filtered and limited customer suggestions
    val customerSuggestions by viewModel.customerSuggestions.collectAsStateWithLifecycle()

    // Custom qty dialog state
    var editingQtyItem by remember { mutableStateOf<Item?>(null) }
    var editingQtyText by remember { mutableStateOf(TextFieldValue("")) }
    val editingQtyFocus = remember { FocusRequester() }
    val qtySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showCheckoutSheet by remember { mutableStateOf(false) }
    val checkoutSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val focusRequesterDiscount = remember { FocusRequester() }
    val focusRequesterCustomer = remember { FocusRequester() }
    val focusRequesterGstin = remember { FocusRequester() }
    val focusRequesterAddress = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // O(1) cart lookup
    val cartMap by remember { derivedStateOf { viewModel.cartItems.associateBy { it.item.id } } }

    val filteredItems by remember {
        derivedStateOf {
            if (searchQ.isBlank()) {
                allItems
            } else {
                allItems.filter { it.name.contains(searchQ, ignoreCase = true) }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllData(false)
    }

    // Undo last sale countdown
    var undoSecondsLeft by remember { mutableStateOf(0) }
    LaunchedEffect(viewModel.lastSaleId, viewModel.lastSaleTime) {
        if (viewModel.lastSaleId != null) {
            val elapsed = (System.currentTimeMillis() - viewModel.lastSaleTime) / 1000
            undoSecondsLeft = (30 - elapsed).toInt().coerceAtLeast(0)
            while (undoSecondsLeft > 0) {
                kotlinx.coroutines.delay(1000)
                undoSecondsLeft--
            }
        }
    }

    val subtotal by remember {
        derivedStateOf {
            viewModel.cartItems.sumOf {
                it.item.sellPrice *
                    it.quantity
            }
        }
    }

    val taxSummary by remember {
        derivedStateOf {
            BillingEngine.calculateInvoiceTaxes(
                cartItems = viewModel.cartItems,
                totalDiscount = viewModel.cartDiscount,
                businessGstin = viewModel.businessGstin,
                customerGstin = viewModel.cartCustomerGstin,
            )
        }
    }

    val grandTotal by remember {
        derivedStateOf { taxSummary.grandTotal }
    }

    val totalTax by remember {
        derivedStateOf { taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst }
    }

    val isUdhaarMode = viewModel.cartPaymentMode == "Udhaar"

    if (showSuccessScreen) {
        androidx.activity.compose.BackHandler(enabled = showSuccessScreen) {
            viewModel.clearCart()
            showSuccessScreen = false
        }
        SalesSuccessScreen(
            saleId = generatedSaleId,
            totalAmount = generatedTotalAmount,
            cartItems = lastCartSnap,
            discount = viewModel.cartDiscount,
            paymentMode = lastPaymentMode,
            isEstimate = generatedSaleType == "ESTIMATE",
            onBack = {
                viewModel.clearCart()
                showSuccessScreen = false
            },
        )
        return
    }

    // ── Qty Edit Dialog ───────────────────────────────────────────────────────
    if (editingQtyItem != null) {
        val item = editingQtyItem!!
        val step = stepForUnit(item.unit)
        LaunchedEffect(Unit) {
            try {
                editingQtyFocus.requestFocus()
            } catch (_: Exception) {
            }
        }

        ModalBottomSheet(
            onDismissRequest = { editingQtyItem = null },
            sheetState = qtySheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        unitIcon(item.unit),
                        contentDescription = stringResource(R.string.ui_element_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier
                                .size(26.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text =
                                stringResource(
                                    id = R.string.sales_item_stock,
                                    formatQty(item.quantity),
                                    item.unit,
                                    item.sellPrice.toRupee(),
                                    item.unit,
                                ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.primaryGradient)
                                .clickable(onClickLabel = "Action") {
                                    val cur = editingQtyText.text.toDoubleOrNull() ?: step
                                    val next = (cur - step).coerceAtLeast(0.0)
                                    val s = formatQty(next)
                                    editingQtyText = TextFieldValue(s, TextRange(s.length))
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "−",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }

                    OutlinedTextField(
                        value = editingQtyText,
                        onValueChange = { editingQtyText = it },
                        modifier =
                            Modifier
                                .width(130.dp)
                                .padding(horizontal = 12.dp)
                                .focusRequester(editingQtyFocus),
                        textStyle =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            ),
                        suffix = {
                            Text(
                                item.unit,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    val qty = editingQtyText.text.toDoubleOrNull()
                                    if (qty != null && qty > 0.0) {
                                        if (cartMap[item.id] == null) {
                                            viewModel.addToCart(item, qty)
                                        } else {
                                            viewModel.updateCartQty(item, qty)
                                        }
                                    }
                                    editingQtyItem = null
                                },
                            ),
                        shape = RoundedCornerShape(14.dp),
                    )

                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.primaryGradient)
                                .clickable(onClickLabel = "Action") {
                                    val cur = editingQtyText.text.toDoubleOrNull() ?: 0.0
                                    val next = cur + step
                                    val s = formatQty(next)
                                    editingQtyText = TextFieldValue(s, TextRange(s.length))
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "+",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }

                // Quick Presets Row
                val presets =
                    when (item.unit.lowercase()) {
                        "kg", "ltr", "liter" -> listOf(0.5, 1.0, 2.0, 5.0)
                        "gm", "g" -> listOf(100.0, 250.0, 500.0, 1000.0)
                        else -> listOf(1.0, 2.0, 5.0, 10.0)
                    }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    presets.forEach { presetVal ->
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.primaryGradient)
                                    .clickable(onClickLabel = "Action") {
                                        val cur = editingQtyText.text.toDoubleOrNull() ?: 0.0
                                        val next = cur + presetVal
                                        val s = formatQty(next)
                                        editingQtyText = TextFieldValue(s, TextRange(s.length))
                                    }.padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "+${formatQty(presetVal)} ${item.unit}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }

                Text(
                    text =
                        when {
                            step == 1.0 -> "Type exact amount or use + / −"
                            step == 0.5 -> "Tap + / − for 0.5 ${item.unit} steps, or type exact (e.g. 2.5)"
                            else -> "Tap + / − for ${formatQty(step)} ${item.unit} steps, or type exact"
                        },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                val enteredQty = editingQtyText.text.toDoubleOrNull() ?: 0.0
                if (enteredQty > 0) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${formatQty(enteredQty)} ${item.unit} × ${item.sellPrice.toRupee()}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                        Text(
                            "= ${(enteredQty * item.sellPrice).toRupee()}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.Button(
                        onClick = { editingQtyItem = null },
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(id = R.string.btn_cancel), fontWeight = FontWeight.Bold)
                    }
                    PrimaryButton(
                        onClick = {
                            val qty = editingQtyText.text.toDoubleOrNull()
                            if (qty != null && qty > 0.0) {
                                if (cartMap[item.id] == null) {
                                    viewModel.addToCart(item, qty)
                                } else {
                                    viewModel.updateCartQty(item, qty)
                                }
                            }
                            editingQtyItem = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(id = R.string.sales_add_to_cart), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCheckoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            sheetState = checkoutSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            var showAdvancedBilling by remember { mutableStateOf(false) }
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Checkout Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = grandTotal.toRupee(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                OutlinedTextField(
                    value = if (viewModel.cartDiscount == 0.0) "" else viewModel.cartDiscount.toString(),
                    onValueChange = {
                        viewModel.cartDiscount =
                            (it.toDoubleOrNull() ?: 0.0).coerceAtMost(taxSummary.subTotal)
                    },
                    label = {
                        Text(
                            stringResource(id = R.string.sales_discount_rupee), fontSize = 11.sp,
                            modifier =
                                androidx.compose.ui.Modifier
                                    .autoMarquee(),
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusRequesterCustomer.requestFocus() },
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequesterDiscount),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                // ── Payment Mode Chips ──────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Payment Mode",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        paymentModes.forEach { mode ->
                            val isUdhaar = mode == "Udhaar"
                            val isSelected = viewModel.cartPaymentMode == mode
                            Box(
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            when {
                                                isSelected && isUdhaar -> MaterialTheme.colorScheme.error
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                        ).clickable(onClickLabel = "Action") {
                                            viewModel.cartPaymentMode = mode
                                            customerNameError = false
                                        }.padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icon =
                                        when (mode) {
                                            "Udhaar" -> androidx.compose.material.icons.Icons.Outlined.MenuBook
                                            "Cash" -> androidx.compose.material.icons.Icons.Outlined.Payments
                                            "UPI" -> androidx.compose.material.icons.Icons.Outlined.QrCodeScanner
                                            else -> null
                                        }
                                    val textColor =
                                        when {
                                            isSelected && isUdhaar -> MaterialTheme.colorScheme.onError
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    if (icon != null) {
                                        Icon(
                                            icon, contentDescription = stringResource(R.string.ui_element_desc),
                                            modifier =
                                                Modifier
                                                    .size(16.dp),
                                            tint = textColor,
                                        )
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = mode,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor,
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Udhaar info banner ──────────────────────────────────
                AnimatedVisibility(visible = isUdhaarMode) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = stringResource(R.string.ui_element_desc),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.sales_udhaar_warning),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                // ── Customer Name with Autocomplete ────────────────────
                androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val boxWidth = maxWidth
                    OutlinedTextField(
                        value = viewModel.cartCustomerName,
                        onValueChange = { name ->
                            viewModel.cartCustomerName = name
                            viewModel.updateCustomerSearch(name)
                            customerNameError = false
                            showCustomerSuggestions = true
                        },
                        label = {
                            Text(
                                text =
                                    if (isUdhaarMode) {
                                        "Customer Name *"
                                    } else {
                                        stringResource(id = R.string.sales_customer_label)
                                    },
                                modifier =
                                    androidx.compose.ui.Modifier
                                        .autoMarquee(),
                            )
                        },
                        placeholder = {
                            Text(
                                text = if (isUdhaarMode) "Required for Udhaar" else "Optional",
                                fontSize = 12.sp,
                                modifier =
                                    androidx.compose.ui.Modifier
                                        .autoMarquee(),
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = stringResource(R.string.ui_element_desc),
                                tint = if (customerNameError) Coral500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequesterCustomer)
                                .onFocusChanged { state ->
                                    if (state.isFocused) {
                                        showCustomerSuggestions = true
                                    }
                                },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = customerNameError,
                        keyboardOptions =
                            KeyboardOptions(
                                imeAction = if (showAdvancedBilling) ImeAction.Next else ImeAction.Done,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onNext = {
                                    if (showAdvancedBilling) {
                                        focusRequesterGstin.requestFocus()
                                    }
                                },
                                onDone = {
                                    focusManager.clearFocus()
                                },
                            ),
                        supportingText = {
                            Text(
                                text = if (customerNameError) "Customer name is required for Udhaar" else " ",
                                color =
                                    if (customerNameError) {
                                        Coral500
                                    } else {
                                        androidx.compose.ui.graphics.Color.Transparent
                                    },
                                fontSize = 11.sp,
                            )
                        },
                    )

                    // ── Autocomplete dropdown ──
                    com.storebook.inventoryapp.ui.components.StoreBookAutocompleteDropdown(
                        modifier = Modifier.width(boxWidth),
                        expanded = showCustomerSuggestions,
                        onDismissRequest = { showCustomerSuggestions = false },
                        suggestions = customerSuggestions,
                        itemText = { it },
                        onSuggestionSelected = { name ->
                            viewModel.selectCustomer(name)
                            showCustomerSuggestions = false
                            customerNameError = false
                        },
                        avatarColor =
                            if (isUdhaarMode) Coral500.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary,
                        avatarTextColor = if (isUdhaarMode) Coral500 else MaterialTheme.colorScheme.onPrimary,
                        additionalContent = { name ->
                            if (isUdhaarMode) {
                                val bal = udhaarBalances.find { it.customerName == name }
                                if (bal != null && bal.netBalance > 0) {
                                    Text(
                                        text =
                                            stringResource(
                                                id = R.string.sales_current_due,
                                                bal.netBalance.toRupee(),
                                            ),
                                        fontSize = 10.sp,
                                        color = Coral500,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        },
                    )
                }

                Text(
                    text =
                        if (showAdvancedBilling) {
                            "Hide Additional Billing Details"
                        } else {
                            "Add GSTIN & Address (Optional)"
                        },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClickLabel = "Action") { showAdvancedBilling = !showAdvancedBilling }
                            .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                )

                AnimatedVisibility(visible = showAdvancedBilling) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // ── Customer GSTIN ──────────────────────────────────────────
                        OutlinedTextField(
                            value = viewModel.cartCustomerGstin,
                            onValueChange = { viewModel.cartCustomerGstin = it },
                            label = {
                                Text(
                                    "Customer GSTIN (Optional)",
                                    modifier =
                                        androidx.compose.ui.Modifier
                                            .autoMarquee(),
                                )
                            },
                            keyboardOptions =
                                KeyboardOptions(
                                    imeAction = ImeAction.Next,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onNext = { focusRequesterAddress.requestFocus() },
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequesterGstin),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )

                        // ── Customer Address ────────────────────────────────────────
                        OutlinedTextField(
                            value = viewModel.cartCustomerAddress,
                            onValueChange = { viewModel.cartCustomerAddress = it },
                            label = {
                                Text(
                                    "Customer Address (Optional)",
                                    modifier =
                                        androidx.compose.ui.Modifier
                                            .autoMarquee(),
                                )
                            },
                            keyboardOptions =
                                KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = { focusManager.clearFocus() },
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequesterAddress),
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }

                // ── Checkout Button ─────────────────────────────────────
                androidx.compose.material3.Button(
                    onClick = {
                        // Validate: Udhaar requires customer name
                        if (isUdhaarMode && viewModel.cartCustomerName.trim().isBlank()) {
                            customerNameError = true
                            return@Button
                        }
                        showCustomerSuggestions = false
                        lastPaymentMode = viewModel.cartPaymentMode
                        lastCartSnap = viewModel.cartItems.toList()
                        showCheckoutSheet = false
                        viewModel.checkout(paymentMode = viewModel.cartPaymentMode, type = "SALE") { saleId, total ->
                            if (saleId > 0) {
                                generatedSaleId = saleId
                                generatedTotalAmount = total
                                generatedSaleType = "SALE"
                                showSuccessScreen = true
                            } else {
                                android.widget.Toast
                                    .makeText(
                                        context,
                                        "Checkout failed — check stock and retry",
                                        android.widget.Toast.LENGTH_LONG,
                                    ).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = viewModel.cartPaymentMode != "Estimate",
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (isUdhaarMode) {
                                    Coral500
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                        ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon =
                            when (viewModel.cartPaymentMode) {
                                "Udhaar" -> androidx.compose.material.icons.Icons.Outlined.MenuBook
                                "UPI" -> androidx.compose.material.icons.Icons.Outlined.QrCodeScanner
                                else -> androidx.compose.material.icons.Icons.Outlined.Payments
                            }
                        Icon(
                            icon, contentDescription = stringResource(R.string.ui_element_desc),
                            modifier =
                                Modifier
                                    .size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text =
                                when (viewModel.cartPaymentMode) {
                                    "Udhaar" -> "Record as Udhaar"
                                    "UPI" -> "Confirm UPI Sale"
                                    else -> stringResource(id = R.string.sales_checkout)
                                },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedVisibility(visible = !isUdhaarMode) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            showCustomerSuggestions = false
                            lastPaymentMode = viewModel.cartPaymentMode
                            lastCartSnap = viewModel.cartItems.toList()
                            showCheckoutSheet = false
                            viewModel.checkout(paymentMode = viewModel.cartPaymentMode, type = "ESTIMATE") {
                                saleId,
                                total,
                                ->
                                if (saleId > 0) {
                                    generatedSaleId = saleId
                                    generatedTotalAmount = total
                                    generatedSaleType = "ESTIMATE"
                                    showSuccessScreen = true
                                } else {
                                    android.widget.Toast
                                        .makeText(
                                            context,
                                            "Checkout failed — check stock and retry",
                                            android.widget.Toast.LENGTH_LONG,
                                        ).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                androidx.compose.material.icons.Icons.Outlined.Description,
                                contentDescription = stringResource(R.string.ui_element_desc),
                                modifier =
                                    Modifier
                                        .size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Save as Estimate",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.primaryGradient)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                            .clickable(onClickLabel = "Action") {
                                customerNameError = false
                                navController.popBackStack()
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.tab_sales),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text =
                            if (viewModel.cartItems.isNotEmpty()) {
                                "${viewModel.cartItems.size} items · ${grandTotal.toRupee()}"
                            } else {
                                "Add items to create a new sale"
                            },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                    )
                }

                if (viewModel.cartItems.isNotEmpty()) {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.clearCart()
                            customerNameError = false
                        },
                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Text("Clear", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Undo banner
                AnimatedVisibility(
                    visible = viewModel.lastSaleId != null && undoSecondsLeft > 0,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = stringResource(R.string.ui_element_desc),
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "बिक्री रद्द करें? (${undoSecondsLeft}s)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            androidx.compose.material3.Button(
                                onClick = {
                                    viewModel.undoLastSale {
                                        android.widget.Toast
                                            .makeText(
                                                context,
                                                context.getString(R.string.toast_undo_success),
                                                android.widget.Toast.LENGTH_SHORT,
                                            ).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                    ),
                            ) {
                                Text(
                                    stringResource(id = R.string.btn_undo),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }

                // Search bar
                // Search bar with Autocomplete
                androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val boxWidth = maxWidth
                    OutlinedTextField(
                        value = searchQ,
                        onValueChange = {
                            searchQ = it
                            showItemSuggestions = true
                        },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.sales_add_cart_hint),
                                modifier =
                                    androidx.compose.ui.Modifier
                                        .autoMarquee(),
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .onFocusChanged { state ->
                                    if (state.isFocused) {
                                        showItemSuggestions = true
                                    }
                                },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.ui_element_desc))
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQ.isNotEmpty()) {
                                    IconButton(onClick = { searchQ = "" }) {
                                        Icon(
                                            Icons.Rounded.Cancel,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    scanner
                                        .startScan()
                                        .addOnSuccessListener { barcode ->
                                            val code = barcode.rawValue
                                            if (!code.isNullOrBlank()) {
                                                val matched =
                                                    allItems.firstOrNull { it.barcode == code }
                                                        ?: allItems.firstOrNull { it.hsnCode == code }
                                                if (matched != null) {
                                                    if (stepForUnit(matched.unit) == 1.0) {
                                                        viewModel.addToCart(matched, 1.0)
                                                        android.widget.Toast
                                                            .makeText(
                                                                context,
                                                                "Added: ${matched.name}",
                                                                android.widget.Toast.LENGTH_SHORT,
                                                            ).show()
                                                    } else {
                                                        val step = stepForUnit(matched.unit)
                                                        val s = formatQty(step)
                                                        editingQtyText = TextFieldValue(s, TextRange(s.length))
                                                        editingQtyItem = matched
                                                    }
                                                } else {
                                                    android.widget.Toast
                                                        .makeText(
                                                            context,
                                                            "No item found with barcode/HSN: $code",
                                                            android.widget.Toast.LENGTH_LONG,
                                                        ).show()
                                                }
                                            }
                                        }.addOnFailureListener { e: Exception ->
                                            android.widget.Toast
                                                .makeText(
                                                    context,
                                                    "Scan failed: ${e.message}",
                                                    android.widget.Toast.LENGTH_SHORT,
                                                ).show()
                                        }
                                }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )

                    // Autocomplete Dropdown
                    com.storebook.inventoryapp.ui.components.StoreBookAutocompleteDropdown(
                        modifier = Modifier.width(boxWidth).padding(horizontal = 16.dp),
                        expanded = showItemSuggestions && searchQ.isNotBlank(),
                        onDismissRequest = { showItemSuggestions = false },
                        suggestions = filteredItems,
                        itemText = { it.name },
                        onSuggestionSelected = { item ->
                            val step = stepForUnit(item.unit)
                            if (step == 1.0) {
                                if (cartMap[item.id] == null) {
                                    viewModel.addToCart(item, 1.0)
                                } else {
                                    viewModel.updateCartQty(item, cartMap[item.id]!!.quantity + 1.0)
                                }
                                android.widget.Toast
                                    .makeText(context, "Added: ${item.name}", android.widget.Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                val s = formatQty(step)
                                editingQtyText = TextFieldValue(s, TextRange(s.length))
                                editingQtyItem = item
                            }
                            searchQ = ""
                            showItemSuggestions = false
                            focusManager.clearFocus()
                        },
                        avatarColor = MaterialTheme.colorScheme.secondaryContainer,
                        avatarTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        additionalContent = { item ->
                            Text(
                                text = "Stock: ${formatQty(item.quantity)} ${item.unit} · ${item.sellPrice.toRupee()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }

                // Items list
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.search_no_results),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        contentPadding =
                            PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = if (viewModel.cartItems.isNotEmpty()) 340.dp else 16.dp,
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            val inCart = cartMap[item.id]
                            val step = stepForUnit(item.unit)
                            val isPcs = step == 1.0

                            Card(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable(onClickLabel = "Action") {
                                            if (inCart == null) {
                                                if (isPcs) {
                                                    viewModel.addToCart(item, 1.0)
                                                } else {
                                                    val s = formatQty(step)
                                                    editingQtyText = TextFieldValue(s, TextRange(s.length))
                                                    editingQtyItem = item
                                                }
                                            }
                                        },
                                shape = RoundedCornerShape(20.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            if (inCart != null) {
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            },
                                    ),
                                border =
                                    BorderStroke(
                                        1.dp,
                                        if (inCart != null) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        },
                                    ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (inCart !=
                                                            null
                                                        ) {
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                        } else {
                                                            MaterialTheme.colorScheme.surfaceVariant
                                                        },
                                                    ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (!isPcs) {
                                                Icon(
                                                    unitIcon(item.unit),
                                                    contentDescription = stringResource(R.string.ui_element_desc),
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier =
                                                        Modifier
                                                            .size(20.dp),
                                                )
                                            } else {
                                                Text(
                                                    text = item.name.take(2).uppercase(),
                                                    color =
                                                        if (inCart !=
                                                            null
                                                        ) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        },
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp,
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                maxLines = 1,
                                                modifier = Modifier.autoMarquee(),
                                            )
                                            Text(
                                                text = "${item.sellPrice.toRupee()}/${item.unit}  •  Stock: ${formatQty(
                                                    item.quantity,
                                                )}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }

                                    if (inCart != null) {
                                        // Unified Pill Stepper
                                        Row(
                                            modifier =
                                                Modifier
                                                    .height(36.dp)
                                                    .clip(RoundedCornerShape(18.dp))
                                                    .background(MaterialTheme.primaryGradient),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxHeight()
                                                        .width(36.dp)
                                                        .clickable(onClickLabel = "Action") {
                                                            if (viewModel.isHapticFeedbackEnabled) {
                                                                haptic
                                                                    .performHapticFeedback(HapticFeedbackType.LongPress)
                                                            }
                                                            viewModel.changeCartQtyRelative(item, -step)
                                                        },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    "−",
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                )
                                            }

                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxHeight()
                                                        .clickable(onClickLabel = "Action") {
                                                            val curr = cartMap[item.id]
                                                            curr?.let {
                                                                val s = formatQty(it.quantity)
                                                                editingQtyText = TextFieldValue(s, TextRange(s.length))
                                                                editingQtyItem = item
                                                            }
                                                        }.padding(horizontal = 4.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text =
                                                        formatQty(cartMap[item.id]?.quantity ?: inCart.quantity) +
                                                            if (!isPcs) " ${item.unit}" else "",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                )
                                            }

                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxHeight()
                                                        .width(36.dp)
                                                        .clickable(onClickLabel = "Action") {
                                                            if (viewModel.isHapticFeedbackEnabled) {
                                                                haptic
                                                                    .performHapticFeedback(HapticFeedbackType.LongPress)
                                                            }
                                                            viewModel.changeCartQtyRelative(item, step)
                                                        },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    "+",
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .clip(RoundedCornerShape(18.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .clickable(onClickLabel = "Action") {
                                                        if (viewModel.isHapticFeedbackEnabled) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        }
                                                        viewModel.addToCart(item, step)
                                                    }.padding(horizontal = 14.dp, vertical = 8.dp),
                                        ) {
                                            Text(
                                                text =
                                                    if (isPcs) {
                                                        stringResource(id = R.string.btn_add)
                                                    } else {
                                                        stringResource(id = R.string.btn_add) +
                                                            " ${item.unit}"
                                                    },
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── STICKY CHECKOUT BAR ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = viewModel.cartItems.isNotEmpty(),
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Total + Quick Checkout row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${viewModel.cartItems.size} items in cart",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "${grandTotal.toRupee()}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                if (totalTax > 0.0) {
                                    Text(
                                        "(incl. ${totalTax.toRupee()} tax)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }

                            // Optional Add Details
                            TextButton(onClick = { showCheckoutSheet = true }) {
                                Text("+ Add Details", fontSize = 13.sp)
                            }
                        }

                        // Compact Customer Selection
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = viewModel.cartCustomerName,
                                onValueChange = { name ->
                                    viewModel.cartCustomerName = name
                                    viewModel.updateCustomerSearch(name)
                                    customerNameError = false
                                    showCustomerSuggestions = true
                                },
                                placeholder = {
                                    Text(
                                        text = "Customer Name (Optional)",
                                        fontSize = 13.sp,
                                        modifier =
                                            androidx.compose.ui.Modifier
                                                .autoMarquee(),
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = stringResource(R.string.ui_element_desc),
                                        tint =
                                            if (customerNameError) {
                                                Coral500
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { state ->
                                            if (state.isFocused) {
                                                showCustomerSuggestions = true
                                            }
                                        },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                isError = customerNameError,
                            )

                            // ── Autocomplete dropdown ──
                            DropdownMenu(
                                expanded = showCustomerSuggestions && customerSuggestions.isNotEmpty(),
                                onDismissRequest = { showCustomerSuggestions = false },
                                properties = PopupProperties(focusable = false),
                                modifier =
                                    Modifier
                                        .fillMaxWidth(0.9f)
                                        .heightIn(max = 240.dp)
                                        .background(MaterialTheme.colorScheme.surface),
                            ) {
                                customerSuggestions.forEachIndexed { index, name ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            ) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .size(30.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.primaryGradient),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        name.firstOrNull()?.uppercase() ?: "?",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                    )
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = name,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                    )
                                                    val bal = udhaarBalances.find { it.customerName == name }
                                                    if (bal != null && bal.netBalance > 0) {
                                                        Text(
                                                            text =
                                                                stringResource(
                                                                    id = R.string.sales_current_due,
                                                                    bal.netBalance.toRupee(),
                                                                ),
                                                            fontSize = 10.sp,
                                                            color = Coral500,
                                                            fontWeight = FontWeight.Bold,
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectCustomer(name)
                                            showCustomerSuggestions = false
                                            customerNameError = false
                                        },
                                    )
                                    if (index < customerSuggestions.lastIndex) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    }
                                }
                            }
                        }

                        // Payment Modes
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            paymentModes.forEach { mode ->
                                val isSelected = viewModel.cartPaymentMode == mode
                                Box(
                                    modifier =
                                        Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                when {
                                                    isSelected && mode == "Udhaar" -> Coral500
                                                    isSelected && mode == "Estimate" ->
                                                        androidx.compose.ui.graphics
                                                            .Color(0xFF607D8B)
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                },
                                            ).clickable(onClickLabel = "Action") {
                                                viewModel.cartPaymentMode = mode
                                                if (mode == "Udhaar") {
                                                    showCheckoutSheet = true
                                                }
                                            }.padding(horizontal = 16.dp, vertical = 10.dp),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val icon =
                                            when (mode) {
                                                "Udhaar" -> androidx.compose.material.icons.Icons.Outlined.MenuBook
                                                "Cash" -> androidx.compose.material.icons.Icons.Outlined.Payments
                                                "UPI" -> androidx.compose.material.icons.Icons.Outlined.QrCodeScanner
                                                "Estimate" -> androidx.compose.material.icons.Icons.Outlined.Description
                                                else -> null
                                            }
                                        val textColor =
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        if (icon != null) {
                                            Icon(
                                                icon, contentDescription = stringResource(R.string.ui_element_desc),
                                                modifier =
                                                    Modifier
                                                        .size(16.dp),
                                                tint = textColor,
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = mode,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor,
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Charge Button
                        androidx.compose.material3.Button(
                            onClick = {
                                if (viewModel.cartPaymentMode == "Udhaar" &&
                                    viewModel.cartCustomerName.trim().isBlank()
                                ) {
                                    showCheckoutSheet = true
                                } else {
                                    lastPaymentMode = viewModel.cartPaymentMode
                                    lastCartSnap = viewModel.cartItems.toList()
                                    val type = if (viewModel.cartPaymentMode == "Estimate") "ESTIMATE" else "SALE"
                                    viewModel.checkout(
                                        paymentMode =
                                            if (type ==
                                                "ESTIMATE"
                                            ) {
                                                "Cash"
                                            } else {
                                                viewModel.cartPaymentMode
                                            },
                                        type = type,
                                    ) { saleId, total ->
                                        if (saleId > 0) {
                                            generatedSaleId = saleId
                                            generatedTotalAmount = total
                                            generatedSaleType = type
                                            showSuccessScreen = true
                                        } else {
                                            android.widget.Toast
                                                .makeText(
                                                    context,
                                                    "Checkout failed — check stock and retry",
                                                    android.widget.Toast.LENGTH_LONG,
                                                ).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        when (viewModel.cartPaymentMode) {
                                            "Udhaar" -> Coral500
                                            "Estimate" ->
                                                androidx.compose.ui.graphics
                                                    .Color(0xFF607D8B)
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                ),
                        ) {
                            Text(
                                text =
                                    if (viewModel.cartPaymentMode ==
                                        "Estimate"
                                    ) {
                                        "Save Estimate"
                                    } else {
                                        "Charge ${grandTotal.toRupee()}"
                                    },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Success Screen ───────────────────────────────────────────────────────────

@Composable
fun SalesSuccessScreen(
    saleId: Long,
    totalAmount: Double,
    cartItems: List<CartItem>,
    discount: Double,
    paymentMode: String = "Cash",
    isEstimate: Boolean = false,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    // Removed auto-dismiss so the user can read the invoice
    val invoiceText =
        remember(saleId, totalAmount, cartItems, discount) {
            val itemsStr = StringBuilder()
            cartItems.forEachIndexed { i, c ->
                itemsStr.append(
                    "${i + 1}. ${c.item.name} (${formatQty(c.quantity)} ${c.item.unit}) " +
                        "@ ${c.item.sellPrice.toRupee()}/${c.item.unit} " +
                        "= ${(c.item.sellPrice * c.quantity).toRupee()}\n",
                )
            }
            val subtotal = cartItems.sumOf { it.item.sellPrice * it.quantity }
            val taxAmount = totalAmount - (subtotal - discount)
            val taxLine = if (taxAmount > 0.0) "Tax: ₹${String.format("%.2f", taxAmount)}\n" else ""

            context.getString(
                R.string.sales_invoice_template,
                "#$saleId",
                itemsStr.toString() + taxLine,
                subtotal,
                discount,
                totalAmount,
            )
        }

    val isUdhaar = paymentMode == "Udhaar"

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUdhaar) {
                            Coral500.copy(alpha = 0.12f)
                        } else {
                            Emerald500.copy(alpha = 0.12f)
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.ui_element_desc),
                tint = if (isUdhaar) Coral500 else Emerald500,
                modifier = Modifier.size(64.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text =
                if (isEstimate) {
                    "Estimate Saved!"
                } else if (isUdhaar) {
                    "Udhaar दर्ज हुई!"
                } else {
                    stringResource(id = R.string.sales_success_title)
                },
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text =
                if (isEstimate) {
                    "Quotation #$saleId saved. Stock was not deducted."
                } else if (isUdhaar) {
                    "${totalAmount.toRupee()} — उधार खाते में जोड़ दिया गया।"
                } else {
                    stringResource(id = R.string.sales_success_desc, totalAmount)
                },
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                ),
        )

        // Payment mode badge
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isUdhaar) {
                            Coral500.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    ).padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Text(
                text =
                    when {
                        isEstimate || paymentMode == "Estimate" -> "Estimation Stored"
                        paymentMode == "Udhaar" -> "Recorded in Udhaar Ledger"
                        paymentMode == "UPI" -> "UPI Payment"
                        else -> "Cash Payment"
                    },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUdhaar) Coral500 else MaterialTheme.colorScheme.onPrimary,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bill summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                cartItems.forEach { c ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "${c.item.name}  ×  ${formatQty(c.quantity)} ${c.item.unit}",
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${(c.item.sellPrice * c.quantity).toRupee()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (discount > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            stringResource(id = R.string.sales_discount_short),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                        )
                        Text("-${discount.toRupee()}", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
                val subtotal = cartItems.sumOf { it.item.sellPrice * it.quantity }
                val taxAmount = totalAmount - (subtotal - discount)
                if (taxAmount > 0.0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Taxes",
                            fontSize = 13.sp,
                        )
                        Text("+${taxAmount.toRupee()}", fontSize = 13.sp)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(id = R.string.sales_total_short),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                    Text(
                        "${totalAmount.toRupee()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (isUdhaar) Coral500 else MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        androidx.compose.material3.Button(
            onClick = {
                val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        data =
                            Uri.parse("https://api.whatsapp.com/send?text=${URLEncoder.encode(invoiceText, "UTF-8")}")
                    }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                stringResource(id = R.string.btn_share_whatsapp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        androidx.compose.material3.Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(stringResource(id = R.string.btn_close), fontWeight = FontWeight.Bold)
        }
    }
}
