package com.example.orthodoxapp.util

/**
 * Production Financial Validation Engine
 */
object Validator {

    fun validateAmount(amount: Double): ValidationResult {
        return if (amount <= 0) {
            ValidationResult.Error("Amount must be greater than zero.")
        } else if (amount > 10_000_000) {
            ValidationResult.Error("Amount exceeds maximum transaction limit.")
        } else {
            ValidationResult.Success
        }
    }

    fun validateEmail(email: String): ValidationResult {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return if (email.isBlank()) {
            ValidationResult.Error("Email cannot be empty.")
        } else if (!email.matches(emailRegex)) {
            ValidationResult.Error("Invalid email format.")
        } else {
            ValidationResult.Success
        }
    }

    fun validateDescription(desc: String): ValidationResult {
        return if (desc.isBlank()) {
            ValidationResult.Error("Description is required.")
        } else if (desc.length < 3) {
            ValidationResult.Error("Description is too short.")
        } else if (desc.length > 500) {
            ValidationResult.Error("Description exceeds 500 characters.")
        } else {
            ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    val isSuccess: Boolean get() = this is Success
}
