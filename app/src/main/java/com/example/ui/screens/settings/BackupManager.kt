package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import com.example.data.local.AlarmEntity
import com.example.domain.repository.AlarmRepository
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupAlarmEntity(
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val label: String,
    val daysOfWeek: Int,
    val dateMillis: Long,
    val isVibrate: Boolean,
    val soundUri: String,
    val isSnooze: Boolean
)

@Serializable
data class AppBackupData(
    val themeColorIndex: Int,
    val is24HourFormat: Boolean,
    val defaultVibrate: Boolean,
    val notificationsEnabled: Boolean,
    val appLanguage: String,
    val clockStyleIndex: Int,
    val alarmRingtoneIndex: Int,
    val showWorldClockGlobe: Boolean,
    val worldClockGlobeStyle: Int,
    val temperatureUnit: Int,
    val savedCities: List<String>,
    val snoozeDuration: Int,
    val alarmFadeInDuration: Int,
    val weatherRefreshInterval: Int
)

@Serializable
data class FullBackupPayload(
    val version: String = "1.35.44",
    val timestamp: Long,
    val settings: AppBackupData,
    val alarms: List<BackupAlarmEntity>
)

object BackupManager {
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    suspend fun generateBackupJson(
        settingsRepository: SettingsRepository,
        alarmRepository: AlarmRepository
    ): String {
        val settings = AppBackupData(
            themeColorIndex = settingsRepository.themeColorIndex.first(),
            is24HourFormat = settingsRepository.is24HourFormat.first(),
            defaultVibrate = settingsRepository.defaultVibrate.first(),
            notificationsEnabled = settingsRepository.notificationsEnabled.first(),
            appLanguage = settingsRepository.appLanguage.first(),
            clockStyleIndex = settingsRepository.clockStyleIndex.first(),
            alarmRingtoneIndex = settingsRepository.alarmRingtoneIndex.first(),
            showWorldClockGlobe = settingsRepository.showWorldClockGlobe.first(),
            worldClockGlobeStyle = settingsRepository.worldClockGlobeStyle.first(),
            temperatureUnit = settingsRepository.temperatureUnit.first(),
            savedCities = settingsRepository.savedCities.first().toList(),
            snoozeDuration = settingsRepository.snoozeDuration.first(),
            alarmFadeInDuration = settingsRepository.alarmFadeInDuration.first(),
            weatherRefreshInterval = settingsRepository.weatherRefreshInterval.first()
        )

        val alarmsList = alarmRepository.getAllAlarms().first().map {
            BackupAlarmEntity(
                hour = it.hour,
                minute = it.minute,
                isEnabled = it.isEnabled,
                label = it.label,
                daysOfWeek = it.daysOfWeek,
                dateMillis = it.dateMillis,
                isVibrate = it.isVibrate,
                soundUri = it.soundUri,
                isSnooze = it.isSnooze
            )
        }

        val payload = FullBackupPayload(
            timestamp = System.currentTimeMillis(),
            settings = settings,
            alarms = alarmsList
        )

        return json.encodeToString(payload)
    }

    suspend fun restoreBackupJson(
        jsonString: String,
        settingsRepository: SettingsRepository,
        alarmRepository: AlarmRepository
    ): Boolean {
        return try {
            val payload = json.decodeFromString<FullBackupPayload>(jsonString)
            
            // Restore settings
            settingsRepository.setThemeColorIndex(payload.settings.themeColorIndex)
            settingsRepository.set24HourFormat(payload.settings.is24HourFormat)
            settingsRepository.setDefaultVibrate(payload.settings.defaultVibrate)
            settingsRepository.setNotificationsEnabled(payload.settings.notificationsEnabled)
            settingsRepository.setAppLanguage(payload.settings.appLanguage)
            settingsRepository.setClockStyleIndex(payload.settings.clockStyleIndex)
            settingsRepository.setAlarmRingtoneIndex(payload.settings.alarmRingtoneIndex)
            settingsRepository.setShowWorldClockGlobe(payload.settings.showWorldClockGlobe)
            settingsRepository.setWorldClockGlobeStyle(payload.settings.worldClockGlobeStyle)
            settingsRepository.setTemperatureUnit(payload.settings.temperatureUnit)
            settingsRepository.setSavedCities(payload.settings.savedCities.toSet())
            settingsRepository.setSnoozeDuration(payload.settings.snoozeDuration)
            settingsRepository.setAlarmFadeInDuration(payload.settings.alarmFadeInDuration)
            settingsRepository.setWeatherRefreshInterval(payload.settings.weatherRefreshInterval)

            // Restore Alarms (clear and re-insert)
            val existingAlarms = alarmRepository.getAllAlarms().first()
            existingAlarms.forEach {
                alarmRepository.deleteAlarmById(it.id)
            }

            payload.alarms.forEach {
                alarmRepository.insertAlarm(
                    AlarmEntity(
                        hour = it.hour,
                        minute = it.minute,
                        isEnabled = it.isEnabled,
                        label = it.label,
                        daysOfWeek = it.daysOfWeek,
                        dateMillis = it.dateMillis,
                        isVibrate = it.isVibrate,
                        soundUri = it.soundUri,
                        isSnooze = it.isSnooze
                    )
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareBackup(context: Context, jsonString: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, jsonString)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export AI Clock Backup")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
