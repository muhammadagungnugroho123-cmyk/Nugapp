package com.example.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ClockApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val appContainer = (context.applicationContext as ClockApplication).container
            CoroutineScope(Dispatchers.IO).launch {
                val alarms = appContainer.alarmRepository.getAllAlarms().first()
                alarms.forEach { alarm ->
                    if (alarm.isEnabled) {
                        appContainer.alarmScheduler.schedule(alarm)
                    }
                }
            }
        }
    }
}
