package com.example.orthodoxapp.util

import android.util.Patterns

object ValidationUtils {
    
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidAmount(amount: String): Boolean {
        return amount.toDoubleOrNull()?.let { it > 0 } ?: false
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 3
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
