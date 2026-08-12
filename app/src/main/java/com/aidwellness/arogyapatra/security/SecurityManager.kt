package com.aidwellness.arogyapatra.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityManager {

    // Calibrated OWASP PBKDF2 work factor for PIN KDF
    private const val PIN_ITERATIONS = 210000
    private const val KEY_LENGTH = 256
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30000L // 30 seconds

    private var failedAttempts = 0
    private var lockoutTimestampMs = 0L

    fun hashPin(pin: String, existingSalt: ByteArray? = null): String {
        val salt = existingSalt ?: ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val spec: KeySpec = PBEKeySpec(pin.toCharArray(), salt, PIN_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return "${salt.toHex()}:${hash.toHex()}"
    }

    fun verifyPin(inputPin: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrEmpty()) return true

        if (isLockedOut()) {
            return false
        }

        val parts = storedHash.split(":")
        if (parts.size != 2) return false

        val salt = parts[0].fromHex()
        val storedHashBytes = parts[1].fromHex()

        val spec: KeySpec = PBEKeySpec(inputPin.toCharArray(), salt, PIN_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val computedHashBytes = factory.generateSecret(spec).encoded

        val isValid = constantTimeEquals(computedHashBytes, storedHashBytes)
        if (isValid) {
            resetFailedAttempts()
        } else {
            recordFailedAttempt()
        }
        return isValid
    }

    fun isLockedOut(): Boolean {
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            val elapsed = System.currentTimeMillis() - lockoutTimestampMs
            if (elapsed < LOCKOUT_DURATION_MS) {
                return true
            } else {
                resetFailedAttempts()
            }
        }
        return false
    }

    fun getRemainingLockoutSeconds(): Int {
        if (!isLockedOut()) return 0
        val elapsed = System.currentTimeMillis() - lockoutTimestampMs
        val remainingMs = LOCKOUT_DURATION_MS - elapsed
        return (remainingMs / 1000).toInt().coerceAtLeast(1)
    }

    private fun recordFailedAttempt() {
        failedAttempts++
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockoutTimestampMs = System.currentTimeMillis()
        }
    }

    fun resetFailedAttempts() {
        failedAttempts = 0
        lockoutTimestampMs = 0L
    }

    fun canAuthenticateWithBiometrics(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock ArogyaPatra",
        subtitle: String = "Use biometric authentication to access medical documents",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    resetFailedAttempts()
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Biometric authentication failed.")
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Constant-time byte array equality verification using MessageDigest.isEqual
     * to prevent timing side-channel attacks.
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        return MessageDigest.isEqual(a, b)
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray {
        val len = length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
