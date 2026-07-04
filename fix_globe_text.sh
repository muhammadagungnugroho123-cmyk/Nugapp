sed -i '/drawCircle(color = Color.White/a \
                            val paint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 28f }\
                            drawContext.canvas.nativeCanvas.drawText(city.name, dotX + 10f, dotY + 10f, paint)' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
sed -i '25i import androidx.compose.ui.graphics.nativeCanvas' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
