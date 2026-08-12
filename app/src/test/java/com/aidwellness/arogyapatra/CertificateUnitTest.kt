package com.aidwellness.arogyapatra

import com.aidwellness.arogyapatra.backup.BackupEnvelope
import com.aidwellness.arogyapatra.backup.BackupPayload
import com.aidwellness.arogyapatra.data.local.entity.AppSettingsEntity
import com.aidwellness.arogyapatra.data.local.entity.CertificateEntity
import com.aidwellness.arogyapatra.data.local.entity.ClinicProfileEntity
import com.aidwellness.arogyapatra.data.local.entity.DoctorProfileEntity
import com.aidwellness.arogyapatra.data.local.entity.PatientEntity
import com.aidwellness.arogyapatra.domain.usecase.CalculateCertificateDurationUseCase
import com.aidwellness.arogyapatra.domain.usecase.CalculatePatientAgeUseCase
import com.aidwellness.arogyapatra.domain.usecase.GenerateCertificateNumberUseCase
import com.aidwellness.arogyapatra.domain.usecase.ValidateTemplateUseCase
import com.aidwellness.arogyapatra.pdf.PdfGenerator
import com.aidwellness.arogyapatra.security.CryptoUtils
import com.aidwellness.arogyapatra.security.SecurityManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CertificateUnitTest {

    private val gson: Gson = GsonBuilder().create()

    @Before
    fun setUp() {
        SecurityManager.resetFailedAttempts()
    }

    // ==========================================
    // 1. SECURITY & PIN AUDIT TESTS
    // ==========================================

    @Test
    fun testPbkdf2PinHashingAndVerification() {
        val pin = "1234"
        val hash = SecurityManager.hashPin(pin)

        assertTrue(hash.contains(":"))
        val parts = hash.split(":")
        assertEquals(2, parts.size)
        assertTrue(SecurityManager.verifyPin("1234", hash))
        assertFalse(SecurityManager.verifyPin("9999", hash))
    }

    @Test
    fun testSaltUniquenessPerHash() {
        val hash1 = SecurityManager.hashPin("1234")
        val hash2 = SecurityManager.hashPin("1234")

        assertNotEquals(hash1, hash2) // Different random salts generate unique outputs
    }

    @Test
    fun testPinLockoutTriggerAfterFiveFailedAttempts() {
        val pinHash = SecurityManager.hashPin("4321")
        assertFalse(SecurityManager.isLockedOut())

        // Attempt 1 to 4 failed
        repeat(4) {
            assertFalse(SecurityManager.verifyPin("0000", pinHash))
            assertFalse(SecurityManager.isLockedOut())
        }

        // Attempt 5 failed -> triggers lockout
        assertFalse(SecurityManager.verifyPin("0000", pinHash))
        assertTrue(SecurityManager.isLockedOut())
        assertTrue(SecurityManager.getRemainingLockoutSeconds() > 0)
    }

    @Test
    fun testLockoutResetOnCorrectPin() {
        val pinHash = SecurityManager.hashPin("7777")
        repeat(3) { SecurityManager.verifyPin("1111", pinHash) }
        assertTrue(SecurityManager.verifyPin("7777", pinHash))
        assertFalse(SecurityManager.isLockedOut())
    }

    // ==========================================
    // 2. DYNAMIC AGE & DATE CALCULATION TESTS
    // ==========================================

    @Test
    fun testDynamicAgeCalculationFromDob() {
        val patient = PatientEntity(
            patientId = "PAT-1001",
            fullName = "Test Patient",
            dob = "1994-05-14",
            gender = "Male",
            phone = "+91 98765 43210",
            address = "Delhi"
        )
        val useCase = CalculatePatientAgeUseCase()
        
        // Birthday passed in year 2026 (May 14 vs Aug 11)
        val ageAfterBirthday = useCase.execute(patient, "11 Aug 2026")
        assertEquals(32, ageAfterBirthday)

        // Birthday NOT yet reached in year 2026 (May 14 vs Jan 10)
        val ageBeforeBirthday = useCase.execute(patient, "10 Jan 2026")
        assertEquals(31, ageBeforeBirthday)
    }

    @Test
    fun testLeapYearDobAgeCalculation() {
        val patient = PatientEntity(
            patientId = "PAT-2000",
            fullName = "Leap Patient",
            dob = "2000-02-29",
            gender = "Female",
            phone = "+91 98765 00000",
            address = "Mumbai"
        )
        val useCase = CalculatePatientAgeUseCase()
        val age = useCase.execute(patient, "28 Feb 2026")
        assertEquals(25, age)
    }

    @Test
    fun testRestDurationCalculation() {
        val useCase = CalculateCertificateDurationUseCase()
        val duration = useCase.execute("12 Aug 2026", "15 Aug 2026")
        assertEquals(4, duration)
    }

    // ==========================================
    // 3. TEMPLATE ENGINE & UNICODE TESTS
    // ==========================================

    @Test
    fun testTemplateValidationEngineWithUnicode() {
        val useCase = ValidateTemplateUseCase()
        val template = "This is to certify that {{patient_name}}, aged {{age}} years, has been suffering from {{diagnosis}}."
        val values = mapOf(
            "patient_name" to "राहुल शर्मा (Rahul Sharma)",
            "age" to "32",
            "diagnosis" to "Acute Typhoid Fever"
        )
        val result = useCase.execute(template, values)
        assertTrue(result.isValid)
        assertTrue(result.processedText.contains("राहुल शर्मा"))
    }

    @Test
    fun testTemplateValidationDetectsUnresolvedPlaceholders() {
        val useCase = ValidateTemplateUseCase()
        val template = "Certified that {{patient_name}} has {{diagnosis}} and requires {{missing_var}}."
        val values = mapOf(
            "patient_name" to "Priya Das",
            "diagnosis" to "Fever"
        )
        val result = useCase.execute(template, values)
        assertFalse(result.isValid)
        assertTrue(result.unresolvedPlaceholders.contains("missing_var"))
    }

    // ==========================================
    // 4. AES-256-GCM ENCRYPTED BACKUP & BRANDING ASSET TESTS
    // ==========================================

    @Test
    fun testAesGcmEncryptionDecryption() {
        val secretMessage = "{\"patientName\":\"Priya Das\",\"certNo\":\"MC-2026-000102\"}"
        val passphrase = "SecurePassword123"

        val encrypted = CryptoUtils.encrypt(secretMessage, passphrase)
        assertNotNull(encrypted.ciphertextBase64)
        assertNotNull(encrypted.ivHex)
        assertNotNull(encrypted.saltHex)

        val decrypted = CryptoUtils.decrypt(encrypted.ciphertextBase64, passphrase, encrypted.ivHex, encrypted.saltHex)
        assertEquals(secretMessage, decrypted)
    }

    @Test
    fun testAesGcmWrongPassphraseFails() {
        val secretMessage = "{\"patientName\":\"Secret Medical Data\"}"
        val passphrase = "RightPassword123"
        val wrongPassphrase = "WrongPassword999"

        val encrypted = CryptoUtils.encrypt(secretMessage, passphrase)
        try {
            CryptoUtils.decrypt(encrypted.ciphertextBase64, wrongPassphrase, encrypted.ivHex, encrypted.saltHex)
            fail("Decryption should have failed with wrong passphrase")
        } catch (e: Exception) {
            assertTrue(true) // Decryption failure expected
        }
    }

    @Test
    fun testCrossDeviceBackupWithBrandingImagesSimulation() {
        val doctor = DoctorProfileEntity(name = "Dr. Anita Roy", registrationNumber = "MCI-554433", signatureUri = "file:///data/user/0/com.aidwellness.arogyapatra/files/branding/signatures/doc_sig.png")
        val clinic = ClinicProfileEntity(clinicName = "Care Clinic", organizationStampUri = "file:///data/user/0/com.aidwellness.arogyapatra/files/branding/stamps/clinic_stamp.png")
        val patient = PatientEntity(patientId = "PAT-99", fullName = "Vikram Singh", dob = "1988-11-20", gender = "Male", phone = "+91 99999 88888", address = "Jaipur")
        val cert = CertificateEntity(certificateNumber = "MC-2026-000999", patientId = 99, patientName = "Vikram Singh", patientAge = 37, patientGender = "Male", certificateType = "Sick Leave", issueDate = "11 Aug 2026")

        val fakeSignatureBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        val fakeStampBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="

        val payload = BackupPayload(
            doctorProfile = doctor,
            clinicProfile = clinic,
            patients = listOf(patient),
            certificates = listOf(cert),
            templates = emptyList(),
            signatureBase64 = fakeSignatureBase64,
            stampBase64 = fakeStampBase64
        )

        val jsonPayload = gson.toJson(payload)
        val passphrase = "CrossDevicePassphrase2026"

        // Encrypt on Device A
        val encrypted = CryptoUtils.encrypt(jsonPayload, passphrase)
        val envelope = BackupEnvelope(
            schemaVersion = 1,
            exportedAt = System.currentTimeMillis(),
            ivHex = encrypted.ivHex,
            saltHex = encrypted.saltHex,
            encryptedPayload = encrypted.ciphertextBase64
        )
        val transferredFileString = gson.toJson(envelope)

        // Device B: Receive & Decrypt
        val receivedEnvelope = gson.fromJson(transferredFileString, BackupEnvelope::class.java)
        val decryptedJson = CryptoUtils.decrypt(
            receivedEnvelope.encryptedPayload,
            passphrase,
            receivedEnvelope.ivHex,
            receivedEnvelope.saltHex
        )

        val restoredPayload = gson.fromJson(decryptedJson, BackupPayload::class.java)
        assertNotNull(restoredPayload.doctorProfile)
        assertEquals("Dr. Anita Roy", restoredPayload.doctorProfile?.name)
        assertEquals(fakeSignatureBase64, restoredPayload.signatureBase64)
        assertEquals(fakeStampBase64, restoredPayload.stampBase64)
    }

    // ==========================================
    // 5. PDF ASPECT SCALING & AD MOB TESTS
    // ==========================================

    @Test
    fun testPdfAspectScalingBounds() {
        // Image 300x100 (3:1 aspect ratio) fitting in max 140x45
        val (w, h) = PdfGenerator.calculateScaledBounds(300, 100, 140f, 45f)
        assertEquals(135f, w, 0.1f)
        assertEquals(45f, h, 0.1f)
    }

    @Test
    fun testCertificateNumberFormatting() {
        val settings = AppSettingsEntity(certificatePrefix = "MC-2026-", certificateCounter = 128)
        val useCase = GenerateCertificateNumberUseCase()
        val certNum = useCase.execute(settings)
        assertEquals("MC-2026-000128", certNum)
    }
}
