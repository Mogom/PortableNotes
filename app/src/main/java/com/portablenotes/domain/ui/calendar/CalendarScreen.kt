package com.portablenotes.domain.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portablenotes.shared.calendar.Screens.CalendarMonth
import com.portablenotes.domain.ui.theme.PortableNotesTheme
import kotlinx.datetime.LocalDate

private val CalendarBackground = Color(0xFFF8F8F4)
private val CalendarInk = Color(0xFF252B27)
private val CalendarMuted = Color(0xFF7C857F)
private val CalendarAccent = Color(0xFFB65C45)
private val CalendarToday = Color(0xFF176B58)
private val MonthNames = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)
private val WeekdayNames = listOf("L", "M", "X", "J", "V", "S", "D")

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    val today = remember { CalendarMonth.today() }
    var visibleMonth by remember { mutableStateOf(CalendarMonth.of(today.year, today.month.ordinal + 1)) }
    var selectedDate by remember { mutableStateOf(today) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CalendarBackground
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            MonthHeader(
                month = visibleMonth,
                onPrevious = { visibleMonth = visibleMonth.previous() },
                onNext = { visibleMonth = visibleMonth.next() }
            )
            CalendarGrid(
                month = visibleMonth,
                today = today,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
        }
    }
}
@Composable
private fun MonthHeader(
    month: CalendarMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${MonthNames[month.month - 1]} ${month.year}",
            color = CalendarInk,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonthButton(
                label = "Mes anterior",
                symbol = "‹",
                onClick = onPrevious
            )
            MonthButton(
                label = "Mes siguiente",
                symbol = "›",
                onClick = onNext
            )
        }
    }
}

@Composable
private fun MonthButton(label: String, symbol: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.semantics { contentDescription = label }) {
        Text(
            text = symbol,
            color = CalendarInk,
            fontSize = 32.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
private fun CalendarGrid(
    month: CalendarMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WeekdayNames.forEach { weekday ->
                Box(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = weekday,
                        color = CalendarMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        month.days.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    if (date == null) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        CalendarDay(
                            date = date,
                            isToday = date == today,
                            isSelected = date == selectedDate,
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            onClick = { onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = if (isSelected) CalendarAccent else Color.Transparent
    val textColor = if (isSelected) CalendarBackground else CalendarInk
    val dayLabel = "${date.day} de ${MonthNames[date.month.ordinal]} de ${date.year}"

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .then(
                if (isToday && !isSelected) border(1.dp,
                    CalendarToday, CircleShape)
                else Modifier
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = dayLabel
                selected = isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.day.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    PortableNotesTheme {
        CalendarScreen()
    }
}