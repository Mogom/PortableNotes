package com.portablenotes.domain.models.events

import java.time.LocalDate

/** Otro evento que cae a la misma hora que el evento que se está creando, y el primer día en que coinciden. */
data class EventConflict(val other: Event, val date: LocalDate)

/**
 * Eventos que chocan con [candidate]: ocurren el mismo día a la misma hora.
 * Como los eventos solo tienen hora de inicio (sin duración), "chocar" significa empezar a la misma hora.
 * Tiene en cuenta eventos repetidos (diario, semanal...) mirando [horizonDays] días hacia adelante.
 * Se ignoran el propio evento (al editar) y los eventos únicos ya marcados como hechos.
 */
fun findConflicts(candidate: Event, existing: List<Event>, horizonDays: Int = 400): List<EventConflict> {
    val result = mutableListOf<EventConflict>()
    for (other in existing) {
        if (other.id == candidate.id) continue
        if (other.time != candidate.time) continue
        if (other.done && other.repeat == Repeat.None) continue

        val first = firstSharedDay(candidate, other, horizonDays) ?: continue
        result += EventConflict(other, first)
    }
    return result.sortedBy { it.date }
}

private fun firstSharedDay(a: Event, b: Event, horizonDays: Int): LocalDate? {
    // Dos eventos únicos: basta comparar las fechas
    if (a.repeat == Repeat.None && b.repeat == Repeat.None) return a.date.takeIf { it == b.date }

    var day = if (a.date.isAfter(b.date)) a.date else b.date
    for (i in 0 until horizonDays) {
        if (a.occursOn(day) && b.occursOn(day)) return day
        day = day.plusDays(1)
    }
    return null
}
