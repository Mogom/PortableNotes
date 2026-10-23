package com.example.portablenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.portablenotes.ui.AppColors
import com.example.portablenotes.ui.events.EventsState
import com.example.portablenotes.ui.events.Priority
import com.example.portablenotes.ui.events.hhmm
import com.example.portablenotes.ui.events.sampleEvents
import com.example.portablenotes.ui.home.DayEvent
import com.example.portablenotes.ui.navigation.AppBottomBar
import com.example.portablenotes.ui.navigation.AppTab
import com.example.portablenotes.ui.screens.EventsScreen
import com.example.portablenotes.ui.screens.HomeScreen
import com.example.portablenotes.ui.screens.TasksScreen
import com.example.portablenotes.ui.tasks.TasksState
import com.example.portablenotes.ui.tasks.sampleTasks
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent { App() }
    }
}

@Preview
@Composable
fun App() {
    val today = remember { LocalDate.now() }
    val eventsState = remember { EventsState(sampleEvents(today)) }   // compartido entre pantallas
    var tab by remember { mutableStateOf(AppTab.Home) }
    val navController = rememberNavController()
    val tasksState = remember { TasksState(sampleTasks()) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedTab = AppTab.entries.find { it.route == currentRoute } ?: AppTab.Home

    MaterialTheme {
        Scaffold(
            bottomBar = {
                AppBottomBar(
                    selected = selectedTab,
                    onSelect = { tab ->
                        if (tab.route != currentRoute) {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppTab.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(
                    AppTab.Home.route,
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) }
                ) {
                    HomeScreen(
                        today = today,
                        eventsFor = { date ->
                            eventsState.occurrencesOn(date).map {
                                DayEvent(
                                    id = it.id,
                                    title = it.title,
                                    time = it.time.hhmm(),
                                    place = it.type.label,
                                    tint = when (it.priority) {
                                        Priority.High -> AppColors.Tint2
                                        Priority.Medium -> AppColors.Tint1
                                        Priority.Low -> AppColors.Tint3
                                    },
                                )
                            }
                        },
                    )
                }

                composable(
                    AppTab.Events.route,
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) }
                ) {
                    EventsScreen(state = eventsState, today = today)
                }

                composable(
                    AppTab.Routines.route,
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) }
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(AppColors.Background),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Rutinas (próximamente)", color = AppColors.TextSecondary)
                    }
                }
                composable(
                    AppTab.Tasks.route,
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) }
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(AppColors.Background),
                        contentAlignment = Alignment.Center,
                    ) {
                        TasksScreen(tasksState)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AppPreview() = App()