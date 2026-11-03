package com.portablenotes.domain.ui.navigation

/** Rutas de navegación del flujo de autenticación. */
object AuthRoutes {
    const val Login = "login"
    const val Register = "register"
    const val ForgotPassword = "forgot_password"

    val all = setOf(Login, Register, ForgotPassword)
}
