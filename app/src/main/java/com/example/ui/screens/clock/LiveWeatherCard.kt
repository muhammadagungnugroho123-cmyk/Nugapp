package com.example.ui.screens.clock

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Refresh
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import java.text.SimpleDateFormat

data class HourlyForecast(val time: String, val temp: Double, val code: Int, val isDay: Boolean, val precipProb: Int = 0)

data class WeatherData(
    val temp: Double = 0.0,
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val description: String = "Loading...",
    val isDay: Boolean = true,
    val locationName: String = "Locating...",
    val timeOfDayStr: String = "",
    val code: Int = 0,
    val utcOffsetSeconds: Int = 0,
    val hourly: List<HourlyForecast> = emptyList()
)

fun getWeatherDescriptionKey(code: Int): String {
    return when (code) {
        0 -> "weather_clear"
        1, 2 -> "weather_mostly_clear"
        3 -> "weather_cloudy"
        45, 48 -> "weather_fog"
        51, 53, 55 -> "weather_drizzle"
        61, 63, 65 -> "weather_rain"
        80, 81, 82 -> "weather_heavy_rain"
        71, 73, 75, 77, 85, 86 -> "weather_snow"
        95 -> "weather_thunderstorm"
        96, 99 -> "weather_thunderstorm_hail"
        else -> "weather_overcast"
    }
}

fun getTimeOfDayKey(hour: Int): String {
    return when (hour) {
        in 0..4 -> "time_early_morning"
        in 5..10 -> "time_morning"
        in 11..14 -> "time_noon"
        in 15..17 -> "time_afternoon"
        else -> "time_evening"
    }
}

