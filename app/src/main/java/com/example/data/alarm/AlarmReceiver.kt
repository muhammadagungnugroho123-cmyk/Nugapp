package com.example.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.ClockApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val soundUri = intent.getStringExtra("ALARM_SOUND_URI")
        val isVibrate = intent.getBooleanExtra("ALARM_VIBRATE", true)
        
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("ALARM_SOUND_URI", soundUri)
            putExtra("ALARM_VIBRATE", isVibrate)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Reschedule if repeating, otherwise disable
        val appContainer = (context.applicationContext as ClockApplication).container
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = appContainer.alarmRepository.getAlarmById(alarmId)
            if (alarm != null) {
                if (alarm.daysOfWeek > 0) {
                    // Schedule the next occurrence
                    appContainer.alarmScheduler.schedule(alarm)
                } else {
                    // Disable it since it was a one-time alarm
                    val disabledAlarm = alarm.copy(isEnabled = false)
                    appContainer.alarmRepository.updateAlarm(disabledAlarm)
                }
            }
        }
    }
}
