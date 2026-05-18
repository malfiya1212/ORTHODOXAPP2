package com.example.orthodoxapp.data.local.dao

import androidx.room.*
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.data.model.Certificate
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // --- DIOCESE & CHURCH ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiocese(diocese: Diocese): Long

    @Query("SELECT * FROM dioceses ORDER BY name ASC")
    fun getAllDioceses(): Flow<List<Diocese>>

    @Update
    suspend fun updateDiocese(diocese: Diocese)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChurch(church: Church): Long

    @Query("SELECT * FROM churches ORDER BY name ASC")
    fun getAllChurches(): Flow<List<Church>>

    @Query("SELECT * FROM churches WHERE dioceseId = :dioceseId ORDER BY name ASC")
    fun getChurchesByDiocese(dioceseId: Long): Flow<List<Church>>

    @Query("SELECT * FROM churches WHERE id = :id LIMIT 1")
    suspend fun getChurchByIdSync(id: Long): Church?

    @Update
    suspend fun updateChurch(church: Church)

    @Query("UPDATE churches SET status = :status WHERE id = :id")
    suspend fun updateChurchStatus(id: Long, status: String)

    // --- USERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE churchId = :churchId AND roleId = 3 LIMIT 1")
    suspend fun getChurchAdminSync(churchId: Long): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: Long): User?
    
    @Update
    suspend fun updateUser(user: User)

    @Query("""
        UPDATE users 
        SET status = 'INACTIVE' 
        WHERE roleId = 6 
        AND status = 'ACTIVE' 
        AND id NOT IN (
            SELECT createdBy FROM income WHERE date > :sixMonthsAgo
        )
    """)
    suspend fun deactivateInactiveMembers(sixMonthsAgo: Long)

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE churchId = :churchId")
    fun getUsersByChurch(churchId: Long): Flow<List<User>>

    @Query("SELECT * FROM users WHERE dioceseId = :dioceseId")
    fun getUsersByDiocese(dioceseId: Long): Flow<List<User>>

    // --- ACCOUNTS & TRANSACTIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Query("SELECT * FROM accounts WHERE churchId = :churchId")
    fun getAccountsByChurch(churchId: Long): Flow<List<Account>>

    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>

    @Update
    suspend fun updateAccount(account: Account)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransaction): Long

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>



    // --- INCOME ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: Income): Long

    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<Income>>

    @Query("SELECT * FROM income WHERE churchId = :churchId ORDER BY date DESC")
    fun getIncomeByChurch(churchId: Long): Flow<List<Income>>

    @Query("SELECT income.* FROM income INNER JOIN churches ON income.churchId = churches.id WHERE churches.dioceseId = :dioceseId ORDER BY income.date DESC")
    fun getIncomeByDiocese(dioceseId: Long): Flow<List<Income>>

    @Query("SELECT * FROM income WHERE createdBy = :userId ORDER BY date DESC")
    fun getIncomeByUserId(userId: Long): Flow<List<Income>>

    @Query("UPDATE income SET status = :status WHERE id = :id")
    suspend fun updateIncomeStatus(id: Long, status: String)

    @Query("SELECT * FROM income WHERE id = :id")
    suspend fun getIncomeById(id: Long): Income?

    // --- EXPENSES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE churchId = :churchId ORDER BY date DESC")
    fun getExpensesByChurch(churchId: Long): Flow<List<Expense>>

    @Query("SELECT expenses.* FROM expenses INNER JOIN churches ON expenses.churchId = churches.id WHERE churches.dioceseId = :dioceseId ORDER BY expenses.date DESC")
    fun getExpensesByDiocese(dioceseId: Long): Flow<List<Expense>>

    @Query("UPDATE expenses SET status = :status WHERE id = :id")
    suspend fun patchExpenseStatus(id: Long, status: String)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    // --- SALARIES & EMPLOYEES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Query("SELECT * FROM employees WHERE churchId = :churchId")
    fun getEmployeesByChurch(churchId: Long): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalary(salary: Salary): Long

    @Query("SELECT * FROM salaries WHERE employeeId IN (SELECT id FROM employees WHERE churchId = :churchId)")
    fun getSalariesByChurch(churchId: Long): Flow<List<Salary>>

    @Query("UPDATE salaries SET status = :status WHERE id = :id")
    suspend fun updateSalaryStatus(id: Long, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryApproval(approval: SalaryApproval): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryPayment(payment: SalaryPayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryLedger(entry: SalaryLedgerEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntry): Long


    // --- CHURE SYSTEM ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChureGroup(group: ChureGroup): Long

    @Query("SELECT * FROM chure_groups")
    fun getAllChureGroups(): Flow<List<ChureGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChureMember(member: ChureMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChurePayment(payment: ChurePayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChurePayout(payout: ChurePayout): Long

    // --- AUDIT & INFRA ---
    @Insert
    suspend fun insertAuditLog(log: AuditLog)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY dueDate ASC")
    fun getRemindersByUser(userId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE churchId = :churchId ORDER BY dueDate ASC")
    fun getRemindersByChurch(churchId: Long): Flow<List<Reminder>>

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Query("SELECT * FROM budgets WHERE churchId = :churchId")
    fun getBudgetsByChurch(churchId: Long): Flow<List<Budget>>

    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Query("SELECT * FROM documents WHERE churchId = :churchId")
    fun getDocumentsByChurch(churchId: Long): Flow<List<Document>>

    // --- REAL-WORLD ENTERPRISE MODULES ---



    @Update
    suspend fun updateSalary(salary: Salary)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: ChurchAsset): Long

    @Query("SELECT * FROM church_assets WHERE churchId = :churchId")
    fun getAssetsByChurch(churchId: Long): Flow<List<ChurchAsset>>

    @Query("SELECT * FROM church_assets")
    fun getAllAssets(): Flow<List<ChurchAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: FinancialApproval): Long

    @Query("SELECT * FROM financial_approvals WHERE entityType = :type AND entityId = :id")
    fun getApprovalsForEntity(type: String, id: Long): Flow<List<FinancialApproval>>

    // --- NOTIFICATIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Query("SELECT * FROM notifications ORDER BY date DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY date DESC")
    fun getNotificationsByUserId(userId: Long): Flow<List<Notification>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    // --- CERTIFICATES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: Certificate): Long

    @Query("SELECT * FROM certificates WHERE userId = :userId ORDER BY issuedDate DESC")
    fun getCertificatesByUserId(userId: Long): Flow<List<Certificate>>
}
