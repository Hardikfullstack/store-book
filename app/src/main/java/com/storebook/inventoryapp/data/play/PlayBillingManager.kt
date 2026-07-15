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
import com.google.firebase.auth.FirebaseAuth
import com.storebook.inventoryapp.dataconnect.StorebookConnectorConnector
import com.storebook.inventoryapp.utils.SecurityUtils
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
                if (com.storebook.inventoryapp.BuildConfig.DEBUG) Log.d(TAG, "PurchaseUpdated: code=${billingResult.responseCode} n=${purchaseList?.size}")
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

                            // Persist to SharedPreferences so UI picks it up immediately without re-login
                            if (!ids.isEmpty()) {
                                val prefs = SecurityUtils.getEncryptedPrefs(appContext)
                                prefs.edit().putBoolean("is_premium", true).apply()
                                if (com.storebook.inventoryapp.BuildConfig.DEBUG) Log.d(TAG, "SharedPrefs updated: is_premium=true")
                            }
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
        if (com.storebook.inventoryapp.BuildConfig.DEBUG) Log.d(TAG, "Query result: count=${purchases?.size}")
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

        val inAppProducts = Products.ALL_PRODUCTS.filter { !it.contains("monthly") && !it.contains("yearly") }
        val subsProducts = Products.ALL_PRODUCTS.filter { it.contains("monthly") || it.contains("yearly") }

        val combinedList = mutableListOf<ProductDetails>()
        var queriesCompleted = 0
        var hasError = false
        var lastErrorMsg = ""

        fun checkCompletion() {
            queriesCompleted++
            if (queriesCompleted == 2) {
                if (combinedList.isNotEmpty()) {
                    onSuccess(combinedList)
                } else {
                    onFailed(if (hasError) lastErrorMsg else "No products found")
                }
            }
        }

        if (inAppProducts.isNotEmpty()) {
            val inAppParams = QueryProductDetailsParams.newBuilder()
                .setProductList(inAppProducts.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }).build()
            billingClient.queryProductDetailsAsync(inAppParams) { result, details ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && details != null) {
                    combinedList.addAll(details)
                } else {
                    hasError = true
                    lastErrorMsg = result.debugMessage ?: "Error querying INAPP"
                }
                checkCompletion()
            }
        } else {
            checkCompletion()
        }

        if (subsProducts.isNotEmpty()) {
            val subsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(subsProducts.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }).build()
            billingClient.queryProductDetailsAsync(subsParams) { result, details ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && details != null) {
                    combinedList.addAll(details)
                } else {
                    hasError = true
                    lastErrorMsg = result.debugMessage ?: "Error querying SUBS"
                }
                checkCompletion()
            }
        } else {
            checkCompletion()
        }
    }

    fun launchBillingFlow(
        activity: androidx.activity.ComponentActivity,
        productDetails: ProductDetails,
        offerToken: String? = null,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit,
    ) {
        _state.value = _state.value.copy(isLoading = true)

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (offerToken != null) {
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val params =
            BillingFlowParams
                .newBuilder()
                .setProductDetailsParamsList(
                    listOf(productDetailsParamsBuilder.build()),
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
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (com.storebook.inventoryapp.BuildConfig.DEBUG) Log.w(TAG, "Error disconnecting billing client", e)
        }
    }
}
