package com.aidwellness.arogyapatra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "certificates",
    indices = [
        Index(value = ["certificateNumber"], unique = true),
        Index(value = ["patientId"]),
        Index(value = ["issueDate"]),
        Index(value = ["certificateType"])
    ]
)
data class CertificateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val certificateNumber: String,
    val patientId: Long,
    val patientName: String,
    val patientAge: Int,
    val patientGender: String,
    val certificateType: String,
    val issueDate: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val durationDays: Int? = null,
    val diagnosis: String? = null,
    val clinicalFindings: String? = null,
    val purpose: String? = null,
    val fitnessStatement: String? = null,
    val restrictions: String? = null,
    val doctorRemarks: String? = null,
    val customContent: String? = null,
    val pdfPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
