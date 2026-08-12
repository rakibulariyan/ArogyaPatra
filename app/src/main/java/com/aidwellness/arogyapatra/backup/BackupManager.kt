package com.aidwellness.arogyapatra.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.aidwellness.arogyapatra.data.local.AppDatabase
import com.aidwellness.arogyapatra.data.local.entity.*
import com.aidwellness.arogyapatra.data.repository.ArogyaRepository
import com.aidwellness.arogyapatra.security.CryptoUtils
import com.aidwellness.arogyapatra.util.AppLogger
import com.aidwellness.arogyapatra.util.BrandingImageStorage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.firstOrNull
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Base64

data class BackupEnvelope(
    val schemaVersion: Int = 1,
    val appName: String = "ArogyaPatra",
    val exportedAt: Long = System.currentTimeMillis(),
    val ivHex: String,
    val saltHex: String,
    val encryptedPayload: String
)

data class BackupPayload(
    val doctorProfile: DoctorProfileEntity?,
    val clinicProfile: ClinicProfileEntity?,
    val patients: List<PatientEntity>,
    val certificates: List<CertificateEntity>,
    val templates: List<TemplateEntity>,
    val signatureBase64: String? = null,
    val stampBase64: String? = null
)

object BackupManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportEncryptedBackupToUri(
        context: Context,
        uri: Uri,
        repository: ArogyaRepository,
        passphrase: String = "ArogyaPatraSecurePassphrase2026"
    ): Boolean {
        return try {
            val doctor = repository.getDoctorProfile().firstOrNull()
            val clinic = repository.getClinicProfile().firstOrNull()
            val patients = repository.getAllPatients().firstOrNull() ?: emptyList()
            val certificates = repository.getAllCertificates().firstOrNull() ?: emptyList()
            val templates = repository.getAllTemplates().firstOrNull() ?: emptyList()

            // Encode signature & stamp images to Base64 for cross-device backup
            val signatureBase64 = doctor?.signatureUri?.let { readImageAsBase64(it) }
            val stampBase64 = clinic?.organizationStampUri?.let { readImageAsBase64(it) }

            val payload = BackupPayload(
                doctorProfile = doctor,
                clinicProfile = clinic,
                patients = patients,
                certificates = certificates,
                templates = templates,
                signatureBase64 = signatureBase64,
                stampBase64 = stampBase64
            )

            val jsonPayload = gson.toJson(payload)
            val encrypted = CryptoUtils.encrypt(jsonPayload, passphrase)

            val envelope = BackupEnvelope(
                schemaVersion = 1,
                exportedAt = System.currentTimeMillis(),
                ivHex = encrypted.ivHex,
                saltHex = encrypted.saltHex,
                encryptedPayload = encrypted.ciphertextBase64
            )

            val envelopeJson = gson.toJson(envelope)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(envelopeJson.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            AppLogger.e("Export backup failed", e)
            false
        }
    }

    suspend fun importEncryptedBackupFromUri(
        context: Context,
        uri: Uri,
        database: AppDatabase,
        repository: ArogyaRepository,
        passphrase: String = "ArogyaPatraSecurePassphrase2026"
    ): Boolean {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
            } ?: return false

            val envelope = gson.fromJson(jsonString, BackupEnvelope::class.java) ?: return false
            val decryptedJson = CryptoUtils.decrypt(
                ciphertextBase64 = envelope.encryptedPayload,
                passphrase = passphrase,
                ivHex = envelope.ivHex,
                saltHex = envelope.saltHex
            )

            val payload = gson.fromJson(decryptedJson, BackupPayload::class.java) ?: return false

            // Restore branding images to app-private storage if available
            var restoredSignatureUri = payload.doctorProfile?.signatureUri
            if (!payload.signatureBase64.isNullOrEmpty()) {
                val restoredUri = restoreBase64Image(context, payload.signatureBase64, "branding/signatures", "doctor_signature")
                if (restoredUri != null) restoredSignatureUri = restoredUri
            }

            var restoredStampUri = payload.clinicProfile?.organizationStampUri
            if (!payload.stampBase64.isNullOrEmpty()) {
                val restoredUri = restoreBase64Image(context, payload.stampBase64, "branding/stamps", "clinic_stamp")
                if (restoredUri != null) restoredStampUri = restoredUri
            }

            val updatedDoctor = payload.doctorProfile?.copy(signatureUri = restoredSignatureUri)
            val updatedClinic = payload.clinicProfile?.copy(organizationStampUri = restoredStampUri)

            // Perform atomic Room transaction
            database.withTransaction {
                updatedDoctor?.let { repository.updateDoctorProfile(it) }
                updatedClinic?.let { repository.updateClinicProfile(it) }

                payload.patients.forEach { repository.insertPatient(it) }
                payload.certificates.forEach { repository.insertCertificate(it) }
                payload.templates.forEach { repository.insertTemplate(it) }
            }

            true
        } catch (e: Exception) {
            AppLogger.e("Import backup failed", e)
            false
        }
    }

    private fun readImageAsBase64(fileUriStr: String): String? {
        return try {
            val uri = Uri.parse(fileUriStr)
            val path = uri.path ?: return null
            val file = File(path)
            if (!file.exists()) return null
            val bytes = file.readBytes()
            Base64.getEncoder().encodeToString(bytes)
        } catch (e: Exception) {
            AppLogger.e("Failed to read image as Base64", e)
            null
        }
    }

    private fun restoreBase64Image(context: Context, base64Str: String, subDir: String, filePrefix: String): String? {
        return try {
            val bytes = Base64.getDecoder().decode(base64Str)
            val dir = File(context.filesDir, subDir)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${filePrefix}_${System.currentTimeMillis()}.png")
            file.writeBytes(bytes)
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            AppLogger.e("Failed to restore Base64 image", e)
            null
        }
    }
}
