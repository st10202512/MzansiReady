package com.mzansiready.app.util

object Validators {

    private val EMAIL_PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidFullName(name: String): Boolean = name.trim().length >= 2

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && EMAIL_PATTERN.matches(email.trim())

    fun isValidPassword(password: String): Boolean =
        password.length >= 8 && password.any { it.isDigit() } && password.any { it.isUpperCase() }

    fun passwordsMatch(password: String, confirmation: String): Boolean =
        password.isNotEmpty() && password == confirmation

    fun isValidLatitude(value: Double): Boolean = value in -90.0..90.0

    fun isValidLongitude(value: Double): Boolean = value in -180.0..180.0
}