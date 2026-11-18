package com.example.parrot.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parrot.data.MusicFolder
import com.example.parrot.player.PlayerState
import com.example.parrot.data.Track
import com.example.parrot.ui.MiniPlayer

@Composable
fun LibraryContent(
    modifier: Modifier = Modifier,
    folders: List<MusicFolder>,
    tracks: List<Track>,
    onRemoveFolder: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    playerState: PlayerState,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Music Folders",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        folders.forEach { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(folder.name, modifier = Modifier.weight(1f))
                TextButton(onClick = { onRemoveFolder(folder.id) }) {
                    Text("Remove")
                }
            }
        }

        HorizontalDivider()

        LibraryTrackList(
            tracks = tracks,
            onTrackClick = onTrackClick,
            modifier = Modifier.weight(1f)
        )

        if (playerState.item != null) {
            MiniPlayer(
                state = playerState,
                onPlayPause = onPlayPause,
                onPrevious = onPrevious,
                onNext = onNext,
                onSeek = onSeek,
                onShuffle = onShuffle,
                onRepeat = onRepeat
            )
        }
    }
}