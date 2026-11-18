package com.example.parrot.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String) {
    DARK_TEAL("Dark Teal"),
    DARK_PURPLE("Dark Purple"),
    DARK_AMBER("Dark Amber"),
    LIGHT("Light")
}

fun AppTheme.toColorScheme(): ColorScheme = when (this) {
    AppTheme.DARK_TEAL -> darkColorScheme(
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
    AppTheme.DARK_PURPLE -> darkColorScheme(
        primary = Color(0xFFCFBCFF),
        onPrimary = Color(0xFF36256A),
        secondary = Color(0xFFCBC2DB),
        onSecondary = Color(0xFF332D41),
        tertiary = Color(0xFFEBB8D5),
        onTertiary = Color(0xFF4A253F),
        background = Color(0xFF13121A),
        onBackground = Color(0xFFE6E1E9),
        surface = Color(0xFF1B1A22),
        onSurface = Color(0xFFE6E1E9),
        surfaceVariant = Color(0xFF292832),
        onSurfaceVariant = Color(0xFFC9C5D0),
        outline = Color(0xFF938F99),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005)
    )
    AppTheme.DARK_AMBER -> darkColorScheme(
        primary = Color(0xFFFFC46B),
        onPrimary = Color(0xFF452B00),
        secondary = Color(0xFFE0C38C),
        onSecondary = Color(0xFF3E2E10),
        tertiary = Color(0xFFB9CC9A),
        onTertiary = Color(0xFF253316),
        background = Color(0xFF15130E),
        onBackground = Color(0xFFE8E2D6),
        surface = Color(0xFF1D1B15),
        onSurface = Color(0xFFE8E2D6),
        surfaceVariant = Color(0xFF2C2A22),
        onSurfaceVariant = Color(0xFFCCC6BA),
        outline = Color(0xFF96908A),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005)
    )
    AppTheme.LIGHT -> androidx.compose.material3.lightColorScheme(
        primary = Color(0xFF006A60),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF4A635F),
        onSecondary = Color(0xFFFFFFFF),
        tertiary = Color(0xFF456179),
        onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFFAFDFB),
        onBackground = Color(0xFF191C1B),
        surface = Color(0xFFFAFDFB),
        onSurface = Color(0xFF191C1B),
        surfaceVariant = Color(0xFFDAE5E1),
        onSurfaceVariant = Color(0xFF3F4947),
        outline = Color(0xFF6F7977),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF)
    )
}