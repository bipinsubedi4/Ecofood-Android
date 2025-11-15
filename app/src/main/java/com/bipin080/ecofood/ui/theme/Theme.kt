package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    secondary = DarkGreen,
    onSecondary = Color.White,
    background = LightGray,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun EcoFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Use the renamed typography
        content = content
    )
}
