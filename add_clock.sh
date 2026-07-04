sed -i '57i @Composable\
fun RealtimeClock(utcOffsetSeconds: Int) {\
    var time by remember { mutableStateOf("") }\
    LaunchedEffect(utcOffsetSeconds) {\
        while (true) {\
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())\
            val offsetHours = Math.abs(utcOffsetSeconds) / 3600\
            val offsetMinutes = (Math.abs(utcOffsetSeconds) % 3600) / 60\
            val sign = if (utcOffsetSeconds >= 0) "+" else "-"\
            sdf.timeZone = java.util.TimeZone.getTimeZone("GMT$sign$offsetHours:${String.format("%02d", offsetMinutes)}")\
            time = sdf.format(java.util.Date())\
            kotlinx.coroutines.delay(1000)\
        }\
    }\
    Text(time, color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)\
}' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
