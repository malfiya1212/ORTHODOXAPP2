package com.example.orthodoxapp

import com.example.orthodoxapp.util.ValidationUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Professional Software Engineering Unit Tests for Tewahedo Connect.
 */
class ValidationUnitTest {

    @Test
    fun email_isCorrect() {
        assertTrue(ValidationUtils.isValidEmail("test@church.com"))
        assertFalse(ValidationUtils.isValidEmail("invalid-email"))
    }

    @Test
    fun amount_isCorrect() {
        assertTrue(ValidationUtils.isValidAmount("100.50"))
        assertFalse(ValidationUtils.isValidAmount("-10"))
        assertFalse(ValidationUtils.isValidAmount("abc"))
    }

    @Test
    fun name_isCorrect() {
        assertTrue(ValidationUtils.isValidName("Abba Gebre"))
        assertFalse(ValidationUtils.isValidName("Ab"))
    }
}
