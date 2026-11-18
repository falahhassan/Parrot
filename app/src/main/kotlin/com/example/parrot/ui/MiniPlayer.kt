package com.example.parrot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parrot.player.PlayerState

@Composable
fun MiniPlayer(
    state: PlayerState,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val metadata = state.item?.mediaMetadata

    val title = metadata?.title?.toString()
        ?: "Unknown Title"

    val artist = metadata?.artist?.toString()
        ?: "Unknown Artist"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // Song information
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )

            Text(
                text = artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            // Progress
            Slider(
                value = state.positionMs.toFloat(),
                onValueChange = {
                    onSeek(it.toLong())
                },
                valueRange = 0f..state.durationMs
                    .coerceAtLeast(1L).toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = formatTime(state.positionMs),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onPrevious) {
                    Text("⏮")
                }

                IconButton(onClick = onPlayPause) {
                    Text(
                        if (state.isPlaying) "⏸" else "▶",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                IconButton(onClick = onNext) {
                    Text("⏭")
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = formatTime(state.durationMs),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%d:%02d".format(minutes, seconds)
}