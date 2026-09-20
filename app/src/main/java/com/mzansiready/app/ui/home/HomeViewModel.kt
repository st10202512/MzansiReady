package com.mzansiready.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansiready.app.data.local.WeatherCacheEntity
import com.mzansiready.app.data.repo.LocationRepository
import com.mzansiready.app.data.repo.WeatherRepository
import com.mzansiready.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val weather: WeatherCacheEntity? = null,
    val error: String? = null
)

class HomeViewModel(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    // Johannesburg by default
    private val defaultKey = "home"
    private val defaultName = "Johannesburg"
    private val defaultLat = -26.2041
    private val defaultLng = 28.0473

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            // Sync pending locations in the background (best-effort)
            try {
                locationRepository.syncPending()
            } catch (_: Exception) {
                // Ignore — weather is more important
            }

            weatherRepository.getWeather(
                defaultKey, defaultName, defaultLat, defaultLng
            )
                .onSuccess { w ->
                    _state.update { it.copy(loading = false, weather = w, error = null) }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = t.message ?: "Could not load weather"
                        )
                    }
                }
        }
    }
}