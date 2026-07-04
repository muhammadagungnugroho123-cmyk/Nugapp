awk '/SwitchPreference\(/ && !done {
    print "                    SwitchPreference("
    print "                        title = \"Show 3D Globe\","
    print "                        subtitle = \"Show the interactive 3D globe in World Clock\","
    print "                        checked = showWorldClockGlobe,"
    print "                        onCheckedChange = { viewModel.setShowWorldClockGlobe(it) }"
    print "                    )"
    print "                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))"
    done = 1
}1' app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt
