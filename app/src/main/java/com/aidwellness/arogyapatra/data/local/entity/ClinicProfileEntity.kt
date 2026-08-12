package com.aidwellness.arogyapatra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clinic_profile")
data class ClinicProfileEntity(
    @PrimaryKey val id: Long = 1,
    val clinicName: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val logoUri: String? = null,
    val organizationStampUri: String? = null,
    val primaryColorHex: String = "#007A78",
    val secondaryColorHex: String = "#00B4D8"
)
