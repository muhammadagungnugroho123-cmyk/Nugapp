package com.example.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.local.AlarmEntity
import com.example.domain.alarm.AlarmScheduler
import java.util.Calendar

class AlarmSchedulerImpl(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_SOUND_URI", alarm.soundUri)
            putExtra("ALARM_VIBRATE", alarm.isVibrate)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            if (alarm.dateMillis > 0L) {
                val selectedCal = Calendar.getInstance().apply { timeInMillis = alarm.dateMillis }
                set(Calendar.YEAR, selectedCal.get(Calendar.YEAR))
                set(Calendar.MONTH, selectedCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, selectedCal.get(Calendar.DAY_OF_MONTH))
            } else if (alarm.daysOfWeek > 0) {
                // Find next valid day correctly, accounting for DST
                var found = false
                val tempCal = Calendar.getInstance()
                tempCal.timeInMillis = this.timeInMillis
                
                for (i in 0..7) {
                    val currentDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
                    // Calendar.MONDAY is 2, Calendar.SUNDAY is 1. We want Mon=0, Sun=6.
                    val bitmaskIndex = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - 2
                    
                    if ((alarm.daysOfWeek and (1 shl bitmaskIndex)) != 0) {
                        if (i == 0 && tempCal.timeInMillis <= System.currentTimeMillis()) {
                            // If it's today but time has passed, try next days
                        } else {
                            timeInMillis = tempCal.timeInMillis
                            found = true
                            break
                        }
                    }
                    tempCal.add(Calendar.DAY_OF_YEAR, 1)
                }
                
                if (!found) {
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
            } else {
                // If the time is in the past, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        if (alarm.dateMillis > 0L && calendar.timeInMillis <= System.currentTimeMillis()) {
            return
        }

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(
                calendar.timeInMillis,
                pendingIntent
            )
            alarmManager.setAlarmClock(
                alarmClockInfo,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun cancel(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
