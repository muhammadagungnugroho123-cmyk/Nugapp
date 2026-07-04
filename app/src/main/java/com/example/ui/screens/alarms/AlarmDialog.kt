package com.example.ui.screens.alarms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AlarmEntity
import com.example.network.AiService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.util.NetworkMonitor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDialog(
    appLanguage: String = "en",
    initialAlarm: AlarmEntity?,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)
    
    var hour by remember { mutableStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableStateOf(initialAlarm?.minute ?: 30) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "Bangun Pagi") }
    var daysOfWeek by remember { mutableStateOf(initialAlarm?.daysOfWeek ?: 0) }
    var aiFeatureEnabled by remember { mutableStateOf(true) }
    var dateMillis by remember { mutableStateOf(initialAlarm?.dateMillis ?: 0L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var aiSuggestion by remember { mutableStateOf("Memuat saran AI...") }
    var soundUriStr by remember { mutableStateOf(initialAlarm?.soundUri ?: "") }
    var soundName by remember { mutableStateOf("Default Ringtone") }

    val ringtonePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: android.net.Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI, android.net.Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                soundUriStr = uri.toString()
                val ringtone = android.media.RingtoneManager.getRingtone(context, uri)
                soundName = ringtone?.getTitle(context) ?: "Custom Ringtone"
            }
        }
    }
    
    LaunchedEffect(soundUriStr) {
        if (soundUriStr.isNotEmpty() && soundUriStr != "Nebula Rhapsody" && soundUriStr != "Over the Horizon by SUGA of BTS") {
            try {
                val uri = android.net.Uri.parse(soundUriStr)
                val ringtone = android.media.RingtoneManager.getRingtone(context, uri)
                soundName = ringtone?.getTitle(context) ?: "Custom Ringtone"
            } catch(e:Exception) {}
        }
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(aiFeatureEnabled, isConnected, hour, minute) {
        if (aiFeatureEnabled) {
            if (!isConnected) {
                aiSuggestion = com.example.ui.util.Translations.getString(appLanguage, "ai_offline_desc") ?: "Fitur AI tidak tersedia saat offline."
                return@LaunchedEffect
            }
            aiSuggestion = "Memuat saran AI..."
            delay(5000) // Debounce for 5 seconds to prevent excessive API calls
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY") {
                    aiSuggestion = "SARAN AI: API Key belum diatur."
                    return@LaunchedEffect
                }
                val currentDateTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                val response = AiService.generateContent(
                    prompt = "Saat ini jam $currentDateTime. Alarm disetel untuk $hour:$minute. Berikan satu kalimat singkat memotivasi atau saran tidur/bangun terkait alarm ini (Maksimal 15 kata).",
                    temperature = 0.5f
                )
                aiSuggestion = "SARAN AI: " + response
            } catch (e: Exception) {
                if (e.message?.contains("429") == true) {
                    aiSuggestion = "Sistem Pendinginan: API Gemini telah mencapai batas maksimal."
                } else {
                    aiSuggestion = "SARAN AI: Gagal memuat saran. Periksa koneksi internet."
                }
            }
        } else {
            aiSuggestion = "Fitur AI dinonaktifkan."
        }
    }

    if (showDatePicker) {
        val todayUtcMillis = remember {
            val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
        val initialDate = if (dateMillis > 0) dateMillis else todayUtcMillis
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Title
                Text(
                    text = com.example.ui.util.Translations.getString(appLanguage, "add_new_alarm"),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp,
                        color = Color(0xFF00E5FF)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Sci-Fi Time Selector Visualization
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val primaryColor = Color(0xFF00E5FF)
                    val trackColor = Color(0xFF1E3A5F).copy(alpha = 0.5f)

                    val particles = remember {
                        List(40) {
                            val a = Math.random() * Math.PI * 2
                            val r = Math.random()
                            val radius = (Math.random() * 3).toFloat()
                            val alpha = (Math.random() * 0.5 + 0.1).toFloat()
                            Triple(a, r, Pair(radius, alpha))
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background particles
                        for (particle in particles) {
                            val r = (size.width / 1.5) * particle.second
                            val x = size.width / 2 + r * cos(particle.first)
                            val y = size.height / 2 + r * sin(particle.first)
                            drawCircle(
                                color = Color.White.copy(alpha = particle.third.second),
                                radius = particle.third.first,
                                center = Offset(x.toFloat(), y.toFloat())
                            )
                        }

                        val strokeWidth = 12.dp.toPx()
                        
                        // Rings
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        
                        // Fake progress
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = 140f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )

                        // Complex inner patterns (Sci-Fi rings) with bounds checking
                        for (i in 0 until 3) {
                            val w = size.width - 64.dp.toPx() - i * 20
                            val h = size.height - 128.dp.toPx() - i * 40
                            if (w > 0 && h > 0) {
                                drawOval(
                                    color = primaryColor.copy(alpha = 0.3f),
                                    style = Stroke(2.dp.toPx()),
                                    topLeft = Offset(32.dp.toPx() + i * 10, 64.dp.toPx() + i * 20),
                                    size = androidx.compose.ui.geometry.Size(w, h)
                                )
                            }
                        }
                    }

                    // Time display
                    Box(modifier = Modifier.padding(top = 16.dp)) {
                        MyApplicationTheme(themeColorIndex = 0) { // Wrap in theme if needed or just use directly
                            WheelTimePicker(
                                initialHour = hour,
                                initialMinute = minute,
                                onTimeChanged = { h, m -> 
                                    hour = h
                                    minute = m
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glassy Dashboard Panel for settings
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF131D26).copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Dynamic Date
                        val calendar = Calendar.getInstance().apply { timeInMillis = if (dateMillis > 0) dateMillis else System.currentTimeMillis() }
                        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                                    contentDescription = "Date",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = dateFormat.format(calendar.time).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Alarm Name
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text(com.example.ui.util.Translations.getString(appLanguage, "alarm_name"), color = Color(0xFF00E5FF)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF00E5FF).copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF00E5FF)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Days
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(com.example.ui.util.Translations.getString(appLanguage, "repeat_days"), color = Color(0xFFFFD700), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp), fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0B131E).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val days = com.example.ui.util.Translations.getList(appLanguage, "days")
                                days.forEachIndexed { index, day ->
                                    val isSelected = (daysOfWeek and (1 shl index)) != 0
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFFFFD700) else Color.Transparent)
                                            .clickable {
                                                daysOfWeek = daysOfWeek xor (1 shl index)
                                                // Reset dateMillis if repeating
                                                if (daysOfWeek > 0) dateMillis = 0L
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day,
                                            color = if (isSelected) Color(0xFF0B131E) else Color.Gray,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // AI Smart Alarm feature
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B131E).copy(alpha = 0.5f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(com.example.ui.util.Translations.getString(appLanguage, "smart_alarm"), color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Switch(
                                            checked = aiFeatureEnabled,
                                            onCheckedChange = { aiFeatureEnabled = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF00BFA5),
                                                uncheckedThumbColor = Color.Gray,
                                                uncheckedTrackColor = Color.DarkGray
                                            ),
                                            modifier = Modifier.scale(0.8f)
                                        )
                                        Text(if (aiFeatureEnabled) com.example.ui.util.Translations.getString(appLanguage, "active") else com.example.ui.util.Translations.getString(appLanguage, "inactive"), color = if (aiFeatureEnabled) Color(0xFFFFD700) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }

                            // Ringtone Selection
                            Card(
                                modifier = Modifier.weight(1f).clickable {
                                    try {
                                        val intent = android.content.Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER)
                                        intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_ALARM)
                                        intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, com.example.ui.util.Translations.getString(appLanguage, "choose_ringtone"))
                                        if (soundUriStr.isNotEmpty()) {
                                            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, android.net.Uri.parse(soundUriStr))
                                        }
                                        ringtonePickerLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        // Fallback if no ringtone picker is available
                                    }
                                },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B131E).copy(alpha = 0.5f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(com.example.ui.util.Translations.getString(appLanguage, "alarm_sound"), color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(soundName, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // AI Suggestion Text
                        androidx.compose.animation.AnimatedVisibility(visible = aiFeatureEnabled) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B131E).copy(alpha = 0.3f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    androidx.compose.material3.Icon(
                                        imageVector = if (isConnected) androidx.compose.material.icons.Icons.Default.AutoAwesome else androidx.compose.material.icons.Icons.Default.CloudOff,
                                        contentDescription = "AI",
                                        tint = if (isConnected) Color(0xFFFFD700) else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = aiSuggestion,
                                        color = if (isConnected) Color.White else Color.Gray,
                                        fontSize = 13.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        fontStyle = if (!isConnected) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(com.example.ui.util.Translations.getString(appLanguage, "cancel").uppercase(), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        var motivation = ""
                                        if (aiFeatureEnabled && isConnected) {
                                            try {
                                                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                                                if (apiKey.isNotEmpty() && apiKey != "YOUR_GEMINI_API_KEY") {
                                                    val response = AiService.generateContent(
                                                        prompt = "Berikan ucapan motivasi pagi super singkat 5-10 kata, sangat inspiratif dan membakar semangat. Bahasa Indonesia.",
                                                        temperature = 0.7f
                                                    )
                                                    motivation = response.replace("\"", "")
                                                }
                                            } catch (e: Exception) {}
                                        }
                                        val finalLabel = if (motivation.isNotEmpty()) "$label|||$motivation" else label
                                        onSave(
                                            AlarmEntity(
                                                hour = hour,
                                                minute = minute,
                                                label = finalLabel,
                                                isVibrate = true,
                                                isEnabled = true,
                                                daysOfWeek = daysOfWeek,
                                                dateMillis = dateMillis,
                                                soundUri = soundUriStr,
                                                isSnooze = true
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color(0xFF0B131E)),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp)
                            ) {
                                Text(com.example.ui.util.Translations.getString(appLanguage, "save_alarm").uppercase(), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

