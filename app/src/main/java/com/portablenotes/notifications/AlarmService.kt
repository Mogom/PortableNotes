package com.portablenotes.notifications

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.portablenotes.R

/**
 * Alarma de prioridad alta: sonido en bucle + vibración continua hasta que el usuario
 * pulse "Detener" / "Posponer" o pasen [AUTO_STOP_MS].
 */
class AlarmService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoStop = Runnable { stopSelf() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(EventAlarmScheduler.EXTRA_EVENT_ID).orEmpty()
        val title = intent?.getStringExtra(EventAlarmScheduler.EXTRA_TITLE).orEmpty()
        val timeText = intent?.getStringExtra(EventAlarmScheduler.EXTRA_TIME_TEXT).orEmpty()
        val priority = intent?.getStringExtra(EventAlarmScheduler.EXTRA_PRIORITY).orEmpty()

        NotificationChannels.ensure(this)
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(id, title, timeText, priority),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0,
        )

        startSoundAndVibration()
        handler.removeCallbacks(autoStop)
        handler.postDelayed(autoStop, AUTO_STOP_MS)
        return START_NOT_STICKY
    }

    private fun buildNotification(id: String, title: String, timeText: String, priority: String) =
        NotificationCompat.Builder(this, NotificationChannels.ALARM)
            .setSmallIcon(R.drawable.ic_stat_event)
            .setContentTitle(title.ifBlank { "Evento" })
            .setContentText("Es ahora · $timeText")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(EventAlarmReceiver.openAppIntent(this))
            .setFullScreenIntent(EventAlarmReceiver.openAppIntent(this), true)
            .addAction(0, "Detener", actionIntent(EventAlarmReceiver.ACTION_STOP, id, title, priority))
            .addAction(
                0,
                "Posponer ${NotificationPrefs.SNOOZE_MINUTES} min",
                actionIntent(EventAlarmReceiver.ACTION_SNOOZE, id, title, priority),
            )
            .build()

    private fun actionIntent(action: String, id: String, title: String, priority: String): PendingIntent {
        val intent = Intent(this, EventAlarmReceiver::class.java)
            .setAction(action)
            .putExtra(EventAlarmScheduler.EXTRA_EVENT_ID, id)
            .putExtra(EventAlarmScheduler.EXTRA_TITLE, title)
            .putExtra(EventAlarmScheduler.EXTRA_PRIORITY, priority)
        return PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun startSoundAndVibration() {
        stopSoundAndVibration()

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        player = try {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                setWakeMode(this@AlarmService, PowerManager.PARTIAL_WAKE_LOCK)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "No se pudo reproducir el sonido de la alarma", e)
            null
        }

        vibrator = getVibrator().also { v ->
            val effect = VibrationEffect.createWaveform(longArrayOf(0, 800, 500, 800, 500, 1200, 1000), 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                v.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(
                    effect,
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(),
                )
            }
        }
    }

    private fun stopSoundAndVibration() {
        player?.let {
            runCatching { if (it.isPlaying) it.stop() }
            it.release()
        }
        player = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun getVibrator(): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    override fun onDestroy() {
        handler.removeCallbacks(autoStop)
        stopSoundAndVibration()
        super.onDestroy()
    }

    private companion object {
        const val NOTIFICATION_ID = 4242
        const val AUTO_STOP_MS = 60_000L
    }
}
