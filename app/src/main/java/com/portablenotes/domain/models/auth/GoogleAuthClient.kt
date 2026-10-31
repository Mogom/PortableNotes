package com.portablenotes.domain.models.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Muestra el selector de cuentas de Google (Credential Manager) y devuelve el idToken,
 * que luego se entrega a Firebase con AuthViewModel.signInWithGoogle().
 * Debe recibir el contexto de la Activity (LocalContext.current en Compose).
 */
object GoogleAuthClient {

    suspend fun getIdToken(context: Context, webClientId: String): String {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
            .build()

        val credential = CredentialManager.create(context).getCredential(context, request).credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        throw IllegalStateException("Credencial de Google inesperada")
    }

    /** null = el usuario canceló: no se muestra ningún error. */
    fun errorMessage(t: Throwable): String? = when (t) {
        is GetCredentialCancellationException -> null
        is NoCredentialException -> "No hay una cuenta de Google disponible en este dispositivo"
        else -> "No se pudo iniciar sesión con Google"
    }
}
