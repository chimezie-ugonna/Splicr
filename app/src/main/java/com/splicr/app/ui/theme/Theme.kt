package com.splicr.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

private val DarkColorScheme = darkColorScheme(
    primary = brandLemon,
    secondary = PurpleGrey80,
    tertiary = border,
    background = black,
    onBackground = white
)

private val LightColorScheme = lightColorScheme(
    primary = brandLemon,
    secondary = PurpleGrey40,
    tertiary = border,
    background = black,
    onBackground = white
)

@Composable
fun SplicrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    val window = (view.context as Activity).window
    window.statusBarColor = colorScheme.background.toArgb()
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    window.navigationBarColor = colorScheme.background.toArgb()
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography, shapes = Shapes, content = content
    )
}