sed -i '/val currentDateTime = java.text.SimpleDateFormat/i \
                                        var weatherInfo = "User has no saved cities."\
                                        try {\
                                            val prefs = context.getSharedPreferences("weather_prefs", android.content.Context.MODE_PRIVATE)\
                                            val savedCities = prefs.getString("saved_cities", "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()\
                                            if (savedCities.isNotEmpty()) {\
                                                val city = savedCities.first()\
                                                val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${city.replace(" ", "+")}&count=1"\
                                                val geoResponse = java.net.URL(geoUrl).readText()\
                                                val geoJson = org.json.JSONObject(geoResponse)\
                                                if (geoJson.has("results")) {\
                                                    val results = geoJson.getJSONArray("results")\
                                                    val lat = results.getJSONObject(0).getDouble("latitude")\
                                                    val lon = results.getJSONObject(0).getDouble("longitude")\
                                                    val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto"\
                                                    val wRes = java.net.URL(weatherUrl).readText()\
                                                    val wJson = org.json.JSONObject(wRes).getJSONObject("current")\
                                                    weatherInfo = "Current weather in $city: ${wJson.getDouble("temperature_2m")}°C, Code: ${wJson.getInt("weather_code")}, Wind: ${wJson.getDouble("wind_speed_10m")}km/h."\
                                                }\
                                            }\
                                        } catch(e: Exception) { }\
' app/src/main/java/com/example/ui/screens/ai/AiAssistantScreen.kt
