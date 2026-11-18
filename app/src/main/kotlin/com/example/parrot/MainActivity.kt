
package com.example.parrot

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.parrot.data.Track
import com.example.parrot.data.MusicFolder
import com.example.parrot.ui.theme.CarPlayerTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.rememberCoroutineScope
import com.example.parrot.ui.MiniPlayer

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        appContainer = AppContainer(this)

        setContent {
            CarPlayerTheme {
                MusicLibraryScreen(appContainer)
            }
        }
    }

    override fun onDestroy() {
        if (::appContainer.isInitialized && isFinishing) {
            appContainer.libraryRepository.close()
        }
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicLibraryScreen(container: AppContainer) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val playerState by container.playerConnection.state.collectAsState()

    val permission = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val tracks by container.libraryRepository.tracks
    .collectAsState()

    val folders by container.folderRepository.folders
    .collectAsState(initial = emptyList())

    val permissionLauncher =
    rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    val folderPicker =
    rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->

        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val folderName = try {
                    DocumentsContract.getTreeDocumentId(uri)
                    .substringAfterLast("/")
                    .substringAfterLast(":")
                } catch (_: Exception) {
                    "Selected Folder"
                }

                scope.launch {
                    container.folderRepository.addFolder(
                        MusicFolder(
                            id = uri.toString(),
                            name = folderName.ifBlank {
                                "Selected Folder"
                            },
                            location = uri.toString(),
                            isTreeUri = true
                        )
                    )
                }
            } catch (_: SecurityException) {
                // Permission could not be retained.
            }
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            container.libraryRepository.start()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CarPlayer") },
                actions = {
                    TextButton(
                        onClick = {
                            folderPicker.launch(null)
                        }
                    ) {
                        Text("Add Folder")
                    }
                }
            )
        }
    ) { padding ->

        if (!hasPermission) {
            Column(
                modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Allow access to your local music.")

                Button(
                    onClick = {
                        permissionLauncher.launch(permission)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Grant Permission")
                }
            }
        } else {
            Column(
                modifier = Modifier
                .fillMaxSize()
                .padding(padding)
            ) {
                Text(
                    text = "Music Folders",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                folders.forEach { folder ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(folder.name)

                        TextButton(
                            onClick = {
                                scope.launch {
                                    container.folderRepository.removeFolder(folder.id)
                                }
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }

                HorizontalDivider()

                LibraryTrackList(
                    tracks = tracks,
                    onTrackClick = { selectedTrack ->
                        container.playerConnection.play(
                            tracks,
                            tracks.indexOf(selectedTrack)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                if (playerState.item != null) {
                    MiniPlayer(
                        state = playerState,
                        onPlayPause = {
                            container.playerConnection.toggle()
                        },
                        onPrevious = {
                            container.playerConnection.prev()
                        },
                        onNext = {
                            container.playerConnection.next()
                        },
                        onSeek = {
                            container.playerConnection.seek(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryTrackList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier
)
{
    if (tracks.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No music found")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            items(
                items = tracks,
                key = { it.id }
            ) { track ->
                Column(
                    modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onTrackClick(track)
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()
            }
        }
    }
}
