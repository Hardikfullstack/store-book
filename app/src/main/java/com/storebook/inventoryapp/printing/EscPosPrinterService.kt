package com.storebook.inventoryapp.printing

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.storebook.inventoryapp.data.billing.BillingEngine
import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.InvoiceSettings
import com.storebook.inventoryapp.shared.domain.models.Sale
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "EscPosPrinterService"

@Suppress("MagicNumber")
private object Esc {
    const val ESC = 0x1B.toByte()
    const val GS = 0x1D.toByte()
    const val FS = 0x1C.toByte()

    val INIT = byteArrayOf(ESC, 0x40)
    val CODE_TABLE_ASCII = byteArrayOf(ESC, 0x74, 0x00)
    val FEED_AND_CUT_PARTIAL = byteArrayOf(GS, 0x69, 0x00)
    val FEED_AND_CUT_FULL = byteArrayOf(GS, 0x69, 0x01)

    const val ALIGN_CENTER = 0x01
    const val ALIGN_LEFT = 0x00
    const val ALIGN_RIGHT = 0x02

    const val STYLE_NORMAL = 0x00.toByte()
    const val STYLE_BOLD = 0x08.toByte()
    const val STYLE_DOUBLE = 0x11.toByte()

    fun boldOn() = byteArrayOf(GS, 0x21, STYLE_BOLD)

    fun doubleSize() = byteArrayOf(GS, 0x21, STYLE_DOUBLE)

    fun normal() = byteArrayOf(GS, 0x21, STYLE_NORMAL)

    fun align(n: Int) = byteArrayOf(ESC, 0x61, n.toByte())
}

enum class PrinterWidth(
    val columns: Int,
    val label: String,
) {
    MM58(32, "58mm"),
    MM80(42, "80mm"),
}

