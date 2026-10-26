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
import androidx.compose.ui.Modifier
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

/**
 * Pantalla para recuperar la contraseña.
 * Cuando [emailSent] es true muestra la confirmación en lugar del formulario.
 */
@Composable
fun ForgotPasswordScreen(
    onSendResetLink: (email: String) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    emailSent: Boolean = false,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    val emailError = if (submitted) AuthValidation.emailError(email) else null

    fun submit() {
        submitted = true
        if (AuthValidation.emailError(email) == null) {
            focus.clearFocus()
            onSendResetLink(email.trim())
        }
    }

    AuthLayout(
        title = "Recupera tu contraseña",
        subtitle = if (emailSent) {
            "Revisa tu bandeja de entrada"
        } else {
            "Te enviaremos un enlace para crear una nueva"
        },
        modifier = modifier,
    ) {
        if (emailSent) {
            Text(
                text = "Si ${email.trim()} tiene una cuenta, recibirás un correo con las instrucciones. " +
                    "Si no lo ves, revisa la carpeta de spam.",
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
            )
            Spacer(Modifier.height(24.dp))
            AuthPrimaryButton(text = "Volver a iniciar sesión", onClick = onBackToLogin)
        } else {
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electrónico",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onImeAction = ::submit,
                errorText = emailError,
                enabled = !isLoading,
            )
            Spacer(Modifier.height(12.dp))
            AuthErrorMessage(errorMessage)
            AuthPrimaryButton(text = "Enviar enlace", onClick = ::submit, isLoading = isLoading)

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(
                    onClick = onBackToLogin,
                    enabled = !isLoading,
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Accent),
                ) {
                    Text("Volver a iniciar sesión", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(onSendResetLink = {}, onBackToLogin = {})
}
