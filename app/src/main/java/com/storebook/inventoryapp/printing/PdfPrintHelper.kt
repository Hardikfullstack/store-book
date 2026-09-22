package com.storebook.inventoryapp.printing

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.InvoiceSettings
import com.storebook.inventoryapp.shared.domain.models.Sale
import com.storebook.inventoryapp.utils.InvoicePdfGenerator

object PdfPrintHelper {
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun postOnMain(r: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r()
        } else {
            mainHandler.post(r)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun printA4(
        context: Context,
        sale: Sale,
        items: List<CartItem>,
        shopName: String = "StoreBook POS",
        shopAddress: String = "",
        shopGstin: String = "",
        invoiceSettings: InvoiceSettings? = null,
    ): Boolean {
        if (items.isEmpty()) {
            postOnMain { toast(context, "Cannot print — no items in sale") }
            return false
        }

        val pdfFile =
            InvoicePdfGenerator.generateInvoicePdf(
                context,
                sale,
                items,
                shopName,
                shopAddress,
                shopGstin,
                invoiceSettings,
            ) ?: run {
                postOnMain { toast(context, "Failed to generate invoice PDF") }
                return false
            }

        val pdfBytes = pdfFile.readBytes()

        val adapter = PrintManagerAdapter(context)
        val prefix = if (sale.type == "ESTIMATE") "Estimate" else "Invoice"
        return adapter.printPdf(
            printName = "$prefix #${sale.id}",
            pdfBytes = pdfBytes,
            onToast = { msg ->
                postOnMain {
                    toast(
                        context,
                        msg,
                    )
                }
            },
        )
    }

    fun printThermal(
        context: Context,
        sale: Sale,
        items: List<CartItem>,
        width: PrinterWidth,
        invoiceSettings: InvoiceSettings? = null,
    ): Boolean {
        if (items.isEmpty()) {
            postOnMain { toast(context, "Cannot print — no items in sale") }
            return false
        }

        val success =
            EscPosPrinterService().printThermalReceipt(
                context,
                sale,
                items,
                width,
                invoiceSettings,
            )

        postOnMain {
            toast(
                context,
                if (success) "Receipt sent to printer" else "Printing failed — check printer connection",
            )
        }

        return success
    }

    private fun toast(
        context: Context,
        message: String,
    ) {
        android.widget.Toast
            .makeText(context, message, android.widget.Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * No runtime permission is needed for the system print framework —
     * [PrintManager] is resolved via [Context.PRINT_SERVICE].  Only if you
     * write PDFs to shared external storage would you need
     * [android.Manifest.permission.MANAGE_EXTERNAL_STORAGE] on Android 11+.
     */
}
