package com.example.imoprep.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The practice site is light-only, so the app is too. Colors are fixed (not wallpaper-based)
// so the look matches the website.
private val AppColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = BrandSoft,
    onPrimaryContainer = BrandDark,
    secondary = Violet,
    onSecondary = Color.White,
    tertiary = Accent,
    onTertiary = Color.White,
    background = PageBg,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    onSurfaceVariant = InkSoft,
    outlineVariant = LineGrey,
    error = Danger,
    onError = Color.White,
)

@Composable
fun ImoPrepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = Typography,
        content = content
    )
}
