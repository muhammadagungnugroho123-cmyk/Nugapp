import re

with open('app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt', 'r') as f:
    content = f.read()

# We need to replace the section from "// Outer atmosphere glow" to the end of "cities.forEach { ... }"
start_str = "// Outer atmosphere glow"
end_str = "drawContext.canvas.nativeCanvas.drawText(\"\\${city.name} $timeString\", dotX + 15f, dotY + 10f, paint)\n                        }\n                    }"

start_idx = content.find(start_str)
end_idx = content.find(end_str) + len(end_str)

if start_idx == -1 or content.find(end_str) == -1:
    print("Could not find start or end block")
    exit(1)

new_block = """// Outer atmosphere glow
                    val isRealistic = globeStyle == 1
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isRealistic) Color(0xFF1E283A).copy(alpha = atmospherePulse * 0.5f) else Color(0xFF81D4FA).copy(alpha = atmospherePulse), 
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * 1.25f,
                        ),
                        radius = radius * 1.25f,
                        center = center
                    )
                    
                    // Ocean base (realistic blue or default blue)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isRealistic) listOf(Color(0xFF141F31), Color(0xFF0F1520), Color(0xFF070B12), Color(0xFF000000)) 
                                     else listOf(Color(0xFF4FC3F7), Color(0xFF0288D1), Color(0xFF01579B), Color(0xFF000000)),
                            center = Offset(center.x - radius * 0.4f, center.y - radius * 0.4f),
                            radius = radius * 1.6f
                        ),
                        radius = radius,
                        center = center
                    )

                    val tiltRad = Math.toRadians(manualTilt.toDouble())
                    
                    if (isRealistic) {
                        // Draw realistic map from WorldMapData
                        val mapPath = androidx.compose.ui.graphics.Path()
                        for (poly in WorldMapData.polygons) {
                            var isFirst = true
                            for (i in poly.indices step 2) {
                                val lon = poly[i]
                                val lat = poly[i+1]
                                val latRad = Math.toRadians(lat.toDouble())
                                val lonRad = Math.toRadians((lon + rotationAngle + manualRotation).toDouble())
                                
                                val x1 = kotlin.math.cos(latRad) * kotlin.math.sin(lonRad)
                                val y1 = -kotlin.math.sin(latRad)
                                val z1 = kotlin.math.cos(latRad) * kotlin.math.cos(lonRad)
                                
                                val x = x1
                                val y = y1 * kotlin.math.cos(tiltRad) - z1 * kotlin.math.sin(tiltRad)
                                val z = y1 * kotlin.math.sin(tiltRad) + z1 * kotlin.math.cos(tiltRad)
                                
                                if (z > -0.05) {
                                    val px = center.x + (x * radius).toFloat()
                                    val py = center.y + (y * radius).toFloat()
                                    if (isFirst) {
                                        mapPath.moveTo(px, py)
                                        isFirst = false
                                    } else {
                                        mapPath.lineTo(px, py)
                                    }
                                } else {
                                    isFirst = true
                                }
                            }
                        }
                        drawPath(mapPath, color = Color(0xFF6B655C), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    } else {
                        // Default Blue: Rotating Longitude Lines
                        for (lon in 0..360 step 30) {
                            val path = androidx.compose.ui.graphics.Path()
                            var isFirst = true
                            for (lat in -90..90 step 5) {
                                val latRad = Math.toRadians(lat.toDouble())
                                val lonRad = Math.toRadians((lon + rotationAngle + manualRotation).toDouble())
                                val x1 = kotlin.math.cos(latRad) * kotlin.math.sin(lonRad)
                                val y1 = -kotlin.math.sin(latRad)
                                val z1 = kotlin.math.cos(latRad) * kotlin.math.cos(lonRad)
                                val x = x1
                                val y = y1 * kotlin.math.cos(tiltRad) - z1 * kotlin.math.sin(tiltRad)
                                val z = y1 * kotlin.math.sin(tiltRad) + z1 * kotlin.math.cos(tiltRad)
                                
                                if (z > -0.1) {
                                    val px = center.x + (x * radius).toFloat()
                                    val py = center.y + (y * radius).toFloat()
                                    if (isFirst) {
                                        path.moveTo(px, py)
                                        isFirst = false
                                    } else {
                                        path.lineTo(px, py)
                                    }
                                } else {
                                    isFirst = true
                                }
                            }
                            drawPath(path, color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 1.dp.toPx()))
                        }
    
                        // Rotating Latitude Lines
                        for (lat in -80..80 step 20) {
                            val path = androidx.compose.ui.graphics.Path()
                            var isFirst = true
                            for (lon in 0..360 step 5) {
                                val latRad = Math.toRadians(lat.toDouble())
                                val lonRad = Math.toRadians((lon + rotationAngle + manualRotation).toDouble())
                                val x1 = kotlin.math.cos(latRad) * kotlin.math.sin(lonRad)
                                val y1 = -kotlin.math.sin(latRad)
                                val z1 = kotlin.math.cos(latRad) * kotlin.math.cos(lonRad)
                                val x = x1
                                val y = y1 * kotlin.math.cos(tiltRad) - z1 * kotlin.math.sin(tiltRad)
                                val z = y1 * kotlin.math.sin(tiltRad) + z1 * kotlin.math.cos(tiltRad)
                                
                                if (z > -0.1) {
                                    val px = center.x + (x * radius).toFloat()
                                    val py = center.y + (y * radius).toFloat()
                                    if (isFirst) {
                                        path.moveTo(px, py)
                                        isFirst = false
                                    } else {
                                        path.lineTo(px, py)
                                    }
                                } else {
                                    isFirst = true
                                }
                            }
                            drawPath(path, color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 1.dp.toPx()))
                        }
                    }
                    
                    // Inner shadow for 3D sphere effect
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color(0xAA000000)),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                    
                    // Draw dots for added cities
                    val paintName = android.graphics.Paint().apply { 
                        isAntiAlias = true
                        setShadowLayer(8f, 0f, 4f, android.graphics.Color.BLACK)
                        typeface = android.graphics.Typeface.DEFAULT
                        color = android.graphics.Color.WHITE
                        textSize = 32f
                    }
                    val paintTime = android.graphics.Paint().apply { 
                        isAntiAlias = true
                        setShadowLayer(8f, 0f, 4f, android.graphics.Color.BLACK)
                        typeface = android.graphics.Typeface.DEFAULT
                        color = android.graphics.Color.parseColor("#B0BEC5")
                        textSize = 28f
                    }
                    
                    cities.forEach { city ->
                        val latRad = Math.toRadians(city.lat.toDouble())
                        val lonRad = Math.toRadians((city.lon.toDouble() + rotationAngle + manualRotation))
                        
                        val x1 = kotlin.math.cos(latRad) * kotlin.math.sin(lonRad)
                        val y1 = -kotlin.math.sin(latRad)
                        val z1 = kotlin.math.cos(latRad) * kotlin.math.cos(lonRad)
                        
                        val x = x1
                        val y = y1 * kotlin.math.cos(tiltRad) - z1 * kotlin.math.sin(tiltRad)
                        val z = y1 * kotlin.math.sin(tiltRad) + z1 * kotlin.math.cos(tiltRad)
                        
                        // If z > 0, it's on the front of the globe
                        if (z > 0) {
                            val dotX = center.x + (x * radius).toFloat()
                            val dotY = center.y + (y * radius).toFloat()
                            
                            drawCircle(
                                color = if (isRealistic) Color(0xFF90CAF9) else Color(0xFF00E5FF),
                                radius = 4.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                            
                            sdfTime.timeZone = java.util.TimeZone.getTimeZone(city.timeZoneId)
                            val timeString = sdfTime.format(currentTimeState.value)
                            
                            // Adjust text position slightly
                            val textX = dotX - (paintName.measureText(city.name) / 2f)
                            drawContext.canvas.nativeCanvas.drawText(city.name, textX, dotY - 15f, paintName)
                            val timeX = dotX - (paintTime.measureText(timeString) / 2f)
                            drawContext.canvas.nativeCanvas.drawText(timeString, timeX, dotY + 25f, paintTime)
                        }
                    }"""

new_content = content[:start_idx] + new_block + content[end_idx:]

with open('app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt', 'w') as f:
    f.write(new_content)
