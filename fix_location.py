import re

with open('app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt', 'r') as f:
    content = f.read()

# We want to replace the whole LaunchedEffect(hasLocationPermission, refreshTrigger) block.
# We'll use a regex that matches from LaunchedEffect(hasLocationPermission, refreshTrigger) to the end of that block.
# Wait, it's safer to just replace lines 176 to 257.

lines = content.split('\n')

new_block = """    LaunchedEffect(hasLocationPermission, refreshTrigger) {
        if (hasLocationPermission) {
            isLoading = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            
            val performFetch = { lat: Double, lon: Double -> 
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    var locName = "Unknown"
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            locName = "${address.locality ?: address.subAdminArea ?: address.adminArea}, ${address.countryCode}"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        locName = "Lat: ${"%.2f".format(lat)}"
                    }

                    try {
                        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m&hourly=temperature_2m,weather_code,precipitation_probability,is_day&timezone=auto&forecast_days=2"
                        val response = URL(url).readText()
                        val json = JSONObject(response)
                        val current = json.getJSONObject("current")
                        val currentTimeStrOpenMeteo = current.getString("time")
                        val hourOpenMeteo = currentTimeStrOpenMeteo.substring(11, 13).toInt()
                        val timeOfDayStr = when (hourOpenMeteo) {
                            in 0..4 -> "Dini Hari"
                            in 5..10 -> "Pagi"
                            in 11..14 -> "Siang"
                            in 15..17 -> "Sore"
                            else -> "Malam"
                        }
                        val temp = current.getDouble("temperature_2m")
                        val humidity = current.getInt("relative_humidity_2m")
                        val windSpeed = current.getDouble("wind_speed_10m")
                        val isDay = current.getInt("is_day") == 1
                        val code = current.getInt("weather_code")
                        
                        val desc = when(code) {
                            0 -> "Cerah"
                            1, 2, 3 -> "Cerah Berawan"
                            45, 48 -> "Berkabut"
                            51, 53, 55 -> "Gerimis"
                            61, 63, 65 -> "Hujan"
                            71, 73, 75 -> "Salju"
                            95, 96, 99 -> "Badai Petir"
                            else -> "Berawan"
                        }

                        val hourlyList = mutableListOf<HourlyForecast>()
                        val hourlyJson = json.getJSONObject("hourly")
                        val timeArr = hourlyJson.getJSONArray("time")
                        val tempArr = hourlyJson.getJSONArray("temperature_2m")
                        val codeArr = hourlyJson.getJSONArray("weather_code")
                        val isDayArr = hourlyJson.getJSONArray("is_day")
                        val precipArr = hourlyJson.getJSONArray("precipitation_probability")
                        
                        val currentTimeStr = current.getString("time")
                        var startIndex = 0
                        for (i in 0 until timeArr.length()) {
                            if (timeArr.getString(i) >= currentTimeStr) {
                                startIndex = i
                                break
                            }
                        }
                        
                        for (i in startIndex until minOf(startIndex + 4, timeArr.length())) {
                            val timeStr = timeArr.getString(i).substring(11)
                            val hTemp = tempArr.getDouble(i)
                            val hCode = codeArr.getInt(i)
                            val hIsDay = isDayArr.getInt(i) == 1
                            val hPrecip = precipArr.getInt(i)
                            hourlyList.add(HourlyForecast(timeStr, hTemp, hCode, hIsDay, hPrecip))
                        }

                        withContext(Dispatchers.Main) {
                            weatherData = WeatherData(temp, humidity, windSpeed, desc, isDay, locName, timeOfDayStr, code, json.optInt("utc_offset_seconds", 0), hourlyList)
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            isLoading = false
                        }
                    }
                }
            }

            try {
                fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->
                    if (location != null) {
                        performFetch(location.latitude, location.longitude)
                    } else {
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            val lat = lastLoc?.latitude ?: -6.2088
                            val lon = lastLoc?.longitude ?: 106.8456
                            performFetch(lat, lon)
                        }.addOnFailureListener {
                            performFetch(-6.2088, 106.8456)
                        }
                    }
                }.addOnFailureListener {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        val lat = lastLoc?.latitude ?: -6.2088
                        val lon = lastLoc?.longitude ?: 106.8456
                        performFetch(lat, lon)
                    }.addOnFailureListener {
                        performFetch(-6.2088, 106.8456)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }"""

# Insert it between line 175 and 258
before = lines[:175]
after = lines[257:]

new_content = '\n'.join(before) + '\n' + new_block + '\n' + '\n'.join(after)

with open('app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt', 'w') as f:
    f.write(new_content)
