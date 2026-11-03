package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.models.auth.AuthValidation
import com.portablenotes.domain.ui.components.DangerRed
import com.portablenotes.domain.ui.components.FormField
import com.portablenotes.domain.ui.components.GoogleButton
import com.portablenotes.domain.ui.components.PrimaryButton
import com.portablenotes.domain.ui.components.TitleText
import com.portablenotes.domain.ui.components.UiText
import com.portablenotes.domain.ui.theme.AppTheme
import com.portablenotes.domain.ui.theme.Manrope

/**
 * Login 5a: bloque de color con dos notas decorativas y "notas", luego el formulario.
 * No conoce Firebase: recibe callbacks y estado ([isLoading], [errorMessage]).
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
    val t = AppTheme.tokens
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

    AuthScaffold(modifier) {
        // Bloque de color con notas decorativas
        Box(
            Modifier
                .padding(horizontal = 14.dp)
                .padding(top = 4.dp)
                .fillMaxWidth()
                .height(286.dp)
                .clip(t.cardShape)
                .background(t.accent),
        ) {
            DecoNote(
                Modifier.offset(28.dp, 40.dp).size(146.dp).rotate(-9f),
                background = t.accent2, content = t.onAccent2,
            ) {
                TitleText("Compras", 18.sp, color = t.onAccent2)
                UiText("☐ Pan\n☐ Café\n☐ Fruta", 13.sp, FontWeight.Medium, t.onAccent2, style = androidx.compose.ui.text.TextStyle(lineHeight = 20.sp))
            }
            DecoNote(
                Modifier.align(Alignment.TopEnd).offset((-26).dp, 64.dp).size(width = 160.dp, height = 120.dp).rotate(6f),
                background = Color.White, content = Color(0xFF16151A),
            ) {
                UiText("HOY · 09:00", 12.sp, FontWeight.ExtraBold, Color(0xFF6E6C75))
                TitleText("Reunión de equipo", 18.sp, color = Color(0xFF16151A))
            }
            TitleText("notas", 20.sp, Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 20.dp), color = t.onAccent)
        }

        Column(
            Modifier.padding(horizontal = 24.dp).padding(top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TitleText("Hola de nuevo", 30.sp)
                UiText("Inicia sesión para seguir con tu día", 15.sp, FontWeight.Medium, t.muted)
            }
            FormField(
                value = email, onValueChange = { email = it },
                placeholder = "Correo electrónico", icon = Icons.Outlined.Mail,
                errorText = emailError, enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
            )
            FormField(
                value = password, onValueChange = { password = it },
                placeholder = "Contraseña", icon = Icons.Outlined.Lock,
                isPassword = true, errorText = passwordError, enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                trailing = {
                    UiText(
                        "¿Olvidaste?", 13.sp, FontWeight.ExtraBold, t.text,
                        Modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onForgotPassword),
                    )
                },
            )
            if (errorMessage != null) UiText(errorMessage, 13.sp, FontWeight.SemiBold, DangerRed)
            PrimaryButton("Iniciar sesión", ::submit, loading = isLoading)
            GoogleButton(onGoogleSignIn, enabled = !isLoading)
            SwitchAuthLink("¿Sin cuenta? ", "Regístrate", onGoToRegister)
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Fondo, scroll, teclado y ancho máximo comunes a Login y Registro. */
@Composable
internal fun AuthScaffold(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val t = AppTheme.tokens
    Box(
        modifier
            .fillMaxSize()
            .background(t.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState())) { content() }
    }
}

/** Nota decorativa rotada con sombra. */
@Composable
internal fun DecoNote(
    modifier: Modifier,
    background: Color,
    content: Color,
    body: @Composable () -> Unit,
) {
    val t = AppTheme.tokens
    Column(
        modifier
            .dropShadow(t.smallShape, Shadow(radius = 40.dp, color = Color(0x33000000), offset = DpOffset(0.dp, 18.dp)))
            .clip(t.smallShape)
            .background(background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) { body() }
}

/** "¿Sin cuenta? Regístrate" / "¿Ya tienes cuenta? Inicia sesión". */
@Composable
internal fun SwitchAuthLink(prefix: String, action: String, onClick: () -> Unit) {
    val t = AppTheme.tokens
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = t.muted)) { append(prefix) }
                withStyle(SpanStyle(color = t.text, fontWeight = FontWeight.ExtraBold)) { append(action) }
            },
            modifier = Modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onClick).padding(6.dp),
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
    }
}
