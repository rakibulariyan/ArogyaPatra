package com.aidwellness.arogyapatra.data.repository

import com.aidwellness.arogyapatra.data.local.dao.DoctorClinicDao
import com.aidwellness.arogyapatra.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SettingsRepository(private val dao: DoctorClinicDao) {
    fun getAppSettings(): Flow<AppSettingsEntity?> = dao.getAppSettings()
    suspend fun getAppSettingsDirect(): AppSettingsEntity? = withContext(Dispatchers.IO) { dao.getAppSettingsDirect() }
    suspend fun updateAppSettings(settings: AppSettingsEntity): Long = withContext(Dispatchers.IO) { dao.updateAppSettings(settings) }
}
