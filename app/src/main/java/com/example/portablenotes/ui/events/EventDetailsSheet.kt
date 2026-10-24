package com.example.portablenotes.ui.events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portablenotes.ui.AppColors
import com.example.portablenotes.ui.routines.LocalRoutines
import com.example.portablenotes.ui.routines.Routine
import com.example.portablenotes.ui.routines.iconFor
import java.time.LocalDate

/** Hoja con la información completa de los eventos de un día (o de un evento concreto). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    date: LocalDate,
    events: List<Event>,
    onDismiss: () -> Unit,
    onToggleDone: (Event) -> Unit,
    onEdit: (Event) -> Unit,
    onDelete: (Event) -> Unit,
) {
    // Si el último evento se elimina, cerramos la hoja
    LaunchedEffect(events.isEmpty()) { if (events.isEmpty()) onDismiss() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column {
                Text(date.prettyLong(), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                Text(
                    "${events.size} ${if (events.size == 1) "evento" else "eventos"}",
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary,
                )
            }
            events.forEach { event ->
                DetailCard(
                    event = event,
                    date = date,
                    onToggleDone = { onToggleDone(event) },
                    onEdit = { onEdit(event) },
                    onDelete = { onDelete(event) },
                )
            }
        }
    }
}

@Composable
private fun DetailCard(
    event: Event,
    date: LocalDate,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    event.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                )
                if (event.done) {
                    Box(
                        Modifier.clip(CircleShape).background(AppColors.SurfaceMuted)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text("Hecho", fontSize = 12.sp, color = AppColors.TextPrimary) }
                }
            }

            HorizontalDivider(color = AppColors.Border)

            InfoRow("Fecha") { ValueText(date.prettyLong()) }
            InfoRow("Hora") { ValueText(event.time.hhmm()) }
            InfoRow("Se repite") {
                ValueText(
                    if (event.repeat == Repeat.None) event.repeat.label
                    else "${event.repeat.label} (desde ${event.date.dayOfMonth}/${event.date.monthValue}/${event.date.year})"
                )
            }
            InfoRow("Tipo") { ValueText(event.type.label) }
            InfoRow("Prioridad") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityBars(event.priority)
                    Spacer(Modifier.width(8.dp))
                    ValueText(event.priority.label)
                }
            }
            if (event.notes.isNotBlank()) InfoRow("Notas") { ValueText(event.notes) }

            // Rutina anclada: se ve al abrir la descripción del evento
            val routine = LocalRoutines.current.firstOrNull { it.id == event.routineId }
            if (routine != null) AnchoredRoutine(routine)

            Spacer(Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (event.repeat == Repeat.None) {
                    Button(
                        onClick = onToggleDone,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (event.done) AppColors.SurfaceMuted else AppColors.Accent,
                            contentColor = if (event.done) AppColors.TextPrimary else AppColors.OnAccent,
                        ),
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (event.done) "Desmarcar" else "Marcar como hecho", fontSize = 14.sp)
                    }
                } else {
                    Text(
                        "Evento recurrente: los cambios aplican a toda la serie",
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                    )
                }
                CircleIconButton(Icons.Rounded.Edit, "Editar", onEdit)
                CircleIconButton(Icons.Rounded.Delete, "Eliminar", onDelete)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(92.dp), fontSize = 13.sp, color = AppColors.TextSecondary)
        content()
    }
}

@Composable
private fun ValueText(text: String) {
    Text(text, fontSize = 14.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium)
}

@Composable
private fun AnchoredRoutine(routine: Routine) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AppColors.SurfaceMuted,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(iconFor(routine.iconKey), contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(routine.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                    Text("Rutina · ${routine.category.label}", fontSize = 12.sp, color = AppColors.TextSecondary)
                }
            }
            if (routine.description.isNotBlank()) {
                Text(routine.description, fontSize = 13.sp, color = AppColors.TextPrimary)
            }
        }
    }
}