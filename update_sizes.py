with open('app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt', 'r') as f:
    content = f.read()
content = content.replace("textSize = 32f", "textSize = 28f")
content = content.replace("textSize = 28f", "textSize = 36f", 1) # Wait, it's risky
