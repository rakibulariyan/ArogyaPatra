package com.aidwellness.arogyapatra

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.aidwellness.arogyapatra.ads.AdManager
import com.aidwellness.arogyapatra.ads.ConsentManager
import com.aidwellness.arogyapatra.data.local.AppDatabase
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.ui.navigation.NavGraph
import com.aidwellness.arogyapatra.ui.navigation.StartupState
import com.aidwellness.arogyapatra.ui.theme.ArogyaPatraTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = ArogyaRepository(db)

        // Initialize AdMob & Request UMP Consent at App Startup
        ConsentManager.gatherConsent(this) { canRequest ->
            if (canRequest) {
                AdManager.initialize(applicationContext)
            }
        }

        setContent {
            val appSettings by repository.getAppSettings().collectAsState(initial = null)

            // Derive startup state from persisted settings
            val startupState by remember {
                derivedStateOf {
                    val settings = appSettings
                    when {
                        settings == null -> StartupState.Initializing
                        !settings.isOnboardingCompleted -> StartupState.Onboarding
                        !settings.pinHash.isNullOrEmpty() -> StartupState.Locked
                        else -> StartupState.Ready
                    }
                }
            }

            // Track unlock state separately so it can be mutated by the lock screen
            var hasUnlocked by remember { mutableStateOf(false) }

            val effectiveStartupState = when {
                startupState == StartupState.Locked && hasUnlocked -> StartupState.Ready
                else -> startupState
            }

            ArogyaPatraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // When startup state changes from Initializing, navigate to the correct destination
                    LaunchedEffect(effectiveStartupState) {
                        if (effectiveStartupState != StartupState.Initializing) {
                            val target = when (effectiveStartupState) {
                                StartupState.Onboarding -> "onboarding"
                                StartupState.Locked -> "lock"
                                StartupState.Ready -> "home"
                                else -> return@LaunchedEffect
                            }
                            // Only navigate if we're not already there
                            val currentRoute = navController.currentDestination?.route
                            if (currentRoute != target && currentRoute == "splash") {
                                navController.navigate(target) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                    }

                    NavGraph(
                        navController = navController,
                        repository = repository,
                        startupState = effectiveStartupState,
                        onUnlockApp = { hasUnlocked = true },
                        onOnboardingComplete = {
                            // State will automatically transition via appSettings flow
                        }
                    )
                }
            }
        }
    }
}