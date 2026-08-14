package com.aidwellness.arogyapatra.ads

import android.app.Activity
import android.content.Context
import com.aidwellness.arogyapatra.util.AppLogger
import com.google.android.ump.*

object ConsentManager {

    private var consentInformation: ConsentInformation? = null
    var didFallbackToTrue = false
        private set

    fun gatherConsent(
        activity: Activity,
        onConsentGathered: (canRequestAds: Boolean) -> Unit
    ) {
        android.util.Log.i("ConsentManager", "gatherConsent: Starting consent gathering flow")
        val paramsBuilder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)

        if (com.aidwellness.arogyapatra.BuildConfig.DEBUG) {
            android.util.Log.i("ConsentManager", "gatherConsent: Configuring debug UMP settings with device ID 02338C6012913B39CD6A943B5CBCCBC5")
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .addTestDeviceHashedId("02338C6012913B39CD6A943B5CBCCBC5")
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .build()
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        val params = paramsBuilder.build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        android.util.Log.i("ConsentManager", "gatherConsent: Requesting consent info update")
        consentInformation?.requestConsentInfoUpdate(
            activity,
            params,
            {
                android.util.Log.i("ConsentManager", "gatherConsent: Consent info update success")
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        android.util.Log.e("ConsentManager", "gatherConsent: Consent form load/show error: ${formError.message} (code: ${formError.errorCode})")
                        AppLogger.e("Consent form error: ${formError.message}")
                    } else {
                        android.util.Log.i("ConsentManager", "gatherConsent: Consent form completed successfully")
                    }
                    onConsentGathered(canRequestAds(activity))
                }
            },
            { requestError ->
                android.util.Log.e("ConsentManager", "gatherConsent: Consent info update failed: ${requestError.message} (code: ${requestError.errorCode})")
                AppLogger.e("Consent info update error: ${requestError.message}")
                if (requestError.errorCode == 3) {
                    android.util.Log.w("ConsentManager", "gatherConsent: Publisher misconfiguration (code 3). Falling back to allowing ad requests.")
                    didFallbackToTrue = true
                }
                onConsentGathered(canRequestAds(activity))
            }
        )
    }

    fun canRequestAds(context: Context): Boolean {
        if (didFallbackToTrue) return true
        val info = consentInformation ?: UserMessagingPlatform.getConsentInformation(context)
        return info.canRequestAds()
    }

    fun isPrivacyOptionsRequired(context: Context): Boolean {
        val info = consentInformation ?: UserMessagingPlatform.getConsentInformation(context)
        return info.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun showPrivacyOptionsForm(activity: Activity, onDismiss: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) {
                AppLogger.e("Error showing privacy options form: ${error.message}")
            }
            onDismiss()
        }
    }
}
