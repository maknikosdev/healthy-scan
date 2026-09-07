package com.healthyscan.app.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.healthyscan.app.R
import com.healthyscan.app.ocr.TesseractOcrHelper
import kotlinx.coroutines.launch
import java.io.File

/**
 * "Scan Label" flow. Captures a photo of a nutrition/ingredients label with
 * CameraX and runs fully offline OCR on it using Tesseract (see
 * ocr/TesseractOcrHelper.kt) — it has real Greek + English trained models,
 * unlike ML Kit's text recognizer which only supports Latin script.
 *
 * Turning the raw recognized lines into structured nutrition fields
 * (calories/sugar/protein/etc.) is left as a parsing step you can plug in —
 * the lines are already split out and ready to feed into a parser.
 */
@Composable
fun LabelScanScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var recognizedLines by remember { mutableStateOf<List<String>?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !hasCameraPermission -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.scan_camera_permission_needed))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(stringResource(R.string.scan_grant_permission))
                    }
                }
            }
            recognizedLines != null -> {
                LabelReviewList(lines = recognizedLines.orEmpty(), onDone = onDone)
            }
            else -> {
                LabelCaptureView(
                    isProcessing = isProcessing,
                    onCapture = { imageFile ->
                        isProcessing = true
                        scope.launch {
                            recognizedLines = TesseractOcrHelper.recognizeLines(context, imageFile)
                            isProcessing = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LabelReviewList(lines: List<String>, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(stringResource(R.string.confirm_details), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.confirm_details_body),
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(lines) { line -> Text("• $line", modifier = Modifier.padding(vertical = 2.dp)) }
        }
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(stringResource(R.string.action_continue))
        }
    }
}

@Composable
private fun LabelCaptureView(isProcessing: Boolean, onCapture: (File) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val capture = ImageCapture.Builder().build()
            imageCapture = capture
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
            } catch (e: Exception) {
                // Camera binding failed.
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.ui.viewinterop.AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Guidance frame: helps the person fill the frame with just the
        // ingredients paragraph, which is what actually gets OCR'd well.
        if (!isProcessing) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .size(width = 320.dp, height = 220.dp)
                        .border(width = 3.dp, color = Color.White, shape = RoundedCornerShape(16.dp))
                )
                Text(
                    text = stringResource(R.string.label_scan_tip),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = stringResource(R.string.label_reading),
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                Button(onClick = {
                    val capture = imageCapture ?: return@Button
                    val outputFile = File.createTempFile("healthyscan_label_", ".jpg", context.cacheDir)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
                    capture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onCapture(outputFile)
                            }
                            override fun onError(exception: ImageCaptureException) { /* no-op */ }
                        }
                    )
                }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Text(stringResource(R.string.home_scan_label_title), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
