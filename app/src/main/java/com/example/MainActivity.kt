package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    setContent {
      val appContainer = (application as ClockApplication).container
      val themeColorIndex by appContainer.settingsRepository.themeColorIndex.collectAsState(initial = 0)
      val appLanguage by appContainer.settingsRepository.appLanguage.collectAsState(initial = "en")
      val clockStyleIndex by appContainer.settingsRepository.clockStyleIndex.collectAsState(initial = 0)
      
      MyApplicationTheme(themeColorIndex = themeColorIndex) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          var showSplash by remember { mutableStateOf(true) }
          val permissionsLauncher = rememberLauncherForActivityResult(
              ActivityResultContracts.RequestMultiplePermissions()
          ) { _ -> }

          LaunchedEffect(Unit) {
              val permissionsToRequest = mutableListOf<String>()
              
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                  permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
              }
              
              permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
              permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
              
              
              val ungrantedPermissions = permissionsToRequest.filter {
                  ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
              }
              
              if (ungrantedPermissions.isNotEmpty()) {
                  permissionsLauncher.launch(ungrantedPermissions.toTypedArray())
              }
              
              delay(1000) // 1 second
              showSplash = false
          }

          if (showSplash) {
            val infiniteTransition = rememberInfiniteTransition()
            val angle by infiniteTransition.animateFloat(
              initialValue = 0f,
              targetValue = 360f,
              animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
              )
            )
            val scale by infiniteTransition.animateFloat(
              initialValue = 0.8f,
              targetValue = 1.2f,
              animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
              )
            )

            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.Schedule,
                  contentDescription = "Splash Icon",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        rotationZ = angle
                        scaleX = scale
                        scaleY = scale
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                  text = "CHRONOS",
                  style = MaterialTheme.typography.headlineMedium,
                  color = MaterialTheme.colorScheme.primary,
                  textAlign = TextAlign.Center
                )
              }
            }
          } else {
            Box(modifier = Modifier.fillMaxSize()) {
              com.example.ui.components.AnimatedGradientBackground()
              AppNavigation(appLanguage = appLanguage, clockStyleIndex = clockStyleIndex)
            }
          }
        }
      }
    }
  }
}

