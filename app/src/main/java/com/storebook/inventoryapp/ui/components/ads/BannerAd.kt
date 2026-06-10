package com.storebook.inventoryapp.ui.components.ads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.ui.components.loader.shimmerEffect
import com.storebook.inventoryapp.utils.AnalyticsManager
import com.storebook.inventoryapp.utils.LocalAppConfig

@Composable
fun BannerAdSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) // Standard banner height
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(10.dp)
                    .shimmerEffect()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(30.dp)
                .shimmerEffect(RoundedCornerShape(18.dp))
        )
    }
}

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adId: String? = null,
    isBackground: Boolean = false,
    placement: String = "unknown_screen"
) {
    val appConfig = LocalAppConfig.current
    if (appConfig?.result?.google_ads_on_off != "on") return

    val finalAdId = adId ?: "ca-app-pub-3940256099942544/6300978111" // Default Ad Id
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val adSize = remember(screenWidth) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidth)
    }

    var isAdLoaded by remember { mutableStateOf(false) }
    var isAdFailed by remember { mutableStateOf(false) }

    if (isAdFailed) {
        return
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(adSize)
                    adUnitId = finalAdId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isAdLoaded = true
                            AnalyticsManager.logAdEvent("banner", placement, "loaded")
                        }

                        override fun onAdClicked() {
                            AnalyticsManager.logAdEvent("banner", placement, "clicked")
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            isAdLoaded = false
                            isAdFailed = true
                            AnalyticsManager.logAdEvent("banner", placement, "failed_to_load")
                        }

                        override fun onAdImpression() {
                            AnalyticsManager.logAdEvent("banner", placement, "impression")
                        }
                    }
                    AnalyticsManager.logAdEvent("banner", placement, "request")
                    loadAd(AdRequest.Builder().build())
                }
            }
        )

        if (!isAdLoaded) {
            Surface(
                color = if (isBackground) {
                    colorResource(R.color.black_text)
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adSize.height.dp)
            ) {
                BannerAdSkeleton()
            }
        }
    }
}
