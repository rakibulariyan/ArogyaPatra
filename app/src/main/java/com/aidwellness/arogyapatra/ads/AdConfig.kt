package com.aidwellness.arogyapatra.ads

import com.aidwellness.arogyapatra.BuildConfig

object AdConfig {
    // Official Google AdMob Test Banner Ad Unit ID (Used only for DEBUG builds)
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    // Real Production Banner Ad Unit ID (Created in Google AdMob Console for RELEASE builds)
    private const val PROD_BANNER_ID = "ca-app-pub-9706933622096205/7037563478"

    val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID
}


