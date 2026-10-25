package com.portablenotes.domain.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.AppColors
import com.portablenotes.domain.ui.calendar.events.CircleIconButton
import com.portablenotes.domain.ui.calendar.events.monthName
import java.time.LocalDate
import java.time.YearMonth

private val weekdayInitials = listOf("L", "M", "M", "J", "V", "S", "D")

/**
 * Calendario mensual. Siempre dibuja 6 filas para que la altura sea estable;
 * las celdas se reparten el espacio que defina quien lo usa (50 % en EventsScreen).
 */
@Composable
fun MonthCalendar(
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    daysWithEvents: Set<LocalDate>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val offset = month.atDay(1).dayOfWeek.value - 1   // lunes = 0
    val length = month.lengthOfMonth()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = AppColors.Surface,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, AppColors.Border),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 14.dp)) {
            // Cabecera
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(start = 6.dp)) {
                    Text(month.monthName(), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                    Text(month.year.toString(), fontSize = 13.sp, color = AppColors.TextSecondary)
                }
                CircleIconButton(
                    Icons.Rounded.KeyboardArrowLeft,
                    "Mes anterior",
                    onPrevMonth
                )
                Spacer(Modifier.width(8.dp))
                CircleIconButton(
                    Icons.Rounded.KeyboardArrowRight,
                    "Mes siguiente",
                    onNextMonth
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth()) {
                weekdayInitials.forEach {
                    Text(
                        it,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Cuadrícula 6 x 7
            Column(Modifier.weight(1f).fillMaxWidth()) {
                repeat(6) { row ->
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        repeat(7) { col ->
                            val day = row * 7 + col - offset + 1
                            if (day in 1..length) {
                                val date = month.atDay(day)
                                DayCell(
                                    day = day,
                                    isSelected = date == selected,
                                    isToday = date == today,
                                    hasEvent = date in daysWithEvents,
                                    onClick = { onDayClick(date) },
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(2.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.92f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(if (isSelected) AppColors.Accent else Color.Transparent)
                .then(if (isToday && !isSelected) Modifier.border(1.dp, AppColors.Accent, CircleShape) else Modifier)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                day.toString(),
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) AppColors.OnAccent else AppColors.TextPrimary,
            )
            if (hasEvent) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 3.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AppColors.OnAccent else AppColors.Accent)
                )
            }
        }
    }
}