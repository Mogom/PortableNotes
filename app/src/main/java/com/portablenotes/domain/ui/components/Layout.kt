package com.portablenotes.domain.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Espacio que ocupa la barra flotante desde el borde inferior:
 * 18 dp de margen + 66 dp de barra + 16 dp de aire, más la barra de navegación del sistema.
 * El FAB se coloca justo encima y el contenido deja este espacio libre al final del scroll.
 */
@Composable
fun barClearance(): Dp = 100.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

/** Espacio libre al final de cada pantalla con scroll (barra + FAB). */
@Composable
fun contentBottomPadding(): Dp = barClearance() + 16.dp

/** Barra de estado arriba y, en horizontal, el recorte de cámara a los lados. */
@Composable
fun Modifier.screenInsets(): Modifier =
    statusBarsPadding().windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))

/** Puntos de quiebre: teléfono < 600 dp ≤ tablet / horizontal < 900 dp ≤ tablet grande. */
object Breakpoints {
    val Medium = 600.dp
    val Expanded = 900.dp

    /** Ancho máximo del contenido en tablets, para que no se estire de borde a borde. */
    val MaxContent = 1100.dp
}

/** Columnas de una grilla según el ancho disponible (2 en teléfono, 3 en tablet, 4 en tablet grande). */
fun gridColumns(width: Dp): Int = when {
    width >= Breakpoints.Expanded -> 4
    width >= Breakpoints.Medium -> 3
    else -> 2
}
