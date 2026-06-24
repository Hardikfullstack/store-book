package com.storebook.inventoryapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes(
    val string: String,
) {
    @Serializable
    data class Language(
        val isFirstTime: Boolean = false,
    ) : Routes("language_screen")

    @Serializable
    object Dashboard : Routes("dashboard_screen")

    @Serializable
    object Inventory : Routes("inventory_screen")

    @Serializable
    object Sales : Routes("sales_screen")

    @Serializable
    object Udhaar : Routes("udhaar_screen")

    @Serializable
    object More : Routes("more_screen")

    @Serializable
    object PremiumPlans : Routes("premium_plans_screen")

    @Serializable
    object SalesHistory : Routes("sales_history_screen")

    @Serializable
    object SalesAnalytics : Routes("sales_analytics_screen")

    @Serializable
    object Auth : Routes("auth_screen")
    @Serializable
    object Quotations : Routes("quotations_screen")

    @Serializable
    object InviteStaff : Routes("invite_staff_screen")

    @Serializable
    object SupplierLedger : Routes("supplier_ledger_screen")

    @Serializable
    object GSTReport : Routes("gst_report_screen")

    @Serializable
    object Splash : Routes("splash_screen")
}
