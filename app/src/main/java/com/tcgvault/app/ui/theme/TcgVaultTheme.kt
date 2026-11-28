package com.tcgvault.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * App palette. The screens read colours through the properties below, which resolve
 * against the palette provided by [TcgVaultTheme], so switching light/dark recolours
 * everything without touching individual screens.
 */
@Immutable
data class VaultPalette(
    val isLight: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val gold: Color,
    val accentInk: Color,
    val emerald: Color,
    val yugiohAccent: Color,
    val mtgAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val subtleBorder: Color
)

// True OLED Graphite Minimal palette — neutral zinc/obsidian, zero blue/purple tint
val DarkVaultPalette = VaultPalette(
    isLight = false,
    background = Color(0xFF09090B),
    surface = Color(0xFF111114),
    surfaceElevated = Color(0xFF1A1A1F),
    gold = Color(0xFFF59E0B),         // Used strictly for favorite stars & highlights
    accentInk = Color(0xFFFAFAFA),    // Primary high-contrast ink & primary CTA
    emerald = Color(0xFF22C55E),      // Collected state & completion
    yugiohAccent = Color(0xFFEAB308), // Subtle Millennium gold for YGO
    mtgAccent = Color(0xFFF97316),    // Subtle Planeswalker ember for MTG
    textPrimary = Color(0xFFFAFAFA),
    textSecondary = Color(0xFFA1A1AA),
    textMuted = Color(0xFF71717A),
    subtleBorder = Color(0xFF26262B)
)

// Light counterpart: same zinc family, accents deepened for contrast on white
val LightVaultPalette = VaultPalette(
    isLight = true,
    background = Color(0xFFF4F4F5),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF4F4F5),
    gold = Color(0xFFD97706),
    accentInk = Color(0xFF18181B),
    emerald = Color(0xFF16A34A),
    yugiohAccent = Color(0xFFCA8A04),
    mtgAccent = Color(0xFFEA580C),
    textPrimary = Color(0xFF18181B),
    textSecondary = Color(0xFF52525B),
    textMuted = Color(0xFF71717A),
    subtleBorder = Color(0xFFE4E4E7)
)

val LocalVaultPalette = staticCompositionLocalOf { DarkVaultPalette }

val VaultBackground: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.background
val VaultSurface: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.surface
val VaultSurfaceElevated: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.surfaceElevated
val VaultGold: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.gold
/** Primary high-contrast ink: near-white in dark mode, near-black in light mode. */
val VaultAccentWhite: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.accentInk
val VaultEmerald: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.emerald
val YugiohAccent: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.yugiohAccent
val MtgAccent: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.mtgAccent
val TextPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.textPrimary
val TextSecondary: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.textSecondary
val TextMuted: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.textMuted
val SubtleBorder: Color
    @Composable @ReadOnlyComposable get() = LocalVaultPalette.current.subtleBorder

val MonoFont = FontFamily.Monospace

private fun colorSchemeFor(p: VaultPalette) = if (p.isLight) {
    lightColorScheme(
        primary = p.accentInk,
        onPrimary = p.surface,
        secondary = p.textSecondary,
        onSecondary = p.surface,
        tertiary = p.emerald,
        onTertiary = p.surface,
        background = p.background,
        onBackground = p.textPrimary,
        surface = p.surface,
        onSurface = p.textPrimary,
        surfaceVariant = p.surfaceElevated,
        onSurfaceVariant = p.textSecondary,
        outline = p.subtleBorder
    )
} else {
    darkColorScheme(
        primary = p.accentInk,
        onPrimary = p.background,
        secondary = p.textSecondary,
        onSecondary = p.background,
        tertiary = p.emerald,
        onTertiary = p.background,
        background = p.background,
        onBackground = p.textPrimary,
        surface = p.surface,
        onSurface = p.textPrimary,
        surfaceVariant = p.surfaceElevated,
        onSurfaceVariant = p.textSecondary,
        outline = p.subtleBorder
    )
}

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
fun TcgVaultTheme(lightMode: Boolean = false, content: @Composable () -> Unit) {
    val palette = if (lightMode) LightVaultPalette else DarkVaultPalette
    CompositionLocalProvider(LocalVaultPalette provides palette) {
        MaterialTheme(
            colorScheme = colorSchemeFor(palette),
            typography = VaultTypography,
            content = content
        )
    }
}
