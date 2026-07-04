package com.example.domain.alarm

import com.example.data.local.AlarmEntity

interface AlarmScheduler {
    fun schedule(alarm: AlarmEntity)
    fun cancel(alarm: AlarmEntity)
}
