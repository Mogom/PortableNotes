package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 * Pantalla de registro (nombre, correo, contraseña y confirmación, o Google).
 * Igual que el login: solo dibuja y delega la lógica en los callbacks.
 */
@Composable
fun RegisterScreen(
    onRegister: (name: String, email: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    val nameError = if (submitted) AuthValidation.nameError(name) else null
    val emailError = if (submitted) AuthValidation.emailError(email) else null
    val passwordError = if (submitted) AuthValidation.passwordError(password) else null
    val confirmError = if (submitted) AuthValidation.confirmError(password, confirm) else null

    fun submit() {
        submitted = true
        val valid = AuthValidation.nameError(name) == null &&
            AuthValidation.emailError(email) == null &&
            AuthValidation.passwordError(password) == null &&
            AuthValidation.confirmError(password, confirm) == null
        if (valid) {
            focus.clearFocus()
            onRegister(name.trim(), email.trim(), password)
        }
    }

    AuthLayout(
        title = "Crea tu cuenta",
        subtitle = "Organiza tus eventos, rutinas y tareas en un solo lugar",
        modifier = modifier,
    ) {
        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre",
            imeAction = ImeAction.Next,
            onImeAction = { focus.moveFocus(FocusDirection.Down) },
            errorText = nameError,
            enabled = !isLoading,
        )
        Spacer(Modifier.height(4.dp))
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
            imeAction = ImeAction.Next,
            onImeAction = { focus.moveFocus(FocusDirection.Down) },
            errorText = passwordError,
            enabled = !isLoading,
        )
        Spacer(Modifier.height(4.dp))
        AuthTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = "Confirmar contraseña",
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = ::submit,
            errorText = confirmError,
            enabled = !isLoading,
        )

        Spacer(Modifier.height(12.dp))
        AuthErrorMessage(errorMessage)
        AuthPrimaryButton(text = "Crear cuenta", onClick = ::submit, isLoading = isLoading)

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
            Text("¿Ya tienes cuenta?", fontSize = 14.sp, color = AppColors.TextSecondary)
            TextButton(
                onClick = onGoToLogin,
                enabled = !isLoading,
                colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Accent),
            ) {
                Text("Inicia sesión", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    RegisterScreen(
        onRegister = { _, _, _ -> },
        onGoogleSignIn = {},
        onGoToLogin = {},
    )
}
