package com.example.parrot.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.parrot.AppContainer
import com.example.parrot.ui.library.MusicLibraryScreen
import com.example.parrot.ui.settings.SettingsScreen

sealed interface Screen {
    data object Library : Screen
    data object Settings : Screen
}

@Composable
fun ParrotApp(container: AppContainer) {
    var screen by remember { mutableStateOf<Screen>(Screen.Library) }

    when (screen) {
        Screen.Library -> MusicLibraryScreen(
            container = container,
            onOpenSettings = { screen = Screen.Settings }
        )
        Screen.Settings -> SettingsScreen(
            container = container,
            onBack = { screen = Screen.Library }
        )
    }
}