import sys

with open('app/src/main/java/com/example/ui/screens/worldclock/WorldClockViewModel.kt', 'r') as f:
    content = f.read()

# We need to change the class signature
content = content.replace("class WorldClockViewModel : ViewModel() {", """import androidx.lifecycle.ViewModelProvider
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.collectLatest

class WorldClockViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {""")

# We remove the hardcoded _cities initial value
content = content.replace("""    private val _cities = MutableStateFlow(
        listOf(
            WorldCity("London", "UK", "Europe/London", 51.5f, -0.1f, "🇬🇧"),
            WorldCity("New York", "USA", "America/New_York", 40.7f, -74.0f, "🇺🇸"),
            WorldCity("Tokyo", "Japan", "Asia/Tokyo", 35.6f, 139.6f, "🇯🇵"),
            WorldCity("Sydney", "Australia", "Australia/Sydney", -33.8f, 151.2f, "🇦🇺")
        )
    )""", """    private val _cities = MutableStateFlow<List<WorldCity>>(emptyList())""")

# In init block, load from settings
init_old = """    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTime.value = System.currentTimeMillis()
            }
        }
    }"""
init_new = """    init {
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
    }"""
content = content.replace(init_old, init_new)

# In addCity, save back to settings
add_city_old = """    fun addCity(city: WorldCity) {
        val currentList = _cities.value.toMutableList()
        if (currentList.none { it.name == city.name }) {
            currentList.add(city)
            _cities.value = currentList
        }
    }"""
add_city_new = """    fun addCity(city: WorldCity) {
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
    }"""
content = content.replace(add_city_old, add_city_new)

with open('app/src/main/java/com/example/ui/screens/worldclock/WorldClockViewModel.kt', 'w') as f:
    f.write(content)
