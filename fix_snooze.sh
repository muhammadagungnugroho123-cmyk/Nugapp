sed -i '57,83c\
        if (intent?.action == ACTION_SNOOZE) {\
            if (alarmId != -1) {\
                val appContainer = (application as ClockApplication).container\
                CoroutineScope(Dispatchers.IO).launch {\
                    var soundUriStr = ""\
                    var isVibrateSetting = true\
                    val alarm = appContainer.alarmRepository.getAlarmById(alarmId)\
                    if (alarm != null) {\
                        soundUriStr = alarm.soundUri\
                        isVibrateSetting = alarm.isVibrate\
                        if (alarm.label.startsWith("Snooze:")) {\
                            appContainer.alarmRepository.deleteAlarmById(alarm.id)\
                        } else if (alarm.daysOfWeek == 0) {\
                            appContainer.alarmRepository.updateAlarm(alarm.copy(isEnabled = false))\
                        }\
                    }\
                    val cal = java.util.Calendar.getInstance()\
                    cal.add(java.util.Calendar.MINUTE, 10)\
                    val snoozeAlarm = com.example.data.local.AlarmEntity(\
                        id = 0,\
                        hour = cal.get(java.util.Calendar.HOUR_OF_DAY),\
                        minute = cal.get(java.util.Calendar.MINUTE),\
                        label = if (alarmLabel.startsWith("Snooze:")) alarmLabel else "Snooze: $alarmLabel",\
                        isEnabled = true,\
                        daysOfWeek = 0,\
                        dateMillis = cal.timeInMillis,\
                        soundUri = soundUriStr,\
                        isVibrate = isVibrateSetting\
                    )\
                    appContainer.alarmRepository.insertAlarm(snoozeAlarm)\
                }\
            }\
            stopAlarm()\
            stopSelf()\
            return START_NOT_STICKY\
        }\
' app/src/main/java/com/example/data/alarm/AlarmService.kt

sed -i 's/Snooze (5 mnt)/Snooze/g' app/src/main/java/com/example/data/alarm/AlarmService.kt
