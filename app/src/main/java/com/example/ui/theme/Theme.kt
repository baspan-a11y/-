package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryGreen,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkMintText,
    secondary = ChampionshipGold,
    onSecondary = Color(0xFF281D00),
    secondaryContainer = Color(0xFF433100),
    onSecondaryContainer = SoftGoldContainer,
    tertiary = BrightClover,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = DarkSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = Color(0xFF233529),
    error = Color(0xFFF87171),
    errorContainer = ErrorContainerDark,
    onErrorContainer = Color(0xFFFECACA)
)

private val LightColorScheme = lightColorScheme(
    primary = ShamrockGreen,
    onPrimary = Color.White,
    primaryContainer = MintMist,
    onPrimaryContainer = DeepForestGreen,
    secondary = ChampionshipGold,
    onSecondary = Color.White,
    secondaryContainer = SoftGoldContainer,
    onSecondaryContainer = DarkGoldText,
    tertiary = BrightClover,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = MintSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceVariant,
    outline = LightOutline,
    outlineVariant = Color(0xFFDCE6DF),
    error = LiveRed,
    errorContainer = ErrorContainerLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PrasinaNeaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
