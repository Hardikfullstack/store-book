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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.storebook.inventoryapp.ui.screens.storebook.UdhaarScreen
import com.storebook.inventoryapp.ui.screens.storebook.SupplierLedgerScreen
import com.storebook.inventoryapp.ui.screens.storebook.GSTReportScreen
import com.storebook.inventoryapp.ui.theme.Poppins

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
    val storeBookViewModel: com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel =
        androidx.lifecycle.viewmodel.compose
            .viewModel()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

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

    // Quick Expense mini-dialog
    if (showQuickExpense) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQuickExpense = false },
            title = { Text("Log Expense", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = quickExpenseAmount,
                        onValueChange = { quickExpenseAmount = it },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = quickExpenseDesc,
                        onValueChange = { quickExpenseDesc = it },
                        label = { Text("Description") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    val amt = quickExpenseAmount.toDoubleOrNull()
                    if (amt != null && quickExpenseDesc.isNotBlank()) {
                        storeBookViewModel.logOverheadExpense(quickExpenseDesc.trim(), amt)
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

    Scaffold(
        floatingActionButton = {
            if (showBottomBar && currentRoute != "com.storebook.inventoryapp.ui.navigation.Routes.Inventory") {
                SpeedDialFab(
                    expanded = speedDialExpanded,
                    onToggle = { speedDialExpanded = !speedDialExpanded },
                    onNewSale = {
                        speedDialExpanded = false
                        navController.navigate(Routes.Sales)
                    },
                    onRestock = {
                        speedDialExpanded = false
                        navController.navigate(Routes.Inventory)
                    },
                    onExpense = {
                        speedDialExpanded = false
                        showQuickExpense = true
                    },
                    onAddParty = {
                        speedDialExpanded = false
                        navController.navigate(Routes.Udhaar)
                    },
                    onBulkRestock = {
                        speedDialExpanded = false
                        navController.navigate(Routes.Inventory)
                    },
                    onQuickBill = {
                        speedDialExpanded = false
                        navController.navigate(Routes.Sales)
                    },
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                ModernBottomNavBar(
                    tabs = tabs,
                    currentRoute = currentRoute,
                    cartCount = storeBookViewModel.cartItems.size,
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
                                android.util.Log.e("AppNav", "Navigation error", e)
                            }
                        }
                    },
                )
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Dashboard,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
            composable<Routes.Dashboard> {
                DashboardScreen(navController = navController, viewModel = storeBookViewModel)
            }
            composable<Routes.Inventory> {
                InventoryScreen(viewModel = storeBookViewModel)
            }
            composable<Routes.Sales> {
                SalesScreen(navController = navController, viewModel = storeBookViewModel)
            }
            composable<Routes.SalesHistory> {
                com.storebook.inventoryapp.ui.screens.storebook.SalesHistoryScreen(
                    navController = navController,
                    viewModel = storeBookViewModel,
                )
            }
            composable<Routes.SalesAnalytics> {
                com.storebook.inventoryapp.ui.screens.storebook.SalesAnalyticsScreen(
                    navController = navController,
                    viewModel = storeBookViewModel,
                )
            }
            composable<Routes.Quotations> {
                com.storebook.inventoryapp.ui.screens.storebook.QuotationScreen(
                    navController = navController,
                    viewModel = storeBookViewModel,
                )
            }
            composable<Routes.Udhaar> {
                UdhaarScreen(viewModel = storeBookViewModel)
            }
            composable<Routes.More> {
                MoreScreen(navController = navController, viewModel = storeBookViewModel)
            }
            composable<Routes.PremiumPlans> {
                Scaffold { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        com.storebook.inventoryapp.ui.screens.storebook.ProBillingView(
                            isProActive = storeBookViewModel.isPremiumUser,
                            onRequireSignIn = {
                                navController.navigate(Routes.Auth)
                            },
                            onDismiss = {
                                navController.popBackStack()
                            },
                        )
                    }
                }
            }
            composable<Routes.Auth> {
                AuthScreen(
                    onAuthSuccess = {
                        storeBookViewModel.refreshUserState()
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
                    viewModel = storeBookViewModel,
                )
            }
            composable<Routes.SupplierLedger> {
                SupplierLedgerScreen(
                    viewModel = storeBookViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Routes.GSTReport> {
                GSTReportScreen(
                    navController = navController,
                    viewModel = storeBookViewModel
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
    onBulkRestock: () -> Unit,
    onQuickBill: () -> Unit,
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
        Triple(Icons.Filled.LibraryAdd, "Bulk Restock", onBulkRestock),
        Triple(Icons.Filled.ReceiptLong, "Quick Bill", onQuickBill),
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
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .scale(scale)
                                        .size(24.dp),
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                            contentDescription = null,
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
