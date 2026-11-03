package com.portablenotes.domain.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.portablenotes.R
import com.portablenotes.domain.ui.AppColors

// ---------- Fuentes ----------

val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)
private val Bricolage = FontFamily(
    Font(R.font.bricolage_bold, FontWeight.Bold),
    Font(R.font.bricolage_extrabold, FontWeight.ExtraBold),
)
private val Unbounded = FontFamily(Font(R.font.unbounded_semibold, FontWeight.SemiBold))
private val InstrumentSerif = FontFamily(Font(R.font.instrument_serif, FontWeight.Normal))

/** Letra de títulos: familia, peso y espaciado. El texto de la interfaz siempre es Manrope. */
data class TitleFont(val family: FontFamily, val weight: FontWeight, val letterSpacing: TextUnit)

fun LetraTitulos.font(): TitleFont = when (this) {
    LetraTitulos.MODERNA -> TitleFont(Bricolage, FontWeight.ExtraBold, (-0.02).em)
    LetraTitulos.REDONDA -> TitleFont(Unbounded, FontWeight.SemiBold, (-0.02).em)
    LetraTitulos.CLASICA -> TitleFont(InstrumentSerif, FontWeight.Normal, (-0.01).em)
}

// ---------- Tokens resueltos ----------

/** Colores, formas y estilos ya calculados a partir de [AppStyle]. Se leen con `AppTheme.tokens`. */
@Immutable
data class AppTokens(
    val style: AppStyle,
    val paletteName: String,
    // Colores base
    val bg: Color,
    val surf: Color,
    val surf2: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val dark: Boolean,
    // Acentos
    val accent: Color,
    val onAccent: Color,
    val accent2: Color,
    val onAccent2: Color,
    // Formas
    val rCard: Dp,
    val rSmall: Dp,
    val cellShape: Shape,
    val rSeg: Dp,
    // Letra de títulos
    val title: TitleFont,
) {
    /** onAccent al 18 %: chips y pistas sobre el bloque de color. */
    val onSoft: Color get() = onAccent.copy(alpha = 0x2E / 255f)

    /** Sombra del FAB. */
    val glow: Color get() = accent.copy(alpha = 0x55 / 255f)

    val cardShape: Shape get() = RoundedCornerShape(rCard)
    val smallShape: Shape get() = RoundedCornerShape(rSmall)
    val segShape: Shape get() = RoundedCornerShape(rSeg)

    // Tarjetas según el estilo elegido
    val cardBg: Color get() = if (style.tarjeta == EstiloTarjeta.RELLENO) surf2 else surf
    val cardBorder: Color? get() = if (style.tarjeta == EstiloTarjeta.BORDE) border else null
    val cardShadow: Boolean get() = style.tarjeta == EstiloTarjeta.SOMBRA && !dark

    /** Título con la letra elegida. */
    fun titleStyle(size: TextUnit): TextStyle = TextStyle(
        fontFamily = title.family,
        fontWeight = title.weight,
        fontSize = size,
        letterSpacing = title.letterSpacing,
    )
}

fun resolveTokens(style: AppStyle): AppTokens {
    val p = paletteById(style.colorId)
    val f = style.fondo
    val acc = p.accentFor(f)
    val on = p.onAccentFor(f)

    data class Base(val bg: Color, val surf: Color, val surf2: Color, val text: Color, val muted: Color, val border: Color)
    val b = when (f) {
        Fondo.BLANCO -> Base(Color.White, Color.White, mix(acc, 12, Color.White), Color(0xFF16151A), Color(0xFF6E6C75), Color(0xFFECEBE8))
        Fondo.HUESO -> Base(Color(0xFFF5F4F0), Color.White, mix(acc, 14, Color.White), Color(0xFF16151A), Color(0xFF6E6C75), Color(0xFFE8E6E0))
        Fondo.TENIDO -> Base(mix(acc, 10, Color(0xFFFAFAF8)), Color.White, mix(acc, 16, Color.White), Color(0xFF16151A), Color(0xFF6E6C75), mix(acc, 20, Color.White))
        Fondo.OSCURO -> Base(Color(0xFF141418), Color(0xFF1F1F25), mix(acc, 22, Color(0xFF1F1F25)), Color(0xFFF3F2F7), Color(0xFFA3A1AD), Color(0xFF2C2C33))
        Fondo.NEGRO -> Base(Color.Black, Color(0xFF121214), mix(acc, 22, Color(0xFF121214)), Color(0xFFF5F5F5), Color(0xFF9C9CA3), Color(0xFF232326))
    }

    val (rCard, rSmall, cell, rSeg) = when (style.esquinas) {
        Esquinas.RECTAS -> listOf(10, 8, 10, 6)
        Esquinas.SUAVES -> listOf(20, 14, 12, 10)
        Esquinas.REDONDAS -> listOf(28, 18, -1, 14)
    }

    return AppTokens(
        style = style,
        paletteName = p.name,
        bg = b.bg, surf = b.surf, surf2 = b.surf2, text = b.text, muted = b.muted, border = b.border,
        dark = f.isDark,
        accent = acc,
        onAccent = on,
        // Sin segundo color, la app queda en un solo color: accent2 = surf2
        accent2 = if (style.segundoColor) p.accent2 else b.surf2,
        onAccent2 = if (style.segundoColor) p.onAccent2 else b.text,
        rCard = rCard.dp,
        rSmall = rSmall.dp,
        cellShape = if (cell < 0) CircleShape else RoundedCornerShape(cell.dp),
        rSeg = rSeg.dp,
        title = style.letra.font(),
    )
}

