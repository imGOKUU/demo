package com.demo.attendancepro.ui.screens.addstaff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.attendancepro.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddStaffUiState(
    val name: String = "",
    val employeeId: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val createdStaffId: Long? = null
)

@HiltViewModel
class AddStaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddStaffUiState())
    val uiState: StateFlow<AddStaffUiState> = _uiState

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, error = null) }
    }

    fun onEmployeeIdChange(value: String) {
        _uiState.update { it.copy(employeeId = value, error = null) }
    }

    fun saveAndContinue() {
        val state = _uiState.value
        if (state.isSaving) return
        if (state.name.isBlank() || state.employeeId.isBlank()) {
            _uiState.update { it.copy(error = "Name and Employee ID are required") }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val id = staffRepository.addStaff(state.name.trim(), state.employeeId.trim())
            _uiState.update { it.copy(isSaving = false, createdStaffId = id) }
        }
    }
}