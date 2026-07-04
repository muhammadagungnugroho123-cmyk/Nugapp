package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_COLOR = intPreferencesKey("theme_color")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val DEFAULT_VIBRATE = booleanPreferencesKey("default_vibrate")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val CLOCK_STYLE_INDEX = intPreferencesKey("clock_style_index")
        val ALARM_RINGTONE_INDEX = intPreferencesKey("alarm_ringtone_index")
        val SHOW_WORLD_CLOCK_GLOBE = booleanPreferencesKey("show_world_clock_globe")
        val WORLD_CLOCK_GLOBE_STYLE = intPreferencesKey("world_clock_globe_style")
        val TEMPERATURE_UNIT = intPreferencesKey("temperature_unit")
        val SAVED_CITIES = stringSetPreferencesKey("saved_cities")
        val SNOOZE_DURATION = intPreferencesKey("snooze_duration")
        val ALARM_FADE_IN_DURATION = intPreferencesKey("alarm_fade_in_duration")
        val WEATHER_REFRESH_INTERVAL = intPreferencesKey("weather_refresh_interval")
        val AUTO_CLOUD_BACKUP = booleanPreferencesKey("auto_cloud_backup")
        val LAST_BACKUP_TIMESTAMP = androidx.datastore.preferences.core.longPreferencesKey("last_backup_timestamp")
    }

    override val themeColorIndex: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_COLOR] ?: 0
    }

    override val is24HourFormat: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_24_HOUR] ?: true
    }

    override val defaultVibrate: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_VIBRATE] ?: true
    }

    override val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    override val appLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_LANGUAGE] ?: "system"
    }

    override val clockStyleIndex: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CLOCK_STYLE_INDEX] ?: 0
    }

    override val showWorldClockGlobe: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_WORLD_CLOCK_GLOBE] ?: true
    }

    override val worldClockGlobeStyle: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WORLD_CLOCK_GLOBE_STYLE] ?: 1
    }

    override val savedCities: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SAVED_CITIES] ?: setOf("London", "New York", "Tokyo", "Sydney")
    }

    override val temperatureUnit: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TEMPERATURE_UNIT] ?: 0
    }
    override val alarmRingtoneIndex: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ALARM_RINGTONE_INDEX] ?: 0
    }

    override val snoozeDuration: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SNOOZE_DURATION] ?: 9
    }

    override val alarmFadeInDuration: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ALARM_FADE_IN_DURATION] ?: 10
    }

    override val weatherRefreshInterval: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WEATHER_REFRESH_INTERVAL] ?: 60
    }

    override val autoCloudBackup: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_CLOUD_BACKUP] ?: true
    }

    override val lastBackupTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_BACKUP_TIMESTAMP] ?: 0L
    }

    override suspend fun setThemeColorIndex(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_COLOR] = index
        }
    }

    override suspend fun set24HourFormat(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_24_HOUR] = enabled
        }
    }

    override suspend fun setDefaultVibrate(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_VIBRATE] = enabled
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE] = language
        }
    }

    override suspend fun setClockStyleIndex(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOCK_STYLE_INDEX] = index
        }
    }

    override suspend fun setShowWorldClockGlobe(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_WORLD_CLOCK_GLOBE] = enabled
        }
    }

    override suspend fun setWorldClockGlobeStyle(style: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORLD_CLOCK_GLOBE_STYLE] = style
        }
    }

    override suspend fun setSavedCities(cities: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_CITIES] = cities
        }
    }

    override suspend fun setTemperatureUnit(unit: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE_UNIT] = unit
        }
    }
    override suspend fun setAlarmRingtoneIndex(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALARM_RINGTONE_INDEX] = index
        }
    }

    override suspend fun setSnoozeDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SNOOZE_DURATION] = minutes
        }
    }

    override suspend fun setAlarmFadeInDuration(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALARM_FADE_IN_DURATION] = seconds
        }
    }

    override suspend fun setWeatherRefreshInterval(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEATHER_REFRESH_INTERVAL] = minutes
        }
    }

    override suspend fun setAutoCloudBackup(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_CLOUD_BACKUP] = enabled
        }
    }

    override suspend fun setLastBackupTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BACKUP_TIMESTAMP] = timestamp
        }
    }
}
