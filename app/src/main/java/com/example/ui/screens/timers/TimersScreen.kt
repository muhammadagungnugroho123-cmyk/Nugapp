package com.example.ui.screens.timers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.network.AiService

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun CustomTimeDialog(
    appLanguage: String = "en",
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("0") }
    var seconds by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(com.example.ui.util.Translations.getString(appLanguage, "custom_timer")) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hours,
                    onValueChange = { if (it.length <= 2) hours = it.filter { char -> char.isDigit() } },
                    label = { Text("h") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).padding(4.dp),
                    singleLine = true
                )
                Text(":", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { if (it.length <= 2) minutes = it.filter { char -> char.isDigit() } },
                    label = { Text("m") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).padding(4.dp),
                    singleLine = true
                )
                Text(":", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = seconds,
                    onValueChange = { if (it.length <= 2) seconds = it.filter { char -> char.isDigit() } },
                    label = { Text("s") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).padding(4.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val h = hours.toIntOrNull() ?: 0
                    val m = minutes.toIntOrNull() ?: 0
                    val s = seconds.toIntOrNull() ?: 0
                    val total = h * 3600 + m * 60 + s
                    if (total > 0) {
                        onConfirm(total)
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(com.example.ui.util.Translations.getString(appLanguage, "set"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(com.example.ui.util.Translations.getString(appLanguage, "cancel"))
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimersScreen(
    appLanguage: String = "en",
    viewModel: TimersViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val isRunning by viewModel.isRunning.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    var showCustomTimeDialog by remember { mutableStateOf(false) }

    var aiCommand by remember { mutableStateOf("") }
    var isAiProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val networkMonitor = remember { com.example.ui.util.NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)

    if (showCustomTimeDialog) {
        CustomTimeDialog(
            appLanguage = appLanguage,
            onDismiss = { showCustomTimeDialog = false },
            onConfirm = { customTime ->
                viewModel.setTotalTime(customTime)
                showCustomTimeDialog = false
            }
        )
    }

    fun formatTime(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    val progress = if (totalTime > 0) {
        timeRemaining.toFloat() / totalTime.toFloat()
    } else 0f

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, 
        topBar = {
            TopAppBar(
                title = { Text(com.example.ui.util.Translations.getString(appLanguage, "timer")) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(300.dp),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = formatTime(timeRemaining),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable(enabled = !isRunning) {
                        showCustomTimeDialog = true
                    }.padding(16.dp)
                )
            }

            // Presets
            if (!isRunning && timeRemaining == totalTime) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = totalTime == 5 * 60,
                        onClick = { viewModel.setTotalTime(5 * 60) },
                        label = { Text("5m") }
                    )
                    FilterChip(
                        selected = totalTime == 10 * 60,
                        onClick = { viewModel.setTotalTime(10 * 60) },
                        label = { Text("10m") }
                    )
                    FilterChip(
                        selected = totalTime == 25 * 60,
                        onClick = { viewModel.setTotalTime(25 * 60) },
                        label = { Text("25m Pomodoro") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                                        if (lowerCmd == "start" || lowerCmd == "mulai" || lowerCmd == "stop" || lowerCmd == "berhenti" || lowerCmd == "pause" || lowerCmd == "jeda") {
                                            when {
                                                lowerCmd == "start" || lowerCmd == "mulai" -> if (!isRunning) viewModel.toggleTimer()
                                                lowerCmd == "stop" || lowerCmd == "berhenti" || lowerCmd == "pause" || lowerCmd == "jeda" -> if (isRunning) viewModel.toggleTimer()
                                            }
                                            isAiProcessing = false
                                            return@launch
                                        }

                                        val reply = AiService.generateContent(
                                            prompt = commandText,
                                            systemInstructionStr = "You control a timer. Respond with ONLY the number of seconds (e.g. '300' for 5 minutes), OR 'START', OR 'STOP'/'PAUSE' based on user command.",
                                            temperature = 0.0f
                                        ).trim().uppercase()
                                        
                                        when {
                                            reply.contains("START") -> if (!isRunning) viewModel.toggleTimer()
                                            reply.contains("STOP") || reply.contains("PAUSE") -> if (isRunning) viewModel.toggleTimer()
                                            reply.toIntOrNull() != null -> {
                                                val seconds = reply.toInt()
                                                if (seconds > 0) {
                                                    viewModel.setTotalTime(seconds)
                                                }
                                            }
                                            else -> {
                                                // Fallback to local parsing
                                                val lowerCmd = commandText.lowercase()
                                                when {
                                                    lowerCmd.contains("mulai") || lowerCmd.contains("start") -> if (!isRunning) viewModel.toggleTimer()
                                                    lowerCmd.contains("berhenti") || lowerCmd.contains("stop") || lowerCmd.contains("pause") -> if (isRunning) viewModel.toggleTimer()
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Fallback on error
                                        val lowerCmd = commandText.lowercase()
                                        when {
                                            lowerCmd.contains("mulai") || lowerCmd.contains("start") -> if (!isRunning) viewModel.toggleTimer()
                                            lowerCmd.contains("berhenti") || lowerCmd.contains("stop") || lowerCmd.contains("pause") -> if (isRunning) viewModel.toggleTimer()
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

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp, top = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { viewModel.stopTimer() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(32.dp)
                    )
                }

                FloatingActionButton(
                    onClick = { viewModel.toggleTimer() },
                    modifier = Modifier.size(80.dp),
                    containerColor = if (isRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}
