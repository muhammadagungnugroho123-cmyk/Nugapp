package com.example.domain.repository

import com.example.data.local.AlarmEntity
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<AlarmEntity>>
    suspend fun getAlarmById(id: Int): AlarmEntity?
    suspend fun insertAlarm(alarm: AlarmEntity)
    suspend fun updateAlarm(alarm: AlarmEntity)
    suspend fun deleteAlarmById(id: Int)
}
