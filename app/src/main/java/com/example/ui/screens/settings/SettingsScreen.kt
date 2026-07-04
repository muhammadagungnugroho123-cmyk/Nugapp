package com.example.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.ClockApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appContainer = (context.applicationContext as ClockApplication).container
    // We should actually pass SettingsRepository in AppContainer.
    // Let's assume it's there. We'll update AppContainer next.
    
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            appContainer.settingsRepository,
            appContainer.alarmRepository
        )
    )

    val themeColorIndex by viewModel.themeColorIndex.collectAsState()
    val clockStyleIndex by viewModel.clockStyleIndex.collectAsState()
    val alarmRingtoneIndex by viewModel.alarmRingtoneIndex.collectAsState()
    val showWorldClockGlobe by viewModel.showWorldClockGlobe.collectAsState()
    val worldClockGlobeStyle by viewModel.worldClockGlobeStyle.collectAsState()
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val defaultVibrate by viewModel.defaultVibrate.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val snoozeDuration by viewModel.snoozeDuration.collectAsState()
    val alarmFadeInDuration by viewModel.alarmFadeInDuration.collectAsState()
    val weatherRefreshInterval by viewModel.weatherRefreshInterval.collectAsState()
    val autoCloudBackup by viewModel.autoCloudBackup.collectAsState()
    val lastBackupTimestamp by viewModel.lastBackupTimestamp.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isSyncing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showImportDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var importText by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var importError by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var syncMessage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, 
        topBar = {
            LargeTopAppBar(
                title = { Text(com.example.ui.util.Translations.getString(appLanguage, "settings")) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSectionCard(com.example.ui.util.Translations.getString(appLanguage, "appearance")) {
                    // Theme selection with Palette icon header
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = com.example.ui.util.Translations.getString(appLanguage, "theme_color"),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ubah warna aksen tema aplikasi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val options = listOf(
                                androidx.compose.ui.graphics.Color.Gray, // Dynamic
                                androidx.compose.ui.graphics.Color(0xFF2979FF), // Blue
                                androidx.compose.ui.graphics.Color(0xFF00E676), // Green
                                androidx.compose.ui.graphics.Color(0xFFFF9100), // Orange
                                androidx.compose.ui.graphics.Color(0xFFD500F9)  // Purple
                            )
                            val optionLabels = listOf("Auto", "Blue", "Green", "Orange", "Purple")
                            
                            options.forEachIndexed { index, color ->
                                val selected = themeColorIndex == index
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setThemeColorIndex(index) },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                if (index == 0) androidx.compose.ui.graphics.Brush.linearGradient(
                                                    listOf(androidx.compose.ui.graphics.Color(0xFF00E5FF), androidx.compose.ui.graphics.Color(0xFFD500F9))
                                                ) else androidx.compose.ui.graphics.Brush.linearGradient(
                                                    listOf(color.copy(alpha=0.7f), color)
                                                ),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .border(
                                                width = if (selected) 3.dp else 1.dp,
                                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = androidx.compose.ui.graphics.Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = optionLabels[index],
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Clock Style selection
                    SegmentedPreference(
                        title = com.example.ui.util.Translations.getString(appLanguage, "clock_style") ?: "Clock Style",
                        subtitle = "Pilih gaya jam utama",
                        icon = Icons.Default.Schedule
                    ) {
                        val options = listOf("Default", "Digital", "Analog")
                        options.forEachIndexed { index, label ->
                            val selected = clockStyleIndex == index
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setClockStyleIndex(index) }
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                    ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Language selection
                    SegmentedPreference(
                        title = com.example.ui.util.Translations.getString(appLanguage, "language"),
                        subtitle = "Pilih bahasa aplikasi",
                        icon = Icons.Default.Language
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val langOptions = listOf("system", "en", "id", "zh")
                            val langLabels = listOf("Auto", "EN", "ID", "ZH")
                            langOptions.forEachIndexed { index, langKey ->
                                SegmentedButton(
                                    selected = appLanguage == langKey,
                                    onClick = { viewModel.setAppLanguage(langKey) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = langOptions.size)
                                ) {
                                    Text(langLabels[index], style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionCard(com.example.ui.util.Translations.getString(appLanguage, "time_date")) {
                    SwitchPreference(
                        title = "Show 3D Globe",
                        subtitle = "Show the interactive 3D globe in World Clock",
                        checked = showWorldClockGlobe,
                        onCheckedChange = { viewModel.setShowWorldClockGlobe(it) },
                        icon = Icons.Default.Public
                    )
                    if (showWorldClockGlobe) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SegmentedPreference(
                            title = "Globe Style",
                            subtitle = "Pilih tampilan visual bola dunia 3D",
                            icon = Icons.Default.Map
                        ) {
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                SegmentedButton(
                                    selected = worldClockGlobeStyle == 0,
                                    onClick = { viewModel.setWorldClockGlobeStyle(0) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                ) { Text("Default Blue") }
                                SegmentedButton(
                                    selected = worldClockGlobeStyle == 1,
                                    onClick = { viewModel.setWorldClockGlobeStyle(1) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                ) { Text("Realistic Map") }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SwitchPreference(
                        title = com.example.ui.util.Translations.getString(appLanguage, "24_hour_format"),
                        subtitle = com.example.ui.util.Translations.getString(appLanguage, "use_24_hour_format"),
                        checked = is24HourFormat,
                        onCheckedChange = { viewModel.set24HourFormat(it) },
                        icon = Icons.Default.AccessTime
                    )
                }
            }

            item {
                SettingsSectionCard(com.example.ui.util.Translations.getString(appLanguage, "weather")) {
                    SegmentedPreference(
                        title = "Temperature Unit",
                        subtitle = "Pilih skala suhu cuaca",
                        icon = Icons.Default.Thermostat
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = temperatureUnit == 0,
                                onClick = { viewModel.setTemperatureUnit(0) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("Celsius (°C)") }
                            SegmentedButton(
                                selected = temperatureUnit == 1,
                                onClick = { viewModel.setTemperatureUnit(1) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("Fahrenheit (°F)") }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SegmentedPreference(
                        title = "Weather Refresh Interval",
                        subtitle = "Interval sinkronisasi data cuaca otomatis",
                        icon = Icons.Default.Cloud
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = listOf(15, 30, 60, 120)
                            options.forEachIndexed { index, mins ->
                                val label = when(mins) {
                                    15 -> "15m"
                                    30 -> "30m"
                                    60 -> "1h"
                                    else -> "2h"
                                }
                                SegmentedButton(
                                    selected = weatherRefreshInterval == mins,
                                    onClick = { viewModel.setWeatherRefreshInterval(mins) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) { Text(label) }
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionCard(com.example.ui.util.Translations.getString(appLanguage, "alarms_timers")) {
                    // Ringtone selection
                    SegmentedPreference(
                        title = com.example.ui.util.Translations.getString(appLanguage, "ringtone") ?: "Ringtone",
                        subtitle = "Pilih nada dering alarm utama",
                        icon = Icons.Default.MusicNote
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = listOf("AI Cosmos", "AI Pulse", "Basic")
                            options.forEachIndexed { index, label ->
                                SegmentedButton(
                                    selected = alarmRingtoneIndex == index,
                                    onClick = {
                                        viewModel.setAlarmRingtoneIndex(index)
                                        coroutineScope.launch {
                                            try {
                                                val tg = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                                                when(index) {
                                                    0 -> {
                                                        tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)
                                                        delay(250)
                                                        tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)
                                                    }
                                                    1 -> {
                                                        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
                                                    }
                                                    2 -> {
                                                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
                                                    }
                                                }
                                                delay(500)
                                                tg.release()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SwitchPreference(
                        title = com.example.ui.util.Translations.getString(appLanguage, "default_vibrate"),
                        subtitle = com.example.ui.util.Translations.getString(appLanguage, "vibrate_desc"),
                        checked = defaultVibrate,
                        onCheckedChange = { viewModel.setDefaultVibrate(it) },
                        icon = Icons.Default.Vibration
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SwitchPreference(
                        title = com.example.ui.util.Translations.getString(appLanguage, "notifications"),
                        subtitle = com.example.ui.util.Translations.getString(appLanguage, "notifications_desc"),
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                        icon = Icons.Default.Notifications
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SegmentedPreference(
                        title = "Snooze Duration",
                        subtitle = "Durasi tunda ketika alarm berbunyi",
                        icon = Icons.Default.Snooze
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = listOf(5, 9, 10, 15)
                            options.forEachIndexed { index, mins ->
                                SegmentedButton(
                                    selected = snoozeDuration == mins,
                                    onClick = { viewModel.setSnoozeDuration(mins) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) { Text("${mins}m") }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SegmentedPreference(
                        title = "Alarm Fade-In",
                        subtitle = "Meningkatkan volume secara bertahap",
                        icon = Icons.Default.TrendingUp
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = listOf(0, 5, 10, 20)
                            options.forEachIndexed { index, secs ->
                                SegmentedButton(
                                    selected = alarmFadeInDuration == secs,
                                    onClick = { viewModel.setAlarmFadeInDuration(secs) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                ) { Text(if (secs == 0) "Off" else "${secs}s") }
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionCard("Backup & Cloud Sync") {
                    SwitchPreference(
                        title = "Auto Cloud Backup",
                        subtitle = "Automatically keep settings and alarms synced with cloud backup",
                        checked = autoCloudBackup,
                        onCheckedChange = { viewModel.setAutoCloudBackup(it) },
                        icon = Icons.Default.Cloud
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    val formattedDate = remember(lastBackupTimestamp) {
                        if (lastBackupTimestamp == 0L) {
                            "Never"
                        } else {
                            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                            sdf.format(java.util.Date(lastBackupTimestamp))
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cloud Sync Status",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Last synced: $formattedDate",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Synced status",
                                    tint = if (lastBackupTimestamp > 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    isSyncing = true
                                    syncMessage = "Connecting to cloud server..."
                                    viewModel.triggerCloudSync { success ->
                                        isSyncing = false
                                        syncMessage = if (success) "Cloud Backup synced successfully!" else "Sync failed. Try again."
                                    }
                                },
                                enabled = !isSyncing,
                                modifier = Modifier.weight(1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                )
                            ) {
                                Text("Sync Now", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.exportBackup(context) { jsonString ->
                                        BackupManager.shareBackup(context, jsonString)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Text("Export Share", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    importText = ""
                                    importError = null
                                    showImportDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Text("Import Code", style = MaterialTheme.typography.labelMedium)
                            }

                            TextButton(
                                onClick = {
                                    viewModel.resetToDefaults()
                                    syncMessage = "Settings restored to defaults"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset Defaults", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        syncMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            androidx.compose.runtime.LaunchedEffect(msg) {
                                delay(3000)
                                if (syncMessage == msg) syncMessage = null
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionCard(com.example.ui.util.Translations.getString(appLanguage, "about")) {
                    PreferenceItem(
                        title = com.example.ui.util.Translations.getString(appLanguage, "version"),
                        subtitle = "1.35.44",
                        onClick = {},
                        icon = Icons.Default.Info
                    )
                }
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore Backup") },
            text = {
                Column {
                    Text(
                        text = "Paste your exported backup JSON code below to restore your exact preferences and alarm schedules.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { 
                            importText = it
                            importError = null 
                        },
                        placeholder = { Text("Paste JSON backup code here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        isError = importError != null,
                        maxLines = 8
                    )
                    importError?.let { err ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importText.isBlank()) {
                            importError = "Backup text cannot be empty"
                            return@Button
                        }
                        viewModel.importBackup(importText) { success ->
                            if (success) {
                                showImportDialog = false
                                syncMessage = "Backup restored successfully!"
                            } else {
                                importError = "Invalid backup code. Please verify JSON correctness."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Restore Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun SettingsSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                letterSpacing = 1.sp
            )
            content()
        }
    }
}

@Composable
fun SwitchPreference(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun PreferenceItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SegmentedPreference(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}
