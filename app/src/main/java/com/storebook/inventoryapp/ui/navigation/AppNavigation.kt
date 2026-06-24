package com.storebook.inventoryapp.ui.navigation

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
<<<<<<< HEAD
=======
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
>>>>>>> 5679363 (all textfield keybord next click set, inventory filter change and ui dark mode)
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.storebook.inventoryapp.ui.theme.Poppins

data class BottomNavTab(
    val route: String,
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
                    "com.storebook.inventoryapp.ui.navigation.Routes.Dashboard",
                    Icons.Outlined.Home,
                    Icons.Filled.Home,
                    R.string.tab_dashboard,
                ),
                BottomNavTab(
                    "com.storebook.inventoryapp.ui.navigation.Routes.Inventory",
                    @Suppress("DEPRECATION") Icons.Outlined.List,
                    @Suppress("DEPRECATION") Icons.Filled.List,
                    R.string.tab_inventory,
                ),
                BottomNavTab(
                    "com.storebook.inventoryapp.ui.navigation.Routes.Sales",
                    Icons.Outlined.ShoppingCart,
                    Icons.Filled.ShoppingCart,
                    R.string.tab_sales,
                ),
                BottomNavTab(
                    "com.storebook.inventoryapp.ui.navigation.Routes.Udhaar",
                    Icons.Outlined.Book,
                    Icons.Filled.Book,
                    R.string.tab_udhaar,
                ),
                BottomNavTab(
                    "com.storebook.inventoryapp.ui.navigation.Routes.More",
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

<<<<<<< HEAD
=======
    var speedDialExpanded by remember { mutableStateOf(false) }
    // Quick Expense dialog state
    var showQuickExpense by remember { mutableStateOf(false) }
    var quickExpenseAmount by remember { mutableStateOf("") }
    var quickExpenseDesc by remember { mutableStateOf("") }

    val focusRequesterDesc = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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

>>>>>>> 5679363 (all textfield keybord next click set, inventory filter change and ui dark mode)
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ModernBottomNavBar(
                    tabs = tabs,
                    currentRoute = currentRoute,
                    cartCount = storeBookViewModel.cartItems.size,
                    onTabSelected = { tab ->
                        if (tab.route != currentRoute) {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
            val isSelected = currentRoute == tab.route
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
