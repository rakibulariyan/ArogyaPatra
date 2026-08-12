package com.aidwellness.arogyapatra.ads

import android.app.Activity
import android.content.Context
import com.aidwellness.arogyapatra.util.AppLogger
import com.google.android.ump.*

object ConsentManager {

    private var consentInformation: ConsentInformation? = null

    fun gatherConsent(
        activity: Activity,
        onConsentGathered: (canRequestAds: Boolean) -> Unit
    ) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation?.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        AppLogger.e("Consent form error: ${formError.message}")
                    }
                    onConsentGathered(canRequestAds(activity))
                }
            },
            { requestError ->
                AppLogger.e("Consent info update error: ${requestError.message}")
                onConsentGathered(canRequestAds(activity))
            }
        )
    }

    fun canRequestAds(context: Context): Boolean {
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
