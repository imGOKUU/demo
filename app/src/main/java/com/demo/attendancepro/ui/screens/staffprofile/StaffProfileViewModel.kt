package com.demo.attendancepro.ui.screens.staffprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.attendancepro.data.local.entity.AttendanceEntity
import com.demo.attendancepro.data.local.entity.StaffEntity
import com.demo.attendancepro.data.repository.AttendanceRepository
import com.demo.attendancepro.data.repository.StaffRepository
import com.demo.attendancepro.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StaffProfileUiState(
    val staff: StaffEntity? = null,
    val attendanceRecords: List<AttendanceEntity> = emptyList()
)

@HiltViewModel
class StaffProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    staffRepository: StaffRepository,
    attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val staffId: Long = checkNotNull(savedStateHandle[Screen.ARG_STAFF_ID])

    val uiState: StateFlow<StaffProfileUiState> = combine(
        staffRepository.getStaffById(staffId),
        attendanceRepository.getAttendanceForStaff(staffId)
    ) { staff, attendance ->
        StaffProfileUiState(staff = staff, attendanceRecords = attendance)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StaffProfileUiState())
}
