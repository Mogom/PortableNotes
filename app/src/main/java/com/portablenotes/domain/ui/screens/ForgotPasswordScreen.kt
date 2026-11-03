package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.models.auth.AuthValidation
import com.portablenotes.domain.ui.components.DangerRed
import com.portablenotes.domain.ui.components.FormField
import com.portablenotes.domain.ui.components.PrimaryButton
import com.portablenotes.domain.ui.components.RoundIconButton
import com.portablenotes.domain.ui.components.TitleText
import com.portablenotes.domain.ui.components.UiText
import com.portablenotes.domain.ui.theme.AppTheme

/**
 * Recuperar contraseña con el estilo 5a (no estaba en el diseño: sigue el de Registro).
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
    val t = AppTheme.tokens
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

    AuthScaffold(modifier) {
        Box(
            Modifier
                .padding(horizontal = 14.dp)
                .padding(top = 4.dp)
                .fillMaxWidth()
                .height(150.dp)
                .clip(t.cardShape)
                .background(t.accent)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            RoundIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Volver", t.onSoft, t.onAccent, onBackToLogin)
            Box(
                Modifier.align(Alignment.TopEnd).offset((-8).dp, 18.dp).size(72.dp).rotate(-8f)
                    .clip(t.cellShape).background(t.accent2)
            )
            TitleText("notas", 20.sp, Modifier.align(Alignment.BottomStart), color = t.onAccent)
        }

        Column(
            Modifier.padding(horizontal = 24.dp).padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.padding(bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TitleText("Recupera tu contraseña", 30.sp)
                UiText(
                    if (emailSent) "Revisa tu bandeja de entrada" else "Te enviaremos un enlace para crear una nueva",
                    15.sp, FontWeight.Medium, t.muted,
                )
            }
            if (emailSent) {
                UiText(
                    "Si ${email.trim()} tiene una cuenta, recibirás un correo con las instrucciones. Si no lo ves, revisa la carpeta de spam.",
                    14.sp, FontWeight.Medium, t.muted,
                )
                Spacer(Modifier.height(8.dp))
                PrimaryButton("Volver a iniciar sesión", onBackToLogin)
            } else {
                FormField(
                    email, { email = it }, "Correo electrónico", Icons.Outlined.Mail,
                    errorText = emailError, enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
                if (errorMessage != null) UiText(errorMessage, 13.sp, FontWeight.SemiBold, DangerRed)
                PrimaryButton("Enviar enlace", ::submit, loading = isLoading)
                SwitchAuthLink("", "Volver a iniciar sesión", onBackToLogin)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
