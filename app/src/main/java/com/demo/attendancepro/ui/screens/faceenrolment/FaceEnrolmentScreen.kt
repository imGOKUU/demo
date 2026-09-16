package com.demo.attendancepro.ui.screens.faceenrolment

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.demo.attendancepro.camera.SelfieCamera
import com.demo.attendancepro.ui.components.RequirePermissions
import com.demo.attendancepro.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceEnrolmentScreen(
    viewModel: FaceEnrolmentViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEnrolmentComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.status) {
        if (uiState.status is EnrolmentStatus.Success) {
            onEnrolmentComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.staffName.isBlank()) "Face Enrolment" else "Enrol Face — ${uiState.staffName}")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val status = uiState.status) {
                is EnrolmentStatus.Loading -> CenteredMessage {
                    CircularProgressIndicator()
                }
                is EnrolmentStatus.AlreadyEnrolled -> CenteredMessage {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Face already enrolled for this account.",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "A staff member's face can only be enrolled once. Re-enrolling isn't available from this screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBack) { Text("Back to Staff List") }
                }
                is EnrolmentStatus.Capturing -> {
                    RequirePermissions(
                        permissions = listOf(Manifest.permission.CAMERA),
                        rationale = "Camera access is needed to capture the staff member's face."
                    ) {
                        val outputFile = remember { FileUtils.newSelfieFile(context, "enroll") }
                        SelfieCamera(
                            outputFile = outputFile,
                            modifier = Modifier.fillMaxSize(),
                            instructions = "Face the camera in good light, keep only one person in frame, then tap to capture.",
                            onImageCaptured = { bitmap, file -> viewModel.onSelfieCaptured(bitmap, file) },
                            onError = { }
                        )
                    }
                }
                is EnrolmentStatus.Processing -> CenteredMessage {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Verifying face…", style = MaterialTheme.typography.titleMedium)
                }
                is EnrolmentStatus.Success -> CenteredMessage {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Face enrolled successfully", style = MaterialTheme.typography.titleMedium)
                }
                is EnrolmentStatus.Failed -> CenteredMessage {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        status.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onBack) { Text("Cancel") }
                        Button(onClick = viewModel::retry) { Text("Try again") }
                    }
                }
                is EnrolmentStatus.DuplicateFace -> CenteredMessage {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "This face is already enrolled for ${status.existingStaffName}.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onBack) { Text("Back to Staff List") }
                        Button(onClick = viewModel::retry) { Text("Try different face") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}