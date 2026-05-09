package com.example.orthodoxapp.util

import android.util.Log
import com.example.orthodoxapp.data.local.dao.FinanceDao
import com.example.orthodoxapp.data.model.AuditLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Production Error Handler & Analytics Interceptor
 */
object ErrorHandler {
    
    private var dao: FinanceDao? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(financeDao: FinanceDao) {
        this.dao = financeDao
    }

    fun logError(userId: Long?, action: String, error: Throwable, details: String? = null) {
        val errorMessage = error.message ?: "Unknown error"
        Log.e("EcclesiasticalError", "Error during $action: $errorMessage", error)

        // Log to database for remote auditing
        scope.launch {
            try {
                dao?.insertAuditLog(AuditLog(
                    userId = userId,
                    action = "CRITICAL_ERROR",
                    details = "Action: $action | Error: $errorMessage | Context: $details",
                    timestamp = System.currentTimeMillis()
                ))
            } catch (e: Exception) {
                Log.e("ErrorHandler", "Failed to log error to DB", e)
            }
        }
    }

    fun logSecurityViolation(userId: Long?, resource: String, details: String) {
        Log.w("SecurityViolation", "User $userId attempted unauthorized access to $resource")
        scope.launch {
            dao?.insertAuditLog(AuditLog(
                userId = userId,
                action = "SECURITY_VIOLATION",
                details = "Resource: $resource | Details: $details",
                timestamp = System.currentTimeMillis()
            ))
        }
    }
}
