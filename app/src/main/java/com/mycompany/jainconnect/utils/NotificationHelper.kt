package com.mycompany.jainconnect.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.mycompany.jainconnect.data.models.Event
import com.mycompany.jainconnect.receivers.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_ID = "jain_events_channel"
    const val CHANNEL_NAME = "Jain Events"
    const val EXTRA_EVENT_TITLE = "extra_event_title"
    const val EXTRA_EVENT_ID = "extra_event_id"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Reminders for upcoming Jain events"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleEventReminder(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Parse Event Date/Time
        // Expected format: Date "YYYY-MM-DD" or "DD-MM-YYYY", Time "HH:mm"
        val eventTimeMillis = parseDateTime(event.date, event.time)

        if (eventTimeMillis == -1L) {
            Log.e("NotificationHelper", "Invalid date/time for event: ${event.name}")
            return
        }

        // Schedule for 2 hours before, or if less than 2 hours, schedule for 15 mins before?
        // Let's do: 2 hours before.
        val reminderTime = eventTimeMillis - (2 * 60 * 60 * 1000)

        if (reminderTime < System.currentTimeMillis()) {
            // Event is too close or in past, don't schedule
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_EVENT_TITLE, event.name)
            putExtra(EXTRA_EVENT_ID, event._id)
        }

        // RequestCode needs to be unique per event
        val requestCode = event._id.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact or ask for permission (skipping permission flow for MVP)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
            Log.d("NotificationHelper", "Scheduled alarm for ${event.name} at $reminderTime")
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Failed to schedule alarm: ${e.message}")
        }
    }

    private fun parseDateTime(dateStr: String?, timeStr: String?): Long {
        if (dateStr.isNullOrEmpty()) return -1L
        
        // Normalize Date
        // Try ISO first YYYY-MM-DD
        // Then DD-MM-YYYY
        
        val calendar = Calendar.getInstance()
        var dateParsed = false

        // Try YYYY-MM-DD
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr)
            if (date != null) {
                calendar.time = date
                dateParsed = true
            }
        } catch (e: Exception) { }

        if (!dateParsed) {
            try {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val date = sdf.parse(dateStr)
                if (date != null) {
                    calendar.time = date
                    dateParsed = true
                }
            } catch (e: Exception) { }
        }
        
        if (!dateParsed) return -1L

        // Parse Time
        if (!timeStr.isNullOrEmpty()) {
            try {
                // Try HH:mm (24h) or hh:mm a (12h)
                // Let's assume standard HH:mm for now as per backend controller
                val timeParts = timeStr.split(":")
                if (timeParts.size >= 2) {
                    val hour = timeParts[0].toIntOrNull() ?: 9
                    val minute = timeParts[1].toIntOrNull() ?: 0
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                }
            } catch (e: Exception) {
                // Default to 9 AM if time parse fails
                 calendar.set(Calendar.HOUR_OF_DAY, 9)
                 calendar.set(Calendar.MINUTE, 0)
            }
        } else {
             calendar.set(Calendar.HOUR_OF_DAY, 9)
             calendar.set(Calendar.MINUTE, 0)
        }
        
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }
}
