package com.portablenotes.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

/**
 * Único lugar que habla con Firebase Authentication.
 * Una sesión anónima (de las pruebas anteriores) NO cuenta como "sesión iniciada".
 */
class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    /** uid del usuario con sesión real, o null si no hay. */
    val currentUid: String?
        get() = auth.currentUser?.takeIf { !it.isAnonymous }?.uid

    /** Emite el uid cada vez que se inicia o cierra sesión. */
    val uidFlow: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.takeIf { !it.isAnonymous }?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(email: String, password: String, displayName: String?) {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
        if (!displayName.isNullOrBlank()) {
            val profile = UserProfileChangeRequest.Builder().setDisplayName(displayName.trim()).build()
            user?.updateProfile(profile)?.await()
        }
    }

    suspend fun signInWithGoogle(idToken: String) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun signOut() = auth.signOut()
}