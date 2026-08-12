package com.aidwellness.arogyapatra.data.local.dao

import androidx.room.*
import com.aidwellness.arogyapatra.data.local.entity.AppSettingsEntity
import com.aidwellness.arogyapatra.data.local.entity.ClinicProfileEntity
import com.aidwellness.arogyapatra.data.local.entity.DoctorProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorClinicDao {
    @Query("SELECT * FROM doctor_profile WHERE id = 1 LIMIT 1")
    fun getDoctorProfile(): Flow<DoctorProfileEntity?>

    @Query("SELECT * FROM doctor_profile WHERE id = 1 LIMIT 1")
    fun getDoctorProfileDirect(): DoctorProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateDoctorProfile(profile: DoctorProfileEntity): Long

    @Query("SELECT * FROM clinic_profile WHERE id = 1 LIMIT 1")
    fun getClinicProfile(): Flow<ClinicProfileEntity?>

    @Query("SELECT * FROM clinic_profile WHERE id = 1 LIMIT 1")
    fun getClinicProfileDirect(): ClinicProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateClinicProfile(profile: ClinicProfileEntity): Long

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getAppSettingsDirect(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateAppSettings(settings: AppSettingsEntity): Long
}
