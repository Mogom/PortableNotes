package com.portablenotes.domain.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.theme.AppTheme
import com.portablenotes.domain.ui.theme.AppTokens
import com.portablenotes.domain.ui.theme.Manrope

/** Rojo de "Cerrar sesión" y errores. */
val DangerRed = Color(0xFFE5342B)

// ---------- Texto ----------

/** Título con la letra elegida en Estilo. */
@Composable
fun TitleText(
    text: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.tokens.text,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(text, modifier = modifier, style = AppTheme.tokens.titleStyle(size), color = color, maxLines = maxLines)
}

/** Texto de interfaz en Manrope. */
@Composable
fun UiText(
    text: String,
    size: TextUnit,
    weight: FontWeight = FontWeight.SemiBold,
    color: Color = AppTheme.tokens.text,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = TextStyle.Default,
) {
    Text(
        text,
        modifier = modifier,
        style = style.copy(fontFamily = Manrope, fontWeight = weight, fontSize = size),
        color = color,
        maxLines = maxLines,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
    )
}

/** "COLOR", "FONDO", "TARJETAS"… */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        fontFamily = Manrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        letterSpacing = 0.06.em,
        color = AppTheme.tokens.muted,
    )
}

// ---------- Tarjetas ----------

/** Fondo, borde y sombra de tarjeta según Estilo › Tarjetas. */
fun Modifier.styledCard(t: AppTokens, shape: Shape = t.cardShape): Modifier {
    var m = this
    if (t.cardShadow) {
        m = m.dropShadow(shape, Shadow(radius = 22.dp, color = Color(0x1214141E), offset = DpOffset(0.dp, 6.dp)))
    }
    m = m.clip(shape).background(t.cardBg)
    t.cardBorder?.let { m = m.border(1.5.dp, it, shape) }
    return m
}

@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    shape: Shape = AppTheme.tokens.cardShape,
    content: @Composable () -> Unit,
) {
    Box(modifier.styledCard(AppTheme.tokens, shape)) { content() }
}

/** Botón flotante "+" con sombra del color de acento. */
@Composable
fun AppFab(contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = AppTheme.tokens
    Box(
        modifier
            .size(58.dp)
            .then(
                if (t.dark) Modifier
                else Modifier.dropShadow(t.smallShape, Shadow(radius = 28.dp, color = t.glow, offset = DpOffset(0.dp, 14.dp)))
            )
            .clip(t.smallShape)
            .background(t.accent)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Add, contentDescription, tint = t.onAccent, modifier = Modifier.size(26.dp))
    }
}

/** Botón cuadrado "+" de las cabeceras (Rutinas, Tareas). */
@Composable
fun AddSquareButton(contentDescription: String, onClick: () -> Unit) {
    val t = AppTheme.tokens
    Box(
        Modifier.size(48.dp).clip(t.smallShape).background(t.accent).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Add, contentDescription, tint = t.onAccent, modifier = Modifier.size(22.dp))
    }
}

/** Botón redondo de 40 dp (volver, etc.). */
@Composable
fun RoundIconButton(icon: ImageVector, contentDescription: String, background: Color, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(background).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ---------- Controles ----------

/** Interruptor de 50×30 dp: pista acento / surf2, círculo blanco. */
@Composable
fun AppSwitch(checked: Boolean) {
    val t = AppTheme.tokens
    val track by animateColorAsState(if (checked) t.accent else t.surf2, label = "track")
    val x by animateDpAsState(if (checked) 20.dp else 0.dp, label = "thumb")
    Box(Modifier.size(width = 50.dp, height = 30.dp).clip(CircleShape).background(track).padding(3.dp)) {
        Box(
            Modifier.offset(x = x).size(24.dp).shadow(2.dp, CircleShape).background(Color.White, CircleShape)
        )
    }
}

/** Fila de tarjeta con título, descripción y switch; toda la fila se puede tocar. */
@Composable
fun SwitchCard(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val t = AppTheme.tokens
    Row(
        modifier
            .fillMaxWidth()
            .styledCard(t)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            UiText(title, 15.sp, FontWeight.ExtraBold)
            UiText(subtitle, 13.sp, FontWeight.Medium, t.muted)
        }
        Spacer(Modifier.width(12.dp))
        AppSwitch(checked)
    }
}

