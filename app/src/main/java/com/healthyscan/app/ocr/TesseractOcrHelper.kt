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
import kotlin.math.max
import kotlin.math.min

/**
 * Wraps Tesseract4Android: extracts the bundled Greek + English "best"
 * (highest-accuracy) trained-data files from assets on first use, then runs
 * fully offline, on-device OCR with an image-preprocessing pipeline tuned
 * for photographed food labels — including curved jar/bottle labels, which
 * mainly fail OCR not because of the curvature itself but because of the
 * uneven lighting and glare that curved, glossy surfaces produce.
 *
 * Recognition strategy (see [recognizeLines]):
 *   1. Correct EXIF rotation, downscale to a sane working resolution.
 *   2. Convert to grayscale and apply *local* illumination normalization
 *      (an integral-image / summed-area-table technique) — this evens out
 *      the bright-spot/shadow gradient typical of curved, glossy labels
 *      without destroying thin character strokes the way a naive global
 *      contrast stretch would.
 *   3. Run Tesseract (Greek+English combined model) on the normalized image.
 *   4. If Tesseract's own confidence score is low, retry on a locally
 *      adaptive-binarized version of the same image (Bradley's algorithm,
 *      built from the same integral image) as a fallback, and keep whichever
 *      pass scored a higher mean confidence.
 *
 * This does NOT geometrically "unwarp" a curved surface — true perspective
 * correction for a cylindrical label needs a proper computer-vision library
 * (e.g. OpenCV) and is a much larger addition. What's implemented here
 * addresses the dominant real-world failure mode (lighting, not geometry)
 * for typical close-up label photos.
 */
object TesseractOcrHelper {

    private const val LANGUAGES = "ell+eng" // Greek + English combined model
    private val bundledFiles = listOf("ell.traineddata", "eng.traineddata")

    /** Working resolution cap. Large enough to keep small print legible,
     *  small enough that the integral-image preprocessing stays fast. */
    private const val MAX_DIMENSION = 1600

    /** Below this Tesseract mean-confidence (0-100), we retry with the
     *  binarized fallback pass instead of trusting the first result. */
    private const val CONFIDENCE_RETRY_THRESHOLD = 60

    private fun ensureTrainedDataExtracted(context: Context) {
        val tessdataDir = File(context.filesDir, "tessdata")
        if (!tessdataDir.exists()) tessdataDir.mkdirs()

        for (fileName in bundledFiles) {
            val outFile = File(tessdataDir, fileName)
            val assetSize = context.assets.open("tessdata/$fileName").use { it.available().toLong() }
            // Re-extract if missing OR if the bundled asset changed size since
            // last extraction (e.g. after an app update that swapped models).
            if (outFile.exists() && outFile.length() == assetSize) continue

            context.assets.open("tessdata/$fileName").use { input ->
                FileOutputStream(outFile).use { output -> input.copyTo(output) }
            }
        }
    }

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

        val largestSide = max(upright.width, upright.height)
        return if (largestSide > MAX_DIMENSION) {
            val scale = MAX_DIMENSION.toFloat() / largestSide
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

    // --- Preprocessing: grayscale + integral image + local normalization/threshold ---

    private fun toGrayscale(bitmap: Bitmap): IntArray {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val gray = IntArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            // Standard luminance weighting
            gray[i] = ((r * 299 + g * 587 + b * 114) / 1000)
        }
        return gray
    }

    /** Summed-area table over the grayscale image, size (w+1) x (h+1), for O(1) window sums. */
    private fun buildIntegralImage(gray: IntArray, w: Int, h: Int): LongArray {
        val integral = LongArray((w + 1) * (h + 1))
        for (y in 0 until h) {
            var rowSum = 0L
            for (x in 0 until w) {
                rowSum += gray[y * w + x]
                val above = integral[y * (w + 1) + (x + 1)]
                integral[(y + 1) * (w + 1) + (x + 1)] = above + rowSum
            }
        }
        return integral
    }

