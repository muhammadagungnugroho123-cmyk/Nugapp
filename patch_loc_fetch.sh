awk '/var finalLat = -6.2088/ {
    print "                    val performFetch = { lat: Double, lon: Double -> "
    print "                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {"
    print "                            var locName = \"Unknown\""
    print "                            try {"
    print "                                val geocoder = Geocoder(context, Locale.getDefault())"
    print "                                @Suppress(\"DEPRECATION\")"
    print "                                val addresses = geocoder.getFromLocation(lat, lon, 1)"
    print "                                if (!addresses.isNullOrEmpty()) {"
    print "                                    val address = addresses[0]"
    print "                                    locName = \"${address.locality ?: address.subAdminArea ?: address.adminArea}, ${address.countryCode}\""
    print "                                }"
    print "                            } catch (e: Exception) {"
    print "                                e.printStackTrace()"
    print "                                locName = \"Lat: ${\"%.2f\".format(lat)}\""
    print "                            }"
    print ""
    print "                            try {"
    print "                                val url = \"https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m&hourly=temperature_2m,weather_code,precipitation_probability,is_day&timezone=auto&forecast_days=2\""
    in_fetch = 1
    next
}
/val url = "https:\/\/api.open-meteo.com\/v1\/forecast/ {
    if (in_fetch) {
        # Already handled
        in_skip = 1
        next
    }
}
/weatherData = WeatherData/ {
    if (in_skip) {
        print $0
        next
    }
}
/isLoading = false/ {
    if (in_skip) {
        print $0
        print "                            }"
        print "                        } catch (e: Exception) {"
        print "                            e.printStackTrace()"
        print "                            withContext(Dispatchers.Main) {"
        print "                                isLoading = false"
        print "                            }"
        print "                        }"
        print "                    }"
        print "                }"
        print "                if (location != null) {"
        print "                    performFetch(location.latitude, location.longitude)"
        print "                } else {"
        print "                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->"
        print "                        val lat = lastLoc?.latitude ?: -6.2088"
        print "                        val lon = lastLoc?.longitude ?: 106.8456"
        print "                        performFetch(lat, lon)"
        print "                    }.addOnFailureListener {"
        print "                        performFetch(-6.2088, 106.8456)"
        print "                    }"
        print "                }"
        in_skip = 0
        in_fetch = 0
        skip_lines = 4 # Skip the catch block that follows isLoading = false in original code
        next
    }
}
skip_lines > 0 {
    skip_lines--
    next
}
!in_skip { print }
' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
