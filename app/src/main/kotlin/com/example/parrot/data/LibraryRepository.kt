
package com.example.parrot.data

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.collect

private val Context.libraryDataStore by preferencesDataStore(
    name = "library_cache"
)

class LibraryRepository(
    private val context: Context,
    private val scanner: MediaScanner,
    private val folderRepository: FolderRepository
) {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )
    private var started = false

    private val cacheKey = stringPreferencesKey("tracks")

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())

    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private var observerRegistered = false

    private val observer = object : ContentObserver(
        Handler(Looper.getMainLooper())
    ) {
        override fun onChange(selfChange: Boolean) {
            refresh()
        }
    }

    fun start() {
        if (started) return
        started = true

        scope.launch {
            loadCache()
            registerObserver()

            folderRepository.folders.collect {
                refresh()
            }
        }
    }

    private suspend fun loadCache() {
        val preferences = context.libraryDataStore.data.first()
        val json = preferences[cacheKey] ?: return

        try {
            val array = JSONArray(json)
            val result = mutableListOf<Track>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                result.add(
                    Track(
                        id = item.getLong("id"),
                        uri = item.getString("uri"),
                        title = item.getString("title"),
                        artist = item.getString("artist"),
                        album = item.getString("album"),
                        durationMs = item.getLong("durationMs"),
                        folder = item.getString("folder")
                    )
                )
            }

            _tracks.value = result

        } catch (_: Exception) {
            _tracks.value = emptyList()
        }
    }

    fun refresh() {
        scope.launch {
            try {
                val folders = folderRepository.getFolders()
                val result = scanner.scan(folders)

                _tracks.value = result
                saveCache(result)

            } catch (_: SecurityException) {
                // Keep cached tracks until permission is granted.
            }
        }
    }

    private suspend fun saveCache(tracks: List<Track>) {
        val array = JSONArray()

        tracks.forEach { track ->
            array.put(
                JSONObject().apply {
                    put("id", track.id)
                    put("uri", track.uri)
                    put("title", track.title)
                    put("artist", track.artist)
                    put("album", track.album)
                    put("durationMs", track.durationMs)
                    put("folder", track.folder)
                }
            )
        }

        context.libraryDataStore.edit { preferences ->
            preferences[cacheKey] = array.toString()
        }
    }

    private fun registerObserver() {
        if (observerRegistered) return

        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )

        observerRegistered = true
    }

    fun groupByFolder(): Map<String, List<Track>> {
        return tracks.value.groupBy { it.folder }
    }

    fun groupByAlbum(): Map<String, List<Track>> {
        return tracks.value.groupBy { it.album }
    }

    fun close() {
        if (observerRegistered) {
            context.contentResolver.unregisterContentObserver(observer)
            observerRegistered = false
        }
    }
}
