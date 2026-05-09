package com.example.orthodoxapp.repository

import com.example.orthodoxapp.data.local.dao.FinanceDao
import com.example.orthodoxapp.data.local.dao.UserDao
import com.example.orthodoxapp.data.local.dao.SyncDao
import com.example.orthodoxapp.data.network.ApiService
import com.example.orthodoxapp.data.model.*
import kotlinx.coroutines.flow.Flow
import androidx.room.withTransaction
import com.example.orthodoxapp.data.local.AppDatabase

class FinanceRepository(
    private val db: AppDatabase,
    private val financeDao: FinanceDao,
    private val userDao: UserDao,
    private val syncDao: SyncDao,
    private val api: ApiService
) {
    // Financial Flows
    val allIncome: Flow<List<Income>> = financeDao.getAllIncome()
    val allExpenses: Flow<List<Expense>> = financeDao.getAllExpenses()
    val allTransactions: Flow<List<FinancialTransaction>> = financeDao.getAllTransactions()
    val allAuditLogs: Flow<List<AuditLog>> = financeDao.getAllAuditLogs()

    // Scoped Data
    fun getAccountsByChurch(churchId: Long) = financeDao.getAccountsByChurch(churchId)

    suspend fun addIncome(income: Income) {
        val id = financeDao.insertIncome(income)
        syncDao.addToQueue(SyncQueue(entityType = "INCOME", entityId = id, action = "INSERT"))
    }

    suspend fun addExpense(expense: Expense) {
        val id = financeDao.insertExpense(expense)
        syncDao.addToQueue(SyncQueue(entityType = "EXPENSE", entityId = id, action = "INSERT"))
    }

    // Organization Methods (delegated via UserDao)
    fun getAllDioceses() = userDao.getAllDioceses()
    fun getAllChurches() = userDao.getAllChurches()
}
