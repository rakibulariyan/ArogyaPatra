package com.aidwellness.arogyapatra.pdf

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {
    /**
     * Generates a QR Code matrix bitmap.
     * Note: Encodes minimal certificate metadata (Cert ID, Date, Doctor Reg No).
     * Does NOT encode sensitive patient personal data (name, phone, address, diagnosis).
     */
    fun generateQrCodeBitmap(content: String, width: Int = 200, height: Int = 200): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
