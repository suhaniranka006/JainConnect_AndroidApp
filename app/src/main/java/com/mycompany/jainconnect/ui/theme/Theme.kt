package com.mycompany.jainconnect.ui.theme

import android.app.Activity
import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
        darkColorScheme(
                primary = SaffronPrimary,
                onPrimary = PureWhite,
                primaryContainer = SaffronDark,
                onPrimaryContainer = GoldLight,
                secondary = GoldPrimary,
                onSecondary = DarkText,
                secondaryContainer = GoldDark,
                onSecondaryContainer = PureWhite,
                tertiary = SaffronLight,
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E),
                onBackground = SpiritualWhite,
                onSurface = SpiritualWhite,
                surfaceVariant = Color(0xFF2C2C2C),
                onSurfaceVariant = Color(0xFFCAC4D0),
                error = ErrorRed
        )

private val LightColorScheme =
        lightColorScheme(
                primary = SaffronPrimary,
                onPrimary = PureWhite,
                primaryContainer = SaffronLight,
                onPrimaryContainer = SaffronDark,
                secondary = GoldPrimary,
                onSecondary = DarkText,
                secondaryContainer = GoldLight,
                onSecondaryContainer = GoldDark,
                tertiary = SaffronDark,
                background = AppBackground,
                surface = CardSurface,
                onBackground = DarkText,
                onSurface = DarkText,
                surfaceVariant = Color.White, // Force pure white for cards
                onSurfaceVariant = Color.Black, // Darker text for better readability
                error = ErrorRed
        )

@Composable
fun JainConnectTheme(
        darkTheme: Boolean = ThemeState.isDarkMode,
        // Dynamic color is available on Android 12+
        dynamicColor: Boolean = false, // Disable dynamic color to enforce our premium branding
        content: @Composable () -> Unit
) {
        val colorScheme =
                when {
                        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                                val context = LocalContext.current
                                if (darkTheme) dynamicDarkColorScheme(context)
                                else dynamicLightColorScheme(context)
                        }
                        darkTheme -> DarkColorScheme
                        else -> LightColorScheme
                }
        val view = LocalView.current
        if (!view.isInEditMode) {
                SideEffect {
                        val window = (view.context as Activity).window
                        window.statusBarColor = if (darkTheme) Color(0xFF121212).toArgb() else Color.White.toArgb()
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                }
        }

        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
