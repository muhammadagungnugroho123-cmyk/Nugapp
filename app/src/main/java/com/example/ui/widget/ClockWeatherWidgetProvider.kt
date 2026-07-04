package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.ClockApplication
import com.example.R
import com.example.data.local.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar

class ClockWeatherWidgetProvider : AppWidgetProvider() {

    private val client = OkHttpClient()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val appContainer = (context.applicationContext as? ClockApplication)?.container ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Get next alarm
            val alarms = try {
                appContainer.alarmRepository.getAllAlarms().first()
            } catch (e: Exception) {
                emptyList<AlarmEntity>()
            }
            val nextAlarmStr = getNextAlarmString(alarms)

            // 2. Get first saved city or default
            val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
            val savedCitiesStr = prefs.getString("saved_cities", "Jakarta") ?: "Jakarta"
            val firstCity = savedCitiesStr.split(",").firstOrNull { it.isNotBlank() } ?: "Jakarta"

            // 3. Fetch weather for that city
            val (tempStr, descStr) = fetchWeatherForCity(firstCity)

            // 4. Update all widgets
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_clock_weather)
                views.setTextViewText(R.id.widget_next_alarm, nextAlarmStr)
                views.setTextViewText(R.id.widget_weather_city, firstCity)
                views.setTextViewText(R.id.widget_weather_temp, tempStr)
                views.setTextViewText(R.id.widget_weather_desc, descStr)

                // Add PendingIntent to open the app on click
                val pendingIntent = PendingIntent.getActivity(
                    context, 0,
                    Intent(context, com.example.MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_background, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun getNextAlarmString(alarms: List<AlarmEntity>): String {
        val enabledAlarms = alarms.filter { it.isEnabled }
        if (enabledAlarms.isEmpty()) return "No Active Alarms"

        val now = Calendar.getInstance()
        val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        var nearestAlarm: AlarmEntity? = null
        var minDiff = Int.MAX_VALUE

        for (alarm in enabledAlarms) {
            val alarmMin = alarm.hour * 60 + alarm.minute
            var diff = alarmMin - nowMin
            if (diff < 0) {
                diff += 24 * 60 // Next day
            }
            if (diff < minDiff) {
                minDiff = diff
                nearestAlarm = alarm
            }
        }

        nearestAlarm?.let {
            return String.format("Alarm: %02d:%02d", it.hour, it.minute)
        }

        return "No Active Alarms"
    }

    private fun fetchWeatherForCity(city: String): Pair<String, String> {
        return try {
            val geocodeUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$city&count=1&language=en&format=json"
            val geoRequest = Request.Builder().url(geocodeUrl).build()
            client.newCall(geoRequest).execute().use { response ->
                if (!response.isSuccessful) return Pair("--°C", "Offline")
                val responseBody = response.body?.string() ?: return Pair("--°C", "Offline")
                val geoJson = JSONObject(responseBody)
                val results = geoJson.optJSONArray("results") ?: return Pair("--°C", "Unknown City")
                if (results.length() == 0) return Pair("--°C", "Unknown City")
                val firstResult = results.getJSONObject(0)
                val lat = firstResult.getDouble("latitude")
                val lon = firstResult.getDouble("longitude")

                val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
                val weatherRequest = Request.Builder().url(weatherUrl).build()
                client.newCall(weatherRequest).execute().use { wResponse ->
                    if (!wResponse.isSuccessful) return Pair("--°C", "Offline")
                    val wBody = wResponse.body?.string() ?: return Pair("--°C", "Offline")
                    val wJson = JSONObject(wBody)
                    val current = wJson.getJSONObject("current")
                    val temp = current.getDouble("temperature_2m")
                    val code = current.getInt("weather_code")
                    val desc = when (code) {
                        0 -> "Clear"
                        1, 2, 3 -> "Partly Cloudy"
                        45, 48 -> "Foggy"
                        51, 53, 55 -> "Drizzle"
                        61, 63, 65 -> "Rainy"
                        71, 73, 75 -> "Snowy"
                        80, 81, 82 -> "Rain Showers"
                        95, 96, 99 -> "Thunderstorm"
                        else -> "Cloudy"
                    }
                    return Pair("${temp.toInt()}°C", desc)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("--°C", "Offline")
        }
    }
}
