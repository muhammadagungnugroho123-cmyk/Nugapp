package com.example.data.calendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.ui.screens.clock.CalendarEvent

object CalendarAlarmScheduler {
    fun scheduleEventAlarms(context: Context, events: List<CalendarEvent>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        for (event in events) {
            // Schedule 15 minutes before the event
            val notifyTime = event.startTime - (15 * 60 * 1000)
            
            if (notifyTime > System.currentTimeMillis()) {
                val intent = Intent(context, CalendarNotificationReceiver::class.java).apply {
                    putExtra("EVENT_TITLE", event.title)
                    putExtra("EVENT_DESC", event.description)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    event.title.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notifyTime,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
