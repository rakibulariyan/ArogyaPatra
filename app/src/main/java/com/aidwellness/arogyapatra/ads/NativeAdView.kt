package com.aidwellness.arogyapatra.ads

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.aidwellness.arogyapatra.R
import com.aidwellness.arogyapatra.util.AppLogger
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Loads and retains a state-managed NativeAd instance.
 */
@Composable
fun rememberNativeAd(adUnitId: String = AdConfig.nativeAdUnitId): NativeAd? {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    if (!ConsentManager.canRequestAds(context)) return null

    LaunchedEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd = ad
                AppLogger.d("NativeAdView: Native Ad loaded successfully.")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    AppLogger.e("NativeAdView: Native Ad failed to load: ${error.message} (code: ${error.code})")
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(nativeAd) {
        onDispose {
            nativeAd?.destroy()
        }
    }

    return nativeAd
}

/**
 * Jetpack Compose wrapper for inflating and binding the AdMob NativeAdView layout.
 */
@Composable
fun NativeAdCard(
    nativeAd: NativeAd,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                val inflater = LayoutInflater.from(context)
                val adView = inflater.inflate(R.layout.gma_native_ad_layout, null) as NativeAdView
                
                // Bind views to AdMob NativeAdView layout components
                val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                val callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
                val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)

                adView.headlineView = headlineView
                adView.bodyView = bodyView
                adView.callToActionView = callToActionView
                adView.iconView = iconView

                // Populate data
                headlineView.text = nativeAd.headline
                
                if (nativeAd.body == null) {
                    bodyView.visibility = android.view.View.GONE
                } else {
                    bodyView.visibility = android.view.View.VISIBLE
                    bodyView.text = nativeAd.body
                }

                if (nativeAd.callToAction == null) {
                    callToActionView.visibility = android.view.View.INVISIBLE
                } else {
                    callToActionView.visibility = android.view.View.VISIBLE
                    callToActionView.text = nativeAd.callToAction
                }

                if (nativeAd.icon == null) {
                    iconView.visibility = android.view.View.GONE
                } else {
                    iconView.visibility = android.view.View.VISIBLE
                    iconView.setImageDrawable(nativeAd.icon?.drawable)
                }

                // Register nativeAd object with the NativeAdView container
                adView.setNativeAd(nativeAd)

                adView
            },
            update = { adView ->
                // Registered nativeAd object is already bound in factory;
                // update callback is handled by factory lifecycle.
            }
        )
    }
}
