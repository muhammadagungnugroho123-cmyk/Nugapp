package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val alarmRepository: com.example.domain.repository.AlarmRepository? = null
) : ViewModel() {

    val themeColorIndex: StateFlow<Int> = settingsRepository.themeColorIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val is24HourFormat: StateFlow<Boolean> = settingsRepository.is24HourFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultVibrate: StateFlow<Boolean> = settingsRepository.defaultVibrate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val appLanguage: StateFlow<String> = settingsRepository.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val clockStyleIndex: StateFlow<Int> = settingsRepository.clockStyleIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val showWorldClockGlobe: StateFlow<Boolean> = settingsRepository.showWorldClockGlobe
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val worldClockGlobeStyle: StateFlow<Int> = settingsRepository.worldClockGlobeStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val temperatureUnit: StateFlow<Int> = settingsRepository.temperatureUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val alarmRingtoneIndex: StateFlow<Int> = settingsRepository.alarmRingtoneIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val snoozeDuration: StateFlow<Int> = settingsRepository.snoozeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 9)

    val alarmFadeInDuration: StateFlow<Int> = settingsRepository.alarmFadeInDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val weatherRefreshInterval: StateFlow<Int> = settingsRepository.weatherRefreshInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)

    val autoCloudBackup: StateFlow<Boolean> = settingsRepository.autoCloudBackup
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val lastBackupTimestamp: StateFlow<Long> = settingsRepository.lastBackupTimestamp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setThemeColorIndex(index: Int) {
        viewModelScope.launch {
            settingsRepository.setThemeColorIndex(index)
        }
    }

    fun set24HourFormat(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.set24HourFormat(enabled)
        }
    }

    fun setDefaultVibrate(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDefaultVibrate(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setAppLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(language)
        }
    }

    fun setClockStyleIndex(index: Int) {
        viewModelScope.launch {
            settingsRepository.setClockStyleIndex(index)
        }
    }

    fun setShowWorldClockGlobe(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowWorldClockGlobe(enabled)
        }
    }

    fun setWorldClockGlobeStyle(style: Int) {
        viewModelScope.launch {
            settingsRepository.setWorldClockGlobeStyle(style)
        }
    }

    fun setTemperatureUnit(unit: Int) {
        viewModelScope.launch {
            settingsRepository.setTemperatureUnit(unit)
        }
    }
    fun setAlarmRingtoneIndex(index: Int) {
        viewModelScope.launch {
            settingsRepository.setAlarmRingtoneIndex(index)
        }
    }

    fun setSnoozeDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setSnoozeDuration(minutes)
        }
    }

    fun setAlarmFadeInDuration(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setAlarmFadeInDuration(seconds)
        }
    }

    fun setWeatherRefreshInterval(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setWeatherRefreshInterval(minutes)
        }
    }

    fun setAutoCloudBackup(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCloudBackup(enabled)
        }
    }

    fun setLastBackupTimestamp(timestamp: Long) {
        viewModelScope.launch {
            settingsRepository.setLastBackupTimestamp(timestamp)
        }
    }

    fun getAlarmRepository(): com.example.domain.repository.AlarmRepository? = alarmRepository

    fun exportBackup(context: android.content.Context, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            alarmRepository?.let { alarmRepo ->
                val json = BackupManager.generateBackupJson(settingsRepository, alarmRepo)
                onSuccess(json)
            }
        }
    }

    fun importBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            alarmRepository?.let { alarmRepo ->
                val success = BackupManager.restoreBackupJson(jsonString, settingsRepository, alarmRepo)
                if (success) {
                    settingsRepository.setLastBackupTimestamp(System.currentTimeMillis())
                }
                onResult(success)
            } ?: onResult(false)
        }
    }

    fun triggerCloudSync(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            delay(1500)
            settingsRepository.setLastBackupTimestamp(System.currentTimeMillis())
            onResult(true)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.setThemeColorIndex(0)
            settingsRepository.set24HourFormat(true)
            settingsRepository.setDefaultVibrate(true)
            settingsRepository.setNotificationsEnabled(true)
            settingsRepository.setAppLanguage("en")
            settingsRepository.setClockStyleIndex(0)
            settingsRepository.setAlarmRingtoneIndex(0)
            settingsRepository.setShowWorldClockGlobe(true)
            settingsRepository.setWorldClockGlobeStyle(1)
            settingsRepository.setTemperatureUnit(0)
            settingsRepository.setSavedCities(setOf("London", "New York", "Tokyo", "Sydney"))
            settingsRepository.setSnoozeDuration(9)
            settingsRepository.setAlarmFadeInDuration(10)
            settingsRepository.setWeatherRefreshInterval(60)
        }
    }

    companion object {
        fun provideFactory(
            settingsRepository: SettingsRepository,
            alarmRepository: com.example.domain.repository.AlarmRepository? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(settingsRepository, alarmRepository) as T
                }
            }
    }
}
