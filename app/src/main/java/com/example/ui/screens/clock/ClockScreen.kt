package com.example.ui.screens.clock

import androidx.compose.animation.core.*
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    appLanguage: String = "en",
    clockStyleIndex: Int = 0,
    hasActiveAlarm: Boolean = false,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToWeather: () -> Unit = {}
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            while (true) {
                currentTime = Calendar.getInstance()
                delay(50)
            }
        }
    }

    val locale = when (appLanguage) {
        "id", "in" -> Locale.forLanguageTag("id")
        "zh" -> Locale.CHINESE
        else -> Locale.ENGLISH
    }
    val timeFormat = remember(appLanguage) { SimpleDateFormat("HH:mm:ss", locale) }
    val amPmFormat = remember(appLanguage) { SimpleDateFormat("a", locale) }
    val dateFormat = remember(appLanguage) { SimpleDateFormat("EEE, dd MMM", locale) }

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, 
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "CHRONOS",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 4.sp,
                            color = Color(0xFF00E5FF)
                        )
                    ) 
                },
                navigationIcon = {
                    if (hasActiveAlarm) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alarm Active",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            )
                        }
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    var showWeatherDialog by remember { mutableStateOf(false) }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Menu, contentDescription = "Menu", tint = Color.Gray)
                    }
                        
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                            
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cuaca", color = Color.White) },
                            onClick = { 
                                showMenu = false
                                onNavigateToWeather()
                            },
                            leadingIcon = {
                                Icon(androidx.compose.material.icons.Icons.Default.Cloud, contentDescription = null, tint = Color.White)
                            }
                        )
                    }

                    if (showWeatherDialog) {
                        AlertDialog(
                            onDismissRequest = { showWeatherDialog = false },
                            containerColor = Color(0xFF0B131E),
                            titleContentColor = Color.White,
                            textContentColor = Color.White,
                            title = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF00E5FF))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cuaca")
                                }
                            },
                            text = { Text("Coming Soon", style = MaterialTheme.typography.bodyLarge) },
                            confirmButton = {
                                TextButton(onClick = { showWeatherDialog = false }) {
                                    Text("OK", color = Color(0xFF00E5FF))
                                }
                            }
                        )
                    }
                       
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent, 
                )
            )
        },
           
    ) { padding ->
        Box(
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
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                if (clockStyleIndex == 0) {
                // Sci-Fi Clock Face
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val primaryColor = Color(0xFF00E5FF) // Neon Cyan
                    val trackColor = Color(0xFF1E3A5F).copy(alpha = 0.5f)
                       
                    val second = currentTime.get(Calendar.SECOND)
                    val ms = currentTime.get(Calendar.MILLISECOND)
                    val animatedProgress = (second * 1000 + ms) / 60000f

                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.98f,
                        targetValue = 1.02f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    val rotationAnim by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(60000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    val stars = remember {
                        List(50) {
                            val r = Math.random()
                            val a = Math.random() * Math.PI * 2
                            val alpha = (Math.random() * 0.5 + 0.1).toFloat()
                            val size = (Math.random() * 3).toFloat()
                            floatArrayOf(r.toFloat(), a.toFloat(), alpha, size)
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)) {
                        // Draw some space particles/stars around the clock
                        stars.forEach { star ->
                            val r = (size.width / 1.5f) * star[0]
                            val a = star[1] + Math.toRadians(rotationAnim.toDouble()).toFloat()
                            val x = size.width / 2 + r * cos(a)
                            val y = size.height / 2 + r * sin(a)
                            drawCircle(
                                color = Color.White.copy(alpha = star[2]),
                                radius = star[3],
                                center = Offset(x, y)
                            )
                        }

                        val strokeWidth = 12.dp.toPx()
                        val glowWidth = 24.dp.toPx()

                        // Outer ring background
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )

                        // Inner decorative ring
                        drawArc(
                            color = primaryColor.copy(alpha = 0.2f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(2.dp.toPx()),
                            topLeft = Offset(16.dp.toPx(), 16.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(size.width - 32.dp.toPx(), size.height - 32.dp.toPx())
                        )

                        // Progress ring glow
                        drawArc(
                            color = primaryColor.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(glowWidth, cap = StrokeCap.Round)
                        )

                        // Main progress ring
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )

                        // Indicator dot
                        val headAngle = (animatedProgress * 360f) - 90f
                        val headAngleRad = Math.toRadians(headAngle.toDouble())
                        val radius = size.width / 2
                        val headX = (radius + radius * cos(headAngleRad)).toFloat()
                        val headY = (radius + radius * sin(headAngleRad)).toFloat()

                        drawCircle(
                            color = Color.White,
                            radius = 8.dp.toPx(),
                            center = Offset(headX, headY)
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 8.dp.toPx(),
                            center = Offset(headX, headY),
                            style = Stroke(4.dp.toPx())
                        )
                    }

                    // Digital Time Display inside the ring
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeFormat.format(currentTime.time),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp
                            ),
                            color = primaryColor
                        )
                        Text(
                            text = amPmFormat.format(currentTime.time).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = primaryColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                } else if (clockStyleIndex == 1) {
                    Box(
                        modifier = Modifier
                            .size(320.dp)
                            .padding(16.dp)
                            .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormat.format(currentTime.time),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = amPmFormat.format(currentTime.time).uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = 4.sp
                                ),
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(320.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val primaryColor = Color(0xFF00E5FF)
                        val hour = currentTime.get(Calendar.HOUR)
                        val minute = currentTime.get(Calendar.MINUTE)
                        val second = currentTime.get(Calendar.SECOND)
                        val ms = currentTime.get(Calendar.MILLISECOND)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color.White.copy(alpha = 0.1f), radius = size.width / 2)
                            drawCircle(color = primaryColor, radius = size.width / 2, style = Stroke(4.dp.toPx()))
                            for (i in 0 until 12) {
                                val angle = Math.toRadians((i * 30 - 90).toDouble())
                                val startX = (size.width / 2 + (size.width / 2 - 20.dp.toPx()) * cos(angle)).toFloat()
                                val startY = (size.height / 2 + (size.width / 2 - 20.dp.toPx()) * sin(angle)).toFloat()
                                val endX = (size.width / 2 + (size.width / 2) * cos(angle)).toFloat()
                                val endY = (size.height / 2 + (size.width / 2) * sin(angle)).toFloat()
                                drawLine(color = Color.White.copy(alpha = 0.5f), start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 2.dp.toPx())
                            }
                            val hourAngle = Math.toRadians((hour * 30 + minute * 0.5 - 90).toDouble())
                            val hX = (size.width / 2 + (size.width / 3.5f) * cos(hourAngle)).toFloat()
                            val hY = (size.height / 2 + (size.width / 3.5f) * sin(hourAngle)).toFloat()
                            drawLine(color = Color.White, start = Offset(size.width / 2, size.height / 2), end = Offset(hX, hY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
                            val minAngle = Math.toRadians((minute * 6 + second * 0.1 - 90).toDouble())
                            val mX = (size.width / 2 + (size.width / 2.5f) * cos(minAngle)).toFloat()
                            val mY = (size.height / 2 + (size.width / 2.5f) * sin(minAngle)).toFloat()
                            drawLine(color = Color.White.copy(alpha = 0.8f), start = Offset(size.width / 2, size.height / 2), end = Offset(mX, mY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                            val secAngle = Math.toRadians((second * 6 + ms * 0.006 - 90).toDouble())
                            val sX = (size.width / 2 + (size.width / 2.2f) * cos(secAngle)).toFloat()
                            val sY = (size.height / 2 + (size.width / 2.2f) * sin(secAngle)).toFloat()
                            drawLine(color = primaryColor, start = Offset(size.width / 2, size.height / 2), end = Offset(sX, sY), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                            drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // Glassy Dashboard Panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dateFormat.format(currentTime.time).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Light
                            ),
                            color = Color.White
                        )
                           
                        Spacer(modifier = Modifier.height(24.dp))
                           
                        // Live Weather Card
                        LiveWeatherCard()
                           
                        Spacer(modifier = Modifier.height(16.dp))
                           
                        // Calendar Sync Events
                        // Calendar Sync Events
                        CalendarEventsCard()
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Weather Notification
                        AnimatedWeatherNotificationItem()
                    }
                }


            }
        }
    }
}
