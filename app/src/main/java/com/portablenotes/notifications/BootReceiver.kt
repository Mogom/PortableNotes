package com.portablenotes.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reprograma las alarmas cuando el sistema las pierde: reinicio del teléfono,
 * actualización de la app, cambio de hora / zona horaria o cambio del permiso de alarmas exactas.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationChannels.ensure(context)
        EventAlarmScheduler.rescheduleAll(context)
        MotivationScheduler.sync(context)
    }
}
