package com.mzansiready.app.ui.auth

import android.content.Intent
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
import com.mzansiready.app.databinding.ActivityRegisterBinding
import com.mzansiready.app.ui.home.HomeActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo: AuthRepository = MzansiApp.from(this@RegisterActivity).authRepository
                return RegisterViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme BEFORE inflating layout
        applySavedTheme()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                binding.etFullName.text?.toString().orEmpty(),
                binding.etEmail.text?.toString().orEmpty(),
                binding.etPassword.text?.toString().orEmpty(),
                binding.etConfirm.text?.toString().orEmpty()
            )
        }

        binding.tvGoLogin.setOnClickListener { finish() }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progress.visibility =
                    if (state.loading) View.VISIBLE else View.GONE
                binding.btnRegister.isEnabled = !state.loading

                state.error?.let {
                    Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_LONG).show()
                    viewModel.consumeError()
                }
                if (state.success) {
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
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
}