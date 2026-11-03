package com.portablenotes.domain.ui.theme

import androidx.compose.ui.graphics.Color

// ---------- Opciones que el usuario elige en Temas y Estilo ----------

enum class Fondo(val label: String) { BLANCO("Blanco"), HUESO("Hueso"), TENIDO("Teñido"), OSCURO("Oscuro"), NEGRO("Negro") }
enum class EstiloTarjeta(val label: String) { SOMBRA("Sombra"), BORDE("Borde"), RELLENO("Relleno") }
enum class Esquinas(val label: String) { RECTAS("Rectas"), SUAVES("Suaves"), REDONDAS("Redondas") }
enum class LetraTitulos(val label: String) { MODERNA("Moderna"), REDONDA("Redonda"), CLASICA("Clásica") }
enum class EstiloBarra(val label: String) { SOLO_ICONOS("Solo iconos"), CON_NOMBRE("Con nombre") }
enum class EncabezadoInicio(val label: String) { BLOQUE_COLOR("Bloque de color"), TARJETA("Tarjeta") }

/** Todo lo que el usuario personaliza. Se guarda con DataStore (ver StyleViewModel). */
data class AppStyle(
    val colorId: String = "oceano",
    val fondo: Fondo = Fondo.BLANCO,
    val segundoColor: Boolean = false,
    val tarjeta: EstiloTarjeta = EstiloTarjeta.SOMBRA,
    val esquinas: Esquinas = Esquinas.REDONDAS,
    val letra: LetraTitulos = LetraTitulos.MODERNA,
    val barra: EstiloBarra = EstiloBarra.SOLO_ICONOS,
    val encabezado: EncabezadoInicio = EncabezadoInicio.BLOQUE_COLOR,
)

// ---------- Paleta de 12 colores ----------

data class Palette(
    val id: String,
    val name: String,
    val accent: Color,
    val onAccent: Color,
    val accent2: Color,
    val onAccent2: Color,
)

val Palettes = listOf(
    Palette("oceano", "Océano", Color(0xFF0A6CFF), Color(0xFFFFFFFF), Color(0xFFFFC542), Color(0xFF1A1300)),
    Palette("fucsia", "Fucsia", Color(0xFFFF2E93), Color(0xFFFFFFFF), Color(0xFFFFD60A), Color(0xFF1A1300)),
    Palette("menta", "Menta", Color(0xFF0E9C80), Color(0xFFFFFFFF), Color(0xFFFFB547), Color(0xFF1A1300)),
    Palette("lavanda", "Lavanda", Color(0xFF6C4DF5), Color(0xFFFFFFFF), Color(0xFFFF8FB1), Color(0xFF2A0A16)),
    Palette("sol", "Sol", Color(0xFFFFB800), Color(0xFF2A1E00), Color(0xFFFF6B3D), Color(0xFFFFFFFF)),
    Palette("bosque", "Bosque", Color(0xFF2F7D4F), Color(0xFFFFFFFF), Color(0xFFE8A33D), Color(0xFF1A1300)),
    Palette("coral", "Coral", Color(0xFFFF6B5B), Color(0xFF2A0E0A), Color(0xFFFFD166), Color(0xFF1A1300)),
    Palette("noche", "Noche", Color(0xFF4F5BFF), Color(0xFFFFFFFF), Color(0xFFFF7AC6), Color(0xFF2A0A1E)),
    Palette("electrico", "Eléctrico", Color(0xFF2B3BFF), Color(0xFFFFFFFF), Color(0xFFC6F432), Color(0xFF111111)),
    Palette("mandarina", "Mandarina", Color(0xFFFF6A00), Color(0xFF111111), Color(0xFF2B3BFF), Color(0xFFFFFFFF)),
    Palette("turquesa", "Turquesa", Color(0xFF00B89A), Color(0xFF04201B), Color(0xFFFF2E93), Color(0xFFFFFFFF)),
    Palette("carbon", "Carbón", Color(0xFF1C1C1C), Color(0xFFFFFFFF), Color(0xFFFF5A36), Color(0xFFFFFFFF)),
)

fun paletteById(id: String): Palette = Palettes.firstOrNull { it.id == id } ?: Palettes.first()

/** Carbón es casi negro: en fondos oscuros se invierte para que se vea. */
fun Palette.accentFor(fondo: Fondo): Color =
    if (id == "carbon" && fondo.isDark) Color(0xFFF2F2F2) else accent

fun Palette.onAccentFor(fondo: Fondo): Color =
    if (id == "carbon" && fondo.isDark) Color(0xFF111111) else onAccent

val Fondo.isDark: Boolean get() = this == Fondo.OSCURO || this == Fondo.NEGRO

/** Color de la muestra en Temas › Fondo. */
fun Fondo.swatch(accent: Color): Color = when (this) {
    Fondo.BLANCO -> Color(0xFFFFFFFF)
    Fondo.HUESO -> Color(0xFFF5F4F0)
    Fondo.TENIDO -> mix(accent, 18, Color(0xFFFAFAF8))
    Fondo.OSCURO -> Color(0xFF1F1F25)
    Fondo.NEGRO -> Color(0xFF000000)
}

/** Mezcla `p` % del acento sobre `base`. */
fun mix(accent: Color, p: Int, base: Color): Color = androidx.compose.ui.graphics.lerp(base, accent, p / 100f)
