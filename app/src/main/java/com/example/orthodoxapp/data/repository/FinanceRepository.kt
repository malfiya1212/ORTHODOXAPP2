package com.example.orthodoxapp.data.repository

import com.example.orthodoxapp.data.local.AppDatabase
import com.example.orthodoxapp.data.local.dao.FinanceDao
import com.example.orthodoxapp.data.local.dao.SyncDao
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.data.network.ApiService
import com.example.orthodoxapp.data.network.AuthResponse
import com.example.orthodoxapp.data.network.LoginRequest
import com.example.orthodoxapp.util.EmailUtility
import com.example.orthodoxapp.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import androidx.room.withTransaction

@Suppress("SpellCheckingInspection", "unused")
class FinanceRepository(
    private val db: AppDatabase,
    private val dao: FinanceDao,
    private val syncDao: SyncDao,
    private val api: ApiService,
    private val exchangeRateApi: com.example.orthodoxapp.data.network.ExchangeRateApiService
) {
    suspend fun getLatestExchangeRates(base: String) = exchangeRateApi.getLatestRates(base)
    // Auth Methods
    suspend fun login(email: String, passwordRaw: String): Result<AuthResponse> {
        return try {
            val passwordHash = com.example.orthodoxapp.security.PasswordHasher.hashPassword(passwordRaw)
            val response = api.login(LoginRequest(email, passwordHash))
            if (response.isSuccessful) {
                response.body()?.let { 
                    SecurityManager.authenticate(it.user, it.token)
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

    // Flows
    val allDioceses: Flow<List<Diocese>> = dao.getAllDioceses()
    val allChurches: Flow<List<Church>> = dao.getAllChurches()
    val allUsers: Flow<List<User>> = dao.getAllUsers()
    val allTransactions: Flow<List<FinancialTransaction>> = dao.getAllTransactions()
    val allChureGroups: Flow<List<ChureGroup>> = dao.getAllChureGroups()
    val allIncome: Flow<List<Income>> = dao.getAllIncome()
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    val allAuditLogs: Flow<List<AuditLog>> = dao.getAllAuditLogs()
    val allBudgets: Flow<List<Budget>> = dao.getAllBudgets()
    val allAccounts: Flow<List<Account>> = dao.getAllAccounts()
    val allAssets: Flow<List<ChurchAsset>> = dao.getAllAssets()

    // Scoped Data Access
    fun getIncomeByChurch(churchId: Long) = dao.getIncomeByChurch(churchId)
    fun getIncomeByDiocese(dioceseId: Long) = dao.getIncomeByDiocese(dioceseId)
    fun getIncomeByUserId(userId: Long) = dao.getIncomeByUserId(userId)
    fun getExpensesByChurch(churchId: Long) = dao.getExpensesByChurch(churchId)
    fun getExpensesByDiocese(dioceseId: Long) = dao.getExpensesByDiocese(dioceseId)
    fun getChurchesByDiocese(dioceseId: Long) = dao.getChurchesByDiocese(dioceseId)
    fun getAccountsByChurch(churchId: Long) = dao.getAccountsByChurch(churchId)
    fun getEmployeesByChurch(churchId: Long) = dao.getEmployeesByChurch(churchId)
    fun getAssetsByChurch(churchId: Long) = dao.getAssetsByChurch(churchId)
    fun getSalariesByChurch(churchId: Long) = dao.getSalariesByChurch(churchId)
    fun getUsersByChurch(churchId: Long) = dao.getUsersByChurch(churchId)
    fun getUsersByDiocese(dioceseId: Long) = dao.getUsersByDiocese(dioceseId)

    suspend fun insertAuditLog(log: AuditLog) {
        dao.insertAuditLog(log)
    }

    private suspend fun repositoryLogAction(userId: Long, action: String, details: String, tableName: String = "", recordId: Long = 0) {
        dao.insertAuditLog(AuditLog(
            userId = userId,
            action = action,
            details = details,
            tableName = tableName,
            recordId = recordId,
            timestamp = System.currentTimeMillis()
        ))
    }

    // Organization Methods
    suspend fun addDiocese(name: String, bishopName: String? = null, location: String? = null, description: String? = null): Long {
        return dao.insertDiocese(Diocese(name = name, bishopName = bishopName, location = location, description = description))
    }

    suspend fun addChurch(name: String, location: String, dioceseId: Long? = 1L): Long {
        return dao.insertChurch(Church(name = name, location = location, dioceseId = dioceseId, status = "ACTIVE"))
    }

    suspend fun addOrganizationWithAdmin(
        type: String,
        name: String,
        location: String,
        parentId: Long?,
        admin: User
    ) {
        db.withTransaction {
            if (type == "Diocese") {
                val dioceseId = dao.insertDiocese(Diocese(name = name, location = location))
                val adminUser = admin.copy(
                    dioceseId = dioceseId,
                    churchId = null,
                    role = "DIOCESE_ADMIN",
                    roleId = 2L
                )
                dao.insertUser(adminUser)
            } else {
                val churchId = dao.insertChurch(Church(name = name, location = location, dioceseId = parentId, status = "ACTIVE"))
                val adminUser = admin.copy(
                    dioceseId = parentId,
                    churchId = churchId,
                    role = "CHURCH_ADMIN",
                    roleId = 3L
                )
                dao.insertUser(adminUser)
            }
        }
    }

    suspend fun addDioceseWithChurchAndAdmin(
        dioceseName: String,
        dioceseLocation: String?,
        churchName: String,
        churchLocation: String?,
        admin: User
    ) {
        db.withTransaction {
            val dioceseId = dao.insertDiocese(Diocese(name = dioceseName, location = dioceseLocation))
            val churchId = dao.insertChurch(
                Church(
                    name = churchName,
                    location = churchLocation,
                    dioceseId = dioceseId,
                    status = "ACTIVE"
                )
            )
            val adminUser = admin.copy(
                dioceseId = dioceseId,
                churchId = churchId,
                role = "CHURCH_ADMIN",
                roleId = 3L
            )
            dao.insertUser(adminUser)
        }
    }


    suspend fun updateDiocese(diocese: Diocese) = dao.updateDiocese(diocese)
    suspend fun updateChurch(church: Church) = dao.updateChurch(church)
    suspend fun updateChurchStatus(id: Long, status: String) {
        dao.updateChurchStatus(id, status)
    }

    suspend fun registerUser(user: User): Long {
        return dao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        dao.updateUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return dao.getUserByEmail(email)
    }

    suspend fun checkInactiveMembers() {
        val sixMonthsInMillis = 180L * 24 * 60 * 60 * 1000
        val sixMonthsAgo = System.currentTimeMillis() - sixMonthsInMillis
        dao.deactivateInactiveMembers(sixMonthsAgo)
    }

    suspend fun addIncome(
        amount: Double,
        source: String,
        accountId: Long,
        churchId: Long,
        category: String? = "Other",
        status: String = "PENDING",
        userId: Long = 1L,
        description: String? = null,
        paymentMethod: String? = "Cash",
        referenceNumber: String? = null,
        originalAmount: Double? = null,
        originalCurrency: String? = "ETB"
    ) {
        val id = dao.insertIncome(
            Income(
                amount = amount,
                date = System.currentTimeMillis(),
                source = source,
                accountId = accountId,
                churchId = churchId,
                category = category ?: "Other",
                status = status,
                createdBy = userId,
                description = description,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                originalAmount = originalAmount,
                originalCurrency = originalCurrency
            )
        )
        syncDao.addToQueue(SyncQueue(entityType = "INCOME", entityId = id, action = "INSERT"))
        repositoryLogAction(userId, "RECORD_INCOME", "Income of $amount ETB recorded: ${description ?: source}", "income", id)

        
        // Notification to Church Admin
        val admin = dao.getChurchAdminSync(churchId)
        if (admin != null && admin.id != userId) {
            val contributor = dao.getUserByIdSync(userId)
            val church = dao.getChurchByIdSync(churchId)
            val contributorName = contributor?.name ?: "Unknown Member"
            val churchName = church?.name ?: "Church"

            // Insert admin notification
            dao.insertNotification(
                Notification(
                    userId = admin.id,
                    title = "New Contribution Approval",
                    message = "Ref: ${referenceNumber ?: "N/A"} - $contributorName ($churchName) contributed $amount ETB. Awaiting your approval.",
                    type = "Approval",
                    entityId = id
                )
            )

            // Send reference email to admin
            EmailUtility.sendReferenceEmail(
                recipientEmail = admin.email,
                referenceNumber = referenceNumber ?: "N/A",
                memberName = contributorName,
                amount = amount,
                paymentMethod = paymentMethod ?: "Cash"
            )
        }

        // Auto-activate member if inactive
        val user = dao.getUserByIdSync(userId)
        if (user != null && user.status == "INACTIVE") {
            dao.updateUser(user.copy(status = "ACTIVE"))
            repositoryLogAction(1L, "ACTIVATE_MEMBER", "User ${user.name} was auto-activated due to new contribution.")
        }
    }

    suspend fun addExpense(amount: Double, recipient: String, accountId: Long, churchId: Long, category: String, status: String = "PENDING", userId: Long = 1L, description: String? = null, paymentMethod: String? = "Cash", referenceNumber: String? = null) {
        val id = dao.insertExpense(Expense(amount = amount, date = System.currentTimeMillis(), recipient = recipient, accountId = accountId, churchId = churchId, category = category, status = status, requestedBy = userId, description = description, paymentMethod = paymentMethod, referenceNumber = referenceNumber))
        syncDao.addToQueue(SyncQueue(entityType = "EXPENSE", entityId = id, action = "INSERT"))
        repositoryLogAction(userId, "RECORD_EXPENSE", "Expense of $amount ETB recorded: ${description ?: recipient}", "expenses", id)
    }

    suspend fun addChureGroup(name: String, contributionAmount: Double, frequency: String, churchId: Long) {
        dao.insertChureGroup(ChureGroup(
            name = name,
            contributionAmount = contributionAmount,
            frequency = frequency,
            churchId = churchId
        ))
    }

    suspend fun approveIncome(id: Long, userId: Long) {
        dao.updateIncomeStatus(id, "APPROVED")
        repositoryLogAction(userId, "APPROVE_INCOME", "Income record $id approved")
    }

    suspend fun rejectIncome(id: Long, userId: Long) {
        dao.updateIncomeStatus(id, "REJECTED")
        repositoryLogAction(userId, "REJECT_INCOME", "Income record $id rejected")
    }

    suspend fun pushNotificationToChurch(churchId: Long, title: String, message: String, type: String) {
        val members = dao.getUsersByChurch(churchId).first()
        for (member in members) {
            dao.insertNotification(Notification(
                userId = member.id,
                title = title,
                message = message,
                type = type
            ))
        }
    }

    suspend fun approveExpense(id: Long, userId: Long) {
        dao.patchExpenseStatus(id, "APPROVED")
        repositoryLogAction(userId, "APPROVE_EXPENSE", "Expense record $id approved")
    }

    suspend fun rejectExpense(id: Long, userId: Long) {
        dao.patchExpenseStatus(id, "REJECTED")
        repositoryLogAction(userId, "REJECT_EXPENSE", "Expense record $id rejected")
    }

    suspend fun approveChurch(id: Long, userId: Long) {
        dao.updateChurchStatus(id, "ACTIVE")
        repositoryLogAction(userId, "APPROVE_CHURCH", "Church $id approved")
    }

    suspend fun rejectChurch(id: Long, userId: Long) {
        dao.updateChurchStatus(id, "REJECTED")
        repositoryLogAction(userId, "REJECT_CHURCH", "Church $id rejected")
    }

    val allNotifications: Flow<List<Notification>> = dao.getAllNotifications()
    fun getNotificationsByUser(userId: Long) = dao.getNotificationsByUserId(userId)

    suspend fun addNotification(userId: Long, title: String, message: String, type: String = "INFO") {
        dao.insertNotification(Notification(userId = userId, title = title, message = message, type = type))
    }

    suspend fun markNotificationRead(id: Long) = dao.markNotificationAsRead(id)


    // --- REAL-WORLD ENTERPRISE METHODS ---

    suspend fun addEmployee(fullName: String, role: String, churchId: Long, salary: Double, phone: String? = null) {
        dao.insertEmployee(Employee(fullName = fullName, role = role, churchId = churchId, salary = salary, phone = phone))
        repositoryLogAction(1L, "REGISTER_PERSONNEL", "Personnel $fullName ($role) registered", "employees")
    }

    suspend fun processSalary(employeeId: Long, amount: Double, month: Int, year: Int, userId: Long) {
        val id = dao.insertSalary(Salary(employeeId = employeeId, amount = amount, month = month, year = year, status = "PENDING"))
        repositoryLogAction(userId, "INIT_PAYROLL", "Salary for month $month processed", "salaries", id)
    }

    suspend fun addChurchAsset(name: String, type: String, value: Double, churchId: Long, description: String? = null) {
        dao.insertAsset(ChurchAsset(name = name, type = type, value = value, churchId = churchId, description = description))
        repositoryLogAction(1L, "REGISTER_ASSET", "Asset $name ($type) registered", "church_assets")
    }

    fun getCertificatesByUser(userId: Long): Flow<List<Certificate>> = dao.getCertificatesByUserId(userId)

    suspend fun addCertificate(userId: Long, churchId: Long, title: String, awardType: String, description: String? = null, issuerId: Long) {
        val certSerial = "CERT-${System.currentTimeMillis().toString().takeLast(6)}-${userId}"
        dao.insertCertificate(Certificate(
            userId = userId,
            churchId = churchId,
            title = title,
            awardType = awardType,
            description = description,
            issuedBy = issuerId,
            certificateId = certSerial
        ))
        
        // Notify the user
        dao.insertNotification(Notification(
            userId = userId,
            title = "New Award Received!",
            message = "Congratulations! You have been awarded a '$title' certificate for your $awardType.",
            type = "Award",
            entityId = null
        ))
        
        repositoryLogAction(issuerId, "ISSUE_CERTIFICATE", "Certificate $certSerial issued to user $userId", "certificates")
    }

    suspend fun submitForApproval(entityType: String, entityId: Long, reviewerId: Long, comments: String? = null) {
        dao.insertApproval(FinancialApproval(entityType = entityType, entityId = entityId, reviewerId = reviewerId, comments = comments, stage = 1, status = "PENDING"))
    }

    suspend fun seedDatabase() {
        val dioceses = listOf("Addis Ababa", "Amhara", "Oromia", "Tigray", "Somali", "Afar", "Sidama", "Harar")
        dioceses.forEach { name -> dao.insertDiocese(Diocese(name = name)) }
        dao.insertChurch(Church(name = "St. George Cathedral", location = "Addis Ababa", dioceseId = 1L))
        dao.insertUser(User(id = 1, name = "Synod Admin", email = "superadmin@church.com", roleId = 1L, passwordHash = com.example.orthodoxapp.security.PasswordHasher.hashPassword("Super@123")))
        dao.insertAccount(Account(name = "Main Fund", churchId = 1L, balance = 100000.0))
        
        // Seed some initial clergy
        dao.insertEmployee(Employee(fullName = "Abba Gebre Selassie", role = "Head Priest", churchId = 1L, salary = 8500.0))

        // Seed Chure Groups
        dao.insertChureGroup(ChureGroup(name = "St. Mary Mutual Aid", contributionAmount = 1000.0, frequency = "Monthly", churchId = 1L))
        dao.insertChureGroup(ChureGroup(name = "Youth Spiritual Fund", contributionAmount = 500.0, frequency = "Weekly", churchId = 1L))

        // Seed Notifications
        dao.insertNotification(Notification(userId = 1, title = "System Initialized", message = "The Tewahedo Connect ERP system has been successfully configured.", type = "INFO"))
        dao.insertNotification(Notification(userId = 1, title = "Welcome Admin", message = "You have full access to the Holy Synod National Dashboard.", type = "APPROVAL"))
    }
}
