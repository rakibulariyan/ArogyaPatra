package com.aidwellness.arogyapatra.domain.usecase

import com.aidwellness.arogyapatra.data.local.entity.PatientEntity

class CalculatePatientAgeUseCase {
    fun execute(patient: PatientEntity, asOfDateStr: String? = null): Int {
        return patient.calculateAgeAsOf(asOfDateStr)
    }
}
