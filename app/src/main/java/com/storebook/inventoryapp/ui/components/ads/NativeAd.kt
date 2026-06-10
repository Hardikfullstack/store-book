package com.storebook.inventoryapp.ui.components.ads

import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.storebook.inventoryapp.R
import com.storebook.inventoryapp.ui.components.loader.shimmerEffect
import com.storebook.inventoryapp.ui.theme.isAppDarkMode
import com.storebook.inventoryapp.utils.AnalyticsManager
import com.storebook.inventoryapp.utils.LocalAppConfig

@Composable
fun NativeAd(
    adId: String?,
    modifier: Modifier = Modifier,
    isSmall: Boolean = true,
    isBackground: Boolean = false,
    useExitLayout: Boolean = false,
    placement: String = "unknown_screen"
) {
    val appConfig = LocalAppConfig.current
    if (appConfig?.result?.google_ads_on_off != "on") return

    val finalAdId = adId ?: "ca-app-pub-3940256099942544/2247696110" // Google Test ID
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isAdLoaded by remember { mutableStateOf(false) }
    var isAdFailed by remember { mutableStateOf(false) }

    if (isAdFailed) {
        return
    }

    LaunchedEffect(finalAdId) {
        AnalyticsManager.logAdEvent("native", placement, "request")
        val adLoader = AdLoader.Builder(context, finalAdId)
            .forNativeAd { ad: NativeAd ->
                nativeAd?.destroy()
                nativeAd = ad
                isAdLoaded = true
                AnalyticsManager.logAdEvent("native", placement, "loaded")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isAdLoaded = false
                    isAdFailed = true
                    AnalyticsManager.logAdEvent("native", placement, "failed_to_load")
                }

                override fun onAdClicked() {
                    AnalyticsManager.logAdEvent("native", placement, "clicked")
                }

                override fun onAdImpression() {
                    AnalyticsManager.logAdEvent("native", placement, "impression")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(Unit) {
        onDispose { nativeAd?.destroy() }
    }

    val cornerRadius = if (useExitLayout) 14.dp else 10.dp
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSecondaryColor = MaterialTheme.colorScheme.onSecondary

    val layoutId = remember(useExitLayout, isSmall) {
        when {
            useExitLayout -> R.layout.ad_native_exit
            isSmall -> R.layout.ad_native_small
            else -> R.layout.ad_native_medium
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        if (isAdLoaded && nativeAd != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(cornerRadius)),
                factory = { ctx ->
                    val adView = LayoutInflater.from(ctx).inflate(layoutId, null) as NativeAdView

                    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                    val callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
                    val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
                    val cardView = adView.getChildAt(0) as? CardView

                    adView.headlineView = headlineView
                    adView.bodyView = bodyView
                    adView.callToActionView = callToActionView
                    adView.iconView = iconView

                    val cardBgColor = if (isBackground) {
                        ctx.getColor(R.color.black_text)
                    } else {
                        onPrimaryColor.toArgb()
                    }
                    cardView?.setCardBackgroundColor(cardBgColor)
                    headlineView?.setTextColor(secondaryColor.toArgb())
                    bodyView?.setTextColor(onSecondaryColor.toArgb())

                    if (!isSmall || useExitLayout) {
                        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
                        adView.mediaView = mediaView
                    }

                    nativeAd?.let { ad ->
                        headlineView?.text = ad.headline

                        if (ad.body == null) {
                            bodyView?.visibility = android.view.View.INVISIBLE
                        } else {
                            bodyView?.visibility = android.view.View.VISIBLE
                            bodyView?.text = ad.body
                        }

                        if (ad.callToAction == null) {
                            callToActionView?.visibility = android.view.View.INVISIBLE
                        } else {
                            callToActionView?.visibility = android.view.View.VISIBLE
                            callToActionView?.text = ad.callToAction
                        }

                        if (ad.icon == null) {
                            iconView?.visibility = android.view.View.GONE
                        } else {
                            iconView?.setImageDrawable(ad.icon?.drawable)
                            iconView?.visibility = android.view.View.VISIBLE
                        }

                        adView.setNativeAd(ad)
                    }

                    adView
                },
                update = {
                    // We could rebind here if data updates rapidly
                }
            )
        } else {
            Surface(
                color = if (isBackground) {
                    colorResource(R.color.black_text)
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(cornerRadius)),
                shape = RoundedCornerShape(cornerRadius)
            ) {
                if (useExitLayout) {
                    NativeAdExitSkeleton()
                } else if (isSmall) {
                    BannerAdSkeleton()
                } else {
                    NativeAdMediumSkeleton()
                }
            }
        }
    }
}

@Composable
fun NativeAdExitSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shimmerEffect(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shimmerEffect(RoundedCornerShape(20.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .shimmerEffect()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shimmerEffect(RoundedCornerShape(50))
        )
    }
}

@Composable
fun NativeAdMediumSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isAppDarkMode) Color(0xFF212121) else MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shimmerEffect(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .shimmerEffect()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .shimmerEffect(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .shimmerEffect(RoundedCornerShape(50))
        )
    }
}
