package com.storebook.inventoryapp.shared.gstin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GSTINValidatorTests {

    @Test
    fun testValidGSTIN() {
        // 27 = Maharashtra, PAN 4th char 'C' = Company
        val gstin = "27AABCC1234D1ZB"
        val result = GSTINValidator.validateGSTIN(gstin)

        assertTrue(result.isValid)
        assertEquals("27", result.stateCode)
        assertEquals("Maharashtra", result.stateName)
        assertEquals(
            "Private / Public Limited Company",
            result.entityType
        )
    }

    @Test
    fun testInvalidLength() {
        val gstin = "27AABCC1234D1"
        val result = GSTINValidator.validateGSTIN(gstin)

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        assertTrue(
            result.errors.contains(
                "GSTIN must be exactly 15 characters"
            )
        )
    }

    @Test
    fun testInvalidStateCode() {
        val gstin = "AAABCC1234D1ZBB"
        val result = GSTINValidator.validateGSTIN(gstin)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.contains("State code must be numeric")
        )
    }

    @Test
    fun testInvalidPANFormat() {
        // PAN portion does not match AAAAA9999A pattern
        val gstin = "271234567890Z1A"
        val result = GSTINValidator.validateGSTIN(gstin)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.contains(
                "PAN portion must match format AAAAA9999A"
            )
        )
    }

    @Test
    fun testInvalidEntityNumber() {
        // 13th char is '0' which is invalid (must be 1-9 or A-Z)
        val gstin = "27AABCC1234D0ZB"
        val result = GSTINValidator.validateGSTIN(gstin)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.contains(
                "Entity number must be 1-9 or A-Z"
            )
        )
    }

    @Test
    fun testInvalid14thCharacter() {
        // 14th char must be 'Z'
        val gstin = "27AABCC1234D1AB"
        val result = GSTINValidator.validateGSTIN(gstin)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.contains("14th character must be Z")
        )
    }

    @Test
    fun testChecksumValidation() {
        // Valid GSTIN
        val validGstin = "27AABCC1234D1ZB"
        val validResult = GSTINValidator.validateGSTIN(validGstin)
        assertTrue(validResult.isValid)

        // Same GSTIN with wrong check digit
        val invalidGstin = "27AABCC1234D1ZA"
        val invalidResult =
            GSTINValidator.validateGSTIN(invalidGstin)
        assertFalse(invalidResult.isValid)
        assertTrue(
            invalidResult.errors.contains(
                "GSTIN checksum is invalid"
            )
        )
    }

    @Test
    fun testInterstateComparison() {
        // 27 = Maharashtra, 33 = Tamil Nadu
        val gstin1 = "27AABCC1234D1ZB"
        val gstin2 = "33AABCC1234D1ZI"

        assertTrue(
            GSTINValidator.isInterstateGSTIN(gstin1, gstin2)
        )
    }

    @Test
    fun testSameStateComparison() {
        val gstin1 = "27AABCC1234D1ZB"
        val gstin2 = "27AABPA1234D1ZM"

        assertFalse(
            GSTINValidator.isInterstateGSTIN(gstin1, gstin2)
        )
    }

    @Test
    fun testNullGSTIN() {
        val result = GSTINValidator.validateGSTIN(null)

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        assertTrue(result.errors.contains("GSTIN is required"))
    }

    @Test
    fun testEmptyGSTIN() {
        val result = GSTINValidator.validateGSTIN("")

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        assertTrue(result.errors.contains("GSTIN is required"))
    }

    @Test
    fun testGetStateName() {
        assertEquals(
            "Maharashtra",
            GSTINValidator.getGSTStateName("27")
        )
        assertEquals(
            "Karnataka",
            GSTINValidator.getGSTStateName("29")
        )
        assertEquals(
            "Tamil Nadu",
            GSTINValidator.getGSTStateName("33")
        )
        assertEquals(
            "Delhi",
            GSTINValidator.getGSTStateName("07")
        )
        assertEquals(null, GSTINValidator.getGSTStateName("98"))
    }

    @Test
    fun testEntityTypeInference() {
        // Entity type comes from PAN's 4th character (GSTIN index 5)
        // PAN format AAAAA9999A — 4th char (PAN[3]) is the entity type
        val testCases = mapOf(
            // C at GSTIN[5] → Company
            "27AABCA1234D1ZD" to
                "Private / Public Limited Company",
            // P at GSTIN[5] → Individual / Proprietorship
            "27AABPA1234D1ZM" to
                "Individual / Proprietorship",
            // F at GSTIN[5] → Firm / LLP
            "27AABFA1234D1Z7" to
                "Firm / Limited Liability Partnership",
            // G at GSTIN[5] → Government Department
            "27AABGA1234D1Z5" to "Government Department"
        )

        for ((gstin, expectedType) in testCases) {
            val result = GSTINValidator.validateGSTIN(gstin)
            assertEquals(
                expectedType,
                result.entityType,
                "Entity type mismatch for GSTIN $gstin"
            )
        }
    }
}
