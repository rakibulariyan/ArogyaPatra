package com.aidwellness.arogyapatra.data.repository

import com.aidwellness.arogyapatra.data.local.dao.CertificateDao
import com.aidwellness.arogyapatra.data.local.entity.CertificateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CertificateRepository(private val certificateDao: CertificateDao) {
    fun getAllCertificates(): Flow<List<CertificateEntity>> = certificateDao.getAllCertificates()
    fun getCertificatesForPatient(patientId: Long): Flow<List<CertificateEntity>> = certificateDao.getCertificatesForPatient(patientId)
    fun getCertificatesByType(type: String): Flow<List<CertificateEntity>> = certificateDao.getCertificatesByType(type)
    fun searchCertificates(query: String): Flow<List<CertificateEntity>> = certificateDao.searchCertificates(query)
    suspend fun getCertificateById(id: Long): CertificateEntity? = withContext(Dispatchers.IO) { certificateDao.getCertificateById(id) }
    suspend fun insertCertificate(certificate: CertificateEntity): Long = withContext(Dispatchers.IO) { certificateDao.insertCertificate(certificate) }
    suspend fun updateCertificate(certificate: CertificateEntity): Int = withContext(Dispatchers.IO) { certificateDao.updateCertificate(certificate) }
    suspend fun deleteCertificate(certificate: CertificateEntity): Int = withContext(Dispatchers.IO) { certificateDao.deleteCertificate(certificate) }
    fun getCertificatesTodayCount(todayDate: String): Flow<Int> = certificateDao.getCertificatesTodayCount(todayDate)
    fun getCertificatesThisMonthCount(monthYear: String): Flow<Int> = certificateDao.getCertificatesThisMonthCount(monthYear)
    fun getTotalCertificateCount(): Flow<Int> = certificateDao.getTotalCertificateCount()
}
