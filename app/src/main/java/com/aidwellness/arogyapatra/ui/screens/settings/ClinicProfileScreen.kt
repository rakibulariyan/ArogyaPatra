package com.aidwellness.arogyapatra.ui.screens.settings

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aidwellness.arogyapatra.data.local.entity.ClinicProfileEntity
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.ui.components.ImageCropDialog
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import com.aidwellness.arogyapatra.util.BrandingImageStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicProfileScreen(
    repository: ArogyaRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clinicProfile by repository.getClinicProfile().collectAsState(initial = null)

    var clinicName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<String?>(null) }
    var stampUri by remember { mutableStateOf<String?>(null) }
    var primaryColorHex by remember { mutableStateOf("#007A78") }
    var secondaryColorHex by remember { mutableStateOf("#00B4D8") }

    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var pickTarget by remember { mutableStateOf<String?>(null) } // "LOGO" or "STAMP"
    var showCropDialog by remember { mutableStateOf(false) }
    var showRemoveStampConfirmDialog by remember { mutableStateOf(false) }
    var showRemoveLogoConfirmDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedImageUri = uri
            showCropDialog = true
        }
    }

    val presetColors = listOf("#007A78", "#1E40AF", "#047857", "#B91C1C", "#6D28D9", "#0F172A")

    LaunchedEffect(clinicProfile) {
        clinicProfile?.let {
            clinicName = it.clinicName
            address = it.address
            phone = it.phone
            email = it.email
            logoUri = it.logoUri
            stampUri = it.organizationStampUri
            primaryColorHex = it.primaryColorHex
            secondaryColorHex = it.secondaryColorHex
        }
    }

    if (showCropDialog && pickedImageUri != null) {
        val title = if (pickTarget == "LOGO") "Crop Clinic Logo" else "Crop Organization Stamp"
        val aspect = if (pickTarget == "LOGO") 2f / 1f else 1f / 1f

        ImageCropDialog(
            imageUri = pickedImageUri!!,
            title = title,
            aspectRatio = aspect,
            onCropApplied = { croppedBitmap: Bitmap ->
                showCropDialog = false
                if (pickTarget == "LOGO") {
                    val savedUri = BrandingImageStorage.saveClinicLogo(context, croppedBitmap, logoUri)
                    if (savedUri != null) logoUri = savedUri
                } else {
                    val savedUri = BrandingImageStorage.saveOrganizationStamp(context, croppedBitmap, stampUri)
                    if (savedUri != null) stampUri = savedUri
                }
            },
            onCancel = { showCropDialog = false }
        )
    }

    if (showRemoveStampConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveStampConfirmDialog = false },
            title = { Text("Remove organization stamp?") },
            text = { Text("The stamp will be removed from generated medical certificates.") },
            confirmButton = {
                TextButton(onClick = {
                    showRemoveStampConfirmDialog = false
                    BrandingImageStorage.deleteImageFile(stampUri)
                    stampUri = null
                    Toast.makeText(context, "Stamp removed", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveStampConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRemoveLogoConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveLogoConfirmDialog = false },
            title = { Text("Remove clinic logo?") },
            text = { Text("The logo will be removed from the clinic header.") },
            confirmButton = {
                TextButton(onClick = {
                    showRemoveLogoConfirmDialog = false
                    BrandingImageStorage.deleteImageFile(logoUri)
                    logoUri = null
                    Toast.makeText(context, "Logo removed", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveLogoConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinic Profile & Branding", fontWeight = FontWeight.Bold) },
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
                value = clinicName,
                onValueChange = { clinicName = it },
                label = { Text("Clinic / Hospital Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Clinic Address *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                minLines = 2
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Contact Phone *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Contact Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            // CLINIC LOGO SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Clinic Logo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (logoUri.isNullOrEmpty()) {
                        OutlinedButton(
                            onClick = {
                                pickTarget = "LOGO"
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Clinic Logo")
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp, 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(model = logoUri, contentDescription = "Logo", contentScale = ContentScale.Fit)
                            }
                            OutlinedButton(
                                onClick = {
                                    pickTarget = "LOGO"
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Change")
                            }
                            OutlinedButton(
                                onClick = { showRemoveLogoConfirmDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }

            // ORGANIZATION STAMP SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Organization Stamp",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Upload the clinic, hospital, or organization stamp used on generated certificates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (stampUri.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Approval, contentDescription = null, tint = EmeraldTealPrimary, modifier = Modifier.size(32.dp))
                                Text("No stamp uploaded", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                OutlinedButton(
                                    onClick = {
                                        pickTarget = "STAMP"
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Upload Stamp")
                                }
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = stampUri,
                                    contentDescription = "Organization Stamp Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxHeight()
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        pickTarget = "STAMP"
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change")
                                }

                                OutlinedButton(
                                    onClick = { showRemoveStampConfirmDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }
            }

            Text("Brand Accent Color", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presetColors.forEach { hex ->
                    val color = parseHexColor(hex)
                    val isSelected = primaryColorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(if (isSelected) 3.dp else 0.dp, Color.Black, CircleShape)
                            .clickable { primaryColorHex = hex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (clinicName.isBlank() || address.isBlank() || phone.isBlank()) {
                        Toast.makeText(context, "Please fill required fields (*)", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            val updated = ClinicProfileEntity(
                                id = 1,
                                clinicName = clinicName.trim(),
                                address = address.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                logoUri = logoUri,
                                organizationStampUri = stampUri,
                                primaryColorHex = primaryColorHex,
                                secondaryColorHex = secondaryColorHex
                            )
                            repository.updateClinicProfile(updated)
                            Toast.makeText(context, "Clinic branding updated!", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
            ) {
                Text("Save Branding", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF007A78)
    }
}
