package com.portablenotes.domain.ui.screens

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.portablenotes.domain.ui.AppColors
import com.portablenotes.domain.ui.auth.AuthLayout
import com.portablenotes.domain.ui.auth.AuthPrimaryButton
import com.portablenotes.notifications.NotificationPermissions
import com.portablenotes.notifications.NotificationPrefs

/**
 * Pantalla que se muestra la primera vez que se abre la app y pide los permisos
 * necesarios para los recordatorios y las alarmas.
 */
@Composable
fun PermissionsScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var notificationsOk by remember { mutableStateOf(NotificationPermissions.notificationsEnabled(context)) }
    var exactAlarmsOk by remember { mutableStateOf(NotificationPermissions.canScheduleExact(context)) }
    var denied by rememberSaveable { mutableStateOf(false) }

    // Al volver de Ajustes, vuelve a comprobar el estado de los permisos
    val activity = remember(context) { context.findComponentActivity() }
    DisposableEffect(activity) {
        val lifecycle = activity?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsOk = NotificationPermissions.notificationsEnabled(context)
                exactAlarmsOk = NotificationPermissions.canScheduleExact(context)
            }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsOk = NotificationPermissions.notificationsEnabled(context)
        if (!granted) denied = true
    }

    val primaryText = when {
        !notificationsOk && denied -> "Abrir ajustes"
        !notificationsOk -> "Permitir notificaciones"
        !exactAlarmsOk -> "Permitir alarmas exactas"
        else -> "Continuar"
    }

    fun onPrimary() {
        when {
            !notificationsOk && (denied || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) ->
                context.startActivity(NotificationPermissions.appNotificationSettingsIntent(context))
            !notificationsOk -> notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            !exactAlarmsOk -> context.startActivity(NotificationPermissions.exactAlarmSettingsIntent(context))
            else -> onFinished()
        }
    }

    AuthLayout(
        title = "Activa las notificaciones",
        subtitle = "Para que no se te pase ningún evento",
        modifier = modifier,
    ) {
        PermissionItem(
            icon = Icons.Rounded.Notifications,
            title = "Recordatorios",
            description = "Te avisamos ${NotificationPrefs.DEFAULT_LEAD_MINUTES} minutos antes de cada evento.",
            granted = notificationsOk,
        )
        Spacer(Modifier.height(12.dp))
        PermissionItem(
            icon = Icons.Rounded.Alarm,
            title = "Alarma con vibración",
            description = "A la hora del evento suena y vibra, según su prioridad.",
            granted = notificationsOk && exactAlarmsOk,
        )

        Spacer(Modifier.height(28.dp))
        AuthPrimaryButton(text = primaryText, onClick = ::onPrimary)

        if (!(notificationsOk && exactAlarmsOk)) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onFinished, modifier = Modifier.fillMaxWidth()) {
                Text("Ahora no", fontSize = 14.sp, color = AppColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    granted: Boolean,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface, shape)
            .border(1.dp, AppColors.Border, shape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(AppColors.Tint2, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.Accent)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
            Text(description, fontSize = 13.sp, color = AppColors.TextSecondary)
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = if (granted) "Permitido" else "Pendiente",
            tint = if (granted) AppColors.Accent else AppColors.TextSecondary,
        )
    }
}

private fun Context.findComponentActivity(): ComponentActivity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PermissionsScreenPreview() {
    PermissionsScreen(onFinished = {})
}
