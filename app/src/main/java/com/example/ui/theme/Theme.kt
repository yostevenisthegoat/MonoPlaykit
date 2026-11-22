package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) {
    DARK("Dark Theme"),
    SYSTEM("System Theme"),
    LIGHT("Light Theme"),
}

private val MonoDarkColorScheme = darkColorScheme(
    primary = MonoAccent,
    onPrimary = MonoBgMain,
    primaryContainer = MonoSurfaceSecondary,
    onPrimaryContainer = MonoTextPrimary,
    secondary = MonoTextSecondary,
    onSecondary = MonoBgMain,
    secondaryContainer = MonoCardElevated,
    onSecondaryContainer = MonoTextPrimary,
    tertiary = MonoTextMuted,
    onTertiary = MonoTextPrimary,
    background = MonoBgMain,
    onBackground = MonoTextPrimary,
    surface = MonoBgSecondary,
    onSurface = MonoTextPrimary,
    surfaceVariant = MonoCardPrimary,
    onSurfaceVariant = MonoTextSecondary,
    surfaceContainerLowest = MonoBgMain,
    surfaceContainerLow = MonoBgSecondary,
    surfaceContainer = MonoCardPrimary,
    surfaceContainerHigh = MonoCardElevated,
    surfaceContainerHighest = MonoSurfaceSecondary,
    outline = MonoBorder,
    outlineVariant = MonoBorder,
    error = MonoTeamRed,
    onError = MonoTextPrimary,
)

private val MonoLightColorScheme = lightColorScheme(
    primary = MonoLightAccent,
    onPrimary = MonoLightCardPrimary,
    primaryContainer = MonoLightSurfaceSecondary,
    onPrimaryContainer = MonoLightTextPrimary,
    secondary = MonoLightTextSecondary,
    onSecondary = MonoLightCardPrimary,
    secondaryContainer = MonoLightCardElevated,
    onSecondaryContainer = MonoLightTextPrimary,
    tertiary = MonoLightTextMuted,
    onTertiary = MonoLightCardPrimary,
    background = MonoLightBgMain,
    onBackground = MonoLightTextPrimary,
    surface = MonoLightBgSecondary,
    onSurface = MonoLightTextPrimary,
    surfaceVariant = MonoLightCardPrimary,
    onSurfaceVariant = MonoLightTextSecondary,
    surfaceContainerLowest = MonoLightCardPrimary,
    surfaceContainerLow = MonoLightBgMain,
    surfaceContainer = MonoLightCardPrimary,
    surfaceContainerHigh = MonoLightCardElevated,
    surfaceContainerHighest = MonoLightSurfaceSecondary,
    outline = MonoLightBorder,
    outlineVariant = MonoLightBorder,
    error = MonoTeamRed,
    onError = MonoTextPrimary,
)

val MonoShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

object MonoTheme {
    val palette: MonoPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalMonoPalette.current
}

@Composable
fun MonoPlayKitTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val palette = if (useDark) DarkMonoPalette else LightMonoPalette
    val colorScheme = if (useDark) MonoDarkColorScheme else MonoLightColorScheme

    CompositionLocalProvider(LocalMonoPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MonoTypography,
            shapes = MonoShapes,
            content = content,
        )
    }
}
