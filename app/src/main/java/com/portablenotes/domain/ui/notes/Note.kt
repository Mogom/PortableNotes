package com.portablenotes.domain.ui.notes

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    /** Milisegundos desde 1970 (como createdAt en tareas y rutinas). */
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Notas del usuario. Se sincronizan con Firestore en users/{uid}/notes (ver CloudStore). */
class NotesState(initial: List<Note> = emptyList()) {
    val notes = mutableStateListOf<Note>().apply { addAll(initial) }

    /** Más recientes primero. */
    fun recent(): List<Note> = notes.sortedByDescending { it.updatedAt }

    fun search(query: String): List<Note> {
        val q = query.trim()
        if (q.isEmpty()) return recent()
        return recent().filter { it.title.contains(q, ignoreCase = true) || it.body.contains(q, ignoreCase = true) }
    }

    fun upsert(note: Note) {
        val updated = note.copy(updatedAt = System.currentTimeMillis())
        val i = notes.indexOfFirst { it.id == note.id }
        if (i >= 0) notes[i] = updated else notes.add(updated)
    }

    fun delete(id: String) {
        notes.removeAll { it.id == id }
    }
}
