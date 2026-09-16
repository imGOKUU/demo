package com.demo.attendancepro.ui.screens.faceenrolment

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.attendancepro.data.local.dao.EnrolmentOutcome
import com.demo.attendancepro.data.repository.StaffRepository
import com.demo.attendancepro.face.FaceDetectionResult
import com.demo.attendancepro.face.FaceRecognitionManager
import com.demo.attendancepro.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.demo.attendancepro.face.MatchResult
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class EnrolmentStatus {
    object Loading : EnrolmentStatus()
    object AlreadyEnrolled : EnrolmentStatus()
    object Capturing : EnrolmentStatus()
    object Processing : EnrolmentStatus()
    object Success : EnrolmentStatus()
    data class Failed(val message: String) : EnrolmentStatus()
    data class DuplicateFace(val existingStaffName: String) : EnrolmentStatus()
}

data class FaceEnrolmentUiState(
    val staffName: String = "",
    val status: EnrolmentStatus = EnrolmentStatus.Loading
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
            val staff = staffRepository.getStaffOnce(staffId)
            _uiState.update {
                it.copy(
                    staffName = staff?.name.orEmpty(),
                    status = if (staff?.faceEmbedding != null) {
                        EnrolmentStatus.AlreadyEnrolled
                    } else {
                        EnrolmentStatus.Capturing
                    }
                )
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
                    val otherEmbeddings = staffRepository.getEnrolledEmbeddings()
                        .filter { (id, _) -> id != staffId }
                    val duplicateCheck = faceRecognitionManager.findBestMatch(embedding, otherEmbeddings)

                    if (duplicateCheck is MatchResult.Matched) {
                        val existingName = staffRepository.getStaffOnce(duplicateCheck.staffId)?.name
                            ?: "another staff member"
                        file.delete()
                        _uiState.update { it.copy(status = EnrolmentStatus.DuplicateFace(existingName)) }
                        return@launch
                    }
                    when (staffRepository.saveEnrolment(staffId, embedding, file.absolutePath)) {
                        EnrolmentOutcome.Saved -> _uiState.update { it.copy(status = EnrolmentStatus.Success) }
                        EnrolmentOutcome.AlreadyEnrolled -> {
                            file.delete()
                            _uiState.update { it.copy(status = EnrolmentStatus.AlreadyEnrolled) }
                        }
                        EnrolmentOutcome.StaffNotFound -> fail(file, "Staff record not found.")
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