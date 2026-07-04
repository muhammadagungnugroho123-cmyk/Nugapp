package com.example.ui.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.AiService
import com.example.network.Content
import com.example.network.Part
import kotlinx.coroutines.launch
import org.json.JSONObject

import androidx.compose.ui.platform.LocalContext
import com.example.ui.util.NetworkMonitor

data class ChatMessage(val text: String, val isUser: Boolean, val isError: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    appLanguage: String = "en",
    alarmsViewModel: com.example.ui.screens.alarms.AlarmsViewModel? = null,
    timersViewModel: com.example.ui.screens.timers.TimersViewModel? = null,
    stopwatchViewModel: com.example.ui.screens.stopwatch.StopwatchViewModel? = null
) {
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)
    
    var messages by remember { mutableStateOf(listOf(ChatMessage(com.example.ui.util.Translations.getString(appLanguage, "ai_greeting"), false))) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var messageTimestamps by remember { mutableStateOf(listOf<Long>()) }
    val coroutineScope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, 
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHRONOS AI",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Light,
                                letterSpacing = 2.sp,
                                color = Color(0xFF00E5FF)
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent, 
                    
                )
            )
        },
        
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isConnected) {
                Surface(
                    color = Color(0xFFB00020),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = com.example.ui.util.Translations.getString(appLanguage, "offline_mode"),
                        color = Color.White,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            CircularProgressIndicator(
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            Surface(
                color = Color(0xFF131D26),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp, max = 120.dp),
                        placeholder = { Text(com.example.ui.util.Translations.getString(appLanguage, "ask_schedule"), color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val userMessage = ChatMessage(inputText, true)
                                messages = messages + userMessage
                                val currentInput = inputText
                                inputText = ""
                                isLoading = true
                                
                                coroutineScope.launch {
                                    val lowerInput = currentInput.lowercase()
                                    val offlineReply = when {
                                        lowerInput.contains("pasang alarm") -> {
                                            alarmsViewModel?.addAlarm(com.example.data.local.AlarmEntity(hour = 7, minute = 0, isEnabled = true, label = "AI Alarm"))
                                            "Alarm disetel untuk pukul 07:00."
                                        }
                                        lowerInput == "mulai" || lowerInput == "start" -> {
                                            if (stopwatchViewModel?.isRunning?.value == false) stopwatchViewModel.toggleStopwatch()
                                            if (timersViewModel?.isRunning?.value == false) timersViewModel.toggleTimer()
                                            "Stopwatch/Timer dimulai."
                                        }
                                        lowerInput == "stop" || lowerInput == "berhenti" -> {
                                            if (stopwatchViewModel?.isRunning?.value == true) stopwatchViewModel.toggleStopwatch()
                                            if (timersViewModel?.isRunning?.value == true) timersViewModel.toggleTimer()
                                            "Stopwatch/Timer dihentikan."
                                        }
                                        else -> null
                                    }
                                    
                                    if (offlineReply != null) {
                                        messages = messages + ChatMessage(offlineReply, false)
                                        isLoading = false
                                        return@launch
                                    }

                                    val now = System.currentTimeMillis()
                                    val recentMsgs = messageTimestamps.filter { now - it < 15 * 60 * 1000 }
                                    if (recentMsgs.size >= 50) {
                                        messages = messages + ChatMessage("Sistem Pendinginan: Anda telah mencapai batas 50 pesan. Harap tunggu beberapa saat agar AI dapat memulihkan energinya.", false)
                                        isLoading = false
                                        return@launch
                                    }
                                    messageTimestamps = recentMsgs + now

                                    if (!isConnected) {
                                        messages = messages + ChatMessage(com.example.ui.util.Translations.getString(appLanguage, "no_internet_ai"), false, isError = true)
                                        isLoading = false
                                        return@launch
                                    }
                                    try {
                                        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                                        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY") {
                                            messages = messages + ChatMessage(com.example.ui.util.Translations.getString(appLanguage, "api_key_not_set"), false, isError = true)
                                            return@launch
                                        }
                                        val historyContents = messages.takeLast(4).filter { !it.isError }.map { msg ->
                                            Content(
                                                role = if (msg.isUser) "user" else "model",
                                                parts = listOf(Part(text = msg.text))
                                            )
                                        }
                                        var weatherInfo = "User has no saved cities."
                                        try {
                                            val prefs = context.getSharedPreferences("weather_prefs", android.content.Context.MODE_PRIVATE)
                                            val savedCities = prefs.getString("saved_cities", "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                                            if (savedCities.isNotEmpty()) {
                                                val city = savedCities.first()
                                                val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${city.replace(" ", "+")}&count=1"
                                                val geoResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { java.net.URL(geoUrl).readText() }
                                                val geoJson = org.json.JSONObject(geoResponse)
                                                if (geoJson.has("results")) {
                                                    val results = geoJson.getJSONArray("results")
                                                    val lat = results.getJSONObject(0).getDouble("latitude")
                                                    val lon = results.getJSONObject(0).getDouble("longitude")
                                                    val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto"
                                                    val wRes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { java.net.URL(weatherUrl).readText() }
                                                    val wJson = org.json.JSONObject(wRes).getJSONObject("current")
                                                    weatherInfo = "Current weather in $city: ${wJson.getDouble("temperature_2m")}°C, Code: ${wJson.getInt("weather_code")}, Wind: ${wJson.getDouble("wind_speed_10m")}km/h."
                                                }
                                            }
                                        } catch(e: Exception) { }

                                        val currentDateTime = java.text.SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                                        val replyText = AiService.generateContentWithHistory(
                                            contents = historyContents,
                                            systemInstructionStr = """
                                                You are CHRONOS AI, a smart assistant for a clock app.\nApp features: World Clock (with live weather, temperature, morning/afternoon/night data), Alarms, Stopwatch, Timers, Settings.
                                                Current date and time is $currentDateTime. $weatherInfo
                                                You can converse normally, but you MUST ALWAYS return your final response as a JSON object.
                                                Format:
                                                {
                                                  "reply": "Your conversational reply here in user's language.",
                                                  "action": "NONE" | "SET_ALARM" | "START_TIMER" | "STOP_TIMER" | "RESET_TIMER" | "START_STOPWATCH" | "STOP_STOPWATCH" | "RESET_STOPWATCH" | "LAP_STOPWATCH",
                                                  "action_params": {
                                                    "hour": 7, // For SET_ALARM (0-23)
                                                    "minute": 30, // For SET_ALARM (0-59)
                                                    "seconds": 300 // For START_TIMER (duration in seconds)
                                                  }
                                                }
                                                Examples:
                                                - User: "Set alarm for 5 AM" -> "action": "SET_ALARM", "action_params": {"hour": 5, "minute": 0}
                                                - User: "Timer for 10 minutes" -> "action": "START_TIMER", "action_params": {"seconds": 600}
                                                - User: "Stop the timer" -> "action": "STOP_TIMER"
                                                Be helpful, and always provide valid JSON ONLY without any markdown blocks.
                                            """.trimIndent(),
                                            temperature = 0.7f,
                                            responseMimeType = "application/json"
                                        )
                                        
                                        try {
                                            val cleanedReply = replyText.replace("```json", "").replace("```", "").trim()
                                            val jsonObj = JSONObject(cleanedReply)
                                            val reply = jsonObj.optString("reply", "I'm sorry, I couldn't process that.")
                                            val action = jsonObj.optString("action", "NONE")
                                            
                                            messages = messages + ChatMessage(reply, false)
                                            
                                            when (action) {
                                                "SET_ALARM" -> {
                                                    val params = jsonObj.optJSONObject("action_params")
                                                    if (params != null && params.has("hour") && params.has("minute")) {
                                                        val h = params.getInt("hour")
                                                        val m = params.getInt("minute")
                                                        alarmsViewModel?.addAlarm(com.example.data.local.AlarmEntity(hour = h, minute = m, isEnabled = true, label = "AI Alarm"))
                                                    }
                                                }
                                                "START_TIMER" -> {
                                                    val params = jsonObj.optJSONObject("action_params")
                                                    if (params != null && params.has("seconds")) {
                                                        val s = params.getInt("seconds")
                                                        timersViewModel?.setTotalTime(s)
                                                        if (timersViewModel?.isRunning?.value == false) timersViewModel.toggleTimer()
                                                    }
                                                }
                                                "STOP_TIMER" -> {
                                                    if (timersViewModel?.isRunning?.value == true) timersViewModel.toggleTimer()
                                                }
                                                "RESET_TIMER" -> {
                                                    timersViewModel?.stopTimer()
                                                }
                                                "START_STOPWATCH" -> {
                                                    if (stopwatchViewModel?.isRunning?.value == false) stopwatchViewModel.toggleStopwatch()
                                                }
                                                "STOP_STOPWATCH" -> {
                                                    if (stopwatchViewModel?.isRunning?.value == true) stopwatchViewModel.toggleStopwatch()
                                                }
                                                "RESET_STOPWATCH" -> {
                                                    stopwatchViewModel?.resetStopwatch()
                                                }
                                                "LAP_STOPWATCH" -> {
                                                    if (stopwatchViewModel?.isRunning?.value == true) stopwatchViewModel.lapOrReset()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            messages = messages + ChatMessage("${com.example.ui.util.Translations.getString(appLanguage, "ai_reply")}$replyText", false)
                                        }
                                    } catch (e: Exception) {
                                        val errorMsg = e.message ?: "Unknown error"
                                        if (errorMsg.contains("429")) {
                                            messages = messages + ChatMessage("Batas penggunaan API Gemini tercapai. Sistem saat ini sedang dalam mode pendinginan agresif. Harap tunggu beberapa saat untuk mencoba lagi.", false, isError = true)
                                        } else {
                                            messages = messages + ChatMessage("${com.example.ui.util.Translations.getString(appLanguage, "error_occurred")}$errorMsg${com.example.ui.util.Translations.getString(appLanguage, "check_api_key")}", false, isError = true)
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                        enabled = !isLoading && inputText.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF00E5FF))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (message.isUser) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E3A5F).copy(alpha = 0.5f),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 16.dp
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (message.isUser) Color(0xFF00E5FF).copy(alpha = 0.5f) else Color.Transparent)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                modifier = Modifier.padding(12.dp),
                fontSize = 15.sp
            )
        }
    }
}
