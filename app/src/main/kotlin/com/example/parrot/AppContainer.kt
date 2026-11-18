
package com.example.parrot

import android.content.Context
import com.example.parrot.data.FolderRepository
import com.example.parrot.data.LibraryRepository
import com.example.parrot.data.MediaScanner
import com.example.parrot.player.PlayerConnection
import com.example.parrot.data.ThemePreferences

class AppContainer(context: Context) {

    val applicationContext: Context = context.applicationContext
    val themePreferences = ThemePreferences(applicationContext)
    val playerConnection = PlayerConnection(applicationContext)

    val mediaScanner = MediaScanner(applicationContext)
    val folderRepository = FolderRepository(applicationContext)

    val libraryRepository = LibraryRepository(
        applicationContext,
        mediaScanner,
        folderRepository
    )

    
}
