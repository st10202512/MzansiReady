package com.mzansiready.app.ui.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mzansiready.app.MzansiApp
import com.mzansiready.app.R
import com.mzansiready.app.data.repo.AuthRepository
import com.mzansiready.app.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = MzansiApp.from(this@SettingsActivity)
                val repo: AuthRepository = app.authRepository
                return SettingsViewModel(repo, app.session) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme BEFORE inflating layout
        applySavedTheme()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load current settings into UI
        binding.switchNotifications.isChecked = viewModel.notificationsEnabled()
        binding.radioEnglish.isChecked = viewModel.language() == "en"
        binding.radioIsiZulu.isChecked = viewModel.language() == "zu"
        binding.radioLight.isChecked = viewModel.theme() == "light"
        binding.radioDark.isChecked = viewModel.theme() == "dark"

        binding.btnSave.setOnClickListener {
            viewModel.save(
                language = if (binding.radioIsiZulu.isChecked) "zu" else "en",
                theme = if (binding.radioDark.isChecked) "dark" else "light",
                notifications = binding.switchNotifications.isChecked
            )
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progress.visibility =
                    if (state.loading) View.VISIBLE else View.GONE

                state.error?.let {
                    Toast.makeText(this@SettingsActivity, it, Toast.LENGTH_LONG).show()
                    viewModel.consumeError()
                }
                if (state.saved) {
                    Toast.makeText(this@SettingsActivity, "Settings saved", Toast.LENGTH_SHORT).show()
                    viewModel.consumeSaved()

                    // Apply language immediately
                    applyLanguage(state.language)

                    // Recreate the activity so the new theme takes effect
                    recreate()
                }
            }
        }
    }

    private fun applySavedTheme() {
        val savedTheme = MzansiApp.from(this).session.theme()
        when (savedTheme) {
            "dark" -> setTheme(R.style.Theme_MzansiReady_Dark)
            else -> setTheme(R.style.Theme_MzansiReady)
        }
    }

    private fun applyLanguage(code: String) {
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}