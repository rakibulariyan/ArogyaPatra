package com.aidwellness.arogyapatra.data.repository

import com.aidwellness.arogyapatra.data.local.dao.DoctorClinicDao
import com.aidwellness.arogyapatra.data.local.entity.ClinicProfileEntity
import com.aidwellness.arogyapatra.data.local.entity.DoctorProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProfileRepository(private val dao: DoctorClinicDao) {
    fun getDoctorProfile(): Flow<DoctorProfileEntity?> = dao.getDoctorProfile()
    suspend fun getDoctorProfileDirect(): DoctorProfileEntity? = withContext(Dispatchers.IO) { dao.getDoctorProfileDirect() }
    suspend fun updateDoctorProfile(profile: DoctorProfileEntity): Long = withContext(Dispatchers.IO) { dao.updateDoctorProfile(profile) }

    fun getClinicProfile(): Flow<ClinicProfileEntity?> = dao.getClinicProfile()
    suspend fun getClinicProfileDirect(): ClinicProfileEntity? = withContext(Dispatchers.IO) { dao.getClinicProfileDirect() }
    suspend fun updateClinicProfile(profile: ClinicProfileEntity): Long = withContext(Dispatchers.IO) { dao.updateClinicProfile(profile) }
}
