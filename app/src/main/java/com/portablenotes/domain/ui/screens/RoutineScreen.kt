package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portablenotes.ui.AppColors
import com.example.portablenotes.ui.routines.Routine
import com.example.portablenotes.ui.routines.RoutinesState
import com.example.portablenotes.ui.routines.RoutineCategory
import com.example.portablenotes.ui.routines.RoutineIcons
import com.example.portablenotes.ui.routines.iconFor
import java.util.UUID

private sealed interface RoutineEditorRequest {
    data object New : RoutineEditorRequest
    data class Edit(val routine: Routine) : RoutineEditorRequest
}

@Composable
fun RoutinesScreen(
    state: RoutinesState,
    modifier: Modifier = Modifier,
    linkedEvents: (Routine) -> Int = { 0 },
) {
    var filter by remember { mutableStateOf<RoutineCategory?>(null) }
    var editor by remember { mutableStateOf<RoutineEditorRequest?>(null) }

    val all = state.routines.toList()
    val visible = if (filter == null) all else all.filter { it.category == filter }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.BackgroundTop, AppColors.Background)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Rutinas", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                    Text(
                        if (all.isEmpty()) "Crea tu primera rutina" else "${all.size} ${if (all.size == 1) "rutina" else "rutinas"}",
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary,
                    )
                }
                Box(
                    Modifier.size(42.dp).clip(CircleShape).background(AppColors.Accent)
                        .clickable { editor = RoutineEditorRequest.New },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Add, "Nueva rutina", tint = AppColors.OnAccent, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Filtro por categoría
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Chip("Todas", filter == null) { filter = null }
                RoutineCategory.entries.forEach { c -> Chip(c.label, filter == c) { filter = c } }
            }

            Spacer(Modifier.height(14.dp))

            if (visible.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin rutinas aquí", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Toca + para crear una", fontSize = 13.sp, color = AppColors.TextSecondary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 104.dp),   // espacio para la barra inferior
                ) {
                    items(visible, key = { it.id }) { routine ->
                        RoutineCard(
                            routine = routine,
                            linked = linkedEvents(routine),
                            onClick = { editor = RoutineEditorRequest.Edit(routine) },
                        )
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
private fun RoutineCard(routine: Routine, linked: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            IconBadge(routine.iconKey, size = 42)
            Spacer(Modifier.height(6.dp))
            Text(
                routine.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(routine.category.label, fontSize = 12.sp, color = AppColors.Accent)
            if (routine.description.isNotBlank()) {
                Text(
                    routine.description,
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (linked > 0) {
                Spacer(Modifier.height(4.dp))
                Box(Modifier.clip(CircleShape).background(AppColors.SurfaceMuted).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(
                        "$linked ${if (linked == 1) "evento" else "eventos"}",
                        fontSize = 11.sp,
                        color = AppColors.TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun IconBadge(iconKey: String, size: Int) {
    Box(
        Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3).dp))
            .background(AppColors.Accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(iconFor(iconKey), contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size((size / 2).dp))
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) AppColors.Accent else AppColors.SurfaceMuted)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) AppColors.OnAccent else AppColors.TextPrimary,
        )
    }
}

/** Crear o editar. Solo la categoría "Personalizada" deja elegir el ícono; las demás lo asignan solas. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RoutineEditorSheet(
    routine: Routine?,
    onSave: (Routine) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(routine?.name ?: "") }
    var description by remember { mutableStateOf(routine?.description ?: "") }
    var category by remember { mutableStateOf(routine?.category ?: RoutineCategory.Daily) }
    var customIcon by remember { mutableStateOf(if (routine?.category == RoutineCategory.Custom) routine.iconKey else "star") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.Accent,
        unfocusedBorderColor = AppColors.Border,
        focusedLabelColor = AppColors.Accent,
        cursorColor = AppColors.Accent,
        focusedContainerColor = AppColors.Surface,
        unfocusedContainerColor = AppColors.Surface,
        focusedTextColor = AppColors.TextPrimary,
        unfocusedTextColor = AppColors.TextSecondary
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.Background,
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
                color = AppColors.TextPrimary,
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

            Text("Categoría", fontSize = 13.sp, color = AppColors.TextSecondary, fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RoutineCategory.entries.forEach { c -> Chip(c.label, c == category) { category = c } }
            }

            if (category == RoutineCategory.Custom) {
                Text("Ícono", fontSize = 13.sp, color = AppColors.TextSecondary, fontWeight = FontWeight.Medium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoutineIcons.all.forEach { (key, vector) ->
                        val selected = key == customIcon
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) AppColors.Accent else AppColors.SurfaceMuted)
                                .clickable { customIcon = key },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(vector, null, tint = if (selected) AppColors.OnAccent else AppColors.TextPrimary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(category.defaultIconKey, size = 40)
                    Spacer(Modifier.width(10.dp))
                    Text("Ícono asignado automáticamente", fontSize = 13.sp, color = AppColors.TextSecondary)
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, AppColors.Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextPrimary),
                ) { Text("Cancelar") }
                Button(
                    onClick = {
                        onSave(
                            Routine(
                                id = routine?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                description = description.trim(),
                                category = category,
                                iconKey = if (category == RoutineCategory.Custom) customIcon else category.defaultIconKey,
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Accent,
                        contentColor = AppColors.OnAccent,
                        disabledContainerColor = AppColors.SurfaceMuted,
                        disabledContentColor = AppColors.TextSecondary,
                    ),
                ) { Text("Guardar") }
            }

            if (onDelete != null) {
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text("Eliminar rutina", color = AppColors.TextSecondary)
                }
            }
        }
    }
}