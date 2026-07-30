package com.storebook.inventoryapp.data.billing

import com.storebook.inventoryapp.shared.domain.models.CartItem
import com.storebook.inventoryapp.shared.domain.models.Item
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingEngineTest {
    @Test
    fun `discount clamped when discount exceeds subtotal`() {
        val item =
            Item(
                id = 1,
                name = "Test",
                quantity = 5.0,
                unit = "PC",
                buyPrice = 50.0,
                sellPrice = 100.0,
                lowStockThreshold = 2.0,
                category = "cat",
            )
        val cartItems = listOf(CartItem(item, 2))

        val summary =
            BillingEngine.calculateInvoiceTaxes(
                listOf(CartItem(item, 2)),
                99999.0,
                "",
                "",
            )

        assertEquals(50.0, summary.subTotal, 0.01)
        assertEquals(50.0, summary.totalDiscount, 0.01)
        assertEquals(0.0, summary.netTaxableAmount, 0.01)
        assertEquals(0.0, summary.grandTotal, 0.01)
    }

    @Test
    fun `zero subtotal returns zeroed summary`() {
        val cartItems = emptyList<CartItem>()

        val summary = BillingEngine.calculateInvoiceTaxes(cartItems, 0.0, "", "")

        assertEquals(0.0, summary.subTotal, 0.01)
        assertEquals(0.0, summary.totalDiscount, 0.01)
        assertEquals(0.0, summary.netTaxableAmount, 0.01)
        assertEquals(0.0, summary.grandTotal, 0.01)
        assertTrue(summary.itemDetails.isEmpty())
    }

    @Test
    fun `intra state splits tax into CGST and SGST`() {
        val item =
            Item(
                id = 1,
                name = "Wheat",
                quantity = 5.0,
                unit = "KG",
                buyPrice = 20.0,
                sellPrice = 40.0,
                lowStockThreshold = 1.0,
                category = "grain",
                taxRate = 18.0,
            )

        val summary =
            BillingEngine.calculateInvoiceTaxes(
                listOf(CartItem(item, 1)),
                0.0,
                "27AADFU0935F1ZM",
                "27AABCT1234C1ZX",
            )

        assertEquals(7.20, summary.totalCgst, 0.01)
        assertEquals(7.20, summary.totalSgst, 0.01)
    }

    @Test
    fun `interstate uses IGST not CGST SGST`() {
        val item =
            Item(
                id = 1,
                name = "Rice",
                quantity = 5.0,
                unit = "KG",
                buyPrice = 10.0,
                sellPrice = 20.0,
                lowStockThreshold = 1.0,
                category = "grain",
                taxRate = 18.0,
            )

        val summary =
            BillingEngine.calculateInvoiceTaxes(
                listOf(CartItem(item, 1)),
                0.0,
                "27AAAAA0000A1Z3",
                "06BBBBB0000B1Z4",
            )

        assertEquals(3.60, summary.totalIgst, 0.01)
        assertEquals(0.0, summary.totalCgst, 0.01)
        assertEquals(0.0, summary.totalSgst, 0.01)
    }

    @Test
    fun `intra state uses CGST SGST not IGST`() {
        val item =
            Item(
                id = 2,
                name = "Oil",
                quantity = 5.0,
                unit = "L",
                buyPrice = 30.0,
                sellPrice = 60.0,
                lowStockThreshold = 1.0,
                category = "oil",
                taxRate = 18.0,
            )

        val summary =
            BillingEngine.calculateInvoiceTaxes(
                listOf(CartItem(item, 1)),
                0.0,
                "27CCCCC0000C1Z5",
                "27DDDDD0000D1Z5",
            )

        assertEquals(0.0, summary.totalIgst, 0.01)
        assertEquals(5.40, summary.totalCgst, 0.01)
        assertEquals(5.40, summary.totalSgst, 0.01)
    }

    @Test
    fun `missing GSTIN defaults to intra-state`() {
        val item =
            Item(
                id = 3,
                name = "Tea",
                quantity = 5.0,
                unit = "PKG",
                buyPrice = 40.0,
                sellPrice = 80.0,
                lowStockThreshold = 1.0,
                category = "tea",
                taxRate = 18.0,
            )

        val summary =
            BillingEngine.calculateInvoiceTaxes(
                listOf(CartItem(item, 1)),
                0.0,
                "",
                "",
            )

        assertEquals(7.20, summary.totalCgst + summary.totalSgst, 0.01)
    }

    @Test
    fun `discount prorated proportionally across items`() {
        val item1 =
            Item(
                id = 1,
                name = "A",
                quantity = 5.0,
                unit = "PC",
                buyPrice = 10.0,
                sellPrice = 30.0,
                lowStockThreshold = 1.0,
                category = "a",
            )
        val item2 =
            Item(
                id = 2,
                name = "B",
                quantity = 5.0,
                unit = "PC",
                buyPrice = 20.0,
                sellPrice = 70.0,
                lowStockThreshold = 1.0,
                category = "b",
            )

        val cartItems = listOf(CartItem(item1, 2), CartItem(item2, 2))
        val subTotal = (30 * 2 + 70 * 2).toDouble()

        val summary =
            BillingEngine.calculateInvoiceTaxes(
                cartItems,
                50.0,
                "",
                "",
            )

        assertEquals(subTotal, summary.subTotal, 0.01)
        assertEquals(50.0, summary.totalDiscount, 0.01)
        assertTrue(summary.itemDetails.size == 2)
    }

    @Test
    fun `grand total equals net taxable plus all taxes`() {
        val item =
            Item(
                id = 4,
                name = "Salt",
                quantity = 5.0,
                unit = "KG",
                buyPrice = 75.0,
                sellPrice = 120.0,
                lowStockThreshold = 1.0,
                category = "salt",
                taxRate = 18.0,
            )

        val summary =
            BillingEngine.calculateInvoiceTaxes(
                listOf(CartItem(item, 3)),
                50.0,
                "",
                "",
            )

        val expected = summary.netTaxableAmount + summary.totalIgst
        assertEquals(expected, summary.grandTotal, 0.01)
    }

    @Test
    fun `item detail net amount is after proration before tax`() {
        val item =
            Item(
                id = 5,
                name = "Sugar",
                quantity = 2,
                unit = "PC",
                buyPrice = 19.0,
                sellPrice = 38.0,
                lowStockThreshold = 1.0,
                category = "sugar",
            )

        val summary =
            BillingEngine.calculateInvoiceTaxes(
                listOf(CartItem(item, 3)),
                19.5,
                "",
                "",
            )

        assertEquals(94.5, summary.itemDetails[0].netAmountBeforeTax, 0.01)
    }
}
