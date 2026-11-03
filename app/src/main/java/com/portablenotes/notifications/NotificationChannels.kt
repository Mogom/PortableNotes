package com.portablenotes.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/**
 * Canales de notificación según el nivel de prioridad del evento:
 *  - REMINDER: aviso previo ("empieza en 10 min").
 *  - SIMPLE:   prioridad baja, notificación sencilla a la hora del evento.
 *  - VIBRATE:  prioridad media, notificación emergente con vibración.
 *  - ALARM:    prioridad alta, el sonido y la vibración los controla [AlarmService].
 *  - MOTIVATION: mensajes motivadores (2 al día).
 */
object NotificationChannels {
    const val REMINDER = "event_reminder"
    const val SIMPLE = "event_simple"
    const val VIBRATE = "event_vibrate"
    const val ALARM = "event_alarm"
    const val MOTIVATION = "motivation"

    fun ensure(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val reminder = NotificationChannel(REMINDER, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Aviso unos minutos antes de cada evento"
        }
        val simple = NotificationChannel(SIMPLE, "Eventos (prioridad baja)", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Notificación sencilla a la hora del evento"
        }
        val vibrate = NotificationChannel(VIBRATE, "Eventos (prioridad media)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notificación con vibración a la hora del evento"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400)
        }
        val alarm = NotificationChannel(ALARM, "Alarmas (prioridad alta)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Alarma con sonido y vibración a la hora del evento"
            setSound(null, null)          // el sonido lo reproduce AlarmService en bucle
            enableVibration(false)        // la vibración también
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        val motivation = NotificationChannel(MOTIVATION, "Mensajes motivadores", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Un empujón por la mañana y otro por la tarde"
        }
        manager.createNotificationChannels(listOf(reminder, simple, vibrate, alarm, motivation))
    }
}
