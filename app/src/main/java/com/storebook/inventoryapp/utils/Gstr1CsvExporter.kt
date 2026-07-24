package com.storebook.inventoryapp.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.storebook.inventoryapp.data.billing.BillingEngine
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Sale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Gstr1CsvExporter {
    private val dateFmt = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

    suspend fun exportGstr1Csv(
        context: Context,
        fileName: String,
        sales: List<Sale>,
        businessGstin: String,
        allItemsMap: Map<Long, Item>,
    ) = withContext(Dispatchers.IO) {
        val b2bRows = mutableListOf<String>()
        val b2cAggregated = mutableMapOf<Double, DoubleArray>() // TaxRate -> [TaxableValue, TaxAmount]

        for (sale in sales) {
            val hasBuyerGstin = !sale.customerGstin.isNullOrBlank()

            val cartItems =
                sale.items.map { saleItem ->
                    val actualItem =
                        allItemsMap[saleItem.itemId] ?: Item(
                            id = saleItem.itemId,
                            name = saleItem.itemName,
                            quantity = 0.0,
                            unit = saleItem.unit,
                            buyPrice = saleItem.buyPrice,
                            sellPrice = saleItem.sellPrice,
                            lowStockThreshold = 0.0,
                            category = "",
                        )
                    CartItem(item = actualItem.copy(sellPrice = saleItem.sellPrice), quantity = saleItem.quantity)
                }

            val taxSummary =
                BillingEngine.calculateInvoiceTaxes(
                    cartItems = cartItems,
                    totalDiscount = sale.discountAmount,
                    businessGstin = businessGstin.takeIf { it.isNotBlank() },
                    customerGstin = sale.customerGstin?.takeIf { it.isNotBlank() },
                )

            if (hasBuyerGstin) {
                // B2B - group by rate for this invoice
                val taxesByRate = taxSummary.itemDetails.groupBy { it.cartItem.item.taxRate }
                taxesByRate.forEach { (rate, details) ->
                    val taxableValue = details.sumOf { it.netAmountBeforeTax }
                    val taxAmount = details.sumOf { it.totalTaxAmount }
                    val dateStr = dateFmt.format(Date(sale.timestamp))
                    val invNo = "INV${sale.id}"
                    val gstin = sale.customerGstin ?: ""

                    b2bRows.add("$gstin,$invNo,$dateStr,%.2f,%.2f".format(taxableValue, taxAmount))
                }
            } else {
                // B2C - Aggregate by rate
                val taxesByRate = taxSummary.itemDetails.groupBy { it.cartItem.item.taxRate }
                taxesByRate.forEach { (rate, details) ->
                    val taxableValue = details.sumOf { it.netAmountBeforeTax }
                    val taxAmount = details.sumOf { it.totalTaxAmount }

                    val current = b2cAggregated.getOrDefault(rate, doubleArrayOf(0.0, 0.0))
                    current[0] += taxableValue
                    current[1] += taxAmount
                    b2cAggregated[rate] = current
                }
            }
        }

        val csvBuilder = StringBuilder()
        csvBuilder.append("GSTIN of buyer,Invoice No,Date,Taxable Value,Tax Amount\n")

        // Append B2B
        for (row in b2bRows) {
            csvBuilder.append(row).append("\n")
        }

        // Append B2C (Aggregated)
        // For B2C, GSTIN is empty, Invoice No is "B2C_AGG", Date is current date
        val currentDateStr = dateFmt.format(Date())
        b2cAggregated.forEach { (rate, values) ->
            if (values[0] > 0 || values[1] > 0) {
                csvBuilder.append(",B2C_AGG_$rate%,$currentDateStr,%.2f,%.2f\n".format(values[0], values[1]))
            }
        }

        val file = File(context.cacheDir, "$fileName.csv")
        FileOutputStream(file).use { fos ->
            fos.write(csvBuilder.toString().toByteArray())
        }

        val uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )

        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

        val chooser = Intent.createChooser(intent, "Share GSTR-1 CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