/** Check circular: hecho = acento con ✓; pendiente = solo borde. */
@Composable
fun CheckDot(done: Boolean, size: Dp = 24.dp, onClick: (() -> Unit)? = null) {
    val t = AppTheme.tokens
    val bg by animateColorAsState(if (done) t.accent else Color.Transparent, label = "chk")
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .border(2.dp, if (done) t.accent else t.border, CircleShape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Checkbox, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Icon(Icons.Rounded.Check, null, tint = t.onAccent, modifier = Modifier.size(size * 0.6f))
    }
}

/** Control segmentado (Estilo): pista surf2, opción activa en surf con sombra suave. */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    height: Dp = 40.dp,
    textStyle: @Composable (T) -> TextStyle = { TextStyle(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) },
) {
    val t = AppTheme.tokens
    Row(
        Modifier.fillMaxWidth().clip(t.smallShape).background(t.surf2).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { o ->
            val on = o == selected
            SegmentOption(on, height, { onSelect(o) }) {
                Text(label(o), style = textStyle(o), color = if (on) t.text else t.muted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun RowScope.SegmentOption(on: Boolean, height: Dp, onClick: () -> Unit, content: @Composable () -> Unit) {
    val t = AppTheme.tokens
    val shape = t.segShape
    Box(
        Modifier
            .weight(1f)
            .height(height)
            .then(if (on && !t.dark) Modifier.shadow(2.dp, shape) else Modifier)
            .clip(shape)
            .background(if (on) (if (t.dark) t.surf2 else t.surf) else Color.Transparent)
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Chip tipo pastilla (categorías, "Ver mes", "Estilo ›"). */
@Composable
fun PillChip(
    label: String,
    background: Color,
    content: Color,
    border: Color? = null,
    onClick: (() -> Unit)? = null,
    trailing: ImageVector? = null,
) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(background)
            .then(if (border != null) Modifier.border(1.5.dp, border, CircleShape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 15.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        UiText(label, 14.sp, FontWeight.ExtraBold, content, maxLines = 1)
        if (trailing != null) Icon(trailing, null, tint = content, modifier = Modifier.size(14.dp))
    }
}

/** Barra de progreso redondeada. */
@Composable
fun ProgressBar(progress: Float, track: Color, fill: Color, height: Dp = 6.dp, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(track)) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(CircleShape)
                .background(fill)
        )
    }
}

// ---------- Campos de formulario ----------

/** Campo de 52–54 dp: fondo surf, borde 1.5 dp, icono inicial. */
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    height: Dp = 54.dp,
    isPassword: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    val t = AppTheme.tokens
    var visible by rememberSaveable { mutableStateOf(false) }
    val borderColor = if (errorText != null) DangerRed else t.border
    Column(modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = t.text),
            cursorBrush = SolidColor(t.accent),
            visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(height)
                        .clip(t.smallShape)
                        .background(t.surf)
                        .border(1.5.dp, borderColor, t.smallShape)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(icon, null, tint = t.muted, modifier = Modifier.size(19.dp))
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) UiText(placeholder, 15.sp, FontWeight.Medium, t.muted, maxLines = 1)
                        inner()
                    }
                    trailing?.invoke()
                    if (isPassword) {
                        Icon(
                            if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = t.muted,
                            modifier = Modifier.size(19.dp).clip(CircleShape).clickable { visible = !visible },
                        )
                    }
                }
            },
        )
        if (errorText != null) {
            UiText(errorText, 12.sp, FontWeight.SemiBold, DangerRed, Modifier.padding(start = 6.dp, top = 4.dp))
        }
    }
}

/** Botón principal de 56 dp en acento. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, loading: Boolean = false) {
    val t = AppTheme.tokens
    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(t.smallShape)
            .background(if (enabled) t.accent else t.accent.copy(alpha = 0.45f))
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            androidx.compose.material3.CircularProgressIndicator(Modifier.size(24.dp), color = t.onAccent, strokeWidth = 2.5.dp)
        } else {
            UiText(text, 16.sp, FontWeight.ExtraBold, t.onAccent)
        }
    }
}

/** "Continuar con Google": fondo surf2. */
@Composable
fun GoogleButton(onClick: () -> Unit, enabled: Boolean = true) {
    val t = AppTheme.tokens
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(t.smallShape)
            .background(t.surf2)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            androidx.compose.ui.res.painterResource(com.portablenotes.R.drawable.ic_google_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        UiText("Continuar con Google", 15.sp, FontWeight.Bold)
    }
}
