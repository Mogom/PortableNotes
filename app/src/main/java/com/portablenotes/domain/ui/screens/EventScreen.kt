package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portablenotes.ui.AppColors
import com.example.portablenotes.ui.calendar.MonthCalendar
import com.example.portablenotes.ui.events.daysWithEvents
import com.example.portablenotes.ui.events.hhmm
import com.example.portablenotes.ui.events.on
import com.example.portablenotes.ui.events.prettyLong
import java.time.LocalDate
import java.time.YearMonth

sealed interface EditorRequest {
    data class New(val date: LocalDate) : EditorRequest
    data class Edit(val event: com.example.portablenotes.ui.events.Event) : EditorRequest
}

private data class DetailRequest(val date: LocalDate, val eventId: String?)

private sealed interface ListRow {
    data class Header(val date: LocalDate) : ListRow
    data class Item(val event: com.example.portablenotes.ui.events.Event, val date: LocalDate) : ListRow
}

private fun buildRows(month: YearMonth, all: List<com.example.portablenotes.ui.events.Event>): List<ListRow> = buildList {
    for (d in 1..month.lengthOfMonth()) {
        val date = month.atDay(d)
        val evs = all.on(date)          // ya ordenados por hora
        if (evs.isNotEmpty()) {
            add(ListRow.Header(date))
            evs.forEach { add(ListRow.Item(it, date)) }
        }
    }
}

/**
 * Pantalla de eventos:
 *  - Calendario mensual = 50 % de la altura de la pantalla.
 *  - Debajo, eventos del mes agrupados por fecha y ordenados por hora.
 *  - Tocar un día con punto abre su detalle; "+" crea un evento.
 */
@Composable
fun EventsScreen(
    state: com.example.portablenotes.ui.events.EventsState,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var selected by remember { mutableStateOf(today) }
    var detail by remember { mutableStateOf<DetailRequest?>(null) }
    var editor by remember { mutableStateOf<EditorRequest?>(null) }

    val all = state.events.toList()
    val dots = remember(month, all) { all.daysWithEvents(month) }
    val rows = remember(month, all) { buildRows(month, all) }
    val listState = rememberLazyListState()

    // Al elegir un día, la lista se desplaza hasta su fecha
    LaunchedEffect(selected, rows) {
        val index = rows.indexOfFirst { it is ListRow.Header && it.date == selected }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    fun goTo(target: YearMonth) {
        month = target
        selected = if (target == YearMonth.from(today)) today else target.atDay(1)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.BackgroundTop, AppColors.Background)))
    ) {
        val calendarHeight = maxHeight * 0.50f   // 👈 50 % de la pantalla
        val barSpace = 104.dp

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Eventos", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                }
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AppColors.Accent)
                        .clickable { editor = EditorRequest.New(selected) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Add, "Nuevo evento", tint = AppColors.OnAccent, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            MonthCalendar(
                month = month,
                selected = selected,
                today = today,
                daysWithEvents = dots,
                onPrevMonth = { goTo(month.minusMonths(1)) },
                onNextMonth = { goTo(month.plusMonths(1)) },
                onDayClick = { d ->
                    selected = d
                    if (d in dots) detail = DetailRequest(d, null)
                },
                modifier = Modifier.fillMaxWidth().height(calendarHeight),
            )

            Spacer(Modifier.height(16.dp))

            EventsList(
                rows = rows,
                selected = selected,
                today = today,
                listState = listState,
                bottomPadding = barSpace,
                onEventClick = { e, d -> detail = DetailRequest(d, e.id) },
                onToggleDone = { state.toggleDone(it.id) },
                modifier = Modifier.weight(1f),
            )
        }
    }

    // ----- Hojas -----
    detail?.let { req ->
        val evs = all.on(req.date).filter { req.eventId == null || it.id == req.eventId }
        _root_ide_package_.com.example.portablenotes.ui.events.EventDetailSheet(
            date = req.date,
            events = evs,
            onDismiss = { detail = null },
            onToggleDone = { state.toggleDone(it.id) },
            onEdit = { detail = null; editor = EditorRequest.Edit(it) },
            onDelete = { state.delete(it.id) },
        )
    }

    editor?.let { req ->
        _root_ide_package_.com.example.portablenotes.ui.events.EventEditorSheet(
            event = (req as? EditorRequest.Edit)?.event,
            initialDate = (req as? EditorRequest.New)?.date ?: today,
            onSave = { state.upsert(it); editor = null },
            onDismiss = { editor = null },
        )
    }
}

@Composable
private fun EventsList(
    rows: List<ListRow>,
    selected: LocalDate,
    today: LocalDate,
    listState: LazyListState,
    bottomPadding: Dp,
    onEventClick: (com.example.portablenotes.ui.events.Event, LocalDate) -> Unit,
    onToggleDone: (com.example.portablenotes.ui.events.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (rows.isEmpty()) {
        Column(modifier.fillMaxWidth().padding(top = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sin eventos este mes", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Toca + para crear uno", fontSize = 13.sp, color = AppColors.TextSecondary)
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = bottomPadding),
    ) {
        items(
            items = rows,
            key = {
                when (it) {
                    is ListRow.Header -> "h${it.date}"
                    is ListRow.Item -> "e${it.event.id}_${it.date}"
                }
            },
        ) { row ->
            when (row) {
                is ListRow.Header -> DateHeader(row.date, isSelected = row.date == selected, isToday = row.date == today)
                is ListRow.Item -> EventCard(
                    event = row.event,
                    onClick = { onEventClick(row.event, row.date) },
                    onToggleDone = { onToggleDone(row.event) },
                )
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate, isSelected: Boolean, isToday: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            date.prettyLong(),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary,
        )
        if (isToday) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.clip(CircleShape).background(AppColors.Accent).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text("Hoy", fontSize = 10.sp, color = AppColors.OnAccent)
            }
        }
    }
}

@Composable
private fun EventCard(event: com.example.portablenotes.ui.events.Event, onClick: () -> Unit, onToggleDone: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier.fillMaxWidth().alpha(if (event.done) 0.55f else 1f),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (event.repeat == _root_ide_package_.com.example.portablenotes.ui.events.Repeat.None) {
                DoneCheck(done = event.done, onClick = onToggleDone)
            } else {
                // Los eventos recurrentes no se marcan como hechos
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(AppColors.SurfaceMuted),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Refresh, "Se repite", tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    event.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary,
                    textDecoration = if (event.done) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (event.repeat == _root_ide_package_.com.example.portablenotes.ui.events.Repeat.None) event.type.label else "${event.type.label} · ${event.repeat.label}",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Box(Modifier.clip(CircleShape).background(AppColors.SurfaceMuted).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(event.time.hhmm(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                }
                Spacer(Modifier.height(6.dp))
                _root_ide_package_.com.example.portablenotes.ui.events.PriorityBars(event.priority)
            }
        }
    }
}

@Composable
private fun DoneCheck(done: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (done) AppColors.Accent else AppColors.Surface)
            .then(if (done) Modifier else border(1.5.dp, AppColors.TextSecondary, CircleShape))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Icon(Icons.Rounded.Check, "Hecho", tint = AppColors.OnAccent, modifier = Modifier.size(16.dp))
    }
}