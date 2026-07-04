awk '/var offset by remember/ {
    print $0
    print "            val stars = remember { "
    print "                List(60) { "
    print "                    Triple("
    print "                        Math.random().toFloat(), "
    print "                        Math.random().toFloat(),"
    print "                        (Math.random() * 2f + 0.5f).toFloat() "
    print "                    )"
    print "                } "
    print "            }"
    next
}
/contentAlignment = Alignment.Center/ {
    print $0
    print "            ) {"
    print "                Canvas(modifier = Modifier.fillMaxSize()) {"
    print "                    stars.forEach { (rx, ry, starRadius) ->"
    print "                        drawCircle("
    print "                            color = Color.White.copy(alpha = 0.4f),"
    print "                            radius = starRadius,"
    print "                            center = Offset(rx * size.width, ry * size.height)"
    print "                        )"
    print "                    }"
    print "                }"
    in_box = 1
    next
}
/translationY = offset.y/ {
    print "                            translationY = offset.y + bobbingOffset"
    next
}
/colors = listOf\(Color\(0xFF81D4FA\).copy\(alpha = 0.4f\), Color.Transparent\),/ {
    print "                            colors = listOf(Color(0xFF81D4FA).copy(alpha = atmospherePulse), Color.Transparent),"
    next
}
/radius = radius \* 1.2f/ {
    print "                            radius = radius * 1.25f"
    next
}
{ print }
' app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/screens/worldclock/WorldClockScreen.kt
