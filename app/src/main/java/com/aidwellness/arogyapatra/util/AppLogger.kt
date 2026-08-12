package com.aidwellness.arogyapatra.util

import android.util.Log
import com.aidwellness.arogyapatra.BuildConfig

object AppLogger {

    private const val TAG = "ArogyaPatra"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, sanitizeMessage(message))
        }
    }

    fun i(message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, sanitizeMessage(message))
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, sanitizeMessage(message), throwable)
        }
    }

    private fun sanitizeMessage(message: String): String {
        // Redact any phone numbers or sensitive keywords if accidentally passed
        return message.replace(Regex("\\+?\\d{10,12}"), "[REDACTED_PHONE]")
    }
}
