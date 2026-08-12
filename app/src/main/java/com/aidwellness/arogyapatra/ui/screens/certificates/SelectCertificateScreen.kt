package com.aidwellness.arogyapatra.ui.screens.certificates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary

data class CertificateTypeOption(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCertificateScreen(
    onCertificateSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val certificateOptions = listOf(
        CertificateTypeOption(
            title = "Medical Certificate",
            description = "General-purpose medical certificate for treatment and clinical care.",
            icon = Icons.Default.MedicalServices
        ),
        CertificateTypeOption(
            title = "Sick Leave",
            description = "Certify illness and specify required rest period with auto-calculated duration.",
            icon = Icons.Default.EventBusy
        ),
        CertificateTypeOption(
            title = "Fitness Certificate",
            description = "Certify patient's medical fitness for work, school, or activities.",
            icon = Icons.Default.FitnessCenter
        ),
        CertificateTypeOption(
            title = "Medical Examination",
            description = "Detailed certificate documenting clinical findings and exam purpose.",
            icon = Icons.Default.AssignmentInd
        ),
        CertificateTypeOption(
            title = "Return to Work",
            description = "Certify that patient is fit to resume work following prior leave.",
            icon = Icons.Default.WorkHistory
        ),
        CertificateTypeOption(
            title = "Custom Certificate",
            description = "Create a custom medical certificate with personalized placeholders.",
            icon = Icons.Default.EditNote
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Certificate Type", fontWeight = FontWeight.Bold) },
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
        ) {
            Text(
                text = "Step 1 of 3 — Choose Template",
                style = MaterialTheme.typography.labelLarge.copy(color = EmeraldTealPrimary, fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(certificateOptions) { option ->
                    CertificateTypeCard(option = option, onClick = { onCertificateSelected(option.title) })
                }
            }
        }
    }
}

@Composable
private fun CertificateTypeCard(
    option: CertificateTypeOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = EmeraldTealPrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.title,
                        tint = EmeraldTealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
