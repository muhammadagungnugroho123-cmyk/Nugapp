sed -i '/val worldClockGlobeStyle: Flow<Int>/a\    val temperatureUnit: Flow<Int>' app/src/main/java/com/example/domain/repository/SettingsRepository.kt
sed -i '/suspend fun setWorldClockGlobeStyle(style: Int)/a\    suspend fun setTemperatureUnit(unit: Int)' app/src/main/java/com/example/domain/repository/SettingsRepository.kt
