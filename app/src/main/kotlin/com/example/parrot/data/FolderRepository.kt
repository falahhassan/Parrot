
package com.example.parrot.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import android.content.Intent
import android.net.Uri

private val Context.folderDataStore by preferencesDataStore(
    name = "music_folders"
)

data class MusicFolder(
    val id: String,
    val name: String,
    val location: String,
    val isTreeUri: Boolean
)

class FolderRepository(context: Context) {

    private val appContext = context.applicationContext
    private val foldersKey = stringPreferencesKey("folders")

    val folders: Flow<List<MusicFolder>> =
    appContext.folderDataStore.data.map { preferences ->
        val saved = preferences[foldersKey]

        if (saved == null) {
            listOf(
                MusicFolder(
                    id = "default",
                    name = "CarPlayer",
                    location = "Music/CarPlayer",
                    isTreeUri = false
                )
            )
        } else {
            decodeFolders(saved)
        }
    }

    suspend fun addFolder(folder: MusicFolder) {
        appContext.folderDataStore.edit { preferences ->
            val current = preferences[foldersKey]?.let {
                decodeFolders(it)
            } ?: listOf(
                MusicFolder(
                    id = "default",
                    name = "CarPlayer",
                    location = "Music/CarPlayer",
                    isTreeUri = false
                )
            )

            if (current.none { it.location == folder.location }) {
                preferences[foldersKey] =
                encodeFolders(current + folder)
            }
        }
    }

    suspend fun removeFolder(folderId: String) {
        var removedFolder: MusicFolder? = null

        appContext.folderDataStore.edit { preferences ->
            val current = preferences[foldersKey]?.let {
                decodeFolders(it)
            } ?: listOf(
                MusicFolder(
                    id = "default",
                    name = "CarPlayer",
                    location = "Music/CarPlayer",
                    isTreeUri = false
                )
            )

            removedFolder = current.firstOrNull { it.id == folderId }

            preferences[foldersKey] =
            encodeFolders(current.filterNot { it.id == folderId })
        }

        removedFolder?.let { folder ->
            if (folder.isTreeUri) {
                try {
                    appContext.contentResolver.releasePersistableUriPermission(
                        Uri.parse(folder.location),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                }
            }
        }
    }

    suspend fun getFolders(): List<MusicFolder> {
        return folders.first()
    }

    private fun encodeFolders(
        folders: List<MusicFolder>
    ): String {
        val array = JSONArray()

        folders.forEach { folder ->
            array.put(
                JSONObject().apply {
                    put("id", folder.id)
                    put("name", folder.name)
                    put("location", folder.location)
                    put("isTreeUri", folder.isTreeUri)
                }
            )
        }

        return array.toString()
    }

    private fun decodeFolders(
        json: String
    ): List<MusicFolder> {
        if (json.isBlank()) return emptyList()

        return try {
            val array = JSONArray(json)

            List(array.length()) { index ->
                val item = array.getJSONObject(index)

                MusicFolder(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    location = item.getString("location"),
                    isTreeUri = item.getBoolean("isTreeUri")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
