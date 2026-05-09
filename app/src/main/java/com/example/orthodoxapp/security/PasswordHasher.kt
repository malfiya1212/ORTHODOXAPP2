package com.example.orthodoxapp.security

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Enterprise Password Hashing Utility
 * Provides secure hashing for passwords before transmission to the backend or local storage.
 * Standard: SHA-256 (Secure Hash Algorithm 256-bit)
 */
object PasswordHasher {

    /**
     * Hashes a plain-text password into a hex string using SHA-256.
     */
    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray())
            
            // Convert byte array to Hex String
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            // Fallback to plain text (extremely unlikely in Android)
            password
        }
    }

    /**
     * Verifies if a plain password matches a stored hash.
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        val newHash = hashPassword(password)
        return newHash.equals(hash, ignoreCase = true)
    }
}
