package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Blue Theme
private val BlueDarkColorScheme = darkColorScheme(primary = Color(0xFF00E5FF), secondary = Color(0xFF2979FF), tertiary = Color(0xFF651FFF), background = Color(0xFF0B0F19), surface = Color(0xFF131A2A))
private val BlueLightColorScheme = lightColorScheme(primary = Color(0xFF2979FF), secondary = Color(0xFF2962FF), tertiary = Color(0xFF304FFE), background = Color(0xFFF5F7FA), surface = Color(0xFFFFFFFF))

// Green Theme
private val GreenDarkColorScheme = darkColorScheme(primary = Color(0xFF00E676), secondary = Color(0xFF00BFA5), tertiary = Color(0xFF1DE9B6), background = Color(0xFF0A1410), surface = Color(0xFF11211A))
private val GreenLightColorScheme = lightColorScheme(primary = Color(0xFF00C853), secondary = Color(0xFF00BFA5), tertiary = Color(0xFF00B8D4), background = Color(0xFFF1F8F6), surface = Color(0xFFFFFFFF))

// Orange Theme
private val OrangeDarkColorScheme = darkColorScheme(primary = Color(0xFFFF9100), secondary = Color(0xFFFF3D00), tertiary = Color(0xFFFFC400), background = Color(0xFF1A120A), surface = Color(0xFF2B1D11))
private val OrangeLightColorScheme = lightColorScheme(primary = Color(0xFFFF6D00), secondary = Color(0xFFDD2C00), tertiary = Color(0xFFFFAB00), background = Color(0xFFFFF8F0), surface = Color(0xFFFFFFFF))

// Purple Theme
private val PurpleCustomDarkColorScheme = darkColorScheme(primary = Color(0xFFD500F9), secondary = Color(0xFF651FFF), tertiary = Color(0xFFF50057), background = Color(0xFF140A1A), surface = Color(0xFF22112B))
private val PurpleCustomLightColorScheme = lightColorScheme(primary = Color(0xFFAA00FF), secondary = Color(0xFF6200EA), tertiary = Color(0xFFC51162), background = Color(0xFFFAF5FC), surface = Color(0xFFFFFFFF))

@Composable
fun MyApplicationTheme(
  themeColorIndex: Int = 0,
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeColorIndex) {
      1 -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
      2 -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
      3 -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
      4 -> if (darkTheme) PurpleCustomDarkColorScheme else PurpleCustomLightColorScheme
      else -> {
        // Dynamic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        }
      }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
