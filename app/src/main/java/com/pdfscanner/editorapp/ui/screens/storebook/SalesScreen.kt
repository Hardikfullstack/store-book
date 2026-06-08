package com.pdfscanner.editorapp.ui.screens.storebook

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pdfscanner.editorapp.R
import com.pdfscanner.editorapp.data.repository.CartItem
import com.pdfscanner.editorapp.ui.navigation.Routes
import com.pdfscanner.editorapp.ui.theme.Emerald500
import com.pdfscanner.editorapp.ui.theme.WhatsAppGreen
import com.pdfscanner.editorapp.ui.viewmodels.StoreBookViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(navController: NavController, viewModel: StoreBookViewModel) {
    val allItems by viewModel.allItems.collectAsState()
    val context = LocalContext.current

    var searchQ by remember { mutableStateOf("") }
    var showSuccessScreen by remember { mutableStateOf(false) }
    var generatedSaleId by remember { mutableStateOf(-1L) }
    var generatedTotalAmount by remember { mutableStateOf(0.0) }
    var lastCartSnap by remember { mutableStateOf<List<CartItem>>(emptyList()) }

    // Payment mode state
    var paymentMode by remember { mutableStateOf("Cash") }
    val paymentModes = listOf("Cash", "UPI", "Credit")

    // O(1) cart lookup via Map
    val cartMap by remember(viewModel.cartItems) {
        derivedStateOf { viewModel.cartItems.associateBy { it.item.id } }
    }

    val filteredItems by remember(allItems, searchQ) {
        derivedStateOf {
            if (searchQ.isBlank()) allItems
            else allItems.filter { it.name.contains(searchQ, ignoreCase = true) }
        }
    }

    val subtotal by remember(viewModel.cartItems) {
        derivedStateOf { viewModel.cartItems.sumOf { it.item.sellPrice * it.quantity } }
    }
    val grandTotal by remember(subtotal, viewModel.cartDiscount) {
        derivedStateOf { (subtotal - viewModel.cartDiscount).coerceAtLeast(0.0) }
    }

    if (showSuccessScreen) {
        SalesSuccessScreen(
            saleId = generatedSaleId,
            totalAmount = generatedTotalAmount,
            cartItems = lastCartSnap,
            discount = viewModel.cartDiscount,
            onBack = {
                showSuccessScreen = false
                navController.navigate(Routes.Dashboard) {
                    popUpTo(Routes.Dashboard) { inclusive = true }
                }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            viewModel.clearCart()
                            navController.popBackStack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.tab_sales),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (viewModel.cartItems.isNotEmpty()) {
                        Text(
                            text = "${viewModel.cartItems.size} items · ₹${String.format("%.0f", grandTotal)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar
                OutlinedTextField(
                    value = searchQ,
                    onValueChange = { searchQ = it },
                    placeholder = { Text(stringResource(id = R.string.sales_add_cart_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                // Items list — tap to add, stepper if in cart
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.search_no_results),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 4.dp,
                            // Extra bottom padding so last item isn't hidden behind sticky bar
                            bottom = if (viewModel.cartItems.isNotEmpty()) 180.dp else 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            val inCart = cartMap[item.id]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Tap anywhere = add to cart
                                        if (inCart == null) viewModel.addToCart(item, 1.0)
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (inCart != null)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                elevation = CardDefaults.cardElevation(if (inCart != null) 2.dp else 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Avatar initials
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (inCart != null) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = item.name.take(2).uppercase(),
                                                color = if (inCart != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                text = "₹${String.format("%.0f", item.sellPrice)} · ${item.quantity} ${item.unit} left",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    if (inCart != null) {
                                        // Inline stepper
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .clickable { viewModel.updateCartQty(item, inCart.quantity - 1) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("−", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            }
                                            Text(
                                                text = "${inCart.quantity.toInt()}",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                modifier = Modifier.width(24.dp),
                                                textAlign = TextAlign.Center
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .clickable { viewModel.updateCartQty(item, inCart.quantity + 1) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("+", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            }
                                        }
                                    } else {
                                        // "Tap to add" hint
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                "Add",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // STICKY CHECKOUT BAR — appears as soon as cart has items
            AnimatedVisibility(
                visible = viewModel.cartItems.isNotEmpty(),
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cart summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${viewModel.cartItems.size} items in cart",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "₹${String.format("%.0f", grandTotal)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Discount field - compact
                            OutlinedTextField(
                                value = if (viewModel.cartDiscount == 0.0) "" else viewModel.cartDiscount.toString(),
                                onValueChange = { viewModel.cartDiscount = it.toDoubleOrNull() ?: 0.0 },
                                label = { Text("Discount ₹", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(110.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Customer name field
                        OutlinedTextField(
                            value = viewModel.cartCustomerName,
                            onValueChange = { viewModel.cartCustomerName = it },
                            label = { Text(stringResource(id = R.string.sales_customer_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Payment mode chip row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Payment:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            paymentModes.forEach { mode ->
                                FilterChip(
                                    label = mode,
                                    isSelected = paymentMode == mode,
                                    onClick = { paymentMode = mode }
                                )
                            }
                        }

                        // Checkout button
                        Button(
                            onClick = {
                                lastCartSnap = viewModel.cartItems.toList()
                                viewModel.checkout { saleId, total ->
                                    generatedSaleId = saleId
                                    generatedTotalAmount = total
                                    showSuccessScreen = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                stringResource(id = R.string.sales_checkout),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SalesSuccessScreen(
    saleId: Long,
    totalAmount: Double,
    cartItems: List<CartItem>,
    discount: Double,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val invoiceText = remember(saleId, totalAmount, cartItems, discount) {
        val itemsStr = StringBuilder()
        cartItems.forEachIndexed { i, c ->
            itemsStr.append("${i + 1}. ${c.item.name} (${c.quantity.toInt()} ${c.item.unit}) - ₹${String.format("%.0f", c.item.sellPrice * c.quantity)}\n")
        }
        val subtotal = cartItems.sumOf { it.item.sellPrice * it.quantity }
        context.getString(
            R.string.sales_invoice_template,
            "#${saleId}",
            itemsStr.toString(),
            subtotal,
            discount,
            totalAmount
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Emerald500.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Emerald500,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.sales_success_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.sales_success_desc, totalAmount),
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Bill summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                cartItems.forEach { c ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${c.item.name} ×${c.quantity.toInt()}", fontSize = 13.sp)
                        Text("₹${String.format("%.0f", c.item.sellPrice * c.quantity)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (discount > 0) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        Text("-₹${String.format("%.0f", discount)}", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("₹${String.format("%.0f", totalAmount)}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?text=${URLEncoder.encode(invoiceText, "UTF-8")}")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(id = R.string.btn_share_whatsapp), fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(id = R.string.btn_back_home), fontWeight = FontWeight.Bold)
        }
    }
}
