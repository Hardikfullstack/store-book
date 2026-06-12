package com.storebook.inventoryapp.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.storebook.inventoryapp.data.billing.BillingEngine
import com.storebook.inventoryapp.data.repository.CartItem
import com.storebook.inventoryapp.data.repository.Sale
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoicePdfGenerator {
    fun generateInvoicePdf(
        context: Context,
        sale: Sale,
        cartItems: List<CartItem>,
        shopName: String,
        shopAddress: String,
        shopGstin: String,
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint =
            Paint().apply {
                color = Color.BLACK
                textSize = 14f
            }

        var yPos = 50f
        val leftMargin = 50f
        val rightMargin = 545f

        // 1. Header (Shop Details)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText(shopName, leftMargin, yPos, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        yPos += 20f
        if (shopAddress.isNotBlank()) {
            val lines = shopAddress.split("\n")
            for (line in lines) {
                canvas.drawText(line, leftMargin, yPos, paint)
                yPos += 15f
            }
        }
        val actualShopGstin = sale.businessGstin ?: shopGstin
        if (actualShopGstin.isNotBlank()) {
            canvas.drawText("GSTIN: $actualShopGstin", leftMargin, yPos, paint)
            yPos += 15f
        }

        // Title
        yPos += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("TAX INVOICE", pageInfo.pageWidth / 2f, yPos, paint)
        paint.textAlign = Paint.Align.LEFT

        // 2. Invoice Meta & Customer Details
        yPos += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(sale.timestamp))
        canvas.drawText("Invoice No: #${sale.id}", leftMargin, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Date: $dateStr", rightMargin, yPos, paint)
        paint.textAlign = Paint.Align.LEFT

        yPos += 20f
        canvas.drawText("Customer Name: ${sale.customerName ?: "Cash Customer"}", leftMargin, yPos, paint)
        yPos += 15f
        if (!sale.customerAddress.isNullOrBlank()) {
            val lines = sale.customerAddress.split("\n")
            for ((index, line) in lines.withIndex()) {
                canvas.drawText(if (index == 0) "Address: $line" else line, leftMargin, yPos, paint)
                yPos += 15f
            }
        }
        if (!sale.customerGstin.isNullOrBlank()) {
            canvas.drawText("GSTIN: ${sale.customerGstin}", leftMargin, yPos, paint)
            yPos += 15f
        }

        // 3. Table Header
        yPos += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Item", leftMargin, yPos, paint)
        canvas.drawText("Qty", 300f, yPos, paint)
        canvas.drawText("Rate", 400f, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total", rightMargin, yPos, paint)
        paint.textAlign = Paint.Align.LEFT
        yPos += 10f
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)

        // 4. Items
        yPos += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        var subtotal = 0.0

        for (item in cartItems) {
            val itemName = item.item.name.take(20) + if (item.item.name.length > 20) "..." else ""
            canvas.drawText(itemName, leftMargin, yPos, paint)
            canvas.drawText("${item.quantity}", 300f, yPos, paint)
            canvas.drawText(String.format("Rs %.2f", item.item.sellPrice), 400f, yPos, paint)
            val lineTotal = item.item.sellPrice * item.quantity
            subtotal += lineTotal
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("Rs %.2f", lineTotal), rightMargin, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
            yPos += 20f
        }

        yPos += 10f
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)

        // 5. Totals & Tax
        yPos += 20f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Subtotal: ${String.format("Rs %.2f", subtotal)}", rightMargin, yPos, paint)
        yPos += 20f
        if (sale.discountAmount > 0) {
            canvas.drawText("Discount: -${String.format("Rs %.2f", sale.discountAmount)}", rightMargin, yPos, paint)
            yPos += 20f
        }

        val taxSummary =
            BillingEngine.calculateInvoiceTaxes(
                cartItems = cartItems,
                totalDiscount = sale.discountAmount,
                businessGstin = actualShopGstin,
                customerGstin = sale.customerGstin,
            )

        val totalTax = taxSummary.totalCgst + taxSummary.totalSgst + taxSummary.totalIgst
        if (totalTax > 0) {
            val taxesByRate = taxSummary.itemDetails.groupBy { it.cartItem.item.taxRate }
            taxesByRate.forEach { (rate, details) ->
                if (rate > 0) {
                    val sumCgst = details.sumOf { it.cgstAmount }
                    val sumSgst = details.sumOf { it.sgstAmount }
                    val sumIgst = details.sumOf { it.igstAmount }

                    if (sumCgst > 0 || sumSgst > 0) {
                        val halfRate = String.format(Locale.US, "%.1f", rate / 2).replace(".0", "")
                        if (sumCgst > 0) {
                            canvas.drawText(
                                "CGST ($halfRate%): ${String.format("Rs %.2f", sumCgst)}",
                                rightMargin,
                                yPos,
                                paint,
                            )
                            yPos += 20f
                        }
                        if (sumSgst > 0) {
                            canvas.drawText(
                                "SGST ($halfRate%): ${String.format("Rs %.2f", sumSgst)}",
                                rightMargin,
                                yPos,
                                paint,
                            )
                            yPos += 20f
                        }
                    } else if (sumIgst > 0) {
                        val fmtRate = String.format(Locale.US, "%.1f", rate).replace(".0", "")
                        canvas.drawText(
                            "IGST ($fmtRate%): ${String.format("Rs %.2f", sumIgst)}",
                            rightMargin,
                            yPos,
                            paint,
                        )
                        yPos += 20f
                    }
                }
            }
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("Grand Total: ${String.format("Rs %.2f", sale.totalAmount)}", rightMargin, yPos, paint)

        document.finishPage(page)

        // Clean up any old cached PDFs to ensure no permanent storage buildup
        context.cacheDir.listFiles()?.forEach {
            if (it.name.startsWith("Invoice_") && it.name.endsWith(".pdf") && it.name != "Invoice_${sale.id}.pdf") {
                it.delete()
            }
        }

        val file = File(context.cacheDir, "Invoice_${sale.id}.pdf")
        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
        document.close()

        return file
    }
}
