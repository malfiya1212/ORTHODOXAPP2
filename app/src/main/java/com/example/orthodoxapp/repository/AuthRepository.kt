package com.example.orthodoxapp.repository

import com.example.orthodoxapp.data.network.ApiService
import com.example.orthodoxapp.data.local.dao.UserDao
import com.example.orthodoxapp.data.model.User
import com.example.orthodoxapp.security.SecurityManager
import com.example.orthodoxapp.security.PasswordHasher
import com.example.orthodoxapp.data.network.LoginRequest
import com.example.orthodoxapp.data.network.AuthResponse

class AuthRepository(
    private val userDao: UserDao,
    private val api: ApiService
) {
    suspend fun login(email: String, passwordRaw: String): Result<AuthResponse> {
        return try {
            val passwordHash = PasswordHasher.hashPassword(passwordRaw)
            val response = api.login(LoginRequest(email, passwordHash))
            if (response.isSuccessful) {
                response.body()?.let { 
                    // In production, we'd map UserDto to User entity here
                    // For now, we update the SecurityManager with the session
                    Result.success(it) 
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            com.example.orthodoxapp.util.ErrorHandler.logError(null, "LOGIN_ATTEMPT", e, email)
            Result.failure(e)
        }
    }
}
