sed -i '/val calendar = Calendar.getInstance()/,/val currentMillis = System.currentTimeMillis()/c\
                val currentTimeStr = current.getString("time")\
                val hour = currentTimeStr.substring(11, 13).toInt()\
                val timeOfDayStr = when (hour) {\
                    in 0..4 -> "Dini Hari"\
                    in 5..10 -> "Pagi"\
                    in 11..14 -> "Siang"\
                    in 15..17 -> "Sore"\
                    else -> "Malam"\
                }\
\
                val hourlyList = mutableListOf<HourlyForecast>()\
                val hourlyJson = json.getJSONObject("hourly")\
                val timeArr = hourlyJson.getJSONArray("time")\
                val tempArr = hourlyJson.getJSONArray("temperature_2m")\
                val codeArr = hourlyJson.getJSONArray("weather_code")\
                val isDayArr = hourlyJson.getJSONArray("is_day")\
                val precipArr = hourlyJson.getJSONArray("precipitation_probability")' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
