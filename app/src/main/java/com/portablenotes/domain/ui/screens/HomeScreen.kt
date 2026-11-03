package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.AppColors
import com.portablenotes.domain.ui.calendar.WeekCalendar
import com.portablenotes.domain.ui.events.CircleIconButton
import com.portablenotes.domain.ui.home.DayEvent
import com.portablenotes.domain.ui.home.HomeMetrics
import com.portablenotes.domain.ui.home.MetricsBubble
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
    userName: String = "",
    onOpenSettings: () -> Unit = {},
    metrics: HomeMetrics = HomeMetrics(),
) {
    var selectedDate by remember { mutableStateOf(today) }
    val events = remember(selectedDate) { eventsFor(selectedDate) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(com.portablenotes.domain.ui.AppColors.BackgroundTop, com.portablenotes.domain.ui.AppColors.Background)))
    ) {
        val wide = maxWidth >= 720.dp            // tablet o teléfono en horizontal
        val calendarHeight = maxHeight * 0.30f   // 👈 30 % de la pantalla
        val barSpace = 100.dp                    // espacio reservado para la barra flotante

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { Greeting(userName) }
                CircleIconButton(Icons.Rounded.Settings, "Ajustes", onOpenSettings)
            }
            Spacer(Modifier.height(16.dp))

            if (wide) {
                // Tablet / horizontal: calendario y métricas a la izquierda, eventos del día a la derecha
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(
                        Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    ) {
                        com.portablenotes.domain.ui.calendar.WeekCalendar(
                            selectedDate = selectedDate,
                            today = today,
                            eventsCount = events.size,
                            onDateSelected = { selectedDate = it },
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        MetricsBubble(metrics)
                        Spacer(Modifier.height(12.dp))
                    }
                    com.portablenotes.domain.ui.home.TodayEventsPanel(
                        events = events,
                        bottomPadding = 12.dp,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            } else {
                com.portablenotes.domain.ui.calendar.WeekCalendar(
                    selectedDate = selectedDate,
                    today = today,
                    eventsCount = events.size,
                    onDateSelected = { selectedDate = it },
                    modifier = Modifier.fillMaxWidth().height(calendarHeight),
                )

                Spacer(Modifier.height(12.dp))

                MetricsBubble(metrics)

                Spacer(Modifier.height(16.dp))

                com.portablenotes.domain.ui.home.TodayEventsPanel(
                    events = events,
                    bottomPadding = barSpace,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Greeting(name: String) {
    Column(Modifier.padding(horizontal = 4.dp)) {
        Text(if (name.isBlank()) "Hola" else "Hola, $name", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = com.portablenotes.domain.ui.AppColors.TextPrimary)
        Text("Esto es lo que tienes hoy", fontSize = 14.sp, color = com.portablenotes.domain.ui.AppColors.TextSecondary)
    }
}