sed -i '/drawContext.canvas.nativeCanvas.drawText(city.name, dotX + 15f, dotY + 10f, paint)/c\
                            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())\
                            sdfTime.timeZone = TimeZone.getTimeZone(city.timeZoneId)\
                            val timeString = sdfTime.format(currentTime)\
                            drawContext.canvas.nativeCanvas.drawText("${city.name} $timeString", dotX + 15f, dotY + 10f, paint)' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
