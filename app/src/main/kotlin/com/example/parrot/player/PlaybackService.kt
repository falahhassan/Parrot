
package com.example.parrot.player

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.parrot.data.MediaScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking

class PlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var sessionStore: SessionStore

    private var mediaSession: MediaSession? = null

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        sessionStore = SessionStore(this)

        player = ExoPlayer.Builder(this).build().apply {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()

            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)

            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (!isPlaying) {
                        saveSession()
                    }
                }
            })
        }

        mediaSession = MediaSession.Builder(this, player).build()

        restoreSession()

        serviceScope.launch {
            while (isActive) {
                delay(5_000)
                saveSession()
            }
        }
    }

    private fun restoreSession() {
        serviceScope.launch {
            val saved = withContext(Dispatchers.IO) {
                sessionStore.restore()
            }

            if (saved.queueIds.isEmpty()) return@launch

            val tracks = withContext(Dispatchers.IO) {
                MediaScanner(this@PlaybackService).scan()
            }

            val trackMap = tracks.associateBy { it.id }

            val restoredTracks = saved.queueIds.mapNotNull {
                trackMap[it]
            }

            if (restoredTracks.isEmpty()) return@launch

            val items = restoredTracks.map { track ->
                MediaItem.Builder()
                    .setMediaId(track.id.toString())
                    .setUri(track.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setAlbumTitle(track.album)
                            .build()
                    )
                    .build()
            }

            val index = saved.index.coerceIn(
                0,
                items.lastIndex
            )

            player.setMediaItems(
                items,
                index,
                saved.positionMs
            )

            player.prepare()
        }
    }

    private fun saveSession() {
        if (!::player.isInitialized) return

        val queueIds = (0 until player.mediaItemCount)
            .mapNotNull {
                player.getMediaItemAt(it).mediaId.toLongOrNull()
            }

        if (queueIds.isEmpty()) return

        val saved = SavedSession(
            queueIds = queueIds,
            index = player.currentMediaItemIndex,
            positionMs = player.currentPosition
        )

        serviceScope.launch(Dispatchers.IO) {
            sessionStore.save(saved)
        }
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    override fun onDestroy() {
        if (::player.isInitialized) {
            val queueIds = (0 until player.mediaItemCount)
                .mapNotNull {
                    player.getMediaItemAt(it).mediaId.toLongOrNull()
                }

            if (queueIds.isNotEmpty()) {
                val saved = SavedSession(
                    queueIds = queueIds,
                    index = player.currentMediaItemIndex,
                    positionMs = player.currentPosition
                )

                runBlocking(Dispatchers.IO) {
                    sessionStore.save(saved)
                }
            }

            player.release()
        }

        mediaSession?.release()
        mediaSession = null

        serviceScope.cancel()

        super.onDestroy()
    }
}
