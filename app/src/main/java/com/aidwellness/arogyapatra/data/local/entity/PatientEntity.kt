package com.aidwellness.arogyapatra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "patients",
    indices = [
        Index(value = ["fullName"]),
        Index(value = ["phone"]),
        Index(value = ["patientId"], unique = true)
    ]
)
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: String,
    val fullName: String,
    val dob: String, // Preferred source of truth for age
    val gender: String,
    val phone: String,
    val address: String,
    val bloodGroup: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Dynamically calculates patient age as of a specific date (e.g. certificate issue date).
     */
    fun calculateAgeAsOf(asOfDateStr: String? = null): Int {
        val dateFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        )

        var dobCalendar: Calendar? = null
        for (sdf in dateFormats) {
            try {
                val parsed = sdf.parse(dob)
                if (parsed != null) {
                    dobCalendar = Calendar.getInstance().apply { time = parsed }
                    break
                }
            } catch (e: Exception) {
                // try next format
            }
        }

        if (dobCalendar == null) return 30 // Safe fallback if DOB is invalid

        var targetCalendar = Calendar.getInstance()
        if (!asOfDateStr.isNullOrBlank()) {
            for (sdf in dateFormats) {
                try {
                    val parsed = sdf.parse(asOfDateStr)
                    if (parsed != null) {
                        targetCalendar = Calendar.getInstance().apply { time = parsed }
                        break
                    }
                } catch (e: Exception) {
                    // try next format
                }
            }
        }

        var age = targetCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
        if (targetCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age.coerceAtLeast(0)
    }
}
