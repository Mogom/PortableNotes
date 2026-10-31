package com.portablenotes.domain.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Paleta oscura basada en la imagen de referencia:
 *   001512 Rich black · 04272D Gunmetal · 8D9997 Cool gray · FFFFFF White · 02CDAD Turquesa
 * Los nombres se mantienen, así que el resto de la app no necesita cambios.
 */
object AppColors {
    // --- Colores base de la referencia ---
    private val RichBlack = Color(0xFF001512)
    private val Gunmetal = Color(0xFF04272D)
    private val CoolGray = Color(0xFF8D9997)
    private val White = Color(0xFFFFFFFF)
    private val Turquoise = Color(0xFF02CDAD)

    // --- Fondos ---
    val Background = RichBlack
    val BackgroundTop = RichBlack          // pon Gunmetal aquí si quieres degradado hacia arriba
    val Surface = Gunmetal                   // tarjetas, calendario, barra inferior
    val SurfaceMuted = Color(0xFF12333A)     // chips, botones circulares, pills (Gunmetal + 10 % gris)
    val Border = Color(0xFF1E4249)           // bordes finos de tarjetas

    // --- Texto ---
    val TextPrimary = White
    val TextSecondary = CoolGray

    // --- Acento (día seleccionado, tab activo, botones, check) ---
    val Accent = Turquoise
    val OnAccent = RichBlack                 // texto/íconos sobre el turquesa

    // --- Tonos para iconos de eventos (derivados del gunmetal / turquesa) ---
    val Tint1 = Color(0xFF0E3A40)
    val Tint2 = Color(0xFF0B4F4F)            // prioridad alta: un poco más turquesa
    val Tint3 = Color(0xFF0A3238)
}

/**
 * Tema Material en oscuro. Es necesario para que los diálogos (selector de fecha y hora),
 * los campos de texto y las hojas usen esta paleta y no la clara por defecto.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Accent,
        onPrimary = _root_ide_package_.com.portablenotes.domain.ui.AppColors.OnAccent,
        primaryContainer = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Accent,
        onPrimaryContainer = _root_ide_package_.com.portablenotes.domain.ui.AppColors.OnAccent,
        secondary = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Accent,
        onSecondary = _root_ide_package_.com.portablenotes.domain.ui.AppColors.OnAccent,
        background = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Background,
        onBackground = _root_ide_package_.com.portablenotes.domain.ui.AppColors.TextPrimary,
        surface = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Surface,
        onSurface = _root_ide_package_.com.portablenotes.domain.ui.AppColors.TextPrimary,
        surfaceVariant = _root_ide_package_.com.portablenotes.domain.ui.AppColors.SurfaceMuted,
        onSurfaceVariant = _root_ide_package_.com.portablenotes.domain.ui.AppColors.TextSecondary,
        outline = _root_ide_package_.com.portablenotes.domain.ui.AppColors.TextSecondary,
        outlineVariant = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Border,
        surfaceContainerLow = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Surface,
        surfaceContainer = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Surface,
        surfaceContainerHigh = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Surface,
        surfaceContainerHighest = _root_ide_package_.com.portablenotes.domain.ui.AppColors.SurfaceMuted,
    )
    MaterialTheme(colorScheme = scheme, content = content)
}