package com.tcgvault.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// True OLED Graphite Minimal palette — neutral zinc/obsidian, zero blue/purple tint
val VaultBackground = Color(0xFF09090B)
val VaultSurface = Color(0xFF111114)
val VaultSurfaceElevated = Color(0xFF1A1A1F)
val VaultGold = Color(0xFFF59E0B)         // Used strictly for favorite stars & highlights
val VaultAccentWhite = Color(0xFFFAFAFA)  // Primary high-contrast ink & primary CTA
val VaultEmerald = Color(0xFF22C55E)      // Collected state & completion
val YugiohAccent = Color(0xFFEAB308)      // Subtle Millennium gold for YGO
val MtgAccent = Color(0xFFF97316)         // Subtle Planeswalker ember for MTG
val TextPrimary = Color(0xFFFAFAFA)
val TextSecondary = Color(0xFFA1A1AA)
val TextMuted = Color(0xFF71717A)
val SubtleBorder = Color(0xFF26262B)

val MonoFont = FontFamily.Monospace

private val VaultColorScheme = darkColorScheme(
    primary = VaultAccentWhite,
    onPrimary = VaultBackground,
    secondary = TextSecondary,
    onSecondary = VaultBackground,
    tertiary = VaultEmerald,
    onTertiary = VaultBackground,
    background = VaultBackground,
    onBackground = TextPrimary,
    surface = VaultSurface,
    onSurface = TextPrimary,
    surfaceVariant = VaultSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SubtleBorder
)

private val VaultTypography = Typography(
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.4).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp
    )
)

@Composable
fun TcgVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VaultColorScheme,
        typography = VaultTypography,
        content = content
    )
}
