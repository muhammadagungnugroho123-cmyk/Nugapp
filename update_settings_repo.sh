sed -i '/val alarmRingtoneIndex: Flow<Int>/a\    val showWorldClockGlobe: Flow<Boolean>' app/src/main/java/com/example/domain/repository/SettingsRepository.kt
sed -i '/suspend fun setAlarmRingtoneIndex(index: Int)/a\    suspend fun setShowWorldClockGlobe(enabled: Boolean)' app/src/main/java/com/example/domain/repository/SettingsRepository.kt
