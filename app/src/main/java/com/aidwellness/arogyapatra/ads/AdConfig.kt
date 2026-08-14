package com.aidwellness.arogyapatra.ads

import com.aidwellness.arogyapatra.BuildConfig

object AdConfig {
    // Official Google AdMob Test Ad Unit IDs (Used only for DEBUG builds)
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"

    // Real Production Ad Unit IDs (Created in Google AdMob Console for RELEASE builds)
    private const val PROD_BANNER_ID = "ca-app-pub-9706933622096205/7037563478"
    private const val PROD_APP_OPEN_ID = "ca-app-pub-9706933622096205/3922484975"
    private const val PROD_INTERSTITIAL_ID = "ca-app-pub-9706933622096205/3084880173"
    private const val PROD_NATIVE_ID = "ca-app-pub-9706933622096205/3343262445"

    val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

    val appOpenAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_APP_OPEN_ID else PROD_APP_OPEN_ID

    val interstitialAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

    val nativeAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_NATIVE_ID else PROD_NATIVE_ID
}


