package com.example.orthodoxapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.orthodoxapp.security.SignalRManager
import com.example.orthodoxapp.security.SecurityManager
import com.example.orthodoxapp.security.PasswordHasher
import com.example.orthodoxapp.data.network.PaymentInitResponse
import com.example.orthodoxapp.data.network.PaymentVerificationResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class ActionState {
    object Idle : ActionState()
    object Loading : ActionState()
    object Success : ActionState()
    data class Error(val message: String) : ActionState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("SpellCheckingInspection")
class FinancialViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState

    fun resetActionState() { _actionState.value = ActionState.Idle }

    val currentUser = MutableStateFlow<User?>(null)
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    val currentRole = MutableStateFlow(UserRole.MEMBER)
    val isDarkMode = MutableStateFlow(false)
    val currentLanguage = MutableStateFlow(com.example.orthodoxapp.util.Language.ENGLISH)
    
    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
    }

    fun toggleLanguage() {
        currentLanguage.value = if (currentLanguage.value == com.example.orthodoxapp.util.Language.ENGLISH) 
            com.example.orthodoxapp.util.Language.AMHARIC 
        else 
            com.example.orthodoxapp.util.Language.ENGLISH
    }

    private val _otpState = MutableStateFlow<String?>(null)
    private val _isOtpVerified = MutableStateFlow(false)
    val isOtpVerified: StateFlow<Boolean> = _isOtpVerified

    fun sendOtpToEmail(email: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val otp = (100000..999999).random().toString()
            _otpState.value = otp
            val success = com.example.orthodoxapp.util.EmailUtility.sendOtpEmail(email, otp)
            if (success) {
                _actionState.value = ActionState.Idle
            } else {
                _actionState.value = ActionState.Error("Failed to send email. Check your connection.")
            }
            onComplete(success)
        }
    }

    fun verifyOtp(inputOtp: String): Boolean {
        // Master override "123456" for testing/emergency connectivity issues
        return if (inputOtp == _otpState.value || inputOtp == "123456") {
            _isOtpVerified.value = true
            true
        } else {
            false
        }
    }
    

        
    val groups: StateFlow<List<ChureGroup>> = repository.allChureGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        

    val accounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val transactions: StateFlow<List<FinancialTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<Employee>> = combine(currentUser, currentRole) { user, role ->
        user?.churchId?.let { repository.getEmployeesByChurch(it) } ?: flowOf(emptyList())
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salaries: StateFlow<List<Salary>> = combine(currentUser, currentRole) { user, role ->
        user?.churchId?.let { repository.getSalariesByChurch(it) } ?: flowOf(emptyList())
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assets: StateFlow<List<ChurchAsset>> = combine(currentUser, currentRole) { user, role ->
        when (role) {
            UserRole.SYNOD_ADMIN, UserRole.AUDITOR -> repository.allAssets
            else -> user?.churchId?.let { repository.getAssetsByChurch(it) } ?: flowOf(emptyList())
        }
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<Notification>> = currentUser.flatMapLatest { user ->
        user?.id?.let { repository.getNotificationsByUser(it) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAsset(name: String, type: String, value: Double, churchId: Long, description: String? = null) {
        if (name.length < 3) {
            _actionState.value = ActionState.Error("Asset name is too short")
            return
        }
        if (value <= 0) {
            _actionState.value = ActionState.Error("Valuation must be greater than zero")
            return
        }

        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            try {
                repository.addChurchAsset(name, type, value, churchId, description)
                _actionState.value = ActionState.Success
            } catch (e: Exception) {
                _actionState.value = ActionState.Error(e.message ?: "Failed to register asset")
            }
        }
    }




    init {
        seedInitialData()
        restoreSession()
    }

    private fun restoreSession() {
        val role = com.example.orthodoxapp.security.SecurityManager.getCurrentRole()
        if (role != null) {
            currentRole.value = role
        }
        val userId = com.example.orthodoxapp.security.SecurityManager.getStoredUserId()
        if (userId != -1L) {
            viewModelScope.launch {
                try {
                    repository.allUsers.first().find { it.id == userId }?.let {
                        currentUser.value = it
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun login(email: String, passwordRaw: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val normalizedEmail = email.trim().lowercase()
            val trimmedPassword = passwordRaw.trim()

            // 1. Master Bypass Logic
            val emailCheck = normalizedEmail.replace(" ", "")
            val passCheck = trimmedPassword.lowercase().replace("@", "")
            
            val fallbackRole = when {
                emailCheck == "superadmin@church.com" && passCheck == "super123" -> "synod_admin"
                emailCheck == "diocese@church.com" && passCheck == "diocese123" -> "diocese_admin"
                emailCheck == "church@church.com" && passCheck == "church123" -> "church_admin"
                emailCheck == "accountant@church.com" && passCheck == "accountant123" -> "accountant"
                emailCheck == "auditor@church.com" && passCheck == "auditor123" -> "auditor"
                else -> null
            }

            if (fallbackRole != null) {
                val roleId = when(fallbackRole) {
                    "synod_admin" -> 1L
                    "diocese_admin" -> 2L
                    "church_admin" -> 3L
                    "accountant" -> 4L
                    "auditor" -> 5L
                    else -> 6L
                }

                val seededUser = try { repository.getUserByEmail(normalizedEmail) } catch (_: Exception) { null }
                val user = seededUser ?: User(
                    id = roleId,
                    name = when(fallbackRole) {
                        "synod_admin" -> "Synod Admin"
                        "diocese_admin" -> "Diocese Admin"
                        "church_admin" -> "Church Administrator"
                        "accountant" -> "Accountant"
                        "auditor" -> "Auditor"
                        else -> "Member"
                    },
                    email = normalizedEmail,
                    roleId = roleId,
                    passwordHash = PasswordHasher.hashPassword(trimmedPassword),
                    dioceseId = if (roleId == 2L) 1L else null,
                    churchId = if (roleId in 3L..4L) 1L else null
                )

                try { repository.registerUser(user) } catch (_: Exception) {}
                loginWithUser(user)
                return@launch
            }

            // 2. DB Check
            try {
                val dbUser = repository.getUserByEmail(normalizedEmail)
                if (dbUser != null) {
                    val hashedInput = PasswordHasher.hashPassword(trimmedPassword)
                    if (dbUser.passwordHash != hashedInput) {
                        _loginState.value = LoginState.Error("Invalid password")
                        return@launch
                    }
                    loginWithUser(dbUser)
                    return@launch
                }
            } catch (_: Exception) {}

            // 3. API Login
            try {
                val result = repository.login(normalizedEmail, trimmedPassword)
                if (result.isSuccess) {
                    loginWithUser(result.getOrThrow().user)
                } else {
                    _loginState.value = LoginState.Error("Account not found.")
                }
            } catch (_: Exception) {
                _loginState.value = LoginState.Error("Network error.")
            }
        }
    }

    fun register(
        name: String, email: String, passwordRaw: String, roleName: String,
        churchId: Long? = null, customChurchName: String? = null
    ) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val passwordHash = PasswordHasher.hashPassword(passwordRaw)
            val user = User(
                name = name,
                email = email.trim().lowercase(),
                passwordHash = passwordHash,
                churchId = churchId,
                customChurchName = customChurchName,
                role = roleName.uppercase(),
                roleId = 6L // Member role ID
            )
            
            try {
                val registeredId = repository.registerUser(user)
                loginWithUser(user.copy(id = registeredId))
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Registration failed: ${e.message}")
            }
        }
    }

    private fun loginWithUser(user: User) {
        currentUser.value = user
        currentRole.value = user.roleId?.let { getRoleFromId(it) } ?: UserRole.MEMBER
        SecurityManager.authenticate(user, "eyJmock_token_${user.email}")
        val roleString = when (user.roleId) {
            1L -> "synod_admin"
            2L -> "diocese_admin"
            3L -> "church_admin"
            4L -> "accountant"
            5L -> "auditor"
            else -> "member"
        }
        setRole(roleString)
        _loginState.value = LoginState.Success(roleString)
    }

    fun setRole(roleString: String) {
        val newRole = when (roleString) {
            "synod_admin" -> UserRole.SYNOD_ADMIN
            "diocese_admin" -> UserRole.DIOCESE_ADMIN
            "church_admin" -> UserRole.CHURCH_ADMIN
            "accountant" -> UserRole.ACCOUNTANT
            "auditor" -> UserRole.AUDITOR
            else -> UserRole.MEMBER
        }
        currentRole.value = newRole
    }

    fun logout() {
        SecurityManager.logout()
        currentUser.value = null
        currentRole.value = UserRole.MEMBER
        _loginState.value = LoginState.Idle
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            try {
                val existingUsers = repository.allUsers.first()
                if (existingUsers.none { it.id == 1L }) {
                    repository.seedDatabase()
                }
            } catch (_: Exception) {}
        }
    }

    // Data Flows
    val income: StateFlow<List<Income>> = combine(currentUser, currentRole) { user, role ->
        when (role) {
            UserRole.SYNOD_ADMIN, UserRole.AUDITOR -> repository.allIncome
            UserRole.DIOCESE_ADMIN -> user?.dioceseId?.let { repository.getIncomeByDiocese(it) } ?: flowOf(emptyList())
            else -> user?.churchId?.let { repository.getIncomeByChurch(it) } ?: flowOf(emptyList())
        }
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = combine(currentUser, currentRole) { user, role ->
        when (role) {
            UserRole.SYNOD_ADMIN, UserRole.AUDITOR -> repository.allExpenses
            UserRole.DIOCESE_ADMIN -> user?.dioceseId?.let { repository.getExpensesByDiocese(it) } ?: flowOf(emptyList())
            else -> user?.churchId?.let { repository.getExpensesByChurch(it) } ?: flowOf(emptyList())
        }
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dioceses: StateFlow<List<Diocese>> = repository.allDioceses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val churches: StateFlow<List<Church>> = combine(currentRole, currentUser) { role, user ->
        // For registration, we need all churches if no user is logged in
        if (user == null) {
            repository.allChurches
        } else {
            when (role) {
                UserRole.SYNOD_ADMIN -> repository.allChurches
                UserRole.DIOCESE_ADMIN -> user.dioceseId?.let { repository.getChurchesByDiocese(it) } ?: flowOf(emptyList())
                else -> user.churchId?.let { id -> repository.allChurches.map { list -> list.filter { it.id == id } } } ?: flowOf(emptyList())
            }
        }
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<User>> = combine(currentUser, currentRole) { user, role ->
        when (role) {
            UserRole.SYNOD_ADMIN -> repository.allUsers
            UserRole.DIOCESE_ADMIN -> user?.dioceseId?.let { repository.getUsersByDiocese(it) } ?: flowOf(emptyList())
            else -> user?.churchId?.let { repository.getUsersByChurch(it) } ?: flowOf(emptyList())
        }
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun addIncome(amount: Double, source: String, accountId: Long, churchId: Long, category: String? = null, description: String? = null, paymentMethod: String? = "Cash") {
        val ref = "INC-${System.currentTimeMillis().toString().takeLast(8)}"
        viewModelScope.launch { 
            repository.addIncome(
                amount, source, accountId, churchId, 
                category = category, 
                userId = currentUser.value?.id ?: 1L, 
                description = description, 
                paymentMethod = paymentMethod,
                referenceNumber = ref
            ) 
        }
    }

    fun addExpense(amount: Double, recipient: String, accountId: Long, churchId: Long, category: String? = "Other", description: String? = null, paymentMethod: String? = "Cash") {
        val ref = "EXP-${System.currentTimeMillis().toString().takeLast(8)}"
        viewModelScope.launch { 
            repository.addExpense(
                amount, recipient, accountId, churchId, 
                category ?: "Other", 
                userId = currentUser.value?.id ?: 1L, 
                description = description, 
                paymentMethod = paymentMethod,
                referenceNumber = ref
            ) 
        }
    }

    fun createChureGroup(name: String, contributionAmount: Double, frequency: String, churchId: Long) {
        viewModelScope.launch { 
            repository.addChureGroup(name, contributionAmount, frequency, churchId)
        }
    }


    fun addDiocese(name: String, bishopName: String? = null) {
        viewModelScope.launch { repository.addDiocese(name, bishopName) }
    }

    fun addChurch(name: String, location: String, dioceseId: Long, adminName: String? = null, adminEmail: String? = null) {
        viewModelScope.launch { 
            val churchId = repository.addChurch(name, location, dioceseId)
            
            // Auto-assign admin if provided
            if (!adminName.isNullOrBlank() && !adminEmail.isNullOrBlank()) {
                val passwordHash = com.example.orthodoxapp.security.PasswordHasher.hashPassword("church123") // Default password
                repository.registerUser(User(
                    name = adminName,
                    email = adminEmail.trim().lowercase(),
                    passwordHash = passwordHash,
                    churchId = churchId,
                    dioceseId = dioceseId,
                    role = "CHURCH_ADMIN",
                    roleId = 3L
                ))
            }
        }
    }

    fun addUser(name: String, email: String, roleId: Long, churchId: Long? = null, dioceseId: Long? = null) {
        viewModelScope.launch {
            val user = User(
                name = name,
                email = email,
                roleId = roleId,
                churchId = churchId,
                dioceseId = dioceseId,
                passwordHash = com.example.orthodoxapp.security.PasswordHasher.hashPassword("Default@123")
            )
            repository.registerUser(user)
        }
    }

    fun approveIncome(incomeId: Long) {
        viewModelScope.launch { repository.approveIncome(incomeId, currentUser.value?.id ?: 1L) }
    }

    fun approveExpense(expenseId: Long) {
        viewModelScope.launch { repository.approveExpense(expenseId, currentUser.value?.id ?: 1L) }
    }

    fun addEmployee(fullName: String, role: String, churchId: Long, salary: Double, phone: String? = null) {
        viewModelScope.launch {
            repository.addEmployee(fullName, role, churchId, salary, phone)
        }
    }

    fun approveIncome(income: Income) {
        viewModelScope.launch { repository.approveIncome(income.id, currentUser.value?.id ?: 1L) }
    }

    fun rejectIncome(id: Long) {
        viewModelScope.launch { repository.rejectIncome(id, currentUser.value?.id ?: 1L) }
    }

    fun approveExpense(expense: Expense) {
        viewModelScope.launch { repository.approveExpense(expense.id, currentUser.value?.id ?: 1L) }
    }

    fun rejectExpense(id: Long) {
        viewModelScope.launch { repository.rejectExpense(id, currentUser.value?.id ?: 1L) }
    }

    fun approveChurch(id: Long) {
        viewModelScope.launch { repository.approveChurch(id, currentUser.value?.id ?: 1L) }
    }

    fun rejectChurch(id: Long) {
        viewModelScope.launch { repository.rejectChurch(id, currentUser.value?.id ?: 1L) }
    }

    private fun getRoleFromId(id: Long): UserRole = when (id) {
        1L -> UserRole.SYNOD_ADMIN
        2L -> UserRole.DIOCESE_ADMIN
        3L -> UserRole.CHURCH_ADMIN
        4L -> UserRole.ACCOUNTANT
        5L -> UserRole.AUDITOR
        else -> UserRole.MEMBER
    }

    fun addChurchAdmin(churchId: Long, name: String, email: String) {
        viewModelScope.launch {
            val passwordHash = com.example.orthodoxapp.security.PasswordHasher.hashPassword("church123")
            repository.registerUser(User(
                name = name,
                email = email.trim().lowercase(),
                passwordHash = passwordHash,
                churchId = churchId,
                role = "CHURCH_ADMIN",
                roleId = 3L
            ))
            _actionState.value = ActionState.Success
        }
    }
}
