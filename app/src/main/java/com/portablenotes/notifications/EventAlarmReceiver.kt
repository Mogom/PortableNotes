package com.portablenotes.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.portablenotes.MainActivity
import com.portablenotes.R
import com.portablenotes.domain.models.events.Priority
import com.portablenotes.domain.models.events.Repeat

/** Recibe el disparo de AlarmManager y muestra el recordatorio, la notificación o la alarma. */
class EventAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            EventAlarmScheduler.ACTION_FIRE -> onFire(context, intent)
            ACTION_STOP -> context.stopService(Intent(context, AlarmService::class.java))
            ACTION_SNOOZE -> {
                context.stopService(Intent(context, AlarmService::class.java))
                val id = intent.getStringExtra(EventAlarmScheduler.EXTRA_EVENT_ID) ?: return
                EventAlarmScheduler.scheduleSnooze(
                    context,
                    id,
                    intent.getStringExtra(EventAlarmScheduler.EXTRA_TITLE).orEmpty(),
                    intent.getStringExtra(EventAlarmScheduler.EXTRA_PRIORITY) ?: Priority.High.name,
                )
            }
        }
    }

    private fun onFire(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EventAlarmScheduler.EXTRA_EVENT_ID) ?: return
        val title = intent.getStringExtra(EventAlarmScheduler.EXTRA_TITLE).orEmpty()
        val timeText = intent.getStringExtra(EventAlarmScheduler.EXTRA_TIME_TEXT).orEmpty()
        val kind = intent.getStringExtra(EventAlarmScheduler.EXTRA_KIND) ?: EventAlarmScheduler.KIND_EVENT
        val lead = intent.getIntExtra(EventAlarmScheduler.EXTRA_LEAD, NotificationPrefs.DEFAULT_LEAD_MINUTES)
        val priority = runCatching {
            Priority.valueOf(intent.getStringExtra(EventAlarmScheduler.EXTRA_PRIORITY).orEmpty())
        }.getOrDefault(Priority.Medium)

        // Programa ya la siguiente ocurrencia (si el evento se repite)
        EventAlarmScheduler.rescheduleEvent(context, id)

        // Si el evento único ya se marcó como hecho, no molestar
        val stored = EventStore.load(context).firstOrNull { it.id == id }
        if (stored != null && stored.done && stored.repeat == Repeat.None) return

        NotificationChannels.ensure(context)

        if (kind == EventAlarmScheduler.KIND_REMINDER) {
            notify(
                context, "$id#r", NotificationChannels.REMINDER, title,
                "Empieza en ${NotificationPrefs.leadLabel(lead)} · $timeText",
            )
            return
        }

        when (priority) {
            Priority.High -> startAlarm(context, id, title, timeText, priority)
            Priority.Medium -> notify(context, "$id#e", NotificationChannels.VIBRATE, title, "Es ahora · $timeText")
            Priority.Low -> notify(context, "$id#e", NotificationChannels.SIMPLE, title, "Es ahora · $timeText")
        }
    }

    private fun startAlarm(context: Context, id: String, title: String, timeText: String, priority: Priority) {
        val service = Intent(context, AlarmService::class.java)
            .putExtra(EventAlarmScheduler.EXTRA_EVENT_ID, id)
            .putExtra(EventAlarmScheduler.EXTRA_TITLE, title)
            .putExtra(EventAlarmScheduler.EXTRA_TIME_TEXT, timeText)
            .putExtra(EventAlarmScheduler.EXTRA_PRIORITY, priority.name)
        try {
            ContextCompat.startForegroundService(context, service)
        } catch (e: Exception) {
            // Sin permiso para servicios en primer plano: al menos una notificación con vibración
            Log.e("EventAlarm", "No se pudo iniciar la alarma", e)
            notify(context, "$id#e", NotificationChannels.VIBRATE, title, "Es ahora · $timeText")
        }
    }

    private fun notify(context: Context, key: String, channel: String, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_event)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(key.hashCode(), notification)
    }

    companion object {
        const val ACTION_STOP = "com.portablenotes.action.ALARM_STOP"
        const val ACTION_SNOOZE = "com.portablenotes.action.ALARM_SNOOZE"

        fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
