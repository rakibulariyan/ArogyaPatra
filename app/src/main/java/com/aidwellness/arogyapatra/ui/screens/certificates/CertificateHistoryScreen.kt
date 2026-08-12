package com.aidwellness.arogyapatra.ui.screens.certificates

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidwellness.arogyapatra.data.local.entity.CertificateEntity
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.pdf.PdfGenerator
import com.aidwellness.arogyapatra.ui.components.CertificateItemCard
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateHistoryScreen(
    repository: ArogyaRepository,
    onNewCertificateClick: () -> Unit,
    onCertificateClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var certToDelete by remember { mutableStateOf<CertificateEntity?>(null) }

    val categories = listOf("All", "Medical Certificate", "Sick Leave", "Fitness Certificate", "Medical Examination", "Return to Work", "Custom Certificate")

    val certificates by if (searchQuery.isNotBlank()) {
        repository.searchCertificates(searchQuery).collectAsState(initial = emptyList())
    } else if (selectedCategory != "All") {
        repository.getCertificatesByType(selectedCategory).collectAsState(initial = emptyList())
    } else {
        repository.getAllCertificates().collectAsState(initial = emptyList())
    }

    val appSettings by repository.getAppSettings().collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Certificate Records", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNewCertificateClick) {
                        Icon(Icons.Default.Add, contentDescription = "New Certificate", tint = EmeraldTealPrimary)
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
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by patient, cert # or date...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Horizontal Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            searchQuery = ""
                        },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldTealPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (certificates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No certificates match your search or filter.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(certificates) { cert ->
                        Column {
                            CertificateItemCard(
                                certificate = cert,
                                onPreviewClick = { onCertificateClick(cert.id) },
                                onShareClick = {
                                    coroutineScope.launch {
                                        val doc = repository.getDoctorProfileDirect()
                                        val clinic = repository.getClinicProfileDirect()
                                        val pdfFile = PdfGenerator.generateCertificatePdf(context, cert, doc, clinic)
                                        pdfFile?.let { PdfGenerator.sharePdf(context, it) }
                                    }
                                },
                                onPdfClick = { onCertificateClick(cert.id) }
                            )
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                                            val counter = appSettings?.certificateCounter ?: 101
                                            val prefix = appSettings?.certificatePrefix ?: "MC-2026-"
                                            val newCertNum = "$prefix${String.format(Locale.US, "%06d", counter)}"

                                            val duplicated = cert.copy(
                                                id = 0,
                                                certificateNumber = newCertNum,
                                                issueDate = todayStr,
                                                createdAt = System.currentTimeMillis()
                                            )
                                            val newId = repository.insertCertificate(duplicated)
                                            appSettings?.let { repository.updateAppSettings(it.copy(certificateCounter = counter + 1)) }
                                            Toast.makeText(context, "Certificate duplicated as $newCertNum", Toast.LENGTH_SHORT).show()
                                            onCertificateClick(newId)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Duplicate", fontSize = 11.sp)
                                }

                                TextButton(
                                    onClick = { certToDelete = cert }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    certToDelete?.let { cert ->
        AlertDialog(
            onDismissRequest = { certToDelete = null },
            title = { Text("Delete Certificate") },
            text = { Text("Are you sure you want to delete certificate ${cert.certificateNumber} for ${cert.patientName}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteCertificate(cert)
                            certToDelete = null
                            Toast.makeText(context, "Certificate deleted.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { certToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
