package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.models.events.Event
import com.portablenotes.domain.models.events.hhmm
import com.portablenotes.domain.ui.components.AppFab
import com.portablenotes.domain.ui.components.Breakpoints
import com.portablenotes.domain.ui.components.barClearance
import com.portablenotes.domain.ui.components.contentBottomPadding
import com.portablenotes.domain.ui.components.screenInsets
import com.portablenotes.domain.ui.components.EmptyBox
import com.portablenotes.domain.ui.components.PillChip
import com.portablenotes.domain.ui.components.ProgressBar
import com.portablenotes.domain.ui.components.TitleText
import com.portablenotes.domain.ui.components.UiText
import com.portablenotes.domain.ui.components.WeekInitials
import com.portablenotes.domain.ui.components.dayTitle
import com.portablenotes.domain.ui.components.longLabel
import com.portablenotes.domain.ui.components.monthLabel
import com.portablenotes.domain.ui.components.styledCard
import com.portablenotes.domain.ui.components.subtitle
import com.portablenotes.domain.ui.components.weekOf
import com.portablenotes.domain.ui.events.icon
import com.portablenotes.domain.ui.theme.AppTheme
import com.portablenotes.domain.ui.theme.EncabezadoInicio
import java.time.LocalDate
import java.time.YearMonth

/** Resumen de las tres mini-tarjetas de Inicio. */
data class HomeSummary(
    val tasksDone: Int = 0,
    val tasksTotal: Int = 0,
    val routinesDone: Int = 0,
    val routinesTotal: Int = 0,
    val notesCount: Int = 0,
)

/**
 * Inicio 5a: saludo + avatar, encabezado (bloque de color o tarjeta) con la semana,
 * eventos del día seleccionado, mini-tarjetas de Tareas / Rutinas / Notas y FAB.
 */
@Composable
fun HomeScreen(
    today: LocalDate,
    userName: String,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    eventsOn: (LocalDate) -> List<Event>,
    summary: HomeSummary,
    onEventClick: (Event) -> Unit,
    onOpenMonth: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenRoutines: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenAccount: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = AppTheme.tokens
    val dayEvents = eventsOn(selectedDate)
    val hasEvents: (LocalDate) -> Boolean = { eventsOn(it).isNotEmpty() }

    // Piezas de la pantalla; en tablet se reparten en dos columnas
    val greeting: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                UiText(today.longLabel(), 14.sp, FontWeight.SemiBold, t.muted)
                TitleText(if (userName.isBlank()) "Hola" else "Hola, $userName", 28.sp, maxLines = 1)
            }
            Box(
                Modifier.size(46.dp).clip(CircleShape).background(t.accent2)
                    .clickable(role = Role.Button, onClick = onOpenAccount),
                contentAlignment = Alignment.Center,
            ) {
                UiText(userName.firstOrNull()?.uppercase() ?: "·", 17.sp, FontWeight.ExtraBold, t.onAccent2)
            }
        }
    }
    val header: @Composable () -> Unit = {
        if (t.style.encabezado == EncabezadoInicio.BLOQUE_COLOR) {
            ColorHeader(today, selectedDate, onSelectDate, hasEvents, onOpenMonth)
        } else {
            CardHeader(today, selectedDate, onSelectDate, hasEvents)
        }
    }
    val dayList: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                TitleText(selectedDate.dayTitle(today), 19.sp, Modifier.weight(1f))
                Box(
                    Modifier.height(28.dp).widthIn(min = 28.dp).clip(CircleShape).background(t.accent).padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    UiText(dayEvents.size.toString(), 13.sp, FontWeight.ExtraBold, t.onAccent)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (dayEvents.isEmpty()) EmptyBox("Día libre")
                dayEvents.forEachIndexed { i, e -> HomeEventCard(e, alt = i % 2 == 1) { onEventClick(e) } }
            }
        }
    }
    val miniCards: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniCard("Tareas", "${summary.tasksDone}/${summary.tasksTotal}", fraction(summary.tasksDone, summary.tasksTotal), t.accent, onOpenTasks)
            MiniCard("Rutinas", "${summary.routinesDone}/${summary.routinesTotal}", fraction(summary.routinesDone, summary.routinesTotal), t.accent2, onOpenRoutines)
            Column(
                Modifier.weight(1f).fillMaxHeight().clip(t.cardShape).background(t.accent2).clickable(onClick = onOpenNotes).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                UiText("Notas", 12.sp, FontWeight.ExtraBold, t.onAccent2)
                TitleText(summary.notesCount.toString(), 22.sp, color = t.onAccent2)
                UiText("Ver todas", 12.sp, FontWeight.ExtraBold, t.onAccent2)
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
                .verticalScroll(rememberScrollState())
                .screenInsets()
                .padding(horizontal = if (wide) 28.dp else 18.dp)
                .padding(top = 12.dp, bottom = contentBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            greeting()
            if (wide) {
                // Tablet / horizontal: semana y resumen a la izquierda, eventos del día a la derecha
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        header()
                        miniCards()
                    }
                    Box(Modifier.weight(1f)) { dayList() }
                }
            } else {
                header()
                dayList()
                miniCards()
            }
        }

        AppFab("Crear", onCreate, Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = barClearance()))
    }
}

