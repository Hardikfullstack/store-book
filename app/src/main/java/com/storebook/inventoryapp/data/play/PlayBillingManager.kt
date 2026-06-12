package com.storebook.inventoryapp.data.play

import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "PlayBillingManager"

class PlayBillingManager(
    private val appContext: android.content.Context,
) {
    private val _state = MutableStateFlow(BillingState())
    val state: StateFlow<BillingState> = _state.asStateFlow()

    private val billingClient =
        BillingClient
            .newBuilder(appContext.applicationContext)
            .setListener { billingResult, purchaseList ->
                Log.d(TAG, "PurchaseUpdated: code=${billingResult.responseCode} n=${purchaseList?.size}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchaseList
                        ?.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                        ?.takeIf { it.isNotEmpty() }
                        ?.let {
                            val ids = it.mapNotNull { p -> p.products.firstOrNull() }.toSet()
                            _state.value =
                                _state.value.copy(
                                    isProUnlocked = true,
                                    purchasedProductIds = ids,
                                )
                        }
                }
            }.enablePendingPurchases()
            .build()

    fun connect() {
        _state.value = _state.value.copy(isLoading = true)
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    val ready = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                    _state.value =
                        _state.value.copy(
                            isBillingReady = ready,
                            isLoading = false,
                            errorMessage = if (!ready) billingResult.debugMessage else null,
                        )
                    if (ready) {
                        queryPurchases()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _state.value = _state.value.copy(isBillingReady = false)
                }
            },
        )
    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams
                .newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        ) { billingResult, purchases ->
            updateProUnlocked(purchases)
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams
                .newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
        ) { billingResult, purchases ->
            updateProUnlocked(purchases)
        }
    }

    private fun updateProUnlocked(purchases: MutableList<Purchase>?) {
        Log.d(TAG, "Query result: count=${purchases?.size}")
        if (purchases?.isNotEmpty() == true) {
            val activeIds =
                purchases
                    .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .mapNotNull { it.products.firstOrNull() }
                    .toSet()

            if (activeIds.isNotEmpty()) {
                _state.value =
                    _state.value.copy(
                        isProUnlocked = true,
                        purchasedProductIds = activeIds,
                    )
            }
        }
    }

    fun isProUnlocked(): Boolean {
        if (_state.value.isProUnlocked) return true
        queryPurchases()
        return _state.value.isProUnlocked
    }

    fun fetchProductDetails(
        onSuccess: (List<ProductDetails>) -> Unit,
        onFailed: (String) -> Unit,
    ) {
        if (!billingClient.isReady) {
            _state.value = _state.value.copy(isBillingReady = false, errorMessage = "Billing not ready")
            onFailed("Billing not ready")
            return
        }

        val params =
            QueryProductDetailsParams
                .newBuilder()
                .setProductList(
                    Products.ALL_PRODUCTS.map { productId ->
                        val isSubscription = productId.contains("monthly") || productId.contains("yearly")
                        QueryProductDetailsParams.Product
                            .newBuilder()
                            .setProductId(productId)
                            .setProductType(
                                if (isSubscription) {
                                    BillingClient.ProductType.SUBS
                                } else {
                                    BillingClient.ProductType.INAPP
                                },
                            ).build()
                    },
                ).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                productDetailsList?.isNotEmpty() == true
            ) {
                onSuccess(productDetailsList)
            } else {
                onFailed(billingResult.debugMessage ?: "No products found")
            }
        }
    }

    fun launchBillingFlow(
        activity: androidx.activity.ComponentActivity,
        productDetails: ProductDetails,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit,
    ) {
        _state.value = _state.value.copy(isLoading = true)

        val params =
            BillingFlowParams
                .newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams
                            .newBuilder()
                            .setProductDetails(productDetails)
                            .build(),
                    ),
                ).build()

        val billingResult = billingClient.launchBillingFlow(activity, params)
        _state.value = _state.value.copy(isLoading = false)

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            onSuccess()
        } else {
            onFail(billingResult.debugMessage ?: "Purchase failed")
        }
    }

    fun endConnection() {
        try {
            billingClient.endConnection()
        } catch (e: Exception) {
            Log.w(TAG, "Error disconnecting billing client", e)
        }
    }
}