class EscPosPrinterService {
    fun encodeReceipt(
        sale: Sale,
        cartItems: List<CartItem>,
        width: PrinterWidth,
        invoiceSettings: InvoiceSettings? = null,
    ): ByteArray {
        val buf = ReceiptBuffer(width.columns)

        buf.init()
        buf.codeTable()

        val shopName = effectiveShopName(invoiceSettings, sale)
        buf.center {
            buf.bold()
            buf.text(shopName)
            buf.normal()
        }
        buf.newline()

        val address = effectiveAddress(invoiceSettings, sale)
        if (address.isNotBlank()) {
            buf.center { buf.text(address) }
            buf.newline()
        }

        val businessGstin = effectiveBusinessGstin(invoiceSettings, sale)
        if (businessGstin.isNotBlank()) {
            buf.center { buf.text("GSTIN: $businessGstin") }
            buf.newline()
        }

        buf.newline()
        buf.divider('-', 16)
        buf.newline()

        val typeLabel = if (sale.type == "ESTIMATE") "Quotation" else "Invoice"
        buf.text("$typeLabel: #${sale.id}")
        buf.newline()

        val dateStr = formatDate(sale.timestamp)
        buf.text("Date: $dateStr")
        buf.newline()

        val customerDisplay = sale.customerName?.takeIf { it.isNotBlank() } ?: "Cash Customer"
        buf.text("Customer: $customerDisplay")
        buf.newline()

        sale.customerGstin?.takeIf { it.isNotBlank() }?.let {
            buf.text("CGSTIN: $it")
            buf.newline()
        }

        sale.customerAddress?.takeIf { it.isNotBlank() }?.let {
            buf.text("Addr: $it")
            buf.newline()
        }

        buf.divider('-', 16)
        buf.newline()

        buf.bold()
        buf.tableHeader()
        buf.normal()
        buf.divider('-', 16)
        buf.newline()

        val taxSummary =
            BillingEngine.calculateInvoiceTaxes(
                cartItems,
                sale.discountAmount,
                businessGstin.takeIf { it.isNotBlank() },
                sale.customerGstin?.takeIf { it.isNotBlank() } ?: "",
            )

        val showGst =
            invoiceSettings?.showGstBreakdown != false &&
                taxSummary.itemDetails.any { it.netAmountBeforeTax > 0 }

        for ((index, cartItem) in cartItems.withIndex()) {
            val lineTotal = cartItem.item.sellPrice * cartItem.quantity

            buf.plain()
            buf.lineItem(cartItem, lineTotal)
            buf.newline()

            if (showGst && cartItem.item.taxRate > 0 && index < taxSummary.itemDetails.size) {
                val td = taxSummary.itemDetails[index]
                val taxLine = buildTaxDetailLine(td, cartItem.item.taxRate)
                if (taxLine.isNotBlank()) {
                    buf.indent()
                    buf.text(taxLine)
                    buf.newline()
                }
            }
        }

        buf.divider('-', 16)
        buf.newline()

        val netAmount = taxSummary.netTaxableAmount
        buf.right { buf.text("Subtotal: ${fmt(netAmount)}") }
        buf.newline()

        if (sale.discountAmount > 0.005) {
            buf.right { buf.text("Discount: -${fmt(sale.discountAmount)}") }
            buf.newline()
        }

        fun firstTaxRate(): Double = cartItems.firstOrNull { it.item.taxRate > 0 }?.item?.taxRate ?: 0.0

        if (showGst && taxSummary.totalCgst > 0.005) {
            val halfRate = halfRateStr(firstTaxRate() / 2)
            buf.right { buf.text("CGST ($halfRate%): ${fmt(taxSummary.totalCgst)}") }
            buf.newline()
        }

        if (showGst && taxSummary.totalSgst > 0.005) {
            val halfRate = halfRateStr(firstTaxRate() / 2)
            buf.right { buf.text("SGST ($halfRate%): ${fmt(taxSummary.totalSgst)}") }
            buf.newline()
        }

        if (showGst && taxSummary.totalIgst > 0.005) {
            val fullRate = halfRateStr(firstTaxRate())
            buf.right { buf.text("IGST ($fullRate%): ${fmt(taxSummary.totalIgst)}") }
            buf.newline()
        }

        buf.divider('=', 16)
        buf.double()
        buf.center { buf.text("GRAND TOTAL: Rs ${fmt(sale.totalAmount)}") }
        buf.normal()
        buf.divider('=', 16)
        buf.newline()

        val footer = invoiceSettings?.footerText ?: "Thank you for shopping with us!"
        if (footer.isNotBlank()) {
            buf.center { buf.text(footer) }
            buf.newline()
        }

        sale.notes?.takeIf { it.isNotBlank() }?.let {
            buf.text("Notes: $it")
            buf.newline()
        }

        buf.feedLines(3)
        buf.cutPartial()

        return buf.toByteArray()
    }

    fun printThermalReceipt(
        context: Context,
        sale: Sale,
        cartItems: List<CartItem>,
        width: PrinterWidth,
        invoiceSettings: InvoiceSettings? = null,
    ): Boolean {
        if (cartItems.isEmpty()) {
            Log.w(TAG, "printThermalReceipt called with empty cart")
            return false
        }

        val bytes = encodeReceipt(sale, cartItems, width, invoiceSettings)
        return sendBytes(context, bytes)
    }

