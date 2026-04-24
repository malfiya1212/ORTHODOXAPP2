package com.example.orthodoxapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.data.repository.FinancialRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class FinancialViewModel(private val repository: FinancialRepository) : ViewModel() {

    // Current Session State
    val currentUser = MutableStateFlow<User?>(
        User(id = 1, name = "Admin User", email = "admin@church.org", roleId = 1) // Default mock for development
    )
    
    val currentRole = MutableStateFlow(UserRole.SUPER_ADMIN)

    val transactions: StateFlow<List<FinancialTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val churches: StateFlow<List<Church>> = repository.allChurches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<ChureGroup>> = repository.allChureGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val income: StateFlow<List<Income>> = combine(repository.allIncome, currentUser, currentRole) { list, user, role ->
        when (role) {
            UserRole.SUPER_ADMIN, UserRole.AUDITOR -> list
            UserRole.REGIONAL_ADMIN -> list.filter { it.churchId in (/* Logic to find churches in region */ emptyList<Long>()) } // Needs more complex query
            UserRole.CHURCH_ADMIN, UserRole.ACCOUNTANT -> list.filter { it.churchId == user?.churchId }
            UserRole.MEMBER -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = combine(repository.allExpenses, currentUser, currentRole) { list, user, role ->
        when (role) {
            UserRole.SUPER_ADMIN, UserRole.AUDITOR -> list
            UserRole.REGIONAL_ADMIN -> list // Filter logic...
            UserRole.CHURCH_ADMIN, UserRole.ACCOUNTANT -> list.filter { it.churchId == user?.churchId }
            UserRole.MEMBER -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val regions: StateFlow<List<Region>> = repository.allRegions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val zones: StateFlow<List<Zone>> = repository.allZones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addIncome(amount: Double, source: String, accountId: Long, churchId: Long) {
        viewModelScope.launch {
            repository.recordIncome(amount, source, accountId, churchId, currentUser.value?.id ?: 0L)
        }
    }

    fun addExpense(amount: Double, recipient: String, accountId: Long, churchId: Long) {
        viewModelScope.launch {
            repository.recordExpense(amount, recipient, accountId, churchId, currentUser.value?.id ?: 0L)
        }
    }

    fun approveIncome(id: Long) {
        viewModelScope.launch {
            repository.updateIncomeStatus(id, "APPROVED", currentUser.value?.id ?: 0L)
        }
    }

    fun rejectIncome(id: Long) {
        viewModelScope.launch {
            repository.updateIncomeStatus(id, "REJECTED", currentUser.value?.id ?: 0L)
        }
    }

    fun approveExpense(id: Long) {
        viewModelScope.launch {
            repository.updateExpenseStatus(id, "APPROVED", currentUser.value?.id ?: 0L)
        }
    }

    fun rejectExpense(id: Long) {
        viewModelScope.launch {
            repository.updateExpenseStatus(id, "REJECTED", currentUser.value?.id ?: 0L)
        }
    }

    fun createReminder(title: String, dueDate: Long, userId: Long) {
        viewModelScope.launch {
            repository.addReminder(title, null, dueDate, userId)
        }
    }

    fun createChureGroup(name: String, contribution: Double, frequency: String, churchId: Long) {
        viewModelScope.launch {
            repository.createChureGroup(name, contribution, frequency, churchId)
        }
    }

    fun addMemberToChure(groupId: Long, userId: Long) {
        viewModelScope.launch {
            repository.addMemberToChure(groupId, userId)
        }
    }

    fun recordChurePayment(memberId: Long, amount: Double, period: Int) {
        viewModelScope.launch {
            repository.recordChurePayment(memberId, amount, period)
        }
    }

    fun performChurePayout(groupId: Long, userId: Long, amount: Double, period: Int) {
        viewModelScope.launch {
            repository.performChurePayout(groupId, userId, amount, period)
        }
    }

    // Security & Auth
    fun sendOtpToEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(1000)
            // Mock: Always succeed for demo
            onResult(true)
        }
    }
}
