package com.aidwellness.arogyapatra.data.local.dao

import androidx.room.*
import com.aidwellness.arogyapatra.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY fullName ASC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    fun getPatientById(id: Long): PatientEntity?

    @Query("SELECT * FROM patients WHERE fullName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR patientId LIKE '%' || :query || '%'")
    fun searchPatients(query: String): Flow<List<PatientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPatient(patient: PatientEntity): Long

    @Update
    fun updatePatient(patient: PatientEntity): Int

    @Delete
    fun deletePatient(patient: PatientEntity): Int

    @Query("SELECT COUNT(*) FROM patients")
    fun getPatientCount(): Flow<Int>
}
