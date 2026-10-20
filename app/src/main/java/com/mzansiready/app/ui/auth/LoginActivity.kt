package com.mzansiready.app.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mzansiready.app.MzansiApp
import com.mzansiready.app.R
import com.mzansiready.app.data.repo.AuthRepository
import com.mzansiready.app.databinding.ActivityLoginBinding
import com.mzansiready.app.ui.home.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var firebaseAuth: FirebaseAuth

    // Your Web Client ID from Google Cloud Console
    private val webClientId = "47253500490-l27i29j8mgcacvkjuc3djstoornhicb6.apps.googleusercontent.com"

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
        // ✅ INSTALL THE SPLASH SCREEN BEFORE super.onCreate()
        installSplashScreen()

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

        // Initialize Firebase and Credential Manager
        firebaseAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etEmail.text?.toString().orEmpty(),
                binding.etPassword.text?.toString().orEmpty()
            )
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // --- GOOGLE SIGN-IN BUTTON ---
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
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
                if (state.success) {
                    // Request notification permission BEFORE going home
                    requestNotificationPermission()
                    goHome()
                }
            }
        }
    }

    // --- REQUEST NOTIFICATION PERMISSION ---
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

    // --- HANDLE PERMISSION RESULT ---
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LoginActivity", "Notification permission granted")
            } else {
                Log.w("LoginActivity", "Notification permission denied")
            }
        }
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        firebaseAuthWithGoogle(idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("LoginActivity", "Received an invalid google id token response", e)
                        Toast.makeText(this@LoginActivity, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: GetCredentialException) {
                Log.e("LoginActivity", "Credential Manager error", e)
                Toast.makeText(this@LoginActivity, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Log.d("LoginActivity", "Firebase sign-in successful: ${user?.email}")

                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val firebaseIdToken = tokenTask.result?.token
                            if (firebaseIdToken != null) {
                                viewModel.loginWithGoogle(firebaseIdToken)
                            } else {
                                Log.e("LoginActivity", "Firebase ID token was null")
                                Toast.makeText(this@LoginActivity, "Failed to get Google token", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.w("LoginActivity", "Failed to get ID token", tokenTask.exception)
                            Toast.makeText(this@LoginActivity, "Failed to get Google token", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.w("LoginActivity", "Firebase sign-in failed", task.exception)
                    Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
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