package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.alarms.AlarmsScreen
import com.example.ui.screens.clock.ClockScreen
import com.example.ui.screens.stopwatch.StopwatchScreen
import com.example.ui.screens.timers.TimersScreen
import com.example.ui.screens.worldclock.WorldClockScreen
import com.example.ui.screens.ai.AiAssistantScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val labelKey: String, val icon: ImageVector) {
    object Clock : Screen("clock", "clock", Icons.Default.Schedule)
    object Alarms : Screen("alarms", "alarms", Icons.Default.AccessAlarm)
    object Timers : Screen("timers", "timers", Icons.Default.HourglassEmpty)
    object Stopwatch : Screen("stopwatch", "stopwatch", Icons.Default.Timer)
    object WorldClock : Screen("world_clock", "world_clock", Icons.Default.Language)
    object AiAssistant : Screen("ai_assistant", "ai_assistant", Icons.Default.AutoAwesome)
    object Settings : Screen("settings", "settings", Icons.Default.Settings)
}

val items = listOf(
    Screen.Clock,
    Screen.Alarms,
    Screen.Timers,
    Screen.Stopwatch,
    Screen.WorldClock,
    Screen.AiAssistant
)

@Composable
fun AppNavigation(appLanguage: String = "en", clockStyleIndex: Int = 0) {
    
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main_pager"
    ) {
        composable("main_pager") {
            val pagerState = rememberPagerState(pageCount = { items.size })
            val coroutineScope = rememberCoroutineScope()
            
            // Shared ViewModels across pager screens
            val stopwatchViewModel: com.example.ui.screens.stopwatch.StopwatchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val timersViewModel: com.example.ui.screens.timers.TimersViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val alarmsViewModel: com.example.ui.screens.alarms.AlarmsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.ui.screens.alarms.AlarmsViewModel.Factory)

            val allAlarms by alarmsViewModel.alarms.collectAsState(initial = emptyList())
            val hasActiveAlarm = remember(allAlarms) { allAlarms.any { it.isEnabled && !it.label.startsWith("Snooze:") } }


            Scaffold(
                bottomBar = {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .shadow(
                                elevation = 16.dp, 
                                shape = RoundedCornerShape(32.dp), 
                                ambientColor = androidx.compose.material3.MaterialTheme.colorScheme.primary, 
                                spotColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                            )
                            .clip(RoundedCornerShape(32.dp))
                    ) {
                        NavigationBar(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            tonalElevation = 0.dp
                        ) {
                            items.forEachIndexed { index, screen ->
                                val label = com.example.ui.util.Translations.getString(appLanguage, screen.labelKey)
                                NavigationBarItem(
                                    icon = {
                                        androidx.compose.material3.BadgedBox(
                                            badge = {
                                                if (screen == Screen.Alarms && hasActiveAlarm) {
                                                    androidx.compose.material3.Badge(
                                                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(screen.icon, contentDescription = label)
                                        }
                                    },
                                    label = { Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                                    selected = pagerState.currentPage == index,
                                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                        indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                    beyondViewportPageCount = 1 // Keeps 1 adjacent tab in memory for lighter swiping without excessive memory
                ) { page ->
                    val navigateToSettings = { navController.navigate(Screen.Settings.route) }
                    when (page) {
                        0 -> ClockScreen(appLanguage = appLanguage, clockStyleIndex = clockStyleIndex, hasActiveAlarm = hasActiveAlarm, onNavigateToSettings = navigateToSettings, onNavigateToWeather = { navController.navigate("weather") })
                        1 -> AlarmsScreen(appLanguage = appLanguage, viewModel = alarmsViewModel, onNavigateToSettings = navigateToSettings)
                        2 -> TimersScreen(appLanguage = appLanguage, viewModel = timersViewModel, onNavigateToSettings = navigateToSettings)
                        3 -> StopwatchScreen(viewModel = stopwatchViewModel, onNavigateToSettings = navigateToSettings)
                        4 -> WorldClockScreen(appLanguage = appLanguage, onNavigateToSettings = navigateToSettings)
                        5 -> AiAssistantScreen(
                            appLanguage = appLanguage,
                            alarmsViewModel = alarmsViewModel,
                            timersViewModel = timersViewModel,
                            stopwatchViewModel = stopwatchViewModel
                        )
                    }
                }
            }
        }
        composable("weather") { com.example.ui.screens.weather.WeatherScreen(onNavigateBack = { navController.navigateUp() }, onNavigateToSettings = { navController.navigate(Screen.Settings.route) }) }
        composable(Screen.Settings.route) { com.example.ui.screens.settings.SettingsScreen(onNavigateBack = { navController.navigateUp() }) }
    }
}
