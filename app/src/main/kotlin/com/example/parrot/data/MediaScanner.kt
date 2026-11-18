
package com.example.parrot.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaScanner(
    private val context: Context
) {

    companion object {
        const val MUSIC_FOLDER = "Music/CarPlayer"
    }

    suspend fun scan(): List<Track> {
        return scan(
            listOf(
                MusicFolder(
                    id = "default",
                    name = "CarPlayer",
                    location = MUSIC_FOLDER,
                    isTreeUri = false
                )
            )
        )
    }

    suspend fun scan(
        folders: List<MusicFolder>
    ): List<Track> = withContext(Dispatchers.IO) {

        val tracks = mutableListOf<Track>()

        folders.forEach { folder ->
            try {
                if (folder.isTreeUri) {
                    scanTree(
                        Uri.parse(folder.location),
                        folder.name,
                        tracks
                    )
                } else {
                    scanMediaStoreFolder(
                        folder.location,
                        tracks
                    )
                }
            } catch (_: Exception) {
                // Continue scanning the remaining folders.
            }
        }

        tracks.distinctBy { it.uri }
            .sortedBy { it.title.lowercase() }
    }

    private fun scanMediaStoreFolder(
        folder: String,
        tracks: MutableList<Track>
    ) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val modern = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val folderColumn = if (modern) {
            MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            MediaStore.Audio.Media.DATA
        }

        val selection = if (modern) {
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND $folderColumn = ?"
        } else {
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND $folderColumn LIKE ?"
        }

        val folderArgument = if (modern) {
            "${folder.trimEnd('/')}/"
        } else {
            "%/${folder.trim('/')}/%"
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            folderColumn
        )

        context.contentResolver.query(
            collection,
            projection,
            selection,
            arrayOf(folderArgument),
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->

            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val folderIndex = cursor.getColumnIndexOrThrow(folderColumn)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val contentUri = ContentUris.withAppendedId(collection, id)

                val folderValue = cursor.getString(folderIndex) ?: ""

                tracks.add(
                    Track(
                        id = id,
                        uri = contentUri.toString(),
                        title = cursor.getString(titleIndex) ?: "Unknown",
                        artist = cursor.getString(artistIndex)
                            ?.takeUnless { it == "<unknown>" }
                            ?: "Unknown Artist",
                        album = cursor.getString(albumIndex)
                            ?.takeUnless { it == "<unknown>" }
                            ?: "Unknown Album",
                        durationMs = cursor.getLong(durationIndex),
                        folder = if (modern) {
                            folderValue.trimEnd('/')
                        } else {
                            folderValue.substringBeforeLast("/")
                                .substringAfterLast("/")
                        }
                    )
                )
            }
        }
    }

    private fun scanTree(
        treeUri: Uri,
        folderName: String,
        tracks: MutableList<Track>
    ) {
        val rootId = DocumentsContract.getTreeDocumentId(treeUri)

        scanTreeChildren(
            treeUri = treeUri,
            parentId = rootId,
            folderName = folderName,
            tracks = tracks
        )
    }

    private fun scanTreeChildren(
        treeUri: Uri,
        parentId: String,
        folderName: String,
        tracks: MutableList<Track>
    ) {
        val childrenUri =
            DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri,
                parentId
            )

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )

        context.contentResolver.query(
            childrenUri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->

            val idIndex = cursor.getColumnIndexOrThrow(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID
            )

            val nameIndex = cursor.getColumnIndexOrThrow(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            )

            val mimeIndex = cursor.getColumnIndexOrThrow(
                DocumentsContract.Document.COLUMN_MIME_TYPE
            )

            while (cursor.moveToNext()) {
                val documentId = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex) ?: "Unknown"
                val mimeType = cursor.getString(mimeIndex) ?: ""

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    scanTreeChildren(
                        treeUri,
                        documentId,
                        "$folderName/$name",
                        tracks
                    )
                } else if (mimeType.startsWith("audio/")) {
                    val documentUri =
                        DocumentsContract.buildDocumentUriUsingTree(
                            treeUri,
                            documentId
                        )

                    readAudioTrack(
                        documentUri,
                        name,
                        folderName,
                        tracks
                    )
                }
            }
        }
    }

    private fun readAudioTrack(
        uri: Uri,
        fileName: String,
        folderName: String,
        tracks: MutableList<Track>
    ) {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, uri)

            val title = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_TITLE
            ) ?: fileName.substringBeforeLast(".")

            val artist = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_ARTIST
            ) ?: "Unknown Artist"

            val album = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_ALBUM
            ) ?: "Unknown Album"

            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L

            val stableId =
                (1L shl 62) + (uri.toString().hashCode().toLong() and 0xFFFFFFFFL)

            tracks.add(
                Track(
                    id = stableId,
                    uri = uri.toString(),
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = duration,
                    folder = folderName
                )
            )
        } catch (_: Exception) {
            // Skip files whose metadata cannot be read.
        } finally {
            retriever.release()
        }
    }
}
