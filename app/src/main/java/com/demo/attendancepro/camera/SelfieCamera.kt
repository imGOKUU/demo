package com.demo.attendancepro.camera

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.demo.attendancepro.util.BitmapUtils
import java.io.File
import java.util.concurrent.Executor

/**
 * Full-screen front-camera preview with a capture button. Saves the photo as a JPEG to
 * [outputFile] and reports it back via [onImageCaptured]; failures are reported via [onError].
 */
@Composable
fun SelfieCamera(
    outputFile: File,
    modifier: Modifier = Modifier,
    onImageCaptured: (Bitmap, File) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }
    var isCapturing by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        onError("Unable to start camera: ${e.message}")
                    }
                }, mainExecutor)
                previewView
            }
        )

        if (isCapturing) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        FloatingActionButton(
            onClick = {
                if (!isCapturing) {
                    isCapturing = true
                    capturePhoto(
                        imageCapture = imageCapture,
                        outputFile = outputFile,
                        executor = mainExecutor,
                        onSuccess = { bitmap ->
                            isCapturing = false
                            onImageCaptured(bitmap, outputFile)
                        },
                        onError = { message ->
                            isCapturing = false
                            onError(message)
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Icon(Icons.Filled.Camera, contentDescription = "Capture selfie")
        }
    }
}

private fun capturePhoto(
    imageCapture: ImageCapture,
    outputFile: File,
    executor: Executor,
    onSuccess: (Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val bitmap = runCatching { BitmapUtils.decodeUprightAndNormalize(outputFile) }.getOrNull()
                if (bitmap != null) {
                    onSuccess(bitmap)
                } else {
                    onError("Could not read captured photo")
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception.message ?: "Capture failed")
            }
        }
    )
}
