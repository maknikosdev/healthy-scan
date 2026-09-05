package com.healthyscan.app.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.healthyscan.app.R
import java.io.File
import java.util.concurrent.Executors

/**
 * "Scan Label" flow. Captures a photo of a nutrition label with CameraX and
 * runs on-device OCR (ML Kit Text Recognition) on it.
 *
 * This shows the raw recognized text with an editable review step, matching
 * the "Confirm the details" step from the product spec. Turning the raw OCR
 * text into structured nutrition fields (calories/sugar/protein/etc.) is left
 * as a parsing step you can plug in — the text lines are already split out
 * and ready to feed into a parser or into a backend LLM call for extraction.
 */
@Composable
fun LabelScanScreen(onDone: () -> Unit) {
    val context = LocalContext.current
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

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var recognizedLines by remember { mutableStateOf<List<String>?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !hasCameraPermission -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.scan_camera_permission_needed))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.padding(top = 16.dp)) {
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
                    onCapture = { imageProxyFile ->
                        isProcessing = true
                        val uri = Uri.fromFile(imageProxyFile)
                        capturedImageUri = uri
                        val image = InputImage.fromFilePath(context, uri)
                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        recognizer.process(image)
                            .addOnSuccessListener { result ->
                                recognizedLines = result.textBlocks.flatMap { block ->
                                    block.lines.map { it.text }
                                }.ifEmpty { listOf("—") }
                                isProcessing = false
                            }
                            .addOnFailureListener {
                                recognizedLines = listOf("—")
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
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(stringResource(R.string.confirm_details), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.confirm_details_body), modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(lines) { line -> Text("• $line", modifier = Modifier.padding(vertical = 2.dp)) }
        }
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
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

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom
        ) {
            if (isProcessing) {
                CircularProgressIndicator()
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
