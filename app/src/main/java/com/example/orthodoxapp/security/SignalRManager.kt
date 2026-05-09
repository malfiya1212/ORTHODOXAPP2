package com.example.orthodoxapp.security

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.Action1
import com.microsoft.signalr.Action2
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

/**
 * SignalR Manager - The Real-Time Engine for OrthodoxApp
 * 
 * Handles live updates from the ASP.NET Core FinanceHub.
 */
object SignalRManager {
    private const val TAG = "SignalRManager"
    private const val HUB_URL = "https://your-api-gateway.com/financeHub" // Placeholder for ASP.NET Gateway

    private var hubConnection: HubConnection? = null

    // Event Streams for the ViewModel
    private val _realTimeEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val realTimeEvents = _realTimeEvents.asSharedFlow()

    fun connect(accessToken: String, churchId: Long?, dioceseId: Long?) {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) return

        hubConnection = HubConnectionBuilder.create(HUB_URL)
            .withAccessTokenProvider(Single.just(accessToken))
            .build()

        // --- 💰 FINANCIAL EVENTS ---
        hubConnection?.on("OnIncomeAdded", Action1 { incomeId: Long ->
            Log.d(TAG, "Income Added: $incomeId")
            _realTimeEvents.tryEmit("REFRESH_INCOME")
        }, Long::class.java)

        hubConnection?.on("OnExpenseApproved", Action1 { expenseId: Long ->
            Log.d(TAG, "Expense Approved: $expenseId")
            _realTimeEvents.tryEmit("REFRESH_EXPENSES")
            _realTimeEvents.tryEmit("REFRESH_BALANCE")
        }, Long::class.java)

        // --- 👥 CHURE EVENTS ---
        hubConnection?.on("OnChurePayment", Action1 { paymentId: Long ->
            Log.d(TAG, "Chure Payment Received: $paymentId")
            _realTimeEvents.tryEmit("REFRESH_CHURE")
        }, Long::class.java)

        // --- 🔔 NOTIFICATIONS ---
        hubConnection?.on("OnNotification", Action2 { title: String, message: String ->
            Log.d(TAG, "New Notification: $title")
            _realTimeEvents.tryEmit("NEW_NOTIFICATION|$title|$message")
        }, String::class.java, String::class.java)

        hubConnection?.start()?.blockingAwait()

        // Join Scoped Groups
        churchId?.let { hubConnection?.send("JoinChurchGroup", it) }
        dioceseId?.let { hubConnection?.send("JoinDioceseGroup", it) }
        
        Log.d(TAG, "SignalR Connected and Groups Joined")
    }

    fun disconnect() {
        hubConnection?.stop()?.blockingAwait()
        hubConnection = null
        Log.d(TAG, "SignalR Disconnected")
    }
}
