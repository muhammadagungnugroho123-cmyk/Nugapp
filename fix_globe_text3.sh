sed -i '/if (z > 0) {/,/}/c\
                        // If z > 0, it is on the front of the globe\
                        if (z > 0) {\
                            val dotX = center.x + (x * radius).toFloat()\
                            val dotY = center.y + (y * radius).toFloat()\
                            drawCircle(\
                                color = Color(0xFF00E5FF),\
                                radius = 4.dp.toPx(),\
                                center = Offset(dotX, dotY)\
                            )\
                            drawCircle(\
                                color = Color.White,\
                                radius = 2.dp.toPx(),\
                                center = Offset(dotX, dotY)\
                            )\
                            val paint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 28f }\
                            drawContext.canvas.nativeCanvas.drawText(city.name, dotX + 15f, dotY + 10f, paint)\
                        }' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
