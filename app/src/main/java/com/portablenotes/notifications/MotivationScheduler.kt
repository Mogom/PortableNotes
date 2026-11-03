package com.portablenotes.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Mensajes motivadores: 2 al día (mañana y tarde), rotando entre 5 mensajes.
 * Usa alarmas NO exactas (no necesitan permiso especial ni gastan batería de más):
 * pueden llegar con algunos minutos de diferencia, y eso está bien para este tipo de aviso.
 */
object MotivationScheduler {
    const val ACTION_MOTIVATION = "com.portablenotes.action.MOTIVATION"
    const val EXTRA_SLOT = "slot"

    /** Momentos del día en que se lanza un mensaje (separados por varias horas a propósito). */
    val SLOTS: List<LocalTime> = listOf(LocalTime.of(10, 0), LocalTime.of(17, 30))

    /** Si la alarma se retrasa más que esto (por ejemplo, teléfono apagado) el mensaje se omite. */
    const val MAX_DELAY_HOURS = 3L

    data class Message(val title: String, val text: String)

    val MESSAGES: List<Message> = listOf(
        Message("Un buen día empieza con un paso", "Elige una cosa pequeña de tu lista y hazla ahora. El resto se acomoda solo."),
        Message("Tú puedes con esto", "No tiene que salir perfecto, solo tiene que empezar. Dale a tu próximo evento."),
        Message("Vas más lejos de lo que crees", "Mira todo lo que ya has sacado adelante. Hoy sumas un poco más."),
        Message("Tu yo del futuro te lo agradece", "Lo que organizas hoy es tiempo libre que ganas mañana. Revisa tus pendientes."),
        Message("Cierra el día con orgullo", "Marca lo que lograste, aunque sea poco. Descansar también es parte del plan."),
    )

    /** Mensaje del día: rota para que no se repita el mismo seguido. */
    fun messageFor(date: LocalDate, slot: Int): Message =
        MESSAGES[((date.toEpochDay() * SLOTS.size + slot) % MESSAGES.size).toInt()]

    /** Deja programados (o cancela) los mensajes según el ajuste del usuario. */
    fun sync(context: Context) {
        if (NotificationPrefs.motivationEnabled(context)) {
            SLOTS.indices.forEach { schedule(context, it) }
        } else {
            SLOTS.indices.forEach { cancel(context, it) }
        }
    }

    /** Programa el próximo disparo del [slot] (hoy si aún no pasó, si no mañana). */
    fun schedule(context: Context, slot: Int) {
        val ctx = context.applicationContext
        val now = LocalDateTime.now()
        var at = LocalDateTime.of(now.toLocalDate(), SLOTS[slot])
        if (!at.isAfter(now)) at = at.plusDays(1)
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ctx.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent(ctx, slot, create = true)!!)
    }

    private fun cancel(context: Context, slot: Int) {
        val pi = pendingIntent(context.applicationContext, slot, create = false) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pi)
        pi.cancel()
    }

    private fun pendingIntent(context: Context, slot: Int, create: Boolean): PendingIntent? {
        val intent = Intent(context, MotivationReceiver::class.java).apply {
            action = ACTION_MOTIVATION
            data = Uri.parse("portablenotes://motivation/$slot")
            putExtra(EXTRA_SLOT, slot)
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or
            if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, 9000 + slot, intent, flags)
    }
}
