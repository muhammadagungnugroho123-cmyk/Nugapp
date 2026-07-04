package com.example.ui.screens.worldclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModelProvider
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.collectLatest

class WorldClockViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _cities = MutableStateFlow<List<WorldCity>>(emptyList())
    val cities: StateFlow<List<WorldCity>> = _cities.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.savedCities.collectLatest { savedNames ->
                val loadedCities = savedNames.mapNotNull { name -> availableCities.find { it.name == name } }
                _cities.value = if (loadedCities.isEmpty()) {
                    listOf(
                        availableCities.find { it.name == "London" }!!,
                        availableCities.find { it.name == "New York" }!!,
                        availableCities.find { it.name == "Tokyo" }!!,
                        availableCities.find { it.name == "Sydney" }!!
                    )
                } else {
                    loadedCities
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTime.value = System.currentTimeMillis()
            }
        }
    }

    val availableCities = listOf(
        WorldCity("London", "UK", "Europe/London", 51.5f, -0.1f, "🇬🇧"),
        WorldCity("New York", "USA", "America/New_York", 40.7f, -74.0f, "🇺🇸"),
        WorldCity("Tokyo", "Japan", "Asia/Tokyo", 35.6f, 139.6f, "🇯🇵"),
        WorldCity("Sydney", "Australia", "Australia/Sydney", -33.8f, 151.2f, "🇦🇺"),
        WorldCity("Paris", "France", "Europe/Paris", 48.8f, 2.3f, "🇫🇷"),
        WorldCity("Dubai", "UAE", "Asia/Dubai", 25.2f, 55.2f, "🇦🇪"),
        WorldCity("Singapore", "Singapore", "Asia/Singapore", 1.3f, 103.8f, "🇸🇬"),
        WorldCity("Hong Kong", "China", "Asia/Hong_Kong", 22.3f, 114.1f, "🇭🇰"),
        WorldCity("Los Angeles", "USA", "America/Los_Angeles", 34.0f, -118.2f, "🇺🇸"),
        WorldCity("Chicago", "USA", "America/Chicago", 41.8f, -87.6f, "🇺🇸"),
        WorldCity("Toronto", "Canada", "America/Toronto", 43.7f, -79.3f, "🇨🇦"),
        WorldCity("Seoul", "South Korea", "Asia/Seoul", 37.5f, 126.9f, "🇰🇷"),
        WorldCity("Moscow", "Russia", "Europe/Moscow", 55.7f, 37.6f, "🇷🇺"),
        WorldCity("Mumbai", "India", "Asia/Kolkata", 19.0f, 72.8f, "🇮🇳"),
        WorldCity("Beijing", "China", "Asia/Shanghai", 39.9f, 116.4f, "🇨🇳"),
        WorldCity("Istanbul", "Turkey", "Europe/Istanbul", 41.0f, 28.9f, "🇹🇷"),
        WorldCity("São Paulo", "Brazil", "America/Sao_Paulo", -23.5f, -46.6f, "🇧🇷"),
        WorldCity("Mexico City", "Mexico", "America/Mexico_City", 19.4f, -99.1f, "🇲🇽"),
        WorldCity("Jakarta", "Indonesia", "Asia/Jakarta", -6.2f, 106.8f, "🇮🇩"),
        WorldCity("Bangkok", "Thailand", "Asia/Bangkok", 13.7f, 100.5f, "🇹🇭"),
        WorldCity("Berlin", "Germany", "Europe/Berlin", 52.5f, 13.4f, "🇩🇪"),
        WorldCity("Rome", "Italy", "Europe/Rome", 41.9f, 12.5f, "🇮🇹"),
        WorldCity("Madrid", "Spain", "Europe/Madrid", 40.4f, -3.7f, "🇪🇸"),
        WorldCity("Cape Town", "South Africa", "Africa/Johannesburg", -33.9f, 18.4f, "🇿🇦"),
        WorldCity("Cairo", "Egypt", "Africa/Cairo", 30.0f, 31.2f, "🇪🇬"),
        WorldCity("Buenos Aires", "Argentina", "America/Argentina/Buenos_Aires", -34.6f, -58.3f, "🇦🇷"),
        WorldCity("Lima", "Peru", "America/Lima", -12.0f, -77.0f, "🇵🇪"),
        WorldCity("Bogotá", "Colombia", "America/Bogota", 4.7f, -74.0f, "🇨🇴"),
        WorldCity("Riyadh", "Saudi Arabia", "Asia/Riyadh", 24.7f, 46.7f, "🇸🇦"),
        WorldCity("Tehran", "Iran", "Asia/Tehran", 35.6f, 51.3f, "🇮🇷"),
        WorldCity("Karachi", "Pakistan", "Asia/Karachi", 24.8f, 67.0f, "🇵🇰"),
        WorldCity("Manila", "Philippines", "Asia/Manila", 14.5f, 120.9f, "🇵🇭"),
        WorldCity("Kuala Lumpur", "Malaysia", "Asia/Kuala_Lumpur", 3.1f, 101.6f, "🇲🇾"),
        WorldCity("Hanoi", "Vietnam", "Asia/Ho_Chi_Minh", 21.0f, 105.8f, "🇻🇳"),
        WorldCity("Taipei", "Taiwan", "Asia/Taipei", 25.0f, 121.5f, "🇹🇼"),
        WorldCity("Wellington", "New Zealand", "Pacific/Auckland", -41.2f, 174.7f, "🇳🇿"),
        WorldCity("Stockholm", "Sweden", "Europe/Stockholm", 59.3f, 18.0f, "🇸🇪"),
        WorldCity("Oslo", "Norway", "Europe/Oslo", 59.9f, 10.7f, "🇳🇴"),
        WorldCity("Copenhagen", "Denmark", "Europe/Copenhagen", 55.6f, 12.5f, "🇩🇰"),
        WorldCity("Helsinki", "Finland", "Europe/Helsinki", 60.1f, 24.9f, "🇫🇮"),
        WorldCity("Amsterdam", "Netherlands", "Europe/Amsterdam", 52.3f, 4.9f, "🇳🇱"),
        WorldCity("Brussels", "Belgium", "Europe/Brussels", 50.8f, 4.3f, "🇧🇪"),
        WorldCity("Vienna", "Austria", "Europe/Vienna", 48.2f, 16.3f, "🇦🇹"),
        WorldCity("Athens", "Greece", "Europe/Athens", 37.9f, 23.7f, "🇬🇷")
    )

    fun addCity(city: WorldCity) {
        val currentList = _cities.value.toMutableList()
        if (currentList.none { it.name == city.name }) {
            currentList.add(city)
            _cities.value = currentList
            viewModelScope.launch {
                settingsRepository.setSavedCities(currentList.map { it.name }.toSet())
            }
        }
    }
    
    companion object {
        fun provideFactory(settingsRepository: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WorldClockViewModel(settingsRepository) as T
                }
            }
    }
}
