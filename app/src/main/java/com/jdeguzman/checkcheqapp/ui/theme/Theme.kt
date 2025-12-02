package com.jdeguzman.checkcheqapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Shapes = Shapes()

private val LightColors = lightColorScheme(
    primary = CheckCheqGreen,
    onPrimary = Color.White,
    secondary = CheckCheqGreenDark,
    onSecondary = Color.Black,
    secondaryContainer = CheckCheqGreenContainer.copy(alpha = 0.1f),
    onSecondaryContainer = CheckCheqGreen,
    tertiary = CheckCheqGreenDark,
    surface = Color(0xFF101010),
    background = Color(0xFF101010)
)

private val DarkColors = darkColorScheme(
    primary = CheckCheqGreen,
    onPrimary = Color.Black,
    secondary = CheckCheqGreenDark,
    onSecondary = Color.Black,
    secondaryContainer = CheckCheqGreenContainer,
    onSecondaryContainer = Color(0xFFB9F6CA)
)

@Composable
fun CheckCheqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
