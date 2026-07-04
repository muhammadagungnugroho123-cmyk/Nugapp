package com.example.ui.screens.alarms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ClockApplication
import com.example.data.local.AlarmEntity
import com.example.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AlarmsViewModel(private val repository: AlarmRepository) : ViewModel() {

    val alarms: Flow<List<AlarmEntity>> = repository.getAllAlarms()

    fun addAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.insertAlarm(alarm)
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm.copy(isEnabled = !alarm.isEnabled))
        }
    }

    fun deleteAlarm(id: Int) {
        viewModelScope.launch {
            repository.deleteAlarmById(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as ClockApplication
                AlarmsViewModel(application.container.alarmRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    appLanguage: String = "en",
    viewModel: AlarmsViewModel = viewModel(factory = AlarmsViewModel.Factory),
    onNavigateToSettings: () -> Unit = {}
) {
    val allAlarms by viewModel.alarms.collectAsState(initial = emptyList())
    val alarms = remember(allAlarms) { allAlarms.filter { !it.label.startsWith("Snooze:") } }
    var showDialog by remember { mutableStateOf(false) }
    var alarmToEdit by remember { mutableStateOf<AlarmEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        AlarmDialog(
            appLanguage = appLanguage,
            initialAlarm = alarmToEdit,
            onDismiss = {
                showDialog = false
                alarmToEdit = null
            },
            onSave = { newAlarm ->
                if (alarmToEdit == null) {
                    viewModel.addAlarm(newAlarm)
                    coroutineScope.launch { snackbarHostState.showSnackbar(com.example.ui.util.Translations.getString(appLanguage, "save_alarm") + " ditambahkan") }
                } else {
                    viewModel.updateAlarm(newAlarm.copy(id = alarmToEdit!!.id))
                    coroutineScope.launch { snackbarHostState.showSnackbar(com.example.ui.util.Translations.getString(appLanguage, "save_alarm") + " diperbarui") }
                }
                showDialog = false
                alarmToEdit = null
            }
        )
    }

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, 
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(com.example.ui.util.Translations.getString(appLanguage, "alarms"), color = androidx.compose.ui.graphics.Color(0xFF00E5FF)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = androidx.compose.ui.graphics.Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                alarmToEdit = null
                showDialog = true
            }, containerColor = androidx.compose.ui.graphics.Color(0xFF00E5FF)) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm", tint = androidx.compose.ui.graphics.Color(0xFF0B131E))
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFF0B131E),
                            androidx.compose.ui.graphics.Color(0xFF152A4A),
                            androidx.compose.ui.graphics.Color(0xFF0B131E)
                        )
                    )
                )
        ) {
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = com.example.ui.util.Translations.getString(appLanguage, "no_alarms"),
                            style = MaterialTheme.typography.titleMedium,
                            color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                items(alarms.sortedWith(compareBy({ it.hour }, { it.minute }))) { alarm ->
                    AlarmItem(
                        appLanguage = appLanguage,
                        alarm = alarm,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onClick = {
                            alarmToEdit = alarm
                            showDialog = true
                        },
                        onDelete = {
                            viewModel.deleteAlarm(alarm.id)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Alarm deleted") }
                        }
                    )
                }
            }
        }
    }
}
}

@Composable
fun AlarmItem(
    appLanguage: String = "en",
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFF131D26).copy(alpha = if (alarm.isEnabled) 0.8f else 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF00E5FF).copy(alpha = if (alarm.isEnabled) 0.4f else 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                        fontSize = 48.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = if (alarm.isEnabled) androidx.compose.ui.graphics.Color(0xFF00E5FF) else androidx.compose.ui.graphics.Color.Gray
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (alarm.label.isNotEmpty()) {
                        Text(
                            text = alarm.label + if (alarm.daysOfWeek > 0) " | " else "",
                            fontSize = 15.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = if (alarm.isEnabled) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f) else androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                    if (alarm.daysOfWeek > 0) {
                        val days = com.example.ui.util.Translations.getList(appLanguage, "days").ifEmpty { listOf("M", "T", "W", "T", "F", "S", "S") }
                        val selectedDays = days.filterIndexed { index, _ -> (alarm.daysOfWeek and (1 shl index)) != 0 }
                        Text(
                            text = selectedDays.joinToString(", "),
                            fontSize = 15.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = if (alarm.isEnabled) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f) else androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                }
                
                if (alarm.isEnabled) {
                    var currentTimeMs by remember { androidx.compose.runtime.mutableStateOf(System.currentTimeMillis()) }
                    
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        while (true) {
                            kotlinx.coroutines.delay(60_000 - (System.currentTimeMillis() % 60_000))
                            currentTimeMs = System.currentTimeMillis()
                        }
                    }
                    
                    val remainingText = remember(alarm.hour, alarm.minute, alarm.daysOfWeek, currentTimeMs) {
                        val now = java.util.Calendar.getInstance().apply { timeInMillis = currentTimeMs }
                        val alarmTime = java.util.Calendar.getInstance().apply {
                            if (alarm.dateMillis > 0L) {
                                timeInMillis = alarm.dateMillis
                            } else {
                                timeInMillis = currentTimeMs
                            }
                            set(java.util.Calendar.HOUR_OF_DAY, alarm.hour)
                            set(java.util.Calendar.MINUTE, alarm.minute)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        
                        if (alarm.dateMillis > 0L) {
                            // If dateMillis is set, we don't adjust the date.
                        } else if (alarm.daysOfWeek == 0) {
                            if (alarmTime.before(now)) {
                                alarmTime.add(java.util.Calendar.DAY_OF_YEAR, 1)
                            }
                        } else {
                            var addedDays = 0
                            while (addedDays < 7) {
                                if (addedDays > 0) {
                                    alarmTime.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                }
                                val dayOfWeek = (alarmTime.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
                                if ((alarm.daysOfWeek and (1 shl dayOfWeek)) != 0 && alarmTime.after(now)) {
                                    break
                                }
                                addedDays++
                            }
                        }

                        val diff = alarmTime.timeInMillis - now.timeInMillis
                        if (diff < 0) {
                            "Expired"
                        } else {
                            val days = diff / (1000 * 60 * 60 * 24)
                            val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                            val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
                            
                            buildString {
                                append("In ")
                                if (days > 0) append("$days d ")
                                if (hours > 0) append("$hours h ")
                                if (minutes > 0) append("$minutes m")
                                if (days == 0L && hours == 0L && minutes <= 0L) append("< 1 m")
                            }.trim()
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(
                                color = androidx.compose.ui.graphics.Color(0xFFFFD700).copy(alpha = 0.15f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = remainingText,
                            fontSize = 13.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFFFFD700),
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = androidx.compose.ui.graphics.Color(0xFF0B131E),
                        checkedTrackColor = androidx.compose.ui.graphics.Color(0xFF00E5FF),
                        uncheckedThumbColor = androidx.compose.ui.graphics.Color.Gray,
                        uncheckedTrackColor = androidx.compose.ui.graphics.Color.DarkGray
                    )
                )
                
                IconButton(onClick = onDelete, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Alarm",
                        tint = if (alarm.isEnabled) androidx.compose.ui.graphics.Color(0xFFFF5252).copy(alpha = 0.8f) else androidx.compose.ui.graphics.Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
