package com.portablenotes.notifications

import android.content.Context
import android.content.SharedPreferences

/** Preferencias simples del módulo de notificaciones. */
object NotificationPrefs {
    private const val FILE = "notification_prefs"
    private const val KEY_PERMISSIONS_SHOWN = "permissions_shown"
    private const val KEY_LEAD_MINUTES = "lead_minutes"
    private const val KEY_MOTIVATION = "motivation_enabled"

    /** Minutos de antelación del recordatorio (0 = sin recordatorio previo). */
    const val DEFAULT_LEAD_MINUTES = 10

    /** Opciones de antelación que ofrece la pantalla de ajustes (en minutos). */
    val LEAD_OPTIONS: List<Int> = listOf(0, 5, 10, 15, 30, 60, 120, 1440)

    fun leadLabel(minutes: Int): String = when {
        minutes <= 0 -> "Sin aviso"
        minutes == 60 -> "1 hora"
        minutes in 120..1439 && minutes % 60 == 0 -> "${minutes / 60} horas"
        minutes == 1440 -> "1 día"
        else -> "$minutes min"
    }

    /** Minutos que se pospone la alarma con el botón "Posponer". */
    const val SNOOZE_MINUTES = 5

    internal fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun permissionsShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PERMISSIONS_SHOWN, false)

    fun setPermissionsShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_PERMISSIONS_SHOWN, true).apply()
    }

    fun leadMinutes(context: Context): Int =
        prefs(context).getInt(KEY_LEAD_MINUTES, DEFAULT_LEAD_MINUTES)

    fun motivationEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MOTIVATION, true)

    fun setMotivationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MOTIVATION, enabled).apply()
    }

    fun setLeadMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt(KEY_LEAD_MINUTES, minutes.coerceAtLeast(0)).apply()
    }
}
