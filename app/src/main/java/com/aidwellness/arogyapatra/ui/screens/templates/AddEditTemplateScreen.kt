package com.aidwellness.arogyapatra.ui.screens.templates

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidwellness.arogyapatra.data.local.entity.TemplateEntity
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.domain.usecase.ValidateTemplateUseCase
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTemplateScreen(
    repository: ArogyaRepository,
    templateId: Long = 0L,
    onSaved: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val validateTemplateUseCase = remember { ValidateTemplateUseCase() }

    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Medical Certificate") }
    var bodyTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var isDefault by remember { mutableStateOf(false) }

    var originalTitle by remember { mutableStateOf("") }
    var originalType by remember { mutableStateOf("Medical Certificate") }
    var originalBodyText by remember { mutableStateOf("") }

    var existingTemplate by remember { mutableStateOf<TemplateEntity?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val placeholders = listOf(
        "{{patient_name}}",
        "{{age}}",
        "{{gender}}",
        "{{date}}",
        "{{address}}",
        "{{diagnosis}}",
        "{{doctor_name}}",
        "{{registration_number}}",
        "{{clinic_name}}",
        "{{certificate_number}}",
        "{{start_date}}",
        "{{end_date}}",
        "{{duration}}",
        "{{purpose}}",
        "{{clinical_findings}}",
        "{{remarks}}"
    )

    val categories = listOf(
        "Medical Certificate",
        "Sick Leave",
        "Fitness Certificate",
        "Medical Examination",
        "Return to Work",
        "Custom Certificate"
    )

    LaunchedEffect(templateId) {
        if (templateId != 0L) {
            val t = repository.getTemplateById(templateId)
            existingTemplate = t
            t?.let {
                title = it.title
                type = it.type
                bodyTextFieldValue = TextFieldValue(it.bodyText)
                isDefault = it.isDefault

                originalTitle = it.title
                originalType = it.type
                originalBodyText = it.bodyText
            }
        }
    }

    val isDirty = remember(title, type, bodyTextFieldValue.text) {
        title.trim() != originalTitle.trim() ||
                type != originalType ||
                bodyTextFieldValue.text.trim() != originalBodyText.trim()
    }

    fun handleBack() {
        if (isDirty) {
            showUnsavedDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler {
        handleBack()
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes in this template. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    onBackClick()
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPreviewDialog) {
        val sampleValues = mapOf(
            "patient_name" to "Patient Name",
            "age" to "35",
            "gender" to "Male",
            "date" to "11 Aug 2026",
            "address" to "Patient Address",
            "diagnosis" to "Clinical Diagnosis",
            "doctor_name" to "Dr. Name",
            "registration_number" to "REG-000000",
            "clinic_name" to "Clinic Name",
            "certificate_number" to "MC-2026-000001",
            "start_date" to "11 Aug 2026",
            "end_date" to "14 Aug 2026",
            "duration" to "4",
            "purpose" to "General Assessment",
            "clinical_findings" to "Clinical findings summary",
            "remarks" to "Doctor remarks",
            "restrictions" to "None",
            "fitness_statement" to "Medically fit"
        )
        val previewResult = validateTemplateUseCase.execute(bodyTextFieldValue.text, sampleValues)

        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = EmeraldTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sample Preview", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "This preview uses sample patient data to demonstrate how the template will render.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 280.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = previewResult.processedText.ifBlank { "Template pattern is empty." },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPreviewDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (templateId == 0L) "New Custom Template" else "Edit Template", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (templateId != 0L && existingTemplate?.isDefault == false) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    existingTemplate?.let { repository.deleteTemplate(it) }
                                    Toast.makeText(context, "Template deleted", Toast.LENGTH_SHORT).show()
                                    onSaved()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
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
            // TEMPLATE TITLE FIELD
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                label = { Text("Template Title *") },
                placeholder = { Text("e.g. Executive Health Fitness Template") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                isError = errorMessage != null && title.isBlank()
            )

            // CATEGORY TYPE DROPDOWN
            Text("Category Type *", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Certificate Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    categories.forEach { categoryOption ->
                        DropdownMenuItem(
                            text = { Text(categoryOption) },
                            onClick = {
                                type = categoryOption
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // AVAILABLE FIELDS SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Available Fields",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Tap a field to insert it into the template pattern at the current cursor position.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        placeholders.forEach { tag ->
                            SuggestionChip(
                                onClick = {
                                    val selection = bodyTextFieldValue.selection
                                    val currentText = bodyTextFieldValue.text
                                    val tagToInsert = " $tag "

                                    val newText = if (selection.start >= 0 && selection.start <= currentText.length) {
                                        currentText.substring(0, selection.start) + tagToInsert + currentText.substring(selection.end)
                                    } else {
                                        currentText + tagToInsert
                                    }

                                    val newCursorPos = (selection.start + tagToInsert.length).coerceAtMost(newText.length)
                                    bodyTextFieldValue = TextFieldValue(
                                        text = newText,
                                        selection = TextRange(newCursorPos)
                                    )
                                },
                                label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = EmeraldTealPrimary.copy(alpha = 0.12f),
                                    labelColor = EmeraldTealPrimary
                                ),
                                modifier = Modifier.semantics {
                                    contentDescription = "Insert placeholder $tag into template"
                                }
                            )
                        }
                    }
                }
            }

            // TEMPLATE BODY PATTERN EDITOR
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = bodyTextFieldValue,
                    onValueChange = {
                        bodyTextFieldValue = it
                        errorMessage = null
                    },
                    label = { Text("Template Body Pattern *") },
                    placeholder = { Text("This is to certify that {{patient_name}}, aged {{age}} years...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 8,
                    isError = errorMessage != null && bodyTextFieldValue.text.isBlank()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Use the fields above to automatically insert patient, doctor, and clinic info.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${bodyTextFieldValue.text.length} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // PREVIEW BUTTON
            OutlinedButton(
                onClick = { showPreviewDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Preview Template", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedBody = bodyTextFieldValue.text.trim()

                    if (trimmedTitle.isBlank()) {
                        errorMessage = "Template Title is required."
                    } else if (trimmedBody.isBlank()) {
                        errorMessage = "Template Body Pattern is required."
                    } else {
                        // Validate placeholders using ValidateTemplateUseCase
                        val sampleCheckMap = placeholders.associate {
                            it.replace("{{", "").replace("}}", "") to "sample"
                        }
                        val validation = validateTemplateUseCase.execute(trimmedBody, sampleCheckMap)

                        if (!validation.isValid) {
                            val badTag = validation.unresolvedPlaceholders.firstOrNull()
                            errorMessage = "Unknown or invalid placeholder tag: {{$badTag}}"
                        } else {
                            isSaving = true
                            coroutineScope.launch {
                                val template = TemplateEntity(
                                    id = templateId,
                                    title = trimmedTitle,
                                    type = type,
                                    bodyText = trimmedBody,
                                    isDefault = isDefault
                                )
                                if (templateId == 0L) {
                                    repository.insertTemplate(template)
                                } else {
                                    repository.updateTemplate(template)
                                }
                                isSaving = false
                                Toast.makeText(context, "Template saved successfully!", Toast.LENGTH_SHORT).show()
                                onSaved()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Template", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
