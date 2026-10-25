package com.portablenotes.domain.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppTab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Inicio", Icons.Rounded.Home),
    Events("event", "Eventos", Icons.Rounded.DateRange),
    Routines("routine", "Rutinas", Icons.Rounded.Refresh),
    Tasks("task", "Tareas", Icons.Rounded.CheckCircle),
}