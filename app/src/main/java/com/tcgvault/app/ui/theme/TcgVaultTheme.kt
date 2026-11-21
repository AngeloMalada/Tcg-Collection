package com.tcgvault.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val VaultBackground = Color(0xFF0D0F18)
val VaultSurface = Color(0xFF161A2B)
val VaultSurfaceElevated = Color(0xFF20263D)
val VaultGold = Color(0xFFE8BC4B)
val VaultEmerald = Color(0xFF34D399)
val YugiohAccent = Color(0xFF9E7BFF)
val MtgAccent = Color(0xFFFF7A45)
val TextPrimary = Color(0xFFF3F5FC)
val TextSecondary = Color(0xFF9EA6C2)
val SubtleBorder = Color(0xFF2B3352)

private val VaultColorScheme = darkColorScheme(
    primary = VaultGold,
    onPrimary = Color(0xFF1A1404),
    secondary = YugiohAccent,
    onSecondary = Color(0xFF120A26),
    tertiary = MtgAccent,
    onTertiary = Color(0xFF260D04),
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
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
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
