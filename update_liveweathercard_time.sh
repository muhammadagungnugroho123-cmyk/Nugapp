sed -i '/val calendar = Calendar.getInstance()/,/}/d' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
sed -i '/val current = json.getJSONObject("current")/a\
                            val currentTimeStrOpenMeteo = current.getString("time")\
                            val hourOpenMeteo = currentTimeStrOpenMeteo.substring(11, 13).toInt()\
                            val timeOfDayStr = when (hourOpenMeteo) {\
                                in 0..4 -> "Dini Hari"\
                                in 5..10 -> "Pagi"\
                                in 11..14 -> "Siang"\
                                in 15..17 -> "Sore"\
                                else -> "Malam"\
                            }' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
