sed -i '/colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B131E))/i \                actions = {\
                    IconButton(onClick = onNavigateToSettings) {\
                        Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)\
                    }\
                },' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
