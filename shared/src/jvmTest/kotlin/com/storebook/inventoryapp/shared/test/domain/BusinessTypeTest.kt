package com.storebook.inventoryapp.shared.test.domain

import com.storebook.inventoryapp.shared.domain.models.BUSINESS_TYPES
import com.storebook.inventoryapp.shared.domain.models.getBusinessTypeEmoji
import com.storebook.inventoryapp.shared.domain.models.getBusinessTypeLabel
import com.storebook.inventoryapp.shared.domain.models.isValidBusinessType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BusinessTypeTest {
    @Test
    fun testBusinessTypesListContainsRequiredIndustries() {
        assertEquals(11, BUSINESS_TYPES.size)
        assertTrue(isValidBusinessType("general"))
        assertTrue(isValidBusinessType("medical"))
        assertTrue(isValidBusinessType("grocery"))
        assertTrue(isValidBusinessType("dairy_sweets"))
        assertTrue(isValidBusinessType("bakery"))
        assertTrue(isValidBusinessType("footwear"))
        assertTrue(isValidBusinessType("electronics"))
        assertTrue(isValidBusinessType("hardware"))
        assertTrue(isValidBusinessType("electrical"))
        assertTrue(isValidBusinessType("stationery"))
        assertTrue(isValidBusinessType("cosmetics"))
        assertFalse(isValidBusinessType("unknown_type"))
    }

    @Test
    fun testBusinessTypeLabelAndEmojiResolvers() {
        assertEquals("General Store", getBusinessTypeLabel(null))
        assertEquals("General Store", getBusinessTypeLabel(""))
        assertEquals("Medical & Pharmacy", getBusinessTypeLabel("medical"))
        assertEquals("Custom Industry", getBusinessTypeLabel("Custom Industry"))

        assertEquals("🏪", getBusinessTypeEmoji(null))
        assertEquals("🏪", getBusinessTypeEmoji(""))
        assertEquals("💊", getBusinessTypeEmoji("medical"))
        assertEquals("🛒", getBusinessTypeEmoji("grocery"))
    }

    @Test
    fun testDefaultCategoriesForBusinessType() {
        val groceryCategories = com.storebook.inventoryapp.shared.domain.models.getDefaultCategoriesForBusinessType("grocery")
        assertTrue(groceryCategories.isNotEmpty())
        assertTrue(groceryCategories.contains("Pulses & Dals"))

        val medicalCategories = com.storebook.inventoryapp.shared.domain.models.getDefaultCategoriesForBusinessType("medical")
        assertTrue(medicalCategories.contains("Prescription Medicines"))

        val fallbackCategories = com.storebook.inventoryapp.shared.domain.models.getDefaultCategoriesForBusinessType("unknown")
        assertTrue(fallbackCategories.contains("Daily Essentials"))
    }
}
