package com.example.parrot.ui.library

import android.content.Intent
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.parrot.data.MusicFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberFolderPicker(
    scope: CoroutineScope,
    onFolderPicked: suspend (MusicFolder) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

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
                onFolderPicked(
                    MusicFolder(
                        id = uri.toString(),
                        name = folderName.ifBlank { "Selected Folder" },
                        location = uri.toString(),
                        isTreeUri = true
                    )
                )
            }
        } catch (_: SecurityException) {
            // Permission could not be retained.
        }
    }

    return { launcher.launch(null) }
}