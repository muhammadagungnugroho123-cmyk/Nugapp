sed -i '42i import androidx.compose.animation.AnimatedVisibility\
import androidx.compose.animation.fadeIn\
import androidx.compose.animation.fadeOut\
import androidx.compose.animation.expandVertically\
import androidx.compose.animation.shrinkVertically' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt

sed -i '/if (errorMessage != null) {/c\
            AnimatedVisibility(\
                visible = errorMessage != null,\
                enter = fadeIn() + expandVertically(),\
                exit = fadeOut() + shrinkVertically()\
            ) {\
                Column {\
                    Spacer(modifier = Modifier.height(8.dp))\
                    Text(errorMessage ?: "", color = Color.Red, style = MaterialTheme.typography.bodyMedium)\
                }\
            }' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt

sed -i '/Spacer(modifier = Modifier.height(8.dp))/d' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
sed -i '/Text(errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodyMedium)/d' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
