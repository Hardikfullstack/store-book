package com.storebook.inventoryapp.data.billing

import com.storebook.inventoryapp.data.repository.CartItem
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

// Enum to define Tax Types
enum class TaxType {
    INTRASTATE, // CGST + SGST
    INTERSTATE, // IGST
}

// Data class to represent the calculated tax for an item
data class ItemTaxDetails(
    val cartItem: CartItem,
    val netAmountBeforeTax: Double, // Amount after discount but before tax
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalTaxAmount: Double,
    val totalAmountWithTax: Double,
)

// Data class to represent the full invoice tax calculation
data class InvoiceTaxSummary(
    val subTotal: Double,
    val totalDiscount: Double,
    val netTaxableAmount: Double, // Subtotal - Discount
    val totalCgst: Double,
    val totalSgst: Double,
    val totalIgst: Double,
    val grandTotal: Double,
    val itemDetails: List<ItemTaxDetails>,
)

object BillingEngine {
    /**
     * Extracts state code from GSTIN. First 2 characters of a valid GSTIN represent the state code.
     */
    fun getStateCodeFromGSTIN(gstin: String?): String? {
        if (gstin.isNullOrBlank() || gstin.length < 2) return null
        return gstin.substring(0, 2)
    }

    /**
     * Determines whether the transaction is Intrastate (CGST+SGST) or Interstate (IGST).
     * If both state codes match, it's Intrastate. Otherwise, Interstate.
     * If GSTIN is not provided or invalid, defaults to Intrastate for local B2C sales.
     */
    fun determineTaxType(
        businessGstin: String?,
        customerGstin: String?,
    ): TaxType {
        val bizState = getStateCodeFromGSTIN(businessGstin)
        val custState = getStateCodeFromGSTIN(customerGstin)

        return if (bizState != null && custState != null && bizState != custState) {
            TaxType.INTERSTATE
        } else {
            TaxType.INTRASTATE
        }
    }

    /**
     * Calculates tax on the NET amount (after discounts).
     * Distributes discount proportionally across items before calculating tax to avoid calculation errors.
     */
    fun calculateInvoiceTaxes(
        cartItems: List<CartItem>,
        totalDiscount: Double,
        businessGstin: String?,
        customerGstin: String?,
    ): InvoiceTaxSummary {
        val taxType = determineTaxType(businessGstin, customerGstin)

        var subTotal = BigDecimal.ZERO
        for (item in cartItems) {
            val price = BigDecimal(item.item.sellPrice.toString())
            val qty = BigDecimal(item.quantity.toString())
            subTotal = subTotal.add(price.multiply(qty))
        }

        val discount = BigDecimal(totalDiscount.toString())
        val actualDiscount = if (discount.compareTo(subTotal) > 0) subTotal else discount
        val netTaxableAmount = subTotal.subtract(actualDiscount)

        // If subtotal is 0, nothing to calculate
        if (subTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return InvoiceTaxSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, emptyList())
        }

        var totalCgst = BigDecimal.ZERO
        var totalSgst = BigDecimal.ZERO
        var totalIgst = BigDecimal.ZERO
        val itemDetails = mutableListOf<ItemTaxDetails>()

        val scale = 4 // Intermediate calculation precision
        val roundMode = RoundingMode.HALF_UP

        for (cartItem in cartItems) {
            val itemGross = BigDecimal(cartItem.item.sellPrice.toString()).multiply(BigDecimal(cartItem.quantity.toString()))
            val itemDiscountRatio = if (subTotal.compareTo(BigDecimal.ZERO) > 0) {
                itemGross.divide(subTotal, scale, roundMode)
            } else {
                BigDecimal.ZERO
            }
            val itemDiscount = actualDiscount.multiply(itemDiscountRatio)
            val itemNetTaxable = itemGross.subtract(itemDiscount).max(BigDecimal.ZERO)

            val taxRate = BigDecimal(cartItem.item.taxRate.toString())
            var cgst = BigDecimal.ZERO
            var sgst = BigDecimal.ZERO
            var igst = BigDecimal.ZERO

            if (taxType == TaxType.INTRASTATE) {
                val halfRate = taxRate.divide(BigDecimal("2"), scale, roundMode)
                cgst = itemNetTaxable.multiply(halfRate).divide(BigDecimal("100"), scale, roundMode)
                sgst = itemNetTaxable.multiply(halfRate).divide(BigDecimal("100"), scale, roundMode)
            } else {
                igst = itemNetTaxable.multiply(taxRate).divide(BigDecimal("100"), scale, roundMode)
            }

            val itemTotalTax = cgst.add(sgst).add(igst)

            totalCgst = totalCgst.add(cgst)
            totalSgst = totalSgst.add(sgst)
            totalIgst = totalIgst.add(igst)

            itemDetails.add(
                ItemTaxDetails(
                    cartItem = cartItem,
                    netAmountBeforeTax = itemNetTaxable.setScale(2, roundMode).toDouble(),
                    cgstAmount = cgst.setScale(2, roundMode).toDouble(),
                    sgstAmount = sgst.setScale(2, roundMode).toDouble(),
                    igstAmount = igst.setScale(2, roundMode).toDouble(),
                    totalTaxAmount = itemTotalTax.setScale(2, roundMode).toDouble(),
                    totalAmountWithTax = itemNetTaxable.add(itemTotalTax).setScale(2, roundMode).toDouble(),
                ),
            )
        }

        val grandTotal = netTaxableAmount.add(totalCgst).add(totalSgst).add(totalIgst)

        return InvoiceTaxSummary(
            subTotal = subTotal.setScale(2, roundMode).toDouble(),
            totalDiscount = actualDiscount.setScale(2, roundMode).toDouble(),
            netTaxableAmount = netTaxableAmount.setScale(2, roundMode).toDouble(),
            totalCgst = totalCgst.setScale(2, roundMode).toDouble(),
            totalSgst = totalSgst.setScale(2, roundMode).toDouble(),
            totalIgst = totalIgst.setScale(2, roundMode).toDouble(),
            grandTotal = grandTotal.setScale(2, roundMode).toDouble(),
            itemDetails = itemDetails,
        )
    }
}
