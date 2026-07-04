sed -i 's/@Suppress("DEPRECATION")//g' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
sed -i 's/val addresses = geocoder.getFromLocation(finalLat, finalLon, 1)/@Suppress("DEPRECATION")\n                                val addresses = geocoder.getFromLocation(finalLat, finalLon, 1)/g' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
