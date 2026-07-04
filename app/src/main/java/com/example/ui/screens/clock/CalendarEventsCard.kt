package com.example.ui.screens.clock

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class CalendarEvent(
    val title: String,
    val description: String,
    val startTime: Long,
    val color: Int
)

@Composable
fun CalendarEventsCard() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions[Manifest.permission.READ_CALENDAR] == true
    }

    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            while (true) {
                withContext(Dispatchers.IO) {
                    val newEvents = mutableListOf<CalendarEvent>()
                    val projection = arrayOf(
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.DESCRIPTION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DISPLAY_COLOR
                    )
                    
                    val now = System.currentTimeMillis()
                    val tomorrow = now + 86400000 * 2 // Look 2 days ahead
                    
                    val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
                    val selectionArgs = arrayOf(now.toString(), tomorrow.toString())
                    
                    try {
                        val cursor: Cursor? = context.contentResolver.query(
                            CalendarContract.Events.CONTENT_URI,
                            projection,
                            selection,
                            selectionArgs,
                            "${CalendarContract.Events.DTSTART} ASC"
                        )
                        
                        cursor?.use {
                            val titleIdx = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                            val descIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)
                            val startIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                            val colorIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DISPLAY_COLOR)
                            
                            var count = 0
                            while (it.moveToNext() && count < 3) {
                                val title = it.getString(titleIdx) ?: "Event"
                                val desc = it.getString(descIdx) ?: ""
                                val startTime = it.getLong(startIdx)
                                val color = it.getInt(colorIdx)
                                
                                newEvents.add(CalendarEvent(title, desc, startTime, color))
                                count++
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    withContext(Dispatchers.Main) {
                        com.example.data.calendar.CalendarAlarmScheduler.scheduleEventAlarms(context, newEvents)
                        events = newEvents
                    }
                }
                // Update every minute for live realtime
                delay(60000)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB388FF).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange, 
                        contentDescription = "Calendar", 
                        tint = Color(0xFFB388FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "CALENDAR SYNC", 
                        color = Color(0xFFB388FF), 
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!hasPermission) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Permission Required",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        IconButton(onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_INSERT)
                                .setData(android.provider.CalendarContract.Events.CONTENT_URI)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Event",
                                tint = Color(0xFFB388FF)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) launcher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.POST_NOTIFICATIONS)) else launcher.launch(arrayOf(Manifest.permission.READ_CALENDAR)) }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tap to sync calendar events", color = Color(0xFFB388FF), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                if (events.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No upcoming events", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    events.forEachIndexed { index, event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(event.color).copy(alpha = 1f).takeIf { event.color != 0 } ?: Color(0xFFB388FF))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.startTime))
                                Text(
                                    text = timeStr,
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        
                        if (index < events.size - 1) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}
