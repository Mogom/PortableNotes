package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.AppColors
import com.portablenotes.domain.ui.auth.AuthErrorMessage
import com.portablenotes.domain.ui.auth.AuthLayout
import com.portablenotes.domain.ui.auth.AuthPrimaryButton
import com.portablenotes.domain.ui.auth.AuthTextField
import com.portablenotes.domain.ui.auth.AuthValidation
import com.portablenotes.domain.ui.auth.GoogleSignInButton
import com.portablenotes.domain.ui.auth.OrDivider

/**
 * Pantalla de inicio de sesión (correo + contraseña, o Google).
 *
 * No conoce Firebase: recibe callbacks y estado ([isLoading], [errorMessage]) desde fuera,
 * así la lógica de autenticación vive en un ViewModel y esta pantalla solo dibuja.
 */
@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    val emailError = if (submitted) AuthValidation.emailError(email) else null
    val passwordError = if (submitted && password.isEmpty()) "Ingresa tu contraseña" else null

    fun submit() {
        submitted = true
        if (AuthValidation.emailError(email) == null && password.isNotEmpty()) {
            focus.clearFocus()
            onLogin(email.trim(), password)
        }
    }

    AuthLayout(
        title = "Inicia sesión",
        subtitle = "Accede para ver tus eventos y rutinas",
        modifier = modifier,
    ) {
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo electrónico",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focus.moveFocus(FocusDirection.Down) },
            errorText = emailError,
            enabled = !isLoading,
        )
        Spacer(Modifier.height(4.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = ::submit,
            errorText = passwordError,
            enabled = !isLoading,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onForgotPassword,
                enabled = !isLoading,
                colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Accent),
            ) {
                Text("¿Olvidaste tu contraseña?", fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(8.dp))
        AuthErrorMessage(errorMessage)
        AuthPrimaryButton(text = "Iniciar sesión", onClick = ::submit, isLoading = isLoading)

        Spacer(Modifier.height(20.dp))
        OrDivider()
        Spacer(Modifier.height(20.dp))

        GoogleSignInButton(onClick = onGoogleSignIn, enabled = !isLoading)

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("¿No tienes cuenta?", fontSize = 14.sp, color = AppColors.TextSecondary)
            TextButton(
                onClick = onGoToRegister,
                enabled = !isLoading,
                colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Accent),
            ) {
                Text("Regístrate", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        onLogin = { _, _ -> },
        onGoogleSignIn = {},
        onForgotPassword = {},
        onGoToRegister = {},
    )
}
