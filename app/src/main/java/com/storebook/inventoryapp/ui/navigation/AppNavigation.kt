package com.storebook.inventoryapp.ui.navigation

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.storebook.inventoryapp.ui.screens.storebook.InventoryScreen
import com.storebook.inventoryapp.ui.screens.storebook.MoreScreen
import com.storebook.inventoryapp.ui.screens.storebook.SalesScreen
import com.storebook.inventoryapp.ui.screens.storebook.SplashScreen
import com.storebook.inventoryapp.ui.screens.storebook.UdhaarScreen
import com.storebook.inventoryapp.ui.screens.storebook.SupplierLedgerScreen
import com.storebook.inventoryapp.ui.screens.storebook.GSTReportScreen
import com.storebook.inventoryapp.ui.theme.Poppins
import com.storebook.inventoryapp.ui.theme.PrimaryButton

data class BottomNavTab(
    val route: Any,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val labelRes: Int,
)

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val navController = rememberNavController()


    val udhaarViewModel: com.storebook.inventoryapp.ui.viewmodel.UdhaarViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.storebook.inventoryapp.ui.viewmodel.AppViewModelFactory(context)
        )

    val inventoryViewModel: com.storebook.inventoryapp.ui.viewmodel.InventoryViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.storebook.inventoryapp.ui.viewmodel.AppViewModelFactory(context)
        )

    val salesViewModel: com.storebook.inventoryapp.ui.viewmodel.SalesViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.storebook.inventoryapp.ui.viewmodel.AppViewModelFactory(context)
        )

    val dashboardViewModel: com.storebook.inventoryapp.ui.viewmodel.DashboardViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.storebook.inventoryapp.ui.viewmodel.AppViewModelFactory(context)
        )

    val purchaseViewModel: com.storebook.inventoryapp.ui.viewmodel.PurchaseViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.storebook.inventoryapp.ui.viewmodel.AppViewModelFactory(context)
        )

    val supplierViewModel: com.storebook.inventoryapp.ui.viewmodel.SupplierViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.storebook.inventoryapp.ui.viewmodel.AppViewModelFactory(context)
        )

    val expenseViewModel: com.storebook.inventoryapp.ui.viewmodel.ExpenseViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.storebook.inventoryapp.ui.viewmodel.AppViewModelFactory(context)
        )

    val moreViewModel: com.storebook.inventoryapp.ui.viewmodel.MoreViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.storebook.inventoryapp.ui.viewmodel.AppViewModelFactory(context)
        )

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Error messages handled in individual ViewModels now
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
    var quickExpenseDesc by remember { mutableStateOf("") }

    val focusRequesterDesc = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Quick Sale state
    var showQuickSale by remember { mutableStateOf(false) }
    var quickSaleAmount by remember { mutableStateOf("") }
    var quickSaleCustomer by remember { mutableStateOf("") }
    var quickSaleCustomerExpanded by remember { mutableStateOf(false) }

    // Quick Restock state
    var showQuickRestock by remember { mutableStateOf(false) }
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

    val customerSuggestions by salesViewModel.customerSuggestions.collectAsState()
    val allItems by salesViewModel.allItems.collectAsState()

    // Quick Expense mini-dialog
    if (showQuickExpense) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQuickExpense = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Log Expense", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = quickExpenseAmount,
                        onValueChange = { quickExpenseAmount = it },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusRequesterDesc.requestFocus() }),
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = quickExpenseDesc,
                        onValueChange = { quickExpenseDesc = it },
                        label = { Text("Description") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = androidx.compose.ui.Modifier.focusRequester(focusRequesterDesc)
                    )
                }
            },
            confirmButton = {
                PrimaryButton(onClick = {
                    val amt = quickExpenseAmount.toDoubleOrNull()
                    if (amt != null && quickExpenseDesc.isNotBlank()) {
                        expenseViewModel.addExpense(
                            type = "OVERHEAD",
                            amount = amt,
                            notes = quickExpenseDesc.trim()
                        )
                        quickExpenseAmount = ""
                        quickExpenseDesc = ""
                        showQuickExpense = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showQuickExpense = false }) { Text("Cancel") }
            }
        )
    }

    if (showQuickSale) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQuickSale = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Quick Cash Sale", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.foundation.layout.BoxWithConstraints {
                        val boxWidth = maxWidth
                        androidx.compose.material3.OutlinedTextField(
                            value = quickSaleCustomer,
                            onValueChange = {
                                quickSaleCustomer = it
                                salesViewModel.customerSuggestions.value
                                quickSaleCustomerExpanded = it.isNotBlank()
                            },
                            label = { Text("Customer Name (Optional)") },
                            singleLine = true,
                        )
                        com.storebook.inventoryapp.ui.components.StoreBookAutocompleteDropdown(
                            modifier = Modifier.width(boxWidth),
                            expanded = quickSaleCustomerExpanded,
                            onDismissRequest = { quickSaleCustomerExpanded = false },
                            suggestions = customerSuggestions,
                            itemText = { it },
                            onSuggestionSelected = { name ->
                                quickSaleCustomer = name
                                quickSaleCustomerExpanded = false
                            },
                            avatarColor = MaterialTheme.colorScheme.primary,
                            avatarTextColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    androidx.compose.material3.OutlinedTextField(
                        value = quickSaleAmount,
                        onValueChange = { quickSaleAmount = it },
                        label = { Text("Total Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    )
                }
            },
            confirmButton = {
                PrimaryButton(onClick = {
                    val amt = quickSaleAmount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        salesViewModel.clearCart()
                        val dummyItem = com.storebook.inventoryapp.shared.domain.models.Item(
                            id = 0L,
                            name = "Quick Sale",
                            quantity = 0.0,
                            unit = "pcs",
                            buyPrice = 0.0,
                            sellPrice = amt,
                            lowStockThreshold = 0.0,
                            category = "General"
                        )
                        salesViewModel.addToCart(dummyItem, 1.0)
                        if (quickSaleCustomer.isNotBlank()) {
                            salesViewModel.cartCustomerName = quickSaleCustomer.trim()
                        }
                        salesViewModel.checkout(paymentMode = "Cash", type = "SALE") { _, _ -> }
                        quickSaleCustomer = ""
                        quickSaleAmount = ""
                        showQuickSale = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showQuickSale = false }) { Text("Cancel") }
            }
        )
    }

    if (showQuickRestock) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQuickRestock = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Quick Add Item", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.foundation.layout.BoxWithConstraints {
                        val boxWidth = maxWidth
                        androidx.compose.material3.OutlinedTextField(
                            value = quickRestockName,
                            onValueChange = {
                                quickRestockName = it
                                selectedRestockItemId = null
                                quickRestockNameExpanded = it.isNotBlank()
                            },
                            label = { Text("Item Name") },
                            singleLine = true,
                        )
                        val filteredItems = allItems.filter { it.name.contains(quickRestockName, ignoreCase = true) }.take(5)
                        com.storebook.inventoryapp.ui.components.StoreBookAutocompleteDropdown(
                            modifier = Modifier.width(boxWidth),
                            expanded = quickRestockNameExpanded,
                            onDismissRequest = { quickRestockNameExpanded = false },
                            suggestions = filteredItems,
                            itemText = { it.name },
                            onSuggestionSelected = { item ->
                                quickRestockName = item.name
                                selectedRestockItemId = item.id
                                quickRestockPrice = item.sellPrice.toString()
                                quickRestockNameExpanded = false
                            },
                            avatarColor = MaterialTheme.colorScheme.secondary,
                            avatarTextColor = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    androidx.compose.material3.OutlinedTextField(
                        value = quickRestockQty,
                        onValueChange = { quickRestockQty = it },
                        label = { Text("Stock Quantity") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = quickRestockPrice,
                        onValueChange = { quickRestockPrice = it },
                        label = { Text("Sale Price (₹)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    )
                }
            },
            confirmButton = {
                PrimaryButton(onClick = {
                    val qty = quickRestockQty.toDoubleOrNull() ?: 0.0
                    val cost = quickRestockPrice.toDoubleOrNull() ?: 0.0
                    if (quickRestockName.isNotBlank() && cost > 0.0) {
                        if (selectedRestockItemId != null) {
                            scope.launch {
                                inventoryViewModel.restockItem(
                                    itemId = selectedRestockItemId!!,
                                    quantityToAdd = qty,
                                    costPrice = cost,
                                    supplierName = null,
                                    supplierPhone = null
                                )
                                inventoryViewModel.loadFilteredItems()
                            }
                        } else {
                            scope.launch {
                                inventoryViewModel.addItem(
                                    name = quickRestockName.trim(),
                                    quantity = qty,
                                    unit = "pcs",
                                    buyPrice = cost,
                                    sellPrice = cost * 1.2,
                                    threshold = 5.0,
                                    category = "Uncategorized",
                                    hsnCode = null,
                                    taxRate = 0.0,
                                    onResult = {}
                                )
                                inventoryViewModel.loadFilteredItems()
                            }
                        }
                        quickRestockName = ""
                        quickRestockQty = ""
                        quickRestockPrice = ""
                        selectedRestockItemId = null
                        showQuickRestock = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showQuickRestock = false }) { Text("Cancel") }
            }
        )
    }

    if (showQuickParty) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQuickParty = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Log Party Balance", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.foundation.layout.BoxWithConstraints {
                        val boxWidth = maxWidth
                        androidx.compose.material3.OutlinedTextField(
                            value = quickPartyName,
                            onValueChange = {
                                quickPartyName = it
                                salesViewModel.customerSuggestions.value
                                quickPartyNameExpanded = it.isNotBlank()
                            },
                            label = { Text("Party Name") },
                            singleLine = true,
                        )
                        com.storebook.inventoryapp.ui.components.StoreBookAutocompleteDropdown(
                            modifier = Modifier.width(boxWidth),
                            expanded = quickPartyNameExpanded,
                            onDismissRequest = { quickPartyNameExpanded = false },
                            suggestions = customerSuggestions,
                            itemText = { it },
                            onSuggestionSelected = { name ->
                                quickPartyName = name
                                quickPartyNameExpanded = false
                            },
                            avatarColor = com.storebook.inventoryapp.ui.theme.Coral500.copy(alpha = 0.12f),
                            avatarTextColor = com.storebook.inventoryapp.ui.theme.Coral500
                        )
                    }
                    androidx.compose.material3.OutlinedTextField(
                        value = quickPartyAmount,
                        onValueChange = { quickPartyAmount = it },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    )
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        androidx.compose.material3.FilterChip(
                            selected = quickPartyType == "CREDIT",
                            onClick = { quickPartyType = "CREDIT" },
                            label = { Text("Given (Due)") }
                        )
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material3.FilterChip(
                            selected = quickPartyType == "PAYMENT",
                            onClick = { quickPartyType = "PAYMENT" },
                            label = { Text("Got (Advance)") }
                        )
                    }
                }
            },
            confirmButton = {
                PrimaryButton(onClick = {
                    val amt = quickPartyAmount.toDoubleOrNull()
                    if (amt != null && quickPartyName.isNotBlank()) {
                        udhaarViewModel.recordUdhaarEntry(quickPartyName.trim(), amt, quickPartyType, "Quick Entry")
                        quickPartyName = ""
                        quickPartyAmount = ""
                        showQuickParty = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showQuickParty = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
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
                                if (com.storebook.inventoryapp.BuildConfig.DEBUG) android.util.Log.e("AppNav", "Navigation error", e)
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
                DashboardScreen(navController = navController, viewModel = dashboardViewModel, salesViewModel = salesViewModel)
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
                    onBack = { navController.popBackStack() }
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
                    viewModel = salesViewModel
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
                        navController.navigate(Routes.Dashboard) {
                            popUpTo(Routes.Auth) { inclusive = true }
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
                    viewModel = dashboardViewModel
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
        label = "fab_rotation"
    )
    val actions = listOf(
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
                        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall)
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
                modifier = Modifier
                    .size(26.dp)
                    .then(Modifier.graphicsLayer { rotationZ = rotation })
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
                        overflow = TextOverflow.Ellipsis,
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
