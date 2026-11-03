package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.models.events.Event
import com.portablenotes.domain.models.events.daysWithEvents
import com.portablenotes.domain.models.events.hhmm
import com.portablenotes.domain.models.events.on
import com.portablenotes.domain.ui.components.AppFab
import com.portablenotes.domain.ui.components.Breakpoints
import com.portablenotes.domain.ui.components.barClearance
import com.portablenotes.domain.ui.components.contentBottomPadding
import com.portablenotes.domain.ui.components.screenInsets
import com.portablenotes.domain.ui.components.EmptyBox
import com.portablenotes.domain.ui.components.TitleText
import com.portablenotes.domain.ui.components.UiText
import com.portablenotes.domain.ui.components.WeekInitials
import com.portablenotes.domain.ui.components.dayTitle
import com.portablenotes.domain.ui.components.monthCells
import com.portablenotes.domain.ui.components.shortLabel
import com.portablenotes.domain.ui.components.styledCard
import com.portablenotes.domain.ui.components.subtitle
import com.portablenotes.domain.ui.theme.AppTheme
import com.portablenotes.domain.ui.theme.EstiloTarjeta
import java.time.LocalDate
import java.time.YearMonth

/**
 * Eventos 5a: selector de mes en pastilla, calendario en tarjeta (semana desde el lunes)
 * y los eventos del día seleccionado. El "+" crea un evento en ese día.
 */
@Composable
fun EventsScreen(
    events: List<Event>,
    today: LocalDate,
    month: YearMonth,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onChangeMonth: (YearMonth) -> Unit,
    onEventClick: (Event) -> Unit,
    onCreate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = AppTheme.tokens
    val dots = remember(month, events) { events.daysWithEvents(month) }
    val dayEvents = remember(selectedDate, events) { events.on(selectedDate) }

    val calendar: @Composable () -> Unit = {
        Column(
            Modifier.fillMaxWidth().styledCard(t).padding(start = 10.dp, end = 10.dp, top = 14.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(Modifier.fillMaxWidth()) {
                WeekInitials.forEach {
                    UiText(it, 12.sp, FontWeight.ExtraBold, t.muted, Modifier.weight(1f), style = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center))
                }
            }
            monthCells(month).chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { d ->
                        Box(Modifier.weight(1f).height(45.dp), contentAlignment = Alignment.Center) {
                            if (d != null) DayCell(d, today, selectedDate, d in dots) { onSelectDate(d) }
                        }
                    }
                }
            }
        }
    }
    val dayList: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TitleText(selectedDate.dayTitle(today), 19.sp, Modifier.padding(horizontal = 4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (dayEvents.isEmpty()) EmptyBox("Sin eventos", Modifier.clickable { onCreate(selectedDate) })
                dayEvents.forEachIndexed { i, e -> EventRow(e, alt = i % 2 == 1) { onEventClick(e) } }
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize().background(t.bg)) {
        val wide = maxWidth >= Breakpoints.Medium
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = Breakpoints.MaxContent)
                .fillMaxSize()
                .screenInsets()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (wide) 28.dp else 18.dp)
                .padding(top = 12.dp, bottom = contentBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                TitleText("Eventos", 28.sp, Modifier.weight(1f))
                MonthPill(month, { onChangeMonth(month.minusMonths(1)) }, { onChangeMonth(month.plusMonths(1)) })
            }
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Box(Modifier.weight(1.1f)) { calendar() }
                    Box(Modifier.weight(1f)) { dayList() }
                }
            } else {
                calendar()
                dayList()
            }
        }

        AppFab("Nuevo evento", { onCreate(selectedDate) }, Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = barClearance()))
    }
}

@Composable
private fun MonthPill(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    val t = AppTheme.tokens
    Row(
        Modifier
            .height(42.dp)
            .clip(CircleShape)
            .background(t.cardBg)
            .then(if (t.style.tarjeta == EstiloTarjeta.BORDE) Modifier.border(1.5.dp, t.border, CircleShape) else Modifier)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Chevron(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Mes anterior", onPrev)
        UiText(month.shortLabel(), 14.sp, FontWeight.ExtraBold)
        Chevron(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Mes siguiente", onNext)
    }
}

@Composable
private fun Chevron(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(28.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = AppTheme.tokens.text, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun DayCell(day: LocalDate, today: LocalDate, selected: LocalDate, hasEvents: Boolean, onClick: () -> Unit) {
    val t = AppTheme.tokens
    val on = day == selected
    val isToday = day == today
    Column(
        Modifier.clip(t.cellShape).clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(t.cellShape)
                .background(if (on) t.accent else Color.Transparent)
                .then(if (isToday && !on) Modifier.border(1.5.dp, t.accent, t.cellShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            UiText(
                day.dayOfMonth.toString(), 14.sp, FontWeight.Bold,
                when {
                    on -> t.onAccent
                    day.isBefore(today) -> t.muted
                    else -> t.text
                },
            )
        }
        Box(Modifier.size(4.dp).clip(CircleShape).background(if (hasEvents && !on) t.accent else Color.Transparent))
    }
}

@Composable
private fun EventRow(event: Event, alt: Boolean, onClick: () -> Unit) {
    val t = AppTheme.tokens
    Row(
        Modifier.fillMaxWidth().styledCard(t).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(width = 64.dp, height = 46.dp).clip(t.smallShape).background(if (alt) t.accent2 else t.accent),
            contentAlignment = Alignment.Center,
        ) {
            UiText(event.time.hhmm(), 15.sp, FontWeight.ExtraBold, if (alt) t.onAccent2 else t.onAccent)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            UiText(event.title, 15.sp, FontWeight.ExtraBold, maxLines = 1)
            UiText(event.subtitle, 13.sp, FontWeight.SemiBold, t.muted, maxLines = 1)
        }
    }
}
