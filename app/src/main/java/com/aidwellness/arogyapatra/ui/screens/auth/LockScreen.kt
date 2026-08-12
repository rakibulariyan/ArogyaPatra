package com.aidwellness.arogyapatra.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.aidwellness.arogyapatra.security.SecurityManager
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary

@Composable
fun LockScreen(
    storedPinHash: String?,
    isBiometricEnabled: Boolean,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Trigger biometric if enabled and not locked out
    LaunchedEffect(Unit) {
        if (isBiometricEnabled && !SecurityManager.isLockedOut() && SecurityManager.canAuthenticateWithBiometrics(context)) {
            (context as? FragmentActivity)?.let { activity ->
                SecurityManager.showBiometricPrompt(
                    activity = activity,
                    onSuccess = { onUnlocked() },
                    onError = { err -> errorMessage = err }
                )
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(EmeraldTealPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = EmeraldTealPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ArogyaPatra Security",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Enter 4-digit PIN to access medical records",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                repeat(4) { index ->
                    val isFilled = index < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) EmeraldTealPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad Grid
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        for (key in row) {
                            KeypadButton(
                                key = key,
                                isBiometricAvailable = isBiometricEnabled && SecurityManager.canAuthenticateWithBiometrics(context),
                                onClick = {
                                    if (SecurityManager.isLockedOut()) {
                                        val sec = SecurityManager.getRemainingLockoutSeconds()
                                        errorMessage = "Too many failed attempts. Try again in $sec seconds."
                                        pinInput = ""
                                        return@KeypadButton
                                    }

                                    errorMessage = null
                                    when (key) {
                                        "DEL" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                        "BIO" -> {
                                            (context as? FragmentActivity)?.let { activity ->
                                                SecurityManager.showBiometricPrompt(
                                                    activity = activity,
                                                    onSuccess = { onUnlocked() },
                                                    onError = { err -> errorMessage = err }
                                                )
                                            }
                                        }
                                        else -> {
                                            if (pinInput.length < 4) {
                                                pinInput += key
                                                if (pinInput.length == 4) {
                                                    if (SecurityManager.verifyPin(pinInput, storedPinHash)) {
                                                        onUnlocked()
                                                    } else {
                                                        if (SecurityManager.isLockedOut()) {
                                                            val sec = SecurityManager.getRemainingLockoutSeconds()
                                                            errorMessage = "Too many failed attempts. Locked out for $sec s."
                                                        } else {
                                                            errorMessage = "Incorrect PIN. Try again."
                                                        }
                                                        pinInput = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    isBiometricAvailable: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(
                if (key == "BIO" || key == "DEL") Color.Transparent
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "DEL" -> Icon(imageVector = Icons.Default.Backspace, contentDescription = "Delete")
            "BIO" -> if (isBiometricAvailable) Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Biometric", tint = EmeraldTealPrimary)
            else -> Text(text = key, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 22.sp))
        }
    }
}
