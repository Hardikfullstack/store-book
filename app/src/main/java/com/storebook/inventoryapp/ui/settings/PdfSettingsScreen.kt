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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.storebook.inventoryapp.shared.domain.models.InvoiceSettings
import com.storebook.inventoryapp.utils.InvoicePdfGenerator

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
    val (initialCleanBank, initialCleanUpi) =
        remember(initialSettings) {
            val rawBank = initialSettings.bankDetails ?: ""
            if (!initialSettings.upiId.isNullOrBlank()) {
                rawBank to initialSettings.upiId
            } else {
                val lines = rawBank.lines()
                val upiLine =
                    lines.firstOrNull {
                        val t = it.trim()
                        t.startsWith("UPI ID:", ignoreCase = true) || t.startsWith("UPI:", ignoreCase = true)
                    }
                val upi = upiLine?.substringAfter(":")?.trim()
                val remaining =
                    lines
                        .filter {
                            val t = it.trim()
                            !t.startsWith("UPI ID:", ignoreCase = true) && !t.startsWith("UPI:", ignoreCase = true)
                        }.joinToString("\n")
                        .trim()
                remaining to upi
            }
        }

    var shopName by remember(initialSettings) { mutableStateOf(initialSettings.shopName ?: "") }
    var shopAddress by remember(initialSettings) { mutableStateOf(initialSettings.shopAddress ?: "") }
    var shopGstin by remember(initialSettings) { mutableStateOf(initialSettings.shopGstin ?: "") }
    var footerText by remember(initialSettings) { mutableStateOf(initialSettings.footerText ?: "") }
    var terms by remember(initialSettings) { mutableStateOf(initialSettings.terms ?: "") }
    var bankDetails by remember(initialSettings) { mutableStateOf(initialCleanBank) }
    var upiId by remember(initialSettings) { mutableStateOf(initialCleanUpi ?: "") }
    var showBankDetails by remember(initialSettings) {
        mutableStateOf(
            initialCleanBank.isNotBlank() || !initialCleanUpi.isNullOrBlank(),
        )
    }
    var accentColor by remember(initialSettings) { mutableStateOf(initialSettings.accentColor ?: "") }
    var logoPath by remember(initialSettings) { mutableStateOf(initialSettings.logoPath ?: "") }

    var isColorDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                if (uri != null) {
                    try {
                        val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        if (rawBytes != null) {
                            val maxLimitBytes = 500 * 1024
                            val mimeType = context.contentResolver.getType(uri)?.lowercase() ?: "image/jpeg"
                            if (rawBytes.size <= maxLimitBytes) {
                                val prefix = if (mimeType.contains("png")) "data:image/png;base64," else "data:image/jpeg;base64,"
                                val base64 = prefix + android.util.Base64.encodeToString(rawBytes, android.util.Base64.NO_WRAP)
                                logoPath = base64
                            } else {
                                val originalBitmap = android.graphics.BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                                if (originalBitmap != null) {
                                    val maxDim = 800
                                    val scale =
                                        if (originalBitmap.width > maxDim || originalBitmap.height > maxDim) {
                                            maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                                        } else {
                                            1f
                                        }
                                    val scaledBitmap =
                                        if (scale < 1f) {
                                            android.graphics.Bitmap.createScaledBitmap(
                                                originalBitmap,
                                                (originalBitmap.width * scale).toInt(),
                                                (originalBitmap.height * scale).toInt(),
                                                true,
                                            )
                                        } else {
                                            originalBitmap
                                        }

                                    val bitmapToCompress =
                                        if (scaledBitmap.hasAlpha()) {
                                            val whiteBitmap =
                                                android.graphics.Bitmap.createBitmap(
                                                    scaledBitmap.width,
                                                    scaledBitmap.height,
                                                    android.graphics.Bitmap.Config.ARGB_8888,
                                                )
                                            val canvas = android.graphics.Canvas(whiteBitmap)
                                            canvas.drawColor(android.graphics.Color.WHITE)
                                            canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
                                            whiteBitmap
                                        } else {
                                            scaledBitmap
                                        }

                                    var quality = 85
                                    var compressedBytes: ByteArray
                                    do {
                                        val outStream = java.io.ByteArrayOutputStream()
                                        bitmapToCompress.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, outStream)
                                        compressedBytes = outStream.toByteArray()
                                        quality -= 15
                                    } while (compressedBytes.size > maxLimitBytes && quality >= 40)

                                    if (compressedBytes.size <= maxLimitBytes) {
                                        val base64 =
                                            "data:image/jpeg;base64," + android.util.Base64.encodeToString(compressedBytes, android.util.Base64.NO_WRAP)
                                        logoPath = base64
                                    } else {
                                        android.widget.Toast
                                            .makeText(
                                                context,
                                                "Logo exceeds 500 KB limit. Please choose a smaller image.",
                                                android.widget.Toast.LENGTH_LONG,
                                            ).show()
                                    }
                                } else {
                                    android.widget.Toast
                                        .makeText(context, "Invalid image format", android.widget.Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.widget.Toast
                            .makeText(context, "Failed to load image", android.widget.Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            },
        )

    fun handleSave() {
        val finalLogoPath: String? = logoPath.takeIf { it.isNotBlank() }

        val settings =
            InvoiceSettings(
                id = initialSettings.id,
                storeId = initialSettings.storeId,
                shopName = shopName.takeIf { it.isNotBlank() },
                shopAddress = shopAddress.takeIf { it.isNotBlank() },
                shopGstin = shopGstin.takeIf { it.isNotBlank() },
                logoPath = finalLogoPath,
                accentColor = accentColor.takeIf { it.isNotBlank() },
                footerText = footerText.takeIf { it.isNotBlank() },
                bankDetails = bankDetails.takeIf { it.isNotBlank() },
                terms = terms.takeIf { it.isNotBlank() },
                upiId = upiId.takeIf { it.isNotBlank() },
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
            item {
                logoUploadField(
                    context = context,
                    currentPath = logoPath,
                    onPick = { imagePicker.launch("image/*") },
                    onRemove = { logoPath = "" },
                )
            }

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

            item { sectionHeader("Accent Color") }

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

            item { textField("Footer Text", footerText, onValueChanged = { footerText = it }) }

            item {
                textField(
                    label = "Terms & Conditions",
                    value = terms,
                    onValueChanged = { terms = it },
                    minLines = 2,
                    maxLines = 4,
                )
            }

            item {
                textField(
                    label = "Bank Details",
                    value = bankDetails,
                    onValueChanged = { bankDetails = it },
                    minLines = 3,
                    maxLines = 6,
                    placeholder = "e.g.\nBank Name: HDFC Bank\nA/C No: 123456789012\nIFSC Code: HDFC0001234\nBranch: Main Branch",
                )
            }

            item {
                textField(
                    label = "UPI ID",
                    value = upiId,
                    onValueChanged = { upiId = it },
                    placeholder = "e.g. yourname@upi or 9876543210@paytm",
                )
            }

            item { sectionHeader("Options") }

            item { toggleRow("Show Bank Details", showBankDetails) { showBankDetails = it } }

            item { Spacer(modifier = Modifier.height(80.dp)) }
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
    context: android.content.Context,
    currentPath: String,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    val bitmap =
        remember(currentPath) {
            InvoicePdfGenerator.loadLogoBitmap(context, currentPath)
        }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).clickable(onClick = onPick),
            ) {
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Shop Logo",
                        modifier =
                            Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).padding(end = 8.dp),
                    )
                }
                Spacer(modifier = Modifier.padding(start = 12.dp))
                Column {
                    Text(text = "Shop Logo", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = if (bitmap != null) "Tap to change (Max 500 KB)" else "Tap to upload (Max 500 KB)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            if (currentPath.isNotBlank() || bitmap != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove logo",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
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
    minLines: Int = 1,
    maxLines: Int = 1,
    placeholder: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(label) },
        placeholder = { placeholder?.let { Text(it) } },
        minLines = minLines,
        maxLines = maxLines,
        singleLine = maxLines == 1,
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
