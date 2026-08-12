package com.aidwellness.arogyapatra.domain.usecase

import com.aidwellness.arogyapatra.data.local.entity.AppSettingsEntity
import java.util.Locale

class GenerateCertificateNumberUseCase {
    fun execute(appSettings: AppSettingsEntity?): String {
        val prefix = appSettings?.certificatePrefix ?: "MC-2026-"
        val counter = appSettings?.certificateCounter ?: 101
        return "$prefix${String.format(Locale.US, "%06d", counter)}"
    }
}
