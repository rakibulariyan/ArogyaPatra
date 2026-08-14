package com.aidwellness.arogyapatra.ads

import android.app.Activity
import android.content.Context
import com.aidwellness.arogyapatra.util.AppLogger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Singleton object that manages preloading and presenting Interstitial ads
 * at natural transition points (e.g. after generating a certificate).
 */
object InterstitialAdManager {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    /**
     * Preloads an interstitial ad if not already loading or loaded.
     */
    fun loadAd(context: Context) {
        if (interstitialAd != null || isLoading) return

        // UMP Consent Check: Ensure we have consent before loading
        if (!ConsentManager.canRequestAds(context)) {
            AppLogger.d("InterstitialAdManager: Consent not yet granted, skipping load.")
            return
        }

        isLoading = true
        val request = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            AdConfig.interstitialAdUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    AppLogger.d("InterstitialAdManager: Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    AppLogger.e("InterstitialAdManager: Interstitial ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                }
            }
        )
    }

    /**
     * Presents the interstitial ad if ready. If not ready, immediately executes [onDismiss].
     * Automatically preloads a new ad after dismissal or failure.
     */
    fun showAdIfReady(activity: Activity, onDismiss: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            AppLogger.d("InterstitialAdManager: Ad not ready, skipping.")
            onDismiss()
            // Reload for next time
            loadAd(activity.applicationContext)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                AppLogger.d("InterstitialAdManager: Interstitial ad dismissed.")
                onDismiss()
                // Preload the next ad
                loadAd(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                AppLogger.e("InterstitialAdManager: Interstitial ad failed to show: ${adError.message}")
                onDismiss()
                // Reload for next time
                loadAd(activity.applicationContext)
            }
        }

        ad.show(activity)
    }
}
