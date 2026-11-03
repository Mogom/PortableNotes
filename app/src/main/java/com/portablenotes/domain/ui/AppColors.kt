package com.portablenotes.domain.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.portablenotes.domain.ui.theme.AppStyle
import com.portablenotes.domain.ui.theme.AppTokens
import com.portablenotes.domain.ui.theme.resolveTokens

/**
 * Colores para las hojas, diálogos y pantallas que aún usan los nombres antiguos
 * (detalle y editor de eventos, notificaciones, permisos, recuperar contraseña).
 *
 * Siguen el tema que el usuario elige en Temas: [com.portablenotes.domain.ui.theme.AppTheme]
 * llama a [update] y, como son estados de Compose, esas pantallas se redibujan solas.
 * Para código nuevo usa mejor `AppTheme.tokens`.
 */
object AppColors {
    private var t by mutableStateOf(resolveTokens(AppStyle()))

    internal fun update(tokens: AppTokens) {
        t = tokens
    }

    // --- Fondos ---
    val Background: Color get() = t.bg
    val BackgroundTop: Color get() = t.bg
    val Surface: Color get() = t.surf
    val SurfaceMuted: Color get() = t.surf2
    val Border: Color get() = t.border

    // --- Texto ---
    val TextPrimary: Color get() = t.text
    val TextSecondary: Color get() = t.muted

    // --- Acento ---
    val Accent: Color get() = t.accent
    val OnAccent: Color get() = t.onAccent

    val Error = Color(0xFFE5342B)

    // --- Fondos de los iconos de eventos ---
    val Tint1: Color get() = t.surf2
    val Tint2: Color get() = t.accent.copy(alpha = 0.18f)
    val Tint3: Color get() = t.surf2
}
