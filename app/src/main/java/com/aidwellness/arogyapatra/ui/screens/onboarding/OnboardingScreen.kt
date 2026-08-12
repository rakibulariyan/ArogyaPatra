package com.aidwellness.arogyapatra.ui.screens.onboarding

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aidwellness.arogyapatra.R
import com.aidwellness.arogyapatra.data.local.entity.ClinicProfileEntity
import com.aidwellness.arogyapatra.data.local.entity.DoctorProfileEntity
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.security.SecurityManager
import com.aidwellness.arogyapatra.ui.components.ImageCropDialog
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import com.aidwellness.arogyapatra.util.BrandingImageStorage
import kotlinx.coroutines.launch

private const val TOTAL_STEPS = 5

@Composable
fun OnboardingScreen(
    repository: ArogyaRepository,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(0) }

    // Doctor Profile State
    var doctorName by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var doctorPhone by remember { mutableStateOf("") }
    var doctorEmail by remember { mutableStateOf("") }
    var signatureUri by remember { mutableStateOf<String?>(null) }

    // Clinic Profile State
    var clinicName by remember { mutableStateOf("") }
    var clinicAddress by remember { mutableStateOf("") }
    var clinicPhone by remember { mutableStateOf("") }
    var clinicEmail by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<String?>(null) }
    var stampUri by remember { mutableStateOf<String?>(null) }
    var primaryColorHex by remember { mutableStateOf("#007A78") }

    // Security State
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isBiometricEnabled by remember { mutableStateOf(false) }

    // Image crop state
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var pickTarget by remember { mutableStateOf<String?>(null) } // "SIGNATURE", "LOGO", "STAMP"
    var showCropDialog by remember { mutableStateOf(false) }

    // Validation error state
    var doctorErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var clinicErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var securityError by remember { mutableStateOf<String?>(null) }

    val signaturePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedImageUri = uri
            pickTarget = "SIGNATURE"
            showCropDialog = true
        }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedImageUri = uri
            pickTarget = "LOGO"
            showCropDialog = true
        }
    }

    val stampPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedImageUri = uri
            pickTarget = "STAMP"
            showCropDialog = true
        }
    }

    // Crop Dialog
    if (showCropDialog && pickedImageUri != null) {
        val title = when (pickTarget) {
            "SIGNATURE" -> "Crop Doctor Signature"
            "LOGO" -> "Crop Clinic Logo"
            else -> "Crop Organization Stamp"
        }
        val aspect = when (pickTarget) {
            "SIGNATURE" -> 3f / 1f
            "LOGO" -> 2f / 1f
            else -> 1f / 1f
        }

        ImageCropDialog(
            imageUri = pickedImageUri!!,
            title = title,
            aspectRatio = aspect,
            onCropApplied = { croppedBitmap: Bitmap ->
                showCropDialog = false
                when (pickTarget) {
                    "SIGNATURE" -> {
                        val saved = BrandingImageStorage.saveDoctorSignature(context, croppedBitmap, signatureUri)
                        if (saved != null) signatureUri = saved
                    }
                    "LOGO" -> {
                        val saved = BrandingImageStorage.saveClinicLogo(context, croppedBitmap, logoUri)
                        if (saved != null) logoUri = saved
                    }
                    "STAMP" -> {
                        val saved = BrandingImageStorage.saveOrganizationStamp(context, croppedBitmap, stampUri)
                        if (saved != null) stampUri = saved
                    }
                }
            },
            onCancel = { showCropDialog = false }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Progress Indicator
            if (currentStep > 0) {
                OnboardingProgressBar(currentStep = currentStep, totalSteps = TOTAL_STEPS - 1)
            }

            // Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f),
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(onGetStarted = { currentStep = 1 })

                    1 -> DoctorProfileStep(
                        name = doctorName, onNameChange = { doctorName = it },
                        qualification = qualification, onQualificationChange = { qualification = it },
                        regNumber = regNumber, onRegNumberChange = { regNumber = it },
                        specialization = specialization, onSpecializationChange = { specialization = it },
                        phone = doctorPhone, onPhoneChange = { doctorPhone = it },
                        email = doctorEmail, onEmailChange = { doctorEmail = it },
                        signatureUri = signatureUri,
                        onUploadSignature = {
                            signaturePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        errors = doctorErrors,
                        onNext = {
                            val errors = mutableMapOf<String, String>()
                            if (doctorName.isBlank()) errors["name"] = "Doctor name is required"
                            if (qualification.isBlank()) errors["qualification"] = "Qualification is required"
                            if (regNumber.isBlank()) errors["regNumber"] = "Registration number is required"
                            if (doctorEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(doctorEmail).matches()) {
                                errors["email"] = "Invalid email format"
                            }
                            doctorErrors = errors
                            if (errors.isEmpty()) {
                                coroutineScope.launch {
                                    repository.updateDoctorProfile(
                                        DoctorProfileEntity(
                                            name = doctorName.trim(),
                                            qualification = qualification.trim(),
                                            registrationNumber = regNumber.trim(),
                                            specialization = specialization.trim(),
                                            phone = doctorPhone.trim(),
                                            email = doctorEmail.trim(),
                                            signatureUri = signatureUri
                                        )
                                    )
                                    currentStep = 2
                                }
                            }
                        },
                        onBack = { currentStep = 0 }
                    )

                    2 -> ClinicProfileStep(
                        clinicName = clinicName, onClinicNameChange = { clinicName = it },
                        address = clinicAddress, onAddressChange = { clinicAddress = it },
                        phone = clinicPhone, onPhoneChange = { clinicPhone = it },
                        email = clinicEmail, onEmailChange = { clinicEmail = it },
                        logoUri = logoUri, stampUri = stampUri,
                        primaryColorHex = primaryColorHex,
                        onColorChange = { primaryColorHex = it },
                        onUploadLogo = {
                            logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onUploadStamp = {
                            stampPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        errors = clinicErrors,
                        onNext = {
                            val errors = mutableMapOf<String, String>()
                            if (clinicName.isBlank()) errors["clinicName"] = "Clinic name is required"
                            if (clinicAddress.isBlank()) errors["address"] = "Clinic address is required"
                            if (clinicPhone.isBlank()) errors["phone"] = "Contact phone is required"
                            clinicErrors = errors
                            if (errors.isEmpty()) {
                                coroutineScope.launch {
                                    repository.updateClinicProfile(
                                        ClinicProfileEntity(
                                            clinicName = clinicName.trim(),
                                            address = clinicAddress.trim(),
                                            phone = clinicPhone.trim(),
                                            email = clinicEmail.trim(),
                                            logoUri = logoUri,
                                            organizationStampUri = stampUri,
                                            primaryColorHex = primaryColorHex,
                                            secondaryColorHex = "#00B4D8"
                                        )
                                    )
                                    currentStep = 3
                                }
                            }
                        },
                        onBack = { currentStep = 1 }
                    )

                    3 -> SecurityStep(
                        pin = pin, onPinChange = { if (it.length <= 4) pin = it },
                        confirmPin = confirmPin, onConfirmPinChange = { if (it.length <= 4) confirmPin = it },
                        isBiometricEnabled = isBiometricEnabled,
                        onBiometricChange = { isBiometricEnabled = it },
                        isBiometricAvailable = SecurityManager.canAuthenticateWithBiometrics(context),
                        error = securityError,
                        onNext = {
                            if (pin.length < 4) {
                                securityError = "PIN must be exactly 4 digits"
                            } else if (pin != confirmPin) {
                                securityError = "PINs do not match"
                            } else {
                                securityError = null
                                coroutineScope.launch {
                                    val settings = repository.getAppSettingsDirect()
                                        ?: com.aidwellness.arogyapatra.data.local.entity.AppSettingsEntity()
                                    val updated = settings.copy(
                                        pinHash = SecurityManager.hashPin(pin),
                                        isBiometricEnabled = isBiometricEnabled
                                    )
                                    repository.updateAppSettings(updated)
                                    currentStep = 4
                                }
                            }
                        },
                        onBack = { currentStep = 2 }
                    )

                    4 -> SetupCompleteStep(
                        doctorName = doctorName,
                        clinicName = clinicName,
                        hasSignature = !signatureUri.isNullOrBlank(),
                        hasLogo = !logoUri.isNullOrBlank(),
                        hasStamp = !stampUri.isNullOrBlank(),
                        hasSecurity = pin.length == 4,
                        hasBiometric = isBiometricEnabled,
                        onGoToDashboard = {
                            coroutineScope.launch {
                                val settings = repository.getAppSettingsDirect()
                                    ?: com.aidwellness.arogyapatra.data.local.entity.AppSettingsEntity()
                                repository.updateAppSettings(settings.copy(isOnboardingCompleted = true))
                                onOnboardingComplete()
                            }
                        }
                    )
                }
            }
        }
    }
}

