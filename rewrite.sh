cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
package com.example.ui.screens.weather

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.clock.HourlyForecast
import com.example.ui.screens.clock.WeatherAnimation
import com.example.ui.screens.clock.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
    
    var savedCities by remember {
        mutableStateOf(prefs.getString("saved_cities", "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList())
    }
    
    val weatherDataList = remember { mutableStateListOf<WeatherData>() }
    var isLoadingList by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition()
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

    suspend fun fetchWeatherData(city: String): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${city.replace(" ", "+")}&count=1&language=en&format=json"
                val geoResponse = URL(geoUrl).readText()
                val geoJson = JSONObject(geoResponse)
                
                if (!geoJson.has("results")) return@withContext null
                
                val result = geoJson.getJSONArray("results").getJSONObject(0)
                val lat = result.getDouble("latitude")
                val lon = result.getDouble("longitude")
                val name = result.getString("name")
                val country = if (result.has("country")) result.getString("country") else ""
                val locName = if (country.isNotEmpty()) "$name, $country" else name

                val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m&hourly=temperature_2m,weather_code,precipitation_probability,is_day&timezone=auto&forecast_days=2"
                val response = URL(weatherUrl).readText()
                val json = JSONObject(response)
                val current = json.getJSONObject("current")
                val temp = current.getDouble("temperature_2m")
                val humidity = current.getInt("relative_humidity_2m")
                val windSpeed = current.getDouble("wind_speed_10m")
                val isDay = current.getInt("is_day") == 1
                val code = current.getInt("weather_code")
                
                val desc = when(code) {
                    0 -> "Cerah"
                    1, 2, 3 -> "Cerah Berawan"
                    45, 48 -> "Berkabut"
                    51, 53, 55 -> "Gerimis"
                    61, 63, 65 -> "Hujan"
                    71, 73, 75, 77, 85, 86 -> "Salju"
                    95, 96, 99 -> "Badai Petir"
                    else -> "Berawan"
                }

                val currentTimeStr = current.getString("time")
                val hour = currentTimeStr.substring(11, 13).toInt()
                val timeOfDayStr = when (hour) {
                    in 0..4 -> "Dini Hari"
                    in 5..10 -> "Pagi"
                    in 11..14 -> "Siang"
                    in 15..17 -> "Sore"
                    else -> "Malam"
                }

                val hourlyList = mutableListOf<HourlyForecast>()
                val hourlyJson = json.getJSONObject("hourly")
                val timeArr = hourlyJson.getJSONArray("time")
                val tempArr = hourlyJson.getJSONArray("temperature_2m")
                val codeArr = hourlyJson.getJSONArray("weather_code")
                val isDayArr = hourlyJson.getJSONArray("is_day")
                val precipArr = hourlyJson.getJSONArray("precipitation_probability")
                
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

                WeatherData(temp, humidity, windSpeed, desc, isDay, locName, timeOfDayStr, code, hourlyList)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun loadWeatherForSavedCities() {
        coroutineScope.launch {
            isLoadingList = true
            val deferreds = savedCities.map { city ->
                async { fetchWeatherData(city) }
            }
            val results = deferreds.awaitAll().filterNotNull()
            weatherDataList.clear()
            weatherDataList.addAll(results)
            isLoadingList = false
        }
    }

    LaunchedEffect(Unit) {
        loadWeatherForSavedCities()
    }

    fun addCity(city: String) {
        if (city.isBlank()) return
        if (savedCities.size >= 10) {
            errorMessage = "Maksimal 10 kota"
            return
        }
        isSearching = true
        errorMessage = null
        coroutineScope.launch {
            val data = fetchWeatherData(city)
            isSearching = false
            if (data != null) {
                if (savedCities.contains(data.locationName)) {
                    errorMessage = "Kota sudah ditambahkan"
                    return@launch
                }
                val newCities = savedCities + data.locationName
                savedCities = newCities
                prefs.edit().putString("saved_cities", newCities.joinToString(",")).apply()
                weatherDataList.add(data)
                searchQuery = ""
            } else {
                errorMessage = "Kota tidak ditemukan"
            }
        }
    }
    
    fun removeCity(cityName: String) {
        val newCities = savedCities.filter { it != cityName }
        savedCities = newCities
        prefs.edit().putString("saved_cities", newCities.joinToString(",")).apply()
        weatherDataList.removeAll { it.locationName == cityName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuaca", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B131E))
            )
        },
        containerColor = Color(0xFF0B131E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Tambah Kota...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { addCity(searchQuery) }),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { addCity(searchQuery) },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                    }
                }
            }
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoadingList) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00E5FF))
                }
            } else if (weatherDataList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada kota yang ditambahkan.", color = Color.Gray)
                        Text("Cari dan tambah hingga 10 kota.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(weatherDataList) { data ->
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
                                                if (!data.isDay) Color(0xFF1A237E).copy(alpha = 0.3f) else Color(0xFF0277BD).copy(alpha = 0.3f),
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
                                    Text(data.locationName.uppercase(), color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                    IconButton(
                                        onClick = { removeCity(data.locationName) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.White.copy(alpha = 0.5f))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text(
                                                "${data.temp.toInt()}",
                                                color = Color.White,
                                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, fontSize = 48.sp)
                                            )
                                            Text("°C", color = Color(0xFF00E5FF), style = MaterialTheme.typography.titleLarge)
                                        }
                                        Text(
                                            data.description,
                                            color = Color.LightGray,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    
                                    WeatherAnimation(
                                        weatherData = data,
                                        rotation = rotation,
                                        cloudDrift = cloudDrift,
                                        rainDropY1 = rainDropY1,
                                        rainDropY2 = rainDropY2,
                                        lightningAlpha = lightningAlpha,
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${data.humidity}%", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) // Wind icon placeholder
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${data.windSpeed} km/h", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                
                                if (data.hourly.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        data.hourly.forEach { forecast ->
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
                                                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                                    WeatherAnimation(
                                                        weatherData = WeatherData(code = forecast.code, isDay = forecast.isDay),
                                                        rotation = rotation,
                                                        cloudDrift = 0f,
                                                        rainDropY1 = rainDropY1,
                                                        rainDropY2 = rainDropY2,
                                                        lightningAlpha = lightningAlpha,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "${forecast.temp.toInt()}°",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
INNER_EOF
