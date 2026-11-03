package com.portablenotes.notifications

import android.content.Context
import com.portablenotes.domain.models.events.Event
import com.portablenotes.domain.models.events.Priority
import com.portablenotes.domain.models.events.Repeat
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/**
 * Copia local mínima de los eventos para poder reprogramar las alarmas sin abrir la app
 * (al reiniciar el teléfono o cuando un evento repetido ya sonó y toca el siguiente).
 */
internal object EventStore {
    private const val KEY_EVENTS = "events_json"
    private const val KEY_SCHEDULED_IDS = "scheduled_ids"

    fun save(context: Context, events: List<Event>) {
        val array = JSONArray()
        events.forEach { e ->
            array.put(
                JSONObject()
                    .put("id", e.id)
                    .put("title", e.title)
                    .put("date", e.date.toString())
                    .put("time", e.time.toString())
                    .put("repeat", e.repeat.name)
                    .put("priority", e.priority.name)
                    .put("done", e.done)
            )
        }
        NotificationPrefs.prefs(context).edit().putString(KEY_EVENTS, array.toString()).apply()
    }

    fun load(context: Context): List<Event> {
        val raw = NotificationPrefs.prefs(context).getString(KEY_EVENTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { i ->
                runCatching {
                    val o = array.getJSONObject(i)
                    Event(
                        id = o.getString("id"),
                        title = o.getString("title"),
                        date = LocalDate.parse(o.getString("date")),
                        time = LocalTime.parse(o.getString("time")),
                        repeat = Repeat.valueOf(o.getString("repeat")),
                        priority = Priority.valueOf(o.getString("priority")),
                        done = o.optBoolean("done", false),
                    )
                }.getOrNull()
            }
        }.getOrDefault(emptyList())
    }

    fun scheduledIds(context: Context): Set<String> =
        NotificationPrefs.prefs(context).getStringSet(KEY_SCHEDULED_IDS, emptySet()).orEmpty().toSet()

    fun saveScheduledIds(context: Context, ids: Set<String>) {
        NotificationPrefs.prefs(context).edit().putStringSet(KEY_SCHEDULED_IDS, ids).apply()
    }
}
