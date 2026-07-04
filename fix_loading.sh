sed -i '/fun loadWeatherForSavedCities/,/    }/c\
    fun loadWeatherForSavedCities() {\
        coroutineScope.launch {\
            isLoadingList = true\
            val deferreds = savedCities.map { city ->\
                async { fetchWeatherData(city) }\
            }\
            val results = deferreds.awaitAll().filterNotNull()\
            weatherDataList.clear()\
            weatherDataList.addAll(results)\
            isLoadingList = false\
        }\
    }' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
