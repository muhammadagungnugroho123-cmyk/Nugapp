package com.example.data.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.ui.screens.alarms.AlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.ClockApplication
import kotlinx.coroutines.flow.first

class AlarmService : Service() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    companion object {
        const val ACTION_STOP = "com.example.action.STOP_ALARM"
        const val ACTION_SNOOZE = "com.example.action.SNOOZE_ALARM"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"
        
        if (intent?.action == ACTION_STOP) {
            if (alarmId != -1) {
                val appContainer = (application as ClockApplication).container
                CoroutineScope(Dispatchers.IO).launch {
                    val alarm = appContainer.alarmRepository.getAlarmById(alarmId)
                    if (alarm != null && alarm.daysOfWeek == 0) {
                        appContainer.alarmRepository.updateAlarm(alarm.copy(isEnabled = false))
                    }
                }
            }
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }
        
        if (intent?.action == ACTION_SNOOZE) {
            if (alarmId != -1) {
                val appContainer = (application as ClockApplication).container
                CoroutineScope(Dispatchers.IO).launch {
                    var soundUriStr = ""
                    var isVibrateSetting = true
                    val snoozeMinutes = appContainer.settingsRepository.snoozeDuration.first()
                    val alarm = appContainer.alarmRepository.getAlarmById(alarmId)
                    if (alarm != null) {
                        soundUriStr = alarm.soundUri
                        isVibrateSetting = alarm.isVibrate
                        if (alarm.label.startsWith("Snooze:")) {
                            appContainer.alarmRepository.deleteAlarmById(alarm.id)
                        } else if (alarm.daysOfWeek == 0) {
                            appContainer.alarmRepository.updateAlarm(alarm.copy(isEnabled = false))
                        }
                    }
                    val cal = java.util.Calendar.getInstance()
                    cal.add(java.util.Calendar.MINUTE, snoozeMinutes)
                    val snoozeAlarm = com.example.data.local.AlarmEntity(
                        id = 0,
                        hour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                        minute = cal.get(java.util.Calendar.MINUTE),
                        label = if (alarmLabel.startsWith("Snooze:")) alarmLabel else "Snooze: $alarmLabel",
                        isEnabled = true,
                        daysOfWeek = 0,
                        dateMillis = cal.timeInMillis,
                        soundUri = soundUriStr,
                        isVibrate = isVibrateSetting
                    )
                    appContainer.alarmRepository.insertAlarm(snoozeAlarm)
                }
            }
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }


        val soundUri = intent?.getStringExtra("ALARM_SOUND_URI")
        val isVibrate = intent?.getBooleanExtra("ALARM_VIBRATE", true) ?: true

        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("ALARM_SOUND_URI", soundUri)
            putExtra("ALARM_VIBRATE", isVibrate)
            putExtra("FROM_SERVICE", true)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP
            putExtra("ALARM_ID", alarmId)
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            alarmId,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            alarmId + 10000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "alarm_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm notifications"
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ Alarm CHRONOS")
            .setContentText(alarmLabel.ifEmpty { "Waktunya bangun!" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(alarmLabel.ifEmpty { "Waktunya bangun untuk hari yang luar biasa!" }))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_popup_sync, "Snooze", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Matikan", stopPendingIntent)
            .setOngoing(true)
            .setColor(resources.getColor(android.R.color.holo_blue_bright, null))
            .build()
            
        startForeground(if (alarmId != -1) alarmId else 1001, notification)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Chronos::AlarmWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes timeout

        val appContainer = (application as ClockApplication).container
        CoroutineScope(Dispatchers.Main).launch {
            val ringtoneIndex = appContainer.settingsRepository.alarmRingtoneIndex.first()
            val fadeInSeconds = appContainer.settingsRepository.alarmFadeInDuration.first()
            playAlarmSoundAndVibrate(soundUri, isVibrate, ringtoneIndex, fadeInSeconds)
        }

        return START_STICKY
    }
    
    private fun playAlarmSoundAndVibrate(soundUriStr: String?, isVibrate: Boolean, ringtoneIndex: Int = 0, fadeInSeconds: Int = 0) {
        try {
            var uri = android.net.Uri.parse(soundUriStr ?: "")
            if (uri.scheme == null || soundUriStr == "Over the Horizon by SUGA of BTS" || soundUriStr.isNullOrEmpty()) {
                uri = when (ringtoneIndex) {
                    1 -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    2 -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                }
            }
            ringtone = RingtoneManager.getRingtone(this, uri)
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            }
            ringtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
                if (fadeInSeconds > 0) {
                    ringtone?.volume = 0f
                }
            }
            ringtone?.play()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && fadeInSeconds > 0) {
                CoroutineScope(Dispatchers.Main).launch {
                    val steps = fadeInSeconds * 10
                    val intervalMs = 100L
                    val volumeStep = 1.0f / steps
                    var currentVolume = 0.0f
                    for (step in 1..steps) {
                        delay(intervalMs)
                        if (ringtone?.isPlaying == true) {
                            currentVolume = (currentVolume + volumeStep).coerceAtMost(1.0f)
                            ringtone?.volume = currentVolume
                        } else {
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        if (isVibrate) {
            try {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                
                val pattern = longArrayOf(0, 500, 500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val attributes = android.os.VibrationAttributes.Builder()
                            .setUsage(android.os.VibrationAttributes.USAGE_ALARM)
                            .build()
                        vibrator?.vibrate(effect, attributes)
                    } else {
                        @Suppress("DEPRECATION")
                        val attributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(effect, attributes)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun stopAlarm() {
        try {
            ringtone?.stop()
            vibrator?.cancel()
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
