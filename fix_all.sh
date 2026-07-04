sed -i '233s/currentTime/currentTimeState.value/' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
sed -i '247s/currentTime/currentTimeState.value/' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
sed -i '256i \    val currentTime = currentTimeProvider()' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
sed -i '259s/currentTimeProvider()/currentTime/' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
