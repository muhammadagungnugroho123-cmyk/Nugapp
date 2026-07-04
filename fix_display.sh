sed -i '104,211c\
            StopwatchDisplay(\
                elapsedTime = elapsedTime,\
                laps = laps,\
                formatTime = ::formatTime\
            )' app/src/main/java/com/example/ui/screens/stopwatch/StopwatchScreen.kt

cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/screens/stopwatch/StopwatchScreen.kt

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
INNER_EOF
