package com.aidwellness.arogyapatra.ui.screens.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidwellness.arogyapatra.ads.BannerAdView
import com.aidwellness.arogyapatra.ads.rememberAdView
import com.aidwellness.arogyapatra.ads.rememberNativeAd
import com.aidwellness.arogyapatra.ads.NativeAdCard
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.pdf.PdfGenerator
import com.aidwellness.arogyapatra.ui.components.*
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    repository: ArogyaRepository,
    onNewCertificateClick: () -> Unit,
    onCreatePatientClick: () -> Unit,
    onTemplatesClick: () -> Unit,
    onCertificatePreviewClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val adView = rememberAdView()
    val nativeAd = rememberNativeAd()

    val doctorProfile by repository.getDoctorProfile().collectAsState(initial = null)
    val clinicProfile by repository.getClinicProfile().collectAsState(initial = null)

    val todayStr = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val monthYearStr = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date()) }

    val todayCount by repository.getCertificatesTodayCount(todayStr).collectAsState(initial = 0)
    val monthCount by repository.getCertificatesThisMonthCount(monthYearStr).collectAsState(initial = 0)
    val patientCount by repository.getPatientCount().collectAsState(initial = 0)

    val recentCertificates by repository.getAllCertificates().collectAsState(initial = emptyList())

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // 1. Doctor & Clinic Greeting Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldTealPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "$greeting, ${if (doctorProfile?.name.isNullOrBlank()) "Doctor" else doctorProfile?.name}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (clinicProfile?.clinicName.isNullOrBlank()) "Your Clinic" else clinicProfile?.clinicName!!,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }
                }
            }

            // 2. Quick Actions Section
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionButton(
                        title = "+ New Cert",
                        icon = Icons.Default.AddCircle,
                        backgroundColor = EmeraldTealPrimary,
                        contentColor = Color.White,
                        onClick = onNewCertificateClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Add Patient",
                        icon = Icons.Default.PersonAdd,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onCreatePatientClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Templates",
                        icon = Icons.Default.Article,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onTemplatesClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2b. Native Advanced Ad Card (User-friendly and highly visible integration)
            nativeAd?.let { ad ->
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    NativeAdCard(nativeAd = ad)
                }
            }

            // 3. Statistics Section
            item {
                Text(
                    text = "Dashboard Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatsCard(
                        title = "Certificates Today",
                        value = todayCount.toString(),
                        icon = Icons.Default.Today,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "This Month",
                        value = monthCount.toString(),
                        icon = Icons.Default.DateRange,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Total Patients",
                        value = patientCount.toString(),
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Recent Certificates Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Recent Certificates",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${recentCertificates.size} Total",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }
            }

            // Recent Certificates List
            if (recentCertificates.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No certificates issued yet. Click '+ New Cert' to generate your first document.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(recentCertificates.take(5)) { cert ->
                    CertificateItemCard(
                        certificate = cert,
                        onPreviewClick = { onCertificatePreviewClick(cert.id) },
                        onShareClick = {
                            coroutineScope.launch {
                                val doc = repository.getDoctorProfileDirect()
                                val clinic = repository.getClinicProfileDirect()
                                val pdfFile = PdfGenerator.generateCertificatePdf(context, cert, doc, clinic)
                                pdfFile?.let { PdfGenerator.sharePdf(context, it) }
                            }
                        },
                        onPdfClick = { onCertificatePreviewClick(cert.id) }
                    )
                }
            }

            // 5. Non-Intrusive AdMob Banner
            adView?.let { ad ->
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    BannerAdView(adView = ad)
                }
            }

            // 6. Legal Safeguard Banner
            item {
                Spacer(modifier = Modifier.height(4.dp))
                LegalSafeguardBanner()
            }
        }
    }

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    fontSize = 12.sp
                )
            )
        }
    }
}
