package com.example.portablenotes.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portablenotes.ui.AppColors
import com.example.portablenotes.ui.tasks.TasksState
import com.example.portablenotes.ui.tasks.Task
import com.example.portablenotes.ui.tasks.TaskRow

private const val MAX_DEPTH = 3   // niveles 0..3 (4 niveles de jerarquía)

private sealed interface TaskDialog {
    data class Add(val parentId: String?) : TaskDialog
    data class Edit(val task: Task) : TaskDialog
}

/**
 * Pantalla de Tareas.
 *  - Interruptor "Organizar en árbol": con jerarquía de subtareas, o lista simple con check.
 *  - Tocar el título edita / elimina; "+" en una fila agrega una subtarea (solo en modo árbol).
 */
@Composable
fun TasksScreen(
    state: TasksState,
    modifier: Modifier = Modifier,
) {
    var dialog by remember { mutableStateOf<TaskDialog?>(null) }
    val rows = state.visibleRows()          // se recalcula al cambiar el estado
    val (done, total) = state.progress()

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
            // Cabecera
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tareas", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                    Text(
                        if (total == 0) "Sin tareas todavía" else "$done de $total completadas",
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary,
                    )
                }
                Box(
                    Modifier.size(42.dp).clip(CircleShape).background(AppColors.Accent)
                        .clickable { dialog = TaskDialog.Add(null) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Add, "Nueva tarea", tint = AppColors.OnAccent, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Progreso
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else done / total.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = AppColors.Accent,
                trackColor = AppColors.SurfaceMuted,
            )

            Spacer(Modifier.height(14.dp))

            // Modo árbol / lista simple
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Surface,
                border = BorderStroke(1.dp, AppColors.Border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Organizar en árbol", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                        Text(
                            if (state.treeMode) "Con subtareas y jerarquía" else "Lista simple con check",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary,
                        )
                    }
                    Switch(
                        checked = state.treeMode,
                        onCheckedChange = { state.enableTreeMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.OnAccent,
                            checkedTrackColor = AppColors.Accent,
                            uncheckedThumbColor = AppColors.TextSecondary,
                            uncheckedTrackColor = AppColors.SurfaceMuted,
                            uncheckedBorderColor = AppColors.Border,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (rows.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Todo en orden", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Toca + para crear tu primera tarea", fontSize = 13.sp, color = AppColors.TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 104.dp),   // espacio para la barra inferior
                ) {
                    items(rows, key = { it.task.id }) { row ->
                        TaskItem(
                            row = row,
                            treeMode = state.treeMode,
                            onToggleDone = { state.toggleDone(row.task.id) },
                            onToggleExpanded = { state.toggleExpanded(row.task.id) },
                            onEdit = { dialog = TaskDialog.Edit(row.task) },
                            onAddChild = { dialog = TaskDialog.Add(row.task.id) },
                        )
                    }
                }
            }
        }
    }

    dialog?.let { d ->
        val editing = (d as? TaskDialog.Edit)?.task
        TaskEditDialog(
            dialog = d,
            warning = if (editing != null && state.childrenOf(editing.id).isNotEmpty())
                "También se eliminarán sus subtareas." else null,
            onSave = { text ->
                when (d) {
                    is TaskDialog.Add -> state.add(text, d.parentId)
                    is TaskDialog.Edit -> state.rename(d.task.id, text)
                }
                dialog = null
            },
            onDelete = editing?.let { { state.delete(it.id); dialog = null } },
            onDismiss = { dialog = null },
        )
    }
}

@Composable
private fun TaskItem(
    row: TaskRow,
    treeMode: Boolean,
    onToggleDone: () -> Unit,
    onToggleExpanded: () -> Unit,
    onEdit: () -> Unit,
    onAddChild: () -> Unit,
) {
    val task = row.task
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AppColors.Surface,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (row.depth * 20).dp)      // sangría por nivel
            .alpha(if (task.done) 0.6f else 1f),
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            // Flecha expandir / contraer (solo si tiene hijos)
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                if (treeMode && row.childCount > 0) {
                    Icon(
                        imageVector = if (task.expanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowRight,
                        contentDescription = if (task.expanded) "Contraer" else "Expandir",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(22.dp).clickable(onClick = onToggleExpanded),
                    )
                }
            }

            CheckCircle(done = task.done, onClick = onToggleDone)

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f).clickable(onClick = onEdit)) {
                Text(
                    task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.done) AppColors.TextSecondary else AppColors.TextPrimary,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (treeMode && row.childCount > 0) {
                Spacer(Modifier.width(8.dp))
                Box(Modifier.clip(CircleShape).background(AppColors.SurfaceMuted).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("${row.doneChildren}/${row.childCount}", fontSize = 11.sp, color = AppColors.TextPrimary)
                }
            }

            if (treeMode && row.depth < MAX_DEPTH) {
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.size(30.dp).clip(CircleShape).background(AppColors.SurfaceMuted).clickable(onClick = onAddChild),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Add, "Agregar subtarea", tint = AppColors.TextPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CheckCircle(done: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (done) AppColors.Accent else AppColors.Surface)
            .then(if (done) Modifier else Modifier.border(1.5.dp, AppColors.TextSecondary, CircleShape))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Icon(Icons.Rounded.Check, "Hecha", tint = AppColors.OnAccent, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun TaskEditDialog(
    dialog: TaskDialog,
    warning: String?,
    onSave: (String) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var text by remember(dialog) { mutableStateOf((dialog as? TaskDialog.Edit)?.task?.title ?: "") }
    val title = when (dialog) {
        is TaskDialog.Add -> if (dialog.parentId == null) "Nueva tarea" else "Nueva subtarea"
        is TaskDialog.Edit -> "Editar tarea"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Título") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Accent,
                        unfocusedBorderColor = AppColors.Border,
                        focusedLabelColor = AppColors.Accent,
                        cursorColor = AppColors.Accent,
                    ),
                )
                if (warning != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(warning, fontSize = 12.sp, color = AppColors.TextSecondary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) {
                Text("Guardar", color = if (text.isNotBlank()) AppColors.Accent else AppColors.TextSecondary)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text("Eliminar", color = AppColors.TextSecondary) }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.TextSecondary) }
            }
        },
    )
}