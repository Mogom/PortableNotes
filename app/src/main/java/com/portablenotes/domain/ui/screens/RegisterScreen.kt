package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.models.auth.AuthValidation
import com.portablenotes.domain.ui.components.DangerRed
import com.portablenotes.domain.ui.components.FormField
import com.portablenotes.domain.ui.components.GoogleButton
import com.portablenotes.domain.ui.components.PrimaryButton
import com.portablenotes.domain.ui.components.RoundIconButton
import com.portablenotes.domain.ui.components.TitleText
import com.portablenotes.domain.ui.components.UiText
import com.portablenotes.domain.ui.theme.AppTheme

/** Registro 5a: bloque de color con botón volver, formulario y Google. Solo dibuja; la lógica va en AuthViewModel. */
@Composable
fun RegisterScreen(
    onRegister: (name: String, email: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    val t = AppTheme.tokens
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

    val next = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) })

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
            RoundIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Volver", t.onSoft, t.onAccent, onGoToLogin)
            // Mini tarjeta de tareas decorativa
            Column(
                Modifier.align(Alignment.TopEnd).offset((-4).dp, 6.dp).size(96.dp).rotate(8f)
                    .clip(t.smallShape).background(Color.White).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                DecoRow(filled = true, width = 44)
                DecoRow(filled = false, width = 36)
                DecoRow(filled = false, width = 50)
            }
            Box(
                Modifier.align(Alignment.TopEnd).offset((-86).dp, 58.dp).size(46.dp).rotate(-10f)
                    .clip(t.cellShape).background(t.accent2)
            )
            TitleText("notas", 20.sp, Modifier.align(Alignment.BottomStart), color = t.onAccent)
        }

        Column(
            Modifier.padding(horizontal = 24.dp).padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(Modifier.padding(bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TitleText("Crea tu cuenta", 30.sp)
                UiText("Organiza tus eventos, rutinas y tareas en un solo lugar", 15.sp, FontWeight.Medium, t.muted)
            }
            FormField(name, { name = it }, "Nombre", Icons.Outlined.Person, height = 52.dp, errorText = nameError, enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = next)
            FormField(email, { email = it }, "Correo electrónico", Icons.Outlined.Mail, height = 52.dp, errorText = emailError, enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next), keyboardActions = next)
            FormField(password, { password = it }, "Contraseña", Icons.Outlined.Lock, height = 52.dp, isPassword = true, errorText = passwordError, enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next), keyboardActions = next)
            FormField(confirm, { confirm = it }, "Confirmar contraseña", Icons.Outlined.Lock, height = 52.dp, isPassword = true, errorText = confirmError, enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }))
            if (errorMessage != null) UiText(errorMessage, 13.sp, FontWeight.SemiBold, DangerRed)
            Spacer(Modifier.height(4.dp))
            PrimaryButton("Crear cuenta", ::submit, loading = isLoading)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).height(1.dp).background(t.border))
                UiText("o", 13.sp, FontWeight.SemiBold, t.muted)
                Box(Modifier.weight(1f).height(1.dp).background(t.border))
            }
            GoogleButton(onGoogleSignIn, enabled = !isLoading)
            SwitchAuthLink("¿Ya tienes cuenta? ", "Inicia sesión", onGoToLogin)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DecoRow(filled: Boolean, width: Int) {
    val t = AppTheme.tokens
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.size(12.dp).clip(CircleShape)
                .then(if (filled) Modifier.background(t.accent) else Modifier.border(2.dp, Color(0xFFD6D4CE), CircleShape))
        )
        Box(Modifier.width(width.dp).height(6.dp).clip(CircleShape).background(Color(0xFFECEBE8)))
    }
}
