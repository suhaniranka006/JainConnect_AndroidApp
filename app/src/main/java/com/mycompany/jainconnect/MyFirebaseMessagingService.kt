package com.mycompany.jainconnect


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_Service"

    /**
     * Yeh tab call hota hai jab app background ya foreground mein notification receive karti hai.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check karein ki message mein notification data hai ya nahi.
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Jain Connect"
            val body = notification.body ?: "You have a new message."

            Log.d(TAG, "Notification Title: $title")
            Log.d(TAG, "Notification Body: $body")

            // Notification ko display karein
            sendNotification(title, body)
        }
    }

    /**
     * Yeh naya token generate hone par call hota hai (jaise app reinstall ya data clear).
     * Yahaan aap token ko apne server par bhej sakte hain (Targeted notification ke liye).
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // Abhi ke liye, hum topics use kar rahe hain, toh isse server par bhejna zaroori nahi hai.
        // sendRegistrationToServer(token)
    }

    /**
     * User ko actual notification bana kar dikhata hai.
     */
    private fun sendNotification(title: String, messageBody: String) {
        val channelId = "jain_connect_tithi"
        val channelName = "Tithi Updates"

        // Notification par click karne se SplashActivity khulegi
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.jainconnect_app_logo) // Apna app logo yahaan rakhein
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 (Oreo) aur usse upar ke liye Notification Channel zaroori hai.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Notification ko display karein
        notificationManager.notify(0, notificationBuilder.build())
    }
}