    private fun windowMean(integral: LongArray, w: Int, h: Int, cx: Int, cy: Int, radius: Int): Int {
        val x0 = max(0, cx - radius)
        val y0 = max(0, cy - radius)
        val x1 = min(w - 1, cx + radius)
        val y1 = min(h - 1, cy + radius)
        val count = (x1 - x0 + 1) * (y1 - y0 + 1)
        val sum = integral[(y1 + 1) * (w + 1) + (x1 + 1)] -
            integral[(y0) * (w + 1) + (x1 + 1)] -
            integral[(y1 + 1) * (w + 1) + (x0)] +
            integral[(y0) * (w + 1) + (x0)]
        return (sum / count).toInt()
    }

    /**
     * Evens out uneven lighting (the main enemy of OCR on glossy/curved
     * labels) by comparing each pixel to its *local* neighborhood average
     * instead of a single global brightness value, then re-centering around
     * mid-gray with a mild contrast boost. Output is still grayscale, not
     * binary — Tesseract's LSTM models generally read soft grayscale better
     * than harshly thresholded images.
     */
    private fun illuminationNormalize(gray: IntArray, w: Int, h: Int, radius: Int): IntArray {
        val integral = buildIntegralImage(gray, w, h)
        val out = IntArray(gray.size)
        val gain = 1.6f
        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                val localMean = windowMean(integral, w, h, x, y, radius)
                val normalized = 128 + ((gray[idx] - localMean) * gain).toInt()
                out[idx] = normalized.coerceIn(0, 255)
            }
        }
        return out
    }

    /** Bradley's adaptive thresholding: classifies each pixel as text (black)
     *  or background (white) relative to its local neighborhood mean, so it
     *  keeps working even when brightness varies a lot across the label. */
    private fun adaptiveBinarize(gray: IntArray, w: Int, h: Int, radius: Int, tPercent: Int = 15): IntArray {
        val integral = buildIntegralImage(gray, w, h)
        val out = IntArray(gray.size)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                val localMean = windowMean(integral, w, h, x, y, radius)
                val isForeground = gray[idx] * 100 <= localMean * (100 - tPercent)
                out[idx] = if (isForeground) 0 else 255
            }
        }
        return out
    }

    private fun grayscaleToBitmap(gray: IntArray, w: Int, h: Int): Bitmap {
        val pixels = IntArray(gray.size)
        for (i in gray.indices) {
            val v = gray[i]
            pixels[i] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }

    private data class Attempt(val text: String, val confidence: Int)

    private fun runTesseract(tess: TessBaseAPI, bitmap: Bitmap): Attempt {
        tess.setImage(bitmap)
        val text = tess.utF8Text ?: ""
        val confidence = try {
            tess.meanConfidence()
        } catch (e: Exception) {
            if (text.isBlank()) 0 else 50
        }
        return Attempt(text, confidence)
    }

    /**
     * Runs OCR on [imageFile] and returns the recognized text split into
     * non-empty lines, ready to show in the "confirm details" review screen.
     * Works fully offline; safe to call from any coroutine.
     */
    suspend fun recognizeLines(context: Context, imageFile: File): List<String> =
        withContext(Dispatchers.Default) {
            ensureTrainedDataExtracted(context)

            val tess = TessBaseAPI()
            try {
                val dataPath = context.filesDir.absolutePath
                val initialized = tess.init(dataPath, LANGUAGES)
                if (!initialized) return@withContext listOf("—")

                tess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK)

                val upright = loadUprightBitmap(imageFile)
                val gray = toGrayscale(upright)
                val w = upright.width
                val h = upright.height
                val windowRadius = max(15, min(w, h) / 12)

                // Pass 1: local illumination-normalized grayscale (usually best for LSTM models)
                val normalizedGray = illuminationNormalize(gray, w, h, windowRadius)
                val normalizedBitmap = grayscaleToBitmap(normalizedGray, w, h)
                var best = runTesseract(tess, normalizedBitmap)

                // Pass 2 (fallback): adaptive-binarized version, only if pass 1
                // wasn't confident — handles stubborn glare/shadow cases.
                if (best.confidence < CONFIDENCE_RETRY_THRESHOLD) {
                    val binary = adaptiveBinarize(gray, w, h, windowRadius)
                    val binaryBitmap = grayscaleToBitmap(binary, w, h)
                    val fallback = runTesseract(tess, binaryBitmap)
                    if (fallback.confidence > best.confidence) {
                        best = fallback
                    }
                }

                best.text
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .ifEmpty { listOf("—") }
            } catch (e: Exception) {
                listOf("—")
            } finally {
                tess.recycle()
            }
        }
}
