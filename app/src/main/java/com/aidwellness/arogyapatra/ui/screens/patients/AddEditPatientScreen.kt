package com.aidwellness.arogyapatra.ui.screens.patients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aidwellness.arogyapatra.data.local.entity.PatientEntity
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPatientScreen(
    repository: ArogyaRepository,
    patientId: Long = 0L,
    onSaved: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("1995-01-01") }
    var gender by remember { mutableStateOf("Male") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var pid by remember { mutableStateOf("PAT-${(1000..9999).random()}") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(patientId) {
        if (patientId != 0L) {
            val existing = repository.getPatientById(patientId)
            existing?.let {
                fullName = it.fullName
                dob = it.dob
                gender = it.gender
                phone = it.phone
                address = it.address
                bloodGroup = it.bloodGroup ?: ""
                pid = it.patientId
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (patientId == 0L) "Add New Patient" else "Edit Patient", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = pid,
                onValueChange = { pid = it },
                label = { Text("Patient ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Text("Gender *", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Male", "Female", "Other").forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        label = { Text(g) }
                    )
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Residential Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                minLines = 2
            )

            OutlinedTextField(
                value = bloodGroup,
                onValueChange = { bloodGroup = it },
                label = { Text("Blood Group (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (fullName.isBlank() || dob.isBlank() || phone.isBlank()) {
                        errorMessage = "Please fill all required fields (*)."
                    } else {
                        coroutineScope.launch {
                            val patient = PatientEntity(
                                id = patientId,
                                patientId = pid,
                                fullName = fullName.trim(),
                                dob = dob.trim(),
                                gender = gender,
                                phone = phone.trim(),
                                address = address.ifBlank { "N/A" },
                                bloodGroup = bloodGroup.ifBlank { null }
                            )
                            if (patientId == 0L) {
                                repository.insertPatient(patient)
                            } else {
                                repository.updatePatient(patient)
                            }
                            onSaved()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
            ) {
                Text(if (patientId == 0L) "Save Patient" else "Update Patient", fontWeight = FontWeight.Bold)
            }
        }
    }
}
