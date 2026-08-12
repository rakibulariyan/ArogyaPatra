package com.aidwellness.arogyapatra.ui.screens.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import com.aidwellness.arogyapatra.ads.ConsentManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy & Data Safety", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldTealPrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldTealPrimary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("100% Offline Medical Data Storage", fontWeight = FontWeight.Bold, color = EmeraldTealPrimary)
                        Text(
                            "ArogyaPatra does NOT upload patient records, diagnosis data, or certificates to any cloud server.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldTealPrimary
                        )
                    }
                }
            }

            PolicySection(
                title = "1. Local Data Storage & Patient Records",
                content = "All patient information, medical certificates, doctor profiles, and clinic details are stored strictly inside an offline SQLite/Room database on your device. The app operates fully offline for all core medical documentation workflows."
            )

            PolicySection(
                title = "2. PIN Security & Biometric Protection",
                content = "Access to ArogyaPatra is protected by PBKDF2-HMAC-SHA256 salted PIN hashing with brute-force lockout protection and Android BiometricPrompt authentication. Raw PINs are never stored or logged."
            )

            PolicySection(
                title = "3. AES-256-GCM Encrypted Backups",
                content = "When you export data backups, the database JSON container is encrypted using AES-256-GCM with a user-supplied key. Plaintext patient data is never exported unencrypted."
            )

            PolicySection(
                title = "4. PDF Generation & Local Storage",
                content = "Generated medical certificates are compiled into standard A4 PDF files stored in your app-private document directory. PDF files are shared only when you explicitly select the share or print action via Android FileProvider."
            )

            PolicySection(
                title = "5. Advertising & Google UMP Consent",
                content = "ArogyaPatra displays non-intrusive banner advertisements provided by Google AdMob. Google UMP handles European & global privacy consent requirements. AdMob processes device identifiers for advertising purposes in accordance with Google's Privacy Policy. No patient healthcare details or medical certificate contents are ever shared with AdMob or advertising networks."
            )

            PolicySection(
                title = "6. Data Retention & Deletion",
                content = "You retain complete control over your data. You can edit, delete, or wipe patient records and generated certificates at any time directly within the application or by clearing local application storage in Android Settings."
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (ConsentManager.isPrivacyOptionsRequired(context)) {
                val activity = context as? Activity
                if (activity != null) {
                    Button(
                        onClick = {
                            ConsentManager.showPrivacyOptionsForm(activity) {}
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
                    ) {
                        Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Ad Privacy & Consent Settings", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aidwellness.com/arogyapatra/privacy-policy"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = EmeraldTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Full HTTPS Privacy Policy Online", fontWeight = FontWeight.Bold, color = EmeraldTealPrimary)
            }
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}
