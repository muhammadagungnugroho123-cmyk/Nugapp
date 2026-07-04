package com.example.ui.screens.alarms

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.outlined.NotificationsActive
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import java.util.Locale
import org.json.JSONObject
import java.net.URL
import kotlinx.coroutines.withContext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ClockApplication
import com.example.data.alarm.AlarmService
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

data class AlarmWeatherData(val temp: Double, val descKey: String, val isDay: Boolean, val locationName: String)

class AlarmActivity : ComponentActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val rawLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val parts = rawLabel.split("|||")
        val label = parts[0]
        val preFetchedGreeting = if (parts.size > 1) parts[1] else ""
        val alarmId = intent.getIntExtra("ALARM_ID", -1)

        setContent {
            val appContainer = (application as ClockApplication).container
            val themeColorIndex by appContainer.settingsRepository.themeColorIndex.collectAsState(initial = 0)
            val appLanguage by appContainer.settingsRepository.appLanguage.collectAsState(initial = "en")
            val snoozeDuration by appContainer.settingsRepository.snoozeDuration.collectAsState(initial = 10)
            val temperatureUnit by appContainer.settingsRepository.temperatureUnit.collectAsState(initial = 0)

            MyApplicationTheme(themeColorIndex = themeColorIndex) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0B131E)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val bgPulse by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(6000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bgPulse"
                    )
                    
                    var aiGreeting by remember { mutableStateOf(preFetchedGreeting) }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    var weatherState by remember { mutableStateOf<AlarmWeatherData?>(null) }
                    
                    val hasLocationPermission = remember {
                        androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                        androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    }
                    
                    LaunchedEffect(hasLocationPermission) {
                        if (hasLocationPermission) {
                            try {
                                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        val lat = location.latitude
                                        val lon = location.longitude
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val geocoder = Geocoder(context, Locale.getDefault())
                                                @Suppress("DEPRECATION")
                                                val addresses = geocoder.getFromLocation(lat, lon, 1)
                                                val locName = if (!addresses.isNullOrEmpty()) {
                                                    addresses[0].locality ?: "Current Location"
                                                } else {
                                                    "Current Location"
                                                }
                                                
                                                val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,is_day,weather_code&timezone=auto"
                                                val response = URL(url).readText()
                                                val json = JSONObject(response)
                                                val current = json.getJSONObject("current")
                                                val temp = current.getDouble("temperature_2m")
                                                val isDay = current.getInt("is_day") == 1
                                                val code = current.getInt("weather_code")
                                                val descKey = when (code) {
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
                                                withContext(Dispatchers.Main) {
                                                    weatherState = AlarmWeatherData(temp, descKey, isDay, locName)
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF030D22))) {
                        // Aurora and Stars Background
                        val starOffset by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(20000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "starOffset"
                        )
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            // Stars
                            val rand = java.util.Random(42)
                            for (i in 0..50) {
                                val x = rand.nextFloat() * size.width
                                val y = (rand.nextFloat() * size.height + (starOffset * size.height)) % size.height
                                val radius = rand.nextFloat() * 2f + 1f
                                val alpha = rand.nextFloat() * 0.5f + 0.1f
                                drawCircle(
                                    color = Color.White.copy(alpha = alpha),
                                    radius = radius,
                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                )
                            }
                            
                            // Aurora 1 (Cyan)
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.6f + (bgPulse * 0.2f)), Color.Transparent),
                                    center = androidx.compose.ui.geometry.Offset(size.width * (0.2f + bgPulse * 0.1f), size.height * 0.3f),
                                    radius = size.maxDimension * 0.7f
                                ),
                                topLeft = androidx.compose.ui.geometry.Offset(-size.width * 0.5f, -size.height * 0.2f),
                                size = androidx.compose.ui.geometry.Size(size.width * 2f, size.height)
                            )
                            // Aurora 2 (Purple)
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF9D4EDD).copy(alpha = 0.5f + ((1f - bgPulse) * 0.2f)), Color.Transparent),
                                    center = androidx.compose.ui.geometry.Offset(size.width * (0.8f - bgPulse * 0.1f), size.height * 0.1f),
                                    radius = size.maxDimension * 0.8f
                                ),
                                topLeft = androidx.compose.ui.geometry.Offset(0f, -size.height * 0.3f),
                                size = androidx.compose.ui.geometry.Size(size.width * 1.5f, size.height * 1.2f)
                            )
                            // Aurora 3 (Teal)
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF00BFA5).copy(alpha = 0.4f + (bgPulse * 0.3f)), Color.Transparent),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * (0.8f - bgPulse * 0.1f)),
                                    radius = size.maxDimension * 0.6f
                                ),
                                topLeft = androidx.compose.ui.geometry.Offset(-size.width * 0.2f, size.height * 0.4f),
                                size = androidx.compose.ui.geometry.Size(size.width * 1.4f, size.height)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 48.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = com.example.ui.util.Translations.getString(appLanguage, "alarms"),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val dateFormat = java.text.SimpleDateFormat("hh:mm", java.util.Locale.getDefault())
                                val amPmFormat = java.text.SimpleDateFormat("a", java.util.Locale.getDefault())
                                val now = java.util.Date()
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = dateFormat.format(now),
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontWeight = FontWeight.Light,
                                            fontSize = 90.sp,
                                            letterSpacing = 2.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = amPmFormat.format(now).uppercase(),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp
                                        ),
                                        color = Color(0xFF00E5FF),
                                        modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = if (label.isEmpty()) "Alarm" else label,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val dateStr = java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.getDefault()).format(now)
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                
                                if (aiGreeting.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = aiGreeting,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                        color = Color(0xFFFFD700),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                                
                                weatherState?.let { weather ->
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.08f), shape = CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        val weatherIcon = when (weather.descKey) {
                                            "weather_clear" -> if (weather.isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
                                            "weather_mostly_clear" -> if (weather.isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
                                            else -> Icons.Default.Cloud
                                        }
                                        Icon(
                                            imageVector = weatherIcon,
                                            contentDescription = null,
                                            tint = if (weather.descKey.contains("clear")) Color(0xFFFFD54F) else Color(0xFF90CAF9),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val localizedDesc = com.example.ui.util.Translations.getString(appLanguage, weather.descKey) ?: ""
                                        val tempStr = if (temperatureUnit == 0) "${weather.temp.toInt()}°C" else "${(weather.temp * 9/5 + 32).toInt()}°F"
                                        Text(
                                            text = "${weather.locationName}: $localizedDesc • $tempStr",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Visual instruction for "Shake to Snooze"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.1f), shape = CircleShape)
                                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f), CircleShape)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (appLanguage == "id") "Goyang HP untuk Snooze" else "Shake device to Snooze",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                            ) {
                                // SNOOZE
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.85f)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                        .clickable { snoozeAndFinish(alarmId, label) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Snooze, contentDescription = "Snooze", tint = Color.White, modifier = Modifier.size(36.dp))
                                        Spacer(Modifier.height(12.dp))
                                        Text(com.example.ui.util.Translations.getString(appLanguage, "snooze").uppercase(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text("${snoozeDuration} MNT", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                    }
                                }
                                // DISMISS
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.85f)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF1E3A5F).copy(alpha=0.6f), Color(0xFF00E5FF).copy(alpha=0.2f))))
                                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                        .clickable { stopAndFinish(alarmId) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AlarmOff, contentDescription = "Dismiss", tint = Color(0xFF00E5FF), modifier = Modifier.size(36.dp))
                                        Spacer(Modifier.height(12.dp))
                                        Text(com.example.ui.util.Translations.getString(appLanguage, "dismiss").uppercase(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun stopService() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
        }
        startService(stopIntent)
    }
    
    private fun stopAndFinish(alarmId: Int) {
        stopService()
        if (alarmId != -1) {
            val appContainer = (application as ClockApplication).container
            CoroutineScope(Dispatchers.IO).launch {
                val alarm = appContainer.alarmRepository.getAlarmById(alarmId)
                if (alarm != null) {
                    if (alarm.label.startsWith("Snooze:")) {
                        appContainer.alarmRepository.deleteAlarmById(alarm.id)
                    } else if (alarm.daysOfWeek == 0) {
                        appContainer.alarmRepository.updateAlarm(alarm.copy(isEnabled = false))
                    }
                }
            }
        }
        finish()
    }
    
    private fun snoozeAndFinish(alarmId: Int, label: String) {
        stopService()
        val appContainer = (application as ClockApplication).container
        CoroutineScope(Dispatchers.IO).launch {
            var soundUri = ""
            var isVibrate = true
            
            val snoozeMinutes = appContainer.settingsRepository.snoozeDuration.first()
            
            if (alarmId != -1) {
                val alarm = appContainer.alarmRepository.getAlarmById(alarmId)
                if (alarm != null) {
                    soundUri = alarm.soundUri
                    isVibrate = alarm.isVibrate
                    if (alarm.label.startsWith("Snooze:")) {
                        appContainer.alarmRepository.deleteAlarmById(alarm.id)
                    } else if (alarm.daysOfWeek == 0) {
                        appContainer.alarmRepository.updateAlarm(alarm.copy(isEnabled = false))
                    }
                }
            }
            
            // Create snooze alarm +snoozeMinutes min
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.MINUTE, snoozeMinutes)
            val snoozeAlarm = com.example.data.local.AlarmEntity(
                id = 0,
                hour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                minute = cal.get(java.util.Calendar.MINUTE),
                label = if (label.startsWith("Snooze:")) label else "Snooze: $label",
                isEnabled = true,
                daysOfWeek = 0,
                dateMillis = cal.timeInMillis,
                soundUri = soundUri,
                isVibrate = isVibrate
            )
            appContainer.alarmRepository.insertAlarm(snoozeAlarm)
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH
            
            val gForce = kotlin.math.sqrt(gX * gX + gY * gY + gZ * gZ)
            if (gForce > 2.2f) { // Sensitive enough for a standard hand shake
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > 2000) {
                    lastShakeTime = now
                    val rawLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
                    val parts = rawLabel.split("|||")
                    val label = parts[0]
                    val alarmId = intent.getIntExtra("ALARM_ID", -1)
                    snoozeAndFinish(alarmId, label)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
