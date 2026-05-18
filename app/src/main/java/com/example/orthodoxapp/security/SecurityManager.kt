package com.example.orthodoxapp.security

import com.example.orthodoxapp.data.model.User
import com.example.orthodoxapp.data.model.UserRole

/**
 * Ecclesiastical Security Manager (Production-Grade)
 * 
 * Handles permission-based access control for the church hierarchy.
 * Transitioned from governmental to church structure.
 */
object SecurityManager {

    private var authenticatedUser: User? = null
    private var authToken: String? = null
    private var prefsHelper: com.example.orthodoxapp.data.local.SharedPrefsHelper? = null

    fun initialize(context: android.content.Context) {
        prefsHelper = com.example.orthodoxapp.data.local.SharedPrefsHelper(context.applicationContext)
        this.authToken = prefsHelper?.getAuthToken()
    }

    fun getAuthToken(): String? = authToken

    fun authenticate(user: User, token: String) {
        this.authenticatedUser = user
        this.authToken = token
        
        prefsHelper?.saveAuthToken(token)
        user.id?.let { prefsHelper?.saveUserId(it) }
        user.roleId?.let { prefsHelper?.saveRoleId(it) }
    }

    fun logout() {
        this.authenticatedUser = null
        this.authToken = null
        prefsHelper?.clearAll()
    }

    fun isTokenValid(): Boolean = authToken != null

    fun getStoredRoleId(): Long {
        return prefsHelper?.getRoleId() ?: -1L
    }

    fun getStoredUserId(): Long {
        return prefsHelper?.getUserId() ?: -1L
    }

    enum class Action {
        CREATE, READ, UPDATE, DELETE, APPROVE, EXPORT, AUDIT
    }

    enum class Resource {
        FINANCIALS, USERS, DIOCESE, CHURCH, AUDIT_LOGS, REPORTING, SYSTEM_CONFIG
    }

    /**
     * Core Authorization Logic
     * Ensures Synod has full access, while Diocese and Church are scoped.
     */
    fun canPerform(role: UserRole, action: Action, resource: Resource): Boolean {
        // Synod Admin is Supreme
        if (role == UserRole.SYNOD_ADMIN) return true

        return when (resource) {
            Resource.FINANCIALS -> when (role) {
                UserRole.DIOCESE_ADMIN -> action in listOf(Action.READ, Action.APPROVE, Action.EXPORT)
                UserRole.CHURCH_ADMIN -> true
                UserRole.MEMBER -> action == Action.READ // Can only read own contributions
                else -> false
            }
            Resource.USERS -> when (role) {
                UserRole.CHURCH_ADMIN -> action in listOf(Action.CREATE, Action.READ, Action.UPDATE)
                UserRole.DIOCESE_ADMIN -> action == Action.READ
                else -> false
            }
            Resource.DIOCESE -> action == Action.READ // Only Synod can modify Diocese
            Resource.CHURCH -> when (role) {
                UserRole.DIOCESE_ADMIN -> action in listOf(Action.CREATE, Action.READ, Action.UPDATE)
                UserRole.CHURCH_ADMIN -> action in listOf(Action.READ, Action.UPDATE)
                else -> action == Action.READ
            }
            Resource.AUDIT_LOGS -> role == UserRole.SYNOD_ADMIN
            Resource.REPORTING -> role != UserRole.MEMBER
            Resource.SYSTEM_CONFIG -> false // Reserved for Synod
            else -> false
        } && (action != Action.DELETE || role == UserRole.SYNOD_ADMIN)
    }

    fun getCurrentRole(): UserRole? {
        val roleId = getStoredRoleId()
        if (roleId == -1L) return null
        return getRoleFromId(roleId)
    }

    fun enforcePermission(action: Action, resource: Resource) {
        val role = getCurrentRole() ?: throw SecurityException("User not authenticated.")
        if (!canPerform(role, action, resource)) {
            throw SecurityException("Access Denied: You do not have permission to perform this action.")
        }
    }

    /**
     * Scoped Access Verification
     * Ensures a Church Admin cannot access another church's data.
     */
    fun isWithinScope(user: User, targetChurchId: Long? = null, targetDioceseId: Long? = null): Boolean {
        val role = user.roleId?.let { getRoleFromId(it) } ?: return false

        if (role == UserRole.SYNOD_ADMIN) return true

        return when (role) {
            UserRole.DIOCESE_ADMIN -> user.dioceseId == targetDioceseId
            UserRole.CHURCH_ADMIN -> user.churchId == targetChurchId
            UserRole.MEMBER -> user.churchId == targetChurchId
            else -> false
        }
    }

    private fun getRoleFromId(id: Long): UserRole {
        return when (id) {
            1L -> UserRole.SYNOD_ADMIN
            2L -> UserRole.DIOCESE_ADMIN
            3L -> UserRole.CHURCH_ADMIN
            else -> UserRole.MEMBER
        }
    }
}
