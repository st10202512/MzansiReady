package com.mzansiready.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mzansiready.app.MzansiApp
import com.mzansiready.app.R
import com.mzansiready.app.data.repo.LocationRepository
import com.mzansiready.app.data.repo.WeatherRepository
import com.mzansiready.app.databinding.ActivityHomeBinding
import com.mzansiready.app.ui.auth.LoginActivity
import com.mzansiready.app.ui.settings.SettingsActivity
import com.mzansiready.app.util.WeatherCodes
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = MzansiApp.from(this@HomeActivity)
                val weather: WeatherRepository = app.weatherRepository
                val locations: LocationRepository = app.locationRepository
                return HomeViewModel(weather, locations, app.session) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applySavedTheme()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = MzansiApp.from(this)
        binding.tvGreeting.text = "Hello, ${app.session.fullName()}!"

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            app.authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progress.visibility =
                    if (state.loading) View.VISIBLE else View.GONE
                binding.swipeRefresh.isRefreshing = false

                // Always show weather (either live, cached, or placeholder)
                state.weather?.let { w ->
                    binding.tvLocation.text = w.locationName
                    binding.tvTemp.text = "${w.temperature.toInt()}°C"
                    binding.tvCondition.text = "${WeatherCodes.emoji(w.weatherCode)} ${w.condition}"
                    binding.tvMinMax.text = "Min ${w.minTemperature.toInt()}°  Max ${w.maxTemperature.toInt()}°"
                    binding.tvRain.text = "Rain chance ${w.rainProbability}%"
                } ?: run {
                    // No weather at all — show friendly message
                    binding.tvLocation.text = "—"
                    binding.tvTemp.text = "—°C"
                    binding.tvCondition.text = state.error ?: "Loading weather..."
                    binding.tvMinMax.text = ""
                    binding.tvRain.text = ""
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun applySavedTheme() {
        val savedTheme = MzansiApp.from(this).session.theme()
        when (savedTheme) {
            "dark" -> setTheme(R.style.Theme_MzansiReady_Dark)
            else -> setTheme(R.style.Theme_MzansiReady)
        }
    }
}