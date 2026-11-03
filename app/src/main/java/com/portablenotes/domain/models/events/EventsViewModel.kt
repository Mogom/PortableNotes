package com.portablenotes.domain.models.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import com.portablenotes.data.repository.EventRepository
import java.time.LocalDate

class EventsViewModel : ViewModel() {
    private val repository = EventRepository()
    val events = mutableStateListOf<Event>()

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

    init {
        repository.startSync(
            scope = viewModelScope,
            events = events
        )
    }
}