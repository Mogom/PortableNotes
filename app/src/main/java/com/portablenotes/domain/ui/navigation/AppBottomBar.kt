package com.portablenotes.domain.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.theme.AppTheme
import com.portablenotes.domain.ui.theme.EstiloBarra
import com.portablenotes.domain.ui.theme.EstiloTarjeta
import com.portablenotes.domain.ui.theme.Manrope

/**
 * Barra flotante del diseño 5a: pastilla `surf` de 66 dp con 6 pestañas.
 * "Solo iconos": activo = círculo de acento. "Con nombre": el activo se expande con su nombre.
 * El borde aparece solo con Estilo › Tarjetas › Borde; la sombra, solo en fondos claros.
 */
@Composable
fun AppBottomBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = AppTheme.tokens
    var m = modifier.fillMaxWidth().height(66.dp)
    if (!t.dark) m = m.dropShadow(CircleShape, Shadow(radius = 30.dp, color = Color(0x1A14141E), offset = DpOffset(0.dp, 10.dp)))
    m = m.clip(CircleShape).background(t.surf)
    if (t.style.tarjeta == EstiloTarjeta.BORDE) m = m.border(1.5.dp, t.border, CircleShape)

    Row(
        m.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTab.entries.forEach { tab ->
            BarItem(tab, tab == selected, t.style.barra == EstiloBarra.CON_NOMBRE) { onSelect(tab) }
        }
    }
}

@Composable
private fun BarItem(tab: AppTab, isSelected: Boolean, withName: Boolean, onClick: () -> Unit) {
    val t = AppTheme.tokens
    val bg by animateColorAsState(if (isSelected) t.accent else Color.Transparent, tween(180), label = "navBg")
    val fg by animateColorAsState(if (isSelected) t.onAccent else t.muted, tween(180), label = "navFg")
    val expanded = withName && isSelected

    Row(
        Modifier
            .height(48.dp)
            .widthIn(min = 46.dp)
            .clip(CircleShape)
            .background(bg)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = if (expanded) 16.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = fg, modifier = Modifier.size(21.dp))
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(150)) + expandHorizontally(tween(180)),
            exit = fadeOut(tween(100)) + shrinkHorizontally(tween(180)),
        ) {
            Row {
                Spacer(Modifier.width(6.dp))
                Text(tab.label, color = fg, fontFamily = Manrope, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            }
        }
    }
}
