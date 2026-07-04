sed -i 's/data.description,/"${data.description} • ${data.timeOfDayStr}",/g' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
