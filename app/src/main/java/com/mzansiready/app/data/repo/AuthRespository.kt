package com.mzansiready.app.data.repo

import android.util.Log
import com.mzansiready.app.data.remote.ApiService
import com.mzansiready.app.data.remote.dto.ApiError
import com.mzansiready.app.data.remote.dto.AuthResponse
import com.mzansiready.app.data.remote.dto.LoginRequest
import com.mzansiready.app.data.remote.dto.RegisterRequest
import com.mzansiready.app.data.remote.dto.SettingsDto
import com.mzansiready.app.util.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository(
    private val api: ApiService,
    private val session: SessionManager
) {

    private val gson = Gson()

    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): Result<AuthResponse> = call {
        api.register(RegisterRequest(fullName.trim(), email.trim().lowercase(), password))
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> = call {
        api.login(LoginRequest(email.trim().lowercase(), password))
    }

    suspend fun updateSettings(
        language: String,
        theme: String,
        notifications: Boolean
    ): Result<SettingsDto> = withContext(Dispatchers.IO) {
        try {
            val bearer = session.bearer()
            Log.d(TAG, "Sending settings update with bearer: $bearer")

            val response = api.updateSettings(
                bearer,
                SettingsDto(language, theme, notifications)
            )

            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response message: ${response.message()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                session.updateSettings(body.preferredLanguage, body.theme, body.notificationsEnabled)
                Result.success(body)
            } else {
                val errorMsg = parseError(response)
                Log.e(TAG, "Settings update failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Settings update exception", t)
            Result.failure(t)
        }
    }

    fun logout() = session.clear()

    private suspend fun call(
        block: suspend () -> Response<AuthResponse>
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                session.save(body)
                Result.success(body)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private fun parseError(response: Response<*>): String {
        val raw = response.errorBody()?.string()
        return try {
            gson.fromJson(raw, ApiError::class.java)?.message
                ?: "Request failed (${response.code()})"
        } catch (_: Exception) {
            "Request failed (${response.code()})"
        }
    }
}