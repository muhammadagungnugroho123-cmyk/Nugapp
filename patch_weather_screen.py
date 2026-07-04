with open('app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt', 'r') as f:
    lines = f.readlines()

out = []
for i, line in enumerate(lines):
    out.append(line)
    if "val weatherData by viewModel.weatherData.collectAsState()" in line:
        out.append("    val appContainer = (context.applicationContext as com.example.ClockApplication).container\n")
        out.append("    val settingsViewModel: com.example.ui.screens.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.ui.screens.settings.SettingsViewModel.provideFactory(appContainer.settingsRepository))\n")
        out.append("    val temperatureUnit by settingsViewModel.temperatureUnit.collectAsState()\n")
        out.append("    val formatTemp = { temp: Double -> if (temperatureUnit == 0) \"${temp.toInt()}°C\" else \"${(temp * 9/5 + 32).toInt()}°F\" }\n")
        out.append("    val formatTempNumOnly = { temp: Double -> if (temperatureUnit == 0) \"${temp.toInt()}\" else \"${(temp * 9/5 + 32).toInt()}\" }\n")
    if "\"${data.temp.toInt()}\"," in line:
        out[-1] = line.replace("\"${data.temp.toInt()}\",", "formatTempNumOnly(data.temp),")
    if "text = \"${forecast.temp.toInt()}°\"," in line:
        out[-1] = line.replace("\"${forecast.temp.toInt()}°\",", "formatTemp(forecast.temp),")

with open('app/src/main/java/com/example/ui/screens/weather/WeatherScreen.kt', 'w') as f:
    f.writelines(out)
