package com.storebook.inventoryapp.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.storebook.inventoryapp.data.billing.BillingEngine
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.Item
import com.storebook.inventoryapp.shared.domain.models.Purchase
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.shared.domain.models.Supplier
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {
    // Formatter for dates in Excel
    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    suspend fun exportGstr1(
        context: Context,
        fileName: String,
        sales: List<Sale>,
        businessName: String,
        businessGstin: String,
        allItemsMap: Map<Long, Item>,
    ) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("GSTR-1 Sales")
        sheet.setDisplayGridlines(true)

        val styles = ExcelStyles(workbook)

        // 1. Title Block
        val titleRow = sheet.createRow(0)
        titleRow.heightInPoints = 30f
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("GSTR-1 Outward Supplies (Sales) Report")
        titleCell.cellStyle = styles.titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 8))

        // Business Metadata
        val metaRow1 = sheet.createRow(2)
        metaRow1.createCell(0).apply {
            setCellValue("Business Name:")
            cellStyle = styles.boldStyle
        }
        metaRow1.createCell(1).setCellValue(businessName)
        metaRow1.createCell(3).apply {
            setCellValue("GSTIN:")
            cellStyle = styles.boldStyle
        }
        metaRow1.createCell(4).setCellValue(businessGstin.ifBlank { "Not Provided" })

        val metaRow2 = sheet.createRow(3)
        metaRow2.createCell(0).apply {
            setCellValue("Generated On:")
            cellStyle = styles.boldStyle
        }
        metaRow2.createCell(1).setCellValue(dateFmt.format(Date()))
        metaRow2.createCell(3).apply {
            setCellValue("Total Sales Count:")
            cellStyle = styles.boldStyle
        }
        metaRow2.createCell(4).setCellValue(sales.size.toDouble())

        // 2. Table Headers
        val headers =
            listOf(
                "Date", "Invoice No", "Customer Name", "Customer GSTIN",
                "Taxable Value (₹)", "CGST (₹)", "SGST (₹)", "IGST (₹)", "Total Amount (₹)",
            )
        val headerRow = sheet.createRow(5)
        headerRow.heightInPoints = 25f
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = styles.headerStyle
        }

        // 3. Populate Data
        var rowIndex = 6
        var sumTaxable = 0.0
        var sumCgst = 0.0
        var sumSgst = 0.0
        var sumIgst = 0.0
        var sumTotal = 0.0

        for (sale in sales) {
            // Map items to CartItem for tax calculation
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

            val row = sheet.createRow(rowIndex++)
            row.createCell(0).apply {
                setCellValue(dateFmt.format(Date(sale.timestamp)))
                cellStyle = styles.centerStyle
            }
            row.createCell(1).apply {
                setCellValue("INV${sale.id.toString().padStart(5, '0')}")
                cellStyle =
                    styles.centerStyle
            }
            row.createCell(2).apply {
                setCellValue(sale.customerName ?: "Cash / B2C Customer")
                cellStyle =
                    styles.leftStyle
            }
            row.createCell(3).apply {
                setCellValue(sale.customerGstin ?: "-")
                cellStyle = styles.centerStyle
            }

            row.createCell(4).apply {
                setCellValue(taxSummary.netTaxableAmount)
                cellStyle = styles.currencyStyle
            }
            row.createCell(5).apply {
                setCellValue(taxSummary.totalCgst)
                cellStyle = styles.currencyStyle
            }
            row.createCell(6).apply {
                setCellValue(taxSummary.totalSgst)
                cellStyle = styles.currencyStyle
            }
            row.createCell(7).apply {
                setCellValue(taxSummary.totalIgst)
                cellStyle = styles.currencyStyle
            }
            row.createCell(8).apply {
                setCellValue(taxSummary.grandTotal)
                cellStyle = styles.currencyStyle
            }

            sumTaxable += taxSummary.netTaxableAmount
            sumCgst += taxSummary.totalCgst
            sumSgst += taxSummary.totalSgst
            sumIgst += taxSummary.totalIgst
            sumTotal += taxSummary.grandTotal
        }

        // 4. Totals Row
        val totalRow = sheet.createRow(rowIndex)
        totalRow.createCell(0).apply {
            setCellValue("Total")
            cellStyle = styles.boldStyle
        }
        for (i in 1..3) {
            totalRow.createCell(i).apply {
                setCellValue("")
                cellStyle = styles.boldStyle
            }
        }
        totalRow.createCell(4).apply {
            setCellValue(sumTaxable)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(5).apply {
            setCellValue(sumCgst)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(6).apply {
            setCellValue(sumSgst)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(7).apply {
            setCellValue(sumIgst)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(8).apply {
            setCellValue(sumTotal)
            cellStyle = styles.rightBoldCurrencyStyle
        }

        // Set column widths manually (Android-safe, avoids AWT autoSizeColumn crash)
        val columnWidths = intArrayOf(12, 12, 20, 18, 18, 15, 15, 15, 18)
        columnWidths.forEachIndexed { i, w ->
            sheet.setColumnWidth(i, w * 256)
        }

        saveAndShareWorkbook(context, workbook, fileName)
    }

    suspend fun exportGstr2(
        context: Context,
        fileName: String,
        purchases: List<Purchase>,
        businessName: String,
        businessGstin: String,
        suppliersMap: Map<Long, Supplier>,
        allItemsMap: Map<Long, Item>,
    ) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("GSTR-2 Purchases")
        sheet.setDisplayGridlines(true)

        val styles = ExcelStyles(workbook)

        // 1. Title Block
        val titleRow = sheet.createRow(0)
        titleRow.heightInPoints = 30f
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("GSTR-2 Inward Supplies (Purchases) Report")
        titleCell.cellStyle = styles.titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 8))

        // Business Metadata
        val metaRow1 = sheet.createRow(2)
        metaRow1.createCell(0).apply {
            setCellValue("Business Name:")
            cellStyle = styles.boldStyle
        }
        metaRow1.createCell(1).setCellValue(businessName)
        metaRow1.createCell(3).apply {
            setCellValue("GSTIN:")
            cellStyle = styles.boldStyle
        }
        metaRow1.createCell(4).setCellValue(businessGstin.ifBlank { "Not Provided" })

        val metaRow2 = sheet.createRow(3)
        metaRow2.createCell(0).apply {
            setCellValue("Generated On:")
            cellStyle = styles.boldStyle
        }
        metaRow2.createCell(1).setCellValue(dateFmt.format(Date()))
        metaRow2.createCell(3).apply {
            setCellValue("Total Purchases Count:")
            cellStyle = styles.boldStyle
        }
        metaRow2.createCell(4).setCellValue(purchases.size.toDouble())

        // 2. Table Headers
        val headers =
            listOf(
                "Date", "Bill No / ID", "Supplier Name", "Supplier GSTIN",
                "Taxable Value (₹)", "CGST (₹)", "SGST (₹)", "IGST (₹)", "Total Amount (₹)",
            )
        val headerRow = sheet.createRow(5)
        headerRow.heightInPoints = 25f
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = styles.headerStyle
        }

        // 3. Populate Data
        var rowIndex = 6
        var sumTaxable = 0.0
        var sumCgst = 0.0
        var sumSgst = 0.0
        var sumIgst = 0.0
        var sumTotal = 0.0

        for (purchase in purchases) {
            val supplier = suppliersMap[purchase.supplierId]
            val supplierGstin = supplier?.gstin ?: ""

            // Map items to CartItem for tax calculation (buy price acts as sell price)
            val cartItems =
                purchase.items.map { pi ->
                    val actualItem =
                        allItemsMap[pi.itemId] ?: Item(
                            id = pi.itemId,
                            name = pi.itemName,
                            quantity = 0.0,
                            unit = pi.unit,
                            buyPrice = pi.buyPrice,
                            sellPrice = pi.buyPrice,
                            lowStockThreshold = 0.0,
                            category = "",
                        )
                    CartItem(item = actualItem.copy(sellPrice = pi.buyPrice), quantity = pi.quantity)
                }

            val taxSummary =
                BillingEngine.calculateInvoiceTaxes(
                    cartItems = cartItems,
                    totalDiscount = 0.0,
                    businessGstin = businessGstin.takeIf { it.isNotBlank() },
                    customerGstin = supplierGstin.takeIf { it.isNotBlank() },
                )

            val row = sheet.createRow(rowIndex++)
            row.createCell(0).apply {
                setCellValue(dateFmt.format(Date(purchase.timestamp)))
                cellStyle =
                    styles.centerStyle
            }
            row.createCell(1).apply {
                setCellValue("PUR${purchase.id.toString().padStart(5, '0')}")
                cellStyle =
                    styles.centerStyle
            }
            row.createCell(2).apply {
                setCellValue(purchase.supplierName)
                cellStyle = styles.leftStyle
            }
            row.createCell(3).apply {
                setCellValue(supplierGstin.ifBlank { "-" })
                cellStyle = styles.centerStyle
            }

            row.createCell(4).apply {
                setCellValue(taxSummary.netTaxableAmount)
                cellStyle = styles.currencyStyle
            }
            row.createCell(5).apply {
                setCellValue(taxSummary.totalCgst)
                cellStyle = styles.currencyStyle
            }
            row.createCell(6).apply {
                setCellValue(taxSummary.totalSgst)
                cellStyle = styles.currencyStyle
            }
            row.createCell(7).apply {
                setCellValue(taxSummary.totalIgst)
                cellStyle = styles.currencyStyle
            }
            row.createCell(8).apply {
                setCellValue(taxSummary.grandTotal)
                cellStyle = styles.currencyStyle
            }

            sumTaxable += taxSummary.netTaxableAmount
            sumCgst += taxSummary.totalCgst
            sumSgst += taxSummary.totalSgst
            sumIgst += taxSummary.totalIgst
            sumTotal += taxSummary.grandTotal
        }

        // 4. Totals Row
        val totalRow = sheet.createRow(rowIndex)
        totalRow.createCell(0).apply {
            setCellValue("Total")
            cellStyle = styles.boldStyle
        }
        for (i in 1..3) {
            totalRow.createCell(i).apply {
                setCellValue("")
                cellStyle = styles.boldStyle
            }
        }
        totalRow.createCell(4).apply {
            setCellValue(sumTaxable)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(5).apply {
            setCellValue(sumCgst)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(6).apply {
            setCellValue(sumSgst)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(7).apply {
            setCellValue(sumIgst)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(8).apply {
            setCellValue(sumTotal)
            cellStyle = styles.rightBoldCurrencyStyle
        }

        // Set column widths manually (Android-safe, avoids AWT autoSizeColumn crash)
        val columnWidths = intArrayOf(12, 15, 20, 18, 18, 15, 15, 15, 18)
        columnWidths.forEachIndexed { i, w ->
            sheet.setColumnWidth(i, w * 256)
        }

        saveAndShareWorkbook(context, workbook, fileName)
    }

    suspend fun exportGstr3B(
        context: Context,
        fileName: String,
        sales: List<Sale>,
        purchases: List<Purchase>,
        businessName: String,
        businessGstin: String,
        suppliersMap: Map<Long, Supplier>,
        allItemsMap: Map<Long, Item>,
    ) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("GSTR-3B Summary")
        sheet.setDisplayGridlines(true)

        val styles = ExcelStyles(workbook)

        // 1. Title Block
        val titleRow = sheet.createRow(0)
        titleRow.heightInPoints = 30f
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("GSTR-3B Monthly Consolidated Summary")
        titleCell.cellStyle = styles.titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 5))

        // Business Metadata
        val metaRow1 = sheet.createRow(2)
        metaRow1.createCell(0).apply {
            setCellValue("Business Name:")
            cellStyle = styles.boldStyle
        }
        metaRow1.createCell(1).setCellValue(businessName)
        metaRow1.createCell(3).apply {
            setCellValue("GSTIN:")
            cellStyle = styles.boldStyle
        }
        metaRow1.createCell(4).setCellValue(businessGstin.ifBlank { "Not Provided" })

        val metaRow2 = sheet.createRow(3)
        metaRow2.createCell(0).apply {
            setCellValue("Generated On:")
            cellStyle = styles.boldStyle
        }
        metaRow2.createCell(1).setCellValue(dateFmt.format(Date()))

        // Aggregate Sales Tax
        var saleTaxable = 0.0
        var saleCgst = 0.0
        var saleSgst = 0.0
        var saleIgst = 0.0
        var saleTotalTax = 0.0

        for (sale in sales) {
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
                    businessGstin = businessGstin,
                    customerGstin = sale.customerGstin,
                )
            saleTaxable += taxSummary.netTaxableAmount
            saleCgst += taxSummary.totalCgst
            saleSgst += taxSummary.totalSgst
            saleIgst += taxSummary.totalIgst
            saleTotalTax += (taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst)
        }

        // Aggregate Purchases Tax (ITC)
        var purchaseTaxable = 0.0
        var purchaseCgst = 0.0
        var purchaseSgst = 0.0
        var purchaseIgst = 0.0
        var purchaseTotalTax = 0.0

        for (purchase in purchases) {
            val supplier = suppliersMap[purchase.supplierId]
            val supplierGstin = supplier?.gstin ?: ""

            val cartItems =
                purchase.items.map { pi ->
                    val actualItem =
                        allItemsMap[pi.itemId] ?: Item(
                            id = pi.itemId,
                            name = pi.itemName,
                            quantity = 0.0,
                            unit = pi.unit,
                            buyPrice = pi.buyPrice,
                            sellPrice = pi.buyPrice,
                            lowStockThreshold = 0.0,
                            category = "",
                        )
                    CartItem(item = actualItem.copy(sellPrice = pi.buyPrice), quantity = pi.quantity)
                }
            val taxSummary =
                BillingEngine.calculateInvoiceTaxes(
                    cartItems = cartItems,
                    totalDiscount = 0.0,
                    businessGstin = businessGstin,
                    customerGstin = supplierGstin,
                )
            purchaseTaxable += taxSummary.netTaxableAmount
            purchaseCgst += taxSummary.totalCgst
            purchaseSgst += taxSummary.totalSgst
            purchaseIgst += taxSummary.totalIgst
            purchaseTotalTax += (taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst)
        }

        // Section 1: Outward Supplies Table
        var rowIdx = 5
        sheet.createRow(rowIdx++).createCell(0).apply {
            setCellValue("1. OUTWARD SUPPLIES (SALES LIABILITY)")
            cellStyle =
                styles.sectionHeaderStyle
        }

        val sec1Headers =
            listOf("Outward Supplies Type", "Taxable Value (₹)", "CGST (₹)", "SGST (₹)", "IGST (₹)", "Total Tax (₹)")
        val sec1HeaderRow = sheet.createRow(rowIdx++)
        sec1Headers.forEachIndexed { i, h ->
            sec1HeaderRow.createCell(i).apply {
                setCellValue(h)
                cellStyle =
                    styles.headerStyle
            }
        }

        val sRow = sheet.createRow(rowIdx++)
        sRow.createCell(0).apply {
            setCellValue("Standard Rated Local & Interstate Supplies")
            cellStyle =
                styles.leftStyle
        }
        sRow.createCell(1).apply {
            setCellValue(saleTaxable)
            cellStyle = styles.currencyStyle
        }
        sRow.createCell(2).apply {
            setCellValue(saleCgst)
            cellStyle = styles.currencyStyle
        }
        sRow.createCell(3).apply {
            setCellValue(saleSgst)
            cellStyle = styles.currencyStyle
        }
        sRow.createCell(4).apply {
            setCellValue(saleIgst)
            cellStyle = styles.currencyStyle
        }
        sRow.createCell(5).apply {
            setCellValue(saleTotalTax)
            cellStyle = styles.currencyStyle
        }

        rowIdx++ // Space

        // Section 2: Inward Supplies Table
        sheet.createRow(rowIdx++).createCell(0).apply {
            setCellValue("2. INWARD SUPPLIES (ELIGIBLE ITC FROM PURCHASES)")
            cellStyle =
                styles.sectionHeaderStyle
        }

        val sec2HeaderRow = sheet.createRow(rowIdx++)
        sec1Headers.forEachIndexed { i, h ->
            sec2HeaderRow.createCell(i).apply {
                setCellValue(h)
                cellStyle =
                    styles.headerStyle
            }
        }

        val pRow = sheet.createRow(rowIdx++)
        pRow.createCell(0).apply {
            setCellValue("Inward Supplies Eligible For Input Tax Credit")
            cellStyle =
                styles.leftStyle
        }
        pRow.createCell(1).apply {
            setCellValue(purchaseTaxable)
            cellStyle = styles.currencyStyle
        }
        pRow.createCell(2).apply {
            setCellValue(purchaseCgst)
            cellStyle = styles.currencyStyle
        }
        pRow.createCell(3).apply {
            setCellValue(purchaseSgst)
            cellStyle = styles.currencyStyle
        }
        pRow.createCell(4).apply {
            setCellValue(purchaseIgst)
            cellStyle = styles.currencyStyle
        }
        pRow.createCell(5).apply {
            setCellValue(purchaseTotalTax)
            cellStyle = styles.currencyStyle
        }

        rowIdx++ // Space

        // Section 3: Net Tax Payable Table
        sheet.createRow(rowIdx++).createCell(0).apply {
            setCellValue("3. NET TAX PAYABLE / (REMAINING ITC)")
            cellStyle =
                styles.sectionHeaderStyle
        }

        val sec3Headers =
            listOf("Tax Component", "Outward Tax (A) (₹)", "ITC Available (B) (₹)", "Net Tax Payable (A - B) (₹)")
        val sec3HeaderRow = sheet.createRow(rowIdx++)
        sec3Headers.forEachIndexed { i, h ->
            sec3HeaderRow.createCell(i).apply {
                setCellValue(h)
                cellStyle =
                    styles.headerStyle
            }
        }

        // CGST Row
        val cgstNetRow = sheet.createRow(rowIdx++)
        cgstNetRow.createCell(0).apply {
            setCellValue("CGST")
            cellStyle = styles.leftStyle
        }
        cgstNetRow.createCell(1).apply {
            setCellValue(saleCgst)
            cellStyle = styles.currencyStyle
        }
        cgstNetRow.createCell(2).apply {
            setCellValue(purchaseCgst)
            cellStyle = styles.currencyStyle
        }
        cgstNetRow.createCell(3).apply {
            setCellValue(saleCgst - purchaseCgst)
            cellStyle = styles.currencyStyle
        }

        // SGST Row
        val sgstNetRow = sheet.createRow(rowIdx++)
        sgstNetRow.createCell(0).apply {
            setCellValue("SGST")
            cellStyle = styles.leftStyle
        }
        sgstNetRow.createCell(1).apply {
            setCellValue(saleSgst)
            cellStyle = styles.currencyStyle
        }
        sgstNetRow.createCell(2).apply {
            setCellValue(purchaseSgst)
            cellStyle = styles.currencyStyle
        }
        sgstNetRow.createCell(3).apply {
            setCellValue(saleSgst - purchaseSgst)
            cellStyle = styles.currencyStyle
        }

        // IGST Row
        val igstNetRow = sheet.createRow(rowIdx++)
        igstNetRow.createCell(0).apply {
            setCellValue("IGST")
            cellStyle = styles.leftStyle
        }
        igstNetRow.createCell(1).apply {
            setCellValue(saleIgst)
            cellStyle = styles.currencyStyle
        }
        igstNetRow.createCell(2).apply {
            setCellValue(purchaseIgst)
            cellStyle = styles.currencyStyle
        }
        igstNetRow.createCell(3).apply {
            setCellValue(saleIgst - purchaseIgst)
            cellStyle = styles.currencyStyle
        }

        // Total Net Row
        val totalNetRow = sheet.createRow(rowIdx)
        totalNetRow.createCell(0).apply {
            setCellValue("Total Net Tax")
            cellStyle = styles.boldStyle
        }
        totalNetRow.createCell(1).apply {
            setCellValue(saleTotalTax)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalNetRow.createCell(2).apply {
            setCellValue(purchaseTotalTax)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalNetRow.createCell(3).apply {
            setCellValue(saleTotalTax - purchaseTotalTax)
            cellStyle =
                styles.rightBoldCurrencyStyle
        }

        // Set column widths manually (Android-safe, avoids AWT autoSizeColumn crash)
        val columnWidths = intArrayOf(45, 18, 18, 18, 18, 18)
        columnWidths.forEachIndexed { i, w ->
            sheet.setColumnWidth(i, w * 256)
        }

        saveAndShareWorkbook(context, workbook, fileName)
    }

    suspend fun exportGstDetailed(
        context: Context,
        fileName: String,
        sales: List<Sale>,
        purchases: List<Purchase>,
        businessName: String,
        businessGstin: String,
        suppliersMap: Map<Long, Supplier>,
        allItemsMap: Map<Long, Item>,
    ) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("GST Detailed Breakup")
        sheet.setDisplayGridlines(true)

        val styles = ExcelStyles(workbook)

        // 1. Title Block
        val titleRow = sheet.createRow(0)
        titleRow.heightInPoints = 30f
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Transaction-wise GST Detailed Breakup")
        titleCell.cellStyle = styles.titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 14))

        // Business Metadata
        val metaRow1 = sheet.createRow(2)
        metaRow1.createCell(0).apply {
            setCellValue("Business Name:")
            cellStyle = styles.boldStyle
        }
        metaRow1.createCell(1).setCellValue(businessName)
        metaRow1.createCell(3).apply {
            setCellValue("GSTIN:")
            cellStyle = styles.boldStyle
        }
        metaRow1.createCell(4).setCellValue(businessGstin.ifBlank { "Not Provided" })

        val metaRow2 = sheet.createRow(3)
        metaRow2.createCell(0).apply {
            setCellValue("Generated On:")
            cellStyle = styles.boldStyle
        }
        metaRow2.createCell(1).setCellValue(dateFmt.format(Date()))

        // 2. Table Headers
        val headers =
            listOf(
                "Date", "Txn ID", "Type", "Party Name", "Party GSTIN",
                "Item Name", "HSN Code", "Tax Rate (%)", "Qty", "Unit",
                "Taxable Value (₹)", "CGST (₹)", "SGST (₹)", "IGST (₹)", "Total Amount (₹)",
            )
        val headerRow = sheet.createRow(5)
        headerRow.heightInPoints = 25f
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = styles.headerStyle
        }

        // 3. Populate Data
        var rowIndex = 6
        var sumQty = 0.0
        var sumTaxable = 0.0
        var sumCgst = 0.0
        var sumSgst = 0.0
        var sumIgst = 0.0
        var sumTotal = 0.0

        // Populate Sales Detailed
        for (sale in sales) {
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
                    businessGstin = businessGstin,
                    customerGstin = sale.customerGstin,
                )

            taxSummary.itemDetails.forEach { detail ->
                val row = sheet.createRow(rowIndex++)
                row.createCell(0).apply {
                    setCellValue(dateFmt.format(Date(sale.timestamp)))
                    cellStyle =
                        styles.centerStyle
                }
                row.createCell(1).apply {
                    setCellValue("INV${sale.id.toString().padStart(5, '0')}")
                    cellStyle =
                        styles.centerStyle
                }
                row.createCell(2).apply {
                    setCellValue("Sale")
                    cellStyle = styles.centerStyle
                }
                row.createCell(3).apply {
                    setCellValue(sale.customerName ?: "Cash / B2C Customer")
                    cellStyle =
                        styles.leftStyle
                }
                row.createCell(4).apply {
                    setCellValue(sale.customerGstin ?: "-")
                    cellStyle = styles.centerStyle
                }

                row.createCell(5).apply {
                    setCellValue(detail.cartItem.item.name)
                    cellStyle = styles.leftStyle
                }
                row.createCell(6).apply {
                    setCellValue(detail.cartItem.item.hsnCode ?: "-")
                    cellStyle =
                        styles.centerStyle
                }
                row.createCell(7).apply {
                    setCellValue(detail.cartItem.item.taxRate)
                    cellStyle = styles.percentStyle
                }
                row.createCell(8).apply {
                    setCellValue(detail.cartItem.quantity)
                    cellStyle = styles.qtyStyle
                }
                row.createCell(9).apply {
                    setCellValue(detail.cartItem.item.unit)
                    cellStyle = styles.centerStyle
                }

                row.createCell(10).apply {
                    setCellValue(detail.netAmountBeforeTax)
                    cellStyle = styles.currencyStyle
                }
                row.createCell(11).apply {
                    setCellValue(detail.cgstAmount)
                    cellStyle = styles.currencyStyle
                }
                row.createCell(12).apply {
                    setCellValue(detail.sgstAmount)
                    cellStyle = styles.currencyStyle
                }
                row.createCell(13).apply {
                    setCellValue(detail.igstAmount)
                    cellStyle = styles.currencyStyle
                }
                row.createCell(14).apply {
                    setCellValue(detail.totalAmountWithTax)
                    cellStyle = styles.currencyStyle
                }

                sumQty += detail.cartItem.quantity
                sumTaxable += detail.netAmountBeforeTax
                sumCgst += detail.cgstAmount
                sumSgst += detail.sgstAmount
                sumIgst += detail.igstAmount
                sumTotal += detail.totalAmountWithTax
            }
        }

        // Populate Purchases Detailed
        for (purchase in purchases) {
            val supplier = suppliersMap[purchase.supplierId]
            val supplierGstin = supplier?.gstin ?: ""

            val cartItems =
                purchase.items.map { pi ->
                    val actualItem =
                        allItemsMap[pi.itemId] ?: Item(
                            id = pi.itemId,
                            name = pi.itemName,
                            quantity = 0.0,
                            unit = pi.unit,
                            buyPrice = pi.buyPrice,
                            sellPrice = pi.buyPrice,
                            lowStockThreshold = 0.0,
                            category = "",
                        )
                    CartItem(item = actualItem.copy(sellPrice = pi.buyPrice), quantity = pi.quantity)
                }

            val taxSummary =
                BillingEngine.calculateInvoiceTaxes(
                    cartItems = cartItems,
                    totalDiscount = 0.0,
                    businessGstin = businessGstin,
                    customerGstin = supplierGstin,
                )

            taxSummary.itemDetails.forEach { detail ->
                val row = sheet.createRow(rowIndex++)
                row.createCell(0).apply {
                    setCellValue(dateFmt.format(Date(purchase.timestamp)))
                    cellStyle =
                        styles.centerStyle
                }
                row.createCell(1).apply {
                    setCellValue("PUR${purchase.id.toString().padStart(5, '0')}")
                    cellStyle =
                        styles.centerStyle
                }
                row.createCell(2).apply {
                    setCellValue("Purchase")
                    cellStyle = styles.centerStyle
                }
                row.createCell(3).apply {
                    setCellValue(purchase.supplierName)
                    cellStyle = styles.leftStyle
                }
                row.createCell(4).apply {
                    setCellValue(supplierGstin.ifBlank { "-" })
                    cellStyle = styles.centerStyle
                }

                row.createCell(5).apply {
                    setCellValue(detail.cartItem.item.name)
                    cellStyle = styles.leftStyle
                }
                row.createCell(6).apply {
                    setCellValue(detail.cartItem.item.hsnCode ?: "-")
                    cellStyle =
                        styles.centerStyle
                }
                row.createCell(7).apply {
                    setCellValue(detail.cartItem.item.taxRate)
                    cellStyle = styles.percentStyle
                }
                row.createCell(8).apply {
                    setCellValue(detail.cartItem.quantity)
                    cellStyle = styles.qtyStyle
                }
                row.createCell(9).apply {
                    setCellValue(detail.cartItem.item.unit)
                    cellStyle = styles.centerStyle
                }

                row.createCell(10).apply {
                    setCellValue(detail.netAmountBeforeTax)
                    cellStyle = styles.currencyStyle
                }
                row.createCell(11).apply {
                    setCellValue(detail.cgstAmount)
                    cellStyle = styles.currencyStyle
                }
                row.createCell(12).apply {
                    setCellValue(detail.sgstAmount)
                    cellStyle = styles.currencyStyle
                }
                row.createCell(13).apply {
                    setCellValue(detail.igstAmount)
                    cellStyle = styles.currencyStyle
                }
                row.createCell(14).apply {
                    setCellValue(detail.totalAmountWithTax)
                    cellStyle = styles.currencyStyle
                }

                sumQty += detail.cartItem.quantity
                sumTaxable += detail.netAmountBeforeTax
                sumCgst += detail.cgstAmount
                sumSgst += detail.sgstAmount
                sumIgst += detail.igstAmount
                sumTotal += detail.totalAmountWithTax
            }
        }

        // 4. Totals Row
        val totalRow = sheet.createRow(rowIndex)
        totalRow.createCell(0).apply {
            setCellValue("Total")
            cellStyle = styles.boldStyle
        }
        for (i in 1..7) {
            totalRow.createCell(i).apply {
                setCellValue("")
                cellStyle = styles.boldStyle
            }
        }
        totalRow.createCell(8).apply {
            setCellValue(sumQty)
            cellStyle = styles.rightBoldQtyStyle
        }
        totalRow.createCell(9).apply {
            setCellValue("")
            cellStyle = styles.boldStyle
        }
        totalRow.createCell(10).apply {
            setCellValue(sumTaxable)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(11).apply {
            setCellValue(sumCgst)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(12).apply {
            setCellValue(sumSgst)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(13).apply {
            setCellValue(sumIgst)
            cellStyle = styles.rightBoldCurrencyStyle
        }
        totalRow.createCell(14).apply {
            setCellValue(sumTotal)
            cellStyle = styles.rightBoldCurrencyStyle
        }

        // Set column widths manually (Android-safe, avoids AWT autoSizeColumn crash)
        val columnWidths = intArrayOf(12, 12, 10, 20, 18, 22, 12, 12, 10, 8, 18, 15, 15, 15, 18)
        columnWidths.forEachIndexed { i, w ->
            sheet.setColumnWidth(i, w * 256)
        }

        saveAndShareWorkbook(context, workbook, fileName)
    }

    private suspend fun saveAndShareWorkbook(
        context: Context,
        workbook: XSSFWorkbook,
        fileName: String,
    ) {
        val file = File(context.cacheDir, "$fileName.xlsx")
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
        }

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = android.content.ClipData.newRawUri("", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            val chooserIntent =
                Intent.createChooser(intent, "Share GST Report").apply {
                    clipData = android.content.ClipData.newRawUri("", uri)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            context.startActivity(chooserIntent)
        }
    }

    // Helper class to encapsulate Excel cell styles
    private class ExcelStyles(
        workbook: XSSFWorkbook,
    ) {
        val titleStyle =
            workbook.createCellStyle().apply {
                val titleFont =
                    workbook.createFont().apply {
                        bold = true
                        fontHeightInPoints = 16.toShort()
                        color = IndexedColors.DARK_BLUE.index
                    }
                setFont(titleFont)
                alignment = HorizontalAlignment.LEFT
                verticalAlignment = VerticalAlignment.CENTER
            }

        val sectionHeaderStyle =
            workbook.createCellStyle().apply {
                val sFont =
                    workbook.createFont().apply {
                        bold = true
                        fontHeightInPoints = 12.toShort()
                        color = IndexedColors.DARK_BLUE.index
                    }
                setFont(sFont)
                alignment = HorizontalAlignment.LEFT
                verticalAlignment = VerticalAlignment.CENTER
            }

        val headerStyle =
            workbook.createCellStyle().apply {
                val headerFont =
                    workbook.createFont().apply {
                        bold = true
                        color = IndexedColors.WHITE.index
                        fontHeightInPoints = 11.toShort()
                    }
                fillForegroundColor = IndexedColors.CORNFLOWER_BLUE.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
                setFont(headerFont)
                setBorders(this, BorderStyle.THIN)
            }

        val boldStyle =
            workbook.createCellStyle().apply {
                val boldFont = workbook.createFont().apply { bold = true }
                setFont(boldFont)
                alignment = HorizontalAlignment.LEFT
            }

        val currencyStyle =
            workbook.createCellStyle().apply {
                setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"))
                alignment = HorizontalAlignment.RIGHT
                setBorders(this, BorderStyle.THIN)
            }

        val rightBoldCurrencyStyle =
            workbook.createCellStyle().apply {
                val boldFont = workbook.createFont().apply { bold = true }
                setFont(boldFont)
                setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"))
                alignment = HorizontalAlignment.RIGHT
                borderTop = BorderStyle.THIN
                borderBottom = BorderStyle.DOUBLE
            }

        val qtyStyle =
            workbook.createCellStyle().apply {
                setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"))
                alignment = HorizontalAlignment.RIGHT
                setBorders(this, BorderStyle.THIN)
            }

        val rightBoldQtyStyle =
            workbook.createCellStyle().apply {
                val boldFont = workbook.createFont().apply { bold = true }
                setFont(boldFont)
                setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"))
                alignment = HorizontalAlignment.RIGHT
                borderTop = BorderStyle.THIN
                borderBottom = BorderStyle.DOUBLE
            }

        val percentStyle =
            workbook.createCellStyle().apply {
                setDataFormat(workbook.createDataFormat().getFormat("0.0%"))
                alignment = HorizontalAlignment.RIGHT
                setBorders(this, BorderStyle.THIN)
            }

        val centerStyle =
            workbook.createCellStyle().apply {
                alignment = HorizontalAlignment.CENTER
                setBorders(this, BorderStyle.THIN)
            }

        val leftStyle =
            workbook.createCellStyle().apply {
                alignment = HorizontalAlignment.LEFT
                setBorders(this, BorderStyle.THIN)
            }

        private fun setBorders(
            style: CellStyle,
            border: BorderStyle,
        ) {
            style.borderBottom = border
            style.borderTop = border
            style.borderLeft = border
            style.borderRight = border
        }
    }
}
