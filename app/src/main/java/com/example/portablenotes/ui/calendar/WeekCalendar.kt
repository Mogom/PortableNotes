package com.example.portablenotes.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portablenotes.ui.AppColors
import java.time.LocalDate

private val monthNames = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)
private val weekdayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

private fun LocalDate.startOfWeek(): LocalDate =
    minusDays((dayOfWeek.value - 1).toLong()) // lunes = 1

/**
 * Calendario semanal compacto. La altura la define quien lo usa
 * (en HomeScreen: 30 % de la pantalla); el contenido se reparte solo.
 */
@Composable
fun WeekCalendar(
    selectedDate: LocalDate,
    today: LocalDate,
    eventsCount: Int,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekStart = selectedDate.startOfWeek()
    val days = List(7) { weekStart.plusDays(it.toLong()) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = AppColors.Surface,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, AppColors.Border),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Cabecera: mes / año + flechas de semana
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(start = 6.dp)) {
                    Text(
                        text = monthNames[selectedDate.monthValue - 1],
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary,
                    )
                    Text(
                        text = selectedDate.year.toString(),
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary,
                    )
                }
                ArrowButton(left = true) { onDateSelected(selectedDate.minusDays(7)) }
                Spacer(Modifier.width(8.dp))
                ArrowButton(left = false) { onDateSelected(selectedDate.plusDays(7)) }
            }

            // Días de la semana
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                days.forEach { date ->
                    DayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }

            Text(
                text = if (eventsCount == 0) "Sin eventos para este día"
                else "$eventsCount ${if (eventsCount == 1) "evento" else "eventos"} para este día",
                fontSize = 12.sp,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) AppColors.Accent else AppColors.SurfaceMuted.copy(alpha = 0.6f))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = weekdayNames[date.dayOfWeek.value - 1],
            fontSize = 11.sp,
            color = if (isSelected) AppColors.OnAccent.copy(alpha = 0.7f) else AppColors.TextSecondary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) AppColors.OnAccent else AppColors.TextPrimary,
        )
        Spacer(Modifier.height(6.dp))
        // Punto indicador de "hoy"
        Box(
            Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !isToday -> androidx.compose.ui.graphics.Color.Transparent
                        isSelected -> AppColors.OnAccent
                        else -> AppColors.Accent
                    }
                )
        )
    }
}

@Composable
private fun ArrowButton(left: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(AppColors.SurfaceMuted)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (left) Icons.Rounded.KeyboardArrowLeft else Icons.Rounded.KeyboardArrowRight,
            contentDescription = if (left) "Semana anterior" else "Semana siguiente",
            tint = AppColors.TextPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}