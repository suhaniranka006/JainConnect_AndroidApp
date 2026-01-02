package com.mycompany.jainconnect.receivers

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.activities.EventActivity
import com.mycompany.jainconnect.utils.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventTitle = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_TITLE) ?: "Jain Event"
        // val eventId = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_ID)

        showNotification(context, eventTitle)
    }

    private fun showNotification(context: Context, title: String) {
        // Intent to open App when tapped
        val tapIntent = Intent(context, EventActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Ensure this resource exists, or use ic_notification if available
            .setContentTitle("Event Reminder")
            .setContentText("You have an upcoming event: $title")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }
}
