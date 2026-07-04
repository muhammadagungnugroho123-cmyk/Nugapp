awk '/var finalLat = -6.2088/ {
    print "                    if (location == null) {"
    print "                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->"
    print "                            val finalLat = lastLoc?.latitude ?: -6.2088"
    print "                            val finalLon = lastLoc?.longitude ?: 106.8456"
    print "                            fetchWeather(context, finalLat, finalLon) { data ->"
    print "                                weatherData = data"
    print "                                isLoading = false"
    print "                            }"
    print "                        }"
    print "                    } else {"
    print "                        val finalLat = location.latitude"
    print "                        val finalLon = location.longitude"
    print "                        fetchWeather(context, finalLat, finalLon) { data ->"
    print "                            weatherData = data"
    print "                            isLoading = false"
    print "                        }"
    print "                    }"
    in_loc = 1
    next
}
/isLoading = false/ && in_loc {
    if (skip_is_loading) {
        skip_is_loading = 0
    }
}
/var locName = "Unknown"/ {
    if (in_loc) {
        in_skip = 1
    }
}
/weatherData = WeatherData/ {
    if (in_skip) {
        in_skip = 0
        in_loc = 0
        next
    }
}
!in_skip && !in_loc { print }
' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
