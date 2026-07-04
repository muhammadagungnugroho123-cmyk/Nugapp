package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeColorIndex: Flow<Int>
    val is24HourFormat: Flow<Boolean>
    val defaultVibrate: Flow<Boolean>
    val notificationsEnabled: Flow<Boolean>
    val appLanguage: Flow<String>
    val clockStyleIndex: Flow<Int>
    val alarmRingtoneIndex: Flow<Int>
    val showWorldClockGlobe: Flow<Boolean>
    val worldClockGlobeStyle: Flow<Int>
    val temperatureUnit: Flow<Int>
    val savedCities: Flow<Set<String>>
    val snoozeDuration: Flow<Int>
    val alarmFadeInDuration: Flow<Int>
    val weatherRefreshInterval: Flow<Int>
    val autoCloudBackup: Flow<Boolean>
    val lastBackupTimestamp: Flow<Long>

    suspend fun setThemeColorIndex(index: Int)
    suspend fun set24HourFormat(enabled: Boolean)
    suspend fun setDefaultVibrate(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setAppLanguage(language: String)
    suspend fun setClockStyleIndex(index: Int)
    suspend fun setAlarmRingtoneIndex(index: Int)
    suspend fun setShowWorldClockGlobe(enabled: Boolean)
    suspend fun setWorldClockGlobeStyle(style: Int)
    suspend fun setTemperatureUnit(unit: Int)
    suspend fun setSavedCities(cities: Set<String>)
    suspend fun setSnoozeDuration(minutes: Int)
    suspend fun setAlarmFadeInDuration(seconds: Int)
    suspend fun setWeatherRefreshInterval(minutes: Int)
    suspend fun setAutoCloudBackup(enabled: Boolean)
    suspend fun setLastBackupTimestamp(timestamp: Long)
}
