sed -i '/center = Offset(dotX, dotY)/a \
                            val paint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 28f }\
                            drawContext.canvas.nativeCanvas.drawText(city.name, dotX + 15f, dotY + 10f, paint)' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
