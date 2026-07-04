sed -i '89,168c\
    LaunchedEffect(hasLocationPermission) {\
        if (hasLocationPermission) {\
            while (true) {\
                isLoading = weatherData == null\
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)\
                try {\
                    fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->\
                        var finalLat = -6.2088 // Default Jakarta\
                        var finalLon = 106.8456\
                        if (location != null) {\
                            finalLat = location.latitude\
                            finalLon = location.longitude\
                        }\
                        \
                        // Fetch Weather and Location\
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {\
                            var locName = "Unknown"\
                            try {\
                                val geocoder = Geocoder(context, Locale.getDefault())\
                                val addresses = geocoder.getFromLocation(finalLat, finalLon, 1)\
                                if (!addresses.isNullOrEmpty()) {\
                                    val address = addresses[0]\
                                    locName = "${address.locality ?: address.subAdminArea ?: address.adminArea}, ${address.countryCode}"\
                                }\
                            } catch (e: Exception) {\
                                e.printStackTrace()\
                                locName = "Lat: ${"%.2f".format(finalLat)}"\
                            }\
\
                            // Time of day logic\
                            val calendar = Calendar.getInstance()\
                            val hour = calendar.get(Calendar.HOUR_OF_DAY)\
                            val timeOfDayStr = when (hour) {\
                                in 0..4 -> "Dini Hari"\
                                in 5..10 -> "Pagi"\
                                in 11..14 -> "Siang"\
                                in 15..17 -> "Sore"\
                                else -> "Malam"\
                            }\
\
                            try {\
                                val url = "https://api.open-meteo.com/v1/forecast?latitude=$finalLat&longitude=$finalLon&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m&timezone=auto"\
                                val response = URL(url).readText()\
                                val json = JSONObject(response)\
                                val current = json.getJSONObject("current")\
                                val temp = current.getDouble("temperature_2m")\
                                val humidity = current.getInt("relative_humidity_2m")\
                                val windSpeed = current.getDouble("wind_speed_10m")\
                                val isDay = current.getInt("is_day") == 1\
                                val code = current.getInt("weather_code")\
                                \
                                val desc = when(code) {\
                                    0 -> "Cerah"\
                                    1, 2, 3 -> "Cerah Berawan"\
                                    45, 48 -> "Berkabut"\
                                    51, 53, 55 -> "Gerimis"\
                                    61, 63, 65 -> "Hujan"\
                                    71, 73, 75 -> "Salju"\
                                    95, 96, 99 -> "Badai Petir"\
                                    else -> "Berawan"\
                                }\
\
                                withContext(Dispatchers.Main) {\
                                    weatherData = WeatherData(temp, humidity, windSpeed, desc, isDay, locName, timeOfDayStr, code)\
                                    isLoading = false\
                                }\
                            } catch (e: Exception) {\
                                e.printStackTrace()\
                                withContext(Dispatchers.Main) {\
                                    isLoading = false\
                                }\
                            }\
                        }\
                    }.addOnFailureListener {\
                        isLoading = false\
                    }\
                } catch (e: SecurityException) {\
                    e.printStackTrace()\
                    isLoading = false\
                }\
                kotlinx.coroutines.delay(300000) // Update every 5 minutes\
            }\
        }\
    }' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