@Composable
fun LiveWeatherCard() {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val appContainer = (context.applicationContext as com.example.ClockApplication).container
    val settingsViewModel: com.example.ui.screens.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.ui.screens.settings.SettingsViewModel.provideFactory(appContainer.settingsRepository))
    val temperatureUnit by settingsViewModel.temperatureUnit.collectAsState()
    val appLanguage by settingsViewModel.appLanguage.collectAsState()
    val weatherRefreshInterval by settingsViewModel.weatherRefreshInterval.collectAsState()
    val formatTemp = { temp: Double -> if (temperatureUnit == 0) "${temp.toInt()}°C" else "${(temp * 9/5 + 32).toInt()}°F" }
    val formatTempNumOnly = { temp: Double -> if (temperatureUnit == 0) "${temp.toInt()}" else "${(temp * 9/5 + 32).toInt()}" }
    
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val rainDropY1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val rainDropY2 by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val lightningAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0
                0f at 2500
                1f at 2600
                0f at 2700
                1f at 2800
                0f at 2900
            },
            repeatMode = RepeatMode.Restart
        )
    )

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var lastFetchTime by remember { mutableStateOf(0L) }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(weatherRefreshInterval) {
        while (true) {
            kotlinx.coroutines.delay(maxOf(5, weatherRefreshInterval).toLong() * 60 * 1000L)
            if (hasLocationPermission) {
                refreshTrigger++
            }
        }
    }

    LaunchedEffect(hasLocationPermission, refreshTrigger) {
        if (hasLocationPermission) {
            val currentTime = System.currentTimeMillis()
            val intervalMs = maxOf(5, weatherRefreshInterval).toLong() * 60 * 1000L
            if (weatherData != null && (currentTime - lastFetchTime) < intervalMs && refreshTrigger > 1) {
                return@LaunchedEffect
            }
            
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
                        val timeOfDayStr = getTimeOfDayKey(hourOpenMeteo)
                        val temp = current.getDouble("temperature_2m")
                        val humidity = current.getInt("relative_humidity_2m")
                        val windSpeed = current.getDouble("wind_speed_10m")
                        val isDay = current.getInt("is_day") == 1
                        val code = current.getInt("weather_code")
                        val desc = getWeatherDescriptionKey(code)

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
                            lastFetchTime = System.currentTimeMillis()
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
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        performFetch(lastLoc.latitude, lastLoc.longitude)
                    } else {
                        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                            .addOnSuccessListener { location ->
                                val lat = location?.latitude ?: -6.2088
                                val lon = location?.longitude ?: 106.8456
                                performFetch(lat, lon)
                            }
                            .addOnFailureListener {
                                performFetch(-6.2088, 106.8456)
                            }
                    }
                }.addOnFailureListener {
                    performFetch(-6.2088, 106.8456)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (weatherData?.isDay == false) Color(0xFF1A237E).copy(alpha = 0.3f) else Color(0xFF0277BD).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = if (weatherData?.isDay == false) Icons.Default.NightsStay else Icons.Default.Cloud
                    val tint = if (weatherData?.isDay == false) Color(0xFFB388FF) else Color(0xFF00E5FF)
                    Icon(
                        icon, 
                        contentDescription = "Weather", 
                        tint = tint.copy(alpha = pulse),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "LIVE WEATHER", 
                        color = tint, 
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                }
                
                if (hasLocationPermission && weatherData != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Red.copy(alpha = pulse)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(weatherData!!.locationName.uppercase(), color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { refreshTrigger++ },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (!hasLocationPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to allow location for live weather", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (weatherData != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherAnimation(
                        weatherData = weatherData!!, 
                        rotation = rotation, 
                        cloudDrift = cloudDrift, 
                        rainDropY1 = rainDropY1, 
                        rainDropY2 = rainDropY2, 
                        lightningAlpha = lightningAlpha,
                        modifier = Modifier.size(100.dp).graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                formatTempNumOnly(weatherData!!.temp),
                                color = Color.White,
                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(if (temperatureUnit == 0) "°C" else "°F", color = Color(0xFF00E5FF), style = MaterialTheme.typography.titleLarge)
                        }
                        val localizedDesc = com.example.ui.util.Translations.getString(appLanguage, weatherData!!.description)
                        val localizedTimeOfDay = com.example.ui.util.Translations.getString(appLanguage, weatherData!!.timeOfDayStr)
                        Text(
                            "$localizedDesc • $localizedTimeOfDay",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${weatherData!!.humidity}%", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("${weatherData!!.windSpeed} km/h", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                if (weatherData!!.code in listOf(95, 96, 99, 61, 63, 65, 71, 73, 75, 77, 85, 86)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Color(0xFFFFB300).copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (weatherData!!.code in listOf(95, 96, 99)) "Peringatan Dini: Potensi Badai Petir" else if (weatherData!!.code in listOf(71, 73, 75, 77, 85, 86)) "Peringatan Dini: Potensi Salju/Suhu Ekstrim" else "Peringatan Dini: Hujan Deras", color = Color(0xFFFFB300), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
                if (weatherData!!.hourly.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Waktu Mendatang",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weatherData!!.hourly.forEach { forecast ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = forecast.time,
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                                    WeatherAnimation(
                                        weatherData = WeatherData(code = forecast.code, isDay = forecast.isDay),
                                        rotation = rotation,
                                        cloudDrift = 0f,
                                        rainDropY1 = rainDropY1,
                                        rainDropY2 = rainDropY2,
                                        lightningAlpha = lightningAlpha,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formatTemp(forecast.temp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                if (forecast.precipProb > 0) {
                                    Text(
                                        text = "${forecast.precipProb}%",
                                        color = Color(0xFF00E5FF),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00E5FF))
                }
            }
        }
    }
}

@Composable
fun WeatherAnimation(
    weatherData: WeatherData,
    rotation: Float,
    cloudDrift: Float,
    rainDropY1: Float,
    rainDropY2: Float,
    lightningAlpha: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        
        when (weatherData.code) {
            0, 1 -> { // Clear / Mostly Clear
                if (weatherData.isDay) {
                    // Sun
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = size.width / 3,
                        center = center
                    )
                    // Sun rays
                    for (i in 0 until 8) {
                        val angle = Math.toRadians((i * 45 + rotation).toDouble())
                        val r1 = size.width / 2.5f
                        val r2 = size.width / 1.8f
                        val start = Offset(
                            center.x + (r1 * cos(angle)).toFloat(),
                            center.y + (r1 * sin(angle)).toFloat()
                        )
                        val end = Offset(
                            center.x + (r2 * cos(angle)).toFloat(),
                            center.y + (r2 * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = Color(0xFFFFD700).copy(alpha = 0.7f),
                            start = start,
                            end = end,
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                } else {
                    // Moon
                    drawCircle(
                        color = Color.LightGray,
                        radius = size.width / 3,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF0B131E), // Background color to create crescent
                        radius = size.width / 3.5f,
                        center = Offset(center.x + 12.dp.toPx(), center.y - 12.dp.toPx())
                    )
                    // Stars
                    drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(center.x - 40.dp.toPx(), center.y - 30.dp.toPx()))
                    drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(center.x + 40.dp.toPx(), center.y - 20.dp.toPx()))
                    drawCircle(Color.White, radius = 1.dp.toPx(), center = Offset(center.x - 20.dp.toPx(), center.y + 40.dp.toPx()))
                }
            }
            2, 3 -> { // Partly Cloudy
                if (weatherData.isDay) {
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = size.width / 3.5f,
                        center = Offset(center.x - 10.dp.toPx(), center.y - 10.dp.toPx())
                    )
                } else {
                    drawCircle(
                        color = Color.LightGray,
                        radius = size.width / 3.5f,
                        center = Offset(center.x - 10.dp.toPx(), center.y - 10.dp.toPx())
                    )
                }
                // Cloud
                drawCircle(Color.White.copy(alpha = 0.9f), radius = 24.dp.toPx(), center = Offset(center.x + 10.dp.toPx() + cloudDrift, center.y + 10.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.9f), radius = 16.dp.toPx(), center = Offset(center.x - 10.dp.toPx() + cloudDrift, center.y + 18.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.9f), radius = 20.dp.toPx(), center = Offset(center.x + 30.dp.toPx() + cloudDrift, center.y + 14.dp.toPx()))
            }
            45, 48 -> { // Fog
                for (i in 0..4) {
                    val yOff = i * 12.dp.toPx() - 20.dp.toPx()
                    val xDrift = if (i % 2 == 0) cloudDrift else -cloudDrift
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.6f),
                        start = Offset(center.x - 30.dp.toPx() + xDrift, center.y + yOff),
                        end = Offset(center.x + 30.dp.toPx() + xDrift, center.y + yOff),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            51, 53, 55, 61, 63, 65 -> { // Rain / Drizzle
                // Cloud
                drawCircle(Color.Gray.copy(alpha = 0.9f), radius = 24.dp.toPx(), center = Offset(center.x, center.y - 10.dp.toPx()))
                drawCircle(Color.Gray.copy(alpha = 0.9f), radius = 16.dp.toPx(), center = Offset(center.x - 24.dp.toPx(), center.y - 2.dp.toPx()))
                drawCircle(Color.Gray.copy(alpha = 0.9f), radius = 20.dp.toPx(), center = Offset(center.x + 24.dp.toPx(), center.y - 6.dp.toPx()))
                
                // Rain
                val rainColor = Color(0xFF00E5FF).copy(alpha = 0.7f)
                drawLine(rainColor, Offset(center.x - 15.dp.toPx(), center.y + rainDropY1), Offset(center.x - 20.dp.toPx(), center.y + rainDropY1 + 10.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
                drawLine(rainColor, Offset(center.x, center.y + rainDropY2), Offset(center.x - 5.dp.toPx(), center.y + rainDropY2 + 10.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
                drawLine(rainColor, Offset(center.x + 15.dp.toPx(), center.y + rainDropY1 - 10.dp.toPx()), Offset(center.x + 10.dp.toPx(), center.y + rainDropY1), 3.dp.toPx(), StrokeCap.Round)
            }
            95, 96, 99 -> { // Thunderstorm
                // Dark Cloud
                drawCircle(Color.DarkGray.copy(alpha = 0.9f), radius = 24.dp.toPx(), center = Offset(center.x, center.y - 10.dp.toPx()))
                drawCircle(Color.DarkGray.copy(alpha = 0.9f), radius = 16.dp.toPx(), center = Offset(center.x - 24.dp.toPx(), center.y - 2.dp.toPx()))
                drawCircle(Color.DarkGray.copy(alpha = 0.9f), radius = 20.dp.toPx(), center = Offset(center.x + 24.dp.toPx(), center.y - 6.dp.toPx()))
                
                // Lightning
                val lightningPath = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(center.x - 10.dp.toPx(), center.y + 20.dp.toPx())
                    lineTo(center.x + 5.dp.toPx(), center.y + 20.dp.toPx())
                    lineTo(center.x - 5.dp.toPx(), center.y + 45.dp.toPx())
                }
                drawPath(
                    path = lightningPath,
                    color = Color(0xFFFFD700).copy(alpha = lightningAlpha),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                )
            }
            71, 73, 75, 77, 85, 86 -> { // Snow
                drawCircle(Color.LightGray.copy(alpha = 0.9f), radius = 24.dp.toPx(), center = Offset(center.x, center.y - 10.dp.toPx()))
                drawCircle(Color.LightGray.copy(alpha = 0.9f), radius = 16.dp.toPx(), center = Offset(center.x - 24.dp.toPx(), center.y - 2.dp.toPx()))
                drawCircle(Color.LightGray.copy(alpha = 0.9f), radius = 20.dp.toPx(), center = Offset(center.x + 24.dp.toPx(), center.y - 6.dp.toPx()))
                drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(center.x - 10.dp.toPx(), center.y + rainDropY1))
                drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(center.x + 10.dp.toPx(), center.y + rainDropY2))
            }
            else -> { // Default Cloudy
                drawCircle(Color.LightGray.copy(alpha = 0.9f), radius = 24.dp.toPx(), center = Offset(center.x, center.y))
                drawCircle(Color.LightGray.copy(alpha = 0.9f), radius = 16.dp.toPx(), center = Offset(center.x - 24.dp.toPx(), center.y + 8.dp.toPx()))
                drawCircle(Color.LightGray.copy(alpha = 0.9f), radius = 20.dp.toPx(), center = Offset(center.x + 24.dp.toPx(), center.y + 4.dp.toPx()))
            }
        }
    }
}
