package com.jdeguzman.checkcheqapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Shapes = Shapes()

// ✅ Light mode palette
private val LightColors = lightColorScheme(
    primary = CheckCheqGreen,
    onPrimary = Color.White,
    secondary = CheckCheqGreenDark,
    onSecondary = Color.Black,
    secondaryContainer = CheckCheqGreenContainer.copy(alpha = 0.08f),
    onSecondaryContainer = CheckCheqGreen,
    surface = Color(0xFFFDFDFD),      // light surface
    background = Color(0xFFF5F5F5),   // light background
    onSurface = Color(0xFF121212),
    onBackground = Color(0xFF121212)
)

// ✅ Dark mode palette
private val DarkColors = darkColorScheme(
    primary = CheckCheqGreen,
    onPrimary = Color.Black,
    secondary = CheckCheqGreenDark,
    onSecondary = Color.Black,
    secondaryContainer = CheckCheqGreenContainer,
    onSecondaryContainer = Color(0xFFB9F6CA),
    surface = Color(0xFF101010),
    background = Color(0xFF000000),
    onSurface = Color(0xFFE0E0E0),
    onBackground = Color(0xFFE0E0E0)
)

/**
 * App theme wrapper.
 *
 * @param darkTheme if true use [DarkColors], otherwise [LightColors].
 */
@Composable
fun CheckCheqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
