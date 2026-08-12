package com.aidwellness.arogyapatra.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aidwellness.arogyapatra.backup.BackupManager
import com.aidwellness.arogyapatra.data.local.AppDatabase
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    repository: ArogyaRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context.applicationContext) }

    var isProcessing by remember { mutableStateOf(false) }

    // Launcher for Exporting Encrypted Backup JSON
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            isProcessing = true
            coroutineScope.launch {
                val success = BackupManager.exportEncryptedBackupToUri(context, it, repository)
                isProcessing = false
                if (success) {
                    Toast.makeText(context, "Encrypted backup exported successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Export failed. Try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Launcher for Importing Encrypted Backup JSON
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            isProcessing = true
            coroutineScope.launch {
                val success = BackupManager.importEncryptedBackupFromUri(context, it, database, repository)
                isProcessing = false
                if (success) {
                    Toast.makeText(context, "Backup data restored successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Import failed. Invalid or corrupted backup file.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore Data", fontWeight = FontWeight.Bold) },
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
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldTealPrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldTealPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Your backup contains sensitive healthcare information. It is automatically encrypted with AES-256-GCM before saving to your device storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldTealPrimary
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Export Encrypted Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Saves all patient database records, certificates, templates, and clinic profiles into an encrypted JSON file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { exportLauncher.launch("ArogyaPatra_Encrypted_Backup_${System.currentTimeMillis()}.json") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Encrypted Backup", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Restore from Backup File", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Decrypts and restores patients, certificates, templates, and profiles into the local database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldTealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import & Restore Backup", fontWeight = FontWeight.Bold, color = EmeraldTealPrimary)
                    }
                }
            }

            if (isProcessing) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    CircularProgressIndicator(color = EmeraldTealPrimary)
                }
            }
        }
    }
}
