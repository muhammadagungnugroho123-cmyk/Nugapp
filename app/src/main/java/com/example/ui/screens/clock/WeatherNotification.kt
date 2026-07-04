package com.example.ui.screens.clock

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedWeatherNotificationItem(
    title: String = "WEATHER WARNING",
    time: String = "NOW",
    desc: String = "Rain expected in your area soon",
    color: Color = Color(0xFF00E5FF)
) {
    val infiniteTransition = rememberInfiniteTransition()
    val dropY1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val dropY2 by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp).align(Alignment.TopCenter)
                )
                // Rain drops
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = color.copy(alpha = 0.7f),
                        start = Offset(size.width * 0.3f, size.height * 0.5f + dropY1),
                        end = Offset(size.width * 0.3f, size.height * 0.5f + dropY1 + 4.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = color.copy(alpha = 0.7f),
                        start = Offset(size.width * 0.7f, size.height * 0.5f + dropY2),
                        end = Offset(size.width * 0.7f, size.height * 0.5f + dropY2 + 4.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.LightGray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }
        }
    }
}
