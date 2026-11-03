package com.portablenotes.domain.ui.events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.models.events.Event
import com.portablenotes.domain.models.events.EventType
import com.portablenotes.domain.models.events.Priority
import com.portablenotes.domain.models.events.Repeat
import com.portablenotes.domain.models.events.hhmm
import com.portablenotes.domain.models.events.prettyLong
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.UUID

/** Crear (event == null) o editar un evento. Funciona igual para eventos que se repiten o no. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventEditorSheet(
    event: Event?,
    initialDate: LocalDate,
    onSave: (Event) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(event?.title ?: "") }
    var notes by remember { mutableStateOf(event?.notes ?: "") }
    var date by remember { mutableStateOf(event?.date ?: initialDate) }
    var time by remember { mutableStateOf(event?.time ?: LocalTime.of(9, 0)) }
    var repeat by remember { mutableStateOf(event?.repeat ?: Repeat.None) }
    var type by remember { mutableStateOf(event?.type ?: EventType.Other) }
    var priority by remember { mutableStateOf(event?.priority ?: Priority.Medium) }
    val routines = com.portablenotes.domain.ui.routines.LocalRoutines.current
    var routineId by remember { mutableStateOf(event?.routineId) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = com.portablenotes.domain.ui.AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                if (event == null) "Nuevo evento" else "Editar evento",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = com.portablenotes.domain.ui.AppColors.TextPrimary,
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PickerField("Fecha", date.prettyLong(), Modifier.weight(1.6f)) { showDate = true }
                PickerField("Hora", time.hhmm(), Modifier.weight(1f)) { showTime = true }
            }

            SectionLabel("Se repite")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Repeat.entries.forEach { SelectChip(it.label, it == repeat) { repeat = it } }
            }

            SectionLabel("Tipo de evento")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EventType.entries.forEach { SelectChip(it.label, it == type) { type = it } }
            }

            SectionLabel("Prioridad")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { SelectChip(it.label, it == priority) { priority = it } }
            }

            if (routines.isNotEmpty()) {
                SectionLabel("Rutina anclada (opcional)")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectChip("Ninguna", routineId == null) { routineId = null }
                    routines.forEach { r -> SelectChip(r.name, r.id == routineId) { routineId = r.id } }
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, com.portablenotes.domain.ui.AppColors.Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = com.portablenotes.domain.ui.AppColors.TextPrimary),
                ) { Text("Cancelar") }
                Button(
                    onClick = {
                        onSave(
                            Event(
                                id = event?.id ?: UUID.randomUUID().toString(),
                                title = title.trim(),
                                date = date,
                                time = time,
                                repeat = repeat,
                                type = type,
                                priority = priority,
                                notes = notes.trim(),
                                done = if (repeat == Repeat.None) event?.done ?: false else false,
                                routineId = routineId,
                            )
                        )
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.portablenotes.domain.ui.AppColors.Accent,
                        contentColor = com.portablenotes.domain.ui.AppColors.OnAccent,
                        disabledContainerColor = com.portablenotes.domain.ui.AppColors.SurfaceMuted,
                        disabledContentColor = com.portablenotes.domain.ui.AppColors.TextSecondary,
                    ),
                ) { Text("Guardar") }
            }
        }
    }

    if (showDate) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDate = false
                }) { Text("Aceptar", color = com.portablenotes.domain.ui.AppColors.TextPrimary) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancelar", color = com.portablenotes.domain.ui.AppColors.TextSecondary) } },
        ) { DatePicker(state = pickerState) }
    }

    if (showTime) {
        val timeState = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTime = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(timeState.hour, timeState.minute)
                    showTime = false
                }) { Text("Aceptar", color = com.portablenotes.domain.ui.AppColors.TextPrimary) }
            },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("Cancelar", color = com.portablenotes.domain.ui.AppColors.TextSecondary) } },
            text = { TimePicker(state = timeState) },
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = com.portablenotes.domain.ui.AppColors.Accent,
    unfocusedBorderColor = com.portablenotes.domain.ui.AppColors.Border,
    focusedLabelColor = com.portablenotes.domain.ui.AppColors.Accent,
    cursorColor = com.portablenotes.domain.ui.AppColors.Accent,
    focusedContainerColor = com.portablenotes.domain.ui.AppColors.Surface,
    unfocusedContainerColor = com.portablenotes.domain.ui.AppColors.Surface,
)

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 13.sp, color = com.portablenotes.domain.ui.AppColors.TextSecondary, fontWeight = FontWeight.Medium)
}

@Composable
private fun PickerField(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = com.portablenotes.domain.ui.AppColors.Surface,
        border = BorderStroke(1.dp, com.portablenotes.domain.ui.AppColors.Border),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, fontSize = 11.sp, color = com.portablenotes.domain.ui.AppColors.TextSecondary)
            Text(value, fontSize = 14.sp, color = com.portablenotes.domain.ui.AppColors.TextPrimary, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}