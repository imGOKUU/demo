package com.demo.attendancepro.ui.screens.faceenrolment

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.attendancepro.data.repository.StaffRepository
import com.demo.attendancepro.face.FaceDetectionResult
import com.demo.attendancepro.face.FaceRecognitionManager
import com.demo.attendancepro.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class EnrolmentStatus {
    object Capturing : EnrolmentStatus()
    object Processing : EnrolmentStatus()
    object Success : EnrolmentStatus()
    data class Failed(val message: String) : EnrolmentStatus()
}

data class FaceEnrolmentUiState(
    val staffName: String = "",
    val status: EnrolmentStatus = EnrolmentStatus.Capturing
)

@HiltViewModel
class FaceEnrolmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val staffRepository: StaffRepository,
    private val faceRecognitionManager: FaceRecognitionManager
) : ViewModel() {

    private val staffId: Long = checkNotNull(savedStateHandle[Screen.ARG_STAFF_ID])

    private val _uiState = MutableStateFlow(FaceEnrolmentUiState())
    val uiState: StateFlow<FaceEnrolmentUiState> = _uiState

    init {
        viewModelScope.launch {
            staffRepository.getStaffOnce(staffId)?.let { staff ->
                _uiState.update { it.copy(staffName = staff.name) }
            }
        }
    }

    fun onSelfieCaptured(bitmap: Bitmap, file: File) {
        _uiState.update { it.copy(status = EnrolmentStatus.Processing) }
        viewModelScope.launch {
            when (val result = faceRecognitionManager.detectSingleFace(bitmap)) {
                is FaceDetectionResult.NoFaceDetected -> fail(file, "No face detected. Please try again.")
                is FaceDetectionResult.MultipleFacesDetected ->
                    fail(file, "Multiple faces detected. Only one person should be in frame.")
                is FaceDetectionResult.Success -> {
                    val embedding = faceRecognitionManager.embed(result.croppedFace)
                    val staff = staffRepository.getStaffOnce(staffId)
                    if (staff == null) {
                        fail(file, "Staff record not found.")
                    } else {
                        staffRepository.saveEnrolment(staff, embedding, file.absolutePath)
                        _uiState.update { it.copy(status = EnrolmentStatus.Success) }
                    }
                }
            }
        }
    }

    private fun fail(file: File, message: String) {
        file.delete()
        _uiState.update { it.copy(status = EnrolmentStatus.Failed(message)) }
    }

    fun retry() {
        _uiState.update { it.copy(status = EnrolmentStatus.Capturing) }
    }
}
