package com.aidwellness.arogyapatra.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    // Bottom Bar Items
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Certificates : Screen("certificates", "Certificates", Icons.Default.Description)
    object Patients : Screen("patients", "Patients", Icons.Default.People)
    object Templates : Screen("templates", "Templates", Icons.Default.Article)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Flow Routes
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Lock : Screen("lock")
    object SelectCertificate : Screen("select_certificate")
    object PatientSelection : Screen("select_patient/{certType}") {
        fun createRoute(certType: String) = "select_patient/${UriCodec.encode(certType)}"
    }
    object CertificateForm : Screen("certificate_form/{certType}/{patientId}") {
        fun createRoute(certType: String, patientId: Long) = "certificate_form/${UriCodec.encode(certType)}/$patientId"
    }
    object CertificatePreview : Screen("certificate_preview/{certId}") {
        fun createRoute(certId: Long) = "certificate_preview/$certId"
    }
    object AddEditPatient : Screen("add_edit_patient/{patientId}") {
        fun createRoute(patientId: Long = 0L) = "add_edit_patient/$patientId"
    }
    object PatientDetail : Screen("patient_detail/{patientId}") {
        fun createRoute(patientId: Long) = "patient_detail/$patientId"
    }
    object AddEditTemplate : Screen("add_edit_template/{templateId}") {
        fun createRoute(templateId: Long = 0L) = "add_edit_template/$templateId"
    }
    object DoctorProfile : Screen("doctor_profile")
    object ClinicProfile : Screen("clinic_profile")
    object SecuritySettings : Screen("security_settings")
    object BackupRestore : Screen("backup_restore")
    object PrivacyPolicy : Screen("privacy_policy")
}

object UriCodec {
    fun encode(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")
    fun decode(value: String): String = java.net.URLDecoder.decode(value, "UTF-8")
}
