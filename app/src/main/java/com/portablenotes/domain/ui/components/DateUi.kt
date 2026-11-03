package com.portablenotes.domain.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.portablenotes.domain.models.events.Event
import com.portablenotes.domain.models.events.Repeat
import com.portablenotes.domain.ui.theme.AppTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// ---------- Fechas en español ----------

val Es: Locale = Locale.forLanguageTag("es-CO")

private fun String.cap() = replaceFirstChar { it.titlecase(Es) }

/** "Martes 3 de noviembre" */
fun LocalDate.longLabel(): String =
    "${dayOfWeek.getDisplayName(TextStyle.FULL, Es).cap()} $dayOfMonth de ${month.getDisplayName(TextStyle.FULL, Es).lowercase(Es)}"

/** "Hoy · Martes 3" o "Jueves 5" */
fun LocalDate.dayTitle(today: LocalDate): String {
    val base = "${dayOfWeek.getDisplayName(TextStyle.FULL, Es).cap()} $dayOfMonth"
    return if (this == today) "Hoy · $base" else base
}

/** "Noviembre" */
fun YearMonth.monthLabel(): String = month.getDisplayName(TextStyle.FULL_STANDALONE, Es).cap()

/** "Nov 2026" */
fun YearMonth.shortLabel(): String = "${month.getDisplayName(TextStyle.SHORT, Es).trimEnd('.').cap()} $year"

/** "Hoy", "Ayer", "1 nov" */
fun LocalDate.relativeLabel(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "Hoy"
    today.minusDays(1) -> "Ayer"
    else -> "$dayOfMonth ${month.getDisplayName(TextStyle.SHORT, Es).trimEnd('.').lowercase(Es)}"
}

/** L M M J V S D (la semana empieza el lunes). */
val WeekInitials = listOf("L", "M", "M", "J", "V", "S", "D")

fun weekOf(date: LocalDate): List<LocalDate> {
    val monday = date.minusDays((date.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
    return (0L..6L).map { monday.plusDays(it) }
}

/** Celdas del mes empezando en lunes; null = hueco. */
fun monthCells(month: YearMonth): List<LocalDate?> {
    val lead = month.atDay(1).dayOfWeek.value - 1
    val cells: List<LocalDate?> = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    return cells + List((7 - cells.size % 7) % 7) { null }
}

/** Segunda línea de un evento: la repetición, o la categoría si no se repite. */
val Event.subtitle: String get() = if (repeat == Repeat.None) type.label else repeat.label

// ---------- Estado compartido Inicio / Eventos ----------

/** Día seleccionado y mes visible, compartidos entre Inicio y Eventos. */
class CalendarViewModel : ViewModel() {
    val today: LocalDate = LocalDate.now()

    var selectedDate by mutableStateOf(today)
        private set
    var visibleMonth by mutableStateOf(YearMonth.from(today))
        private set

    fun select(date: LocalDate) {
        selectedDate = date
        visibleMonth = YearMonth.from(date)
    }

    fun showMonth(month: YearMonth) {
        visibleMonth = month
        selectedDate = if (month == YearMonth.from(today)) today else month.atDay(1)
    }
}

// ---------- Piezas ----------

/** Borde punteado (para "Día libre" / "Sin eventos"). */
fun Modifier.dashedBorder(color: Color, radius: Dp, width: Dp = 1.5.dp): Modifier = drawBehind {
    val w = width.toPx()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
        topLeft = androidx.compose.ui.geometry.Offset(w / 2, w / 2),
        size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w),
    )
}

/** Caja punteada con texto centrado. */
@Composable
fun EmptyBox(text: String, modifier: Modifier = Modifier) {
    val t = AppTheme.tokens
    Box(
        modifier.fillMaxWidth().dashedBorder(t.border, t.rCard).padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        UiText(text, 14.sp, FontWeight.SemiBold, t.muted)
    }
}
