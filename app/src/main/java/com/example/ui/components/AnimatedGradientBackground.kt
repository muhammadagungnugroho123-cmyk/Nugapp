package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate

@Composable
fun AnimatedGradientBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_anim"
    )

    val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val color2 = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
    val color3 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
    val backgroundColor = MaterialTheme.colorScheme.background

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = backgroundColor)
        
        val width = size.width
        val height = size.height

        val x1 = width * animValue
        val y1 = height * 0.2f
        val radius1 = width * 0.8f

        val x2 = width * (1f - animValue)
        val y2 = height * 0.8f
        val radius2 = width * 0.9f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1, Color.Transparent),
                center = Offset(x1, y1),
                radius = radius1
            ),
            radius = radius1,
            center = Offset(x1, y1)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2, Color.Transparent),
                center = Offset(x2, y2),
                radius = radius2
            ),
            radius = radius2,
            center = Offset(x2, y2)
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3, Color.Transparent),
                center = Offset(width * 0.5f, height * animValue),
                radius = radius1
            ),
            radius = radius1,
            center = Offset(width * 0.5f, height * animValue)
        )
    }
}
