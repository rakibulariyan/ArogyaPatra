package com.aidwellness.arogyapatra.pdf

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.aidwellness.arogyapatra.data.local.entity.CertificateEntity
import com.aidwellness.arogyapatra.data.local.entity.ClinicProfileEntity
import com.aidwellness.arogyapatra.data.local.entity.DoctorProfileEntity
import com.aidwellness.arogyapatra.util.AppLogger
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points
    private const val MARGIN = 40f
    private const val FOOTER_RESERVED_HEIGHT = 160f

    fun generateCertificatePdf(
        context: Context,
        certificate: CertificateEntity,
        doctor: DoctorProfileEntity?,
        clinic: ClinicProfileEntity?
    ): File? {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val primaryColor = parseColorHex(clinic?.primaryColorHex ?: "#007A78")
        val secondaryColor = parseColorHex(clinic?.secondaryColorHex ?: "#00B4D8")

        val titlePaint = Paint().apply {
            color = primaryColor
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val clinicNamePaint = Paint().apply {
            color = primaryColor
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val clinicSubPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            isAntiAlias = true
        }

        val certNoPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = Color.rgb(120, 120, 120)
            textSize = 7.5f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val linePaint = Paint().apply {
            color = secondaryColor
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(245, 247, 250)
            style = Paint.Style.FILL
        }

        var yPos = 40f
        val contentWidth = PAGE_WIDTH - (2 * MARGIN)

        fun checkAndCreatePageIfNeeded() {
            if (yPos > PAGE_HEIGHT - FOOTER_RESERVED_HEIGHT) {
                drawFooter(canvas, pageNumber, footerPaint, linePaint)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f
            }
        }

        // 1. Clinic Header
        val clinicName = clinic?.clinicName ?: "Clinic"
        canvas.drawText(clinicName, MARGIN, yPos, clinicNamePaint)
        yPos += 16f

        val clinicAddress = clinic?.address ?: ""
        canvas.drawText(clinicAddress, MARGIN, yPos, clinicSubPaint)
        yPos += 14f

        val clinicContact = "Phone: ${clinic?.phone ?: ""} | Email: ${clinic?.email ?: ""}"
        canvas.drawText(clinicContact, MARGIN, yPos, clinicSubPaint)
        yPos += 18f

        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
        yPos += 26f

        // 2. Certificate Title Header
        val certTitle = certificate.certificateType.uppercase()
        canvas.drawText(certTitle, PAGE_WIDTH / 2f, yPos, titlePaint)
        yPos += 22f

        // 3. Metadata Bar (Cert No & Date)
        val rectF = RectF(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 30f)
        canvas.drawRoundRect(rectF, 6f, 6f, cardBgPaint)

        canvas.drawText("Certificate No: ${certificate.certificateNumber}", MARGIN + 12f, yPos + 19f, certNoPaint)
        val dateText = "Issue Date: ${certificate.issueDate}"
        certNoPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(dateText, PAGE_WIDTH - MARGIN - 12f, yPos + 19f, certNoPaint)
        certNoPaint.textAlign = Paint.Align.LEFT
        yPos += 42f

        // 4. Patient Information Box
        val patientBoxRect = RectF(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 70f)
        canvas.drawRoundRect(patientBoxRect, 8f, 8f, cardBgPaint)

        var pY = yPos + 22f
        val boldPaint = Paint(bodyPaint).apply { isFakeBoldText = true }
        canvas.drawText("PATIENT DETAILS", MARGIN + 15f, pY, boldPaint)
        pY += 20f

        canvas.drawText("Name: ${certificate.patientName}", MARGIN + 15f, pY, bodyPaint)
        canvas.drawText("Age: ${certificate.patientAge} Yrs", MARGIN + 220f, pY, bodyPaint)
        canvas.drawText("Gender: ${certificate.patientGender}", MARGIN + 360f, pY, bodyPaint)
        pY += 18f

        canvas.drawText("Date: ${certificate.issueDate}", MARGIN + 15f, pY, bodyPaint)
        yPos += 88f

        // 5. Body Text & Medical Details
        val bodyText = buildString {
            append("This is to certify that ${certificate.patientName}, aged ${certificate.patientAge} years, ${certificate.patientGender}, ")
            when (certificate.certificateType) {
                "Sick Leave" -> {
                    append("has been suffering from ${certificate.diagnosis ?: "clinical illness"}. ")
                    append("I have advised him/her complete rest from duties for a period of ${certificate.durationDays ?: 1} days ")
                    append("from ${certificate.startDate ?: certificate.issueDate} to ${certificate.endDate ?: certificate.issueDate}.")
                }
                "Fitness Certificate" -> {
                    append("was medically examined on ${certificate.issueDate}. ")
                    append("${certificate.fitnessStatement ?: "Based on clinical examination, the individual is medically fit."} ")
                    if (!certificate.restrictions.isNullOrBlank()) {
                        append("Restrictions/Remarks: ${certificate.restrictions}.")
                    }
                }
                "Medical Examination" -> {
                    append("was examined for ${certificate.purpose ?: "general health assessment"}. ")
                    append("Clinical Findings: ${certificate.clinicalFindings ?: "No remarkable pathology detected"}. ")
                    if (!certificate.doctorRemarks.isNullOrBlank()) {
                        append("Remarks: ${certificate.doctorRemarks}.")
                    }
                }
                "Return to Work" -> {
                    append("was previously advised medical leave. Following re-examination, ")
                    append("he/she is declared fit to resume duties from ${certificate.startDate ?: certificate.issueDate}. ")
                    if (!certificate.restrictions.isNullOrBlank()) {
                        append("Work Restrictions: ${certificate.restrictions}.")
                    }
                }
                else -> {
                    if (!certificate.customContent.isNullOrBlank()) {
                        append(certificate.customContent)
                    } else {
                        append("has been examined and treated for ${certificate.diagnosis ?: "medical condition"}. ")
                        if (!certificate.doctorRemarks.isNullOrBlank()) {
                            append("Remarks: ${certificate.doctorRemarks}.")
                        }
                    }
                }
            }
        }

        val textPaint = TextPaint(bodyPaint).apply { textSize = 12.5f }
        val lines = wrapText(bodyText, textPaint, contentWidth - 20)
        for (line in lines) {
            checkAndCreatePageIfNeeded()
            canvas.drawText(line, MARGIN + 10f, yPos, textPaint)
            yPos += 19f
        }

        yPos += 20f

        if (!certificate.doctorRemarks.isNullOrBlank() && certificate.certificateType != "Medical Examination") {
            checkAndCreatePageIfNeeded()
            canvas.drawText("DOCTOR REMARKS & ADVICE:", MARGIN + 10f, yPos, boldPaint)
            yPos += 18f
            val remarkLines = wrapText(certificate.doctorRemarks, textPaint, contentWidth - 20)
            for (line in remarkLines) {
                checkAndCreatePageIfNeeded()
                canvas.drawText(line, MARGIN + 10f, yPos, textPaint)
                yPos += 18f
            }
            yPos += 20f
        }

        // 6. Signature, Stamp & QR Code Section
        val sigY = PAGE_HEIGHT - 175f

        // Minimal QR Code
        val qrContent = "Certificate No: ${certificate.certificateNumber}\nDate: ${certificate.issueDate}\nDoctor Reg: ${doctor?.registrationNumber ?: ""}"
        val qrBitmap = QrCodeGenerator.generateQrCodeBitmap(qrContent, 85, 85)
        qrBitmap?.let {
            canvas.drawBitmap(it, MARGIN + 10f, sigY - 10f, null)
            canvas.drawText("Certificate Information QR", MARGIN + 10f, sigY + 82f, clinicSubPaint)
        }

        // Organization Stamp Rendering
        clinic?.organizationStampUri?.let { stampUriStr ->
            loadBitmapFromUri(context, stampUriStr)?.let { stampBitmap ->
                val (scaledW, scaledH) = calculateScaledBounds(stampBitmap.width, stampBitmap.height, 75f, 75f)
                val stampX = MARGIN + 180f
                val stampY = sigY - 10f
                canvas.drawBitmap(stampBitmap, null, RectF(stampX, stampY, stampX + scaledW, stampY + scaledH), null)
            }
        }

        // Doctor Signature Block
        val docX = PAGE_WIDTH - MARGIN - 180f
        var dY = sigY - 10f

        canvas.drawText("AUTHORIZED SIGNATURE", docX, dY, certNoPaint)
        dY += 12f

        // Doctor Signature Bitmap Rendering
        doctor?.signatureUri?.let { sigUriStr ->
            loadBitmapFromUri(context, sigUriStr)?.let { sigBitmap ->
                val (scaledW, scaledH) = calculateScaledBounds(sigBitmap.width, sigBitmap.height, 140f, 45f)
                canvas.drawBitmap(sigBitmap, null, RectF(docX, dY, docX + scaledW, dY + scaledH), null)
                dY += scaledH + 6f
            } ?: run {
                dY += 24f
            }
        } ?: run {
            dY += 24f
        }

        val docName = doctor?.name ?: "Doctor"
        val docQual = doctor?.qualification ?: ""
        val docReg = "Reg No: ${doctor?.registrationNumber ?: ""}"
        val docSpec = doctor?.specialization ?: ""

        canvas.drawText(docName, docX, dY, boldPaint)
        dY += 16f
        canvas.drawText(docQual, docX, dY, clinicSubPaint)
        dY += 14f
        canvas.drawText(docSpec, docX, dY, clinicSubPaint)
        dY += 14f
        canvas.drawText(docReg, docX, dY, clinicSubPaint)

        // Draw Professional Footer on Last Page
        drawFooter(canvas, pageNumber, footerPaint, linePaint)
        pdfDocument.finishPage(page)

        val pdfFile = try {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Certificates")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${certificate.certificateNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            file
        } catch (e: Exception) {
            AppLogger.e("Failed to write PDF file", e)
            null
        } finally {
            pdfDocument.close()
        }

        return pdfFile
    }

    private fun loadBitmapFromUri(context: Context, uriStr: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriStr)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            AppLogger.e("Failed to load bitmap from $uriStr", e)
            null
        }
    }

    fun calculateScaledBounds(srcW: Int, srcH: Int, maxW: Float, maxH: Float): Pair<Float, Float> {
        val aspect = srcW.toFloat() / srcH.toFloat()
        var targetW = maxW
        var targetH = maxW / aspect

        if (targetH > maxH) {
            targetH = maxH
            targetW = maxH * aspect
        }
        return Pair(targetW, targetH)
    }

    private fun drawFooter(canvas: Canvas, pageNum: Int, footerPaint: Paint, linePaint: Paint) {
        val footerY = PAGE_HEIGHT - 22f
        canvas.drawLine(MARGIN, footerY - 12f, PAGE_WIDTH - MARGIN, footerY - 12f, linePaint)
        val footerDisclaimer = "Document prepared via ArogyaPatra • Issued by authorized healthcare practitioner • Page $pageNum"
        canvas.drawText(footerDisclaimer, PAGE_WIDTH / 2f, footerY, footerPaint)
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Certificate PDF"))
    }

    fun printPdf(context: Context, pdfFile: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val printAdapter = object : android.print.PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val builder = android.print.PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(android.print.PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback?.onLayoutFinished(builder, true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    val input = java.io.FileInputStream(pdfFile)
                    val output = FileOutputStream(destination?.fileDescriptor)
                    input.copyTo(output)
                    callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print("ArogyaPatra_${pdfFile.name}", printAdapter, null)
    }

    private fun wrapText(text: String, paint: TextPaint, width: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= width) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    private fun parseColorHex(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            Color.parseColor("#007A78")
        }
    }
}
