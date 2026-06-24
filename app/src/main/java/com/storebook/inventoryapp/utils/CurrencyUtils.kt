package com.storebook.inventoryapp.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object CurrencySettings {
    var currencySymbol: String = "₹"
    var currencyLocale: Locale = Locale("en", "IN")
    var maxDecimalPlaces: Int = 2
}

fun updateCurrencyConfig(currencyCode: String) {
    when (currencyCode) {
        "INR" -> {
            CurrencySettings.currencySymbol = "₹"
            CurrencySettings.currencyLocale = Locale("en", "IN")
            CurrencySettings.maxDecimalPlaces = 2
        }
        "USD" -> {
            CurrencySettings.currencySymbol = "$"
            CurrencySettings.currencyLocale = Locale.US
            CurrencySettings.maxDecimalPlaces = 2
        }
        "EUR" -> {
            CurrencySettings.currencySymbol = "€"
            CurrencySettings.currencyLocale = Locale.GERMANY
            CurrencySettings.maxDecimalPlaces = 2
        }
        "GBP" -> {
            CurrencySettings.currencySymbol = "£"
            CurrencySettings.currencyLocale = Locale.UK
            CurrencySettings.maxDecimalPlaces = 2
        }
        "JPY" -> {
            CurrencySettings.currencySymbol = "¥"
            CurrencySettings.currencyLocale = Locale.JAPAN
            CurrencySettings.maxDecimalPlaces = 0
        }
        "CNY" -> {
            CurrencySettings.currencySymbol = "元"
            CurrencySettings.currencyLocale = Locale.CHINA
            CurrencySettings.maxDecimalPlaces = 2
        }
        else -> {
            CurrencySettings.currencySymbol = "₹"
            CurrencySettings.currencyLocale = Locale("en", "IN")
            CurrencySettings.maxDecimalPlaces = 2
        }
    }
}

/**
 * Formats a Double to a currency string without decimal places.
 * Example: 1234567.89 -> ₹12,34,568
 */
fun Double.toRupee(): String {
    val bd = try {
        BigDecimal(this.toString()).setScale(0, RoundingMode.HALF_UP)
    } catch (e: Exception) {
        BigDecimal(this).setScale(0, RoundingMode.HALF_UP)
    }
    val format = NumberFormat.getNumberInstance(CurrencySettings.currencyLocale)
    format.maximumFractionDigits = 0
    val formatted = format.format(bd.abs())
    return if (bd.signum() < 0) "-${CurrencySettings.currencySymbol}$formatted" else "${CurrencySettings.currencySymbol}$formatted"
}

/**
 * Formats a Double to a currency string with up to designated decimal places.
 */
fun Double.toRupeeWithDecimals(): String {
    val bd = try {
        BigDecimal(this.toString()).setScale(CurrencySettings.maxDecimalPlaces, RoundingMode.HALF_UP)
    } catch (e: Exception) {
        BigDecimal(this).setScale(CurrencySettings.maxDecimalPlaces, RoundingMode.HALF_UP)
    }
    val format = NumberFormat.getNumberInstance(CurrencySettings.currencyLocale)
    format.maximumFractionDigits = CurrencySettings.maxDecimalPlaces
    format.minimumFractionDigits = 0
    val formatted = format.format(bd.abs())
    return if (bd.signum() < 0) "-${CurrencySettings.currencySymbol}$formatted" else "${CurrencySettings.currencySymbol}$formatted"
}

fun Double.toBigDecimal(): BigDecimal {
    return try {
        BigDecimal(this.toString())
    } catch (e: Exception) {
        BigDecimal(this)
    }
}

fun <T> Iterable<T>.sumOfBigDecimal(selector: (T) -> BigDecimal): BigDecimal {
    var sum = BigDecimal.ZERO
    for (element in this) {
        sum = sum.add(selector(element))
    }
    return sum
}

