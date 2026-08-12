package com.aidwellness.arogyapatra.data.repository

import com.aidwellness.arogyapatra.data.local.dao.PatientDao
import com.aidwellness.arogyapatra.data.local.entity.PatientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PatientRepository(private val patientDao: PatientDao) {
    fun getAllPatients(): Flow<List<PatientEntity>> = patientDao.getAllPatients()
    fun searchPatients(query: String): Flow<List<PatientEntity>> = patientDao.searchPatients(query)
    suspend fun getPatientById(id: Long): PatientEntity? = withContext(Dispatchers.IO) { patientDao.getPatientById(id) }
    suspend fun insertPatient(patient: PatientEntity): Long = withContext(Dispatchers.IO) { patientDao.insertPatient(patient) }
    suspend fun updatePatient(patient: PatientEntity): Int = withContext(Dispatchers.IO) { patientDao.updatePatient(patient) }
    suspend fun deletePatient(patient: PatientEntity): Int = withContext(Dispatchers.IO) { patientDao.deletePatient(patient) }
    fun getPatientCount(): Flow<Int> = patientDao.getPatientCount()
}