// ─── PROGRESS BAR ────────────────────────────────────────────────────────────

@Composable
private fun OnboardingProgressBar(currentStep: Int, totalSteps: Int) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = EmeraldTealPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = when (currentStep) {
                    1 -> "Doctor Profile"
                    2 -> "Clinic Profile"
                    3 -> "Security"
                    4 -> "Complete"
                    else -> ""
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = EmeraldTealPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

// ─── STEP 0: WELCOME ────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.drawable.ic_arogyapatra_logo),
            contentDescription = "ArogyaPatra Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ArogyaPatra",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldTealPrimary,
                fontSize = 32.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Medical Certificate Management",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Create, manage, preview, print, and share professional medical certificates securely on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
        ) {
            Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Your patient and certificate data is stored locally on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "by AidWellness",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

// ─── STEP 1: DOCTOR PROFILE ─────────────────────────────────────────────────

@Composable
private fun DoctorProfileStep(
    name: String, onNameChange: (String) -> Unit,
    qualification: String, onQualificationChange: (String) -> Unit,
    regNumber: String, onRegNumberChange: (String) -> Unit,
    specialization: String, onSpecializationChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    signatureUri: String?,
    onUploadSignature: () -> Unit,
    errors: Map<String, String>,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Doctor Profile",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Enter the details that will appear on your medical certificates.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        OnboardingTextField(value = name, onValueChange = onNameChange, label = "Doctor Full Name *", error = errors["name"])
        OnboardingTextField(value = qualification, onValueChange = onQualificationChange, label = "Qualification (e.g. MBBS, MD) *", error = errors["qualification"])
        OnboardingTextField(value = regNumber, onValueChange = onRegNumberChange, label = "Medical Registration Number *", error = errors["regNumber"])
        OnboardingTextField(value = specialization, onValueChange = onSpecializationChange, label = "Specialization")
        OnboardingTextField(value = phone, onValueChange = onPhoneChange, label = "Phone Number", keyboardType = KeyboardType.Phone)
        OnboardingTextField(value = email, onValueChange = onEmailChange, label = "Email Address", error = errors["email"], keyboardType = KeyboardType.Email)

        // Signature Upload Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Doctor Signature", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text("Optional. Upload your handwritten signature for medical certificates.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (signatureUri.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = onUploadSignature,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Signature")
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(model = signatureUri, contentDescription = "Signature Preview", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                    }
                    OutlinedButton(onClick = onUploadSignature, shape = RoundedCornerShape(8.dp)) {
                        Text("Change Signature")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
            ) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── STEP 2: CLINIC PROFILE ─────────────────────────────────────────────────

@Composable
private fun ClinicProfileStep(
    clinicName: String, onClinicNameChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    logoUri: String?, stampUri: String?,
    primaryColorHex: String,
    onColorChange: (String) -> Unit,
    onUploadLogo: () -> Unit,
    onUploadStamp: () -> Unit,
    errors: Map<String, String>,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val presetColors = listOf("#007A78", "#1E40AF", "#047857", "#B91C1C", "#6D28D9", "#0F172A")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Clinic Profile & Branding", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Text("Configure your clinic details and branding for certificates.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(4.dp))

        OnboardingTextField(value = clinicName, onValueChange = onClinicNameChange, label = "Clinic / Hospital Name *", error = errors["clinicName"])
        OnboardingTextField(value = address, onValueChange = onAddressChange, label = "Clinic Address *", error = errors["address"], minLines = 2)
        OnboardingTextField(value = phone, onValueChange = onPhoneChange, label = "Contact Phone *", error = errors["phone"], keyboardType = KeyboardType.Phone)
        OnboardingTextField(value = email, onValueChange = onEmailChange, label = "Contact Email", keyboardType = KeyboardType.Email)

        // Logo Upload
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Clinic Logo", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                if (logoUri.isNullOrBlank()) {
                    OutlinedButton(onClick = onUploadLogo, shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Logo (Optional)")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(60.dp, 40.dp).clip(RoundedCornerShape(6.dp)).background(Color.White).border(1.dp, Color.LightGray, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(model = logoUri, contentDescription = "Logo", contentScale = ContentScale.Fit)
                        }
                        OutlinedButton(onClick = onUploadLogo, shape = RoundedCornerShape(8.dp)) { Text("Change") }
                    }
                }
            }
        }

        // Stamp Upload
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Organization Stamp", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                if (stampUri.isNullOrBlank()) {
                    OutlinedButton(onClick = onUploadStamp, shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Approval, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Stamp (Optional)")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).background(Color.White).border(1.dp, Color.LightGray, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(model = stampUri, contentDescription = "Stamp", contentScale = ContentScale.Fit)
                        }
                        OutlinedButton(onClick = onUploadStamp, shape = RoundedCornerShape(8.dp)) { Text("Change") }
                    }
                }
            }
        }

        // Brand Accent Color
        Text("Brand Accent Color", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            presetColors.forEach { hex ->
                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { EmeraldTealPrimary }
                val isSelected = primaryColorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(if (isSelected) 3.dp else 0.dp, Color.Black, CircleShape)
                        .clickable { onColorChange(hex) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Back") }
            Button(onClick = onNext, modifier = Modifier.weight(2f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)) { Text("Continue", fontWeight = FontWeight.Bold) }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── STEP 3: SECURITY ───────────────────────────────────────────────────────

@Composable
private fun SecurityStep(
    pin: String, onPinChange: (String) -> Unit,
    confirmPin: String, onConfirmPinChange: (String) -> Unit,
    isBiometricEnabled: Boolean, onBiometricChange: (Boolean) -> Unit,
    isBiometricAvailable: Boolean,
    error: String?,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Protect Your Data", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Text(
            "ArogyaPatra stores patient and certificate information locally on this device. Set a PIN to protect access.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Create App Lock PIN", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                OutlinedTextField(
                    value = pin,
                    onValueChange = onPinChange,
                    label = { Text("4-Digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    isError = error != null
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = onConfirmPinChange,
                    label = { Text("Confirm PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    isError = error != null
                )

                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Biometric Toggle
        if (isBiometricAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Use Biometric Unlock", fontWeight = FontWeight.SemiBold)
                        Text("Fingerprint or face recognition", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isBiometricEnabled, onCheckedChange = onBiometricChange)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Back") }
            Button(onClick = onNext, modifier = Modifier.weight(2f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)) { Text("Set PIN & Continue", fontWeight = FontWeight.Bold) }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── STEP 4: SETUP COMPLETE ─────────────────────────────────────────────────

@Composable
private fun SetupCompleteStep(
    doctorName: String,
    clinicName: String,
    hasSignature: Boolean,
    hasLogo: Boolean,
    hasStamp: Boolean,
    hasSecurity: Boolean,
    hasBiometric: Boolean,
    onGoToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldTealPrimary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ArogyaPatra is ready to use.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Setup Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryRow(label = "Doctor", value = "Dr. $doctorName", checked = true)
                SummaryRow(label = "Clinic", value = clinicName, checked = true)
                SummaryRow(label = "Signature", value = if (hasSignature) "Configured" else "Not configured", checked = hasSignature)
                SummaryRow(label = "Logo", value = if (hasLogo) "Configured" else "Not configured", checked = hasLogo)
                SummaryRow(label = "Stamp", value = if (hasStamp) "Configured" else "Not configured", checked = hasStamp)
                SummaryRow(label = "Security", value = buildString {
                    if (hasSecurity) append("PIN enabled")
                    if (hasBiometric) append(" + Biometric")
                }, checked = hasSecurity)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGoToDashboard,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
        ) {
            Text("Go to Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, checked: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (checked) EmeraldTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.width(80.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

// ─── REUSABLE COMPONENTS ────────────────────────────────────────────────────

@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = minLines == 1,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = error != null
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
