package com.example.parrot.player


import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.parrot.data.Track
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerState(
    val item: MediaItem? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<MediaItem> = emptyList(),
    val shuffle: Boolean = false,
    val repeat: Int = Player.REPEAT_MODE_OFF
)

class PlayerConnection(context: Context) {

    private val appContext = context.applicationContext

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(
            player: Player,
            events: Player.Events
        ) {
            updateState()
        }
    }

    init {
        val token = SessionToken(
            appContext,
            ComponentName(appContext, PlaybackService::class.java)
        )

        val future = MediaController.Builder(appContext, token)
            .buildAsync()

        future.addListener(
            {
                try {
                    controller = future.get()
                    controller?.addListener(listener)
                    updateState()
                    startPositionUpdates()
                } catch (_: Exception) {
                    // Controller connection failed.
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun updateState() {
        val player = controller ?: return

        val duration = player.duration.takeIf {
            it != C_TIME_UNSET && it >= 0L
        } ?: 0L

        _state.value = PlayerState(
            item = player.currentMediaItem,
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = duration,
            queue = (0 until player.mediaItemCount).map {
                player.getMediaItemAt(it)
            },
            shuffle = player.shuffleModeEnabled,
            repeat = player.repeatMode
        )
    }

    private fun startPositionUpdates() {
        scope.launch {
            while (true) {
                updateState()
                delay(500)
            }
        }
    }

    fun play(list: List<Track>, startIndex: Int) {
        val player = controller ?: return
        if (list.isEmpty()) return

        val items = list.map { track ->
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

        val index = startIndex.coerceIn(0, items.lastIndex)

        player.setMediaItems(items, index, 0L)
        player.prepare()
        player.play()
        updateState()
    }

    fun toggle() {
        val player = controller ?: return

        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }

        updateState()
    }

    fun next() {
        controller?.seekToNext()
        updateState()
    }

    fun prev() {
        controller?.seekToPrevious()
        updateState()
    }

    fun seek(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
        updateState()
    }

    fun setShuffle(enabled: Boolean) {
        controller?.shuffleModeEnabled = enabled
        updateState()
    }

    fun setRepeat(mode: Int) {
        controller?.repeatMode = mode
        updateState()
    }

    fun release() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        scope.cancel()
    }

    private companion object {
        const val C_TIME_UNSET = -9223372036854775807L
    }
}
