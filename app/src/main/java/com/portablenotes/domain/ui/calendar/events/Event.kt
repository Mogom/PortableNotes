package com.portablenotes.domain.ui.calendar.events

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class EventType(val label: String) {
    Work("Trabajo"),
    Leisure("Ocio"),
    Commitment("Compromiso"),
    Relationships("Relaciones"),
    Family("Familiar"),
    Health("Salud"),
    Other("Otro"),
}

enum class Priority(val label: String, val level: Int) {
    High("Alta", 3),
    Medium("Media", 2),
    Low("Baja", 1),
}

enum class Repeat(val label: String) {
    None("No se repite"),
    Daily("Todos los días"),
    Weekly("Cada semana"),
    Monthly("Cada mes"),
    Yearly("Cada año"),
}

data class Event(
    val id: String,
    val title: String,
    val date: LocalDate,          // fecha de inicio (o única fecha si no se repite)
    val time: LocalTime,
    val repeat: Repeat = Repeat.None,
    val type: EventType = EventType.Other,
    val priority: Priority = Priority.Medium,
    val notes: String = "",
    val done: Boolean = false,    // solo aplica si no se repite
    val routineId: String? = null, // rutina anclada (opcional)
) {
    fun occursOn(day: LocalDate): Boolean {
        if (day.isBefore(date)) return false
        return when (repeat) {
            Repeat.None -> day == date
            Repeat.Daily -> true
            Repeat.Weekly -> day.dayOfWeek == date.dayOfWeek
            // Si el día no existe en el mes (ej. 31), cae en el último día del mes
            Repeat.Monthly -> day.dayOfMonth == minOf(date.dayOfMonth, day.lengthOfMonth())
            Repeat.Yearly -> day.month == date.month &&
                    day.dayOfMonth == minOf(date.dayOfMonth, day.lengthOfMonth())
        }
    }
}

// ---------- Helpers de consulta ----------

fun List<Event>.on(day: LocalDate): List<Event> =
    filter { it.occursOn(day) }.sortedBy { it.time }

fun List<Event>.daysWithEvents(month: YearMonth): Set<LocalDate> =
    (1..month.lengthOfMonth()).map { month.atDay(it) }.filter { d -> any { it.occursOn(d) } }.toSet()

// ---------- Formato en español ----------

private val ES: Locale = Locale.forLanguageTag("es-ES")

fun LocalDate.prettyLong(): String =
    format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM",
        ES
    )).replaceFirstChar { it.titlecase(ES) }

fun YearMonth.monthName(): String =
    month.getDisplayName(TextStyle.FULL,
        ES
    ).replaceFirstChar { it.titlecase(
        ES
    ) }

fun LocalTime.hhmm(): String = format(DateTimeFormatter.ofPattern("HH:mm"))

// ---------- Datos de ejemplo ----------

fun sampleEvents(today: LocalDate): List<Event> = listOf(
    Event(
        "1",
        "Reunión de equipo",
        today,
        LocalTime.of(9, 0),
        Repeat.Weekly,
        EventType.Work,
        Priority.High,
        "Revisar avances del sprint"
    ),
    Event(
        "2",
        "Entrenamiento",
        today,
        LocalTime.of(18, 30),
        Repeat.Daily,
        EventType.Health,
        Priority.Medium
    ),
    Event(
        "3",
        "Cena con familia",
        today.plusDays(2),
        LocalTime.of(20, 0),
        Repeat.None,
        EventType.Family,
        Priority.Medium,
        "Llevar postre"
    ),
    Event(
        "4",
        "Entregar informe",
        today.plusDays(4),
        LocalTime.of(11, 0),
        Repeat.None,
        EventType.Commitment,
        Priority.High
    ),
    Event(
        "5",
        "Cine con amigos",
        today.plusDays(6),
        LocalTime.of(19, 15),
        Repeat.None,
        EventType.Leisure,
        Priority.Low
    ),
    Event(
        "6",
        "Cumpleaños de Ana",
        today.plusDays(9),
        LocalTime.of(8, 0),
        Repeat.Yearly,
        EventType.Relationships,
        Priority.Medium
    ),
    Event(
        "7",
        "Pago de arriendo",
        today.plusDays(12),
        LocalTime.of(10, 0),
        Repeat.Monthly,
        EventType.Commitment,
        Priority.High
    ),
)