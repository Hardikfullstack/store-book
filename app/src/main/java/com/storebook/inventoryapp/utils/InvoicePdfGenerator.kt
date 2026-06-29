package com.storebook.inventoryapp.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.storebook.inventoryapp.data.billing.BillingEngine
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.Sale
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
        val prefs = context.getSharedPreferences("storebook_prefs", Context.MODE_PRIVATE)
        val isThermal = prefs.getBoolean("use_thermal_printer", false)

        return if (isThermal) {
            generateThermalInvoice(context, sale, cartItems, shopName, shopAddress, shopGstin)
        } else {
            generateA4Invoice(context, sale, cartItems, shopName, shopAddress, shopGstin)
        }
    }

    private fun generateThermalInvoice(
        context: Context,
        sale: Sale,
        cartItems: List<CartItem>,
        shopName: String,
        shopAddress: String,
        shopGstin: String,
    ): File? {
        val document = PdfDocument()
        
        // Calculate dynamic height for thermal receipt
        val itemHeight = 35f
        val headerHeight = 170f
        val footerHeight = 150f
        val totalHeight = (headerHeight + (cartItems.size * itemHeight) + footerHeight).toInt()
        
        // 3-inch thermal printer width is 80mm ~ 226 points
        val pageInfo = PdfDocument.PageInfo.Builder(226, totalHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        var yPos = 20f
        val leftMargin = 10f
        val rightMargin = 216f

        // Shop Name
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText(shopName, leftMargin, yPos, paint)
        yPos += 15f

        // Shop Address & GSTIN
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 7f
        if (shopAddress.isNotBlank()) {
            val lines = shopAddress.split("\n")
            for (line in lines) {
                canvas.drawText(line, leftMargin, yPos, paint)
                yPos += 10f
            }
        }
        val actualShopGstin = sale.businessGstin ?: shopGstin
        if (actualShopGstin.isNotBlank()) {
            canvas.drawText("GSTIN: $actualShopGstin", leftMargin, yPos, paint)
            yPos += 10f
        }

        // Divider
        yPos += 5f
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
        yPos += 10f

        // Invoice Meta
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(sale.timestamp))
        val labelNo = if (sale.type == "ESTIMATE") "Quote:" else "Inv:"
        canvas.drawText("$labelNo #${sale.id}", leftMargin, yPos, paint)
        yPos += 10f
        canvas.drawText("Date: $dateStr", leftMargin, yPos, paint)
        yPos += 10f
        canvas.drawText("Cust: ${sale.customerName ?: "Cash Customer"}", leftMargin, yPos, paint)
        yPos += 10f

        if (!sale.customerGstin.isNullOrBlank()) {
            canvas.drawText("CGSTIN: ${sale.customerGstin}", leftMargin, yPos, paint)
            yPos += 10f
        }

        // Divider
        yPos += 5f
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
        yPos += 10f

        // Table Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Item", leftMargin, yPos, paint)
        canvas.drawText("Qty", 120f, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total", rightMargin, yPos, paint)
        paint.textAlign = Paint.Align.LEFT
        
        yPos += 5f
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
        yPos += 12f

        // Table Items
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var subtotal = 0.0
        for (item in cartItems) {
            val lineTotal = item.item.sellPrice * item.quantity
            subtotal += lineTotal

            // Draw item name, wrap if too long
            val maxWidth = 100f
            val rawName = item.item.name
            val chars = paint.breakText(rawName, true, maxWidth, null)
            val displayName = if (chars < rawName.length) rawName.substring(0, chars) + ".." else rawName

            canvas.drawText(displayName, leftMargin, yPos, paint)
            canvas.drawText("${item.quantity} ${item.item.unit}", 120f, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("%.2f", lineTotal), rightMargin, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
            yPos += 15f
        }

        // Divider
        yPos += 2f
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
        yPos += 10f

        // Summary
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Subtotal: ${String.format("%.2f", subtotal)}", rightMargin, yPos, paint)
        yPos += 10f
        if (sale.discountAmount > 0) {
            canvas.drawText("Discount: -${String.format("%.2f", sale.discountAmount)}", rightMargin, yPos, paint)
            yPos += 10f
        }

        val taxSummary = BillingEngine.calculateInvoiceTaxes(
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
                            canvas.drawText("CGST ($halfRate%): ${String.format("%.2f", sumCgst)}", rightMargin, yPos, paint)
                            yPos += 10f
                        }
                        if (sumSgst > 0) {
                            canvas.drawText("SGST ($halfRate%): ${String.format("%.2f", sumSgst)}", rightMargin, yPos, paint)
                            yPos += 10f
                        }
                    } else if (sumIgst > 0) {
                        val fmtRate = String.format(Locale.US, "%.1f", rate).replace(".0", "")
                        canvas.drawText("IGST ($fmtRate%): ${String.format("%.2f", sumIgst)}", rightMargin, yPos, paint)
                        yPos += 10f
                    }
                }
            }
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("Grand Total: ${String.format("Rs %.2f", sale.totalAmount)}", rightMargin, yPos, paint)

        // Thank you note
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 7f
        paint.textAlign = Paint.Align.CENTER
        yPos += 20f
        canvas.drawText("Thank you for your business!", pageInfo.pageWidth / 2f, yPos, paint)

        document.finishPage(page)
        
        val prefix = if (sale.type == "ESTIMATE") "Estimate" else "Invoice"
        context.cacheDir.listFiles()?.forEach {
            if ((it.name.startsWith("Invoice_") || it.name.startsWith("Estimate_")) && it.name.endsWith(".pdf") && it.name != "${prefix}_${sale.id}.pdf") {
                it.delete()
            }
        }

        val file = File(context.cacheDir, "${prefix}_${sale.id}.pdf")
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

    private fun generateA4Invoice(
        context: Context,
        sale: Sale,
        cartItems: List<CartItem>,
        shopName: String,
        shopAddress: String,
        shopGstin: String,
    ): File? {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val leftMargin = 50f
        val rightMargin = 545f

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var yPos = 50f

        // Draw header helper
        val drawHeader = {
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
            val titleText = if (sale.type == "ESTIMATE") "QUOTATION / ESTIMATE" else "TAX INVOICE"
            canvas.drawText(titleText, pageWidth / 2f, yPos, paint)
            paint.textAlign = Paint.Align.LEFT

            // Invoice Meta & Customer
            yPos += 30f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 12f

            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = dateFormat.format(Date(sale.timestamp))
            val labelNo = if (sale.type == "ESTIMATE") "Quote No:" else "Invoice No:"
            canvas.drawText("$labelNo #${sale.id}", leftMargin, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Date: $dateStr", rightMargin, yPos, paint)
            paint.textAlign = Paint.Align.LEFT

            yPos += 20f
            canvas.drawText("Customer Name: ${sale.customerName ?: "Cash Customer"}", leftMargin, yPos, paint)
            yPos += 15f
            val address = sale.customerAddress
            if (!address.isNullOrBlank()) {
                val lines = address.split("\n")
                for ((index, line) in lines.withIndex()) {
                    canvas.drawText(if (index == 0) "Address: $line" else line, leftMargin, yPos, paint)
                    yPos += 15f
                }
            }
            if (!sale.customerGstin.isNullOrBlank()) {
                canvas.drawText("GSTIN: ${sale.customerGstin}", leftMargin, yPos, paint)
                yPos += 15f
            }

            // Table Header
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
            yPos += 20f
        }

        drawHeader()

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var subtotal = 0.0

        for (item in cartItems) {
            val lineTotal = item.item.sellPrice * item.quantity
            subtotal += lineTotal

            // Check if page overflows
            if (yPos > pageHeight - 150f) {
                // Finish current page
                document.finishPage(page)
                // Spawn new page
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
                drawHeader()
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val maxWidth = 240f
            val rawName = item.item.name
            val breakChars1 = paint.breakText(rawName, true, maxWidth, null)

            if (breakChars1 < rawName.length) {
                val line1 = rawName.substring(0, breakChars1)
                val remaining = rawName.substring(breakChars1)
                val breakChars2 = paint.breakText(remaining, true, maxWidth - paint.measureText("..."), null)
                val line2 = if (breakChars2 < remaining.length) remaining.substring(0, breakChars2) + "..." else remaining

                canvas.drawText(line1, leftMargin, yPos, paint)
                canvas.drawText("${item.quantity} ${item.item.unit}", 300f, yPos, paint)
                canvas.drawText(String.format("Rs %.2f", item.item.sellPrice), 400f, yPos, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(String.format("Rs %.2f", lineTotal), rightMargin, yPos, paint)
                paint.textAlign = Paint.Align.LEFT

                yPos += 15f
                canvas.drawText(line2, leftMargin, yPos, paint)
                yPos += 20f
            } else {
                canvas.drawText(rawName, leftMargin, yPos, paint)
                canvas.drawText("${item.quantity} ${item.item.unit}", 300f, yPos, paint)
                canvas.drawText(String.format("Rs %.2f", item.item.sellPrice), 400f, yPos, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(String.format("Rs %.2f", lineTotal), rightMargin, yPos, paint)
                paint.textAlign = Paint.Align.LEFT
                yPos += 20f
            }
        }

        // Draw summary section (checks if it fits on page, else spawns new page)
        if (yPos > pageHeight - 150f) {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPos = 50f
        }

        yPos += 10f
        canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
        yPos += 20f

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Subtotal: ${String.format("Rs %.2f", subtotal)}", rightMargin, yPos, paint)
        yPos += 20f
        if (sale.discountAmount > 0) {
            canvas.drawText("Discount: -${String.format("Rs %.2f", sale.discountAmount)}", rightMargin, yPos, paint)
            yPos += 20f
        }

        val actualShopGstin = sale.businessGstin ?: shopGstin
        val taxSummary = BillingEngine.calculateInvoiceTaxes(
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
                            canvas.drawText("CGST ($halfRate%): ${String.format("Rs %.2f", sumCgst)}", rightMargin, yPos, paint)
                            yPos += 20f
                        }
                        if (sumSgst > 0) {
                            canvas.drawText("SGST ($halfRate%): ${String.format("Rs %.2f", sumSgst)}", rightMargin, yPos, paint)
                            yPos += 20f
                        }
                    } else if (sumIgst > 0) {
                        val fmtRate = String.format(Locale.US, "%.1f", rate).replace(".0", "")
                        canvas.drawText("IGST ($fmtRate%): ${String.format("Rs %.2f", sumIgst)}", rightMargin, yPos, paint)
                        yPos += 20f
                    }
                }
            }
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("Grand Total: ${String.format("Rs %.2f", sale.totalAmount)}", rightMargin, yPos, paint)

        document.finishPage(page)

        val prefix = if (sale.type == "ESTIMATE") "Estimate" else "Invoice"
        context.cacheDir.listFiles()?.forEach {
            if ((it.name.startsWith("Invoice_") || it.name.startsWith("Estimate_")) && it.name.endsWith(".pdf") && it.name != "${prefix}_${sale.id}.pdf") {
                it.delete()
            }
        }

        val file = File(context.cacheDir, "${prefix}_${sale.id}.pdf")
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
