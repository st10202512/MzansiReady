package com.mzansiready.app.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mzansiready.app.MzansiApp
import com.mzansiready.app.R
import com.mzansiready.app.data.repo.LocationRepository
import com.mzansiready.app.data.repo.WeatherRepository
import com.mzansiready.app.databinding.ActivityHomeBinding
import com.mzansiready.app.ui.auth.LoginActivity
import com.mzansiready.app.ui.feed.CommunityFeedActivity
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
        // Apply saved theme BEFORE super.onCreate
        applySavedTheme()

        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request notification permission on Android 13+
        requestNotificationPermission()

        val app = MzansiApp.from(this)
        binding.tvGreeting.text = "Hello, ${app.session.fullName()}!"

        // Settings button
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Community Feed button
        binding.btnCommunityFeed.setOnClickListener {
            startActivity(Intent(this, CommunityFeedActivity::class.java))
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            app.authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        // Pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        // Observe weather state
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progress.visibility =
                    if (state.loading) View.VISIBLE else View.GONE
                binding.swipeRefresh.isRefreshing = false

                state.weather?.let { w ->
                    binding.tvLocation.text = w.locationName
                    binding.tvTemp.text = "${w.temperature.toInt()}°C"
                    binding.tvCondition.text = "${WeatherCodes.emoji(w.weatherCode)} ${w.condition}"
                    binding.tvMinMax.text = "Min ${w.minTemperature.toInt()}°  Max ${w.maxTemperature.toInt()}°"
                    binding.tvRain.text = "Rain chance ${w.rainProbability}%"
                } ?: run {
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("HomeActivity", "Notification permission granted")
            } else {
                android.util.Log.w("HomeActivity", "Notification permission denied")
            }
        }
    }

    // ✅ FIX: use AppCompatDelegate so the whole app switches themes
    private fun applySavedTheme() {
        val savedTheme = MzansiApp.from(this).session.theme()
        AppCompatDelegate.setDefaultNightMode(
            if (savedTheme == "dark") AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}