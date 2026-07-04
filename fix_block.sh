sed -i '99,127c\
                        // Fetch Weather and Location\
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {\
                            var locName = "Unknown"\
                            try {\
                                val geocoder = Geocoder(context, Locale.getDefault())\
                                val addresses = geocoder.getFromLocation(lat, lon, 1)\
                                if (!addresses.isNullOrEmpty()) {\
                                    val address = addresses[0]\
                                    locName = "${address.locality ?: address.subAdminArea ?: address.adminArea}, ${address.countryCode}"\
                                }\
                            } catch (e: Exception) {\
                                e.printStackTrace()\
                                locName = "Lat: ${"%.2f".format(lat)}"\
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
                                val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m&timezone=auto"\
                                val response = URL(url).readText()' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
