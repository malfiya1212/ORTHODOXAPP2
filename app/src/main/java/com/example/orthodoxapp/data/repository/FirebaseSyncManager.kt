package com.example.orthodoxapp.data.repository

import android.util.Log
import com.example.orthodoxapp.data.local.dao.FinanceDao
import com.example.orthodoxapp.data.local.dao.SyncDao
import com.example.orthodoxapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * PRODUCTION SYNC MANAGER (Offline-First)
 */
class FirebaseSyncManager(
    private val syncDao: SyncDao,
    private val financeDao: FinanceDao
) {

    /**
     * Start the background processing loop.
     * In a real app, this would be a WorkManager job.
     */
    suspend fun startSyncWorker() = withContext(Dispatchers.IO) {
        while (true) {
            val next = syncDao.getNextInQueue()
            if (next != null) {
                processSyncItem(next)
            }
            delay(5000) // Poll every 5 seconds
        }
    }

    private suspend fun processSyncItem(item: SyncQueue) {
        try {
            Log.d("SyncWorker", "Processing: ${item.entityType} ID:${item.entityId}")
            
            val success = when (item.entityType) {
                "INCOME" -> syncIncome(item.entityId)
                "EXPENSE" -> syncExpense(item.entityId)
                else -> true
            }

            if (success) {
                syncDao.removeFromQueue(item)
                Log.d("SyncWorker", "Successfully synced and removed from queue.")
            } else {
                handleSyncFailure(item, "Network Timeout")
            }
        } catch (e: Exception) {
            handleSyncFailure(item, e.message ?: "Unknown Error")
        }
    }

    private suspend fun syncIncome(id: Long): Boolean {
        val income = financeDao.getIncomeById(id) ?: return true // Already deleted?
        // --- REAL NETWORK CALL WOULD GO HERE ---
        // val response = api.uploadIncome(income)
        delay(1000) // Simulate network latency
        return true 
    }

    private suspend fun syncExpense(id: Long): Boolean {
        val expense = financeDao.getExpenseById(id) ?: return true
        delay(1000)
        return true
    }

    private suspend fun handleSyncFailure(item: SyncQueue, error: String) {
        val updated = item.copy(
            retryCount = item.retryCount + 1,
            lastError = error
        )
        if (updated.retryCount > 10) {
            syncDao.removeFromQueue(item)
            Log.e("SyncWorker", "Max retries reached for ID:${item.entityId}. Discarding.")
        } else {
            syncDao.updateQueueItem(updated)
            Log.w("SyncWorker", "Retry #${updated.retryCount} for ID:${item.entityId}")
        }
    }

    suspend fun queueForSync(type: String, id: Long, action: String = "CREATE") {
        syncDao.addToQueue(SyncQueue(entityType = type, entityId = id, action = action))
    }
}
