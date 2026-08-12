package com.aidwellness.arogyapatra.ui.screens.certificates

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidwellness.arogyapatra.data.local.entity.CertificateEntity
import com.aidwellness.arogyapatra.data.local.entity.ClinicProfileEntity
import com.aidwellness.arogyapatra.data.local.entity.DoctorProfileEntity
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.pdf.PdfGenerator
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificatePreviewScreen(
    repository: ArogyaRepository,
    certId: Long,
    onBackToHome: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var certificate by remember { mutableStateOf<CertificateEntity?>(null) }
    val doctorProfile by repository.getDoctorProfile().collectAsState(initial = null)
    val clinicProfile by repository.getClinicProfile().collectAsState(initial = null)

    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(certId) {
        val cert = repository.getCertificateById(certId)
        certificate = cert
        cert?.let {
            val doc = repository.getDoctorProfileDirect()
            val clinic = repository.getClinicProfileDirect()
            generatedPdfFile = PdfGenerator.generateCertificatePdf(context, it, doc, clinic)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Certificate Preview", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onBackToHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        },
        bottomBar = {
            certificate?.let { cert ->
                Surface(
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                generatedPdfFile?.let { file ->
                                    Toast.makeText(context, "PDF saved to: ${file.name}", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                generatedPdfFile?.let { file ->
                                    PdfGenerator.sharePdf(context, file)
                                }
                            },
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 13.sp)
                        }

                        IconButton(
                            onClick = {
                                generatedPdfFile?.let { file ->
                                    PdfGenerator.printPdf(context, file)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Print", tint = EmeraldTealPrimary)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        certificate?.let { cert ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // A4 Document Paper Simulation Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        // Clinic Header
                        Text(
                            text = clinicProfile?.clinicName ?: "Clinic",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = EmeraldTealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = clinicProfile?.address ?: "123 Healthcare Boulevard, Tech City",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 11.sp)
                        )
                        Text(
                            text = "Phone: ${clinicProfile?.phone ?: "+91 98765 43210"} | Email: ${clinicProfile?.email ?: "info@aidwellness.com"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 11.sp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = EmeraldTealPrimary, thickness = 2.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Certificate Title
                        Text(
                            text = cert.certificateType.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = EmeraldTealPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Cert No & Date Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Certificate No: ${cert.certificateNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                                )
                                Text(
                                    text = "Date: ${cert.issueDate}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Patient Info Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "PATIENT DETAILS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldTealPrimary))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Name: ${cert.patientName}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black))
                                Text(text = "Age / Gender: ${cert.patientAge} Yrs, ${cert.patientGender}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black))
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Main Medical Body Content
                        val formattedBodyText = buildString {
                            append("This is to certify that ${cert.patientName}, aged ${cert.patientAge} years, ${cert.patientGender}, ")
                            when (cert.certificateType) {
                                "Sick Leave" -> {
                                    append("has been suffering from ${cert.diagnosis ?: "clinical illness"}. ")
                                    append("I have advised him/her complete rest from duties for a period of ${cert.durationDays ?: 1} days ")
                                    append("from ${cert.startDate ?: cert.issueDate} to ${cert.endDate ?: cert.issueDate}.")
                                }
                                "Fitness Certificate" -> {
                                    append("was medically examined on ${cert.issueDate}. ")
                                    append("${cert.fitnessStatement ?: "Based on clinical examination, the individual is medically fit."} ")
                                    if (!cert.restrictions.isNullOrBlank()) {
                                        append("Restrictions/Remarks: ${cert.restrictions}.")
                                    }
                                }
                                "Medical Examination" -> {
                                    append("was examined for ${cert.purpose ?: "general health assessment"}. ")
                                    append("Clinical Findings: ${cert.clinicalFindings ?: "No remarkable pathology detected"}. ")
                                    if (!cert.doctorRemarks.isNullOrBlank()) {
                                        append("Remarks: ${cert.doctorRemarks}.")
                                    }
                                }
                                "Return to Work" -> {
                                    append("was previously advised medical leave. Following re-examination, ")
                                    append("he/she is declared fit to resume duties from ${cert.startDate ?: cert.issueDate}. ")
                                    if (!cert.restrictions.isNullOrBlank()) {
                                        append("Work Restrictions: ${cert.restrictions}.")
                                    }
                                }
                                else -> {
                                    if (!cert.customContent.isNullOrBlank()) {
                                        append(cert.customContent)
                                    } else {
                                        append("has been examined and treated for ${cert.diagnosis ?: "medical condition"}. ")
                                        if (!cert.doctorRemarks.isNullOrBlank()) {
                                            append("Remarks: ${cert.doctorRemarks}.")
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = formattedBodyText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black,
                                lineHeight = 20.sp,
                                fontSize = 13.sp
                            )
                        )

                        if (!cert.doctorRemarks.isNullOrBlank() && cert.certificateType != "Medical Examination") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "DOCTOR REMARKS:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.DarkGray))
                            Text(text = cert.doctorRemarks, style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black, fontSize = 13.sp))
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // Signature & QR Footer
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Left QR Verification badge
                            Column {
                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Black)
                                Text("Scan to Verify", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.Gray))
                            }

                            // Right Doctor Stamp & Signature block
                            Column(horizontalAlignment = Alignment.End) {
                                Text("AUTHORIZED SIGNATURE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(doctorProfile?.name ?: "Doctor", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black))
                                Text(doctorProfile?.qualification ?: "", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                Text("Reg No: ${doctorProfile?.registrationNumber ?: ""}", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Generated via ArogyaPatra. Medical certificates must be issued strictly by authorized healthcare professionals.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 9.sp, textAlign = TextAlign.Center),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
