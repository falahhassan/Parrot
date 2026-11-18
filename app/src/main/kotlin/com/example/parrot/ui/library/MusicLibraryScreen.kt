package com.example.parrot.ui.library

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.parrot.AppContainer
import com.example.parrot.ui.MiniPlayer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicLibraryScreen(
    container: AppContainer,
    onOpenSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val playerState by container.playerConnection.state.collectAsState()
    val tracks by container.libraryRepository.tracks.collectAsState()
    val folders by container.folderRepository.folders.collectAsState(initial = emptyList())

    val permission = remember {
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    val openFolderPicker = rememberFolderPicker(scope) { folder ->
        container.folderRepository.addFolder(folder)
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) container.libraryRepository.start()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parrot") },
                actions = {
                    TextButton(onClick = openFolderPicker) { Text("Add Folder") }
                    TextButton(onClick = onOpenSettings) {
                        Text("Settings")
                    }
                }
            )
        }
    ) { padding ->
        if (!hasPermission) {
            PermissionGate(
                modifier = Modifier.padding(padding),
                onRequest = { permissionLauncher.launch(permission) }
            )
        } else {
            LibraryContent(
                modifier = Modifier.padding(padding),
                folders = folders,
                tracks = tracks,
                onRemoveFolder = { id ->
                    scope.launch { container.folderRepository.removeFolder(id) }
                },
                onTrackClick = { track ->
                    container.playerConnection.play(tracks, tracks.indexOf(track))
                },
                playerState = playerState,
                onPlayPause = { container.playerConnection.toggle() },
                onPrevious = { container.playerConnection.prev() },
                onNext = { container.playerConnection.next() },
                onSeek = { container.playerConnection.seek(it) },
                onShuffle = {
                    container.playerConnection.setShuffle(!playerState.shuffle)
                },
                onRepeat = {
                    val nextMode = when (playerState.repeat) {
                        0 -> 2
                        2 -> 1
                        else -> 0
                    }
                    container.playerConnection.setRepeat(nextMode)
                }
            )
        }
    }
}
