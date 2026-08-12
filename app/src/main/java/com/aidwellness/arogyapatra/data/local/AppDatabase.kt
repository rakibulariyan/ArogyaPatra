package com.aidwellness.arogyapatra.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aidwellness.arogyapatra.data.local.dao.*
import com.aidwellness.arogyapatra.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PatientEntity::class,
        CertificateEntity::class,
        DoctorProfileEntity::class,
        ClinicProfileEntity::class,
        TemplateEntity::class,
        AppSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun certificateDao(): CertificateDao
    abstract fun doctorClinicDao(): DoctorClinicDao
    abstract fun templateDao(): TemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clinic_profile ADD COLUMN organizationStampUri TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_settings ADD COLUMN isOnboardingCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arogyapatra_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val doctorDao = database.doctorClinicDao()
            val templateDao = database.templateDao()

            // Seed empty Doctor & Clinic Profile singleton rows
            doctorDao.updateDoctorProfile(DoctorProfileEntity())
            doctorDao.updateClinicProfile(ClinicProfileEntity())
            doctorDao.updateAppSettings(AppSettingsEntity())

            // Seed default certificate templates (application functionality, NOT demo data)
            val templates = listOf(
                TemplateEntity(
                    title = "General Medical Certificate",
                    type = "Medical Certificate",
                    bodyText = "This is to certify that {{patient_name}}, aged {{age}} years, {{gender}}, residing at {{address}}, has been under my medical treatment for {{diagnosis}}. He/She is advised medical attention and care as per clinical findings.",
                    isDefault = true
                ),
                TemplateEntity(
                    title = "Sick Leave Certificate",
                    type = "Sick Leave",
                    bodyText = "This is to certify that {{patient_name}}, aged {{age}} years, {{gender}}, has been suffering from {{diagnosis}}. I have advised him/her complete rest from work/duty for a period of {{duration}} days starting from {{start_date}} to {{end_date}}.",
                    isDefault = true
                ),
                TemplateEntity(
                    title = "Medical Fitness Certificate",
                    type = "Fitness Certificate",
                    bodyText = "This is to certify that I have carefully examined {{patient_name}}, aged {{age}} years, {{gender}}, on {{date}}. Based on my clinical assessment, I find him/her medically fit to resume normal duties/activities.",
                    isDefault = true
                ),
                TemplateEntity(
                    title = "Medical Examination Certificate",
                    type = "Medical Examination",
                    bodyText = "This is to certify that {{patient_name}}, aged {{age}} years, {{gender}}, was medically examined on {{date}} for {{purpose}}. Clinical Findings: {{clinical_findings}}. Remarks: {{remarks}}.",
                    isDefault = true
                ),
                TemplateEntity(
                    title = "Return-to-Work Certificate",
                    type = "Return to Work",
                    bodyText = "This is to certify that {{patient_name}}, aged {{age}} years, {{gender}}, who was previously advised leave from {{start_date}} to {{end_date}} for {{diagnosis}}, has been re-examined on {{date}} and is now fit to resume work with {{restrictions}}.",
                    isDefault = true
                ),
                TemplateEntity(
                    title = "Custom Certificate Template",
                    type = "Custom Certificate",
                    bodyText = "This is a custom medical certificate issued for {{patient_name}}, aged {{age}} years, {{gender}}. Remarks: {{remarks}}.",
                    isDefault = true
                )
            )

            templates.forEach { templateDao.insertTemplate(it) }
        }
    }
}
