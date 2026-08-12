package com.aidwellness.arogyapatra.domain.usecase

import java.text.SimpleDateFormat
import java.util.*

class CalculateCertificateDurationUseCase {
    fun execute(startStr: String, endStr: String): Int {
        val formatters = listOf(
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        )

        for (sdf in formatters) {
            try {
                val d1 = sdf.parse(startStr)
                val d2 = sdf.parse(endStr)
                if (d1 != null && d2 != null) {
                    val diffMillis = d2.time - d1.time
                    val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1
                    return if (days > 0) days else 1
                }
            } catch (e: Exception) {
                // try next format
            }
        }
        return 1
    }
}
