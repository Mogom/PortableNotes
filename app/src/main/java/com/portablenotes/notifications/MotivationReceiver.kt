package com.portablenotes.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.portablenotes.R
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/** Muestra el mensaje motivador del momento del día y programa el de mañana. */
class MotivationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != MotivationScheduler.ACTION_MOTIVATION) return
        val slot = intent.getIntExtra(MotivationScheduler.EXTRA_SLOT, 0)
            .coerceIn(0, MotivationScheduler.SLOTS.lastIndex)

        if (!NotificationPrefs.motivationEnabled(context)) return
        MotivationScheduler.schedule(context, slot)          // el de mañana

        // Si llegó muy tarde (teléfono apagado, etc.) mejor no mostrarlo fuera de lugar
        val scheduled = LocalDateTime.of(LocalDate.now(), MotivationScheduler.SLOTS[slot])
        val delay = Duration.between(scheduled, LocalDateTime.now())
        if (delay.toHours() >= MotivationScheduler.MAX_DELAY_HOURS) return

        NotificationChannels.ensure(context)
        val message = MotivationScheduler.messageFor(LocalDate.now(), slot)
        val notification = NotificationCompat.Builder(context, NotificationChannels.MOTIVATION)
            .setSmallIcon(R.drawable.ic_stat_event)
            .setContentTitle(message.title)
            .setContentText(message.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.text))
            .setContentIntent(EventAlarmReceiver.openAppIntent(context))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(7000 + slot, notification)
    }
}
