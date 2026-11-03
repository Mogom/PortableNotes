package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.components.AddSquareButton
import com.portablenotes.domain.ui.components.CheckDot
import com.portablenotes.domain.ui.components.EmptyBox
import com.portablenotes.domain.ui.components.PillChip
import com.portablenotes.domain.ui.components.TitleText
import com.portablenotes.domain.ui.components.UiText
import com.portablenotes.domain.ui.components.styledCard
import com.portablenotes.domain.ui.routines.Routine
import com.portablenotes.domain.ui.routines.RoutineCategory
import com.portablenotes.domain.ui.routines.RoutinesState
import com.portablenotes.domain.ui.routines.iconFor
import com.portablenotes.domain.ui.theme.AppTheme
import java.time.LocalDate

private sealed interface RoutineEditorRequest {
    data object New : RoutineEditorRequest
    data class Edit(val routine: Routine) : RoutineEditorRequest
}

/**
 * Rutinas 5a: título + "x de y hechas hoy", chips de categoría y grilla de 2 columnas.
 * El check marca la rutina como hecha hoy; tocar la tarjeta la edita.
 */
@Composable
fun RoutinesScreen(
    state: RoutinesState,
    modifier: Modifier = Modifier,
    linkedEvents: (Routine) -> Int = { 0 },
) {
    val t = AppTheme.tokens
    val today = remember { LocalDate.now() }
    var filter by remember { mutableStateOf<RoutineCategory?>(null) }
    var editor by remember { mutableStateOf<RoutineEditorRequest?>(null) }

    val all = state.routines.toList()
    val visible = if (filter == null) all else all.filter { it.category == filter }
    val (doneToday, total) = state.progressOn(today)

    Box(modifier.fillMaxSize().background(t.bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(top = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    TitleText("Rutinas", 28.sp)
                    UiText(
                        if (total == 0) "Crea tu primera rutina" else "$doneToday de $total hechas hoy",
                        14.sp, FontWeight.SemiBold, t.muted,
                    )
                }
                AddSquareButton("Nueva rutina") { editor = RoutineEditorRequest.New }
            }

            // Filtro por categoría
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CategoryChip("Todas", filter == null) { filter = null }
                RoutineCategory.entries.forEach { c -> CategoryChip(c.label, filter == c) { filter = c } }
            }

            Column(Modifier.padding(horizontal = 18.dp)) {
                if (visible.isEmpty()) {
                    EmptyBox(if (all.isEmpty()) "Toca + para crear una rutina" else "Sin rutinas en esta categoría")
                } else {
                    // Grilla de 2 columnas (dentro de un scroll, por eso no se usa LazyVerticalGrid)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        visible.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
                                pair.forEach { r ->
                                    RoutineCard(
                                        routine = r,
                                        today = today,
                                        linked = linkedEvents(r),
                                        onToggleDone = { state.toggleDone(r.id, today) },
                                        onClick = { editor = RoutineEditorRequest.Edit(r) },
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    editor?.let { req ->
        RoutineEditorSheet(
            routine = (req as? RoutineEditorRequest.Edit)?.routine,
            onSave = { state.upsert(it); editor = null },
            onDelete = (req as? RoutineEditorRequest.Edit)?.let { e -> { state.delete(e.routine.id); editor = null } },
            onDismiss = { editor = null },
        )
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val t = AppTheme.tokens
    PillChip(
        label = label,
        background = if (selected) t.accent else t.cardBg,
        content = if (selected) t.onAccent else t.text,
        border = if (selected) null else t.cardBorder,
        onClick = onClick,
    )
}

@Composable
private fun RoutineCard(
    routine: Routine,
    today: LocalDate,
    linked: Int,
    onToggleDone: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = AppTheme.tokens
    val done = routine.isDoneOn(today)
    val streak = routine.streak(today)
    Column(
        modifier.styledCard(t).clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(46.dp).clip(t.smallShape).background(t.surf2), contentAlignment = Alignment.Center) {
                Icon(iconFor(routine.iconKey), null, tint = t.text, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.weight(1f))
            CheckDot(done, size = 28.dp, onClick = onToggleDone)
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            TitleText(routine.name, 17.sp, maxLines = 2)
            val desc = if (routine.description.isBlank()) routine.category.label
                else "${routine.category.label} · ${routine.description}"
            UiText(desc, 13.sp, FontWeight.Bold, t.muted, maxLines = 2)
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val label = when (streak) {
                0 -> "Sin racha"
                1 -> "1 día"
                else -> "$streak días"
            }
            Box(Modifier.clip(CircleShape).background(t.accent2).padding(horizontal = 10.dp, vertical = 4.dp)) {
                UiText(label, 12.sp, FontWeight.ExtraBold, t.onAccent2)
            }
            if (linked > 0) {
                Box(Modifier.clip(CircleShape).background(t.surf2).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    UiText("$linked ${if (linked == 1) "evento" else "eventos"}", 12.sp, FontWeight.ExtraBold, t.text)
                }
            }
        }
    }
}

/** Ícono de la rutina sobre surf2 (lo usa también la hoja de edición). */
@Composable
private fun IconBadge(iconKey: String, size: Int) {
    val t = AppTheme.tokens
    Box(Modifier.size(size.dp).clip(t.smallShape).background(t.surf2), contentAlignment = Alignment.Center) {
        Icon(iconFor(iconKey), contentDescription = null, tint = t.text, modifier = Modifier.size((size / 2).dp))
    }
}

/** Chip de la hoja de edición. */
@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) = CategoryChip(label, selected, onClick)

/** Crear o editar. Solo la categoría "Personalizada" deja elegir el ícono; las demás lo asignan solas. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RoutineEditorSheet(
    routine: com.portablenotes.domain.ui.routines.Routine?,
    onSave: (com.portablenotes.domain.ui.routines.Routine) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(routine?.name ?: "") }
    var description by remember { mutableStateOf(routine?.description ?: "") }
    var category by remember { mutableStateOf(routine?.category ?: com.portablenotes.domain.ui.routines.RoutineCategory.Daily) }
    var customIcon by remember { mutableStateOf(if (routine?.category == com.portablenotes.domain.ui.routines.RoutineCategory.Custom) routine.iconKey else "star") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = com.portablenotes.domain.ui.AppColors.Accent,
        unfocusedBorderColor = com.portablenotes.domain.ui.AppColors.Border,
        focusedLabelColor = com.portablenotes.domain.ui.AppColors.Accent,
        cursorColor = com.portablenotes.domain.ui.AppColors.Accent,
        focusedContainerColor = com.portablenotes.domain.ui.AppColors.Surface,
        unfocusedContainerColor = com.portablenotes.domain.ui.AppColors.Surface,
        focusedTextColor = com.portablenotes.domain.ui.AppColors.TextPrimary,
        unfocusedTextColor = com.portablenotes.domain.ui.AppColors.TextSecondary
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = com.portablenotes.domain.ui.AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                if (routine == null) "Nueva rutina" else "Editar rutina",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = com.portablenotes.domain.ui.AppColors.TextPrimary,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
            )

            Text("Categoría", fontSize = 13.sp, color = com.portablenotes.domain.ui.AppColors.TextSecondary, fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                com.portablenotes.domain.ui.routines.RoutineCategory.entries.forEach { c -> Chip(c.label, c == category) { category = c } }
            }

            if (category == com.portablenotes.domain.ui.routines.RoutineCategory.Custom) {
                Text("Ícono", fontSize = 13.sp, color = com.portablenotes.domain.ui.AppColors.TextSecondary, fontWeight = FontWeight.Medium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.portablenotes.domain.ui.routines.RoutineIcons.all.forEach { (key, vector) ->
                        val selected = key == customIcon
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) com.portablenotes.domain.ui.AppColors.Accent else com.portablenotes.domain.ui.AppColors.SurfaceMuted)
                                .clickable { customIcon = key },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(vector, null, tint = if (selected) com.portablenotes.domain.ui.AppColors.OnAccent else com.portablenotes.domain.ui.AppColors.TextPrimary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(category.defaultIconKey, size = 40)
                    Spacer(Modifier.width(10.dp))
                    Text("Ícono asignado automáticamente", fontSize = 13.sp, color = com.portablenotes.domain.ui.AppColors.TextSecondary)
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
                            com.portablenotes.domain.ui.routines.Routine(
                                id = routine?.id ?: java.util.UUID.randomUUID().toString(),
                                name = name.trim(),
                                description = description.trim(),
                                category = category,
                                iconKey = if (category == com.portablenotes.domain.ui.routines.RoutineCategory.Custom) customIcon else category.defaultIconKey,
                                createdAt = routine?.createdAt ?: System.currentTimeMillis(),
                                // Al editar se conserva el historial de días hechos (racha)
                                completedDates = routine?.completedDates ?: emptyList(),
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
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

            if (onDelete != null) {
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text("Eliminar rutina", color = com.portablenotes.domain.ui.AppColors.TextSecondary)
                }
            }
        }
    }
}