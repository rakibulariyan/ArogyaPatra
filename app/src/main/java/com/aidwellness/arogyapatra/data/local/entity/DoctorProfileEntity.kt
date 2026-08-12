package com.aidwellness.arogyapatra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctor_profile")
data class DoctorProfileEntity(
    @PrimaryKey val id: Long = 1,
    val name: String = "",
    val qualification: String = "",
    val registrationNumber: String = "",
    val specialization: String = "",
    val phone: String = "",
    val email: String = "",
    val signatureUri: String? = null
)
