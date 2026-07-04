package com.example.data.repository

import com.example.data.local.AlarmDao
import com.example.data.local.AlarmEntity
import com.example.domain.alarm.AlarmScheduler
import com.example.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) : AlarmRepository {
    override fun getAllAlarms(): Flow<List<AlarmEntity>> {
        return alarmDao.getAllAlarms()
    }

    override suspend fun getAlarmById(id: Int): AlarmEntity? {
        return alarmDao.getAlarmById(id)
    }

    override suspend fun insertAlarm(alarm: AlarmEntity) {
        val id = alarmDao.insertAlarm(alarm).toInt()
        val savedAlarm = alarm.copy(id = id)
        if (savedAlarm.isEnabled) {
            alarmScheduler.schedule(savedAlarm)
        }
    }

    override suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.updateAlarm(alarm)
        if (alarm.isEnabled) {
            alarmScheduler.schedule(alarm)
        } else {
            alarmScheduler.cancel(alarm)
        }
    }

    override suspend fun deleteAlarmById(id: Int) {
        alarmScheduler.cancel(AlarmEntity(id = id, hour = 0, minute = 0))
        alarmDao.deleteAlarmById(id)
    }
}
