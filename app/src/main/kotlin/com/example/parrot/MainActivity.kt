package com.example.parrot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.parrot.ui.ParrotApp
import com.example.parrot.ui.theme.ParrotTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = AppContainer(this)

        setContent {
            ParrotTheme(appContainer.themePreferences) {
                ParrotApp(appContainer)
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