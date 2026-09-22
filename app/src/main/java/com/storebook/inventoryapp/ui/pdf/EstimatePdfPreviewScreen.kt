package com.storebook.inventoryapp.ui.pdf

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.ui.theme.PrimaryButton
import com.storebook.inventoryapp.utils.ShareUtils
import com.storebook.inventoryapp.utils.toRupee
import java.io.File

@Composable
private fun rememberLogoPainter(logoPath: String?): Painter {
    val context = LocalContext.current
    val bitmapState =
        remember(logoPath) {
            if (!logoPath.isNullOrBlank()) {
                try {
                    if (logoPath.startsWith("content://") || logoPath.startsWith("file://")) {
                        val uri = android.net.Uri.parse(logoPath)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            android.graphics.BitmapFactory.decodeStream(stream)
                        }
                    } else {
                        val file = File(logoPath)
                        if (file.exists() && file.length() > 0L) {
                            android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                        } else {
                            null
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }

    return if (bitmapState != null) {
        BitmapPainter(bitmapState.asImageBitmap())
    } else {
        painterResource(R.drawable.logo)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstimatePdfPreviewScreen(
    estimateId: Long,
    cartItems: List<CartItem>,
    totalAmount: Double = 0.0,
    settings: com.storebook.inventoryapp.shared.domain.models.InvoiceSettings? = null,
    onBack: () -> Unit,
    onPrintThermal: (List<CartItem>) -> Unit = {},
    onSaveFile: (Long) -> Unit = {},
) {
    val context = LocalContext.current
    val effectiveSettings =
        remember(settings) {
            settings ?: try {
                val driver =
                    com.storebook.inventoryapp.shared.data
                        .DatabaseDriverFactory(context, "default")
                        .createDriver()
                val database =
                    com.storebook.inventoryapp.shared.data.local
                        .StoreBookDatabase(driver)
                com.storebook.inventoryapp.shared.data.local
                    .InvoiceSettingsRepository(database)
                    .getInvoiceSettings("default")
            } catch (e: Exception) {
                null
            }
        }

    fun getOrGeneratePdfFile(): File? {
        val file = File(context.cacheDir, "Estimate_$estimateId.pdf")
        if (file.exists()) {
            file.delete()
        }
        if (cartItems.isEmpty()) return null
        val sale =
            com.storebook.inventoryapp.shared.domain.models.Sale(
                id = estimateId,
                customerName = null,
                totalAmount = totalAmount,
                discountAmount = 0.0,
                type = "ESTIMATE",
                timestamp = System.currentTimeMillis(),
            )
        return com.storebook.inventoryapp.utils.InvoicePdfGenerator.generateInvoicePdf(
            context = context,
            sale = sale,
            cartItems = cartItems,
            shopName = "StoreBook",
            shopAddress = "",
            shopGstin = "",
            settings = effectiveSettings,
        )
    }

    val saveLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/pdf"),
            onResult = { uri ->
                if (uri != null) {
                    try {
                        val pdfFile = getOrGeneratePdfFile()
                        if (pdfFile != null && pdfFile.exists()) {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                pdfFile.inputStream().use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            android.widget.Toast
                                .makeText(context, "Saved file to device", android.widget.Toast.LENGTH_SHORT)
                                .show()
                            onSaveFile(estimateId)
                        } else {
                            android.widget.Toast
                                .makeText(context, "Failed to generate PDF file", android.widget.Toast.LENGTH_SHORT)
                                .show()
                        }
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        android.widget.Toast
                            .makeText(context, "Failed to save file: ${e.message}", android.widget.Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            },
        )

    fun sharePdf() {
        val pdfFile = getOrGeneratePdfFile()
        if (pdfFile != null) {
            ShareUtils.sharePdf(context, pdfFile)
        } else {
            android.widget.Toast
                .makeText(context, "Cannot share — no items in estimate", android.widget.Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun sharePdfWhatsApp() {
        val pdfFile = getOrGeneratePdfFile()
        if (pdfFile != null) {
            val msg = "Estimate #$estimateId - Total: ${totalAmount.toRupee()}"
            ShareUtils.sharePdfWhatsApp(
                context,
                pdfFile,
                null,
                msg,
            )
        } else {
            android.widget.Toast
                .makeText(context, "Cannot share — no items in estimate", android.widget.Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun handleSave() {
        val pdfFile = getOrGeneratePdfFile()
        if (pdfFile != null) {
            saveLauncher.launch("Estimate_$estimateId.pdf")
        } else {
            android.widget.Toast
                .makeText(context, "Cannot save — no items in estimate", android.widget.Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun openPdf() {
        val pdfFile = getOrGeneratePdfFile()
        if (pdfFile != null) {
            ShareUtils.openPdf(context, pdfFile)
        } else {
            android.widget.Toast
                .makeText(context, "Cannot open — no items in estimate", android.widget.Toast.LENGTH_SHORT)
                .show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                        )
                    }
                },
                title = { Text("Estimate Preview") },
                actions = {
                    IconButton(
                        onClick = { onPrintThermal(cartItems) },
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print Estimate")
                    }
                    IconButton(
                        onClick = ::sharePdf,
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share Estimate")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onPrintThermal(cartItems) },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Print, contentDescription = "Print Thermal Receipt")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = rememberLogoPainter(effectiveSettings?.logoPath),
                        contentDescription = "Estimate PDF Preview",
                        modifier = Modifier.size(120.dp).padding(top = 32.dp),
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 180.dp),
                    ) {
                        Text(
                            text = "Estimate #$estimateId",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalAmount.toRupee(),
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                ),
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Line Items (${cartItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        cartItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${item.item.name} x ${item.quantity}",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = (item.item.sellPrice * item.quantity).toRupee(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                )
                            }

                            if (index < cartItems.size - 1) {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PrimaryButton(
                        onClick = ::openPdf,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Open PDF")
                    }

                    PrimaryButton(
                        onClick = ::sharePdfWhatsApp,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Share via WhatsApp")
                    }

                    PrimaryButton(
                        onClick = ::handleSave,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Save File")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
