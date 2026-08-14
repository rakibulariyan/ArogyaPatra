package com.aidwellness.arogyapatra

import android.app.Application
import com.aidwellness.arogyapatra.ads.AppOpenAdManager

class ArogyaApplication : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        appOpenAdManager = AppOpenAdManager(this)
    }
}
