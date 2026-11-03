package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.components.AppFab
import com.portablenotes.domain.ui.components.Breakpoints
import com.portablenotes.domain.ui.components.barClearance
import com.portablenotes.domain.ui.components.contentBottomPadding
import com.portablenotes.domain.ui.components.gridColumns
import com.portablenotes.domain.ui.components.screenInsets
import com.portablenotes.domain.ui.components.DangerRed
import com.portablenotes.domain.ui.components.EmptyBox
import com.portablenotes.domain.ui.components.PrimaryButton
import com.portablenotes.domain.ui.components.SectionLabel
import com.portablenotes.domain.ui.components.TitleText
import com.portablenotes.domain.ui.components.UiText
import com.portablenotes.domain.ui.components.relativeLabel
import com.portablenotes.domain.ui.components.styledCard
import com.portablenotes.domain.ui.notes.Note
import com.portablenotes.domain.ui.theme.AppTheme
import com.portablenotes.domain.ui.theme.Manrope
import java.time.Instant
import java.time.ZoneId

/** Notas 5a: título + contador, buscador y grilla de 2 columnas tipo masonry. */
@Composable
fun NotesScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = AppTheme.tokens
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val visible = if (q.isEmpty()) notes else notes.filter { it.title.contains(q, true) || it.body.contains(q, true) }

    BoxWithConstraints(modifier.fillMaxSize().background(t.bg)) {
        val wide = maxWidth >= Breakpoints.Medium
        val columns = gridColumns(minOf(maxWidth, Breakpoints.MaxContent))
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = Breakpoints.MaxContent)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .screenInsets()
                .padding(horizontal = if (wide) 28.dp else 18.dp)
                .padding(top = 12.dp, bottom = contentBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.Bottom) {
                TitleText("Notas", 28.sp, Modifier.weight(1f))
                UiText("${notes.size} ${if (notes.size == 1) "nota" else "notas"}", 14.sp, FontWeight.Bold, t.muted, Modifier.padding(bottom = 6.dp))
            }

            // Buscador
            Row(
                Modifier.fillMaxWidth().height(50.dp).styledCard(t, CircleShape).padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Rounded.Search, null, tint = t.muted, modifier = Modifier.size(19.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = t.text),
                    cursorBrush = SolidColor(t.accent),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) UiText("Buscar en tus notas", 15.sp, FontWeight.Medium, t.muted)
                            inner()
                        }
                    },
                )
            }

            if (visible.isEmpty()) {
                EmptyBox(if (notes.isEmpty()) "Aún no tienes notas. Toca + para crear una" else "No hay notas con ese texto")
            } else {
                // Masonry: 2 columnas en teléfono, 3–4 en tablet; las notas se reparten en orden
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    (0 until columns).forEach { col ->
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            visible.forEachIndexed { i, n -> if (i % columns == col) NoteCard(n, i) { onNoteClick(n) } }
                        }
                    }
                }
            }
        }

        AppFab("Nueva nota", onCreate, Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = barClearance()))
    }
}

/** Colores de la nota según su posición: acento, tarjeta, segundo color, surf2 (y vuelve a empezar). */
@Composable
private fun NoteCard(note: Note, index: Int, onClick: () -> Unit) {
    val t = AppTheme.tokens
    val kind = index % 4
    val (bg, fg, minH) = when (kind) {
        0 -> Triple(t.accent, t.onAccent, 168.dp)
        1 -> Triple(t.cardBg, t.text, 150.dp)
        2 -> Triple(t.accent2, t.onAccent2, 130.dp)
        else -> Triple(t.surf2, t.text, 130.dp)
    }
    val base = if (kind == 1) Modifier.styledCard(t) else Modifier.clip(t.cardShape).background(bg)
    val date = Instant.ofEpochMilli(note.updatedAt).atZone(ZoneId.systemDefault()).toLocalDate()
    Column(
        base.fillMaxWidth().heightIn(min = minH).clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        UiText(date.relativeLabel(), 12.sp, FontWeight.ExtraBold, fg, Modifier.alpha(0.75f))
        if (note.title.isNotBlank()) TitleText(note.title, 17.sp, color = fg, maxLines = 3)
        if (note.body.isNotBlank()) {
            UiText(note.body, 13.sp, FontWeight.Medium, fg, Modifier.alpha(0.85f), maxLines = 8, style = TextStyle(lineHeight = 19.sp))
        }
    }
}

// ---------- Hojas ----------

/** Crear (note == null) o editar una nota. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorSheet(
    note: Note?,
    onSave: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    onDismiss: () -> Unit,
) {
    val t = AppTheme.tokens
    var title by remember { mutableStateOf(note?.title ?: "") }
    var body by remember { mutableStateOf(note?.body ?: "") }
    val canSave = title.isNotBlank() || body.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = t.bg,
    ) {
        Column(
            Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionLabel(if (note == null) "NUEVA NOTA" else "EDITAR NOTA")
            PlainField(title, { title = it }, "Título", t.titleStyle(24.sp), singleLine = true)
            PlainField(
                body, { body = it }, "Escribe aquí…",
                TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
                singleLine = false, minHeight = 180.dp,
            )
            PrimaryButton("Guardar", {
                onSave(note?.copy(title = title.trim(), body = body.trim()) ?: Note(title = title.trim(), body = body.trim()))
            }, enabled = canSave)
            if (note != null) {
                Box(
                    Modifier.fillMaxWidth().clip(t.smallShape).clickable(role = Role.Button) { onDelete(note) }.padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) { UiText("Eliminar nota", 15.sp, FontWeight.ExtraBold, DangerRed) }
            }
        }
    }
}

@Composable
private fun PlainField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    style: TextStyle,
    singleLine: Boolean,
    minHeight: Dp = 0.dp,
) {
    val t = AppTheme.tokens
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = style.copy(color = t.text),
        cursorBrush = SolidColor(t.accent),
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) androidx.compose.material3.Text(placeholder, style = style, color = t.muted.copy(alpha = 0.7f))
                inner()
            }
        },
    )
}

/** Hoja del FAB de Inicio: crear nota o evento. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChooserSheet(onNewNote: () -> Unit, onNewEvent: () -> Unit, onDismiss: () -> Unit) {
    val t = AppTheme.tokens
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = t.bg) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Nueva nota" to onNewNote, "Nuevo evento" to onNewEvent).forEach { (label, action) ->
                Row(
                    Modifier.fillMaxWidth().styledCard(t).clickable(onClick = action).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TitleText(label, 18.sp, Modifier.weight(1f))
                    Spacer(Modifier.size(1.dp))
                }
            }
        }
    }
}
