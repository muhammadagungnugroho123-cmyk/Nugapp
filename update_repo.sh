sed -i '/val showWorldClockGlobe: Flow<Boolean>/a\    val worldClockGlobeStyle: Flow<Int>' app/src/main/java/com/example/domain/repository/SettingsRepository.kt
sed -i '/suspend fun setShowWorldClockGlobe(enabled: Boolean)/a\    suspend fun setWorldClockGlobeStyle(style: Int)' app/src/main/java/com/example/domain/repository/SettingsRepository.kt
