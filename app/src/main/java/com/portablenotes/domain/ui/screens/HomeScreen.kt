package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.AppColors
import com.portablenotes.domain.ui.calendar.WeekCalendar
import com.portablenotes.domain.ui.home.DayEvent
import com.portablenotes.domain.ui.home.TodayEventsPanel
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
    eventsFor: (LocalDate) -> List<com.portablenotes.domain.ui.home.DayEvent>,
    modifier: Modifier = Modifier,
    userName: String = "Cristian",
) {
    var selectedDate by remember { mutableStateOf(today) }
    val events = remember(selectedDate) { eventsFor(selectedDate) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(_root_ide_package_.com.portablenotes.domain.ui.AppColors.BackgroundTop, _root_ide_package_.com.portablenotes.domain.ui.AppColors.Background)))
    ) {
        val calendarHeight = maxHeight * 0.30f   // 👈 30 % de la pantalla
        val barSpace = 100.dp                    // espacio reservado para la barra flotante

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
        ) {
            Greeting(userName)
            Spacer(Modifier.height(16.dp))

            _root_ide_package_.com.portablenotes.domain.ui.calendar.WeekCalendar(
                selectedDate = selectedDate,
                today = today,
                eventsCount = events.size,
                onDateSelected = { selectedDate = it },
                modifier = Modifier.fillMaxWidth().height(calendarHeight),
            )

            Spacer(Modifier.height(20.dp))

            _root_ide_package_.com.portablenotes.domain.ui.home.TodayEventsPanel(
                events = events,
                bottomPadding = barSpace,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Greeting(name: String) {
    Column(Modifier.padding(horizontal = 4.dp)) {
        Text("Hola, $name", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = _root_ide_package_.com.portablenotes.domain.ui.AppColors.TextPrimary)
        Text("Esto es lo que tienes hoy", fontSize = 14.sp, color = _root_ide_package_.com.portablenotes.domain.ui.AppColors.TextSecondary)
    }
}