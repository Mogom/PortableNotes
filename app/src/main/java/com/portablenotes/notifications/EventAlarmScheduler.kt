package com.portablenotes.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.portablenotes.domain.models.events.Event
import com.portablenotes.domain.models.events.Repeat
import com.portablenotes.domain.models.events.hhmm
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Programa con AlarmManager, para cada evento, dos avisos:
 *  - un recordatorio [NotificationPrefs.leadMinutes] minutos antes;
 *  - la notificación / alarma a la hora exacta del evento.
 * Solo se programa la PRÓXIMA ocurrencia; al sonar, el receptor programa la siguiente.
 */
object EventAlarmScheduler {
    const val ACTION_FIRE = "com.portablenotes.action.EVENT_FIRE"

    const val EXTRA_EVENT_ID = "event_id"
    const val EXTRA_TITLE = "title"
    const val EXTRA_TIME_TEXT = "time_text"
    const val EXTRA_PRIORITY = "priority"
    const val EXTRA_KIND = "kind"
    const val EXTRA_LEAD = "lead_minutes"

    const val KIND_REMINDER = "reminder"
    const val KIND_EVENT = "event"

    private const val SLOT_REMINDER = "r"
    private const val SLOT_EVENT = "e"
    private const val SLOT_SNOOZE = "s"

    /** Guarda los eventos y deja programadas exactamente las alarmas que correspondan. */
    fun sync(context: Context, events: List<Event>) {
        val ctx = context.applicationContext
        EventStore.save(ctx, events)
        val currentIds = events.map { it.id }.toSet()
        (EventStore.scheduledIds(ctx) - currentIds).forEach { cancelEvent(ctx, it) }
        val now = LocalDateTime.now()
        events.forEach { scheduleEvent(ctx, it, now) }
        EventStore.saveScheduledIds(ctx, currentIds)
    }

    /** Reprograma todo desde la copia local (arranque del teléfono, cambio de hora, etc.). */
    fun rescheduleAll(context: Context) {
        val ctx = context.applicationContext
        val events = EventStore.load(ctx)
        val now = LocalDateTime.now()
        events.forEach { scheduleEvent(ctx, it, now) }
        EventStore.saveScheduledIds(ctx, events.map { it.id }.toSet())
    }

    /** Programa la siguiente ocurrencia de un solo evento (tras sonar uno repetido). */
    fun rescheduleEvent(context: Context, eventId: String) {
        val ctx = context.applicationContext
        EventStore.load(ctx).firstOrNull { it.id == eventId }?.let { scheduleEvent(ctx, it, LocalDateTime.now()) }
    }

    /** Quita todas las alarmas (por ejemplo al cerrar sesión). */
    fun clearAll(context: Context) {
        val ctx = context.applicationContext
        EventStore.scheduledIds(ctx).forEach { cancelEvent(ctx, it) }
        EventStore.saveScheduledIds(ctx, emptySet())
        EventStore.save(ctx, emptyList())
    }

    /** Vuelve a avisar [NotificationPrefs.SNOOZE_MINUTES] minutos después (botón "Posponer"). */
    fun scheduleSnooze(context: Context, eventId: String, title: String, priorityName: String) {
        val at = LocalDateTime.now().plusMinutes(NotificationPrefs.SNOOZE_MINUTES.toLong())
        val extras = Bundle().apply {
            putString(EXTRA_EVENT_ID, eventId)
            putString(EXTRA_TITLE, title)
            putString(EXTRA_TIME_TEXT, at.toLocalTime().hhmm())
            putString(EXTRA_PRIORITY, priorityName)
            putString(EXTRA_KIND, KIND_EVENT)
        }
        setAlarm(context, at, pendingIntent(context, eventId, SLOT_SNOOZE, extras, create = true)!!)
    }

    // ---------------------------------------------------------------------------------------

    private fun scheduleEvent(context: Context, event: Event, now: LocalDateTime) {
        val lead = NotificationPrefs.leadMinutes(context)

        // 1) A la hora del evento
        val occurrence = event.nextOccurrence(now)
        if (occurrence != null) {
            val extras = extras(event, occurrence, KIND_EVENT, lead)
            setAlarm(context, occurrence, pendingIntent(context, event.id, SLOT_EVENT, extras, create = true)!!)
        } else {
            cancelSlot(context, event.id, SLOT_EVENT)
        }

        // 2) Recordatorio previo (solo si todavía falta más que la antelación)
        val reminderOccurrence = if (lead > 0) event.nextOccurrence(now.plusMinutes(lead.toLong())) else null
        if (reminderOccurrence != null) {
            val extras = extras(event, reminderOccurrence, KIND_REMINDER, lead)
            setAlarm(
                context,
                reminderOccurrence.minusMinutes(lead.toLong()),
                pendingIntent(context, event.id, SLOT_REMINDER, extras, create = true)!!,
            )
        } else {
            cancelSlot(context, event.id, SLOT_REMINDER)
        }
    }

    private fun cancelEvent(context: Context, eventId: String) {
        cancelSlot(context, eventId, SLOT_REMINDER)
        cancelSlot(context, eventId, SLOT_EVENT)
        cancelSlot(context, eventId, SLOT_SNOOZE)
    }

    private fun cancelSlot(context: Context, eventId: String, slot: String) {
        val pi = pendingIntent(context, eventId, slot, extras = null, create = false) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pi)
        pi.cancel()
    }

    private fun extras(event: Event, occurrence: LocalDateTime, kind: String, lead: Int) = Bundle().apply {
        putString(EXTRA_EVENT_ID, event.id)
        putString(EXTRA_TITLE, event.title)
        putString(EXTRA_TIME_TEXT, occurrence.toLocalTime().hhmm())
        putString(EXTRA_PRIORITY, event.priority.name)
        putString(EXTRA_KIND, kind)
        putInt(EXTRA_LEAD, lead)
    }

    /** El mismo (evento, slot) siempre produce el mismo PendingIntent, así se puede cancelar. */
    private fun pendingIntent(
        context: Context,
        eventId: String,
        slot: String,
        extras: Bundle?,
        create: Boolean,
    ): PendingIntent? {
        val intent = Intent(context, EventAlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            data = Uri.parse("portablenotes://event/${Uri.encode(eventId)}/$slot")
            if (extras != null) putExtras(extras)
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or
            if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, "$eventId#$slot".hashCode(), intent, flags)
    }

    private fun setAlarm(context: Context, at: LocalDateTime, pi: PendingIntent) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (NotificationPermissions.canScheduleExact(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        } else {
            // Sin permiso de alarmas exactas: puede retrasarse unos minutos, pero no se pierde.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        }
    }
}

/** Próxima ocurrencia estrictamente posterior a [after], o null si ya no hay (evento único pasado o hecho). */
internal fun Event.nextOccurrence(after: LocalDateTime): LocalDateTime? {
    if (done && repeat == Repeat.None) return null
    val afterDay = after.toLocalDate()
    var day = if (date.isAfter(afterDay)) date else afterDay
    for (i in 0 until 400) {                       // cubre hasta eventos anuales
        if (occursOn(day)) {
            val candidate = LocalDateTime.of(day, time)
            if (candidate.isAfter(after)) return candidate
        }
        day = day.plusDays(1)
    }
    return null
}
