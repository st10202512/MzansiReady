package com.mzansiready.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun validEmail_isAccepted() {
        assertTrue(Validators.isValidEmail("user@example.com"))
    }

    @Test
    fun invalidEmail_isRejected() {
        assertFalse(Validators.isValidEmail("not-an-email"))
        assertFalse(Validators.isValidEmail(""))
    }

    @Test
    fun strongPassword_isAccepted() {
        assertTrue(Validators.isValidPassword("Password123"))
    }

    @Test
    fun weakPassword_isRejected() {
        assertFalse(Validators.isValidPassword("password"))
    }

    @Test
    fun matchingPasswords_areAccepted() {
        assertTrue(Validators.passwordsMatch("same", "same"))
    }

    @Test
    fun mismatchedPasswords_areRejected() {
        assertFalse(Validators.passwordsMatch("abc", "xyz"))
    }
}
