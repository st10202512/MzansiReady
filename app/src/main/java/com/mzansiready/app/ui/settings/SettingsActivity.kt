package com.mzansiready.app.ui.settings

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mzansiready.app.MzansiApp
import com.mzansiready.app.R
import com.mzansiready.app.data.repo.AuthRepository
import com.mzansiready.app.databinding.ActivitySettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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

    // Pick image from gallery
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadProfilePictureToFirestore(uri)
        } else {
            Log.d("SettingsActivity", "No image selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Apply theme BEFORE super.onCreate so the window uses the right theme
        applySavedTheme()

        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Load current settings
        binding.switchNotifications.isChecked = viewModel.notificationsEnabled()

        when (viewModel.language()) {
            "zu" -> binding.radioIsiZulu.isChecked = true
            "xh" -> binding.radioIsiXhosa.isChecked = true
            "af" -> binding.radioAfrikaans.isChecked = true
            else -> binding.radioEnglish.isChecked = true
        }

        if (viewModel.theme() == "dark") {
            binding.radioDark.isChecked = true
        } else {
            binding.radioLight.isChecked = true
        }

        // 3. Save button — reads all four language radio buttons
        binding.btnSave.setOnClickListener {
            val selectedLanguage = when {
                binding.radioIsiZulu.isChecked   -> "zu"
                binding.radioIsiXhosa.isChecked  -> "xh"
                binding.radioAfrikaans.isChecked -> "af"
                else                             -> "en"
            }
            val selectedTheme = if (binding.radioDark.isChecked) "dark" else "light"

            viewModel.save(
                language = selectedLanguage,
                theme = selectedTheme,
                notifications = binding.switchNotifications.isChecked
            )
        }

        // 4. Upload profile picture
        binding.btnUploadPic.setOnClickListener {
            pickImage.launch("image/*")
        }

        // 5. Observe save state
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

                    // Apply language and theme app-wide, then recreate so
                    // the labels and colours refresh immediately.
                    applyLanguage(state.language)
                    applyTheme(viewModel.theme())
                    recreate()
                }
            }
        }
    }

    // ==========================================
    // Apply theme via AppCompatDelegate so all
    // activities switch together.
    // ==========================================
    private fun applySavedTheme() {
        val saved = MzansiApp.from(this).session.theme()
        applyTheme(saved)
    }

    private fun applyTheme(theme: String) {
        AppCompatDelegate.setDefaultNightMode(
            if (theme == "dark") AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    // ==========================================
    // Apply language via AppCompatDelegate so
    // every screen picks up the new locale.
    // ==========================================
    private fun applyLanguage(code: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(code)
        )
    }

    // ==========================================
    // Save profile picture to Firestore (Base64)
    // ==========================================
    private fun uploadProfilePictureToFirestore(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Please sign in with Google first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    Toast.makeText(this@SettingsActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                    binding.progress.visibility = View.GONE
                    return@launch
                }

                val resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
                val outputStream = ByteArrayOutputStream()
                resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

                val bytes = outputStream.toByteArray()
                val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

                withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    val data = mapOf(
                        "profilePictureBase64" to base64Image,
                        "updatedAt" to System.currentTimeMillis()
                    )

                    db.collection("user_profiles")
                        .document(user.uid)
                        .set(data)
                        .addOnSuccessListener {
                            Log.d("SettingsActivity", "Profile picture saved to Firestore")
                            Toast.makeText(this@SettingsActivity, "Profile picture saved!", Toast.LENGTH_SHORT).show()
                            binding.progress.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            Log.e("SettingsActivity", "Firestore save failed", e)
                            Toast.makeText(this@SettingsActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                            binding.progress.visibility = View.GONE
                        }
                }

            } catch (e: Exception) {
                Log.e("SettingsActivity", "Upload error", e)
                Toast.makeText(this@SettingsActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                binding.progress.visibility = View.GONE
            }
        }
    }

    // ==========================================
    // Load profile picture from Firestore (if needed)
    // ==========================================
    private fun loadProfilePictureFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("user_profiles")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val base64 = document.getString("profilePictureBase64")
                    if (!base64.isNullOrEmpty()) {
                        val bytes = Base64.decode(base64, Base64.NO_WRAP)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        Log.d("SettingsActivity", "Profile picture loaded: ${bitmap?.width}x${bitmap?.height}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SettingsActivity", "Failed to load profile picture", e)
            }
    }
}