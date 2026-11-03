package com.portablenotes.data.remote

import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Mantiene sincronizada una lista de Compose con una colección de Firestore, en ambos sentidos:
 *
 *  - Nube → app: un listener en tiempo real reemplaza la lista cuando cambia algo en Firestore.
 *  - App → nube: cada vez que tu lista cambia (EventsState, TasksState, RoutinesState...),
 *    se calcula la diferencia contra lo último que hay en la nube y se escribe en un batch.
 *
 * Así NO tienes que cambiar la lógica de tus estados (cascadas de tareas, toggles, etc.).
 * Firestore guarda caché local y funciona sin internet: los cambios se suben al reconectar.
 */
class FirestoreSync<T : Any>(
    private val collection: CollectionReference,
    private val idOf: (T) -> String,
    private val toMap: (T) -> Map<String, Any?>,
    private val fromDoc: (DocumentSnapshot) -> T?,
    private val query: Query = collection,      // permite orderBy("createdAt")
) {
    private var cloud: Map<String, T> = emptyMap()   // último estado conocido en la nube
    private var ready = false                        // no escribimos hasta recibir la 1.ª carga
    private var registration: ListenerRegistration? = null
    private var job: Job? = null

    /** Llamar desde el hilo principal (por ejemplo dentro de un LaunchedEffect). */
    fun start(scope: CoroutineScope, list: SnapshotStateList<T>) {
        registration = query.addSnapshotListener { snap, error ->
            if (error != null || snap == null) return@addSnapshotListener
            val items = snap.documents.mapNotNull(fromDoc)
            cloud = items.associateBy(idOf)           // primero la nube...
            if (list.toList() != items) {             // ...luego la lista local
                list.clear()
                list.addAll(items)
            }
            ready = true
        }

        job = scope.launch {
            snapshotFlow { list.toList() }.collect { local -> if (ready) push(local) }
        }
    }

    fun stop() {
        registration?.remove()
        job?.cancel()
        registration = null
        job = null
        ready = false
    }

    private fun push(local: List<T>) {
        val localMap = local.associateBy(idOf)
        val ops = mutableListOf<(com.google.firebase.firestore.WriteBatch) -> Unit>()

        for ((id, item) in localMap) {
            if (cloud[id] != item) ops += { b -> b.set(collection.document(id), toMap(item)) }
        }
        for (id in cloud.keys) {
            if (id !in localMap) ops += { b -> b.delete(collection.document(id)) }
        }
        if (ops.isEmpty()) return

        cloud = localMap
        // Firestore limita cada batch a 500 operaciones
        ops.chunked(400).forEach { chunk ->
            val batch = collection.firestore.batch()
            chunk.forEach { it(batch) }
            batch.commit()
        }
    }
}
