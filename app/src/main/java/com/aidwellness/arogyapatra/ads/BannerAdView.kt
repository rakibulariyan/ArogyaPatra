package com.aidwellness.arogyapatra.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aidwellness.arogyapatra.util.AppLogger
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Remembers and loads an AdMob AdView instance at the screen level to prevent
 * excessive ad reloading/recreation during scroll events or recompositions.
 * Properly handles cleanup of resources when the host screen leaves composition.
 */
@Composable
fun rememberAdView(adUnitId: String = AdConfig.bannerAdUnitId): AdView? {
    val context = LocalContext.current

    // Ensure ads are only requested when Google UMP consent permits ad requests
    if (!ConsentManager.canRequestAds(context)) return null

    val adView = remember(context, adUnitId) {
        AdView(context).apply {
            setAdUnitId(adUnitId)
            
            // Compute anchored adaptive banner ad size safely
            val displayMetrics = context.resources.displayMetrics
            val widthPixels = displayMetrics.widthPixels
            val density = displayMetrics.density
            val adWidthDp = (widthPixels / density).toInt()
            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
            
            setAdSize(adSize)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    AppLogger.d("Retained adaptive banner ad loaded successfully (${adSize.width}x${adSize.height})")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    AppLogger.e("Retained adaptive banner ad failed to load: ${error.message} (code: ${error.code})")
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            AppLogger.d("Destroying retained AdView instance to prevent memory leaks")
            adView.destroy()
        }
    }

    return adView
}

/**
 * Display a pre-loaded, retained AdView instance safely inside Compose layout.
 * Ensures the view is detached from any prior parent before layout insertion.
 */
@Composable
fun BannerAdView(
    adView: AdView,
    modifier: Modifier = Modifier
) {
    val adSize = adView.adSize ?: return
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .width(adSize.width.dp)
                .height(adSize.height.dp),
            factory = {
                // Ensure parent view is removed before adding to a new layout
                (adView.parent as? android.view.ViewGroup)?.removeView(adView)
                adView
            }
        )
    }
}
