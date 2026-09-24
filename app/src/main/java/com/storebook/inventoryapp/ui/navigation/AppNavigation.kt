package com.storebook.inventoryapp.ui.navigation
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.storebook.inventoryapp.MainActivity
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.ui.screens.auth.AuthScreen
import com.storebook.inventoryapp.ui.screens.storebook.DashboardScreen
import com.storebook.inventoryapp.ui.screens.storebook.FilterChip
import com.storebook.inventoryapp.ui.screens.storebook.GSTReportScreen
import com.storebook.inventoryapp.ui.screens.storebook.InventoryScreen
import com.storebook.inventoryapp.ui.screens.storebook.MoreScreen
import com.storebook.inventoryapp.ui.screens.storebook.PriceDriftReportScreen
import com.storebook.inventoryapp.ui.screens.storebook.SalesScreen
import com.storebook.inventoryapp.ui.screens.storebook.SplashScreen
import com.storebook.inventoryapp.ui.screens.storebook.StockAuditScreen
import com.storebook.inventoryapp.ui.screens.storebook.SupplierLedgerScreen
import com.storebook.inventoryapp.ui.screens.storebook.UdhaarScreen
import com.storebook.inventoryapp.ui.screens.storebook.formatQty
import com.storebook.inventoryapp.ui.theme.Emerald500
import com.storebook.inventoryapp.ui.theme.Poppins
import com.storebook.inventoryapp.ui.theme.PrimaryButton
import com.storebook.inventoryapp.utils.autoMarquee
import com.storebook.inventoryapp.utils.toRupee
import kotlinx.coroutines.launch

data class BottomNavTab(
    val route: Any,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val labelRes: Int,
)

