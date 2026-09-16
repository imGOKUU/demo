package com.demo.attendancepro.ui.screens.login

import androidx.lifecycle.ViewModel
import com.demo.attendancepro.util.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val error: String? = null,
    val loggedInRole: Role? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val state = _uiState.value
        val role = when {
            state.username == "admin" && state.password == "admin" -> Role.ADMIN
            state.username == "staff" && state.password == "staff" -> Role.STAFF
            else -> null
        }
        if (role != null) {
            _uiState.update { it.copy(loggedInRole = role, error = null) }
        } else {
            _uiState.update { it.copy(error = "Invalid username or password") }
        }
    }

    fun consumeLoginEvent() {
        _uiState.update { it.copy(loggedInRole = null) }
    }
}
