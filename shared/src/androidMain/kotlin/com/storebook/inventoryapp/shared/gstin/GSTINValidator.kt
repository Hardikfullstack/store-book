package com.storebook.inventoryapp.shared.gstin

import java.util.*

/**
 * GSTIN (Goods and Services Tax Identification Number) validator for Indian businesses.
 *
 * GSTIN format:
 * - 15 characters total
 * - First 2: State code (numeric, Census 2011)
 * - Next 10: PAN number (alphanumeric)
 *   - PAN[0–2]: Three uppercase letters
 *   - PAN[3]: Entity-type letter (C=Company, P=Person, H=HUF, F=Firm, etc.)
 *   - PAN[4]: First letter of surname/entity name
 *   - PAN[5–8]: Four digits (0001–9999)
 *   - PAN[9]: Alphabetic check letter
 * - 13th char: Entity registration number (1–9, A–Z)
 * - 14th char: Default 'Z'
 * - 15th char: Check digit (modulo-36 checksum)
 *
 * GSTIN validation includes:
 * 1. Format compliance
 * 2. State code validity
 * 3. Checksum verification (modulo-36)
 * 4. Entity type inference (from PAN's 4th character)
 */
object GSTINValidator {
    private val GST_STATES = mapOf(
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
        "26" to "Dadra and Nagar Haveli and Daman and Diu",
        "27" to "Maharashtra",
        "28" to "Andhra Pradesh",
        "29" to "Karnataka",
        "30" to "Goa",
        "31" to "Lakshadweep",
        "32" to "Kerala",
        "33" to "Tamil Nadu",
        "34" to "Puducherry",
        "35" to "Andaman and Nicobar Islands",
        "36" to "Telangana",
        "37" to "Andhra Pradesh (New)",
        "38" to "Ladakh",
        "97" to "Other Territory",
        "99" to "Centre Jurisdiction"
    )