    fun printViaBluetooth(
        socket: BluetoothSocket,
        receiptBytes: ByteArray,
    ): Boolean =
        try {
            val out = socket.outputStream
            out.write(receiptBytes)
            out.flush()
            Log.d(TAG, "Bluetooth receipt sent (${receiptBytes.size} bytes)")
            true
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) throw e
            Log.e(TAG, "Bluetooth print failed", e)
            false
        }

    private fun sendBytes(
        context: Context,
        bytes: ByteArray,
    ): Boolean {
        val usbDevice = findPrinterUsbDevice(context)
        if (usbDevice != null) {
            return printViaUsb(context, usbDevice, bytes)
        }

        Log.w(TAG, "No USB thermal printer found; pair a printer or inject a BluetoothSocket")
        return false
    }

    private fun findPrinterUsbDevice(context: Context): UsbDevice? {
        val manager =
            context.getSystemService(Context.USB_SERVICE) as? UsbManager
                ?: return null

        for (device in manager.deviceList.values) {
            if (isLikelyPrinter(device)) {
                return device
            }
        }
        return null
    }

    private fun isLikelyPrinter(device: UsbDevice): Boolean {
        return try {
            val deviceClass = device.deviceClass
            if (deviceClass == UsbConstants.USB_CLASS_PRINTER) return true
            if (deviceClass == UsbConstants.USB_CLASS_VENDOR_SPEC && device.interfaceCount > 0) {
                val iface = device.getInterface(0)
                iface.interfaceClass == UsbConstants.USB_CLASS_PRINTER ||
                    iface.interfaceClass == UsbConstants.USB_CLASS_VENDOR_SPEC
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun printViaUsb(
        context: Context,
        usbDevice: UsbDevice,
        bytes: ByteArray,
    ): Boolean {
        val manager =
            context.getSystemService(Context.USB_SERVICE) as? UsbManager
                ?: return false

        if (!manager.hasPermission(usbDevice)) {
            Log.w(TAG, "No USB permission for device ${usbDevice.deviceName} - request it first")
            return false
        }

        val connection =
            manager.openDevice(usbDevice)
                ?: run {
                    Log.e(TAG, "Cannot open USB device: ${usbDevice.deviceName}")
                    return false
                }

        var success = false
        try {
            val iface = usbDevice.getInterface(0)
            if (connection.claimInterface(iface, true)) {
                val bulkOutEndpoint =
                    (0 until iface.endpointCount)
                        .firstOrNull { i ->
                            val ep = iface.getEndpoint(i)
                            ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                                (ep.address and 0x80) == 0x00
                        }?.let { iface.getEndpoint(it) } ?: iface.getEndpoint(0)

                val written = connection.bulkTransfer(bulkOutEndpoint, bytes, bytes.size, 5000)
                success = written == bytes.size
                if (!success) {
                    Log.w(TAG, "USB bulkTransfer wrote $written / ${bytes.size} bytes")
                }
            } else {
                Log.e(TAG, "Cannot claim interface on device ${usbDevice.deviceName}")
            }
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) throw e
            Log.e(TAG, "USB print error", e)
        } finally {
            connection.close()
        }

        return success
    }

    private fun effectiveShopName(
        settings: InvoiceSettings?,
        sale: Sale,
    ): String =
        settings?.shopName?.takeIf { it.isNotBlank() }
            ?: sale.businessAddress
                ?.split(",")
                ?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
            ?: "StoreBook POS"

    private fun effectiveAddress(
        settings: InvoiceSettings?,
        sale: Sale,
    ): String =
        settings?.shopAddress?.takeIf { it.isNotBlank() }
            ?: sale.businessAddress?.takeIf { it.isNotBlank() }
            ?: ""

    private fun effectiveBusinessGstin(
        settings: InvoiceSettings?,
        sale: Sale,
    ): String =
        sale.businessGstin?.takeIf { it.isNotBlank() }
            ?: settings?.shopGstin?.takeIf { it.isNotBlank() }
            ?: ""

    private fun formatDate(timestamp: Long): String =
        try {
            SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
                .format(Date(timestamp))
        } catch (_: Exception) {
            "$timestamp"
        }

    private fun fmt(amount: Double): String = String.format("%.2f", amount)

    private fun halfRateStr(rate: Double): String = String.format(Locale.US, "%.1f", rate).replace(".0", "")

    private fun buildTaxDetailLine(
        taxDetails: com.storebook.inventoryapp.data.billing.ItemTaxDetails,
        taxRate: Double,
    ): String {
        if (taxDetails.igstAmount > 0) {
            return "IGST@${halfRateStr(taxRate)}%: ${fmt(taxDetails.igstAmount)}"
        }

        val half = halfRateStr(taxRate / 2)
        return if (taxDetails.cgstAmount > 0 && taxDetails.sgstAmount > 0) {
            "CGST@$half%: ${fmt(taxDetails.cgstAmount)} | SGST@$half%: ${fmt(taxDetails.sgstAmount)}"
        } else if (taxDetails.cgstAmount > 0) {
            "CGST@$half%: ${fmt(taxDetails.cgstAmount)}"
        } else {
            ""
        }
    }

    @Suppress("TooManyFunctions")
    private inner class ReceiptBuffer(
        val columnCount: Int,
    ) {
        private val buffer = ByteArrayOutputStream()

        fun init() = appendRaw(Esc.INIT)

        fun codeTable() = appendRaw(Esc.CODE_TABLE_ASCII)

        fun bold(): Unit = appendRaw(Esc.boldOn())

        fun normal(): Unit = appendRaw(Esc.normal())

        fun double(): Unit = appendRaw(Esc.doubleSize())

        fun plain(): Unit = appendRaw(Esc.normal())

        fun left(block: ReceiptBuffer.() -> Unit) {
            setAlign(Esc.ALIGN_LEFT)
            block()
            resetAlign()
        }

        fun center(block: ReceiptBuffer.() -> Unit) {
            setAlign(Esc.ALIGN_CENTER)
            block()
            resetAlign()
        }

        fun right(block: ReceiptBuffer.() -> Unit) {
            setAlign(Esc.ALIGN_RIGHT)
            block()
            resetAlign()
        }

        private fun setAlign(n: Int) = appendRaw(Esc.align(n))

        private fun resetAlign() = setAlign(Esc.ALIGN_LEFT)

        fun text(content: String) {
            val clipped =
                if (content.length > columnCount) content.substring(0, columnCount) else content
            appendRaw(clipped.toByteArray(charset("US-ASCII")))
        }

        fun newline() = appendRaw("\n".toByteArray(charset("US-ASCII")))

        fun newlines(count: Int = 1) {
            repeat(count) { newline() }
        }

        fun feedLines(count: Int) {
            if (count > 0) {
                appendRaw(byteArrayOf(Esc.FS, count.coerceAtMost(255).toByte()))
            }
        }

        fun indent() = text("   ")

        fun divider(
            char: Char = '-',
            count: Int = columnCount,
        ) {
            val safeCount = count.coerceIn(1, columnCount)
            val line = char.toString().repeat(safeCount)
            text(line)
            newline()
        }

        fun tableHeader() {
            if (columnCount >= 42) {
                val header = "Item".padEnd(16) + "Qty".padEnd(8) + "Rate".padEnd(9) + "Total"
                text(header)
            } else {
                val header = "Item".padEnd(14) + "Qty".padEnd(6) + "Total"
                text(header)
            }
            newline()
        }

        fun lineItem(
            cartItem: CartItem,
            lineTotal: Double,
        ) {
            val name = cartItem.item.name.take(columnCount - 8)

            if (columnCount >= 42) {
                val qtyStr = "${cartItem.quantity} ${cartItem.item.unit}".take(7)
                val rateStr = fmt(cartItem.item.sellPrice).take(8)
                val totalStr = fmt(lineTotal)
                text(name.padEnd(16) + qtyStr.padEnd(8) + rateStr.padEnd(9) + totalStr)
            } else {
                val qtyStr = "${cartItem.quantity}".take(5)
                val totalStr = fmt(lineTotal)
                text(name.padEnd(14) + qtyStr.padEnd(6) + totalStr)
            }
        }

        fun cutFull() = appendRaw(Esc.FEED_AND_CUT_FULL)

        fun cutPartial() = appendRaw(Esc.FEED_AND_CUT_PARTIAL)

        private fun appendRaw(bytesArr: ByteArray) {
            buffer.write(bytesArr, 0, bytesArr.size)
        }

        fun toByteArray(): ByteArray = buffer.toByteArray()
    }
}
