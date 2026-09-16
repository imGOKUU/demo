package com.demo.attendancepro.ui.screens.markattendance

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.attendancepro.data.repository.AttendanceRepository
import com.demo.attendancepro.data.repository.StaffRepository
import com.demo.attendancepro.face.FaceDetectionResult
import com.demo.attendancepro.face.FaceRecognitionManager
import com.demo.attendancepro.face.MatchResult
import com.demo.attendancepro.location.LocationHelper
import com.demo.attendancepro.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class MarkAttendanceStatus {
    object Capturing : MarkAttendanceStatus()
    object Processing : MarkAttendanceStatus()
    data class Success(val staffName: String) : MarkAttendanceStatus()
    data class Failed(val message: String) : MarkAttendanceStatus()
}

data class MarkAttendanceUiState(
    val status: MarkAttendanceStatus = MarkAttendanceStatus.Capturing
)

@HiltViewModel
class MarkAttendanceViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val attendanceRepository: AttendanceRepository,
    private val faceRecognitionManager: FaceRecognitionManager,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarkAttendanceUiState())
    val uiState: StateFlow<MarkAttendanceUiState> = _uiState

    fun onSelfieCaptured(bitmap: Bitmap, file: File) {
        _uiState.update { it.copy(status = MarkAttendanceStatus.Processing) }
        viewModelScope.launch {
            when (val detection = faceRecognitionManager.detectSingleFace(bitmap)) {
                is FaceDetectionResult.NoFaceDetected -> fail(file, "No face detected. Please try again.")
                is FaceDetectionResult.MultipleFacesDetected ->
                    fail(file, "Multiple faces detected. Only one person should be in frame.")
                is FaceDetectionResult.Success -> matchAndRecord(detection.croppedFace, file)
            }
        }
    }

    private suspend fun matchAndRecord(croppedFace: Bitmap, file: File) {
        val enrolledStaff = staffRepository.getEnrolledEmbeddings()
        if (enrolledStaff.isEmpty()) {
            fail(file, "No staff have been enrolled yet. Contact your admin.")
            return
        }

        val embedding = faceRecognitionManager.embed(croppedFace)
        when (val match = faceRecognitionManager.findBestMatch(embedding, enrolledStaff)) {
            is MatchResult.Matched -> {
                val staff = staffRepository.getStaffOnce(match.staffId)
                if (staff == null) {
                    fail(file, "Matched staff record could not be loaded.")
                    return
                }
                val location = locationHelper.getCurrentLocation()
                attendanceRepository.recordAttendance(
                    staffId = staff.id,
                    date = DateTimeUtils.currentDate(),
                    time = DateTimeUtils.currentTime(),
                    selfiePath = file.absolutePath,
                    latitude = location?.latitude ?: 0.0,
                    longitude = location?.longitude ?: 0.0
                )
                _uiState.update { it.copy(status = MarkAttendanceStatus.Success(staff.name)) }
            }
            is MatchResult.NotMatched -> fail(file, "Face not recognised. Attendance was not recorded.")
        }
    }

    private fun fail(file: File, message: String) {
        file.delete()
        _uiState.update { it.copy(status = MarkAttendanceStatus.Failed(message)) }
    }

    fun retry() {
        _uiState.update { it.copy(status = MarkAttendanceStatus.Capturing) }
    }
}
