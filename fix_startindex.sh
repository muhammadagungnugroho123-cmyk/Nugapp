sed -i '/var startIndex = 0/,/}/c\
                var startIndex = 0\
                for (i in 0 until timeArr.length()) {\
                    if (timeArr.getString(i) >= currentTimeStr) {\
                        startIndex = i\
                        break\
                    }\
                }' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
