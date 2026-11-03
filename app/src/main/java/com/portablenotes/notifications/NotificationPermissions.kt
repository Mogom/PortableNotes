package com.portablenotes.notifications

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/** Consultas y accesos directos a los permisos que necesitan los avisos. */
object NotificationPermissions {

    /** Permiso de notificaciones (POST_NOTIFICATIONS en Android 13+). */
    fun notificationsEnabled(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /**
     * Alarmas exactas. En Android 13+ se concede sola (USE_EXACT_ALARM);
     * en Android 12 (API 31-32) el usuario debe activarla en Ajustes.
     */
    fun canScheduleExact(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        } else true

    fun exactAlarmSettingsIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
        } else appNotificationSettingsIntent(context)

    fun appNotificationSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
}
