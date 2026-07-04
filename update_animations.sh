sed -i '/val lightningAlpha by/i\
    val pulseScale by infiniteTransition.animateFloat(\
        initialValue = 0.95f,\
        targetValue = 1.05f,\
        animationSpec = infiniteRepeatable(\
            animation = tween(2000, easing = FastOutSlowInEasing),\
            repeatMode = RepeatMode.Reverse\
        )\
    )' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
