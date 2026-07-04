sed -i '/val infiniteTransition = rememberInfiniteTransition()/a\
                    val pulseScale by infiniteTransition.animateFloat(\
                        initialValue = 0.98f,\
                        targetValue = 1.02f,\
                        animationSpec = infiniteRepeatable(\
                            animation = tween(2000, easing = FastOutSlowInEasing),\
                            repeatMode = RepeatMode.Reverse\
                        )\
                    )' app/src/main/java/com/example/ui/screens/clock/ClockScreen.kt
sed -i 's/modifier = Modifier.fillMaxSize()/modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)/g' app/src/main/java/com/example/ui/screens/clock/ClockScreen.kt
