package com.portablenotes.domain.ui.home

import com.portablenotes.domain.models.events.Event
import com.portablenotes.domain.models.events.Repeat
import com.portablenotes.domain.ui.tasks.Task
import java.time.LocalDateTime

/**
 * Resumen de cumplimiento para la burbuja de Inicio.
 *  - Tareas: se cuentan solo las "hojas" (las que no tienen subtareas) para no contar dos veces un padre y sus hijos.
 *  - Eventos: solo los que NO se repiten, porque son los únicos que se pueden marcar como hechos.
 *    "No cumplido" = ya pasó su fecha y hora y no se marcó como hecho.
 */
data class HomeMetrics(
    val tasksDone: Int = 0,
    val tasksPending: Int = 0,
    val eventsDone: Int = 0,
    val eventsPending: Int = 0,
    val eventsMissed: Int = 0,
) {
    val tasksTotal: Int get() = tasksDone + tasksPending
    val eventsTotal: Int get() = eventsDone + eventsPending + eventsMissed
    val total: Int get() = tasksTotal + eventsTotal
    val done: Int get() = tasksDone + eventsDone

    /** 0f..1f del total de elementos cumplidos. */
    val progress: Float get() = if (total == 0) 0f else done.toFloat() / total
}

fun computeMetrics(events: List<Event>, tasks: List<Task>, now: LocalDateTime = LocalDateTime.now()): HomeMetrics {
    val parentIds = tasks.mapNotNull { it.parentId }.toSet()
    val leaves = tasks.filter { it.id !in parentIds }

    val oneOff = events.filter { it.repeat == Repeat.None }
    val eventsDone = oneOff.count { it.done }
    val eventsMissed = oneOff.count { !it.done && LocalDateTime.of(it.date, it.time).isBefore(now) }

    return HomeMetrics(
        tasksDone = leaves.count { it.done },
        tasksPending = leaves.count { !it.done },
        eventsDone = eventsDone,
        eventsPending = oneOff.size - eventsDone - eventsMissed,
        eventsMissed = eventsMissed,
    )
}
