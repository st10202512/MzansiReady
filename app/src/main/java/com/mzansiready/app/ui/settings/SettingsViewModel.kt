package com.mzansiready.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansiready.app.data.repo.AuthRepository
import com.mzansiready.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loading: Boolean = false,
    val saved: Boolean = false,
    val language: String = "en",
    val error: String? = null
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState(language = session.language()))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun language(): String = session.language()

    fun theme(): String = session.theme()

    fun notificationsEnabled(): Boolean = session.notificationsEnabled()

    fun save(language: String, theme: String, notifications: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            authRepository.updateSettings(language, theme, notifications)
                .onSuccess { saved ->
                    // Persist to session immediately so the UI can read them
                    session.updateSettings(
                        saved.preferredLanguage,
                        saved.theme,
                        saved.notificationsEnabled
                    )
                    _state.update {
                        it.copy(
                            loading = false,
                            saved = true,
                            language = saved.preferredLanguage
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(loading = false, error = t.message ?: "Save failed")
                    }
                }
        }
    }

    fun consumeError() {
        _state.update { it.copy(error = null) }
    }

    fun consumeSaved() {
        _state.update { it.copy(saved = false) }
    }
}