private fun fraction(done: Int, total: Int) = if (total == 0) 0f else done.toFloat() / total

@Composable
private fun ColorHeader(
    today: LocalDate,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    hasEvents: (LocalDate) -> Boolean,
    onOpenMonth: () -> Unit,
) {
    val t = AppTheme.tokens
    Column(
        Modifier.fillMaxWidth().clip(t.cardShape).background(t.accent).padding(horizontal = 14.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TitleText(YearMonth.from(selected).monthLabel(), 18.sp, Modifier.weight(1f), color = t.onAccent)
            Box(Modifier.clip(CircleShape).background(t.onSoft).clickable(onClick = onOpenMonth).padding(horizontal = 12.dp, vertical = 6.dp)) {
                UiText("Ver mes", 13.sp, FontWeight.ExtraBold, t.onAccent)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekOf(selected).forEachIndexed { i, d ->
                val on = d == selected
                Column(
                    Modifier.width(44.dp).clip(t.smallShape).clickable(role = Role.Button) { onSelect(d) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    UiText(WeekInitials[i], 12.sp, FontWeight.Bold, t.onAccent.copy(alpha = 0.75f))
                    Box(
                        Modifier.size(42.dp).clip(t.cellShape).background(if (on) t.onAccent else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        UiText(d.dayOfMonth.toString(), 16.sp, FontWeight.ExtraBold, if (on) t.accent else t.onAccent)
                    }
                    Box(Modifier.size(4.dp).clip(CircleShape).background(if (hasEvents(d)) t.onAccent else Color.Transparent))
                }
            }
        }
    }
}

@Composable
private fun CardHeader(
    today: LocalDate,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    hasEvents: (LocalDate) -> Boolean,
) {
    val t = AppTheme.tokens
    Column(
        Modifier.fillMaxWidth().styledCard(t).padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.padding(horizontal = 4.dp)) {
            TitleText(YearMonth.from(selected).monthLabel() + " ", 18.sp)
            TitleText(selected.year.toString(), 18.sp, color = t.muted)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            weekOf(selected).forEachIndexed { i, d ->
                val on = d == selected
                val fg = if (on) t.onAccent else t.text
                Column(
                    Modifier.weight(1f).height(72.dp).clip(t.smallShape).background(if (on) t.accent else t.surf2)
                        .clickable(role = Role.Button) { onSelect(d) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                ) {
                    UiText(WeekInitials[i], 11.sp, FontWeight.Bold, fg.copy(alpha = 0.75f))
                    UiText(d.dayOfMonth.toString(), 19.sp, FontWeight.ExtraBold, fg)
                    Box(
                        Modifier.size(5.dp).clip(CircleShape)
                            .background(if (hasEvents(d)) (if (on) t.onAccent else t.accent) else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeEventCard(event: Event, alt: Boolean, onClick: () -> Unit) {
    val t = AppTheme.tokens
    Row(
        Modifier.fillMaxWidth().styledCard(t).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(t.smallShape).background(if (alt) t.accent2 else t.accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(event.type.icon, event.type.label, tint = if (alt) t.onAccent2 else t.onAccent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            UiText(event.title, 15.sp, FontWeight.ExtraBold, maxLines = 1)
            UiText(event.subtitle, 13.sp, FontWeight.SemiBold, t.muted, maxLines = 1)
        }
        Spacer(Modifier.width(10.dp))
        TitleText(event.time.hhmm(), 16.sp)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MiniCard(
    label: String,
    value: String,
    progress: Float,
    barColor: Color,
    onClick: () -> Unit,
) {
    val t = AppTheme.tokens
    Column(
        Modifier.weight(1f).fillMaxHeight().styledCard(t).clickable(onClick = onClick).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        UiText(label, 12.sp, FontWeight.ExtraBold, t.muted)
        TitleText(value, 22.sp)
        // Sin segundo color, accent2 = surf2: la barra de Rutinas usa el acento para seguir viéndose
        ProgressBar(progress, track = t.surf2, fill = if (barColor == t.surf2) t.accent else barColor)
    }
}
