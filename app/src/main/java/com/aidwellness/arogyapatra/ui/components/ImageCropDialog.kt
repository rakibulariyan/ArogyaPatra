package com.aidwellness.arogyapatra.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aidwellness.arogyapatra.ui.theme.EmeraldTealPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ImageCropDialog(
    imageUri: Uri,
    title: String = "Crop Image",
    aspectRatio: Float = 3f / 1f, // Default 3:1 for Signature, 1:1 for Stamp
    onCropApplied: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = false
                        inSampleSize = 1
                    }
                    val decoded = BitmapFactory.decodeStream(stream, null, options)
                    sourceBitmap = decoded
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Crop Viewport Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray.copy(alpha = 0.3f))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    sourceBitmap?.let { bitmap ->
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            // Calculate crop frame rectangle
                            val frameWidth = (canvasWidth * 0.85f).coerceAtMost(canvasHeight * 0.85f * aspectRatio)
                            val frameHeight = frameWidth / aspectRatio

                            val frameLeft = (canvasWidth - frameWidth) / 2f
                            val frameTop = (canvasHeight - frameHeight) / 2f

                            val cropRect = Rect(frameLeft, frameTop, frameLeft + frameWidth, frameTop + frameHeight)

                            // Render Scaled & Panned Image using Android Canvas Bridge
                            drawIntoCanvas { canvas ->
                                val matrix = Matrix().apply {
                                    postTranslate(-bitmap.width / 2f, -bitmap.height / 2f)
                                    postScale(scale, scale)
                                    postTranslate(canvasWidth / 2f + offset.x, canvasHeight / 2f + offset.y)
                                }
                                canvas.nativeCanvas.drawBitmap(bitmap, matrix, null)
                            }

                            // Semi-transparent overlay with clear crop frame cutout
                            val overlayPath = Path().apply {
                                addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                                addRect(cropRect)
                                fillType = PathFillType.EvenOdd
                            }
                            drawPath(overlayPath, Color.Black.copy(alpha = 0.65f))

                            // Draw Crop Frame Grid Lines
                            drawRect(
                                color = EmeraldTealPrimary,
                                topLeft = Offset(cropRect.left, cropRect.top),
                                size = Size(cropRect.width, cropRect.height),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    } ?: CircularProgressIndicator(color = EmeraldTealPrimary, modifier = Modifier.align(Alignment.Center))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = {
                            sourceBitmap?.let { bitmap ->
                                // Process Cropped Bitmap on background thread
                                val cropped = cropBitmapToAspect(bitmap, scale, offset, aspectRatio)
                                onCropApplied(cropped)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTealPrimary),
                        enabled = sourceBitmap != null
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Crop", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun cropBitmapToAspect(source: Bitmap, scale: Float, offset: Offset, aspectRatio: Float): Bitmap {
    val srcWidth = source.width
    val srcHeight = source.height

    var targetWidth = srcWidth
    var targetHeight = (srcWidth / aspectRatio).toInt()

    if (targetHeight > srcHeight) {
        targetHeight = srcHeight
        targetWidth = (srcHeight * aspectRatio).toInt()
    }

    val startX = ((srcWidth - targetWidth) / 2).coerceIn(0, srcWidth - 1)
    val startY = ((srcHeight - targetHeight) / 2).coerceIn(0, srcHeight - 1)

    return Bitmap.createBitmap(source, startX, startY, targetWidth.coerceAtLeast(10), targetHeight.coerceAtLeast(10))
}
