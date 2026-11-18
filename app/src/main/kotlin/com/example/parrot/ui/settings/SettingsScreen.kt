package com.example.parrot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parrot.AppContainer
import com.example.parrot.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val selected by container.themePreferences.theme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            AppTheme.entries.forEach { theme ->
                Row(
                    modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = theme == selected,
                        onClick = { container.themePreferences.setTheme(theme) }
                    )
                    .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = theme == selected,
                        onClick = { container.themePreferences.setTheme(theme) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(theme.displayName, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
