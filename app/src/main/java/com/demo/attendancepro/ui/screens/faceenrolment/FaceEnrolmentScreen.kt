package com.demo.attendancepro.ui.screens.faceenrolment

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
                title = { Text("Enrol Face — ${uiState.staffName}") },
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
                is EnrolmentStatus.Capturing -> {
                    RequirePermissions(
                        permissions = listOf(Manifest.permission.CAMERA),
                        rationale = "Camera access is needed to capture the staff member's face."
                    ) {
                        val outputFile = remember { FileUtils.newSelfieFile(context, "enroll") }
                        SelfieCamera(
                            outputFile = outputFile,
                            modifier = Modifier.fillMaxSize(),
                            onImageCaptured = { bitmap, file -> viewModel.onSelfieCaptured(bitmap, file) },
                            onError = { }
                        )
                    }
                }
                is EnrolmentStatus.Processing -> CenteredMessage {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Verifying face…")
                }
                is EnrolmentStatus.Success -> CenteredMessage {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Face enrolled successfully")
                }
                is EnrolmentStatus.Failed -> CenteredMessage {
                    Text(status.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = viewModel::retry) { Text("Try again") }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}
