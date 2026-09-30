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
}
