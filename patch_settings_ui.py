import sys

with open('app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt', 'r') as f:
    lines = f.readlines()

out = []
for line in lines:
    out.append(line)
    if "val worldClockGlobeStyle by" in line:
        out.append("    val temperatureUnit by viewModel.temperatureUnit.collectAsState()\n")
    if "SettingsSectionCard(com.example.ui.util.Translations.getString(appLanguage, \"alarms_timers\")) {" in line:
        out.insert(-1, """
            item {
                SettingsSectionCard("Weather") {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(text = "Temperature Unit", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = temperatureUnit == 0,
                                onClick = { viewModel.setTemperatureUnit(0) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("Celsius (°C)") }
                            SegmentedButton(
                                selected = temperatureUnit == 1,
                                onClick = { viewModel.setTemperatureUnit(1) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("Fahrenheit (°F)") }
                        }
                    }
                }
            }
""")

with open('app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt', 'w') as f:
    for line in out:
        f.write(line)
