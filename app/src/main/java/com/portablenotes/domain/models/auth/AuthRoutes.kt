package com.portablenotes.domain.ui.navigation

/**
 * Rutas de las pantallas de acceso. Van SEPARADAS de AppTab: AppTab solo debe tener
 * las pestañas de la barra inferior (Home, Events, Routines, Tasks); si Login/Register/ForgotPassword
 * siguen dentro de AppTab, AppBottomBar los dibujaría como botones.
 */
object AuthRoutes {
    const val Login = "login"
    const val Register = "register"
    const val ForgotPassword = "forgot_password"

    val all = setOf(Login, Register, ForgotPassword)
}