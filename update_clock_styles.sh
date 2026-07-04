sed -i '/\/\/ Sci-Fi Clock Face/i \                if (clockStyleIndex == 0) {' app/src/main/java/com/example/ui/screens/clock/ClockScreen.kt
sed -i '/\/\/ Glassy Dashboard Panel/i \                } else if (clockStyleIndex == 1) {\
                    Box(\
                        modifier = Modifier\
                            .size(320.dp)\
                            .padding(16.dp)\
                            .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(32.dp)),\
                        contentAlignment = Alignment.Center\
                    ) {\
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {\
                            Text(\
                                text = timeFormat.format(currentTime.time),\
                                style = MaterialTheme.typography.displayLarge.copy(\
                                    fontSize = 72.sp,\
                                    fontWeight = FontWeight.Bold,\
                                    letterSpacing = 2.sp\
                                ),\
                                color = Color.White\
                            )\
                            Text(\
                                text = amPmFormat.format(currentTime.time).uppercase(),\
                                style = MaterialTheme.typography.titleLarge.copy(\
                                    fontWeight = FontWeight.Light,\
                                    letterSpacing = 4.sp\
                                ),\
                                color = Color(0xFF00E5FF)\
                            )\
                        }\
                    }\
                    Spacer(modifier = Modifier.height(16.dp))\
                } else {\
                    Box(\
                        modifier = Modifier\
                            .size(320.dp)\
                            .padding(16.dp),\
                        contentAlignment = Alignment.Center\
                    ) {\
                        val primaryColor = Color(0xFF00E5FF)\
                        val hour = currentTime.get(Calendar.HOUR)\
                        val minute = currentTime.get(Calendar.MINUTE)\
                        val second = currentTime.get(Calendar.SECOND)\
                        val ms = currentTime.get(Calendar.MILLISECOND)\
                        Canvas(modifier = Modifier.fillMaxSize()) {\
                            drawCircle(color = Color.White.copy(alpha = 0.1f), radius = size.width / 2)\
                            drawCircle(color = primaryColor, radius = size.width / 2, style = Stroke(4.dp.toPx()))\
                            for (i in 0 until 12) {\
                                val angle = Math.toRadians((i * 30 - 90).toDouble())\
                                val startX = (size.width / 2 + (size.width / 2 - 20.dp.toPx()) * cos(angle)).toFloat()\
                                val startY = (size.height / 2 + (size.width / 2 - 20.dp.toPx()) * sin(angle)).toFloat()\
                                val endX = (size.width / 2 + (size.width / 2) * cos(angle)).toFloat()\
                                val endY = (size.height / 2 + (size.width / 2) * sin(angle)).toFloat()\
                                drawLine(color = Color.White.copy(alpha = 0.5f), start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 2.dp.toPx())\
                            }\
                            val hourAngle = Math.toRadians((hour * 30 + minute * 0.5 - 90).toDouble())\
                            val hX = (size.width / 2 + (size.width / 3.5f) * cos(hourAngle)).toFloat()\
                            val hY = (size.height / 2 + (size.width / 3.5f) * sin(hourAngle)).toFloat()\
                            drawLine(color = Color.White, start = Offset(size.width / 2, size.height / 2), end = Offset(hX, hY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)\
                            val minAngle = Math.toRadians((minute * 6 + second * 0.1 - 90).toDouble())\
                            val mX = (size.width / 2 + (size.width / 2.5f) * cos(minAngle)).toFloat()\
                            val mY = (size.height / 2 + (size.width / 2.5f) * sin(minAngle)).toFloat()\
                            drawLine(color = Color.White.copy(alpha = 0.8f), start = Offset(size.width / 2, size.height / 2), end = Offset(mX, mY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)\
                            val secAngle = Math.toRadians((second * 6 + ms * 0.006 - 90).toDouble())\
                            val sX = (size.width / 2 + (size.width / 2.2f) * cos(secAngle)).toFloat()\
                            val sY = (size.height / 2 + (size.width / 2.2f) * sin(secAngle)).toFloat()\
                            drawLine(color = primaryColor, start = Offset(size.width / 2, size.height / 2), end = Offset(sX, sY), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)\
                            drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = Offset(size.width / 2, size.height / 2))\
                        }\
                    }\
                    Spacer(modifier = Modifier.height(16.dp))\
                }' app/src/main/java/com/example/ui/screens/clock/ClockScreen.kt
