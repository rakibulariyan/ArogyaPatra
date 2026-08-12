package com.aidwellness.arogyapatra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Long = 1,
    val pinHash: String? = null,
    val isBiometricEnabled: Boolean = false,
    val certificatePrefix: String = "MC-2026-",
    val certificateCounter: Int = 101,
    val defaultLanguage: String = "English",
    val themeMode: String = "System",
    val isOnboardingCompleted: Boolean = false
)
