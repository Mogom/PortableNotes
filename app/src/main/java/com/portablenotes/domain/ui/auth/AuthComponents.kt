package com.portablenotes.domain.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.AppColors

// AppColors todavía no define un color de error. Si lo agregas ahí (AppColors.Error),
// reemplaza esta constante.
private val ErrorColor = Color(0xFFFF6B6B)

private val FieldShape = RoundedCornerShape(16.dp)

/** Fondo, scroll, teclado y ancho máximo comunes a las pantallas de autenticación. */
@Composable
internal fun AuthLayout(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.BackgroundTop, AppColors.Background)))
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // widthIn evita que el formulario se estire demasiado en tablets / horizontal
        Column(Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
            Text(title, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 14.sp, color = AppColors.TextSecondary)
            Spacer(Modifier.height(28.dp))
            content()
        }
    }
}

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    errorText: String? = null,
    enabled: Boolean = true,
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        label = { Text(label) },
        shape = FieldShape,
        isError = errorText != null,
        supportingText = if (errorText != null) {
            { Text(errorText, fontSize = 12.sp) }
        } else null,
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            disabledTextColor = AppColors.TextSecondary,
            errorTextColor = AppColors.TextPrimary,
            focusedContainerColor = AppColors.Surface,
            unfocusedContainerColor = AppColors.Surface,
            disabledContainerColor = AppColors.Surface,
            errorContainerColor = AppColors.Surface,
            focusedBorderColor = AppColors.Accent,
            unfocusedBorderColor = AppColors.Border,
            disabledBorderColor = AppColors.Border,
            errorBorderColor = ErrorColor,
            focusedLabelColor = AppColors.Accent,
            unfocusedLabelColor = AppColors.TextSecondary,
            disabledLabelColor = AppColors.TextSecondary,
            errorLabelColor = ErrorColor,
            cursorColor = AppColors.Accent,
            errorCursorColor = ErrorColor,
            focusedTrailingIconColor = AppColors.TextSecondary,
            unfocusedTrailingIconColor = AppColors.TextSecondary,
            errorTrailingIconColor = AppColors.TextSecondary,
            errorSupportingTextColor = ErrorColor,
        ),
    )
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = FieldShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Accent,
            contentColor = AppColors.OnAccent,
            disabledContainerColor = AppColors.Accent.copy(alpha = 0.6f),
            disabledContentColor = AppColors.OnAccent,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = AppColors.OnAccent,
            )
        } else {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Botón "Continuar con Google".
 * La "G" es un marcador de posición: antes de publicar, reemplázala por el logotipo oficial
 * siguiendo las guías de marca de Google (developers.google.com/identity/branding-guidelines).
 */
@Composable
internal fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = FieldShape,
        border = BorderStroke(1.dp, AppColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppColors.Surface,
            contentColor = AppColors.TextPrimary,
            disabledContainerColor = AppColors.Surface,
            disabledContentColor = AppColors.TextSecondary,
        ),
    ) {
        Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(12.dp))
        Text("Continuar con Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.Border)
        Text("o", modifier = Modifier.padding(horizontal = 12.dp), fontSize = 13.sp, color = AppColors.TextSecondary)
        HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.Border)
    }
}

/** Mensaje de error general (credenciales incorrectas, sin conexión, etc.). */
@Composable
internal fun AuthErrorMessage(message: String?, modifier: Modifier = Modifier) {
    if (message == null) return
    Text(
        text = message,
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp),
        fontSize = 13.sp,
        color = ErrorColor,
    )
}
