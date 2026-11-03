package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.components.DangerRed
import com.portablenotes.domain.ui.components.PillChip
import com.portablenotes.domain.ui.components.RoundIconButton
import com.portablenotes.domain.ui.components.SectionLabel
import com.portablenotes.domain.ui.components.SegmentedControl
import com.portablenotes.domain.ui.components.SwitchCard
import com.portablenotes.domain.ui.components.TitleText
import com.portablenotes.domain.ui.components.UiText
import com.portablenotes.domain.ui.components.styledCard
import com.portablenotes.domain.ui.theme.AppStyle
import com.portablenotes.domain.ui.theme.AppTheme
import com.portablenotes.domain.ui.theme.EncabezadoInicio
import com.portablenotes.domain.ui.theme.Esquinas
import com.portablenotes.domain.ui.theme.EstiloBarra
import com.portablenotes.domain.ui.theme.EstiloTarjeta
import com.portablenotes.domain.ui.theme.Fondo
import com.portablenotes.domain.ui.theme.LetraTitulos
import com.portablenotes.domain.ui.theme.Palettes
import com.portablenotes.domain.ui.theme.accentFor
import com.portablenotes.domain.ui.theme.font
import com.portablenotes.domain.ui.theme.swatch

/**
 * Temas 5a: vista previa del tema actual, 12 colores, 5 fondos y "Segundo color".
 * "Estilo ›" abre la pantalla de Estilo. Todo se aplica al instante en toda la app.
 */
@Composable
fun ThemesScreen(
    onChange: ((AppStyle) -> AppStyle) -> Unit,
    onOpenStyle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = AppTheme.tokens
    val style = t.style

    ScreenColumn(modifier) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TitleText("Temas", 28.sp, Modifier.weight(1f))
            PillChip("Estilo", t.surf2, t.text, onClick = onOpenStyle, trailing = Icons.AutoMirrored.Rounded.KeyboardArrowRight)
        }

        // Vista previa
        Box(
            Modifier.fillMaxWidth().height(150.dp).clip(t.cardShape).background(t.accent).padding(18.dp),
        ) {
            Column(
                Modifier.align(Alignment.TopEnd).size(width = 118.dp, height = 84.dp).rotate(6f)
                    .clip(t.smallShape).background(t.cardBg).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(Modifier.fillMaxWidth(0.6f).height(9.dp).clip(CircleShape).background(t.accent))
                Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(t.surf2))
                Box(Modifier.fillMaxWidth(0.7f).height(7.dp).clip(CircleShape).background(t.surf2))
            }
            Box(Modifier.align(Alignment.TopEnd).offset((-100).dp, 30.dp).size(50.dp).clip(t.cellShape).background(t.accent2))
            Column(Modifier.align(Alignment.BottomStart)) {
                UiText("Tema actual", 13.sp, FontWeight.ExtraBold, t.onAccent.copy(alpha = 0.85f))
                TitleText(t.paletteName, 30.sp, color = t.onAccent)
            }
        }

        // Color
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("COLOR")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Palettes.chunked(6).forEach { row ->
                    Row(Modifier.fillMaxWidth()) {
                        row.forEach { p ->
                            val on = p.id == style.colorId
                            Column(
                                Modifier.weight(1f).selectable(on, role = Role.RadioButton) { onChange { it.copy(colorId = p.id) } },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box {
                                    SelectRing(on, t.cellShape, 44.dp) {
                                        Box(Modifier.size(44.dp).clip(t.cellShape).background(p.accentFor(style.fondo)))
                                    }
                                    if (style.segundoColor) {
                                        Box(
                                            Modifier.align(Alignment.BottomEnd).offset(3.dp, 3.dp).size(18.dp)
                                                .clip(CircleShape).background(t.bg).padding(2.5.dp).clip(CircleShape).background(p.accent2)
                                        )
                                    }
                                }
                                UiText(p.name, 10.sp, if (on) FontWeight.ExtraBold else FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Fondo
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("FONDO")
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Fondo.entries.forEach { f ->
                    val on = f == style.fondo
                    Column(
                        Modifier.selectable(on, role = Role.RadioButton) { onChange { it.copy(fondo = f) } },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SelectRing(on, t.smallShape, 48.dp) {
                            Box(
                                Modifier.size(48.dp).clip(t.smallShape).background(f.swatch(t.accent))
                                    .border(1.5.dp, if (t.dark) Color(0xFF3A3A42) else Color(0xFFDAD8D2), t.smallShape)
                            )
                        }
                        UiText(f.label, 11.sp, if (on) FontWeight.ExtraBold else FontWeight.SemiBold)
                    }
                }
            }
        }

        SwitchCard(
            title = "Segundo color",
            subtitle = "Combina tu color con otro que le queda",
            checked = style.segundoColor,
            onChange = { v -> onChange { it.copy(segundoColor = v) } },
        )
    }
}

/**
 * Estilo 5a: Tarjetas, Esquinas, Letra de títulos, Barra de navegación, Encabezado de Inicio,
 * y una tarjeta con Notificaciones, Restablecer estilo y Cerrar sesión.
 */
@Composable
fun StyleScreen(
    onChange: ((AppStyle) -> AppStyle) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = AppTheme.tokens
    val s = t.style

    ScreenColumn(modifier) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Volver", t.surf2, t.text, onBack)
            Spacer(Modifier.width(12.dp))
            TitleText("Estilo", 28.sp)
        }

        Option("TARJETAS") {
            SegmentedControl(EstiloTarjeta.entries, s.tarjeta, { it.label }, { v -> onChange { it.copy(tarjeta = v) } })
        }
        Option("ESQUINAS") {
            SegmentedControl(Esquinas.entries, s.esquinas, { it.label }, { v -> onChange { it.copy(esquinas = v) } })
        }
        Option("LETRA DE TÍTULOS") {
            // Cada opción se escribe en su propia fuente
            SegmentedControl(
                LetraTitulos.entries, s.letra, { it.label }, { v -> onChange { it.copy(letra = v) } },
                height = 44.dp,
                textStyle = { l -> TextStyle(fontFamily = l.font().family, fontWeight = l.font().weight, fontSize = 16.sp) },
            )
        }
        Option("BARRA DE NAVEGACIÓN") {
            SegmentedControl(EstiloBarra.entries, s.barra, { it.label }, { v -> onChange { it.copy(barra = v) } })
        }
        Option("ENCABEZADO DE INICIO") {
            SegmentedControl(EncabezadoInicio.entries, s.encabezado, { it.label }, { v -> onChange { it.copy(encabezado = v) } })
        }

        Column(Modifier.fillMaxWidth().styledCard(t)) {
            ActionRow(Icons.Rounded.Notifications, "Notificaciones", t.text, onOpenNotifications)
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.border))
            ActionRow(Icons.Rounded.RestartAlt, "Restablecer estilo", t.text, onReset)
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.border))
            ActionRow(Icons.AutoMirrored.Rounded.Logout, "Cerrar sesión", DangerRed, onSignOut)
        }
    }
}

