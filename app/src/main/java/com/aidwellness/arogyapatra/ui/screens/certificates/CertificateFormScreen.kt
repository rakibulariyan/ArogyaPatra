package com.aidwellness.arogyapatra.ui.screens.certificates

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidwellness.arogyapatra.data.local.entity.CertificateEntity
import com.aidwellness.arogyapatra.data.local.entity.PatientEntity
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.domain.usecase.CalculateCertificateDurationUseCase
import com.aidwellness.arogyapatra.domain.usecase.GenerateCertificateNumberUseCase
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateFormScreen(
    repository: ArogyaRepository,
    certType: String,
    patientId: Long,
    onGeneratePreview: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var patient by remember { mutableStateOf<PatientEntity?>(null) }
    val appSettings by repository.getAppSettings().collectAsState(initial = null)

    val todayStr = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }

    var issueDate by remember { mutableStateOf(todayStr) }
    var startDate by remember { mutableStateOf(todayStr) }
    var endDate by remember { mutableStateOf(todayStr) }

    var diagnosis by remember { mutableStateOf("") }
    var clinicalFindings by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var fitnessStatement by remember { mutableStateOf("Medically fit to resume normal duties.") }
    var restrictions by remember { mutableStateOf("") }
    var doctorRemarks by remember { mutableStateOf("") }
    var customContent by remember { mutableStateOf("") }

    val durationCalculator = remember { CalculateCertificateDurationUseCase() }
    val certNumGenerator = remember { GenerateCertificateNumberUseCase() }

    val durationDays = remember(startDate, endDate) {
        durationCalculator.execute(startDate, endDate)
    }

    LaunchedEffect(patientId) {
        patient = repository.getPatientById(patientId)
        val template = repository.getTemplateByType(certType)
        template?.let {
            if (customContent.isBlank()) {
                customContent = it.bodyText
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$certType Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        patient?.let { p ->
            val computedAge = p.calculateAgeAsOf(issueDate)

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Step 3 of 3 — Fill Medical Details",
                    style = MaterialTheme.typography.labelLarge.copy(color = EmeraldTealPrimary, fontWeight = FontWeight.SemiBold)
                )

                // Selected Patient Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldTealPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = p.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "${p.gender}, $computedAge yrs (DOB: ${p.dob}) • ID: ${p.patientId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }

                OutlinedTextField(
                    value = issueDate,
                    onValueChange = { issueDate = it },
                    label = { Text("Issue Date") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Type specific form inputs with mandatory validation
                when (certType) {
                    "Sick Leave" -> {
                        OutlinedTextField(
                            value = diagnosis,
                            onValueChange = { diagnosis = it },
                            label = { Text("Diagnosis / Clinical Illness *") },
                            placeholder = { Text("e.g. Acute Typhoid / Lumbar Strain") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("From Date *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("To Date *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldTealPrimary.copy(alpha = 0.1f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = EmeraldTealPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-Calculated Rest Duration: $durationDays days",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldTealPrimary,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }

                    "Fitness Certificate" -> {
                        OutlinedTextField(
                            value = fitnessStatement,
                            onValueChange = { fitnessStatement = it },
                            label = { Text("Fitness Assessment Statement *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 2
                        )
                        OutlinedTextField(
                            value = restrictions,
                            onValueChange = { restrictions = it },
                            label = { Text("Restrictions / Exemptions (Optional)") },
                            placeholder = { Text("e.g. Avoid heavy lifting for 2 weeks") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    "Medical Examination" -> {
                        OutlinedTextField(
                            value = purpose,
                            onValueChange = { purpose = it },
                            label = { Text("Examination Purpose *") },
                            placeholder = { Text("e.g. Employment Physical Clearance") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = clinicalFindings,
                            onValueChange = { clinicalFindings = it },
                            label = { Text("Clinical Findings *") },
                            placeholder = { Text("e.g. BP 120/80, Pulse 72/min, Chest Clear") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 2
                        )
                    }

                    "Return to Work" -> {
                        OutlinedTextField(
                            value = diagnosis,
                            onValueChange = { diagnosis = it },
                            label = { Text("Previous Medical Condition") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("Leave From") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("Leave To") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = restrictions,
                            onValueChange = { restrictions = it },
                            label = { Text("Work Restrictions (if any)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    else -> {
                        OutlinedTextField(
                            value = customContent,
                            onValueChange = { customContent = it },
                            label = { Text("Certificate Text / Content *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 4
                        )
                    }
                }

                OutlinedTextField(
                    value = doctorRemarks,
                    onValueChange = { doctorRemarks = it },
                    label = { Text("Doctor Remarks & Advice (Optional)") },
                    placeholder = { Text("e.g. Follow-up after 7 days") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Strict Form Validation
                        val isFormValid = when (certType) {
                            "Sick Leave" -> diagnosis.isNotBlank()
                            "Fitness Certificate" -> fitnessStatement.isNotBlank()
                            "Medical Examination" -> purpose.isNotBlank() && clinicalFindings.isNotBlank()
                            "Custom Certificate" -> customContent.isNotBlank()
                            else -> true
                        }

                        if (!isFormValid) {
                            Toast.makeText(context, "Please fill required medical fields (*)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        coroutineScope.launch {
                            val counter = appSettings?.certificateCounter ?: 101
                            val certNum = certNumGenerator.execute(appSettings)

                            val newCert = CertificateEntity(
                                certificateNumber = certNum,
                                patientId = p.id,
                                patientName = p.fullName,
                                patientAge = computedAge,
                                patientGender = p.gender,
                                certificateType = certType,
                                issueDate = issueDate,
                                startDate = startDate,
                                endDate = endDate,
                                durationDays = durationDays,
                                diagnosis = diagnosis.ifBlank { null },
                                clinicalFindings = clinicalFindings.ifBlank { null },
                                purpose = purpose.ifBlank { null },
                                fitnessStatement = fitnessStatement.ifBlank { null },
                                restrictions = restrictions.ifBlank { null },
                                doctorRemarks = doctorRemarks.ifBlank { null },
                                customContent = customContent.ifBlank { null }
                            )

                            val insertedId = repository.insertCertificate(newCert)
                            appSettings?.let {
                                repository.updateAppSettings(it.copy(certificateCounter = counter + 1))
                            }
                            onGeneratePreview(insertedId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
                ) {
                    Text("Generate & Preview Certificate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
