package com.storebook.inventoryapp.ui.screens.storebook

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.storebook.inventoryapp.data.repository.ExpenseEntry
import com.storebook.inventoryapp.data.repository.Item
import com.storebook.inventoryapp.data.repository.Sale
import com.storebook.inventoryapp.ui.navigation.Routes
import com.storebook.inventoryapp.ui.theme.Coral500
import com.storebook.inventoryapp.ui.theme.Emerald500
import com.storebook.inventoryapp.ui.theme.Gold200
import com.storebook.inventoryapp.ui.theme.Gold400
import com.storebook.inventoryapp.ui.theme.InkBlue500
import com.storebook.inventoryapp.ui.theme.InkBlue700
import com.storebook.inventoryapp.ui.viewmodels.StoreBookViewModel
import com.storebook.inventoryapp.utils.LanguageManager
import com.storebook.inventoryapp.utils.toRupee
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

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
    val currentLang by languageManager.appLanguage.collectAsStateWithLifecycle(initialValue = "en")

    val salesList by viewModel.salesList.collectAsStateWithLifecycle()
    val expensesList by viewModel.expensesList.collectAsStateWithLifecycle()
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()

    var activeModal by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                                                                R.string.toast_csv_imported
                                                        ),
                                                        android.widget.Toast.LENGTH_SHORT,
                                                )
                                                .show()
                                    },
                                    onError = { err ->
                                        android.widget.Toast.makeText(
                                                        context,
                                                        err,
                                                        android.widget.Toast.LENGTH_SHORT
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
                                                                R.string.toast_csv_exported
                                                        ),
                                                        android.widget.Toast.LENGTH_SHORT,
                                                )
                                                .show()
                                    },
                                    onError = { err ->
                                        android.widget.Toast.makeText(
                                                        context,
                                                        err,
                                                        android.widget.Toast.LENGTH_SHORT
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
                                                Brush.linearGradient(listOf(InkBlue700, InkBlue500))
                                        )
                                        .padding(top = paddingValues.calculateTopPadding())
                                        .padding(horizontal = 20.dp, vertical = 20.dp),
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
                                                .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                        ) { Text("🏪", fontSize = 26.sp) }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                    text = stringResource(id = R.string.app_name),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White,
                            )
                            Text(
                                    text = "${allItems.size} items · ${salesList.size} total sales",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
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
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                        .clickable {
                                            activeModal = "PREMIUM"
                                            showSheet = true
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
                                                .background(Gold200.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Gold400,
                                    modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                    text = stringResource(id = R.string.more_pro_plans),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFFB8860B),
                            )
                            Text(
                                    text = stringResource(id = R.string.more_pro_desc),
                                    fontSize = 12.sp,
                                    color = Color(0xFFB8860B).copy(alpha = 0.75f),
                            )
                        }
                        Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Gold400
                        )
                    }
                }
            }

            // Settings options group
            item {
                Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column {
                        IconOptionRow(
                                icon = Icons.Outlined.Language,
                                iconBg = InkBlue500.copy(alpha = 0.12f),
                                iconTint = InkBlue500,
                                title = stringResource(id = R.string.more_language),
                                trailing =
                                        when (currentLang) {
                                            "hi" -> "🇮🇳 हिंदी"
                                            "gu" -> "🪁 ગુજ"
                                            else -> "🌐 English"
                                        },
                                onClick = {
                                    activeModal = "LANGUAGES"
                                    showSheet = true
                                },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        IconOptionRow(
                                icon = Icons.Outlined.PieChart,
                                iconBg = Emerald500.copy(alpha = 0.12f),
                                iconTint = Emerald500,
                                title = stringResource(id = R.string.more_pnl_report),
                                onClick = {
                                    activeModal = "REPORTS"
                                    showSheet = true
                                },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        IconOptionRow(
                                icon = Icons.Outlined.Checklist,
                                iconBg = InkBlue500.copy(alpha = 0.12f),
                                iconTint = InkBlue500,
                                title = "Sales Analytics",
                                onClick = { navController.navigate(Routes.SalesAnalytics) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        IconOptionRow(
                                icon = Icons.Outlined.MoneyOff,
                                iconBg = Coral500.copy(alpha = 0.12f),
                                iconTint = Coral500,
                                title = stringResource(id = R.string.more_expense_track),
                                onClick = {
                                    activeModal = "EXPENSES"
                                    showSheet = true
                                },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        IconOptionRow(
                                icon = Icons.Outlined.LocalShipping,
                                iconBg = Color(0xFF7C3AED).copy(alpha = 0.12f),
                                iconTint = Color(0xFF7C3AED),
                                title = stringResource(id = R.string.exp_restock_title),
                                onClick = {
                                    activeModal = "RESTOCK"
                                    showSheet = true
                                },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        IconOptionRow(
                                icon = Icons.Outlined.Share,
                                iconBg = Color(0xFF0EA5E9).copy(alpha = 0.12f),
                                iconTint = Color(0xFF0EA5E9),
                                title = stringResource(id = R.string.more_csv_export),
                                onClick = {
                                    csvExportLauncher.launch(
                                            "StoreBook_Inventory_${System.currentTimeMillis() / 1000}.csv"
                                    )
                                },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        IconOptionRow(
                                icon = Icons.Outlined.DownloadForOffline,
                                iconBg = Color(0xFF0EA5E9).copy(alpha = 0.12f),
                                iconTint = Color(0xFF0EA5E9),
                                title = stringResource(id = R.string.more_csv_import),
                                onClick = {
                                    csvFilePickerLauncher.launch(
                                            arrayOf("text/*", "application/csv", "text/csv"),
                                    )
                                },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        IconOptionRow(
                                icon = Icons.Outlined.Store,
                                iconBg = Color(0xFF10B981).copy(alpha = 0.12f),
                                iconTint = Color(0xFF10B981),
                                title = "Business Settings",
                                onClick = {
                                    activeModal = "BUSINESS"
                                    showSheet = true
                                },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        IconOptionRow(
                                icon = Icons.Outlined.CloudSync,
                                iconBg = Color(0xFFEAB308).copy(alpha = 0.12f),
                                iconTint = Color(0xFFEAB308),
                                title = stringResource(id = R.string.more_cloud_sync),
                                onClick = { navController.navigate(Routes.Auth) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        IconOptionRow(
                                icon = Icons.Outlined.Restore,
                                iconBg = Color(0xFFEF4444).copy(alpha = 0.12f),
                                iconTint = Color(0xFFEF4444),
                                title = "Seed Dummy Data",
                                onClick = { 
                                    viewModel.seedDummyData()
                                    android.widget.Toast.makeText(context, "Dummy data injected", android.widget.Toast.LENGTH_SHORT).show()
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
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center,
                    )
                }
            }
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
                                        languageManager.saveLanguage(lang)
                                        showSheet = false
                                        try {
                                            AppCompatDelegate.setApplicationLocales(
                                                    LocaleListCompat.forLanguageTags(lang)
                                            )
                                        } catch (e: Exception) {
                                            activity?.let {
                                                it.recreate()
                                                if (Build.VERSION.SDK_INT >=
                                                                Build.VERSION_CODES.UPSIDE_DOWN_CAKE
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
                                                    @Suppress("DEPRECATION")
                                                    it.overridePendingTransition(0, 0)
                                                }
                                            }
                                        }
                                    }
                                },
                        )
                    }
                    "EXPENSES" -> {
                        ExpenseSheetContent(
                                expenseAmount = expenseAmount,
                                onAmountChange = { expenseAmount = it },
                                expenseDesc = expenseDesc,
                                onDescChange = { expenseDesc = it },
                                onSave = {
                                    val amt = expenseAmount.toDoubleOrNull()
                                    if (amt != null && expenseDesc.isNotBlank()) {
                                        viewModel.logOverheadExpense(expenseDesc, amt)
                                        expenseAmount = ""
                                        expenseDesc = ""
                                        showSheet = false
                                        android.widget.Toast.makeText(
                                                        context,
                                                        context.getString(
                                                                R.string.exp_toast_logged
                                                        ),
                                                        android.widget.Toast.LENGTH_SHORT,
                                                )
                                                .show()
                                    }
                                },
                                expensesHistory = expensesList,
                        )
                    }
                    "REPORTS" ->
                            ReportsSheetContent(salesList = salesList, expensesList = expensesList)
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
                                    val qty = restockQty.toDoubleOrNull()
                                    val cost = restockCostPrice.toDoubleOrNull()
                                    if (id != null && qty != null && cost != null) {
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
                                        android.widget.Toast.makeText(
                                                        context,
                                                        context.getString(
                                                                R.string.exp_toast_logged
                                                        ),
                                                        android.widget.Toast.LENGTH_SHORT,
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
                            onDismiss = {
                                showSheet = false
                            }
                        )
                    }
                    "BUSINESS" -> {
                        var nameInput by remember { mutableStateOf(viewModel.businessName) }
                        var gstinInput by remember { mutableStateOf(viewModel.businessGstin) }
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .verticalScroll(rememberScrollState())
                                                .padding(
                                                        horizontal = 24.dp,
                                                        vertical = 16.dp,
                                                )
                                                .padding(bottom = 32.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                    "Business Settings",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("Store Owner's Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                            )
                            OutlinedTextField(
                                    value = gstinInput,
                                    onValueChange = { gstinInput = it },
                                    label = { Text("Store Owner's GSTIN") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                            )
                            var addressInput by remember {
                                mutableStateOf(viewModel.businessAddress)
                            }
                            OutlinedTextField(
                                    value = addressInput,
                                    onValueChange = { addressInput = it },
                                    label = { Text("Store Owner's Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    maxLines = 4,
                                    shape = RoundedCornerShape(12.dp),
                            )
                            Button(
                                    onClick = {
                                        viewModel.updateBusinessName(nameInput)
                                        viewModel.updateBusinessGstin(gstinInput)
                                        viewModel.updateBusinessAddress(addressInput)
                                        showSheet = false
                                        android.widget.Toast.makeText(
                                                        context,
                                                        "Settings saved",
                                                        android.widget.Toast.LENGTH_SHORT,
                                                )
                                                .show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(
                                        stringResource(id = R.string.btn_save),
                                        fontWeight = FontWeight.Bold
                                )
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
        onClick: () -> Unit,
) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable { onClick() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                    modifier =
                            Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
                    contentAlignment = Alignment.Center,
            ) {
                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    contentDescription = null,
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
        LanguageOptionCard("🌐 English", "en", currentLang == "en", onLanguageSelected)
        LanguageOptionCard("🇮🇳 हिंदी (Hindi)", "hi", currentLang == "hi", onLanguageSelected)
        LanguageOptionCard("🪁 ગુજરાતી (Gujarati)", "gu", currentLang == "gu", onLanguageSelected)
    }
}

@Composable
fun LanguageOptionCard(
        label: String,
        code: String,
        active: Boolean,
        onSelect: (String) -> Unit,
) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable { onSelect(code) },
            shape = RoundedCornerShape(14.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    if (active) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                    ),
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if (active) {
                Box(
                        modifier =
                                Modifier.size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                ) {
                    Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// Legacy LanguageRow - kept for compatibility
@Composable
fun LanguageRow(
        label: String,
        code: String,
        active: Boolean,
        onSelect: (String) -> Unit,
) {
    LanguageOptionCard(label, code, active, onSelect)
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
            )
            OutlinedTextField(
                    value = expenseDesc,
                    onValueChange = onDescChange,
                    label = { Text(stringResource(id = R.string.exp_desc_label)) },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
            )
        }

        Button(
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
                                                MaterialTheme.colorScheme.surfaceVariant.copy(
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
                            Text(entry.description, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                    dateFmt.format(Date(entry.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
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
    val totalOverheadExpenses = expensesList.filter { it.type == "OVERHEAD" }.sumOf { it.amount }
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
                InkBlue500
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
                Text(
                        text =
                                if (netProfit >= 0) {
                                    "🎉 " + stringResource(id = R.string.rep_net_profit_label)
                                } else {
                                    "⚠️ " + stringResource(id = R.string.rep_net_loss_label)
                                },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                )
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
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemDropdownExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier =
                            Modifier.menuAnchor(
                                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable
                                    )
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
            )
            OutlinedTextField(
                    value = restockCostPrice,
                    onValueChange = onCostChange,
                    label = { Text(stringResource(id = R.string.exp_cost_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
            )
        }

        OutlinedTextField(
                value = restockSupplier,
                onValueChange = onSupplierChange,
                label = { Text(stringResource(id = R.string.exp_supplier_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
        )
        OutlinedTextField(
                value = restockPhone,
                onValueChange = onPhoneChange,
                label = { Text(stringResource(id = R.string.exp_supplier_phone)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
        )

        Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
        ) { Text(stringResource(id = R.string.btn_save), fontWeight = FontWeight.Bold) }
    }
}

