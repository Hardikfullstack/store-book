package com.storebook.inventoryapp.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.storebook.inventoryapp.data.repository.UdhaarEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UdhaarPdfGenerator {
    fun generateUdhaarStatement(
        context: Context,
        customerName: String,
        netBalance: Double,
        ledgerEntries: List<UdhaarEntry>,
        shopName: String,
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }

        var yPos = 50f
        val leftMargin = 50f

        // Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText(if (shopName.isNotBlank()) shopName else "StoreBook", leftMargin, yPos, paint)

        yPos += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 18f
        canvas.drawText("Udhaar Statement: $customerName", leftMargin, yPos, paint)

        yPos += 20f
        paint.textSize = 14f
        val balanceText = if (netBalance > 0) "You owe: ₹${netBalance}" else if (netBalance < 0) "Advance: ₹${-netBalance}" else "Settled"
        paint.color = if (netBalance > 0) Color.RED else Color.rgb(13, 148, 136)
        canvas.drawText("Net Balance: $balanceText", leftMargin, yPos, paint)
        paint.color = Color.BLACK

        yPos += 40f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Date", leftMargin, yPos, paint)
        canvas.drawText("Type", leftMargin + 150f, yPos, paint)
        canvas.drawText("Amount", leftMargin + 300f, yPos, paint)
        canvas.drawText("Notes", leftMargin + 400f, yPos, paint)

        yPos += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateFmt = SimpleDateFormat("dd MMM yy", Locale.getDefault())

        for (entry in ledgerEntries) {
            canvas.drawText(dateFmt.format(Date(entry.timestamp)), leftMargin, yPos, paint)
            val isCredit = entry.type == "CREDIT"
            paint.color = if (isCredit) Color.RED else Color.rgb(13, 148, 136)
            canvas.drawText(if (isCredit) "Given" else "Received", leftMargin + 150f, yPos, paint)
            canvas.drawText("₹${entry.amount}", leftMargin + 300f, yPos, paint)
            paint.color = Color.BLACK
            val notes = entry.notes ?: "-"
            canvas.drawText(if (notes.length > 20) notes.take(17) + "..." else notes, leftMargin + 400f, yPos, paint)
            yPos += 20f

            if (yPos > 800f) break // Simple pagination prevention for demo
        }

        document.finishPage(page)

        try {
            val file = File(context.cacheDir, "Statement_${customerName.replace(" ", "_")}.pdf")
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }
}
