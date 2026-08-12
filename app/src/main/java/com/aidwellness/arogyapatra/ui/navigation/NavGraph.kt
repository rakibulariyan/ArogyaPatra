package com.aidwellness.arogyapatra.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.aidwellness.arogyapatra.R
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.ui.screens.auth.LockScreen
import com.aidwellness.arogyapatra.ui.screens.certificates.*
import com.aidwellness.arogyapatra.ui.screens.home.HomeScreen
import com.aidwellness.arogyapatra.ui.screens.onboarding.OnboardingScreen
import com.aidwellness.arogyapatra.ui.screens.patients.*
import com.aidwellness.arogyapatra.ui.screens.settings.*
import com.aidwellness.arogyapatra.ui.screens.templates.*
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary

/**
 * Startup state to avoid UI flashes during Room initialization.
 */
enum class StartupState {
    Initializing,
    Onboarding,
    Locked,
    Ready
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: ArogyaRepository,
    startupState: StartupState,
    onUnlockApp: () -> Unit,
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Certificates,
        Screen.Patients,
        Screen.Templates,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = startupState == StartupState.Ready &&
            bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldTealPrimary,
                                selectedTextColor = EmeraldTealPrimary,
                                indicatorColor = EmeraldTealPrimary.copy(alpha = 0.15f)
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        // Determine start destination based on startup state
        val startDestination = when (startupState) {
            StartupState.Initializing -> Screen.Splash.route
            StartupState.Onboarding -> Screen.Onboarding.route
            StartupState.Locked -> Screen.Lock.route
            StartupState.Ready -> Screen.Home.route
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash (shown while initializing)
            composable(Screen.Splash.route) {
                SplashContent()
            }

            // Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    repository = repository,
                    onOnboardingComplete = {
                        onOnboardingComplete()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Lock Screen
            composable(Screen.Lock.route) {
                val settingsState by repository.getAppSettings().collectAsState(initial = null)
                LockScreen(
                    storedPinHash = settingsState?.pinHash,
                    isBiometricEnabled = settingsState?.isBiometricEnabled == true,
                    onUnlocked = {
                        onUnlockApp()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Lock.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    repository = repository,
                    onNewCertificateClick = { navController.navigate(Screen.SelectCertificate.route) },
                    onCreatePatientClick = { navController.navigate(Screen.AddEditPatient.createRoute()) },
                    onTemplatesClick = { navController.navigate(Screen.Templates.route) },
                    onCertificatePreviewClick = { certId -> navController.navigate(Screen.CertificatePreview.createRoute(certId)) }
                )
            }

            composable(Screen.Certificates.route) {
                CertificateHistoryScreen(
                    repository = repository,
                    onNewCertificateClick = { navController.navigate(Screen.SelectCertificate.route) },
                    onCertificateClick = { certId -> navController.navigate(Screen.CertificatePreview.createRoute(certId)) }
                )
            }

            composable(Screen.SelectCertificate.route) {
                SelectCertificateScreen(
                    onCertificateSelected = { type ->
                        navController.navigate(Screen.PatientSelection.createRoute(type))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PatientSelection.route,
                arguments = listOf(navArgument("certType") { type = NavType.StringType })
            ) { backStackEntry ->
                val certType = UriCodec.decode(backStackEntry.arguments?.getString("certType") ?: "Medical Certificate")
                PatientSelectionScreen(
                    repository = repository,
                    certType = certType,
                    onPatientSelected = { patientId ->
                        navController.navigate(Screen.CertificateForm.createRoute(certType, patientId))
                    },
                    onAddNewPatientClick = {
                        navController.navigate(Screen.AddEditPatient.createRoute())
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CertificateForm.route,
                arguments = listOf(
                    navArgument("certType") { type = NavType.StringType },
                    navArgument("patientId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val certType = UriCodec.decode(backStackEntry.arguments?.getString("certType") ?: "Medical Certificate")
                val patientId = backStackEntry.arguments?.getLong("patientId") ?: 0L
                CertificateFormScreen(
                    repository = repository,
                    certType = certType,
                    patientId = patientId,
                    onGeneratePreview = { certId ->
                        navController.navigate(Screen.CertificatePreview.createRoute(certId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CertificatePreview.route,
                arguments = listOf(navArgument("certId") { type = NavType.LongType })
            ) { backStackEntry ->
                val certId = backStackEntry.arguments?.getLong("certId") ?: 0L
                CertificatePreviewScreen(
                    repository = repository,
                    certId = certId,
                    onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Patients.route) {
                PatientListScreen(
                    repository = repository,
                    onPatientClick = { patientId -> navController.navigate(Screen.PatientDetail.createRoute(patientId)) },
                    onAddPatientClick = { navController.navigate(Screen.AddEditPatient.createRoute()) }
                )
            }

            composable(
                route = Screen.AddEditPatient.route,
                arguments = listOf(navArgument("patientId") { type = NavType.LongType })
            ) { backStackEntry ->
                val patientId = backStackEntry.arguments?.getLong("patientId") ?: 0L
                AddEditPatientScreen(
                    repository = repository,
                    patientId = patientId,
                    onSaved = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PatientDetail.route,
                arguments = listOf(navArgument("patientId") { type = NavType.LongType })
            ) { backStackEntry ->
                val patientId = backStackEntry.arguments?.getLong("patientId") ?: 0L
                PatientDetailScreen(
                    repository = repository,
                    patientId = patientId,
                    onCertificateClick = { certId -> navController.navigate(Screen.CertificatePreview.createRoute(certId)) },
                    onEditPatientClick = { navController.navigate(Screen.AddEditPatient.createRoute(patientId)) },
                    onNewCertClick = { navController.navigate(Screen.SelectCertificate.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Templates.route) {
                TemplateListScreen(
                    repository = repository,
                    onAddTemplateClick = { navController.navigate(Screen.AddEditTemplate.createRoute()) },
                    onTemplateClick = { templateId -> navController.navigate(Screen.AddEditTemplate.createRoute(templateId)) }
                )
            }

            composable(
                route = Screen.AddEditTemplate.route,
                arguments = listOf(navArgument("templateId") { type = NavType.LongType })
            ) { backStackEntry ->
                val templateId = backStackEntry.arguments?.getLong("templateId") ?: 0L
                AddEditTemplateScreen(
                    repository = repository,
                    templateId = templateId,
                    onSaved = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository,
                    onDoctorProfileClick = { navController.navigate(Screen.DoctorProfile.route) },
                    onClinicProfileClick = { navController.navigate(Screen.ClinicProfile.route) },
                    onSecurityClick = { navController.navigate(Screen.SecuritySettings.route) },
                    onBackupRestoreClick = { navController.navigate(Screen.BackupRestore.route) },
                    onPrivacyPolicyClick = { navController.navigate(Screen.PrivacyPolicy.route) }
                )
            }

            composable(Screen.DoctorProfile.route) {
                DoctorProfileScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.ClinicProfile.route) {
                ClinicProfileScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.SecuritySettings.route) {
                SecuritySettingsScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.BackupRestore.route) {
                BackupRestoreScreen(
                    repository = repository,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Branded splash content shown while Room settings are loading.
 * No artificial delay — displayed only during genuine initialization.
 */
@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arogyapatra_logo),
                contentDescription = "ArogyaPatra",
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ArogyaPatra",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldTealPrimary,
                    fontSize = 28.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Medical Certificate Management",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            CircularProgressIndicator(
                color = EmeraldTealPrimary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
            )
        }

        Text(
            text = "by AidWellness",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
