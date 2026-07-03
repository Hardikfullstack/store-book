@file:android.annotation.SuppressLint("LocalContextGetResourceValueCall")
package com.storebook.inventoryapp.ui.screens.storebook

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.domain.models.ExpenseEntry
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.ui.navigation.Routes
import com.storebook.inventoryapp.ui.theme.Coral500
import com.storebook.inventoryapp.ui.theme.Emerald500
import com.storebook.inventoryapp.ui.theme.Gold200
import com.storebook.inventoryapp.ui.theme.Gold400
import com.storebook.inventoryapp.ui.theme.AppThemeMode
import com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel
import com.storebook.inventoryapp.utils.LanguageManager
import com.storebook.inventoryapp.utils.toRupee
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import com.storebook.inventoryapp.ui.theme.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
        navController: NavController,
        viewModel: StoreBookViewModel,
) {
        val context = LocalContext.current
        val activity = context as? Activity
        val scope = rememberCoroutineScope()
        val languageManager = remember { LanguageManager(context) }
        val currentLang by
                languageManager.appLanguage.collectAsStateWithLifecycle(initialValue = "en")

        val salesList by viewModel.salesList.collectAsStateWithLifecycle()
        val expensesList by viewModel.expensesList.collectAsStateWithLifecycle()
        val allItems by viewModel.allItems.collectAsStateWithLifecycle()

        val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
        var currentUser by remember { mutableStateOf(auth.currentUser) }

        var activeModal by remember { mutableStateOf("") }
        var showSheet by remember { mutableStateOf(false) }
        var showThemeExpanded by remember { mutableStateOf(false) }
        var showUpgradeDialog by remember { mutableStateOf(false) }
        var showClearDataDialog by remember { mutableStateOf(false) }
        var showLogoutConfirmation by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val themeManager = com.storebook.inventoryapp.ui.theme.appThemeManager

        var expenseType by remember { mutableStateOf("OVERHEAD") }
        var expenseAmount by remember { mutableStateOf("") }
        var expenseDesc by remember { mutableStateOf("") }

        var selectedItemId by remember { mutableStateOf<Long?>(null) }
        var restockQty by remember { mutableStateOf("") }
        var restockCostPrice by remember { mutableStateOf("") }
        var restockSupplier by remember { mutableStateOf("") }
        var restockPhone by remember { mutableStateOf("") }

        val csvFilePickerLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument(),
                        onResult = { uri ->
                                uri?.let {
                                        viewModel.importInventoryFromCSV(
                                                context = context,
                                                fileUri = it,
                                                onSuccess = {
                                                        android.widget.Toast.makeText(
                                                                        context,
                                                                        context.getString(
                                                                                R.string
                                                                                        .toast_csv_imported
                                                                        ),
                                                                        android.widget.Toast
                                                                                .LENGTH_SHORT,
                                                                )
                                                                .show()
                                                },
                                                onError = { err ->
                                                        android.widget.Toast.makeText(
                                                                        context,
                                                                        err,
                                                                        android.widget.Toast
                                                                                .LENGTH_SHORT
                                                                )
                                                                .show()
                                                },
                                        )
                                }
                        },
                )

        val csvExportLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument("text/csv"),
                        onResult = { uri ->
                                uri?.let {
                                        viewModel.exportInventoryToCSV(
                                                context = context,
                                                fileUri = it,
                                                onSuccess = {
                                                        android.widget.Toast.makeText(
                                                                        context,
                                                                        context.getString(
                                                                                R.string
                                                                                        .toast_csv_exported
                                                                        ),
                                                                        android.widget.Toast
                                                                                .LENGTH_SHORT,
                                                                )
                                                                .show()
                                                },
                                                onError = { err ->
                                                        android.widget.Toast.makeText(
                                                                        context,
                                                                        err,
                                                                        android.widget.Toast
                                                                                .LENGTH_SHORT
                                                                )
                                                                .show()
                                                },
                                        )
                                }
                        },
                )

        Scaffold { paddingValues ->
                LazyColumn(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(bottom = paddingValues.calculateBottomPadding()),
                        contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                        // Gradient header with shop info
                        item {
                                Box(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .background(
                                                                Brush.linearGradient(
                                                                        listOf(
                                                                                MaterialTheme.colorScheme.primary,
                                                                                MaterialTheme.colorScheme.tertiary
                                                                        )
                                                                )
                                                        )
                                                        .padding(
                                                                top =
                                                                        paddingValues
                                                                                .calculateTopPadding()
                                                        )
                                                        .padding(
                                                                horizontal = 20.dp,
                                                                vertical = 20.dp
                                                        ),
                                ) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                                // Shop avatar
                                                Box(
                                                        modifier =
                                                                Modifier.size(52.dp)
                                                                        .clip(CircleShape)
                                                                        .background(
                                                                                MaterialTheme.colorScheme.onPrimary.copy(
                                                                                        alpha =
                                                                                                0.15f
                                                                                )
                                                                        ),
                                                        contentAlignment = Alignment.Center,
                                                ) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.ic_store),
                                                            contentDescription = "Shop Icon",
                                                            tint = MaterialTheme.colorScheme.onPrimary,
                                                            modifier = Modifier.size(30.dp)
                                                        )
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Column {
                                                        Text(
                                                                text =
                                                                        stringResource(
                                                                                id =
                                                                                        R.string
                                                                                                .app_name
                                                                        ),
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 18.sp,
                                                                color = MaterialTheme.colorScheme.onPrimary,
                                                        )
                                                        Text(
                                                                text =
                                                                        "${allItems.size} items · ${salesList.size} total sales",
                                                                fontSize = 12.sp,
                                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                                        )
                                                }
                                        }
                                }
                        }

                        // Premium promo card
                        item {
                                Card(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(
                                                                horizontal = 16.dp,
                                                                vertical = 10.dp
                                                        )
                                                        .clickable(onClickLabel = "Action") {
                                                                navController.navigate(Routes.PremiumPlans)
                                                        },
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor = Gold200.copy(alpha = 0.12f)
                                                ),
                                        shape = RoundedCornerShape(18.dp),
                                ) {
                                        Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                                Box(
                                                        modifier =
                                                                Modifier.size(46.dp)
                                                                        .clip(CircleShape)
                                                                        .background(
                                                                                Gold200.copy(
                                                                                        alpha = 0.3f
                                                                                )
                                                                        ),
                                                        contentAlignment = Alignment.Center,
                                                ) {
                                                        Icon(
                                                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_star),
                                                                contentDescription = stringResource(R.string.ui_element_desc),
                                                                tint = Gold400,
                                                                modifier = Modifier.size(24.dp),
                                                        )
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                                text = if (viewModel.isPremiumUser) "Manage Pro Subscription" else stringResource(id = R.string.more_pro_plans),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 15.sp,
                                                                color = MaterialTheme.colorScheme.secondary,
                                                        )
                                                        Text(
                                                                text = if (viewModel.isPremiumUser) "View your active plan details" else stringResource(id = R.string.more_pro_desc),
                                                                fontSize = 12.sp,
                                                                color =
                                                                        MaterialTheme.colorScheme.secondary
                                                                                .copy(
                                                                                        alpha =
                                                                                                0.75f
                                                                                ),
                                                        )
                                                }
                                                Icon(
                                                        Icons.AutoMirrored.Filled
                                                                .KeyboardArrowRight,
                                                        contentDescription = stringResource(R.string.ui_element_desc),
                                                        tint = Gold400
                                                )
                                        }
                                }
                        }

                        // Settings options group
                        item {
                                Card(
                                        modifier =
                                                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.surface
                                                ),
                                        shape = RoundedCornerShape(20.dp),
                                        elevation =
                                                CardDefaults.cardElevation(defaultElevation = 1.dp),
                                ) {
                                        Column {
                                                IconOptionRow(
                                                        icon = Icons.Outlined.Language,
                                                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                        iconTint = MaterialTheme.colorScheme.primary,
                                                        title =
                                                                stringResource(
                                                                        id = R.string.more_language
                                                                ),
                                                        trailing =
                                                                when (currentLang) {
                                                                        "hi" -> "हिंदी"
                                                                        "gu" -> "ગુજ"
                                                                        else -> "English"
                                                                },
                                                        onClick = {
                                                                activeModal = "LANGUAGES"
                                                                showSheet = true
                                                        },
                                                )
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )
                                                IconOptionRow(
                                                        icon = Icons.Outlined.Palette,
                                                        iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                                        iconTint = MaterialTheme.colorScheme.secondary,
                                                        title = "App Theme",
                                                        trailing = if (themeManager.isDarkMode.value) "Dark" else "Light",
                                                        trailingIconRes = if (themeManager.isDarkMode.value) R.drawable.ic_dark_mode else R.drawable.ic_light_mode,
                                                        onClick = {
                                                            if (viewModel.isPremiumUser || viewModel.userRoleType.hasPermission(com.storebook.inventoryapp.ui.viewmodels.AppPermission.MANAGE_PREMIUM) || viewModel.userRole == "staff") {
                                                                showThemeExpanded = !showThemeExpanded
                                                            } else {
                                                                navController.navigate(Routes.PremiumPlans)
                                                            }
                                                        },
                                                )

                                                // ── Inline Theme Selector ────────────────────────
                                                AnimatedVisibility(
                                                    visible = showThemeExpanded,
                                                    enter = expandVertically() + fadeIn(),
                                                    exit = shrinkVertically() + fadeOut(),
                                                ) {
                                                    InlineThemeCard(
                                                        isDarkMode = themeManager.isDarkMode.value,
                                                        themeMode = themeManager.themeMode.value,
                                                        isPremium = viewModel.isPremiumUser,
                                                        onThemeSelected = { isDark -> themeManager.setDarkMode(isDark) },
                                                        onThemeModeSelected = { mode -> themeManager.setThemeMode(mode) },
                                                    )
                                                }
                                                HorizontalDivider(
                                                        modifier = Modifier.padding(horizontal = 16.dp)
                                                )

                                                IconOptionRow(
                                                        icon = Icons.Outlined.PieChart,
                                                        iconBg = Emerald500.copy(alpha = 0.12f),
                                                        iconTint = Emerald500,
                                                        title =
                                                                stringResource(
                                                                        id =
                                                                                R.string
                                                                                        .more_pnl_report
                                                                ),
                                                        onClick = {
                                                                activeModal = "REPORTS"
                                                                showSheet = true
                                                        },
                                                )
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )

                                                IconOptionRow(
                                                        icon = Icons.Outlined.Analytics,
                                                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                        iconTint = MaterialTheme.colorScheme.primary,
                                                        title = "Sales Analytics",
                                                        onClick = {
                                                                navController.navigate(
                                                                        Routes.SalesAnalytics
                                                                )
                                                        },
                                                )
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )

                                                IconOptionRow(
                                                        icon = Icons.Outlined.RequestQuote,
                                                        iconBg = Emerald500.copy(alpha = 0.12f),
                                                        iconTint = Emerald500,
                                                        title = "Quotations & Estimates",
                                                        onClick = {
                                                                navController.navigate(
                                                                        Routes.Quotations
                                                                )
                                                        },
                                                )
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )

                                                IconOptionRow(
                                                        icon = Icons.Outlined.AccountBalanceWallet,
                                                        iconBg = Coral500.copy(alpha = 0.12f),
                                                        iconTint = Coral500,
                                                        title =
                                                                stringResource(
                                                                        id =
                                                                                R.string
                                                                                        .more_expense_track
                                                                ),
                                                        onClick = {
                                                                activeModal = "EXPENSES"
                                                                showSheet = true
                                                        },
                                                )
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )

                                                IconOptionRow(
                                                        icon = Icons.Outlined.Inventory,
                                                        iconBg =
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.12f),
                                                        iconTint = MaterialTheme.colorScheme.primary,
                                                        title =
                                                                stringResource(
                                                                        id =
                                                                                R.string
                                                                                        .exp_restock_title
                                                                ),
                                                        onClick = {
                                                                activeModal = "RESTOCK"
                                                                showSheet = true
                                                        },
                                                )
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )

                                                IconOptionRow(
                                                        icon = Icons.Outlined.FileDownload,
                                                        iconBg =
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.12f),
                                                        iconTint = MaterialTheme.colorScheme.primary,
                                                        title =
                                                                stringResource(
                                                                        id =
                                                                                R.string
                                                                                        .more_csv_export
                                                                ),
                                                        onClick = {
                                                                csvExportLauncher.launch(
                                                                        "StoreBook_Inventory_${System.currentTimeMillis() / 1000}.csv"
                                                                )
                                                        },
                                                )
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )

                                                IconOptionRow(
                                                        icon = Icons.Outlined.FileUpload,
                                                        iconBg =
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.12f),
                                                        iconTint = MaterialTheme.colorScheme.primary,
                                                        title =
                                                                stringResource(
                                                                        id =
                                                                                R.string
                                                                                        .more_csv_import
                                                                ),
                                                        onClick = {
                                                                csvFilePickerLauncher.launch(
                                                                        arrayOf(
                                                                                "text/*",
                                                                                "application/csv",
                                                                                "text/csv"
                                                                        ),
                                                                )
                                                        },
                                                )
                                                Text(
                                                        text = "Format: ID,Item Name,Stock Quantity,Unit,Buy Price,Sell Price,Alert Threshold,Category,HSN Code,Tax Rate",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                        modifier = Modifier.padding(start = 54.dp, bottom = 8.dp, end = 16.dp)
                                                )
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )

                                                if (viewModel.userRoleType.hasPermission(com.storebook.inventoryapp.ui.viewmodels.AppPermission.MANAGE_BUSINESS_SETTINGS)) {
                                                    IconOptionRow(
                                                            icon = Icons.Outlined.SettingsSuggest,
                                                            iconBg =
                                                                    MaterialTheme.colorScheme.tertiary
                                                                            .copy(alpha = 0.12f),
                                                            iconTint = MaterialTheme.colorScheme.tertiary,
                                                            title = "Business Settings",
                                                            onClick = {
                                                                    activeModal = "BUSINESS"
                                                                    showSheet = true
                                                            },
                                                    )
                                                    HorizontalDivider(
                                                            modifier =
                                                                    Modifier.padding(horizontal = 16.dp)
                                                    )
                                                    IconOptionRow(
                                                            icon = Icons.Outlined.GroupAdd,
                                                            iconBg =
                                                                    MaterialTheme.colorScheme.primary
                                                                            .copy(alpha = 0.12f),
                                                            iconTint = MaterialTheme.colorScheme.primary,
                                                            title = "Invite Staff",
                                                            onClick = {
                                                                    navController.navigate(Routes.InviteStaff)
                                                            },
                                                    )
                                                    HorizontalDivider(
                                                            modifier =
                                                                    Modifier.padding(horizontal = 16.dp)
                                                    )
                                                    IconOptionRow(
                                                            icon = Icons.Outlined.AssignmentInd,
                                                            iconBg =
                                                                    MaterialTheme.colorScheme.secondary
                                                                            .copy(alpha = 0.12f),
                                                            iconTint = MaterialTheme.colorScheme.secondary,
                                                            title = "Supplier Ledger",
                                                            onClick = {
                                                                    navController.navigate(Routes.SupplierLedger)
                                                            },
                                                    )
                                                    HorizontalDivider(
                                                            modifier =
                                                                    Modifier.padding(horizontal = 16.dp)
                                                    )
                                                    IconOptionRow(
                                                            icon = Icons.Outlined.Receipt,
                                                            iconBg = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                                            iconTint = MaterialTheme.colorScheme.tertiary,
                                                            title = "GST Reports (GSTR-1)",
                                                            onClick = {
                                                                    navController.navigate(Routes.GSTReport)
                                                            },
                                                    )
                                                    IconOptionRow(
                                                            icon = Icons.Outlined.SwapHoriz,
                                                            iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                                            iconTint = MaterialTheme.colorScheme.secondary,
                                                            title = "Switch Store",
                                                            onClick = {
                                                                    activeModal = "SWITCH_STORE"
                                                                    showSheet = true
                                                            },
                                                    )
                                                    HorizontalDivider(
                                                            modifier =
                                                                    Modifier.padding(horizontal = 16.dp)
                                                    )
                                                }

                                                if (currentUser == null) {
                                                        IconOptionRow(
                                                                icon = Icons.Outlined.CloudSync,
                                                                iconBg =
                                                                        Color(0xFFEAB308)
                                                                                .copy(
                                                                                        alpha =
                                                                                                0.12f
                                                                                ),
                                                                iconTint = Color(0xFFEAB308),
                                                                title =
                                                                        stringResource(
                                                                                id =
                                                                                        R.string
                                                                                                .more_cloud_sync
                                                                        ),
                                                                onClick = {
                                                                        navController.navigate(
                                                                                Routes.Auth
                                                                        )
                                                                },
                                                        )
                                                } else {
                                                        IconOptionRow(
                                                                icon =
                                                                        Icons.AutoMirrored.Outlined
                                                                                .ExitToApp,
                                                                iconBg =
                                                                        MaterialTheme.colorScheme.error
                                                                                .copy(
                                                                                        alpha =
                                                                                                0.12f
                                                                                ),
                                                                iconTint = MaterialTheme.colorScheme.error,
                                                                title = "Logout",
                                                                onClick = {
                                                                        showLogoutConfirmation = true
                                                                },
                                                        )
                                                }
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )

                                                IconOptionRow(
                                                        icon = Icons.Outlined.Storage,
                                                        iconBg =
                                                                MaterialTheme.colorScheme.error
                                                                        .copy(alpha = 0.12f),
                                                        iconTint = MaterialTheme.colorScheme.error,
                                                        title = "Seed Dummy Data",
                                                        onClick = {
                                                                viewModel.seedDummyData()
                                                                android.widget.Toast.makeText(
                                                                                context,
                                                                                "Dummy data injected",
                                                                                android.widget.Toast
                                                                                        .LENGTH_SHORT
                                                                        )
                                                                        .show()
                                                        },
                                                )
                                                HorizontalDivider(
                                                        modifier =
                                                                Modifier.padding(horizontal = 16.dp)
                                                )

                                                IconOptionRow(
                                                        icon = Icons.Outlined.DeleteOutline,
                                                        iconBg =
                                                                MaterialTheme.colorScheme.error
                                                                        .copy(alpha = 0.12f),
                                                        iconTint = MaterialTheme.colorScheme.error,
                                                        title = "Clear Local Data",
                                                        onClick = {
                                                                showClearDataDialog = true
                                                        },
                                                )
                                        }
                                }
                        }

                        // Version footer
                        item {
                                Box(
                                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                                        contentAlignment = Alignment.Center,
                                ) {
                                        Text(
                                                "StoreBook v1.0 · Made for भारत 🇮🇳",
                                                fontSize = 11.sp,
                                                color =
                                                        MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.35f
                                                        ),
                                                textAlign = TextAlign.Center,
                                        )
                                }
                        }
                }

                if (showUpgradeDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showUpgradeDialog = false },
                        icon = { Icon(Icons.Default.Star, contentDescription = stringResource(R.string.ui_element_desc), tint = Gold400, modifier = Modifier.size(36.dp)) },
                        title = { Text("Store Limit Reached", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                        text = { Text("You can only create up to 2 local stores on the free plan. Upgrade to Pro to create unlimited stores, sync to cloud, and access premium features!", textAlign = TextAlign.Center) },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = {
                                    showUpgradeDialog = false
                                    navController.navigate(Routes.PremiumPlans)
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Upgrade to Pro", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showUpgradeDialog = false }, modifier = Modifier.fillMaxWidth()) {
                                Text("Maybe Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                if (showLogoutConfirmation) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showLogoutConfirmation = false },
                        icon = { Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = stringResource(R.string.ui_element_desc), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp)) },
                        title = { Text("Logout Confirmation", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                        text = {
                            if (viewModel.isPremiumUser) {
                                Text("Are you sure you want to logout? Your data is securely backed up to the cloud.", textAlign = TextAlign.Center)
                            } else {
                                Text("WARNING: You are on the free tier. Your sales, purchases, and inventory data are stored locally on this device and NOT synced to the cloud. Logging out or switching accounts may result in permanent data loss. Consider upgrading to Pro to save your data safely.", textAlign = TextAlign.Center)
                            }
                        },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = {
                                    showLogoutConfirmation = false
                                    auth.signOut()
                                    currentUser = null
                                    val prefs = com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(context)
                                    prefs.edit().remove("current_store_id").remove("is_premium").apply()
                                    viewModel.refreshUserState()
                                    navController.navigate(Routes.Auth) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Logout", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showLogoutConfirmation = false }, modifier = Modifier.fillMaxWidth()) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                if (showClearDataDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showClearDataDialog = false },
                        icon = { Icon(Icons.Outlined.Restore, contentDescription = stringResource(R.string.ui_element_desc), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp)) },
                        title = { Text("Clear Local Data?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                        text = { Text("This will permanently delete all your local inventory, sales, Udhaar, and expense data from this device. Are you absolutely sure?", textAlign = TextAlign.Center) },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = {
                                    showClearDataDialog = false
                                    viewModel.clearLocalDatabase()
                                    android.widget.Toast.makeText(context, "Local data cleared successfully", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Yes, Delete All Data", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showClearDataDialog = false }, modifier = Modifier.fillMaxWidth()) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // Bottom Sheets Handler
                if (showSheet) {
                        ModalBottomSheet(
                                onDismissRequest = { showSheet = false },
                                sheetState = sheetState,
                                containerColor = MaterialTheme.colorScheme.surface,
                                dragHandle = { BottomSheetDefaults.DragHandle() },
                        ) {
                                when (activeModal) {
                                        "LANGUAGES" -> {
                                                LanguageSheetContent(
                                                        currentLang = currentLang,
                                                        onLanguageSelected = { lang ->
                                                                scope.launch {
                                                                        languageManager
                                                                                .saveLanguage(lang)
                                                                        showSheet = false
                                                                        try {
                                                                                AppCompatDelegate
                                                                                        .setApplicationLocales(
                                                                                                LocaleListCompat
                                                                                                        .forLanguageTags(
                                                                                                                lang
                                                                                                        )
                                                                                        )
                                                                        } catch (e: Exception) {
                                                                                if (e is kotlinx.coroutines.CancellationException) throw e
                                                                                activity?.let {
                                                                                        it.recreate()
                                                                                        if (Build.VERSION
                                                                                                        .SDK_INT >=
                                                                                                        Build.VERSION_CODES
                                                                                                                .UPSIDE_DOWN_CAKE
                                                                                        ) {
                                                                                                it.overrideActivityTransition(
                                                                                                        Activity.OVERRIDE_TRANSITION_OPEN,
                                                                                                        0,
                                                                                                        0
                                                                                                )
                                                                                                it.overrideActivityTransition(
                                                                                                        Activity.OVERRIDE_TRANSITION_CLOSE,
                                                                                                        0,
                                                                                                        0
                                                                                                )
                                                                                        } else {
                                                                                                @Suppress(
                                                                                                        "DEPRECATION"
                                                                                                )
                                                                                                it.overridePendingTransition(
                                                                                                        0,
                                                                                                        0
                                                                                                )
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        },
                                                )
                                        }
                                        "THEME" -> { /* Handled inline — no longer uses sheet */ }
                                        "EXPENSES" -> {
                                                ExpenseSheetContent(
                                                        expenseAmount = expenseAmount,
                                                        onAmountChange = { expenseAmount = it },
                                                        expenseDesc = expenseDesc,
                                                        onDescChange = { expenseDesc = it },
                                                        onSave = {
                                                                val amt =
                                                                        expenseAmount
                                                                                .toDoubleOrNull()
                                                                if (amt != null &&
                                                                                expenseDesc
                                                                                        .isNotBlank()
                                                                ) {
                                                                        viewModel
                                                                                .logOverheadExpense(
                                                                                        expenseDesc,
                                                                                        amt
                                                                                )
                                                                        expenseAmount = ""
                                                                        expenseDesc = ""
                                                                        showSheet = false
                                                                        android.widget.Toast
                                                                                .makeText(
                                                                                        context,
                                                                                        context.getString(
                                                                                                R.string
                                                                                                        .exp_toast_logged
                                                                                        ),
                                                                                        android.widget
                                                                                                .Toast
                                                                                                .LENGTH_SHORT,
                                                                                )
                                                                                .show()
                                                                }
                                                        },
                                                        expensesHistory = expensesList,
                                                )
                                        }
                                        "REPORTS" ->
                                                ReportsSheetContent(
                                                        salesList = salesList,
                                                        expensesList = expensesList
                                                )
                                        "RESTOCK" -> {
                                                RestockSheetContent(
                                                        allItems = allItems,
                                                        selectedItemId = selectedItemId,
                                                        onItemSelect = { selectedItemId = it },
                                                        restockQty = restockQty,
                                                        onQtyChange = { restockQty = it },
                                                        restockCostPrice = restockCostPrice,
                                                        onCostChange = { restockCostPrice = it },
                                                        restockSupplier = restockSupplier,
                                                        onSupplierChange = { restockSupplier = it },
                                                        restockPhone = restockPhone,
                                                        onPhoneChange = { restockPhone = it },
                                                        onSave = {
                                                                val id = selectedItemId
                                                                val qty =
                                                                        restockQty.toDoubleOrNull()
                                                                val cost =
                                                                        restockCostPrice
                                                                                .toDoubleOrNull()
                                                                if (id != null &&
                                                                                qty != null &&
                                                                                cost != null
                                                                ) {
                                                                        viewModel.logRestockItem(
                                                                                id,
                                                                                qty,
                                                                                cost,
                                                                                restockSupplier,
                                                                                restockPhone
                                                                        )
                                                                        restockQty = ""
                                                                        restockCostPrice = ""
                                                                        restockSupplier = ""
                                                                        restockPhone = ""
                                                                        selectedItemId = null
                                                                        showSheet = false
                                                                        android.widget.Toast
                                                                                .makeText(
                                                                                        context,
                                                                                        context.getString(
                                                                                                R.string
                                                                                                        .exp_toast_logged
                                                                                        ),
                                                                                        android.widget
                                                                                                .Toast
                                                                                                .LENGTH_SHORT,
                                                                                )
                                                                                .show()
                                                                }
                                                        },
                                                )
                                        }
                                        "PREMIUM" -> {
                                                ProBillingView(
                                                        isProActive = viewModel.isPremiumUser,
                                                        onRequireSignIn = {
                                                                showSheet = false
                                                                navController.navigate(Routes.Auth)
                                                        },
                                                        onDismiss = { showSheet = false }
                                                )
                                        }
                                        "BUSINESS" -> {
                                                var nameInput by remember {
                                                        mutableStateOf(viewModel.businessName)
                                                }
                                                var gstinInput by remember {
                                                        mutableStateOf(viewModel.businessGstin)
                                                }
                                                val focusRequesterGstin = remember { FocusRequester() }
                                                val focusRequesterAddress = remember { FocusRequester() }
                                                val focusManager = LocalFocusManager.current
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .verticalScroll(
                                                                                rememberScrollState()
                                                                        )
                                                                        .padding(
                                                                                horizontal = 24.dp,
                                                                                vertical = 16.dp,
                                                                        )
                                                                        .padding(bottom = 32.dp),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp),
                                                ) {
                                                        Text(
                                                                "Business Settings",
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                        OutlinedTextField(
                                                                value = nameInput,
                                                                onValueChange = { nameInput = it },
                                                                label = {
                                                                        Text("Store Owner's Name")
                                                                },
                                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                                                keyboardActions = KeyboardActions(onNext = { focusRequesterGstin.requestFocus() }),
                                                                modifier = Modifier.fillMaxWidth(),
                                                                singleLine = true,
                                                                shape = RoundedCornerShape(12.dp),
                                                        )
                                                        OutlinedTextField(
                                                                value = gstinInput,
                                                                onValueChange = { gstinInput = it },
                                                                label = {
                                                                        Text("Store Owner's GSTIN")
                                                                },
                                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                                                keyboardActions = KeyboardActions(onNext = { focusRequesterAddress.requestFocus() }),
                                                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterGstin),
                                                                singleLine = true,
                                                                shape = RoundedCornerShape(12.dp),
                                                        )
                                                        var addressInput by remember {
                                                                mutableStateOf(
                                                                        viewModel.businessAddress
                                                                )
                                                        }
                                                        OutlinedTextField(
                                                                value = addressInput,
                                                                onValueChange = {
                                                                        addressInput = it
                                                                },
                                                                label = {
                                                                        Text(
                                                                                "Store Owner's Address"
                                                                        )
                                                                },
                                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterAddress),
                                                                minLines = 2,
                                                                maxLines = 4,
                                                                shape = RoundedCornerShape(12.dp),
                                                        )
                                                        var currencyInput by remember {
                                                                mutableStateOf(viewModel.businessCurrency)
                                                        }
                                                        var showCurrencyDropdown by remember {
                                                                mutableStateOf(false)
                                                        }
                                                        Box(modifier = Modifier.fillMaxWidth()) {
                                                            OutlinedTextField(
                                                                value = when (currencyInput) {
                                                                    "INR" -> "Indian Rupee (₹)"
                                                                    "USD" -> "US Dollar ($)"
                                                                    "EUR" -> "Euro (€)"
                                                                    "GBP" -> "British Pound (£)"
                                                                    "JPY" -> "Japanese Yen (¥)"
                                                                    "CNY" -> "Chinese Yuan (元)"
                                                                    else -> currencyInput
                                                                },
                                                                onValueChange = {},
                                                                readOnly = true,
                                                                label = { Text("Business Currency") },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                shape = RoundedCornerShape(12.dp)
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .matchParentSize()
                                                                    .clickable(onClickLabel = "Action") { showCurrencyDropdown = true }
                                                            )
                                                            DropdownMenu(
                                                                expanded = showCurrencyDropdown,
                                                                onDismissRequest = { showCurrencyDropdown = false }
                                                            ) {
                                                                listOf(
                                                                    "INR" to "Indian Rupee (₹)",
                                                                    "USD" to "US Dollar ($)",
                                                                    "EUR" to "Euro (€)",
                                                                    "GBP" to "British Pound (£)",
                                                                    "JPY" to "Japanese Yen (¥)",
                                                                    "CNY" to "Chinese Yuan (元)"
                                                                ).forEach { (code, label) ->
                                                                    DropdownMenuItem(
                                                                        text = { Text(label) },
                                                                        onClick = {
                                                                            currencyInput = code
                                                                            showCurrencyDropdown = false
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        var useThermalPrinter by remember {
                                                                mutableStateOf(
                                                                        com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(context)
                                                                                .getBoolean("use_thermal_printer", false)
                                                                )
                                                        }
                                                        var useHapticFeedback by remember {
                                                                mutableStateOf(viewModel.isHapticFeedbackEnabled)
                                                        }
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Action") { useThermalPrinter = !useThermalPrinter }.padding(vertical = 4.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                        Text("Use Thermal Printer Format", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                                        Text("Generate 3-inch receipts instead of A4 invoices", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                }
                                                                androidx.compose.material3.Switch(
                                                                        checked = useThermalPrinter,
                                                                        onCheckedChange = { useThermalPrinter = it }
                                                                )
                                                        }
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Action") { useHapticFeedback = !useHapticFeedback }.padding(vertical = 4.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                        Text("Enable Haptic Feedback", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                                        Text("Provide tactile vibration on item additions/updates", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                }
                                                                androidx.compose.material3.Switch(
                                                                        checked = useHapticFeedback,
                                                                        onCheckedChange = { useHapticFeedback = it }
                                                                )
                                                        }
                                                        PrimaryButton(
                                                                onClick = {
                                                                        val trimmedGstin = gstinInput.trim().uppercase()
                                                                        if (trimmedGstin.isNotEmpty() && !trimmedGstin.matches(Regex("\\d{2}[A-Z]{5}\\d{4}[A-Z]{1}[A-Z\\d]{1}[Z]{1}[A-Z\\d]{1}"))) {
                                                                                android.widget.Toast.makeText(context, "Invalid GSTIN format (e.g. 22AAAAA0000A1Z5)", android.widget.Toast.LENGTH_SHORT).show()
                                                                                return@PrimaryButton
                                                                        }
                                                                        com.storebook.inventoryapp.utils.SecurityUtils.getEncryptedPrefs(context)
                                                                                .edit()
                                                                                .putBoolean("use_thermal_printer", useThermalPrinter)
                                                                                .apply()
                                                                        viewModel.updateHapticFeedbackEnabled(useHapticFeedback)
                                                                        viewModel
                                                                                .updateBusinessName(
                                                                                        nameInput
                                                                                )
                                                                        viewModel
                                                                                .updateBusinessGstin(
                                                                                        trimmedGstin
                                                                                )
                                                                        viewModel
                                                                                .updateBusinessAddress(
                                                                                        addressInput
                                                                                )
                                                                        viewModel
                                                                                .updateBusinessCurrency(
                                                                                        currencyInput
                                                                                )
                                                                        showSheet = false
                                                                        android.widget.Toast
                                                                                .makeText(
                                                                                        context,
                                                                                        "Settings saved",
                                                                                        android.widget
                                                                                                .Toast
                                                                                                .LENGTH_SHORT,
                                                                                )
                                                                                .show()
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .height(52.dp),
                                                                shape = RoundedCornerShape(14.dp),
                                                        ) {
                                                                Text(
                                                                        stringResource(
                                                                                id =
                                                                                        R.string
                                                                                                .btn_save
                                                                        ),
                                                                        fontWeight = FontWeight.Bold
                                                                )
                                                        }
                                                }
                                        }
                                        "SWITCH_STORE" -> {
                                            var newStoreIdInput by remember { mutableStateOf("") }
                                            Column(
                                                    modifier =
                                                            Modifier.fillMaxWidth()
                                                                    .padding(
                                                                            horizontal = 24.dp,
                                                                            vertical = 16.dp,
                                                                    )
                                                                    .padding(bottom = 32.dp),
                                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                            ) {
                                                Text(
                                                        "Switch Active Store",
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Current Store: ${viewModel.getStoreName(viewModel.activeStoreId)}",
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )

                                                if (viewModel.userStores.isNotEmpty()) {
                                                    Text("Your Associated Stores:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                                    viewModel.userStores.forEach { sId ->
                                                        val storeName = viewModel.getStoreName(sId)
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Action") {
                                                                viewModel.switchStore(sId)
                                                                showSheet = false
                                                                android.widget.Toast.makeText(context, "Switched to $storeName", android.widget.Toast.LENGTH_SHORT).show()
                                                            },
                                                            colors = CardDefaults.cardColors(containerColor = if (sId == viewModel.activeStoreId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                                            shape = RoundedCornerShape(12.dp)
                                                        ) {
                                                            Text(storeName, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = if (sId == viewModel.activeStoreId) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    }
                                                }

                                                OutlinedTextField(
                                                        value = newStoreIdInput,
                                                        onValueChange = { newStoreIdInput = it },
                                                        label = { Text("Or enter Store ID manually") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                )
                                                PrimaryButton(
                                                        onClick = {
                                                            if (newStoreIdInput.isNotBlank()) {
                                                                if (!viewModel.userStores.contains(newStoreIdInput) && !viewModel.isPremiumUser && viewModel.userStores.size >= 2) {
                                                                    showUpgradeDialog = true
                                                                } else {
                                                                    viewModel.switchStore(newStoreIdInput)
                                                                    showSheet = false
                                                                    android.widget.Toast.makeText(
                                                                            context,
                                                                            "Switched to $newStoreIdInput",
                                                                            android.widget.Toast.LENGTH_SHORT,
                                                                    ).show()
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                                        shape = RoundedCornerShape(14.dp),
                                                ) {
                                                    Text("Switch / Create Local Store", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun IconOptionRow(
        icon: ImageVector,
        iconBg: Color,
        iconTint: Color,
        title: String,
        trailing: String? = null,
        trailingIconRes: Int? = null,
        onClick: () -> Unit,
) {
        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .clickable(onClickLabel = "Action") { onClick() }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
        ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                                modifier =
                                        Modifier.size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ),
                                contentAlignment = Alignment.Center,
                        ) {
                                Icon(
                                        imageVector = icon,
                                        contentDescription = stringResource(R.string.ui_element_desc),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                        if (trailingIconRes != null) {
                                Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = trailingIconRes),
                                        contentDescription = stringResource(R.string.ui_element_desc),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (trailing != null) {
                                Text(
                                        text = trailing,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 4.dp),
                                )
                        }
                        Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.ui_element_desc),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                modifier = Modifier.size(20.dp),
                        )
                }
        }
}

// Legacy OptionRow - kept for compatibility
@Composable
fun OptionRow(
        title: String,
        trailing: String? = null,
        onClick: () -> Unit,
) {
        IconOptionRow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = title,
                trailing = trailing,
                onClick = onClick,
        )
}

// --- Sheet Inner Content ---

@Composable
fun LanguageSheetContent(
        currentLang: String,
        onLanguageSelected: (String) -> Unit,
) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
                Text(
                        stringResource(id = R.string.more_language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                LanguageOptionCard("English", "en", R.drawable.ic_english, currentLang == "en", onLanguageSelected)
                LanguageOptionCard(
                        "हिंदी (Hindi)",
                        "hi",
                        R.drawable.ic_hindi,
                        currentLang == "hi",
                        onLanguageSelected
                )
                LanguageOptionCard(
                        "ગુજરાતી (Gujarati)",
                        "gu",
                        R.drawable.ic_gujarati,
                        currentLang == "gu",
                        onLanguageSelected
                )
        }
}

@Composable
fun LanguageOptionCard(
        label: String,
        code: String,
        iconRes: Int,
        active: Boolean,
        onSelect: (String) -> Unit,
) {
        Card(
                modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Action") { onSelect(code) },
                shape = RoundedCornerShape(14.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        if (active) {
                                                MaterialTheme.colorScheme.primary
                                        } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                                        alpha = 0.5f
                                                )
                                        },
                                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
        ) {
                Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = iconRes),
                                        contentDescription = stringResource(R.string.ui_element_desc),
                                        tint = androidx.compose.material3.LocalContentColor.current,
                                        modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        if (active) {
                                Box(
                                        modifier =
                                                Modifier.size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                                MaterialTheme.colorScheme.primary
                                                        ),
                                        contentAlignment = Alignment.Center,
                                ) {
                                        Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                        )
                                }
                        }
                }
        }
}


@Composable
fun InlineThemeCard(
    isDarkMode: Boolean,
    themeMode: AppThemeMode,
    isPremium: Boolean,
    onThemeSelected: (Boolean) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val palettes = listOf(
        Triple(AppThemeMode.INK_BLUE,          com.storebook.inventoryapp.ui.theme.InkBlue700, "Sapphire Blue"),
        Triple(AppThemeMode.FOREST_GREEN,      com.storebook.inventoryapp.ui.theme.ForestGreen700, "Emerald Jade"),
        Triple(AppThemeMode.SUNSET_ORANGE,     com.storebook.inventoryapp.ui.theme.SunsetOrange700, "Sunset Amber"),
        Triple(AppThemeMode.AMETHYST_PURPLE,   com.storebook.inventoryapp.ui.theme.AmethystPurple700, "Royal Amethyst"),
        Triple(AppThemeMode.CRIMSON_RUBY,      com.storebook.inventoryapp.ui.theme.CrimsonRuby700, "Crimson Ruby"),
        Triple(AppThemeMode.CHARCOAL_OBSIDIAN, com.storebook.inventoryapp.ui.theme.CharcoalObsidian700, "Charcoal Obsidian"),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Section 1: Mode Toggle ──────────────────────────────
            Text(
                text = "Mode",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf(
                    Triple(false, R.drawable.ic_light_mode, "Light"),
                    Triple(true,  R.drawable.ic_dark_mode, "Dark"),
                ).forEach { (dark, emoji, label) ->
                    val selected = isDarkMode == dark
                    val bgColor by animateColorAsState(
                        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = tween(200), label = "mode_bg"
                    )
                    val textColor by animateColorAsState(
                        if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(200), label = "mode_text"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .clickable(onClickLabel = "Action") { onThemeSelected(dark) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = emoji as Int),
                                contentDescription = stringResource(R.string.ui_element_desc),
                                tint = textColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label as String,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                            )
                        }
                    }
                }
            }

            // ── Section 2: Color Palette Swatches ──────────────────
            Text(
                text = "Color Palette",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp),
            )

            val lazyListState = rememberLazyListState()

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                LazyRow(
                    state = lazyListState,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 4.dp, end = 40.dp),
                ) {
                    items(palettes) { (mode, color, name) ->
                        val isSelected = themeMode == mode
                        val isLocked = !isPremium && mode != AppThemeMode.INK_BLUE
                        val ringAlpha by animateColorAsState(
                            if (isSelected && !isLocked) color else Color.Transparent,
                            animationSpec = tween(250), label = "ring_${name}"
                        )
                        val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = if (isLocked) {
                                Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    android.widget.Toast.makeText(context, "Only available for Pro users", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Modifier.clickable(onClickLabel = "Action") { onThemeModeSelected(mode) }
                            },
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(BorderStroke(3.dp, ringAlpha), CircleShape)
                                    .padding(4.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                ) {
                                    if (isSelected && !isLocked) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = stringResource(R.string.ui_element_desc),
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .align(Alignment.Center),
                                        )
                                    } else if (isLocked) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.pdf_detail_lock),
                                                contentDescription = "Locked",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Scroll Indicator Arrow (fade gradient + chevron)
                if (lazyListState.canScrollForward) {
                    Box(
                        modifier = Modifier
                            .height(80.dp)
                            .width(50.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "More colors available",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(end = 4.dp, bottom = 22.dp)
                                .size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSheetContent(
    isDarkMode: Boolean,
    themeMode: AppThemeMode,
    isPremium: Boolean,
    onThemeSelected: (Boolean) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
) {
    // Legacy — delegates to inline card for backward compat
    InlineThemeCard(isDarkMode, themeMode, isPremium, onThemeSelected, onThemeModeSelected)
}

@Composable
fun ExpenseSheetContent(
        expenseAmount: String,
        onAmountChange: (String) -> Unit,
        expenseDesc: String,
        onDescChange: (String) -> Unit,
        onSave: () -> Unit,
        expensesHistory: List<ExpenseEntry>,
) {
        val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
        val focusRequesterDesc = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        Column(
                modifier =
                        Modifier.fillMaxWidth()
                                .heightIn(max = 500.dp)
                                .imePadding()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
                Text(
                        stringResource(id = R.string.exp_overhead_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                                value = expenseAmount,
                                onValueChange = onAmountChange,
                                label = { Text(stringResource(id = R.string.exp_amount_label)) },
                                keyboardOptions =
                                        KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                        ),
                                keyboardActions = KeyboardActions(onNext = { focusRequesterDesc.requestFocus() }),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                        )
                        OutlinedTextField(
                                value = expenseDesc,
                                onValueChange = onDescChange,
                                label = { Text(stringResource(id = R.string.exp_desc_label)) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.weight(1.5f).focusRequester(focusRequesterDesc),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                        )
                }

                PrimaryButton(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                ) { Text(stringResource(id = R.string.btn_save), fontWeight = FontWeight.Bold) }

                Text(
                        stringResource(id = R.string.more_recent_expenses),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                )

                LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                        items(expensesHistory) { entry ->
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant.copy(
                                                                        alpha = 0.5f
                                                                ),
                                                ),
                                        shape = RoundedCornerShape(12.dp),
                                ) {
                                        Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                                Column {
                                                        Text(
                                                                entry.description,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp
                                                        )
                                                        Text(
                                                                dateFmt.format(
                                                                        Date(entry.timestamp)
                                                                ),
                                                                fontSize = 11.sp,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface.copy(
                                                                                alpha = 0.45f
                                                                        ),
                                                        )
                                                }
                                                Text(
                                                        "${entry.amount.toRupee()}",
                                                        fontWeight = FontWeight.Black,
                                                        color = Coral500
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
fun ReportsSheetContent(
        salesList: List<Sale>,
        expensesList: List<ExpenseEntry>,
) {
        val totalRevenue = salesList.sumOf { it.totalAmount }
        val totalProfit =
                salesList.sumOf { sale ->
                        sale.items.sumOf { (it.sellPrice - it.buyPrice) * it.quantity } -
                                sale.discountAmount
                }
        val totalOverheadExpenses =
                expensesList.filter { it.type == "OVERHEAD" }.sumOf { it.amount }
        val netProfit = totalProfit - totalOverheadExpenses
        val maxVal = maxOf(totalRevenue, totalProfit, totalOverheadExpenses, 1.0)

        Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
                Text(
                        stringResource(id = R.string.rep_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                )
                Text(
                        text = stringResource(id = R.string.rep_pnl_sub),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
                HorizontalDivider()

                // Bar chart rows
                ReportBarRow(
                        stringResource(id = R.string.rep_revenue_label),
                        totalRevenue,
                        maxVal,
                        MaterialTheme.colorScheme.primary
                )
                ReportBarRow(
                        stringResource(id = R.string.rep_product_profit_label),
                        totalProfit,
                        maxVal,
                        Emerald500
                )
                ReportBarRow(
                        stringResource(id = R.string.rep_expenses_label),
                        totalOverheadExpenses,
                        maxVal,
                        Coral500
                )

                HorizontalDivider()

                // Net profit
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor =
                                                if (netProfit >= 0) Emerald500.copy(alpha = 0.1f)
                                                else Coral500.copy(alpha = 0.1f),
                                ),
                ) {
                        Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                        ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                                imageVector = if (netProfit >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                                contentDescription = stringResource(R.string.ui_element_desc),
                                                tint = if (netProfit >= 0) Emerald500 else Coral500,
                                                modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                                text = if (netProfit >= 0) stringResource(id = R.string.rep_net_profit_label) else stringResource(id = R.string.rep_net_loss_label),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                        )
                                }
                                Text(
                                        text = "${netProfit.toRupee()}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = if (netProfit >= 0) Emerald500 else Coral500,
                                )
                        }
                }
        }
}

@Composable
fun ReportBarRow(
        label: String,
        value: Double,
        maxVal: Double,
        color: Color,
) {
        val fraction = (value / maxVal).toFloat().coerceIn(0f, 1f)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(
                                "${value.toRupee()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                        )
                }
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth(fraction)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(color),
                        )
                }
        }
}

@Composable
fun ReportMetricRow(
        label: String,
        color: Color,
) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockSheetContent(
        allItems: List<Item>,
        selectedItemId: Long?,
        onItemSelect: (Long) -> Unit,
        restockQty: String,
        onQtyChange: (String) -> Unit,
        restockCostPrice: String,
        onCostChange: (String) -> Unit,
        restockSupplier: String,
        onSupplierChange: (String) -> Unit,
        restockPhone: String,
        onPhoneChange: (String) -> Unit,
        onSave: () -> Unit,
) {
        val focusRequesterCost = remember { androidx.compose.ui.focus.FocusRequester() }
        val focusRequesterSupplier = remember { androidx.compose.ui.focus.FocusRequester() }
        val focusRequesterPhone = remember { androidx.compose.ui.focus.FocusRequester() }
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

        Column(
                modifier =
                        Modifier.fillMaxWidth()
                                .heightIn(max = 500.dp)
                                .imePadding()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
                Text(
                        stringResource(id = R.string.exp_restock_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                )

                var itemDropdownExpanded by remember { mutableStateOf(false) }
                val selectedItem = allItems.find { it.id == selectedItemId }

                ExposedDropdownMenuBox(
                        expanded = itemDropdownExpanded,
                        onExpandedChange = { itemDropdownExpanded = !itemDropdownExpanded },
                        modifier = Modifier.fillMaxWidth(),
                ) {
                        OutlinedTextField(
                                readOnly = true,
                                value = selectedItem?.name ?: "Select Stock Item...",
                                onValueChange = {},
                                label = { Text(stringResource(id = R.string.more_stock_item)) },
                                trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = itemDropdownExpanded
                                        )
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier =
                                        Modifier.menuAnchor()
                                                .fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(
                                expanded = itemDropdownExpanded,
                                onDismissRequest = { itemDropdownExpanded = false }
                        ) {
                                allItems.forEach { item ->
                                        DropdownMenuItem(
                                                text = { Text(item.name) },
                                                onClick = {
                                                        onItemSelect(item.id)
                                                        itemDropdownExpanded = false
                                                }
                                        )
                                }
                        }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                                value = restockQty,
                                onValueChange = onQtyChange,
                                label = { Text(stringResource(id = R.string.exp_restock_qty)) },
                                keyboardOptions =
                                        KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = androidx.compose.ui.text.input.ImeAction.Next
                                        ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                        onNext = { focusRequesterCost.requestFocus() }
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                        )
                        OutlinedTextField(
                                value = restockCostPrice,
                                onValueChange = onCostChange,
                                label = { Text(stringResource(id = R.string.exp_cost_price)) },
                                keyboardOptions =
                                        KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = androidx.compose.ui.text.input.ImeAction.Next
                                        ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                        onNext = { focusRequesterSupplier.requestFocus() }
                                ),
                                modifier = Modifier.weight(1f).focusRequester(focusRequesterCost),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                        )
                }

                OutlinedTextField(
                        value = restockSupplier,
                        onValueChange = onSupplierChange,
                        label = { Text(stringResource(id = R.string.exp_supplier_label)) },
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onNext = { focusRequesterPhone.requestFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterSupplier),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                        value = restockPhone,
                        onValueChange = onPhoneChange,
                        label = { Text(stringResource(id = R.string.exp_supplier_phone)) },
                        keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequesterPhone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                )

                PrimaryButton(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                ) { Text(stringResource(id = R.string.btn_save), fontWeight = FontWeight.Bold) }
        }
}
