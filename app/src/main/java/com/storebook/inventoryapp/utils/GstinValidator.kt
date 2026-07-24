package com.storebook.inventoryapp.utils

object GstinValidator {
    val STATE_CODES =
        mapOf(
            "01" to "Jammu and Kashmir",
            "02" to "Himachal Pradesh",
            "03" to "Punjab",
            "04" to "Chandigarh",
            "05" to "Uttarakhand",
            "06" to "Haryana",
            "07" to "Delhi",
            "08" to "Rajasthan",
            "09" to "Uttar Pradesh",
            "10" to "Bihar",
            "11" to "Sikkim",
            "12" to "Arunachal Pradesh",
            "13" to "Nagaland",
            "14" to "Manipur",
            "15" to "Mizoram",
            "16" to "Tripura",
            "17" to "Meghalaya",
            "18" to "Assam",
            "19" to "West Bengal",
            "20" to "Jharkhand",
            "21" to "Odisha",
            "22" to "Chhattisgarh",
            "23" to "Madhya Pradesh",
            "24" to "Gujarat",
            "25" to "Daman and Diu",
            "26" to "Dadra and Nagar Haveli",
            "27" to "Maharashtra",
            "29" to "Karnataka",
            "30" to "Goa",
            "31" to "Lakshadweep",
            "32" to "Kerala",
            "33" to "Tamil Nadu",
            "34" to "Puducherry",
            "35" to "Andaman and Nicobar Islands",
            "36" to "Telangana",
            "37" to "Andhra Pradesh",
            "38" to "Ladakh",
            "97" to "Other Territory",
        )

    private const val CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun validate(gstin: String): ValidationResult {
        val trimmed = gstin.trim().uppercase()
        if (trimmed.isEmpty()) return ValidationResult.Valid(null)

        if (trimmed.length != 15) return ValidationResult.Invalid("Must be exactly 15 characters")

        val stateCode = trimmed.substring(0, 2)
        val stateName =
            STATE_CODES[stateCode]
                ?: return ValidationResult.Invalid("Invalid state code ($stateCode)")

        val panRegex = Regex("[A-Z]{5}\\d{4}[A-Z]{1}")
        if (!trimmed.substring(2, 12).matches(panRegex)) {
            return ValidationResult.Invalid("Invalid PAN format")
        }

        if (trimmed[13] != 'Z') {
            return ValidationResult.Invalid("14th character must be 'Z'")
        }

        var hash = 0
        for (i in 0 until 14) {
            val char = trimmed[i]
            val value = CHARS.indexOf(char)
            if (value == -1) return ValidationResult.Invalid("Invalid character found")
            val multiplier = if (i % 2 == 0) 1 else 2
            val product = value * multiplier
            hash += (product / 36) + (product % 36)
        }

        val checkDigit = (36 - (hash % 36)) % 36
        val expectedChar = CHARS[checkDigit]

        if (trimmed[14] != expectedChar) {
            return ValidationResult.Invalid("Checksum mismatch (expected $expectedChar)")
        }

        return ValidationResult.Valid(stateName)
    }

    sealed class ValidationResult {
        data class Valid(
            val stateName: String?,
        ) : ValidationResult()

        data class Invalid(
            val reason: String,
        ) : ValidationResult()
    }
}
