package com.storebook.inventoryapp.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Formats a Double to Indian Rupee currency string without decimal places.
 * Example: 1234567.89 -> ₹12,34,568
 * Example: -30.0 -> -₹30
 */
fun Double.toRupee(): String {
    val format = NumberFormat.getNumberInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0
    val formatted = format.format(abs(this))
    return if (this < 0) "-₹$formatted" else "₹$formatted"
}

/**
 * Formats a Double to Indian Rupee currency string with up to 2 decimal places.
 * Example: 1234567.89 -> ₹12,34,567.89
 * Example: -30.0 -> -₹30
 */
fun Double.toRupeeWithDecimals(): String {
    val format = NumberFormat.getNumberInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 2
    format.minimumFractionDigits = 0
    val formatted = format.format(abs(this))
    return if (this < 0) "-₹$formatted" else "₹$formatted"
}
