package com.sirofits.youplot.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Palette ─────────────────────────────────────────────────────────────────
// Trail-inspired: deep forest greens, trail-dust orange accent, slate backgrounds

object YouPlotColors {
    val TrailGreen = Color(0xFF1B5E20)       // Deep forest green — primary
    val TrailGreenLight = Color(0xFF4CAF50)  // Mid green — containers
    val SunriseOrange = Color(0xFFF57C00)    // Warm orange — accent / waypoint markers
    val ElevationBlue = Color(0xFF1565C0)    // Cool blue — elevation graph
    val SlateNight = Color(0xFF1A1C1E)       // Near-black background (dark)
    val SlateCard = Color(0xFF2C2F33)        // Card surface (dark)
    val DirtTrail = Color(0xFFF5F0E8)        // Warm off-white (light bg)
    val ErrorRed = Color(0xFFB71C1C)
}

private val DarkColorScheme = darkColorScheme(
    primary = YouPlotColors.TrailGreenLight,
    onPrimary = Color.White,
    primaryContainer = YouPlotColors.TrailGreen,
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = YouPlotColors.SunriseOrange,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF4E3800),
    onSecondaryContainer = Color(0xFFFFDDB3),
    tertiary = YouPlotColors.ElevationBlue,
    background = YouPlotColors.SlateNight,
    surface = YouPlotColors.SlateCard,
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6),
    error = YouPlotColors.ErrorRed,
)

private val LightColorScheme = lightColorScheme(
    primary = YouPlotColors.TrailGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    onPrimaryContainer = YouPlotColors.TrailGreen,
    secondary = YouPlotColors.SunriseOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB3),
    onSecondaryContainer = Color(0xFF2C1600),
    tertiary = YouPlotColors.ElevationBlue,
    background = YouPlotColors.DirtTrail,
    surface = Color.White,
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
    error = YouPlotColors.ErrorRed,
)

@Composable
fun YouPlotTheme(
    darkTheme: Boolean = true, // default dark — trail/outdoor aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YouPlotTypography,
        content = content,
    )
}
