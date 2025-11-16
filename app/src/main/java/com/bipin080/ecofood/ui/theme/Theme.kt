package com.bipin080.ecofood.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = White,
    secondary = SecondaryIndigo,
    onSecondary = White,
    tertiary = AccentCoral, // Using tertiary for the accent color
    onTertiary = White,
    background = BackgroundCream,
    onBackground = TextCharcoal,
    surface = White,
    onSurface = TextCharcoal,
    error = ErrorRed,
    onError = White
)

@Composable
fun EcoFoodTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
