package com.storebook.inventoryapp.utils

object HsnTaxLookup {
    // Map of HSN Code prefix to GST Rate (%)
    val COMMON_HSN_RATES =
        mapOf(
            // 0% (Exempt)
            "0401" to 0.0, // Milk
            "0407" to 0.0, // Eggs
            "0701" to 0.0, // Potatoes, Tomatoes, Onions
            "0702" to 0.0,
            "0703" to 0.0,
            "1001" to 0.0, // Wheat
            "1006" to 0.0, // Rice
            "1101" to 0.0, // Wheat flour
            // 5%
            "0402" to 5.0, // Milk powder
            "0901" to 5.0, // Coffee
            "0902" to 5.0, // Tea
            "0904" to 5.0, // Spices
            "1507" to 5.0, // Edible oils
            "1701" to 5.0, // Sugar
            "2106" to 5.0, // Sweetmeats
            // 12%
            "0405" to 12.0, // Butter, Ghee
            "2009" to 12.0, // Fruit juices
            "2103" to 12.0, // Sauces, ketchup
            "3004" to 12.0, // Medicaments
            // 18%
            "1806" to 18.0, // Chocolates
            "1905" to 18.0, // Biscuits, Pastries
            "2101" to 18.0, // Instant Coffee
            "2201" to 18.0, // Mineral water
            "3304" to 18.0, // Beauty/makeup prep
            "3305" to 18.0, // Hair preparations
            "3401" to 18.0, // Soaps
            "8517" to 18.0, // Mobile phones
            // 28%
            "2202" to 28.0, // Aerated drinks
            "2402" to 28.0, // Cigarettes
            "8703" to 28.0, // Motor cars
        )

    fun getTaxRate(hsnCode: String): Double? {
        val code = hsnCode.trim()
        if (code.length >= 4) {
            val prefix4 = code.substring(0, 4)
            return COMMON_HSN_RATES[prefix4]
        }
        return null
    }
}
