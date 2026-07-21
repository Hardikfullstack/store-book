package com.storebook.inventoryapp.ui.screens.storebook
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Returns the +/- step size for a given unit of measurement.
 *
 * - Whole-count units (pcs, dozen, box, packet):  step = 1.0
 * - Weight / volume decimals (kg, litre, liter):  step = 0.5
 * - Small weight / volume (g, ml):                step = 50.0
 */
fun stepForUnit(unit: String): Double =
    when (unit.lowercase().trim()) {
        "pcs", "piece", "pieces", "dozen", "box", "packet", "pack" -> 1.0
        "kg", "kgs", "kilogram", "kilograms",
        "litre", "liter", "l", "liters", "litres",
        -> 0.5
        "g", "gram", "grams",
        "ml", "milliliter", "millilitre", "milliliters",
        -> 50.0
        else -> 1.0
    }

/**
 * Format a quantity for display.
 * Removes trailing ".0" for whole numbers but keeps necessary decimals.
 *   2.0  → "2"
 *   2.5  → "2.5"
 *   0.25 → "0.25"
 */
fun formatQty(qty: Double): String {
    if (qty.isNaN() || qty.isInfinite()) return "0"
    return if (qty == kotlin.math.floor(qty)) {
        qty.toLong().toString()
    } else {
        qty.toBigDecimal().stripTrailingZeros().toPlainString()
    }
}

/**
 * Emoji label for each unit type — shown as item avatar in the sales list.
 */
fun unitIcon(unit: String): ImageVector =
    when (unit.lowercase().trim()) {
        "kg", "kgs", "kilogram", "kilograms", "g", "gram", "grams" -> Icons.Outlined.Balance
        "litre", "liter", "l", "litres", "liters", "ml", "milliliter", "millilitre" -> Icons.Outlined.WaterDrop
        "dozen" -> Icons.Outlined.Inventory2
        "box" -> Icons.Outlined.Inventory2
        "packet", "pack" -> Icons.Outlined.ShoppingBag
        else -> Icons.Outlined.Numbers
    }
