package com.storebook.inventoryapp.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.storebook.inventoryapp.shared.domain.models.InvoiceSettings

private val TEMPLATE_STYLES = listOf("STANDARD_GST", "MINIMAL", "DETAILED")

private val ACCENT_PRESETS =
    listOf(
        Color(0xFF0F766E) to "Teal",
        Color(0xFFF59E0B) to "Amber",
        Color(0xFFEF4444) to "Red",
        Color(0xFF8B5CF6) to "Violet",
        Color(0xFF10B981) to "Emerald",
        Color(0xFF3B82F6) to "Blue",
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfSettingsScreen(
    initialSettings: InvoiceSettings,
    onBack: () -> Unit = {},
    onSave: (InvoiceSettings) -> Unit,
) {
    var shopName by remember { mutableStateOf(initialSettings.shopName ?: "") }
    var shopAddress by remember { mutableStateOf(initialSettings.shopAddress ?: "") }
    var shopGstin by remember { mutableStateOf(initialSettings.shopGstin ?: "") }
    var headerText by remember { mutableStateOf(initialSettings.headerText ?: "") }
    var footerText by remember { mutableStateOf(initialSettings.footerText ?: "") }
    var terms by remember { mutableStateOf(initialSettings.terms ?: "") }
    var bankDetails by remember { mutableStateOf(initialSettings.bankDetails ?: "") }
    var upiId by remember { mutableStateOf("") }
    var showGstBreakdown by remember { mutableStateOf(initialSettings.showGstBreakdown) }
    var showBankDetails by remember { mutableStateOf(initialSettings.bankDetails?.isNotBlank() == true) }
    var templateStyle by remember { mutableStateOf(initialSettings.templateStyle) }
    var accentColor by remember { mutableStateOf(initialSettings.accentColor ?: "") }
    var logoPath by remember { mutableStateOf(initialSettings.logoPath ?: "") }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isColorDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                uri?.let { logoPath = it.toString() }
            },
        )

    fun handleSave() {
        var finalLogoPath: String? = logoPath.takeIf { it.isNotBlank() }
        if (finalLogoPath != null && finalLogoPath.startsWith("content://")) {
            try {
                val uri = android.net.Uri.parse(finalLogoPath)
                val destFile = java.io.File(context.filesDir, "invoice_logo_${initialSettings.storeId}.png")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                finalLogoPath = destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val effectiveBankDetails =
            buildString {
                if (bankDetails.isNotBlank()) append(bankDetails.trim())
                if (upiId.isNotBlank() && !bankDetails.contains(upiId)) {
                    if (isNotEmpty()) append("\n")
                    append("UPI ID: ${upiId.trim()}")
                }
            }.takeIf { it.isNotBlank() }

        val settings =
            InvoiceSettings(
                id = initialSettings.id,
                storeId = initialSettings.storeId,
                shopName = shopName.takeIf { it.isNotBlank() },
                shopAddress = shopAddress.takeIf { it.isNotBlank() },
                shopGstin = shopGstin.takeIf { it.isNotBlank() },
                logoPath = finalLogoPath,
                accentColor = accentColor.takeIf { it.isNotBlank() },
                headerText = headerText.takeIf { it.isNotBlank() },
                footerText = footerText.takeIf { it.isNotBlank() },
                bankDetails = effectiveBankDetails,
                terms = terms.takeIf { it.isNotBlank() },
                showGstBreakdown = showGstBreakdown,
                templateStyle = templateStyle,
            )
        onSave(settings)
        android.widget.Toast
            .makeText(context, "Invoice settings saved", android.widget.Toast.LENGTH_SHORT)
            .show()
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                title = { Text("Invoice Settings") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = ::handleSave,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Settings")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { sectionHeader("Branding") }

            item { logoUploadField(logoPath, onPick = { imagePicker.launch("image/*") }) }

            item { shopNameField(shopName) { shopName = it } }

            item { textField("Shop Address", shopAddress, onValueChanged = { shopAddress = it }, maxLines = 2) }

            item {
                textField(
                    "Shop GSTIN",
                    shopGstin,
                    onValueChanged = { shopGstin = it },
                    placeholder = "e.g. 27AABCU9603R1ZM",
                )
            }

            item { sectionHeader("Template") }

            item {
                templateStylePicker(
                    current = templateStyle,
                    expanded = isDropdownExpanded,
                    onSelect = { templateStyle = it },
                    onExpandedChanged = { isDropdownExpanded = it },
                )
            }

            item {
                colorPicker(
                    selectedColorHex = accentColor,
                    expanded = isColorDropdownExpanded,
                    onSelect = { color ->
                        accentColor = String.format("#%06X", 0xFFFFFF and color.toArgb())
                        isColorDropdownExpanded = false
                    },
                    onExpandedChanged = { isColorDropdownExpanded = it },
                )
            }

            item { sectionHeader("Content") }

            item { textField("Header Text", headerText, onValueChanged = { headerText = it }) }

            item { textField("Footer Text", footerText, onValueChanged = { footerText = it }) }

            item {
                textField(
                    "Terms & Conditions",
                    terms,
                    onValueChanged = { terms = it },
                    maxLines = 3,
                )
            }

            item { textField("Bank/UPI Details", bankDetails, onValueChanged = { bankDetails = it }) }

            item { textField("UPI ID", upiId, onValueChanged = { upiId = it }) }

            item { sectionHeader("Options") }

            item { toggleRow("Show GST Breakdown", showGstBreakdown) { showGstBreakdown = it } }

            item { toggleRow("Show Bank Details", showBankDetails) { showBankDetails = it } }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun templateStylePicker(
    current: String,
    expanded: Boolean,
    onSelect: (String) -> Unit,
    onExpandedChanged: (Boolean) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChanged,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text("Template Style") },
        )

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChanged(false) },
        ) {
            TEMPLATE_STYLES.forEach { style ->
                DropdownMenuItem(
                    text = { Text(style) },
                    onClick = {
                        onSelect(style)
                        onExpandedChanged(false)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun colorPicker(
    selectedColorHex: String,
    expanded: Boolean,
    onSelect: (Color) -> Unit,
    onExpandedChanged: (Boolean) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChanged,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            value = selectedColorHex.takeIf { it.isNotBlank() } ?: "Select color",
            onValueChange = {},
            readOnly = true,
            leadingIcon = {
                Icon(Icons.Default.ColorLens, contentDescription = null)
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text("Accent Color") },
        )

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChanged(false) },
        ) {
            ACCENT_PRESETS.forEach { (color, label) ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(16.dp).clip(CircleShape).background(color),
                            )
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(label)
                        }
                    },
                    onClick = { onSelect(color) },
                )
            }
        }
    }
}

@Composable
private fun toggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onCheckedChange(!checked) },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun logoUploadField(
    currentPath: String,
    onPick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onPick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.padding(end = 12.dp))
            Column {
                Text(text = "Logo", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = currentPath.takeIf { it.isNotBlank() } ?: "Tap to upload logo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun shopNameField(
    value: String,
    onValueChanged: (String) -> Unit,
) {
    textField("Shop Name", value, onValueChanged)
}

@Composable
private fun textField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    maxLines: Int = 1,
    placeholder: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(label) },
        placeholder = { placeholder?.let { Text(it) } },
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
}

@Composable
private fun sectionHeader(title: String) =
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
