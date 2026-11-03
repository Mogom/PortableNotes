package com.portablenotes.domain.ui.auth

/**
 * Validaciones de los formularios de autenticación.
 * Sin dependencias de Android para poder probarlas con tests unitarios simples.
 * Devuelven el mensaje de error, o null si el valor es válido.
 */
object AuthValidation {
    /** Firebase Authentication exige mínimo 6 caracteres. */
    const val MIN_PASSWORD = 6

    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun isValidEmail(value: String): Boolean = EMAIL.matches(value.trim())

    fun emailError(value: String): String? = when {
        value.isBlank() -> "Ingresa tu correo"
        !isValidEmail(value) -> "Correo no válido"
        else -> null
    }

    fun passwordError(value: String): String? =
        if (value.length < MIN_PASSWORD) "Mínimo $MIN_PASSWORD caracteres" else null

    fun nameError(value: String): String? =
        if (value.isBlank()) "Ingresa tu nombre" else null

    fun confirmError(password: String, confirm: String): String? =
        if (password != confirm) "Las contraseñas no coinciden" else null
}
