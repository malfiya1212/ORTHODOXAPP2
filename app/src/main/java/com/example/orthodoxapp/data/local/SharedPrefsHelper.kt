package com.example.orthodoxapp.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Shared Preferences Helper
 * A lightweight mechanism to store small amounts of primitive data as key-value pairs.
 * It persists data across app sessions (even after app close or device restart).
 */
class SharedPrefsHelper(context: Context) {

    // The SharedPreferences file specifically for this app
    private val prefs: SharedPreferences = context.getSharedPreferences("OrthodoxAppPrefs", Context.MODE_PRIVATE)

    // Unique string identifiers (keys) for the values
    companion object {
        private const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_ROLE_ID = "ROLE_ID"
        private const val KEY_IS_DARK_MODE = "IS_DARK_MODE"
    }

    // --- Saving & Retrieving String type (Auth Token) ---
    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearAuthToken() {
        prefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    // --- Saving & Retrieving Long type (User Data) ---
    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L) // Returns -1 if no user is found
    }

    fun saveRoleId(roleId: Long) {
        prefs.edit().putLong(KEY_ROLE_ID, roleId).apply()
    }

    fun getRoleId(): Long {
        return prefs.getLong(KEY_ROLE_ID, -1L)
    }

    // --- Saving & Retrieving Boolean type (App Settings) ---
    fun saveDarkModePref(isDarkMode: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDarkMode).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_IS_DARK_MODE, false) // Default is light mode (false)
    }

    fun clearAll() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_ROLE_ID)
            .apply()
    }
}
