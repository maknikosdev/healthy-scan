package com.healthyscan.app.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Wraps Tesseract4Android: extracts the bundled Greek + English trained-data
 * files from assets on first use, then runs fully offline, on-device OCR.
 *
 * Chosen over ML Kit's text recognizer because ML Kit has no Greek model at
 * all (Latin/Chinese/Japanese/Korean/Devanagari only) — see the "AI Label
 * Scanner" discussion in the project history. Tesseract is slower and a bit
 * less accurate on curved jar/bottle labels, but it's free forever, works
 * fully offline, and actually reads Greek text.
 */
object TesseractOcrHelper {

    private const val LANGUAGES = "ell+eng" // Greek + English combined model
    private val bundledFiles = listOf("ell.traineddata", "eng.traineddata")

    /** Copies the trained-data files out of assets/tessdata into a real
     *  filesystem path the native Tesseract library can read (required on
     *  API 29+, where APK assets can't be opened directly by native code). */
    private fun ensureTrainedDataExtracted(context: Context) {
        val tessdataDir = File(context.filesDir, "tessdata")
        if (!tessdataDir.exists()) tessdataDir.mkdirs()

        for (fileName in bundledFiles) {
            val outFile = File(tessdataDir, fileName)
            if (outFile.exists() && outFile.length() > 0) continue // already extracted

            context.assets.open("tessdata/$fileName").use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    /** Loads the captured JPEG, corrects its orientation using EXIF data, and
     *  downscales very large photos so recognition stays fast. */
    private fun loadUprightBitmap(file: File): Bitmap {
        val original = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        val upright = if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
        } else {
            original
        }

        // Cap the longest side at 2000px — plenty of detail for label text,
        // keeps recognition time reasonable on older devices.
        val maxDimension = 2000
        val largestSide = maxOf(upright.width, upright.height)
        return if (largestSide > maxDimension) {
            val scale = maxDimension.toFloat() / largestSide
            Bitmap.createScaledBitmap(
                upright,
                (upright.width * scale).toInt(),
                (upright.height * scale).toInt(),
                true
            )
        } else {
            upright
        }
    }

    /**
     * Runs OCR on [imageFile] and returns the recognized text split into
     * non-empty lines, ready to show in the "confirm details" review screen.
     * Safe to call from any coroutine — the actual recognition runs on
     * [Dispatchers.Default] since it's CPU-bound, not I/O-bound.
     */
    suspend fun recognizeLines(context: Context, imageFile: File): List<String> =
        withContext(Dispatchers.Default) {
            ensureTrainedDataExtracted(context)

            val tessBaseApi = TessBaseAPI()
            try {
                val dataPath = context.filesDir.absolutePath
                val initialized = tessBaseApi.init(dataPath, LANGUAGES)
                if (!initialized) return@withContext listOf("—")

                val bitmap = loadUprightBitmap(imageFile)
                tessBaseApi.setImage(bitmap)
                val recognizedText = tessBaseApi.utF8Text ?: ""

                recognizedText
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .ifEmpty { listOf("—") }
            } catch (e: Exception) {
                listOf("—")
            } finally {
                tessBaseApi.recycle()
            }
        }
}
