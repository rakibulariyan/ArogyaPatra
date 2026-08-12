package com.aidwellness.arogyapatra.data.local.dao

import androidx.room.*
import com.aidwellness.arogyapatra.data.local.entity.CertificateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    @Query("SELECT * FROM certificates ORDER BY createdAt DESC")
    fun getAllCertificates(): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM certificates WHERE id = :id LIMIT 1")
    fun getCertificateById(id: Long): CertificateEntity?

    @Query("SELECT * FROM certificates WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getCertificatesForPatient(patientId: Long): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM certificates WHERE certificateType = :type ORDER BY createdAt DESC")
    fun getCertificatesByType(type: String): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM certificates WHERE patientName LIKE '%' || :query || '%' OR certificateNumber LIKE '%' || :query || '%' OR issueDate LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchCertificates(query: String): Flow<List<CertificateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCertificate(certificate: CertificateEntity): Long

    @Update
    fun updateCertificate(certificate: CertificateEntity): Int

    @Delete
    fun deleteCertificate(certificate: CertificateEntity): Int

    @Query("SELECT COUNT(*) FROM certificates")
    fun getTotalCertificateCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM certificates WHERE issueDate = :todayDate")
    fun getCertificatesTodayCount(todayDate: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM certificates WHERE issueDate LIKE '%' || :monthYear || '%'")
    fun getCertificatesThisMonthCount(monthYear: String): Flow<Int>
}
