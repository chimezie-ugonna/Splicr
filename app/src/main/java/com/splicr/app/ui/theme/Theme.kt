package com.splicr.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

private val DarkColorScheme = darkColorScheme(
    primary = brandLemon,
    tertiary = brandSilver,
    background = brandBlack,
    onBackground = brandWhite,
    secondary = brandBlack2,
    error = error,
    surface = lBlack,
    onSurface = brandGrey,
    inverseSurface = success,
    scrim = scrim,
    surfaceVariant = premiumGold,
    onTertiary = brandPurple
)

private val LightColorScheme = lightColorScheme(
    primary = brandLemon,
    tertiary = brandSilver,
    background = brandBlack,
    onBackground = brandWhite,
    secondary = brandBlack2,
    error = error,
    surface = lBlack,
    onSurface = brandGrey,
    inverseSurface = success,
    scrim = scrim,
    surfaceVariant = premiumGold,
    onTertiary = brandPurple
)

@Composable
fun SplicrTheme(
    isSystemInDarkTheme: Boolean = true,
    statusBarColor: Color = Color.Transparent,
    isAppearanceLightStatusBars: Boolean = false,
    navigationBarColor: Color = brandBlack,
    isAppearanceLightNavigationBars: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isSystemInDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = statusBarColor.toArgb()
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                isAppearanceLightStatusBars
            window.navigationBarColor = navigationBarColor.toArgb()
            WindowInsetsControllerCompat(
                window, window.decorView
            ).isAppearanceLightNavigationBars = !isAppearanceLightNavigationBars
        }
    }

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography, shapes = Shapes, content = content
    )
}