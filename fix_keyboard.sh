sed -i '32i import androidx.compose.ui.platform.LocalSoftwareKeyboardController' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
sed -i '/val coroutineScope = rememberCoroutineScope()/a\
    val keyboardController = LocalSoftwareKeyboardController.current' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
sed -i 's/keyboardActions = KeyboardActions(onSearch = { addCity(searchQuery) }),/keyboardActions = KeyboardActions(onSearch = { addCity(searchQuery); keyboardController?.hide() }),/g' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
sed -i 's/onClick = { addCity(searchQuery) },/onClick = { addCity(searchQuery); keyboardController?.hide() },/g' app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt
