package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.components.AddSquareButton
import com.portablenotes.domain.ui.components.Breakpoints
import com.portablenotes.domain.ui.components.contentBottomPadding
import com.portablenotes.domain.ui.components.screenInsets
import com.portablenotes.domain.ui.components.CheckDot
import com.portablenotes.domain.ui.components.EmptyBox
import com.portablenotes.domain.ui.components.ProgressBar
import com.portablenotes.domain.ui.components.SwitchCard
import com.portablenotes.domain.ui.components.TitleText
import com.portablenotes.domain.ui.components.UiText
import com.portablenotes.domain.ui.components.styledCard
import com.portablenotes.domain.ui.tasks.TaskRow
import com.portablenotes.domain.ui.tasks.TasksState
import com.portablenotes.domain.ui.theme.AppTheme

private const val MAX_DEPTH = 3   // niveles 0..3 (4 niveles de jerarquía)

private sealed interface TaskDialog {
    data class Add(val parentId: String?) : TaskDialog
    data class Edit(val task: com.portablenotes.domain.ui.tasks.Task) : TaskDialog
}

/**
 * Tareas 5a: tarjeta de progreso en acento, interruptor "Organizar en árbol" y lista en una tarjeta.
 *  - Tocar el check marca / desmarca. Tocar el título edita o elimina.
 *  - En modo árbol, las subtareas van con sangría y "+" agrega una subtarea.
 */
@Composable
fun TasksScreen(
    state: TasksState,
    modifier: Modifier = Modifier,
) {
    val t = AppTheme.tokens
    var dialog by remember { mutableStateOf<TaskDialog?>(null) }
    val rows = state.visibleRows()
    val (done, total) = state.progress()
    val pct = if (total == 0) 0 else (done * 100 / total)

    val header: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TitleText("Tareas", 28.sp, Modifier.weight(1f))
            AddSquareButton("Nueva tarea") { dialog = TaskDialog.Add(null) }
        }
    }
    val summary: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Progreso
            Column(
                Modifier.fillMaxWidth().clip(t.cardShape).background(t.accent).padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("$pct%", style = t.titleStyle(38.sp).copy(letterSpacing = (-0.03).em), color = t.onAccent)
                    UiText(
                        if (total == 0) "Sin tareas todavía" else "$done de $total completadas",
                        14.sp, FontWeight.Bold, t.onAccent, Modifier.padding(bottom = 8.dp),
                    )
                }
                ProgressBar(pct / 100f, track = t.onSoft, fill = t.onAccent, height = 10.dp)
            }
            SwitchCard(
                title = "Organizar en árbol",
                subtitle = if (state.treeMode) "Con subtareas y jerarquía" else "Lista simple con check",
                checked = state.treeMode,
                onChange = { state.enableTreeMode(it) },
            )
        }
    }
    val list: @Composable () -> Unit = {
        if (rows.isEmpty()) {
            EmptyBox("Todo en orden. Toca + para crear tu primera tarea")
        } else {
            Column(Modifier.fillMaxWidth().styledCard(t).padding(vertical = 6.dp)) {
                rows.forEach { row ->
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
            header()
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Box(Modifier.weight(0.85f)) { summary() }
                    Box(Modifier.weight(1.15f)) { list() }
                }
            } else {
                summary()
                list()
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
    val t = AppTheme.tokens
    val task = row.task
    // Sangría: 16 dp de base y 32 dp más por nivel (48 dp en el primer nivel de subtareas)
    val start = if (treeMode) (16 + row.depth * 32).dp else 16.dp
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onToggleDone).padding(start = start, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CheckDot(task.done, onClick = onToggleDone)
        Text(
            task.title,
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).clickable(onClick = onEdit),
            style = TextStyle(
                fontFamily = com.portablenotes.domain.ui.theme.Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textDecoration = if (task.done) TextDecoration.LineThrough else null,
            ),
            color = if (task.done) t.muted else t.text,
            maxLines = 2,
        )
        if (treeMode && row.childCount > 0) {
            UiText("${row.doneChildren}/${row.childCount}", 12.sp, FontWeight.ExtraBold, t.muted)
            Icon(
                if (task.expanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowRight,
                contentDescription = if (task.expanded) "Contraer" else "Expandir",
                tint = t.muted,
                modifier = Modifier.size(22.dp).clip(CircleShape).clickable(onClick = onToggleExpanded),
            )
        }
        if (treeMode && row.depth < MAX_DEPTH) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(t.surf2).clickable(onClick = onAddChild),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, "Agregar subtarea", tint = t.text, modifier = Modifier.size(16.dp))
            }
        }
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
        containerColor = com.portablenotes.domain.ui.AppColors.Surface,
        title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = com.portablenotes.domain.ui.AppColors.TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Título") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = com.portablenotes.domain.ui.AppColors.Accent,
                        unfocusedBorderColor = com.portablenotes.domain.ui.AppColors.Border,
                        focusedLabelColor = com.portablenotes.domain.ui.AppColors.Accent,
                        cursorColor = com.portablenotes.domain.ui.AppColors.Accent,
                    ),
                )
                if (warning != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(warning, fontSize = 12.sp, color = com.portablenotes.domain.ui.AppColors.TextSecondary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) {
                Text("Guardar", color = if (text.isNotBlank()) com.portablenotes.domain.ui.AppColors.Accent else com.portablenotes.domain.ui.AppColors.TextSecondary)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text("Eliminar", color = com.portablenotes.domain.ui.AppColors.TextSecondary) }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = com.portablenotes.domain.ui.AppColors.TextSecondary) }
            }
        },
    )
}