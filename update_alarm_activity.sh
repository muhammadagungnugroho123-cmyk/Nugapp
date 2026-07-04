sed -i '75,246c\
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")\
                    val bgPulse by infiniteTransition.animateFloat(\
                        initialValue = 0f,\
                        targetValue = 1f,\
                        animationSpec = infiniteRepeatable(\
                            animation = tween(6000, easing = LinearEasing),\
                            repeatMode = RepeatMode.Reverse\
                        ),\
                        label = "bgPulse"\
                    )\
                    \
                    var aiGreeting by remember { mutableStateOf(preFetchedGreeting) }\
                    val context = androidx.compose.ui.platform.LocalContext.current\
                    \
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF030D22))) {\
                        // Aurora Background\
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {\
                            // Aurora 1\
                            drawRect(\
                                brush = Brush.radialGradient(\
                                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.5f + (bgPulse * 0.2f)), Color.Transparent),\
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.3f),\
                                    radius = size.maxDimension * 0.6f\
                                )\
                            )\
                            // Aurora 2\
                            drawRect(\
                                brush = Brush.radialGradient(\
                                    colors = listOf(Color(0xFFB388FF).copy(alpha = 0.4f + ((1f - bgPulse) * 0.2f)), Color.Transparent),\
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f),\
                                    radius = size.maxDimension * 0.7f\
                                )\
                            )\
                            // Aurora 3\
                            drawRect(\
                                brush = Brush.radialGradient(\
                                    colors = listOf(Color(0xFF00BFA5).copy(alpha = 0.3f + (bgPulse * 0.3f)), Color.Transparent),\
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.8f),\
                                    radius = size.maxDimension * 0.5f\
                                )\
                            )\
                        }\
                        \
                        Column(\
                            modifier = Modifier\
                                .fillMaxSize()\
                                .padding(vertical = 48.dp, horizontal = 24.dp),\
                            horizontalAlignment = Alignment.CenterHorizontally,\
                            verticalArrangement = Arrangement.SpaceBetween\
                        ) {\
                            Text(\
                                text = com.example.ui.util.Translations.getString(appLanguage, "alarms"),\
                                color = Color.White,\
                                fontSize = 20.sp,\
                                fontWeight = FontWeight.Medium\
                            )\
                            \
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {\
                                val dateFormat = java.text.SimpleDateFormat("hh:mm", java.util.Locale.getDefault())\
                                val amPmFormat = java.text.SimpleDateFormat("a", java.util.Locale.getDefault())\
                                val now = java.util.Date()\
                                Row(verticalAlignment = Alignment.Bottom) {\
                                    Text(\
                                        text = dateFormat.format(now),\
                                        style = MaterialTheme.typography.displayLarge.copy(\
                                            fontWeight = FontWeight.Light,\
                                            fontSize = 90.sp,\
                                            letterSpacing = 2.sp\
                                        ),\
                                        color = Color.White\
                                    )\
                                    Text(\
                                        text = amPmFormat.format(now).uppercase(),\
                                        style = MaterialTheme.typography.headlineMedium.copy(\
                                            fontWeight = FontWeight.Bold,\
                                            fontSize = 24.sp\
                                        ),\
                                        color = Color(0xFF00E5FF),\
                                        modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)\
                                    )\
                                }\
                                \
                                Spacer(modifier = Modifier.height(8.dp))\
                                \
                                Text(\
                                    text = if (label.isEmpty()) "Alarm" else label,\
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),\
                                    color = Color.White\
                                )\
                                \
                                Spacer(modifier = Modifier.height(8.dp))\
                                \
                                val dateStr = java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale("id", "ID")).format(now)\
                                Text(\
                                    text = dateStr,\
                                    style = MaterialTheme.typography.bodyLarge,\
                                    color = Color.White.copy(alpha = 0.8f)\
                                )\
                                \
                                if (aiGreeting.isNotEmpty()) {\
                                    Spacer(modifier = Modifier.height(16.dp))\
                                    Text(\
                                        text = aiGreeting,\
                                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),\
                                        color = Color(0xFFFFD700),\
                                        textAlign = TextAlign.Center,\
                                        modifier = Modifier.padding(horizontal = 24.dp)\
                                    )\
                                }\
                            }\
                            \
                            Row(\
                                modifier = Modifier.fillMaxWidth(),\
                                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)\
                            ) {\
                                // SNOOZE\
                                Box(\
                                    modifier = Modifier\
                                        .weight(1f)\
                                        .aspectRatio(0.85f)\
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))\
                                        .background(Color.White.copy(alpha = 0.1f))\
                                        .androidx.compose.foundation.border(1.dp, Color.White.copy(alpha = 0.3f), androidx.compose.foundation.shape.RoundedCornerShape(24.dp))\
                                        .androidx.compose.foundation.clickable { snoozeAndFinish(alarmId, label) },\
                                    contentAlignment = Alignment.Center\
                                ) {\
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {\
                                        Icon(Icons.Default.Snooze, contentDescription = "Snooze", tint = Color.White, modifier = Modifier.size(36.dp))\
                                        Spacer(Modifier.height(12.dp))\
                                        Text(com.example.ui.util.Translations.getString(appLanguage, "snooze").uppercase(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)\
                                        Spacer(Modifier.height(4.dp))\
                                        Text("10 MNT", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)\
                                    }\
                                }\
                                // DISMISS\
                                Box(\
                                    modifier = Modifier\
                                        .weight(1f)\
                                        .aspectRatio(0.85f)\
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))\
                                        .background(Brush.linearGradient(listOf(Color(0xFF1E3A5F).copy(alpha=0.6f), Color(0xFF00E5FF).copy(alpha=0.2f))))\
                                        .androidx.compose.foundation.border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), androidx.compose.foundation.shape.RoundedCornerShape(24.dp))\
                                        .androidx.compose.foundation.clickable { stopAndFinish(alarmId) },\
                                    contentAlignment = Alignment.Center\
                                ) {\
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {\
                                        Icon(Icons.Default.AlarmOff, contentDescription = "Dismiss", tint = Color(0xFF00E5FF), modifier = Modifier.size(36.dp))\
                                        Spacer(Modifier.height(12.dp))\
                                        Text(com.example.ui.util.Translations.getString(appLanguage, "dismiss").uppercase(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)\
                                    }\
                                }\
                            }\
                        }\
                    }\
' app/src/main/java/com/example/ui/screens/alarms/AlarmActivity.kt
