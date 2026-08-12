package com.aidwellness.arogyapatra.ads

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aidwellness.arogyapatra.util.AppLogger
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.bannerAdUnitId
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        val configuration = LocalConfiguration.current

        // Ensure ads are only requested when Google UMP consent permits ad requests
        if (!ConsentManager.canRequestAds(context)) {
            return@BoxWithConstraints
        }

        // Determine available screen/container width in dp safely
        val adWidthDp = remember(maxWidth, configuration.screenWidthDp) {
            if (maxWidth.value.isFinite() && maxWidth.value > 0) {
                maxWidth.value.toInt()
            } else {
                configuration.screenWidthDp
            }
        }

        if (adWidthDp <= 0) return@BoxWithConstraints

        // Compute anchored adaptive banner ad size based on available width
        val adSize = remember(adWidthDp, context) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
        }

        var isAdLoaded by remember { mutableStateOf(false) }

        val bannerHeightDp = remember(adSize) {
            adSize.height.dp
        }

        // key(adSize) ensures AdView is only re-created when width/orientation changes,
        // avoiding duplicate ad requests during normal screen recompositions.
        key(adSize) {
            AndroidView(
                modifier = Modifier
                    .width(adSize.width.dp)
                    .height(bannerHeightDp),
                factory = { factoryContext ->
                    AdView(factoryContext).apply {
                        setAdSize(adSize)
                        setAdUnitId(adUnitId)
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isAdLoaded = true
                                AppLogger.d("Anchored adaptive banner ad loaded successfully (${adSize.width}x${adSize.height})")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                isAdLoaded = false
                                AppLogger.e("Anchored adaptive banner ad failed to load: ${error.message} (code: ${error.code})")
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { adView ->
                    adView.destroy()
                }
            )
        }
    }
}

