sed -i '42i import androidx.compose.ui.graphics.graphicsLayer' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
sed -i '/val infiniteTransition = rememberInfiniteTransition()/a\
    val pulseScale by infiniteTransition.animateFloat(\
        initialValue = 0.95f,\
        targetValue = 1.05f,\
        animationSpec = infiniteRepeatable(\
            animation = tween(1500, easing = FastOutSlowInEasing),\
            repeatMode = RepeatMode.Reverse\
        )\
    )' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
sed -i 's/modifier = Modifier.size(100.dp)/modifier = Modifier.size(100.dp).graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)/g' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
