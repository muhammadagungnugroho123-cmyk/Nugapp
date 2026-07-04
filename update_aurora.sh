sed -i '93,120c\
                        // Aurora and Stars Background\
                        val starOffset by infiniteTransition.animateFloat(\
                            initialValue = 0f,\
                            targetValue = 1f,\
                            animationSpec = infiniteRepeatable(\
                                animation = tween(20000, easing = LinearEasing),\
                                repeatMode = RepeatMode.Restart\
                            ),\
                            label = "starOffset"\
                        )\
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {\
                            // Stars\
                            val rand = java.util.Random(42)\
                            for (i in 0..50) {\
                                val x = rand.nextFloat() * size.width\
                                val y = (rand.nextFloat() * size.height + (starOffset * size.height)) % size.height\
                                val radius = rand.nextFloat() * 2f + 1f\
                                val alpha = rand.nextFloat() * 0.5f + 0.1f\
                                drawCircle(\
                                    color = Color.White.copy(alpha = alpha),\
                                    radius = radius,\
                                    center = androidx.compose.ui.geometry.Offset(x, y)\
                                )\
                            }\
                            \
                            // Aurora 1 (Cyan)\
                            drawOval(\
                                brush = Brush.radialGradient(\
                                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.6f + (bgPulse * 0.2f)), Color.Transparent),\
                                    center = androidx.compose.ui.geometry.Offset(size.width * (0.2f + bgPulse * 0.1f), size.height * 0.3f),\
                                    radius = size.maxDimension * 0.7f\
                                ),\
                                topLeft = androidx.compose.ui.geometry.Offset(-size.width * 0.5f, -size.height * 0.2f),\
                                size = androidx.compose.ui.geometry.Size(size.width * 2f, size.height)\
                            )\
                            // Aurora 2 (Purple)\
                            drawOval(\
                                brush = Brush.radialGradient(\
                                    colors = listOf(Color(0xFF9D4EDD).copy(alpha = 0.5f + ((1f - bgPulse) * 0.2f)), Color.Transparent),\
                                    center = androidx.compose.ui.geometry.Offset(size.width * (0.8f - bgPulse * 0.1f), size.height * 0.1f),\
                                    radius = size.maxDimension * 0.8f\
                                ),\
                                topLeft = androidx.compose.ui.geometry.Offset(0f, -size.height * 0.3f),\
                                size = androidx.compose.ui.geometry.Size(size.width * 1.5f, size.height * 1.2f)\
                            )\
                            // Aurora 3 (Teal)\
                            drawOval(\
                                brush = Brush.radialGradient(\
                                    colors = listOf(Color(0xFF00BFA5).copy(alpha = 0.4f + (bgPulse * 0.3f)), Color.Transparent),\
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * (0.8f - bgPulse * 0.1f)),\
                                    radius = size.maxDimension * 0.6f\
                                ),\
                                topLeft = androidx.compose.ui.geometry.Offset(-size.width * 0.2f, size.height * 0.4f),\
                                size = androidx.compose.ui.geometry.Size(size.width * 1.4f, size.height)\
                            )\
                        }\
' app/src/main/java/com/example/ui/screens/alarms/AlarmActivity.kt
