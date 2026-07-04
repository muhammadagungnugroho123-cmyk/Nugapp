sed -i '/var searchQuery by remember/a\
    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
