package com.storebook.inventoryapp.data.billing

import com.storebook.inventoryapp.data.repository.CartItem
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

        var subTotal = 0.0
        for (item in cartItems) {
            subTotal += (item.item.sellPrice * item.quantity)
        }

        val actualDiscount = min(totalDiscount, subTotal)
        val netTaxableAmount = max(0.0, subTotal - actualDiscount)

        // If subtotal is 0, nothing to calculate
        if (subTotal <= 0.0) {
            return InvoiceTaxSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, emptyList())
        }

        var totalCgst = 0.0
        var totalSgst = 0.0
        var totalIgst = 0.0
        val itemDetails = mutableListOf<ItemTaxDetails>()

        for (cartItem in cartItems) {
            val itemGross = cartItem.item.sellPrice * cartItem.quantity
            // Proportion of discount applied to this item
            val itemDiscountRatio = if (subTotal > 0) itemGross / subTotal else 0.0
            val itemDiscount = actualDiscount * itemDiscountRatio
            val itemNetTaxable = max(0.0, itemGross - itemDiscount)

            val taxRate = cartItem.item.taxRate
            var cgst = 0.0
            var sgst = 0.0
            var igst = 0.0

            if (taxType == TaxType.INTRASTATE) {
                cgst = itemNetTaxable * (taxRate / 2) / 100.0
                sgst = itemNetTaxable * (taxRate / 2) / 100.0
            } else {
                igst = itemNetTaxable * taxRate / 100.0
            }

            val itemTotalTax = cgst + sgst + igst

            totalCgst += cgst
            totalSgst += sgst
            totalIgst += igst

            itemDetails.add(
                ItemTaxDetails(
                    cartItem = cartItem,
                    netAmountBeforeTax = itemNetTaxable,
                    cgstAmount = cgst,
                    sgstAmount = sgst,
                    igstAmount = igst,
                    totalTaxAmount = itemTotalTax,
                    totalAmountWithTax = itemNetTaxable + itemTotalTax,
                ),
            )
        }

        val grandTotal = netTaxableAmount + totalCgst + totalSgst + totalIgst

        return InvoiceTaxSummary(
            subTotal = subTotal,
            totalDiscount = actualDiscount,
            netTaxableAmount = netTaxableAmount,
            totalCgst = totalCgst,
            totalSgst = totalSgst,
            totalIgst = totalIgst,
            grandTotal = grandTotal,
            itemDetails = itemDetails,
        )
    }
}
