package com.portablenotes.domain.ui.routines

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Work
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

/** Categorías del requisito: Diaria, GYM, Lectura, Series, Juegos, Ejercicio y Personalizada. */
enum class RoutineCategory(val label: String, val defaultIconKey: String) {
    Daily("Diaria", "sun"),
    Gym("GYM", "gym"),
    Reading("Lectura", "book"),
    Series("Series", "tv"),
    Games("Juegos", "games"),
    Exercise("Ejercicio", "run"),
    Custom("Personalizada", "star"),
}

/**
 * Íconos disponibles. Se guarda la clave (String), no el ImageVector,
 * para poder persistirla en Room más adelante.
 * Requiere: androidx.compose.material:material-icons-extended
 */
object RoutineIcons {
    val all: Map<String, ImageVector> = linkedMapOf(
        "sun" to Icons.Rounded.WbSunny,
        "gym" to Icons.Rounded.FitnessCenter,
        "book" to Icons.Rounded.MenuBook,
        "tv" to Icons.Rounded.Tv,
        "games" to Icons.Rounded.SportsEsports,
        "run" to Icons.Rounded.DirectionsRun,
        "star" to Icons.Rounded.Star,
        "heart" to Icons.Rounded.Favorite,
        "work" to Icons.Rounded.Work,
        "school" to Icons.Rounded.School,
        "food" to Icons.Rounded.Restaurant,
        "sleep" to Icons.Rounded.Bedtime,
        "calm" to Icons.Rounded.SelfImprovement,
        "music" to Icons.Rounded.MusicNote,
        "art" to Icons.Rounded.Brush,
        "code" to Icons.Rounded.Code,
        "pets" to Icons.Rounded.Pets,
        "coffee" to Icons.Rounded.LocalCafe,
        "money" to Icons.Rounded.Savings,
        "travel" to Icons.Rounded.Flight,
    )
}

fun iconFor(key: String): ImageVector = RoutineIcons.all[key] ?: Icons.Rounded.Star

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val category: RoutineCategory = RoutineCategory.Daily,
    /** Automático según la categoría; solo "Personalizada" permite elegirlo. */
    val iconKey: String = category.defaultIconKey,
)

class RoutinesState(initial: List<Routine> = emptyList()) {
    val routines = mutableStateListOf<Routine>().apply { addAll(initial) }

    fun find(id: String?): Routine? = id?.let { rid -> routines.firstOrNull { it.id == rid } }

    fun upsert(routine: Routine) {
        val i = routines.indexOfFirst { it.id == routine.id }
        if (i >= 0) routines[i] = routine else routines.add(routine)
    }

    /** Los eventos que la tenían anclada simplemente dejan de mostrarla. */
    fun delete(id: String) {
        routines.removeAll { it.id == id }
    }
}

/**
 * Lista de rutinas disponible para toda la UI (editor y detalle de eventos).
 * Se provee una sola vez en App() con CompositionLocalProvider.
 */
val LocalRoutines = compositionLocalOf<List<Routine>> { emptyList() }

fun sampleRoutines(): List<Routine> = listOf(
    Routine("r1", "Rutina matutina", "Agua, estiramiento 5 min, revisar agenda del día.", RoutineCategory.Daily),
    Routine("r2", "Pecho y espalda", "4x10 press banca · 4x10 remo · 3x12 aperturas · 10 min de cardio.", RoutineCategory.Gym),
    Routine("r3", "Leer 20 minutos", "Sin celular. Anotar una idea del capítulo.", RoutineCategory.Reading),
    Routine("r4", "Maratón de series", "Máximo 2 capítulos entre semana.", RoutineCategory.Series),
    Routine("r5", "Correr 5K", "Calentar 5 min, ritmo suave, estirar al terminar.", RoutineCategory.Exercise),
)