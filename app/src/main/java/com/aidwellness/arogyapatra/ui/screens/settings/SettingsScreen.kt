package com.aidwellness.arogyapatra.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.ui.components.LegalSafeguardBanner
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: ArogyaRepository,
    onDoctorProfileClick: () -> Unit,
    onClinicProfileClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onBackupRestoreClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val doctorProfile by repository.getDoctorProfile().collectAsState(initial = null)

    val versionName = remember(context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "1.0.3"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Summary Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDoctorProfileClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldTealPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldTealPrimary, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = if (doctorProfile?.name.isNullOrBlank()) "Set up your profile" else doctorProfile?.name!!, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = "Reg No: ${doctorProfile?.registrationNumber ?: "Configure credentials"}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }

            // Setting Groups List
            Text("General Configuration", style = MaterialTheme.typography.labelLarge.copy(color = EmeraldTealPrimary, fontWeight = FontWeight.SemiBold))

            SettingsTile(
                title = "Clinic Profile & Branding",
                subtitle = "Clinic name, address, contact details and colors",
                icon = Icons.Default.LocalHospital,
                onClick = onClinicProfileClick
            )

            SettingsTile(
                title = "App Security & Unlock",
                subtitle = "PIN code protection and Biometric unlock",
                icon = Icons.Default.Security,
                onClick = onSecurityClick
            )

            SettingsTile(
                title = "Data Backup & Restore",
                subtitle = "Export or import encrypted JSON data backups",
                icon = Icons.Default.Backup,
                onClick = onBackupRestoreClick
            )

            SettingsTile(
                title = "Privacy Policy & Data Safety",
                subtitle = "Local data safety details, UMP ad consent, deletion rules",
                icon = Icons.Default.PrivacyTip,
                onClick = onPrivacyPolicyClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            LegalSafeguardBanner()

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "ArogyaPatra v$versionName", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                Text(text = "Company: Aidwellness Health Tech", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
            }
        }
    }
}

@Composable
private fun SettingsTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = EmeraldTealPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}
