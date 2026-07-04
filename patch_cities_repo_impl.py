with open('app/src/main/java/com/example/data/repository/SettingsRepositoryImpl.kt', 'r') as f:
    lines = f.readlines()

out = []
for i, line in enumerate(lines):
    out.append(line)
    if "import androidx.datastore.preferences.core.stringPreferencesKey" in line:
        out.append("import androidx.datastore.preferences.core.stringSetPreferencesKey\n")
    if "val TEMPERATURE_UNIT = intPreferencesKey(\"temperature_unit\")" in line:
        out.append("        val SAVED_CITIES = stringSetPreferencesKey(\"saved_cities\")\n")
    if "override val temperatureUnit: Flow<Int>" in line:
        out.insert(-1, """
    override val savedCities: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SAVED_CITIES] ?: setOf("London", "New York", "Tokyo", "Sydney")
    }
""")
    if "override suspend fun setTemperatureUnit(unit: Int)" in line:
        out.insert(-1, """
    override suspend fun setSavedCities(cities: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_CITIES] = cities
        }
    }
""")

with open('app/src/main/java/com/example/data/repository/SettingsRepositoryImpl.kt', 'w') as f:
    f.writelines(out)
