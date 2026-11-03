package com.portablenotes.domain.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.ui.graphics.vector.ImageVector

/** Pestañas de la barra inferior, en el orden del diseño. */
enum class AppTab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Inicio", Icons.Outlined.Home),
    Events("event", "Eventos", Icons.Outlined.CalendarMonth),
    Notes("notes", "Notas", Icons.Outlined.Description),
    Routines("routine", "Rutinas", Icons.Rounded.Repeat),
    Tasks("task", "Tareas", Icons.Outlined.CheckCircle),
    Themes("themes", "Temas", Icons.Outlined.Palette),
}

/** Pantallas que no están en la barra pero muestran una pestaña activa. */
object AppRoutes {
    /** Temas › Estilo (la barra marca "Temas"). */
    const val Style = "style"
}
