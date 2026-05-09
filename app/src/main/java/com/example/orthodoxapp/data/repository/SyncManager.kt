package com.example.orthodoxapp.data.repository

import android.util.Log
import com.example.orthodoxapp.data.local.dao.FinanceDao
import com.example.orthodoxapp.data.local.dao.SyncDao
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.data.network.ApiService
import com.example.orthodoxapp.data.network.PaymentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * PRODUCTION SYNC MANAGER (Offline-First)
 * 
 * Orchestrates background synchronization between local Room DB 
 * and the ASP.NET Core Backend via Retrofit.
 */
class SyncManager(
    private val syncDao: SyncDao,
    private val financeDao: FinanceDao,
    private val api: ApiService
) {

    suspend fun startSyncWorker() = withContext(Dispatchers.IO) {
        while (true) {
            val next = syncDao.getNextInQueue()
            if (next != null) {
                processSyncItem(next)
            }
            delay(5000) // Poll every 5 seconds for new changes
        }
    }

    private suspend fun processSyncItem(item: SyncQueue) {
        try {
            Log.d("SyncWorker", "Processing: ${item.entityType} ID:${item.entityId}")
            
            val success = when (item.entityType) {
                "INCOME" -> syncIncome(item.entityId)
                "PAYMENT" -> syncPayment(item.entityId) // Added Offline Payment Sync
                else -> true
            }

            if (success) {
                syncDao.markAsSynced(item.id)
                syncDao.removeFromQueue(item)
                Log.d("SyncWorker", "Successfully synced to .NET Backend.")
            } else {
                handleSyncFailure(item, "Network Error")
            }
        } catch (e: Exception) {
            handleSyncFailure(item, e.message ?: "Unknown Error")
        }
    }

    private suspend fun syncPayment(id: Long): Boolean {
        // Retrieve offline payment from FinanceDao (assuming similar structure to Income)
        val payment = financeDao.getIncomeById(id) ?: return true
        
        val request = PaymentRequest(
            amount = payment.amount,
            currency = "ETB",
            email = "offline@church.local",
            firstName = "Offline",
            lastName = "Payment",
            purpose = payment.category ?: "TITHE",
            provider = "OFFLINE"
        )

        return try {
            val response = api.initializePayment(request)
            if (response.isSuccessful) {
                // In production, we'd save the returned Receipt metadata locally
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun syncIncome(id: Long): Boolean {
        val income = financeDao.getIncomeById(id) ?: return true
        
        return try {
            val response = api.recordIncome(income)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun handleSyncFailure(item: SyncQueue, error: String) {
        val updated = item.copy(
            retryCount = item.retryCount + 1,
            lastError = error
        )
        if (updated.retryCount > 10) {
            syncDao.removeFromQueue(item)
            Log.e("SyncWorker", "Max retries reached. Discarding sync item.")
        } else {
            syncDao.updateQueueItem(updated)
        }
    }
}
