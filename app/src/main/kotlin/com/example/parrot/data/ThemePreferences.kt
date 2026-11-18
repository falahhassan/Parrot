package com.example.parrot.data

import android.content.Context
import com.example.parrot.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private fun loadTheme(): AppTheme {
        val saved = prefs.getString(KEY_THEME, null)
        return AppTheme.entries.firstOrNull { it.name == saved }
            ?: AppTheme.DARK_TEAL
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _theme.value = theme
    }

    companion object {
        private const val KEY_THEME = "selected_theme"
    }
}