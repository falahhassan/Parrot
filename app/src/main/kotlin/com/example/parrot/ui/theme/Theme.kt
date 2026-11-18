package com.example.parrot.ui.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CarPlayerDarkColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF003731),

    secondary = Color(0xFFB0CFC9),
    onSecondary = Color(0xFF193531),

    tertiary = Color(0xFFB5C9E8),
    onTertiary = Color(0xFF203047),

    background = Color(0xFF101413),
    onBackground = Color(0xFFE1E3E0),

    surface = Color(0xFF181D1C),
    onSurface = Color(0xFFE1E3E0),

    surfaceVariant = Color(0xFF29312F),
    onSurfaceVariant = Color(0xFFC0C9C6),

    outline = Color(0xFF899390),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun CarPlayerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CarPlayerDarkColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
