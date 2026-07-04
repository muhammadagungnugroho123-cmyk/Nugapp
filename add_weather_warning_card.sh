sed -i '/if (weatherData!!.hourly.isNotEmpty()) {/i \
                if (weatherData!!.code in listOf(95, 96, 99, 61, 63, 65, 71, 73, 75, 77, 85, 86)) {\
                    Spacer(modifier = Modifier.height(16.dp))\
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Color(0xFFFFB300).copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(12.dp)) {\
                        Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))\
                        Spacer(modifier = Modifier.width(8.dp))\
                        Text(if (weatherData!!.code in listOf(95, 96, 99)) "Peringatan Dini: Potensi Badai Petir" else if (weatherData!!.code in listOf(71, 73, 75, 77, 85, 86)) "Peringatan Dini: Potensi Salju/Suhu Ekstrim" else "Peringatan Dini: Hujan Deras", color = Color(0xFFFFB300), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))\
                    }\
                }' app/src/main/java/com/example/ui/screens/clock/LiveWeatherCard.kt
