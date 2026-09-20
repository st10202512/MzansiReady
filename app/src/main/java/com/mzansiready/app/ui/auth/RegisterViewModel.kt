package com.mzansiready.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansiready.app.data.repo.AuthRepository
import com.mzansiready.app.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun register(fullName: String, email: String, password: String, confirm: String) {
        when {
            !Validators.isValidFullName(fullName) ->
                return error("Full name must be at least 2 characters")
            !Validators.isValidEmail(email) ->
                return error("Please enter a valid email address")
            !Validators.isValidPassword(password) ->
                return error("Password must be 8+ chars with uppercase and a digit")
            !Validators.passwordsMatch(password, confirm) ->
                return error("Passwords do not match")
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repository.register(fullName, email, password)
                .onSuccess { _state.update { it.copy(loading = false, success = true) } }
                .onFailure { t ->
                    _state.update { it.copy(loading = false, error = t.message ?: "Registration failed") }
                }
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }

    private fun error(message: String) {
        _state.update { it.copy(error = message) }
    }
}