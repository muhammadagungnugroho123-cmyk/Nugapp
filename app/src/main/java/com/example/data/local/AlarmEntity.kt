package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "",
    val daysOfWeek: Int = 0, // Bitmask for repeating days
    val dateMillis: Long = 0L, // Specific date timestamp
    val isVibrate: Boolean = true,
    val soundUri: String = "",
    val isSnooze: Boolean = true
)
