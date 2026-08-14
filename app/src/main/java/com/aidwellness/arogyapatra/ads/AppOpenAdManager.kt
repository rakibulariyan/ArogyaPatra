package com.aidwellness.arogyapatra.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.aidwellness.arogyapatra.util.AppLogger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * Utility class that handles loading, showing, and observing lifecycle events
 * to present App Open Ads when the app goes to foreground.
 */
class AppOpenAdManager(private val myApplication: Application) :
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false

    private var currentActivity: Activity? = null
    private var loadTime: Long = 0

    init {
        myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Request an App Open Ad if consent is granted and we aren't already loading.
     */
    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) return

        // UMP Consent Check: Ensure we have consent before loading
        if (!ConsentManager.canRequestAds(myApplication)) {
            AppLogger.d("AppOpenAdManager: Consent not yet granted, skipping load.")
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            myApplication,
            AdConfig.appOpenAdUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    AppLogger.d("AppOpenAdManager: App Open Ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    AppLogger.e("AppOpenAdManager: Failed to load App Open Ad: ${loadAdError.message} (code: ${loadAdError.code})")
                }
            }
        )
    }

    /**
     * Check if ad was loaded recently (less than 4 hours ago) and is not null.
     */
    private fun isAdAvailable(): Boolean {
        val wasLoadTimeLessThanFourHoursAgo = Date().time - loadTime < 3600000 * 4
        return appOpenAd != null && wasLoadTimeLessThanFourHoursAgo
    }

    /**
     * Show the App Open Ad if available and if we're not currently showing one.
     */
    fun showAdIfAvailable(activity: Activity) {
        if (isShowingAd) {
            AppLogger.d("AppOpenAdManager: Already showing an ad, skipping.")
            return
        }

        if (!isAdAvailable()) {
            AppLogger.d("AppOpenAdManager: Ad not available, attempting to load one.")
            loadAd()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                AppLogger.d("AppOpenAdManager: App Open Ad dismissed.")
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                AppLogger.e("AppOpenAdManager: App Open Ad failed to show: ${adError.message}")
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                AppLogger.d("AppOpenAdManager: App Open Ad showing.")
            }
        }

        appOpenAd?.show(activity)
    }

    // ── Process Lifecycle Observer Callbacks ──────────────────────────────────

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let {
            showAdIfAvailable(it)
        }
    }

    // ── Activity Lifecycle Observer Callbacks ─────────────────────────────────

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (!isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
