package com.mzansiready.app.data.remote.dto

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val userId: Int,
    val fullName: String,
    val email: String,
    val preferredLanguage: String,
    val theme: String,
    val notificationsEnabled: Boolean
)

data class ApiError(val message: String?)

data class SettingsDto(
    val preferredLanguage: String,
    val theme: String,
    val notificationsEnabled: Boolean
)

data class LocationDto(
    val locationId: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class CreateLocationRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class AlertDto(
    val alertId: Int,
    val title: String,
    val description: String,
    val category: String,
    val area: String
)