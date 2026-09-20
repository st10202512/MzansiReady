package com.mzansiready.app.util

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mzansiready.app.data.remote.dto.AuthResponse

class SessionManager(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(auth: AuthResponse) {
        prefs.edit()
            .putString(KEY_TOKEN, auth.token)
            .putInt(KEY_USER_ID, auth.userId)
            .putString(KEY_NAME, auth.fullName)
            .putString(KEY_EMAIL, auth.email)
            .putString(KEY_LANGUAGE, auth.preferredLanguage)
            .putString(KEY_THEME, auth.theme)
            .putBoolean(KEY_NOTIFICATIONS, auth.notificationsEnabled)
            .apply()
        Log.d(TAG, "Session saved for user ${auth.userId}")
    }

    fun token(): String? = prefs.getString(KEY_TOKEN, null)
    fun bearer(): String = "Bearer ${token().orEmpty()}"
    fun isLoggedIn(): Boolean = !token().isNullOrBlank()
    fun fullName(): String = prefs.getString(KEY_NAME, "") ?: ""
    fun language(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    fun theme(): String = prefs.getString(KEY_THEME, "light") ?: "light"
    fun notificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS, true)

    fun updateSettings(language: String, theme: String, notifications: Boolean) {
        prefs.edit()
            .putString(KEY_LANGUAGE, language)
            .putString(KEY_THEME, theme)
            .putBoolean(KEY_NOTIFICATIONS, notifications)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val TAG = "SessionManager"
        const val FILE_NAME = "mzansi_session"
        const val KEY_TOKEN = "token"
        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "full_name"
        const val KEY_EMAIL = "email"
        const val KEY_LANGUAGE = "language"
        const val KEY_THEME = "theme"
        const val KEY_NOTIFICATIONS = "notifications"
    }
}