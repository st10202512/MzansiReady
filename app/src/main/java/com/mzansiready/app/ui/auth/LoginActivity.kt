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
import com.mzansiready.app.databinding.ActivityLoginBinding
import com.mzansiready.app.ui.home.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo: AuthRepository = MzansiApp.from(this@LoginActivity).authRepository
                return LoginViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme BEFORE inflating layout
        applySavedTheme()

        val app = MzansiApp.from(this)
        if (app.session.isLoggedIn()) {
            goHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etEmail.text?.toString().orEmpty(),
                binding.etPassword.text?.toString().orEmpty()
            )
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progress.visibility =
                    if (state.loading) View.VISIBLE else View.GONE
                binding.btnLogin.isEnabled = !state.loading

                state.error?.let { msg ->
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                    viewModel.consumeError()
                }
                if (state.success) goHome()
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

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}