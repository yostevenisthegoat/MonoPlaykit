package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Official Mono Dark Palette
val MonoBgMain = Color(0xFF080808)
val MonoBgSecondary = Color(0xFF111111)
val MonoCardPrimary = Color(0xFF191919)
val MonoCardElevated = Color(0xFF222222)
val MonoSurfaceSecondary = Color(0xFF292929)
val MonoBorder = Color(0xFF303030)
val MonoTextPrimary = Color(0xFFFFFFFF)
val MonoTextSecondary = Color(0xFFA5A5A5)
val MonoTextMuted = Color(0xFF707070)
val MonoAccent = Color(0xFFE8E8E8)

// Optional Mono Light Palette
val MonoLightBgMain = Color(0xFFF4F4F4)
val MonoLightBgSecondary = Color(0xFFEBEBEB)
val MonoLightCardPrimary = Color(0xFFFFFFFF)
val MonoLightCardElevated = Color(0xFFF9F9F9)
val MonoLightSurfaceSecondary = Color(0xFFE2E2E2)
val MonoLightBorder = Color(0xFFD2D2D2)
val MonoLightTextPrimary = Color(0xFF0E0E0E)
val MonoLightTextSecondary = Color(0xFF545454)
val MonoLightTextMuted = Color(0xFF8A8A8A)
val MonoLightAccent = Color(0xFF181818)

// Restricted Functional Accents
val MonoTeamRed = Color(0xFFD9383A)
val MonoTeamBlue = Color(0xFF2E6FD8)
val MonoSuccess = Color(0xFF34A853)
val MonoWarning = Color(0xFFE69138)

@Immutable
data class MonoPalette(
    val isDark: Boolean,
    val bgMain: Color,
    val bgSecondary: Color,
    val cardPrimary: Color,
    val cardElevated: Color,
    val surfaceSecondary: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val teamRed: Color = MonoTeamRed,
    val teamBlue: Color = MonoTeamBlue,
    val success: Color = MonoSuccess,
    val warning: Color = MonoWarning,
)

val DarkMonoPalette = MonoPalette(
    isDark = true,
    bgMain = MonoBgMain,
    bgSecondary = MonoBgSecondary,
    cardPrimary = MonoCardPrimary,
    cardElevated = MonoCardElevated,
    surfaceSecondary = MonoSurfaceSecondary,
    border = MonoBorder,
    textPrimary = MonoTextPrimary,
    textSecondary = MonoTextSecondary,
    textMuted = MonoTextMuted,
    accent = MonoAccent,
    onAccent = MonoBgMain,
)

val LightMonoPalette = MonoPalette(
    isDark = false,
    bgMain = MonoLightBgMain,
    bgSecondary = MonoLightBgSecondary,
    cardPrimary = MonoLightCardPrimary,
    cardElevated = MonoLightCardElevated,
    surfaceSecondary = MonoLightSurfaceSecondary,
    border = MonoLightBorder,
    textPrimary = MonoLightTextPrimary,
    textSecondary = MonoLightTextSecondary,
    textMuted = MonoLightTextMuted,
    accent = MonoLightAccent,
    onAccent = Color(0xFFFFFFFF),
)

val LocalMonoPalette = staticCompositionLocalOf { DarkMonoPalette }
