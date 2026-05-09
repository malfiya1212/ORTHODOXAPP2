package com.example.orthodoxapp.service

import android.util.Log
// import com.google.firebase.messaging.FirebaseMessagingService
// import com.google.firebase.messaging.RemoteMessage

/**
 * Enterprise Firebase Cloud Messaging Service
 * 
 * To activate:
 * 1. Add 'google-services.json' to the app/ folder.
 * 2. Uncomment the Firebase dependencies in build.gradle.kts.
 * 3. Uncomment the 'extends FirebaseMessagingService' and imports below.
 */
class FirebaseCloudMessagingService /* : FirebaseMessagingService() */ {

    /*
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")
        
        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM", "Message data payload: " + remoteMessage.data)
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // TODO: Send token to your server
    }
    */
}
