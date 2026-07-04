with open('app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt', 'r') as f:
    lines = f.readlines()

out = []
skip = False
for line in lines:
    if "fun WorldClockScreen(" in line:
        out.append(line)
        continue
    if "appLanguage: String =" in line:
        out.append(line)
        continue
    if "viewModel: WorldClockViewModel = viewModel()," in line:
        # we drop it from parameters
        continue
    if "onNavigateToSettings: () -> Unit =" in line:
        out.append(line)
        continue
    
    if "val cities by viewModel.cities.collectAsState()" in line:
        out.append("    val context = androidx.compose.ui.platform.LocalContext.current\n")
        out.append("    val appContainer = (context.applicationContext as com.example.ClockApplication).container\n")
        out.append("    val viewModel: WorldClockViewModel = viewModel(factory = WorldClockViewModel.provideFactory(appContainer.settingsRepository))\n")
        out.append(line)
        continue
        
    if "val context = androidx.compose.ui.platform.LocalContext.current" in line and not skip:
        # we already added it above, skip the original
        skip = True
        continue
    if "val appContainer = (context.applicationContext as com.example.ClockApplication).container" in line and skip:
        continue
        
    out.append(line)

with open('app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt', 'w') as f:
    f.writelines(out)