val LocalAppTokens = staticCompositionLocalOf { resolveTokens(AppStyle()) }

object AppTheme {
    val tokens: AppTokens
        @Composable get() = LocalAppTokens.current
}

private val ColorAnim = tween<Color>(250)

@Composable
private fun animated(c: Color, label: String): Color {
    val v by animateColorAsState(c, ColorAnim, label = label)
    return v
}

/**
 * Tema de la app. Anima los cambios de color (250 ms), los expone en [LocalAppTokens],
 * los pasa a Material 3 y a [AppColors] (que usan las hojas y diálogos del equipo).
 */
@Composable
fun AppTheme(style: AppStyle, content: @Composable () -> Unit) {
    val target = resolveTokens(style)
    val t = target.copy(
        bg = animated(target.bg, "bg"),
        surf = animated(target.surf, "surf"),
        surf2 = animated(target.surf2, "surf2"),
        text = animated(target.text, "text"),
        muted = animated(target.muted, "muted"),
        border = animated(target.border, "border"),
        accent = animated(target.accent, "accent"),
        onAccent = animated(target.onAccent, "onAccent"),
        accent2 = animated(target.accent2, "accent2"),
        onAccent2 = animated(target.onAccent2, "onAccent2"),
    )

    SideEffect { AppColors.update(t) }

    val scheme = if (t.dark) {
        darkColorScheme(
            primary = t.accent, onPrimary = t.onAccent, primaryContainer = t.accent, onPrimaryContainer = t.onAccent,
            secondary = t.accent, onSecondary = t.onAccent,
            background = t.bg, onBackground = t.text, surface = t.surf, onSurface = t.text,
            surfaceVariant = t.surf2, onSurfaceVariant = t.muted, outline = t.border, outlineVariant = t.border,
            surfaceContainerLowest = t.surf, surfaceContainerLow = t.surf, surfaceContainer = t.surf,
            surfaceContainerHigh = t.surf, surfaceContainerHighest = t.surf2,
            error = Color(0xFFE5342B),
        )
    } else {
        lightColorScheme(
            primary = t.accent, onPrimary = t.onAccent, primaryContainer = t.accent, onPrimaryContainer = t.onAccent,
            secondary = t.accent, onSecondary = t.onAccent,
            background = t.bg, onBackground = t.text, surface = t.surf, onSurface = t.text,
            surfaceVariant = t.surf2, onSurfaceVariant = t.muted, outline = t.border, outlineVariant = t.border,
            surfaceContainerLowest = t.surf, surfaceContainerLow = t.surf, surfaceContainer = t.surf,
            surfaceContainerHigh = t.surf, surfaceContainerHighest = t.surf2,
            error = Color(0xFFE5342B),
        )
    }

    val base = androidx.compose.material3.Typography()
    fun TextStyle.m() = copy(fontFamily = Manrope)
    val typography = androidx.compose.material3.Typography(
        displayLarge = base.displayLarge.m(), displayMedium = base.displayMedium.m(), displaySmall = base.displaySmall.m(),
        headlineLarge = base.headlineLarge.m(), headlineMedium = base.headlineMedium.m(), headlineSmall = base.headlineSmall.m(),
        titleLarge = base.titleLarge.m(), titleMedium = base.titleMedium.m(), titleSmall = base.titleSmall.m(),
        bodyLarge = base.bodyLarge.m(), bodyMedium = base.bodyMedium.m(), bodySmall = base.bodySmall.m(),
        labelLarge = base.labelLarge.m(), labelMedium = base.labelMedium.m(), labelSmall = base.labelSmall.m(),
    )

    CompositionLocalProvider(LocalAppTokens provides t) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
