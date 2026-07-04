package com.example.data.calendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class CalendarNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventTitle = intent.getStringExtra("EVENT_TITLE") ?: "Upcoming Event"
        val eventDesc = intent.getStringExtra("EVENT_DESC") ?: ""
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "calendar_ai_channel",
                "AI Calendar Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Smart notifications for upcoming calendar events"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, "calendar_ai_channel")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("✨ AI Reminder: $eventTitle")
            .setContentText(if (eventDesc.isNotEmpty()) eventDesc else "Your event is starting soon. Get ready!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(eventTitle.hashCode(), notification)
    }
}
