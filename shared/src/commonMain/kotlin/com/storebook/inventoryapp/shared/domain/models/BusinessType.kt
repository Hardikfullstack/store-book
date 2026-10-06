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

val DEFAULT_CATEGORIES_BY_BUSINESS_TYPE: Map<String, List<String>> = mapOf(
    "general" to listOf(
        "Daily Essentials",
        "Beverages & Drinks",
        "Snacks & Packaged Food",
        "Personal Care & Hygiene",
        "Cleaning & Household",
        "Stationery & Paper",
        "Dairy Products",
        "Miscellaneous",
    ),
    "medical" to listOf(
        "Prescription Medicines",
        "OTC & Health Supplements",
        "First Aid & Surgical",
        "Personal Hygiene & Sanitization",
        "Baby & Mother Care",
        "Medical Equipment & Devices",
        "Ayurvedic & Herbal",
    ),
    "grocery" to listOf(
        "Daily Essentials",
        "Staples, Grains & Flours",
        "Pulses & Dals",
        "Edible Oils & Ghee",
        "Spices, Masalas & Seasonings",
        "Snacks, Biscuits & Namkeen",
        "Beverages & Cold Drinks",
        "Cleaning & Detergents",
        "Personal Care & Soaps",
        "Dairy, Butter & Eggs",
    ),
    "dairy_sweets" to listOf(
        "Fresh Milk & Buttermilk",
        "Curd, Paneer & Cheese",
        "Pure Ghee & Butter",
        "Traditional Sweets (Mithai)",
        "Bengali & Milk Sweets",
        "Ice Cream, Kulfi & Desserts",
        "Dry Fruit Sweets",
        "Namkeen & Farsan",
    ),
    "bakery" to listOf(
        "Fresh Breads & Pav",
        "Cakes, Pastries & Muffins",
        "Cookies, Rusk & Biscuits",
        "Savory Bakes & Puffs",
        "Chocolates & Candies",
        "Baking Ingredients & Pre-mixes",
    ),
    "footwear" to listOf(
        "Men's Formal & Casual Shoes",
        "Women's Shoes & Heels",
        "Kids' Footwear & School Shoes",
        "Sports & Running Shoes",
        "Sandals, Slippers & Flip-Flops",
        "Socks & Shoe Care Accessories",
    ),
    "electronics" to listOf(
        "Mobile Phones & Tablets",
        "Earphones, Headphones & Audio",
        "Chargers, Cables & Power Banks",
        "Mobile Cases & Screen Guards",
        "Computer & Laptop Peripherals",
        "Smart Watches & Wearables",
        "Small Home Appliances",
    ),
    "hardware" to listOf(
        "Hand Tools & Measuring",
        "Power Tools & Machine Accessories",
        "Plumbing, Pipes & Fittings",
        "Fasteners, Screws, Nuts & Bolts",
        "Paints, Primers & Brushes",
        "Adhesives, Tapes & Sealants",
        "Locks, Hinges & Door Fittings",
    ),
    "electrical" to listOf(
        "Switches, Sockets & Boards",
        "House Wires & Power Cables",
        "LED Bulbs, Tubes & Lighting",
        "Ceiling Fans & Exhausts",
        "MCB, Distribution Boxes & Isolators",
        "Extension Boards & Adapters",
        "Conduit Pipes & Fittings",
    ),
    "stationery" to listOf(
        "Notebooks, Registers & Diaries",
        "Pens, Pencils & Markers",
        "Office Stationery & Desk Accessories",
        "Art, Craft & Drawing Supplies",
        "School Supplies & Geometry Boxes",
        "Files, Folders & Document Bags",
        "Paper Reams & Photocopy Paper",
    ),
    "cosmetics" to listOf(
        "Skin Care & Moisturizers",
        "Hair Care, Shampoos & Oils",
        "Color Cosmetics & Makeup",
        "Perfumes, Deodorants & Scents",
        "Oral Care & Toothpastes",
        "Men's Grooming & Shaving",
        "Bath, Body Wash & Soaps",
    ),
)

fun getDefaultCategoriesForBusinessType(businessType: String?): List<String> {
    val key = businessType?.trim()?.lowercase() ?: "general"
    return DEFAULT_CATEGORIES_BY_BUSINESS_TYPE[key]
        ?: DEFAULT_CATEGORIES_BY_BUSINESS_TYPE["general"]
        ?: listOf("General", "Miscellaneous")
}