    // Character set for modulo-36: 0-9 → 0-9, A-Z → 10-35
    private const val CHAR_SET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    /**
     * Validates a GSTIN number and returns detailed information
     *
     * @param gstin The GSTIN to validate
     * @return [GSTINValidationResult] with validation results
     */
    fun validateGSTIN(gstin: String?): GSTINValidationResult {
        val errors = mutableListOf<String>()

        if (gstin.isNullOrEmpty()) {
            return GSTINValidationResult(
                isValid = false,
                stateCode = null,
                stateName = null,
                entityType = null,
                errors = listOf("GSTIN is required")
            )
        }

        val cleanedGstin = gstin.trim().uppercase(Locale.getDefault())

        if (cleanedGstin.length != 15) {
            errors.add("GSTIN must be exactly 15 characters")
            return GSTINValidationResult(
                isValid = false,
                stateCode = null,
                stateName = null,
                entityType = null,
                errors = errors
            )
        }

        val stateCode = cleanedGstin.substring(0, 2)
        val pan = cleanedGstin.substring(2, 12)
        @Suppress("UNUSED_VARIABLE")
        val entityNumber = cleanedGstin[12]
        @Suppress("UNUSED_VARIABLE")
        val defaultZ = cleanedGstin[13]

        // Validate state code
        if (!stateCode.matches(Regex("[0-9]{2}"))) {
            errors.add("State code must be numeric")
        }

        // Validate PAN portion (chars 3-12)
        if (!pan.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))) {
            errors.add("PAN portion must match format AAAAA9999A")
        }

        // Validate entity number (13th char: 1-9 or A-Z)
        if (!cleanedGstin[12].toString()
            .matches(Regex("[1-9A-Z]"))
        ) {
            errors.add("Entity number must be 1-9 or A-Z")
        }

        // Validate default character (14th char: must be Z)
        if (cleanedGstin[13] != 'Z') {
            errors.add("14th character must be Z")
        }

        // Validate check digit character (15th char: alphanumeric)
        if (!cleanedGstin[14].toString().matches(Regex("[0-9A-Z]"))) {
            errors.add(
                "Check digit must be alphanumeric"
            )
        }

        if (errors.isNotEmpty()) {
            return GSTINValidationResult(
                isValid = false,
                stateCode = null,
                stateName = null,
                entityType = null,
                errors = errors
            )
        }

        // Verify checksum using modulo-36 algorithm
        val isValidChecksum = verifyGSTINChecksum(cleanedGstin)
        if (!isValidChecksum) {
            errors.add("GSTIN checksum is invalid")
        }

        val stateName = GST_STATES[stateCode] ?: "Unknown State"

        // Determine entity type from PAN's 4th character (GSTIN index 5)
        val panEntityChar = cleanedGstin[5]
        val entityType: String? = when (panEntityChar) {
            'C' -> "Private / Public Limited Company"
            'P' -> "Individual / Proprietorship"
            'H' -> "Hindu Undivided Family"
            'F' -> "Firm / Limited Liability Partnership"
            'A' -> "Association of Persons"
            'T' -> "Trust"
            'B' -> "Body of Individuals"
            'L' -> "Local Authority"
            'J' -> "Artificial Juridical Person"
            'G' -> "Government Department"
            else -> null
        }

        return GSTINValidationResult(
            isValid = errors.isEmpty(),
            stateCode = stateCode,
            stateName = stateName,
            entityType = entityType,
            errors = errors
        )
    }

    /**
     * Verifies the checksum of a GSTIN using the modulo-36 algorithm.
     *
     * For each of the first 14 characters:
     *   1. Convert the character to its numeric value (0-9 → 0-9, A-Z → 10-35).
     *   2. Multiply by a factor: 1 for odd positions, 2 for even positions (1-indexed).
     *   3. Compute quotient and remainder when dividing the product by 36.
     *   4. Add quotient + remainder to the running total.
     * The check character value = (36 − (total % 36)) % 36.
     *
     * @param gstin The 15-character GSTIN to verify
     * @return true if checksum is valid, false otherwise
     */
    private fun verifyGSTINChecksum(gstin: String): Boolean {
        var total = 0
        for (i in 0 until 14) {
            val charValue = CHAR_SET.indexOf(gstin[i])
            if (charValue < 0) return false

            val factor = if ((i + 1) % 2 == 0) 2 else 1
            val product = charValue * factor
            total += (product / 36) + (product % 36)
        }

        val checkValue = (36 - (total % 36)) % 36
        val expectedChar = CHAR_SET[checkValue]
        return gstin[14] == expectedChar
    }

    /**
     * Checks if two GSTIN numbers are from different states (interstate)
     *
     * @param gstin1 First GSTIN
     * @param gstin2 Second GSTIN
     * @return true if they're from different states, false otherwise
     */
    fun isInterstateGSTIN(gstin1: String?, gstin2: String?): Boolean {
        if (gstin1.isNullOrEmpty() || gstin2.isNullOrEmpty()) return false
        val state1 = gstin1.trim().uppercase(Locale.getDefault()).substring(0, 2)
        val state2 = gstin2.trim().uppercase(Locale.getDefault()).substring(0, 2)
        return state1 != state2
    }

    /**
     * Gets the state name for a given GSTIN state code
     *
     * @param stateCode The state code (first two digits of GSTIN)
     * @return State name or null if not found
     */
    fun getGSTStateName(stateCode: String): String? {
        return GST_STATES[stateCode]
    }
}

/**
 * Result class for GSTIN validation
 *
 * @property isValid Whether the GSTIN is valid
 * @property stateCode The state code from the GSTIN (nullable)
 * @property stateName The full name of the state (nullable)
 * @property entityType The entity type as inferred from PAN's 4th character (nullable)
 * @property errors List of validation errors (empty if valid)
 */
data class GSTINValidationResult(
    val isValid: Boolean,
    val stateCode: String?,
    val stateName: String?,
    val entityType: String?,
    val errors: List<String>
)
