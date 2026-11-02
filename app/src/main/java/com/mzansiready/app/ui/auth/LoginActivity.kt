package com.mzansiready.app.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

    // ⚠️ Use the WEB Client ID (from google-services.json, client_type = 3)
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

    // ── Fallback launcher for legacy Google Sign-In (used when Credential Manager fails)
    private val legacySignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let {
                Log.d("LoginActivity", "Legacy sign-in succeeded, idToken received")
                firebaseAuthWithGoogle(it)
            } ?: run {
                Log.e("LoginActivity", "Legacy sign-in: no ID token")
                Toast.makeText(this, "No ID token received", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Legacy sign-in failed: code=${e.statusCode}", e)
            Toast.makeText(this, "Sign-In failed (code ${e.statusCode})", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        // Apply theme BEFORE super.onCreate so the window uses the right theme
        applySavedTheme()

        super.onCreate(savedInstanceState)

        val app = MzansiApp.from(this)
        if (app.session.isLoggedIn()) {
            goHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    requestNotificationPermission()
                    goHome()
                }
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

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        firebaseAuthWithGoogle(idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("LoginActivity", "Received an invalid google id token response", e)
                        Toast.makeText(this@LoginActivity, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: GetCredentialProviderConfigurationException) {
                // ── FALLBACK: Play Services not available on this device
                //    → use the legacy GoogleSignInClient API instead
                Log.w("LoginActivity", "Credential Manager provider missing, falling back to legacy sign-in", e)
                signInWithLegacyGoogle()
            } catch (e: GetCredentialException) {
                Log.e("LoginActivity", "Credential Manager error: type=${e.type} message=${e.message}", e)
                Toast.makeText(this@LoginActivity, "Google Sign-In cancelled: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Legacy Google Sign-In fallback (works on more devices)
    private fun signInWithLegacyGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        legacySignInLauncher.launch(client.signInIntent)
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
                Log.d("LoginActivity", "Notification permission granted")
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

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}