package com.storebook.inventoryapp.printing

import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import java.io.FileOutputStream

class PrintManagerAdapter(
    private val context: android.content.Context,
) {
    fun printPdf(
        printName: String,
        pdfBytes: ByteArray,
        onToast: (String) -> Unit = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
    ): Boolean {
        val printManager =
            context.getSystemService(android.content.Context.PRINT_SERVICE) as? PrintManager
                ?: run {
                    onToast("Print service unavailable")
                    return false
                }

        onToast("Print started: $printName")

        printManager.print(
            printName,
            PdfBytesAdapter(pdfBytes, onToast),
            null,
        )

        return true
    }

    private inner class PdfBytesAdapter(
        private val pdfBytes: ByteArray,
        private val onToast: (String) -> Unit,
    ) : PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: android.os.CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: android.os.Bundle?,
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }

            val info =
                PrintDocumentInfo
                    .Builder("invoice.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()

            callback?.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: android.os.CancellationSignal?,
            callback: WriteResultCallback?,
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onWriteCancelled()
                return
            }

            try {
                destination?.fileDescriptor?.let { fd ->
                    FileOutputStream(fd).use { outputStream ->
                        outputStream.write(pdfBytes)
                    }
                }

                onToast("Print completed")
                callback?.onWriteFinished(pages ?: arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                if (e is java.util.concurrent.CancellationException) throw e
                val message = e.message ?: "Unknown error"
                onToast("Print failed: $message")
                callback?.onWriteFailed(message)
            }
        }
    }

    companion object {
        /**
         * The system print framework requires no runtime permission.
         * [PrintManager] is resolved through [android.content.Context.PRINT_SERVICE].
         */
    }
}
