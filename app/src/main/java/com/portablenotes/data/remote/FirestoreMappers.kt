package com.portablenotes.data.remote

import com.portablenotes.domain.models.events.Event
import com.portablenotes.domain.models.events.EventType
import com.portablenotes.domain.models.events.Priority
import com.portablenotes.domain.models.events.Repeat
import com.portablenotes.domain.ui.notes.Note
import com.portablenotes.domain.ui.routines.Routine
import com.portablenotes.domain.ui.routines.RoutineCategory
import com.portablenotes.domain.ui.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.LocalTime

/*
 * Firestore no entiende LocalDate / LocalTime ni enums de Kotlin, así que se guardan como texto:
 *   fecha → "2026-10-24"   hora → "09:00"   enums → su name ("High", "Weekly"...)
 * El id del documento ES el id del modelo, así que no se duplica dentro del documento.
 */

private inline fun <reified E : Enum<E>> enumOr(name: String?, default: E): E =
    enumValues<E>().firstOrNull { it.name == name } ?: default

// ---------------- Eventos ----------------

fun Event.toFirestore(): Map<String, Any?> = mapOf(
    "title" to title,
    "date" to date.toString(),
    "time" to time.toString(),
    "repeat" to repeat.name,
    "type" to type.name,
    "priority" to priority.name,
    "notes" to notes,
    "done" to done,
    "routineId" to routineId,
)

/**
 * Fecha y hora del evento. Lo normal es "date" + "time" (lo que escribe toFirestore),
 * pero también acepta un único campo "date-time" (texto ISO o Timestamp de Firestore).
 */
private fun DocumentSnapshot.eventDateTime(): LocalDateTime {
    val date = getString("date")
    val time = getString("time")
    if (date != null && time != null) return LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time))

    return when (val raw = get("date-time")) {
        is com.google.firebase.Timestamp ->
            LocalDateTime.ofInstant(raw.toDate().toInstant(), ZoneId.systemDefault())
        is String -> LocalDateTime.parse(raw)
        else -> error("El evento no tiene fecha: faltan \"date\"/\"time\" o \"date-time\"")
    }
}

fun DocumentSnapshot.toEvent(): Event? = runCatching {
    Event(
        id = id,
        title = getString("title") ?: "",
        date = eventDateTime().toLocalDate(),
        time = eventDateTime().toLocalTime(),
        repeat = enumOr(getString("repeat"), Repeat.None),
        type = enumOr(getString("type"), EventType.Other),
        priority = enumOr(getString("priority"), Priority.Medium),
        notes = getString("notes") ?: "",
        done = getBoolean("done") ?: false,
        routineId = getString("routineId"),
    )
}.onFailure { android.util.Log.e("Firestore", "No se pudo leer el evento $id", it) }.getOrNull()

// ---------------- Tareas ----------------

fun Task.toFirestore(): Map<String, Any?> = mapOf(
    "title" to title,
    "parentId" to parentId,
    "done" to done,
    "expanded" to expanded,
    "createdAt" to createdAt,
)

fun DocumentSnapshot.toTask(): Task? = runCatching {
    Task(
        id = id,
        title = getString("title") ?: "",
        parentId = getString("parentId"),
        done = getBoolean("done") ?: false,
        expanded = getBoolean("expanded") ?: true,
        createdAt = getLong("createdAt") ?: 0L,
    )
}.getOrNull()

// ---------------- Notas ----------------

fun Note.toFirestore(): Map<String, Any?> = mapOf(
    "title" to title,
    "body" to body,
    "updatedAt" to updatedAt,
)

fun DocumentSnapshot.toNote(): Note? = runCatching {
    Note(
        id = id,
        title = getString("title") ?: "",
        body = getString("body") ?: "",
        updatedAt = getLong("updatedAt") ?: 0L,
    )
}.getOrNull()

// ---------------- Rutinas ----------------

fun Routine.toFirestore(): Map<String, Any?> = mapOf(
    "name" to name,
    "description" to description,
    "category" to category.name,
    "iconKey" to iconKey,
    "createdAt" to createdAt,
    "completedDates" to completedDates,
)

fun DocumentSnapshot.toRoutine(): Routine? = runCatching {
    Routine(
        id = id,
        name = getString("name") ?: "",
        description = getString("description") ?: "",
        category = enumOr(getString("category"), RoutineCategory.Daily),
        iconKey = getString("iconKey") ?: "star",
        createdAt = getLong("createdAt") ?: 0L,
        completedDates = (get("completedDates") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    )
}.getOrNull()
