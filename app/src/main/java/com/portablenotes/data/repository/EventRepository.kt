package com.portablenotes.data.repository

import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.firestore.FirebaseFirestore
import com.portablenotes.data.remote.FirestoreSync
import com.portablenotes.data.remote.toEvent
import com.portablenotes.data.remote.toFirestore
import com.portablenotes.domain.models.events.Event
import kotlinx.coroutines.CoroutineScope
import com.google.firebase.auth.FirebaseAuth

class EventRepository {

    private val collection = FirebaseFirestore
        .getInstance("xtrike-me")
        .collection("events")

    private val sync = FirestoreSync(
        collection = collection,
        idOf = { event -> event.id },
        toMap = { event -> event.toFirestore() },
        fromDoc = { document -> document.toEvent() }
    )

    fun startSync(
        scope: CoroutineScope,
        events: SnapshotStateList<Event>
    ) {
        sync.start(scope, events)
        Log.d("impresion custom", events.toString())
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            Log.e("Usuario autenticado", "Usuario autenticado")
        } else {
            Log.d("Usuario no autenticado", "Usuario no autenticado")
        }
    }
}