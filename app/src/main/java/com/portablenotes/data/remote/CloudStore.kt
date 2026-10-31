package com.portablenotes.data.remote

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.portablenotes.domain.models.Event
import com.portablenotes.domain.ui.routines.Routine
import com.portablenotes.domain.ui.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

/**
 * Punto único de conexión con Firebase.
 *
 * Estructura en Firestore (los datos de cada usuario quedan aislados):
 *   users/{uid}/events/{eventId}
 *   users/{uid}/tasks/{taskId}
 *   users/{uid}/routines/{routineId}
 */
class CloudStore {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()   // caché sin conexión activada por defecto
    private val syncs = mutableListOf<FirestoreSync<*>>()

    /** Entra de forma anónima si no hay sesión. Más adelante puedes vincular email o Google. */

    suspend fun ensureSignedIn(): String {
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user!!.uid
    }

    /**
     * Inicia sesión y conecta las tres listas con Firestore.
     * Pásale las listas de tus estados: eventsState.events, tasksState.tasks, routinesState.routines.
     */
    suspend fun start(
        uid: String,
        scope: CoroutineScope,
        events: SnapshotStateList<Event>,
        tasks: SnapshotStateList<Task>,
        routines: SnapshotStateList<Routine>,
    ) {
        val user = db.collection("users").document(uid)   // antes: ensureSignedIn()

        FirestoreSync(
            collection = user.collection("events"),
            idOf = { it.id }, toMap = { it.toFirestore() }, fromDoc = { it.toEvent() },
        ).also { it.start(scope, events); syncs += it }

        val tasksCol = user.collection("tasks")
        FirestoreSync(
            collection = tasksCol,
            query = tasksCol.orderBy("createdAt", Query.Direction.ASCENDING),
            idOf = { it.id }, toMap = { it.toFirestore() }, fromDoc = { it.toTask() },
        ).also { it.start(scope, tasks); syncs += it }

        val routinesCol = user.collection("routines")
        FirestoreSync(
            collection = routinesCol,
            query = routinesCol.orderBy("createdAt", Query.Direction.ASCENDING),
            idOf = { it.id }, toMap = { it.toFirestore() }, fromDoc = { it.toRoutine() },
        ).also { it.start(scope, routines); syncs += it }
    }

    fun stop() {
        syncs.forEach { it.stop() }
        syncs.clear()
    }
}
