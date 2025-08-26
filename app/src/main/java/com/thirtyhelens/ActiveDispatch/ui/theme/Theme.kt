package com.thirtyhelens.ActiveDispatch.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


// Light scheme built from your palette
private val LightColors = lightColorScheme(
    primary = AppColors.ButtonBlue,
    onPrimary = Color.White,
    primaryContainer = AppColors.GradientTop,
    onPrimaryContainer = Color.White,

    secondary = AppColors.AccentLightPurple,
    onSecondary = Color.White,
    secondaryContainer = AppColors.GradientBottom,
    onSecondaryContainer = Color.White,

    tertiary = AppColors.AccentGreen,
    onTertiary = Color.Black,

    background = AppColors.BackgroundBlue,
    onBackground = Color(0xFFECEFFF),

    surface = AppColors.BackgroundBlue,
    onSurface = Color(0xFFECEFFF),

    surfaceVariant = AppColors.GradientBottom,
    onSurfaceVariant = AppColors.DetailText,

    error = AppColors.AccentRed,
    onError = Color.White,

    outline = AppColors.DetailText,
)

// Dark scheme — same base with slightly brighter foregrounds
private val DarkColors = darkColorScheme(
    primary = AppColors.ButtonBlue,
    onPrimary = Color.White,
    primaryContainer = AppColors.GradientTop,
    onPrimaryContainer = Color.White,

    secondary = AppColors.AccentLightPurple,
    onSecondary = Color.White,
    secondaryContainer = AppColors.GradientBottom,
    onSecondaryContainer = Color.White,

    tertiary = AppColors.AccentGreen,
    onTertiary = Color.Black,

    background = AppColors.BackgroundBlue,
    onBackground = Color(0xFFEFF2FF),

    surface = AppColors.BackgroundBlue,
    onSurface = Color(0xFFEFF2FF),

    surfaceVariant = AppColors.GradientBottom,
    onSurfaceVariant = AppColors.DetailText,

    error = AppColors.AccentRed,
    onError = Color.White,

    outline = AppColors.DetailText,
)

@Composable
fun ActiveDispatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to keep exact brand colors consistent.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}