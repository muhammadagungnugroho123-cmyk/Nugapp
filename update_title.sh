sed -i '/Text(data.locationName.uppercase(), color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))/c\
                                    Column(modifier = Modifier.weight(1f)) {\
                                        Text(data.locationName.uppercase(), color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))\
                                        Spacer(modifier = Modifier.height(2.dp))\
                                        RealtimeClock(data.utcOffsetSeconds)\
                                    }' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
