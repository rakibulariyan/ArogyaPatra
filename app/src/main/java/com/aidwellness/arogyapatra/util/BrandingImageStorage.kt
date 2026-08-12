package com.aidwellness.arogyapatra.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object BrandingImageStorage {

    fun saveDoctorSignature(context: Context, bitmap: Bitmap, oldSignatureUri: String? = null): String? {
        return saveBrandingImage(
            context = context,
            bitmap = bitmap,
            subDir = "branding/signatures",
            filePrefix = "doctor_signature",
            oldImageUri = oldSignatureUri
        )
    }

    fun saveOrganizationStamp(context: Context, bitmap: Bitmap, oldStampUri: String? = null): String? {
        return saveBrandingImage(
            context = context,
            bitmap = bitmap,
            subDir = "branding/stamps",
            filePrefix = "clinic_stamp",
            oldImageUri = oldStampUri
        )
    }

    fun saveClinicLogo(context: Context, bitmap: Bitmap, oldLogoUri: String? = null): String? {
        return saveBrandingImage(
            context = context,
            bitmap = bitmap,
            subDir = "branding/logos",
            filePrefix = "clinic_logo",
            oldImageUri = oldLogoUri
        )
    }

    private fun saveBrandingImage(
        context: Context,
        bitmap: Bitmap,
        subDir: String,
        filePrefix: String,
        oldImageUri: String?
    ): String? {
        return try {
            val dir = File(context.filesDir, subDir)
            if (!dir.exists()) dir.mkdirs()

            val fileName = "${filePrefix}_${System.currentTimeMillis()}.png"
            val file = File(dir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Safe cleanup of old image AFTER successful write
            deleteImageFile(oldImageUri)

            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            AppLogger.e("Failed to save branding image", e)
            null
        }
    }

    fun deleteImageFile(fileUriStr: String?) {
        if (fileUriStr.isNullOrBlank()) return
        try {
            val uri = Uri.parse(fileUriStr)
            val path = uri.path ?: return
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            AppLogger.e("Failed to delete branding file", e)
        }
    }
}
