package com.portablenotes.domain.models.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.portablenotes.data.remote.AuthRepository

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resetEmailSent: Boolean = false,
)

class AuthViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    /** null = sin sesión. La navegación reacciona a este valor. */
    val uid: StateFlow<String?> =
        repo.uidFlow.stateIn(viewModelScope, SharingStarted.Eagerly, repo.currentUid)

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun login(email: String, password: String) = launchAuth { repo.login(email, password) }

    fun register(email: String, password: String, displayName: String? = null) =
        launchAuth { repo.register(email, password, displayName) }

    fun signInWithGoogle(idToken: String) = launchAuth { repo.signInWithGoogle(idToken) }

    fun sendPasswordReset(email: String) = launchAuth {
        repo.sendPasswordReset(email)
        _ui.update { it.copy(resetEmailSent = true) }
    }

    /** Error al obtener la cuenta de Google (cancelar el selector no muestra error). */
    fun onGoogleError(t: Throwable) {
        GoogleAuthClient.errorMessage(t)?.let { msg -> _ui.update { it.copy(errorMessage = msg) } }
    }

    /** Limpia errores y estados al cambiar de pantalla. */
    fun clearStatus() = _ui.update { AuthUiState() }

    fun signOut() = repo.signOut()

    private fun launchAuth(block: suspend () -> Unit) {
        if (_ui.value.isLoading) return                      // evita doble toque
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                block()
                _ui.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, errorMessage = e.toAuthMessage()) }
            }
        }
    }
}

private fun Throwable.toAuthMessage(): String = when (this) {
    is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil (mínimo 6 caracteres)"
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
    is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo"
    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
    is FirebaseNetworkException -> "Sin conexión. Revisa tu internet"
    is FirebaseTooManyRequestsException -> "Demasiados intentos. Intenta más tarde"
    else -> "Ocurrió un error. Intenta de nuevo"
}