package com.example.di

import android.content.Context
import com.example.data.alarm.AlarmSchedulerImpl
import com.example.data.local.AppDatabase
import com.example.data.repository.AlarmRepositoryImpl
import com.example.domain.alarm.AlarmScheduler
import com.example.domain.repository.AlarmRepository

import com.example.data.repository.SettingsRepositoryImpl
import com.example.domain.repository.SettingsRepository

class AppContainer(private val context: Context) {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    val alarmScheduler: AlarmScheduler by lazy { AlarmSchedulerImpl(context) }
    
    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(context) }
    
    val alarmRepository: AlarmRepository by lazy {
        AlarmRepositoryImpl(database.alarmDao(), alarmScheduler)
    }
}
