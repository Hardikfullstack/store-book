package com.storebook.inventoryapp.shared.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class BusinessTypeOption(
    val id: String,
    val label: String,
    val emoji: String,
    val description: String,
)

val BUSINESS_TYPES: List<BusinessTypeOption> = listOf(
    BusinessTypeOption(
        id = "general",
        label = "General Store",
        emoji = "🏪",
        description = "All-purpose general retail store with mixed goods",
    ),
    BusinessTypeOption(
        id = "medical",
        label = "Medical & Pharmacy",
        emoji = "💊",
        description = "Pharmacies, medical stores, and healthcare supplies",
    ),
    BusinessTypeOption(
        id = "grocery",
        label = "Grocery & Kirana",
        emoji = "🛒",
        description = "Daily provisions, food items, and household goods",
    ),
    BusinessTypeOption(
        id = "dairy_sweets",
        label = "Dairy & Sweets (Mithai)",
        emoji = "🥛",
        description = "Milk products, sweets, desserts, and snacks",
    ),
    BusinessTypeOption(
        id = "bakery",
        label = "Bakery & Confectionery",
        emoji = "🍞",
        description = "Breads, pastries, cakes, biscuits, and confectionery",
    ),
    BusinessTypeOption(
        id = "footwear",
        label = "Footwear Store",
        emoji = "👟",
        description = "Shoes, sandals, slippers, and leather accessories",
    ),
    BusinessTypeOption(
        id = "electronics",
        label = "Electronics & Mobiles",
        emoji = "📱",
        description = "Mobile phones, accessories, gadgets, and consumer electronics",
    ),
    BusinessTypeOption(
        id = "hardware",
        label = "Hardware & Tools",
        emoji = "🔨",
        description = "Construction tools, plumbing, paints, and fasteners",
    ),
    BusinessTypeOption(
        id = "electrical",
        label = "Electrical & Lighting",
        emoji = "💡",
        description = "Wiring, switches, lighting fixtures, and electrical appliances",
    ),
    BusinessTypeOption(
        id = "stationery",
        label = "Books & Stationery",
        emoji = "📚",
        description = "Office stationery, books, school supplies, and printing",
    ),
    BusinessTypeOption(
        id = "cosmetics",
        label = "Beauty & Cosmetics",
        emoji = "💄",
        description = "Makeup, skincare, haircare, and personal grooming products",
    ),
)

fun getBusinessTypeLabel(businessType: String?): String {
    if (businessType.isNullOrBlank()) return "General Store"
    return BUSINESS_TYPES.find { it.id == businessType }?.label ?: businessType
}

fun getBusinessTypeEmoji(businessType: String?): String {
    if (businessType.isNullOrBlank()) return "🏪"
    return BUSINESS_TYPES.find { it.id == businessType }?.emoji ?: "🏪"
}

fun isValidBusinessType(type: String): Boolean {
    return BUSINESS_TYPES.any { it.id == type }
}
