package com.example.orthodoxapp.service
 
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.orthodoxapp.R
import com.example.orthodoxapp.data.local.AppDatabase
import com.example.orthodoxapp.data.repository.FirebaseSyncManager
import kotlinx.coroutines.*
 
/**
 * A Background/Foreground Service example.
 * Used for syncing offline financial data with the server.
 */
class FinancialSyncService : Service() {
 
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var db: AppDatabase
    private lateinit var syncManager: FirebaseSyncManager

    override fun onCreate() {
        super.onCreate()
        Log.d("FinancialSyncService", "Service Created")
        db = AppDatabase.getDatabase(this)
        syncManager = FirebaseSyncManager(db.syncDao(), db.financeDao())
        createNotificationChannel()
    }
 
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FinancialSyncService", "Service Started")
        
        val viewIntent = Intent(this, com.example.orthodoxapp.MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, viewIntent, android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, "SYNC_CHANNEL_ID")
            .setContentTitle("Orthodox App Sync")
            .setContentText("Syncing financial data...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.ic_launcher_foreground, "View Dashboard", pendingIntent)
            .build()
            
        startForeground(1, notification)
        
        // Process Sync Queue in the background
        serviceScope.launch {
            processSyncQueue()
        }
        
        return START_STICKY
    }

    private suspend fun processSyncQueue() = coroutineScope {
        try {
            syncManager.startSyncWorker()
        } catch (e: Exception) {
            Log.e("SyncService", "Error in sync worker: ${e.message}")
        }
    }
 
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
 
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("FinancialSyncService", "Service Destroyed")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "SYNC_CHANNEL_ID",
                "Sync Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
