package com.example.ui.screens.alarms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val coroutineScope = rememberCoroutineScope()
    
    // We pad the items with empty strings at the beginning and end so the first and last items can be centered
    val visibleItemsCount = 3
    val paddedItems = listOf("") + items + listOf("")

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                val clampedIndex = index.coerceIn(0, items.size - 1)
                onItemSelected(clampedIndex)
            }
    }

    Box(
        modifier = modifier.height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(paddedItems.size) { index ->
                val text = paddedItems[index]
                val isSelected = index == listState.firstVisibleItemIndex + 1
                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = if (isSelected) 48.sp else 32.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.alpha(if (isSelected) 1f else 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun WheelTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit
) {
    val hours = (0..23).map { String.format("%02d", it) }
    val minutes = (0..59).map { String.format("%02d", it) }

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelPicker(
            items = hours,
            initialIndex = initialHour,
            onItemSelected = { 
                selectedHour = it
                onTimeChanged(selectedHour, selectedMinute)
            },
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = ":",
            fontSize = 48.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        WheelPicker(
            items = minutes,
            initialIndex = initialMinute,
            onItemSelected = { 
                selectedMinute = it
                onTimeChanged(selectedHour, selectedMinute)
            },
            modifier = Modifier.weight(1f)
        )
    }
}
