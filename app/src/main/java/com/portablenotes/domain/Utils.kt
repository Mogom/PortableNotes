package com.portablenotes.domain

private fun Throwable.toFriendlyMessage(): String = when (this) {
    is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
        "Este correo no está registrado"
    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
        "Correo o contraseña incorrectos"
    is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
        "La contraseña debe tener al menos 6 caracteres"
    is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
        "Este correo ya está registrado con otra cuenta"
    is com.google.android.gms.common.api.ApiException ->
        "Error de Google: ${statusCode}"
    else ->
        "Error inesperado. Inténtalo de nuevo"
}