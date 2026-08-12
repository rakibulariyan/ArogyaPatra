package com.aidwellness.arogyapatra.data.repository

import com.aidwellness.arogyapatra.data.local.AppDatabase
import com.aidwellness.arogyapatra.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Composite Repository delegating to specialized domain repositories:
 * PatientRepository, CertificateRepository, TemplateRepository, ProfileRepository, SettingsRepository.
 */
class ArogyaRepository(db: AppDatabase) {

    val patientRepository = PatientRepository(db.patientDao())
    val certificateRepository = CertificateRepository(db.certificateDao())
    val templateRepository = TemplateRepository(db.templateDao())
    val profileRepository = ProfileRepository(db.doctorClinicDao())
    val settingsRepository = SettingsRepository(db.doctorClinicDao())

    // Patients
    fun getAllPatients(): Flow<List<PatientEntity>> = patientRepository.getAllPatients()
    fun searchPatients(query: String): Flow<List<PatientEntity>> = patientRepository.searchPatients(query)
    suspend fun getPatientById(id: Long): PatientEntity? = patientRepository.getPatientById(id)
    suspend fun insertPatient(patient: PatientEntity): Long = patientRepository.insertPatient(patient)
    suspend fun updatePatient(patient: PatientEntity): Int = patientRepository.updatePatient(patient)
    suspend fun deletePatient(patient: PatientEntity): Int = patientRepository.deletePatient(patient)
    fun getPatientCount(): Flow<Int> = patientRepository.getPatientCount()

    // Certificates
    fun getAllCertificates(): Flow<List<CertificateEntity>> = certificateRepository.getAllCertificates()
    fun getCertificatesForPatient(patientId: Long): Flow<List<CertificateEntity>> = certificateRepository.getCertificatesForPatient(patientId)
    fun getCertificatesByType(type: String): Flow<List<CertificateEntity>> = certificateRepository.getCertificatesByType(type)
    fun searchCertificates(query: String): Flow<List<CertificateEntity>> = certificateRepository.searchCertificates(query)
    suspend fun getCertificateById(id: Long): CertificateEntity? = certificateRepository.getCertificateById(id)
    suspend fun insertCertificate(certificate: CertificateEntity): Long = certificateRepository.insertCertificate(certificate)
    suspend fun updateCertificate(certificate: CertificateEntity): Int = certificateRepository.updateCertificate(certificate)
    suspend fun deleteCertificate(certificate: CertificateEntity): Int = certificateRepository.deleteCertificate(certificate)
    fun getCertificatesTodayCount(todayDate: String): Flow<Int> = certificateRepository.getCertificatesTodayCount(todayDate)
    fun getCertificatesThisMonthCount(monthYear: String): Flow<Int> = certificateRepository.getCertificatesThisMonthCount(monthYear)
    fun getTotalCertificateCount(): Flow<Int> = certificateRepository.getTotalCertificateCount()

    // Profiles & App Settings
    fun getDoctorProfile(): Flow<DoctorProfileEntity?> = profileRepository.getDoctorProfile()
    suspend fun getDoctorProfileDirect(): DoctorProfileEntity? = profileRepository.getDoctorProfileDirect()
    suspend fun updateDoctorProfile(profile: DoctorProfileEntity): Long = profileRepository.updateDoctorProfile(profile)

    fun getClinicProfile(): Flow<ClinicProfileEntity?> = profileRepository.getClinicProfile()
    suspend fun getClinicProfileDirect(): ClinicProfileEntity? = profileRepository.getClinicProfileDirect()
    suspend fun updateClinicProfile(profile: ClinicProfileEntity): Long = profileRepository.updateClinicProfile(profile)

    fun getAppSettings(): Flow<AppSettingsEntity?> = settingsRepository.getAppSettings()
    suspend fun getAppSettingsDirect(): AppSettingsEntity? = settingsRepository.getAppSettingsDirect()
    suspend fun updateAppSettings(settings: AppSettingsEntity): Long = settingsRepository.updateAppSettings(settings)

    // Templates
    fun getAllTemplates(): Flow<List<TemplateEntity>> = templateRepository.getAllTemplates()
    suspend fun getTemplateByType(type: String): TemplateEntity? = templateRepository.getTemplateByType(type)
    suspend fun getTemplateById(id: Long): TemplateEntity? = templateRepository.getTemplateById(id)
    suspend fun insertTemplate(template: TemplateEntity): Long = templateRepository.insertTemplate(template)
    suspend fun updateTemplate(template: TemplateEntity): Int = templateRepository.updateTemplate(template)
    suspend fun deleteTemplate(template: TemplateEntity): Int = templateRepository.deleteTemplate(template)
}
