package com.example.parrot.player

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray

private val Context.sessionDataStore by preferencesDataStore(
    name = "playback_session"
)

data class SavedSession(
    val queueIds: List<Long>,
    val index: Int,
    val positionMs: Long
)

class SessionStore(context: Context) {

    private val appContext = context.applicationContext

    private val queueKey = stringPreferencesKey("queue_ids")
    private val indexKey = intPreferencesKey("current_index")
    private val positionKey = longPreferencesKey("position_ms")

    suspend fun save(session: SavedSession) {
        appContext.sessionDataStore.edit { preferences ->
            val array = JSONArray()
            session.queueIds.forEach { array.put(it) }

            preferences[queueKey] = array.toString()
            preferences[indexKey] = session.index
            preferences[positionKey] = session.positionMs
        }
    }

    suspend fun restore(): SavedSession {
        val preferences = appContext.sessionDataStore.data.first()

        val ids = try {
            val array = JSONArray(preferences[queueKey] ?: "[]")
            (0 until array.length()).map { array.getLong(it) }
        } catch (_: Exception) {
            emptyList()
        }

        return SavedSession(
            queueIds = ids,
            index = preferences[indexKey] ?: 0,
            positionMs = preferences[positionKey] ?: 0L
        )
    }
}
