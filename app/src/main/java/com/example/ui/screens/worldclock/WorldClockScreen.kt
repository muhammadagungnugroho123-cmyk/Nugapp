package com.example.ui.screens.worldclock

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(
    appLanguage: String = "en",
    
    onNavigateToSettings: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appContainer = (context.applicationContext as com.example.ClockApplication).container
    val viewModel: WorldClockViewModel = viewModel(factory = WorldClockViewModel.provideFactory(appContainer.settingsRepository))
    val cities by viewModel.cities.collectAsState()
    val settingsViewModel: com.example.ui.screens.settings.SettingsViewModel = viewModel(factory = com.example.ui.screens.settings.SettingsViewModel.provideFactory(appContainer.settingsRepository))
    val showWorldClockGlobe by settingsViewModel.showWorldClockGlobe.collectAsState()
    val globeStyle by settingsViewModel.worldClockGlobeStyle.collectAsState()
    val currentTimeState = viewModel.currentTime.collectAsState()
    var showAddCitySheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val infiniteTransition = rememberInfiniteTransition()
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val atmospherePulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    if (showAddCitySheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAddCitySheet = false
                searchQuery = "" 
            },
            containerColor = Color(0xFF152A4A)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    com.example.ui.util.Translations.getString(appLanguage, "add_city"),
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search city...", color = Color.LightGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color(0xFF00E5FF)
                    )
                )
                LazyColumn {
                    val filteredCities = viewModel.availableCities.filter {
                        it.name.contains(searchQuery, ignoreCase = true) || it.country.contains(searchQuery, ignoreCase = true)
                    }
                    items(filteredCities) { city ->
                        ListItem(
                            headlineContent = { Text(city.name, color = Color.White) },
                            supportingContent = { Text("${city.flag} ${city.country}", color = Color.LightGray) },
                            modifier = Modifier.clickable {
                                viewModel.addCity(city)
                                showAddCitySheet = false
                                searchQuery = ""
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B131E), 
        topBar = {
            TopAppBar(
                title = { Text(com.example.ui.util.Translations.getString(appLanguage, "world_clock"), color = Color(0xFF00E5FF)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCitySheet = true },
                containerColor = Color(0xFF00E5FF)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add City", tint = Color(0xFF0B131E))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B131E),
                            Color(0xFF152A4A),
                            Color(0xFF0B131E)
                        )
                    )
                )
        ) {
            if (showWorldClockGlobe) {
            // Globe 3D Animation
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            var manualRotation by remember { mutableStateOf(0f) }
            var manualTilt by remember { mutableStateOf(0f) }
            val mapPath = remember { androidx.compose.ui.graphics.Path() }
            val paintName = remember { 
                android.graphics.Paint().apply { 
                    isAntiAlias = true
                    setShadowLayer(8f, 0f, 4f, android.graphics.Color.BLACK)
                    typeface = android.graphics.Typeface.DEFAULT
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                }
            }
            val paintTime = remember { 
                android.graphics.Paint().apply { 
                    isAntiAlias = true
                    setShadowLayer(8f, 0f, 4f, android.graphics.Color.BLACK)
                    typeface = android.graphics.Typeface.DEFAULT
                    color = android.graphics.Color.WHITE
                    textSize = 40f
                }
            }
            val stars = remember { 
                List(60) { 
                    Triple(
                        Math.random().toFloat(), 
                        Math.random().toFloat(),
                        (Math.random() * 2f + 0.5f).toFloat() 
                    )
                } 
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    stars.forEach { (rx, ry, starRadius) ->
                        drawCircle(
                            color = Color.White.copy(alpha = 0.4f),
                            radius = starRadius,
                            center = Offset(rx * size.width, ry * size.height)
                        )
                    }
                }
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 3f)
                                manualRotation += pan.x * 0.5f
                                manualTilt = (manualTilt - pan.y * 0.5f).coerceIn(-60f, 60f)
                                val maxOffset = (size.width * (scale - 1)) / 2
                                offset = Offset(
                                    x = (offset.x + pan.x * scale).coerceIn(-maxOffset, maxOffset),
                                    y = (offset.y + pan.y * scale).coerceIn(-maxOffset, maxOffset)
                                )
                            }
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y + bobbingOffset
                        }
                ) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Outer atmosphere glow
                    val isRealistic = globeStyle == 1
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isRealistic) Color(0xFF1E283A).copy(alpha = atmospherePulse * 0.5f) else Color(0xFF81D4FA).copy(alpha = atmospherePulse), 
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * 1.25f
                        ),
                        radius = radius * 1.25f,
                        center = center
                    )
                    
                    // Ocean base (realistic blue or default blue)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isRealistic) listOf(Color(0xFF141F31), Color(0xFF0F1520), Color(0xFF070B12), Color(0xFF000000)) 
                                     else listOf(Color(0xFF4FC3F7), Color(0xFF0288D1), Color(0xFF01579B), Color(0xFF000000)),
                            center = Offset(center.x - radius * 0.4f, center.y - radius * 0.4f),
                            radius = radius * 1.6f
                        ),
                        radius = radius,
                        center = center
                    )

                    val tiltRad = Math.toRadians(manualTilt.toDouble())
                    
                    if (isRealistic) {
                        // Draw realistic map from WorldMapData (using cached path to avoid allocations)
                        mapPath.reset()
                        for (poly in WorldMapData.polygons) {
                            var isFirst = true
                            for (i in poly.indices step 4) { // Decimated coordinate lookup (step by 4 instead of 2 reduces trigonometric workload by 50%)
                                val lon = poly[i]
                                val lat = poly[i+1]
                                val latRad = Math.toRadians(lat.toDouble())
                                val lonRad = Math.toRadians((lon + rotationAngle + manualRotation).toDouble())
                                
                                val x1 = kotlin.math.cos(latRad) * kotlin.math.sin(lonRad)
                                val y1 = -kotlin.math.sin(latRad)
                                val z1 = kotlin.math.cos(latRad) * kotlin.math.cos(lonRad)
                                
                                val x = x1
                                val y = y1 * kotlin.math.cos(tiltRad) - z1 * kotlin.math.sin(tiltRad)
                                val z = y1 * kotlin.math.sin(tiltRad) + z1 * kotlin.math.cos(tiltRad)
                                
                                if (z > -0.05) {
                                    val px = center.x + (x * radius).toFloat()
                                    val py = center.y + (y * radius).toFloat()
                                    if (isFirst) {
                                        mapPath.moveTo(px, py)
                                        isFirst = false
                                    } else {
                                        mapPath.lineTo(px, py)
                                    }
                                } else {
                                    isFirst = true
                                }
                            }
                        }
                        drawPath(mapPath, color = Color(0xFF6B655C), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    } else {
                        // Default Blue: Rotating Longitude Lines
                        for (lon in 0..360 step 30) {
                            val path = androidx.compose.ui.graphics.Path()
                            var isFirst = true
                            for (lat in -90..90 step 10) { // Step 10 instead of 5 for speed
                                val latRad = Math.toRadians(lat.toDouble())
                                val lonRad = Math.toRadians((lon + rotationAngle + manualRotation).toDouble())
                                val x1 = kotlin.math.cos(latRad) * kotlin.math.sin(lonRad)
                                val y1 = -kotlin.math.sin(latRad)
                                val z1 = kotlin.math.cos(latRad) * kotlin.math.cos(lonRad)
                                val x = x1
                                val y = y1 * kotlin.math.cos(tiltRad) - z1 * kotlin.math.sin(tiltRad)
                                val z = y1 * kotlin.math.sin(tiltRad) + z1 * kotlin.math.cos(tiltRad)
                                
                                if (z > -0.1) {
                                    val px = center.x + (x * radius).toFloat()
                                    val py = center.y + (y * radius).toFloat()
                                    if (isFirst) {
                                        path.moveTo(px, py)
                                        isFirst = false
                                    } else {
                                        path.lineTo(px, py)
                                    }
                                } else {
                                    isFirst = true
                                }
                            }
                            drawPath(path, color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 1.dp.toPx()))
                        }
     
                        // Rotating Latitude Lines
                        for (lat in -80..80 step 20) {
                            val path = androidx.compose.ui.graphics.Path()
                            var isFirst = true
                            for (lon in 0..360 step 10) { // Step 10 instead of 5 for speed
                                val latRad = Math.toRadians(lat.toDouble())
                                val lonRad = Math.toRadians((lon + rotationAngle + manualRotation).toDouble())
                                val x1 = kotlin.math.cos(latRad) * kotlin.math.sin(lonRad)
                                val y1 = -kotlin.math.sin(latRad)
                                val z1 = kotlin.math.cos(latRad) * kotlin.math.cos(lonRad)
                                val x = x1
                                val y = y1 * kotlin.math.cos(tiltRad) - z1 * kotlin.math.sin(tiltRad)
                                val z = y1 * kotlin.math.sin(tiltRad) + z1 * kotlin.math.cos(tiltRad)
                                
                                if (z > -0.1) {
                                    val px = center.x + (x * radius).toFloat()
                                    val py = center.y + (y * radius).toFloat()
                                    if (isFirst) {
                                        path.moveTo(px, py)
                                        isFirst = false
                                    } else {
                                        path.lineTo(px, py)
                                    }
                                } else {
                                    isFirst = true
                                }
                            }
                            drawPath(path, color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 1.dp.toPx()))
                        }
                    }
                    
                    // Inner shadow for 3D sphere effect
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color(0xAA000000)),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                    
                    // Draw dots for added cities using cached Paint objects
                    
                    cities.forEach { city ->
                        val latRad = Math.toRadians(city.lat.toDouble())
                        val lonRad = Math.toRadians((city.lon.toDouble() + rotationAngle + manualRotation))
                        
                        val x1 = kotlin.math.cos(latRad) * kotlin.math.sin(lonRad)
                        val y1 = -kotlin.math.sin(latRad)
                        val z1 = kotlin.math.cos(latRad) * kotlin.math.cos(lonRad)
                        
                        val x = x1
                        val y = y1 * kotlin.math.cos(tiltRad) - z1 * kotlin.math.sin(tiltRad)
                        val z = y1 * kotlin.math.sin(tiltRad) + z1 * kotlin.math.cos(tiltRad)
                        
                        // If z > 0, it's on the front of the globe
                        if (z > 0) {
                            val dotX = center.x + (x * radius).toFloat()
                            val dotY = center.y + (y * radius).toFloat()
                            
                            drawCircle(
                                color = if (isRealistic) Color(0xFF90CAF9) else Color(0xFF00E5FF),
                                radius = 4.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                            
                            sdfTime.timeZone = java.util.TimeZone.getTimeZone(city.timeZoneId)
                            val timeString = sdfTime.format(currentTimeState.value)
                            
                            // Adjust text position slightly
                            val textX = dotX - (paintName.measureText(city.name) / 2f)
                            drawContext.canvas.nativeCanvas.drawText(city.name, textX, dotY - 15f, paintName)
                            val timeX = dotX - (paintTime.measureText(timeString) / 2f)
                            drawContext.canvas.nativeCanvas.drawText(timeString, timeX, dotY + 25f, paintTime)
                        }
                    }
                }
            }
            }
            
            // City List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cities) { city ->
                    WorldCityCard(city = city, currentTimeProvider = { currentTimeState.value })
                }
            }
        }
    }
}

@Composable
fun WorldCityCard(city: WorldCity, currentTimeProvider: () -> Long) {
    val currentTime = currentTimeProvider()
    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    sdfTime.timeZone = TimeZone.getTimeZone(city.timeZoneId)
    val timeString = sdfTime.format(currentTime)
    
    val tz = TimeZone.getTimeZone(city.timeZoneId)
    val localTz = TimeZone.getDefault()
    val offsetDiff = tz.getOffset(currentTime) - localTz.getOffset(currentTime)
    val diffHours = offsetDiff / (1000 * 60 * 60)
    val diffString = when {
        diffHours > 0 -> "+$diffHours jam"
        diffHours < 0 -> "$diffHours jam"
        else -> "Waktu lokal"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${city.flag} ${city.country} • $diffString",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = timeString,
                color = Color(0xFF00E5FF),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Light)
            )
        }
    }
}
