package com.portablenotes.domain.ui.calendar.events

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate

/**
 * Estado de eventos. Es un holder simple; puedes moverlo a un ViewModel
 * (StateFlow + Room) sin cambiar la UI.
 */
class EventsState(initial: List<Event> = emptyList()) {
    val events = mutableStateListOf<Event>().apply { addAll(initial) }

    fun occurrencesOn(day: LocalDate): List<Event> = events.toList().on(day)

    fun upsert(event: Event) {
        val i = events.indexOfFirst { it.id == event.id }
        if (i >= 0) events[i] = event else events.add(event)
    }

    fun delete(id: String) {
        events.removeAll { it.id == id }
    }

    /** Solo se puede marcar como hecho si el evento NO se repite. */
    fun toggleDone(id: String) {
        val i = events.indexOfFirst { it.id == id }
        if (i < 0) return
        val e = events[i]
        if (e.repeat == Repeat.None) events[i] = e.copy(done = !e.done)
    }
}