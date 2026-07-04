package com.example.ui.screens.stopwatch

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.network.AiService
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(
    appLanguage: String = "en",
    viewModel: StopwatchViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val isRunning by viewModel.isRunning.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val laps by viewModel.laps.collectAsState()
    
    var aiCommand by remember { mutableStateOf("") }
    var isAiProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val networkMonitor = remember { com.example.ui.util.NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)

    fun formatTime(timeMillis: Long, includeMillis: Boolean = true): String {
        val minutes = (timeMillis / 1000) / 60
        val seconds = (timeMillis / 1000) % 60
        val millis = (timeMillis % 1000) / 10
        return if (includeMillis) {
            String.format("%02d:%02d.%02d", minutes, seconds, millis)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

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
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent, 
                )
            )
        },
         // Dark Sci-Fi Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            StopwatchDisplay(
                elapsedTime = elapsedTime,
                laps = laps,
                formatTime = ::formatTime
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // START / LAP Button
                OutlinedButton(
                    onClick = {
                        if (isRunning) viewModel.lapOrReset() else viewModel.toggleStopwatch()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4CAF50) // Neon Green
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.PlayArrow,
                            contentDescription = "Start / Lap",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRunning) com.example.ui.util.Translations.getString(appLanguage, "lap").uppercase() else com.example.ui.util.Translations.getString(appLanguage, "start").uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // RESET / STOP Button
                OutlinedButton(
                    onClick = {
                        if (isRunning) viewModel.toggleStopwatch() else viewModel.lapOrReset()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE53935) // Neon Red
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE53935))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else androidx.compose.ui.graphics.vector.ImageVector.Builder("Stop", 24.dp, 24.dp, 24f, 24f).run {
                                addPath(androidx.compose.ui.graphics.vector.PathData {
                                    moveTo(6f, 6f)
                                    lineTo(18f, 6f)
                                    lineTo(18f, 18f)
                                    lineTo(6f, 18f)
                                    close()
                                }, fill = androidx.compose.ui.graphics.SolidColor(Color.Black))
                                build()
                            },
                            contentDescription = "Reset / Stop",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRunning) com.example.ui.util.Translations.getString(appLanguage, "stop").uppercase() else com.example.ui.util.Translations.getString(appLanguage, "reset").uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // AI Smart Control Input
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome, 
                        contentDescription = "AI", 
                        tint = Color(0xFF00E5FF), 
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    OutlinedTextField(
                        value = aiCommand,
                        onValueChange = { aiCommand = it },
                        placeholder = { Text(com.example.ui.util.Translations.getString(appLanguage, "ai_suggestion"), fontSize = 14.sp, color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color(0xFF00E5FF)
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (!isConnected) {
                                android.widget.Toast.makeText(context, "Koneksi internet terputus.", android.widget.Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            if (aiCommand.isNotBlank() && !isAiProcessing) {
                                isAiProcessing = true
                                val commandText = aiCommand
                                aiCommand = ""
                                coroutineScope.launch {
                                    try {
                                        val lowerCmd = commandText.lowercase()
                                        if (lowerCmd == "start" || lowerCmd == "mulai" || lowerCmd == "stop" || lowerCmd == "berhenti" || lowerCmd == "pause" || lowerCmd == "lap" || lowerCmd == "reset" || lowerCmd == "ulang") {
                                            when {
                                                lowerCmd == "start" || lowerCmd == "mulai" -> if (!isRunning) viewModel.toggleStopwatch()
                                                lowerCmd == "stop" || lowerCmd == "berhenti" || lowerCmd == "pause" -> if (isRunning) viewModel.toggleStopwatch()
                                                lowerCmd == "lap" -> if (isRunning) viewModel.lapOrReset()
                                                lowerCmd == "reset" || lowerCmd == "ulang" -> if (!isRunning) viewModel.lapOrReset()
                                            }
                                            isAiProcessing = false
                                            return@launch
                                        }

                                        val reply = AiService.generateContent(
                                            prompt = commandText,
                                            systemInstructionStr = "You control a stopwatch. Respond with ONLY ONE word: START, STOP, LAP, RESET, or UNKNOWN based on user command.",
                                            temperature = 0.0f
                                        ).trim().uppercase()
                                        
                                        when {
                                            reply.contains("START") -> if (!isRunning) viewModel.toggleStopwatch()
                                            reply.contains("STOP") || reply.contains("PAUSE") -> if (isRunning) viewModel.toggleStopwatch()
                                            reply.contains("LAP") -> if (isRunning) viewModel.lapOrReset()
                                            reply.contains("RESET") -> if (!isRunning) viewModel.lapOrReset()
                                        }
                                    } catch (e: Exception) {
                                        val lowerCmd = commandText.lowercase()
                                        when {
                                            lowerCmd.contains("start") || lowerCmd.contains("mulai") -> if (!isRunning) viewModel.toggleStopwatch()
                                            lowerCmd.contains("stop") || lowerCmd.contains("berhenti") || lowerCmd.contains("pause") -> if (isRunning) viewModel.toggleStopwatch()
                                            lowerCmd.contains("lap") -> if (isRunning) viewModel.lapOrReset()
                                            lowerCmd.contains("reset") || lowerCmd.contains("ulang") -> if (!isRunning) viewModel.lapOrReset()
                                        }
                                    } finally {
                                        isAiProcessing = false
                                    }
                                }
                            }
                        },
                        enabled = !isAiProcessing
                    ) {
                        if (isAiProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF00E5FF), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Laps Container
            if (laps.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(
                         // Darker card background
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Performance Graph",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // Visual Performance Graph
                            if (laps.size > 1) {
                                val lapDurations = laps.mapIndexed { index, time -> if (index == 0) time else time - laps[index - 1] }
                                val maxDuration = lapDurations.maxOrNull()?.toFloat() ?: 1f
                                Canvas(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(vertical = 16.dp)) {
                                    val barWidth = size.width / (lapDurations.size * 2).coerceAtLeast(1)
                                    lapDurations.forEachIndexed { index, duration ->
                                        val barHeight = (duration.toFloat() / maxDuration) * size.height
                                        drawRoundRect(
                                            color = Color(0xFF00E5FF),
                                            topLeft = Offset(
                                                x = index * barWidth * 2 + barWidth / 2,
                                                y = size.height - barHeight
                                            ),
                                            size = Size(barWidth, barHeight),
                                            cornerRadius = CornerRadius(4.dp.toPx())
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Lap List",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        itemsIndexed(laps.reversed()) { index, lapTime ->
                            val actualIndex = laps.size - index
                            val previousLapTime = if (actualIndex > 1) laps[actualIndex - 2] else 0L
                            val lapDuration = lapTime - previousLapTime
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LAP $actualIndex:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = formatTime(lapDuration),
                                    style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.sp),
                                    color = Color.White
                                )
                            }
                            if (index < laps.size - 1) {
                                HorizontalDivider(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StopwatchDisplay(
    elapsedTime: Long,
    laps: List<Long>,
    formatTime: (Long, Boolean) -> String
) {
    // Modern Animated Stopwatch Face
    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val primaryColor = Color(0xFF00E5FF) // Neon Cyan
        val trackColor = Color(0xFF1E3A5F).copy(alpha = 0.5f) // Dark Blue track
        
        // Animate progress smoothly
        val animatedProgress = (elapsedTime % 60000) / 60000f
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val glowWidth = 24.dp.toPx()

            // Background track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            
            // Glow effect
            drawArc(
                color = primaryColor.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(glowWidth, cap = StrokeCap.Round)
            )

            // Main stroke
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            
            // Draw little tick marks inside the ring occasionally
            for (i in 0 until 60) {
                if (i % 5 != 0) continue // Only draw major ticks

                val angle = (i * 6f) - 90f
                val angleRad = Math.toRadians(angle.toDouble())
                
                val radius = size.width / 2
                val innerRadius = radius - 30.dp.toPx()
                
                val startX = (radius + innerRadius * cos(angleRad)).toFloat()
                val startY = (radius + innerRadius * sin(angleRad)).toFloat()
                
                val endX = (radius + (radius - 12.dp.toPx()) * cos(angleRad)).toFloat()
                val endY = (radius + (radius - 12.dp.toPx()) * sin(angleRad)).toFloat()
                
                drawLine(
                    color = primaryColor.copy(alpha = 0.3f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Draw the little head circle (indicator dot)
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
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(elapsedTime, true),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                ),
                color = primaryColor
            )
            if (laps.isNotEmpty()) {
                val currentLapTime = elapsedTime - (laps.lastOrNull() ?: 0L)
                Text(
                    text = formatTime(currentLapTime, true),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = primaryColor.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}
