package com.mycompany.jainconnect.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Singleton to manage dark mode state across the app
 */
object ThemeState {
    var isDarkMode by mutableStateOf(false)
    
    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }
}
