package com.example.parrot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.parrot.data.ThemePreferences

@Composable
fun ParrotTheme(
    themePreferences: ThemePreferences,
    content: @Composable () -> Unit
) {
    val selectedTheme by themePreferences.theme.collectAsState()

    MaterialTheme(
        colorScheme = selectedTheme.toColorScheme(),
        content = content
    )
}