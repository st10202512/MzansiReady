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

data class LoginUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (!Validators.isValidEmail(email)) {
            _state.update { it.copy(error = "Please enter a valid email address") }
            return
        }
        if (password.isEmpty()) {
            _state.update { it.copy(error = "Password is required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repository.login(email, password)
                .onSuccess {
                    _state.update { it.copy(loading = false, success = true) }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(loading = false, error = t.message ?: "Login failed")
                    }
                }
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }
}