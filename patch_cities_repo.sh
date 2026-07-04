sed -i '/val temperatureUnit: Flow<Int>/a\    val savedCities: Flow<Set<String>>' app/src/main/java/com/example/domain/repository/SettingsRepository.kt
sed -i '/suspend fun setTemperatureUnit/a\    suspend fun setSavedCities(cities: Set<String>)' app/src/main/java/com/example/domain/repository/SettingsRepository.kt