@Composable
private fun DirectionOption(
    modifier: Modifier = Modifier,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selected) {
                        color.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                            .copy(alpha = 0.4f)
                    },
                ).border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                ).clickable(onClickLabel = "Select $title") { onClick() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) color else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val navController = rememberNavController()

    val udhaarViewModel: com.storebook.inventoryapp.ui.viewmodel.UdhaarViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory =
                com.storebook.inventoryapp.ui.viewmodel
                    .AppViewModelFactory(context),
        )

    val inventoryViewModel: com.storebook.inventoryapp.ui.viewmodel.InventoryViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory =
                com.storebook.inventoryapp.ui.viewmodel
                    .AppViewModelFactory(context),
        )

    val salesViewModel: com.storebook.inventoryapp.ui.viewmodel.SalesViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory =
                com.storebook.inventoryapp.ui.viewmodel
                    .AppViewModelFactory(context),
        )

    val dashboardViewModel: com.storebook.inventoryapp.ui.viewmodel.DashboardViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory =
                com.storebook.inventoryapp.ui.viewmodel
                    .AppViewModelFactory(context),
        )

    val purchaseViewModel: com.storebook.inventoryapp.ui.viewmodel.PurchaseViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory =
                com.storebook.inventoryapp.ui.viewmodel
                    .AppViewModelFactory(context),
        )

    val supplierViewModel: com.storebook.inventoryapp.ui.viewmodel.SupplierViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory =
                com.storebook.inventoryapp.ui.viewmodel
                    .AppViewModelFactory(context),
        )

    val expenseViewModel: com.storebook.inventoryapp.ui.viewmodel.ExpenseViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory =
                com.storebook.inventoryapp.ui.viewmodel
                    .AppViewModelFactory(context),
        )

    val moreViewModel: com.storebook.inventoryapp.ui.viewmodel.MoreViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory =
                com.storebook.inventoryapp.ui.viewmodel
                    .AppViewModelFactory(context),
        )

    val stockAuditViewModel: com.storebook.inventoryapp.ui.viewmodel.StockAuditViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory =
                com.storebook.inventoryapp.ui.viewmodel
                    .AppViewModelFactory(context),
        )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Error messages handled in individual ViewModels now
    }

    /* FIX: Reload screen data whenever navigation targets that tab,
       so that changes made on other screens are reflected immediately. */
    androidx.compose.runtime.LaunchedEffect(currentRoute) {
        if (currentRoute == "com.storebook.inventoryapp.ui.navigation.Routes.Udhaar") {
            udhaarViewModel.loadUdhaar()
        }
        if (currentRoute == "com.storebook.inventoryapp.ui.navigation.Routes.Dashboard") {
            dashboardViewModel.loadAllData()
            dashboardViewModel.loadTodaySnapshot()
        }
    }

    // Check if onboarding is completed
    val sharedPref = remember { context.getSharedPreferences("storebook_prefs", Context.MODE_PRIVATE) }
    var onboardingCompleted by remember {
        mutableStateOf(sharedPref.getBoolean("onboarding_completed", false))
    }

    val tabs =
        remember {
            listOf(
                BottomNavTab(
                    Routes.Dashboard,
                    Icons.Outlined.Home,
                    Icons.Filled.Home,
                    R.string.tab_dashboard,
                ),
                BottomNavTab(
                    Routes.Inventory,
                    @Suppress("DEPRECATION") Icons.Outlined.List,
                    @Suppress("DEPRECATION") Icons.Filled.List,
                    R.string.tab_inventory,
                ),
                BottomNavTab(
                    Routes.Sales,
                    Icons.Outlined.ShoppingCart,
                    Icons.Filled.ShoppingCart,
                    R.string.tab_sales,
                ),
                BottomNavTab(
                    Routes.Udhaar,
                    Icons.Outlined.Book,
                    Icons.Filled.Book,
                    R.string.tab_udhaar,
                ),
                BottomNavTab(
                    Routes.More,
                    Icons.Outlined.Menu,
                    Icons.Filled.Menu,
                    R.string.tab_more,
                ),
            )
        }

    val showBottomBar =
        currentRoute in
            listOf(
                "com.storebook.inventoryapp.ui.navigation.Routes.Dashboard",
                "com.storebook.inventoryapp.ui.navigation.Routes.Inventory",
                "com.storebook.inventoryapp.ui.navigation.Routes.Udhaar",
                "com.storebook.inventoryapp.ui.navigation.Routes.More",
                "com.storebook.inventoryapp.ui.navigation.Routes.Sales",
            )

    var speedDialExpanded by remember { mutableStateOf(false) }
    // Quick Expense dialog state
    var showQuickExpense by remember { mutableStateOf(false) }
    var quickExpenseAmount by remember { mutableStateOf("") }
    var quickExpenseAmountError by remember { mutableStateOf(false) }
    var quickExpenseDescError by remember { mutableStateOf(false) }
    var isQuickExpenseSubmitting by remember { mutableStateOf(false) }

    var quickExpenseDesc by remember { mutableStateOf("") }

    val focusRequesterDesc = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Quick Sale state
    var showQuickSale by remember { mutableStateOf(false) }
    var quickSaleAmount by remember { mutableStateOf("") }
    var quickSaleCustomer by remember { mutableStateOf("") }
    var quickSaleAmountError by remember { mutableStateOf(false) }
    var isQuickSaleSubmitting by remember { mutableStateOf(false) }
    var quickSaleCustomerExpanded by remember { mutableStateOf(false) }

    // Quick Restock state
    var showQuickRestock by remember { mutableStateOf(false) }
    var quickRestockNameError by remember { mutableStateOf(false) }
    var quickRestockQtyError by remember { mutableStateOf(false) }
    var quickRestockPriceError by remember { mutableStateOf(false) }
    var isQuickRestockSubmitting by remember { mutableStateOf(false) }
    var quickRestockName by remember { mutableStateOf("") }
    var quickRestockQty by remember { mutableStateOf("") }
    var quickRestockPrice by remember { mutableStateOf("") }
    var quickRestockNameExpanded by remember { mutableStateOf(false) }
    var selectedRestockItemId by remember { mutableStateOf<Long?>(null) }

    // Quick Add Party state
    var showQuickParty by remember { mutableStateOf(false) }
    var quickPartyName by remember { mutableStateOf("") }
    var quickPartyAmount by remember { mutableStateOf("") }
    var quickPartyType by remember { mutableStateOf("CREDIT") }
    var quickPartyNameExpanded by remember { mutableStateOf(false) }
    var quickPartyNameError by remember { mutableStateOf(false) }
    var quickPartyAmountError by remember { mutableStateOf(false) }
    var isQuickPartySubmitting by remember { mutableStateOf(false) }

    val customerSuggestions by salesViewModel.customerSuggestions.collectAsState()
    val allItems by salesViewModel.allItems.collectAsState()

    // Quick Expense mini-dialog
    if (showQuickExpense) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!isQuickExpenseSubmitting) showQuickExpense = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Log Expense", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Amount — hero input, consistent with the Quick Cash Sale dialog
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Expense Amount",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = quickExpenseAmount,
                            onValueChange = {
                                quickExpenseAmount = it
                                quickExpenseAmountError = false
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
                            prefix = { Text("₹", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                            textStyle =
                                LocalTextStyle.current.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = quickExpenseAmountError,
                            keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                    imeAction = ImeAction.Next,
                                ),
                            keyboardActions = KeyboardActions(onNext = { focusRequesterDesc.requestFocus() }),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                ),
                            supportingText =
                                if (quickExpenseAmountError) {
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

                    // Common expense categories — fills description with one tap
                    val commonExpenses = listOf("Rent", "Electricity", "Staff Wages", "Transport", "Maintenance")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(commonExpenses, key = { it }) { label ->
                            FilterChip(
                                label = label,
                                isSelected = quickExpenseDesc == label,
                                onClick = {
                                    quickExpenseDesc = label
                                    quickExpenseDescError = false
                                },
                            )
                        }
                    }

                    androidx.compose.material3.OutlinedTextField(
                        value = quickExpenseDesc,
                        onValueChange = {
                            quickExpenseDesc = it
                            quickExpenseDescError = false
                        },
                        label = {
                            Text(
                                "Description",
                                modifier =
                                    androidx.compose.ui.Modifier
                                        .autoMarquee(),
                            )
                        },
                        placeholder = {
                            Text(
                                "e.g. Shop rent for September",
                                modifier =
                                    androidx.compose.ui.Modifier
                                        .autoMarquee(),
                            )
                        },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterDesc),
                        singleLine = true,
                        isError = quickExpenseDescError,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        keyboardOptions =
                            androidx.compose.foundation.text
                                .KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        supportingText =
                            if (quickExpenseDescError) {
                                { Text("Enter a description", fontSize = 11.sp) }
                            } else {
                                null
                            },
                    )
                }
            },
            confirmButton = {
                PrimaryButton(
                    enabled = !isQuickExpenseSubmitting,
                    onClick = {
                        val amt = quickExpenseAmount.toDoubleOrNull()
                        quickExpenseAmountError = amt == null || amt <= 0.0
                        quickExpenseDescError = quickExpenseDesc.isBlank()

                        if (quickExpenseAmountError || quickExpenseDescError) return@PrimaryButton

                        isQuickExpenseSubmitting = true
                        expenseViewModel.addExpense(
                            type = "OVERHEAD",
                            amount = amt!!,
                            notes = quickExpenseDesc.trim(),
                        )
                        isQuickExpenseSubmitting = false
                        quickExpenseAmount = ""
                        quickExpenseDesc = ""
                        showQuickExpense = false
                    },
                ) {
                    if (isQuickExpenseSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Log Expense")
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    enabled = !isQuickExpenseSubmitting,
                    onClick = { showQuickExpense = false },
                ) { Text("Cancel") }
            },
        )
    }

    if (showQuickSale) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!isQuickSaleSubmitting) showQuickSale = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Quick Cash Sale", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Amount — the hero input, large and centered like a calculator display
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Amount Received",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = quickSaleAmount,
                            onValueChange = {
                                quickSaleAmount = it
                                quickSaleAmountError = false
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
                            prefix = { Text("₹", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                            textStyle =
                                LocalTextStyle.current.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = quickSaleAmountError,
                            keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                ),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                ),
                            supportingText =
                                if (quickSaleAmountError) {
                                    {
                                        Text(
                                            "Enter a valid amount", fontSize = 11.sp, textAlign = TextAlign.Center,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth(),
                                        )
                                    }
                                } else {
                                    null
                                },
                        )
                    }

                    // Quick amount presets — common cash sale values
                    val presets = listOf(50, 100, 200, 500)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets, key = { it }) { preset ->
                            FilterChip(
                                label = "₹$preset",
                                isSelected = quickSaleAmount.toDoubleOrNull() == preset.toDouble(),
                                onClick = {
                                    quickSaleAmount = preset.toString()
                                    quickSaleAmountError = false
                                },
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Customer — optional, visually secondary
                    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val boxWidth = maxWidth
                        val filteredCustomers =
                            remember(quickSaleCustomer, customerSuggestions) {
                                if (quickSaleCustomer.isBlank()) {
                                    emptyList()
                                } else {
                                    customerSuggestions.filter {
                                        it.contains(quickSaleCustomer.trim(), ignoreCase = true)
                                    }
                                }
                            }
                        androidx.compose.material3.OutlinedTextField(
                            value = quickSaleCustomer,
                            onValueChange = {
                                quickSaleCustomer = it
                                quickSaleCustomerExpanded = it.isNotBlank()
                            },
                            label = {
                                Text(
                                    "Customer Name (Optional)",
                                    modifier =
                                        androidx.compose.ui.Modifier
                                            .autoMarquee(),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                        com.storebook.inventoryapp.ui.components.StoreBookAutocompleteDropdown(
                            modifier = Modifier.width(boxWidth),
                            expanded = quickSaleCustomerExpanded,
                            onDismissRequest = { quickSaleCustomerExpanded = false },
                            suggestions = filteredCustomers,
                            itemText = { it },
                            onSuggestionSelected = { name ->
                                quickSaleCustomer = name
                                quickSaleCustomerExpanded = false
                            },
                            avatarColor = MaterialTheme.colorScheme.primary,
                            avatarTextColor = MaterialTheme.colorScheme.onPrimary,
                            openAbove = true,
                        )
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    enabled = !isQuickSaleSubmitting,
                    onClick = {
                        val amt = quickSaleAmount.toDoubleOrNull()
                        quickSaleAmountError = amt == null || amt <= 0.0
                        if (quickSaleAmountError) return@PrimaryButton

                        isQuickSaleSubmitting = true
                        salesViewModel.clearCart()
                        val dummyItem =
                            com.storebook.inventoryapp.shared.domain.models.Item(
                                id = 0L,
                                name = "Quick Sale",
                                quantity = 0.0,
                                unit = "pcs",
                                buyPrice = 0.0,
                                sellPrice = amt!!,
                                lowStockThreshold = 0.0,
                                category = "General",
                            )
                        salesViewModel.addToCart(dummyItem, 1.0)
                        if (quickSaleCustomer.isNotBlank()) {
                            salesViewModel.cartCustomerName = quickSaleCustomer.trim()
                        }
                        salesViewModel.checkout(paymentMode = "Cash", type = "SALE") { _, _ ->
                            isQuickSaleSubmitting = false
                            quickSaleCustomer = ""
                            quickSaleAmount = ""
                            showQuickSale = false
                        }
                    },
                ) {
                    if (isQuickSaleSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Complete Sale")
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    enabled = !isQuickSaleSubmitting,
                    onClick = { showQuickSale = false },
                ) { Text("Cancel") }
            },
        )
    }

    if (showQuickRestock) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!isQuickRestockSubmitting) showQuickRestock = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Quick Add / Restock", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val boxWidth = maxWidth
                        androidx.compose.material3.OutlinedTextField(
                            value = quickRestockName,
                            onValueChange = {
                                quickRestockName = it
                                selectedRestockItemId = null
                                quickRestockNameExpanded = it.isNotBlank()
                                quickRestockNameError = false
                            },
                            label = {
                                Text(
                                    "Item Name",
                                    modifier =
                                        androidx.compose.ui.Modifier
                                            .autoMarquee(),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = quickRestockNameError,
                            leadingIcon =
                                if (selectedRestockItemId != null) {
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
                            supportingText =
                                if (quickRestockNameError) {
                                    { Text("Enter an item name", fontSize = 11.sp) }
                                } else {
                                    null
                                },
                        )
                        val filteredItems =
                            allItems
                                .filter { it.name.contains(quickRestockName, ignoreCase = true) }
                                .take(5)
                        com.storebook.inventoryapp.ui.components.StoreBookAutocompleteDropdown(
                            modifier = Modifier.width(boxWidth),
                            expanded = quickRestockNameExpanded,
                            onDismissRequest = { quickRestockNameExpanded = false },
                            suggestions = filteredItems,
                            itemText = { it.name },
                            onSuggestionSelected = { item ->
                                quickRestockName = item.name
                                selectedRestockItemId = item.id
                                quickRestockPrice = item.buyPrice.toString()
                                quickRestockNameExpanded = false
                            },
                            avatarColor = MaterialTheme.colorScheme.secondary,
                            avatarTextColor = MaterialTheme.colorScheme.onSecondary,
                            additionalContent = { item ->
                                Text(
                                    text = "Stock: ${formatQty(item.quantity)} ${item.unit} · ${item.sellPrice.toRupee()}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                    }

                    // Mode indicator — makes the ambiguous "what happens on save" explicit
                    val isRestockMode = selectedRestockItemId != null
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isRestockMode) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    },
                                ).padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (isRestockMode) Icons.Default.Add else Icons.Default.NewReleases,
                            contentDescription = null,
                            tint = if (isRestockMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text =
                                if (isRestockMode) {
                                    "Restocking existing item — quantity will be added to current stock"
                                } else {
                                    "New item will be created"
                                },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isRestockMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        androidx.compose.material3.OutlinedTextField(
                            value = quickRestockQty,
                            onValueChange = {
                                quickRestockQty = it
                                quickRestockQtyError = false
                            },
                            label = {
                                Text(
                                    if (isRestockMode) "Add Quantity" else "Stock Quantity",
                                    modifier =
                                        androidx.compose.ui.Modifier
                                            .autoMarquee(),
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = quickRestockQtyError,
                            keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                ),
                            supportingText =
                                if (quickRestockQtyError) {
                                    { Text("Required", fontSize = 11.sp) }
                                } else {
                                    null
                                },
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = quickRestockPrice,
                            onValueChange = {
                                quickRestockPrice = it
                                quickRestockPriceError = false
                            },
                            label = {
                                Text(
                                    if (isRestockMode) "Buy Price (₹/unit)" else "Cost Price (₹)",
                                    modifier =
                                        androidx.compose.ui.Modifier
                                            .autoMarquee(),
                                )
                            },
                            prefix = { Text("₹ ") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = quickRestockPriceError,
                            keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                ),
                            supportingText =
                                if (quickRestockPriceError) {
                                    { Text("Required", fontSize = 11.sp) }
                                } else {
                                    null
                                },
                        )
                    }

                    // Live preview so the ambiguous "what will happen" is fully transparent
                    val qtyVal = quickRestockQty.toDoubleOrNull()
                    val priceVal = quickRestockPrice.toDoubleOrNull()
                    if (qtyVal != null && qtyVal > 0 && priceVal != null && priceVal > 0) {
                        val selectedItem = allItems.find { it.id == selectedRestockItemId }
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (isRestockMode && selectedItem != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("New stock level", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${formatQty(selectedItem.quantity + qtyVal)} ${selectedItem.unit}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Sale price (auto, 20% markup)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        (priceVal * 1.2).toRupee(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Total cost", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${"%.2f".format(qtyVal * priceVal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    enabled = !isQuickRestockSubmitting,
                    onClick = {
                        val qty = quickRestockQty.toDoubleOrNull()
                        val cost = quickRestockPrice.toDoubleOrNull()

                        quickRestockNameError = quickRestockName.isBlank()
                        quickRestockQtyError = qty == null || qty <= 0.0
                        quickRestockPriceError = cost == null || cost <= 0.0

                        if (quickRestockNameError || quickRestockQtyError || quickRestockPriceError) {
                            return@PrimaryButton
                        }

                        val safeQty = qty!!
                        val safeCost = cost!!
                        isQuickRestockSubmitting = true

                        val currentId = selectedRestockItemId
                        if (currentId != null) {
                            scope.launch {
                                inventoryViewModel.restockItem(
                                    itemId = currentId,
                                    quantityToAdd = safeQty,
                                    costPrice = safeCost,
                                    supplierName = null,
                                    supplierPhone = null,
                                )
                                inventoryViewModel.loadFilteredItems()
                                isQuickRestockSubmitting = false
                                quickRestockName = ""
                                quickRestockQty = ""
                                quickRestockPrice = ""
                                selectedRestockItemId = null
                                showQuickRestock = false
                            }
                        } else {
                            scope.launch {
                                inventoryViewModel.addItem(
                                    name = quickRestockName.trim(),
                                    quantity = safeQty,
                                    unit = "pcs",
                                    buyPrice = safeCost,
                                    sellPrice = safeCost * 1.2,
                                    threshold = 5.0,
                                    category = "Uncategorized",
                                    hsnCode = null,
                                    taxRate = 0.0,
                                    onResult = {},
                                )
                                inventoryViewModel.loadFilteredItems()
                                isQuickRestockSubmitting = false
                                quickRestockName = ""
                                quickRestockQty = ""
                                quickRestockPrice = ""
                                selectedRestockItemId = null
                                showQuickRestock = false
                            }
                        }
                    },
                ) {
                    if (isQuickRestockSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    enabled = !isQuickRestockSubmitting,
                    onClick = { showQuickRestock = false },
                ) { Text("Cancel") }
            },
        )
    }

    if (showQuickParty) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!isQuickPartySubmitting) showQuickParty = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Log Party Balance", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Party name with autocomplete
                    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val boxWidth = maxWidth
                        val filteredParties =
                            remember(quickPartyName, customerSuggestions) {
                                if (quickPartyName.isBlank()) {
                                    emptyList()
                                } else {
                                    customerSuggestions.filter {
                                        it.contains(quickPartyName.trim(), ignoreCase = true)
                                    }
                                }
                            }
                        androidx.compose.material3.OutlinedTextField(
                            value = quickPartyName,
                            onValueChange = {
                                quickPartyName = it
                                quickPartyNameExpanded = it.isNotBlank()
                                quickPartyNameError = false
                            },
                            label = {
                                Text(
                                    "Party Name",
                                    modifier =
                                        androidx.compose.ui.Modifier
                                            .autoMarquee(),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = quickPartyNameError,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            supportingText =
                                if (quickPartyNameError) {
                                    { Text("Enter a party name", fontSize = 11.sp) }
                                } else {
                                    null
                                },
                        )
                        com.storebook.inventoryapp.ui.components.StoreBookAutocompleteDropdown(
                            modifier = Modifier.width(boxWidth),
                            expanded = quickPartyNameExpanded,
                            onDismissRequest = { quickPartyNameExpanded = false },
                            suggestions = filteredParties,
                            itemText = { it },
                            onSuggestionSelected = { name ->
                                quickPartyName = name
                                quickPartyNameExpanded = false
                            },
                            avatarColor =
                                com.storebook.inventoryapp.ui.theme.Coral500
                                    .copy(alpha = 0.12f),
                            avatarTextColor = com.storebook.inventoryapp.ui.theme.Coral500,
                            openAbove = true,
                        )
                    }

                    // Direction toggle — the highest-stakes choice in this form, so it's large,
                    // color-coded, and impossible to miss or mis-tap
                    Column {
                        Text(
                            text = "Transaction Type",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            DirectionOption(
                                modifier = Modifier.weight(1f),
                                selected = quickPartyType == "CREDIT",
                                icon = Icons.AutoMirrored.Filled.ArrowForward,
                                title = "Given",
                                subtitle = "They owe you",
                                color = MaterialTheme.colorScheme.error,
                                onClick = { quickPartyType = "CREDIT" },
                            )
                            DirectionOption(
                                modifier = Modifier.weight(1f),
                                selected = quickPartyType == "PAYMENT",
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                title = "Got",
                                subtitle = "You owe them",
                                color = Emerald500,
                                onClick = { quickPartyType = "PAYMENT" },
                            )
                        }
                    }

                    // Amount — styled and tinted to match the selected direction for reinforcement
                    val directionColor = if (quickPartyType == "CREDIT") MaterialTheme.colorScheme.error else Emerald500
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(directionColor.copy(alpha = 0.08f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (quickPartyType == "CREDIT") "Amount Given" else "Amount Received",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = quickPartyAmount,
                            onValueChange = {
                                quickPartyAmount = it
                                quickPartyAmountError = false
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
                                Text("₹", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = directionColor)
                            },
                            textStyle =
                                LocalTextStyle.current.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = directionColor,
                                ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = quickPartyAmountError,
                            keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                ),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                ),
                            supportingText =
                                if (quickPartyAmountError) {
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

                    // Plain-language confirmation line — removes any remaining ambiguity before saving
                    if (quickPartyName.isNotBlank() &&
                        quickPartyAmount.toDoubleOrNull() != null &&
                        quickPartyAmount.toDoubleOrNull()!! > 0
                    ) {
                        val amt = quickPartyAmount.toDoubleOrNull() ?: 0.0
                        Text(
                            text =
                                if (quickPartyType == "CREDIT") {
                                    "${quickPartyName.trim()} now owes you ₹${"%.2f".format(amt)} more"
                                } else {
                                    "You now owe ${quickPartyName.trim()} ₹${"%.2f".format(amt)} less"
                                },
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    enabled = !isQuickPartySubmitting,
                    onClick = {
                        val amt = quickPartyAmount.toDoubleOrNull()
                        quickPartyNameError = quickPartyName.isBlank()
                        quickPartyAmountError = amt == null || amt <= 0.0

                        if (quickPartyNameError || quickPartyAmountError) return@PrimaryButton

                        isQuickPartySubmitting = true
                        udhaarViewModel.recordUdhaarEntry(quickPartyName.trim(), amt!!, quickPartyType, "Quick Entry")
                        isQuickPartySubmitting = false
                        quickPartyName = ""
                        quickPartyAmount = ""
                        showQuickParty = false
                    },
                ) {
                    if (isQuickPartySubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Save Entry")
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    enabled = !isQuickPartySubmitting,
                    onClick = { showQuickParty = false },
                ) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            if (showBottomBar && currentRoute != "com.storebook.inventoryapp.ui.navigation.Routes.Inventory") {
                SpeedDialFab(
                    expanded = speedDialExpanded,
                    onToggle = { speedDialExpanded = !speedDialExpanded },
                    onNewSale = {
                        speedDialExpanded = false
                        showQuickSale = true
                    },
                    onRestock = {
                        speedDialExpanded = false
                        showQuickRestock = true
                    },
                    onExpense = {
                        speedDialExpanded = false
                        showQuickExpense = true
                    },
                    onAddParty = {
                        speedDialExpanded = false
                        showQuickParty = true
                    },
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                ModernBottomNavBar(
                    tabs = tabs,
                    currentRoute = currentRoute,
                    cartCount = salesViewModel.cartItems.size,
                    onTabSelected = { tab ->
                        val routeName = tab.route::class.qualifiedName
                        val startDest = navController.graph.findStartDestination()
                        if (routeName != currentRoute) {
                            try {
                                navController.navigate(tab.route) {
                                    popUpTo(startDest.id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = tab.route != Routes.Dashboard
                                }
                            } catch (e: Exception) {
                                if (e is kotlinx.coroutines.CancellationException) throw e
                                if (com.storebook.inventoryapp.BuildConfig.DEBUG) {
                                    android.util.Log
                                        .e("AppNav", "Navigation error", e)
                                }
                            }
                        }
                    },
                )
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Splash,
            modifier =
                Modifier
                    .fillMaxSize()
                    .let {
                        if (currentRoute == "com.storebook.inventoryapp.ui.navigation.Routes.Splash") {
                            it
                        } else {
                            it.padding(paddingValues)
                        }
                    },
            enterTransition = {
                fadeIn(animationSpec = tween(200)) +
                    slideInHorizontally(
                        animationSpec = tween(200),
                        initialOffsetX = { it / 12 },
                    )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(150)) +
                    slideOutHorizontally(
                        animationSpec = tween(150),
                        targetOffsetX = { -it / 12 },
                    )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) +
                    slideInHorizontally(
                        animationSpec = tween(200),
                        initialOffsetX = { -it / 12 },
                    )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(150)) +
                    slideOutHorizontally(
                        animationSpec = tween(150),
                        targetOffsetX = { it / 12 },
                    )
            },
        ) {
            // ── Splash Screen ──────────────────────────────────────────
            composable<Routes.Splash> {
                SplashScreen(
                    onSplashFinished = {
                        val destination = if (onboardingCompleted) Routes.Dashboard else Routes.Dashboard
                        navController.navigate(destination) {
                            popUpTo(Routes.Splash) { inclusive = true }
                        }
                    },
                )
            }

            composable<Routes.Dashboard> {
                DashboardScreen(
                    navController = navController,
                    viewModel = dashboardViewModel,
                    salesViewModel = salesViewModel,
                )
            }
            composable<Routes.Inventory> {
                InventoryScreen(viewModel = inventoryViewModel)
            }
            composable<Routes.Sales> {
                SalesScreen(navController = navController, viewModel = salesViewModel)
            }
            composable<Routes.SupplierLedger> {
                com.storebook.inventoryapp.ui.screens.storebook.SupplierLedgerScreen(
                    viewModel = supplierViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Routes.SalesHistory> {
                com.storebook.inventoryapp.ui.screens.storebook.SalesHistoryScreen(
                    navController = navController,
                    viewModel = salesViewModel,
                )
            }
            composable<Routes.SalesAnalytics> {
                com.storebook.inventoryapp.ui.screens.storebook.SalesAnalyticsScreen(
                    navController = navController,
                    viewModel = salesViewModel,
                )
            }
            composable<Routes.Quotations> {
                com.storebook.inventoryapp.ui.screens.storebook.QuotationScreen(
                    navController = navController,
                    viewModel = salesViewModel,
                )
            }
            composable<Routes.Udhaar> {
                UdhaarScreen(viewModel = udhaarViewModel)
            }
            composable<Routes.More> {
                MoreScreen(navController = navController, viewModel = moreViewModel)
            }
            composable<Routes.PremiumPlans> {
                com.storebook.inventoryapp.ui.screens.storebook.ProBillingView(
                    isProActive = moreViewModel.isPremiumUser,
                    onRequireSignIn = {
                        navController.navigate(Routes.Auth)
                    },
                    onDismiss = {
                        navController.popBackStack()
                    },
                )
            }
            composable<Routes.Auth> {
                AuthScreen(
                    onAuthSuccess = {
                        moreViewModel.refreshUserState()
                        // Bug fix: Force a complete app restart to ensure all ViewModels
                        // and AppViewModelFactory reconnect to the correct, freshly-synced
                        // storeId database. This prevents the stale UI bug.
                        activity?.let { act ->
                            act.viewModelStore.clear()
                            val intent = android.content.Intent(act, MainActivity::class.java)
                            intent.flags =
                                android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            act.startActivity(intent)
                            act.finish()
                        }
                    },
                    onNavigateBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Routes.Dashboard) {
                                popUpTo(Routes.Auth) { inclusive = true }
                            }
                        }
                    },
                )
            }
            composable<Routes.InviteStaff> {
                com.storebook.inventoryapp.ui.screens.storebook.InviteStaffScreen(
                    navController = navController,
                    viewModel = moreViewModel,
                )
            }

            composable<Routes.GSTReport> {
                GSTReportScreen(
                    navController = navController,
                    viewModel = dashboardViewModel,
                )
            }

            // E03-S1 — Price Snapshot Audit view
            composable<Routes.PriceDriftReport> {
                PriceDriftReportScreen(
                    navController = navController,
                    viewModel = salesViewModel,
                )
            }

            // Epic E34 — Stock Audit Report
            composable<Routes.StockAudit> {
                StockAuditScreen(
                    navController = navController,
                    viewModel = stockAuditViewModel,
                )
            }

            // E56-S1 — Invoice PDF Settings screen
            composable<Routes.PdfSettings> {
                com.storebook.inventoryapp.ui.settings.PdfSettingsScreen(
                    initialSettings = moreViewModel.invoiceSettings,
                    onBack = { navController.popBackStack() },
                    onSave = moreViewModel::saveInvoiceSettings,
                )
            }

            // E56-S2 — Invoice PDF Preview screen
            composable<Routes.InvoicePdfPreview> { backStackEntry ->
                val saleId = backStackEntry.arguments?.getLong("saleId", 0L) ?: 0L
                androidx.compose.runtime.LaunchedEffect(saleId) {
                    salesViewModel.loadSaleForPdf(saleId)
                }
                com.storebook.inventoryapp.ui.pdf.InvoicePdfPreviewScreen(
                    saleId = saleId,
                    cartItems = salesViewModel.pdfPreviewCartItems.value,
                    totalAmount = salesViewModel.pdfPreviewTotalAmount.value,
                    settings = moreViewModel.invoiceSettings,
                    onBack = { navController.popBackStack() },
                    onPrintThermal = {},
                    onSaveFile = { },
                )
            }

            // E56-S3 — Estimate PDF Preview screen
            composable<Routes.EstimatePdfPreview> { backStackEntry ->
                val estimateId = backStackEntry.arguments?.getLong("estimateId", 0L) ?: 0L
                androidx.compose.runtime.LaunchedEffect(estimateId) {
                    salesViewModel.loadSaleForPdf(estimateId)
                }
                com.storebook.inventoryapp.ui.pdf.EstimatePdfPreviewScreen(
                    estimateId = estimateId,
                    cartItems = salesViewModel.pdfPreviewCartItems.value,
                    totalAmount = salesViewModel.pdfPreviewTotalAmount.value,
                    settings = moreViewModel.invoiceSettings,
                    onBack = { navController.popBackStack() },
                    onPrintThermal = {},
                    onSaveFile = { },
                )
            }
        }
    }
}

@Composable
fun SpeedDialFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onNewSale: () -> Unit,
    onRestock: () -> Unit,
    onExpense: () -> Unit,
    onAddParty: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fab_rotation",
    )
    val actions =
        listOf(
            Triple(Icons.Filled.ShoppingCart, "New Sale", onNewSale),
            Triple(Icons.Filled.Inventory, "Restock", onRestock),
            Triple(Icons.Filled.MoneyOff, "Expense", onExpense),
            Triple(Icons.Filled.PersonAdd, "Add Party", onAddParty),
        )
    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
        actions.forEachIndexed { i, (icon, label, action) ->
            AnimatedVisibility(
                visible = expanded,
                enter = scaleIn(animationSpec = tween(100 + i * 40)) + fadeIn(),
                exit = scaleOut(animationSpec = tween(80)) + fadeOut(),
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    // Label
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(2.dp),
                    ) {
                        Text(
                            label,
                            modifier =
                                Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp,
                                ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    SmallFloatingActionButton(
                        onClick = action,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                    ) { Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp)) }
                }
            }
        }
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Quick Actions",
                modifier =
                    Modifier
                        .size(26.dp)
                        .then(Modifier.graphicsLayer { rotationZ = rotation }),
            )
        }
    }
}

@Composable
private fun ModernBottomNavBar(
    tabs: List<BottomNavTab>,
    currentRoute: String?,
    cartCount: Int,
    onTabSelected: (BottomNavTab) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        tabs.forEach { tab ->
            val isSelected = currentRoute == tab.route::class.qualifiedName
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "tab_scale",
            )
            val isSalesTab = tab.labelRes == R.string.tab_sales

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (isSalesTab && cartCount > 0) {
                        androidx.compose.material3.BadgedBox(
                            badge = {
                                androidx.compose.material3.Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White,
                                ) {
                                    Text(
                                        text = if (cartCount > 9) "9+" else "$cartCount",
                                        fontWeight = FontWeight.Black,
                                        fontFamily = Poppins,
                                    )
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                                contentDescription = stringResource(R.string.ui_element_desc),
                                modifier =
                                    Modifier
                                        .scale(scale)
                                        .size(24.dp),
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                            contentDescription = stringResource(R.string.ui_element_desc),
                            modifier =
                                Modifier
                                    .scale(scale)
                                    .size(24.dp),
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(id = tab.labelRes),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = Poppins,
                        maxLines = 1,
                        modifier =
                            androidx.compose.ui.Modifier
                                .autoMarquee(),
                    )
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
            )
        }
    }
}