// ---------- Piezas ----------

@Composable
private fun ScreenColumn(modifier: Modifier, content: @Composable () -> Unit) {
    val t = AppTheme.tokens
    Box(modifier.fillMaxSize().background(t.bg)) {
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content() }
    }
}

@Composable
private fun Option(label: String, control: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(label)
        control()
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(19.dp))
        UiText(label, 15.sp, FontWeight.ExtraBold, color)
    }
}

/** Anillo doble de selección: 3 dp del fondo + 2.5 dp del color de texto. */
@Composable
private fun SelectRing(selected: Boolean, shape: Shape, size: Dp, content: @Composable () -> Unit) {
    val t = AppTheme.tokens
    val pad = 5.5.dp
    Box(
        Modifier
            .size(size + pad * 2)
            .then(if (selected) Modifier.border(2.5.dp, t.text, ringShape(shape)) else Modifier)
            .padding(pad),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** El anillo sigue la forma de la muestra, un poco más grande. */
@Composable
private fun ringShape(shape: Shape): Shape {
    val t = AppTheme.tokens
    return if (shape == t.cellShape && t.style.esquinas == Esquinas.REDONDAS) CircleShape
    else androidx.compose.foundation.shape.RoundedCornerShape(
        (if (shape == t.cellShape) cellRadius(t.style.esquinas) else t.rSmall) + 5.5.dp
    )
}

private fun cellRadius(e: Esquinas): Dp = when (e) {
    Esquinas.RECTAS -> 10.dp
    Esquinas.SUAVES -> 12.dp
    Esquinas.REDONDAS -> 22.dp
}
