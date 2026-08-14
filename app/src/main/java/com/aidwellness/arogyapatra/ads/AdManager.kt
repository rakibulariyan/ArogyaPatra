package com.aidwellness.arogyapatra.ads

import android.content.Context
import com.aidwellness.arogyapatra.util.AppLogger
import com.google.android.gms.ads.MobileAds

object AdManager {

    var isInitialized = false
        private set

    fun initialize(context: Context) {
        if (isInitialized) return
        MobileAds.initialize(context) { status ->
            isInitialized = true
            AppLogger.d("AdMob SDK initialized successfully: ${status.adapterStatusMap}")
        }
    }
}
