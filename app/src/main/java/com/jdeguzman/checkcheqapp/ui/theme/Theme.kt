package com.jdeguzman.checkcheqapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

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
 * @param fontScale relative text scale (1.0 = default, >1.0 = larger text).
 */
@Composable
fun CheckCheqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val baseTypography = Typography

    // Scale a few core styles (you can add more if you want)
    val scaledTypography = baseTypography.copy(
        bodySmall = baseTypography.bodySmall.copy(
            fontSize = baseTypography.bodySmall.fontSize * fontScale
        ),
        bodyMedium = baseTypography.bodyMedium.copy(
            fontSize = baseTypography.bodyMedium.fontSize * fontScale
        ),
        titleMedium = baseTypography.titleMedium.copy(
            fontSize = baseTypography.titleMedium.fontSize * fontScale
        ),
        headlineSmall = baseTypography.headlineSmall?.copy(
            fontSize = (baseTypography.headlineSmall.fontSize * fontScale)
        ) ?: baseTypography.headlineSmall
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        shapes = Shapes,
        content = content
    )
}