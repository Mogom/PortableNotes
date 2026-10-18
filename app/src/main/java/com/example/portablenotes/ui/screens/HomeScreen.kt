package com.example.portablenotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portablenotes.ui.AppColors
import com.example.portablenotes.ui.navigation.AppTab
import com.example.portablenotes.ui.navigation.AppBottomBar
import com.example.portablenotes.ui.calendar.WeekCalendar
import com.example.portablenotes.ui.home.DayEvent
import com.example.portablenotes.ui.home.TodayEventsPanel
import java.time.LocalDate

/**
 * Pantalla principal.
 * - Calendario: 30 % de la altura disponible.
 * - Debajo: panel de eventos del día (scroll).
 * - Barra de navegación: componente aparte, flotando abajo.
 */
@Composable
fun HomeScreen(
    today: LocalDate,
    eventsFor: (LocalDate) -> List<DayEvent>,
    modifier: Modifier = Modifier,
    userName: String = "Cristian",
) {
    var selectedDate by remember { mutableStateOf(today) }
    val events = remember(selectedDate) { eventsFor(selectedDate) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.BackgroundTop, AppColors.Background)))
    ) {
        val calendarHeight = maxHeight * 0.30f   // 👈 30 % de la pantalla
        val barSpace = 104.dp                    // espacio reservado para la barra flotante

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
        ) {
            Greeting(userName)
            Spacer(Modifier.height(16.dp))

            WeekCalendar(
                selectedDate = selectedDate,
                today = today,
                eventsCount = events.size,
                onDateSelected = { selectedDate = it },
                modifier = Modifier.fillMaxWidth().height(calendarHeight),
            )

            Spacer(Modifier.height(20.dp))

            TodayEventsPanel(
                events = events,
                bottomPadding = barSpace,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Greeting(name: String) {
    Column(Modifier.padding(horizontal = 4.dp)) {
        Text("Hola, $name", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
        Text("Esto es lo que tienes hoy", fontSize = 14.sp, color = AppColors.TextSecondary)
    }
}