package com.storebook.inventoryapp.data.play

object Products {
    const val PRO_LIFETIME = "pro_lifetime"
    const val PRO_MONTHLY = "pro_monthly"
    const val PRO_YEARLY = "pro_yearly"
    val ALL_PRODUCTS = listOf(PRO_LIFETIME, PRO_MONTHLY, PRO_YEARLY)
}

data class BillingState(
    val isProUnlocked: Boolean = false,
    val purchasedProductIds: Set<String> = emptySet(),
    val isBillingReady: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val purchaseToken: String? = null,
    val expirationTimestampMillis: Long? = null
)

sealed interface PurchaseResult {
    object Success : PurchaseResult
    object AlreadyOwned : PurchaseResult
    data class Failure(val message: String) : PurchaseResult
    object Pending : PurchaseResult
}
