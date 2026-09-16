package com.demo.attendancepro.ui.screens.staffslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.attendancepro.data.local.entity.StaffEntity
import com.demo.attendancepro.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StaffListViewModel @Inject constructor(
    staffRepository: StaffRepository
) : ViewModel() {

    val staffList: StateFlow<List<StaffEntity>> = staffRepository.getAllStaff()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
