with open('app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Fix SettingsScreen
content = content.replace("""            item {
            item {
                SettingsSectionCard("Weather") {""", """            item {
                SettingsSectionCard("Weather") {""")
content = content.replace("""                }
            }
                SettingsSectionCard(com.example.ui.util.Translations.getString(appLanguage, "alarms_timers")) {""", """                }
            }
            item {
                SettingsSectionCard(com.example.ui.util.Translations.getString(appLanguage, "alarms_timers")) {""")

with open('app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt', 'w') as f:
    f.write(content)

# Fix SettingsRepositoryImpl
with open('app/src/main/java/com/example/data/repository/SettingsRepositoryImpl.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "override val showWorldClockGlobe:" in line:
        new_lines.append(line)
        new_lines.append("        preferences[PreferencesKeys.SHOW_WORLD_CLOCK_GLOBE] ?: true\n")
        new_lines.append("    }\n")
        new_lines.append("    override val worldClockGlobeStyle: Flow<Int> = context.dataStore.data.map { preferences ->\n")
        new_lines.append("        preferences[PreferencesKeys.WORLD_CLOCK_GLOBE_STYLE] ?: 1\n")
        new_lines.append("    }\n")
        skip = True
        continue
    if skip and "override val savedCities:" in line:
        skip = False
        # fall through
    if skip and "override val temperatureUnit:" in line:
        continue
    if skip:
        continue
    new_lines.append(line)

content = "".join(new_lines)
# Wait, I also need to make sure the set* methods are correct
content = content.replace("""    override suspend fun setShowWorldClockGlobe(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_WORLD_CLOCK_GLOBE] = enabled
        }
    }
    override suspend fun setWorldClockGlobeStyle(style: Int) {
    override suspend fun setSavedCities(cities: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_CITIES] = cities
        }
    }
    override suspend fun setTemperatureUnit(unit: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE_UNIT] = unit
        }
    }
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORLD_CLOCK_GLOBE_STYLE] = style
        }
    }""", """    override suspend fun setShowWorldClockGlobe(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_WORLD_CLOCK_GLOBE] = enabled
        }
    }
    override suspend fun setWorldClockGlobeStyle(style: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORLD_CLOCK_GLOBE_STYLE] = style
        }
    }
    override suspend fun setSavedCities(cities: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_CITIES] = cities
        }
    }
    override suspend fun setTemperatureUnit(unit: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE_UNIT] = unit
        }
    }""")

with open('app/src/main/java/com/example/data/repository/SettingsRepositoryImpl.kt', 'w') as f:
    f.write(content)
