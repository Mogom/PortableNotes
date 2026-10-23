package com.example.portablenotes.ui.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

/** Lista plana con parentId: sirve igual para modo árbol y para lista simple. */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val parentId: String? = null,
    val done: Boolean = false,
    val expanded: Boolean = true,
)

/** Fila lista para pintar. */
data class TaskRow(
    val task: Task,
    val depth: Int,
    val childCount: Int,
    val doneChildren: Int,
)

/**
 * Estado del módulo Tareas.
 *  - treeMode = true  → jerarquía con subtareas; marcar un padre marca a todos sus hijos,
 *                       y un padre queda hecho solo cuando todos sus hijos lo están.
 *  - treeMode = false → lista simple con check; cada tarea es independiente.
 * Puedes moverlo a un ViewModel + Room sin tocar la UI.
 */
class TasksState(initial: List<Task> = emptyList(), initialTreeMode: Boolean = true) {
    val tasks = mutableStateListOf<Task>().apply { addAll(initial) }
    var treeMode by mutableStateOf(initialTreeMode)
        private set

    init { if (initialTreeMode) syncAll() }

    // ---------- Consultas ----------

    fun childrenOf(id: String?): List<Task> = tasks.filter { it.parentId == id }

    private fun descendantsOf(id: String): Set<String> {
        val out = mutableSetOf<String>()
        fun walk(pid: String): Unit {
            childrenOf(pid).forEach {
                out.add(it.id)
                walk(it.id)
            }
        }
        walk(id)
        return out
    }

    fun visibleRows(): List<TaskRow> {
        if (!treeMode) return tasks.map { TaskRow(it, 0, 0, 0) }
        val out = mutableListOf<TaskRow>()
        fun walk(parentId: String?, depth: Int) {
            childrenOf(parentId).forEach { t ->
                val kids = childrenOf(t.id)
                out.add(TaskRow(t, depth, kids.size, kids.count { it.done }))
                if (t.expanded) walk(t.id, depth + 1)
            }
        }
        walk(null, 0)
        return out
    }

    /** (hechas, total). En modo árbol cuenta solo las tareas hoja, que son las que se ejecutan. */
    fun progress(): Pair<Int, Int> {
        val counted = if (treeMode) tasks.filter { t -> tasks.none { it.parentId == t.id } } else tasks.toList()
        return counted.count { it.done } to counted.size
    }

    // ---------- Acciones ----------

    fun setTreeMode(enabled: Boolean) {
        treeMode = enabled
        if (enabled) syncAll()   // al volver al árbol, los padres reflejan el estado de sus hijos
    }

    fun add(title: String, parentId: String? = null) {
        val parent = if (treeMode) parentId else null
        tasks.add(Task(title = title.trim(), parentId = parent))
        if (parent != null) {
            update(parent) { it.copy(expanded = true) }
            syncAncestors(parent)           // un hijo nuevo sin hacer deja al padre sin hacer
        }
    }

    fun rename(id: String, title: String) = update(id) { it.copy(title = title.trim()) }

    fun toggleExpanded(id: String) = update(id) { it.copy(expanded = !it.expanded) }

    fun toggleDone(id: String) {
        val t = tasks.firstOrNull { it.id == id } ?: return
        val newDone = !t.done
        if (!treeMode) {
            update(id) { it.copy(done = newDone) }
            return
        }
        val affected = descendantsOf(id) + id
        for (i in tasks.indices) if (tasks[i].id in affected) tasks[i] = tasks[i].copy(done = newDone)
        syncAncestors(t.parentId)
    }

    fun delete(id: String) {
        val parent = tasks.firstOrNull { it.id == id }?.parentId
        val toRemove = descendantsOf(id) + id
        tasks.removeAll { it.id in toRemove }
        if (treeMode) syncAncestors(parent)
    }

    // ---------- Internos ----------

    private fun update(id: String, transform: (Task) -> Task) {
        val i = tasks.indexOfFirst { it.id == id }
        if (i >= 0) tasks[i] = transform(tasks[i])
    }

    /** Sube por los ancestros: cada padre está hecho solo si todos sus hijos lo están. */
    private fun syncAncestors(startParentId: String?) {
        var p = startParentId
        while (p != null) {
            val allDone = childrenOf(p).let { it.isNotEmpty() && it.all { c -> c.done } }
            update(p) { it.copy(done = allDone) }
            p = tasks.firstOrNull { it.id == p }?.parentId
        }
    }

    private fun syncAll() {
        fun resolve(id: String): Boolean {
            val kids = childrenOf(id)
            val done = if (kids.isEmpty()) tasks.first { it.id == id }.done
            else kids.map { resolve(it.id) }.all { it }
            update(id) { it.copy(done = done) }
            return done
        }
        childrenOf(null).forEach { resolve(it.id) }
    }
}

fun sampleTasks(): List<Task> {
    val app = Task(id = "1", title = "Entregar proyecto de la app")
    val design = Task(id = "2", title = "Diseño de pantallas", parentId = "1")
    val code = Task(id = "3", title = "Programar módulos", parentId = "1")
    return listOf(
        app,
        design,
        Task(id = "4", title = "Pantalla Home", parentId = "2", done = true),
        Task(id = "5", title = "Pantalla Eventos", parentId = "2", done = true),
        code,
        Task(id = "6", title = "Eventos", parentId = "3", done = true),
        Task(id = "7", title = "Rutinas", parentId = "3"),
        Task(id = "8", title = "Tareas", parentId = "3"),
        Task(id = "9", title = "Comprar mercado"),
        Task(id = "10", title = "Llamar al médico"),
    )
}