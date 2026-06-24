package com.you.plot.core.designsystem.theme

import androidx.compose.ui.graphics.Color

object LightColors {
    val primary = Color(0xFF7C4A2D)           // Rich brown - brand
    val onPrimary = Color(0xFFFFFFFF)         // White text on primary
    val primaryContainer = Color(0xFFFFFBF7)  // cardLight - warm white cards
    val onPrimaryContainer = Color(0xFF5C3320) // Deep brown text on container

    val secondary = Color(0xFFA0633E)          // Lighter brown - accent
    val onSecondary = Color(0xFFFFFFFF)
    val secondaryContainer = Color(0xFFE0D5CA) // borderLight as muted container
    val onSecondaryContainer = Color(0xFF5C3320)

    val tertiary = Color(0xFF5C3320)           // Deep brown - 3rd accent
    val onTertiary = Color(0xFFFFFFFF)
    val tertiaryContainer = Color(0xFFF5F0EB)  // bgLight as tertiary container
    val onTertiaryContainer = Color(0xFF3B1F11)

    val error = Color(0xFFBA1A1A)
    val errorContainer = Color(0xFFFFDAD6)
    val onError = Color(0xFFFFFFFF)
    val onErrorContainer = Color(0xFF410002)

    val background = Color(0xFFF5F0EB)        // bgLight - warm off-white
    val onBackground = Color(0xFF1A1A1A)

    val surface = Color(0xFFFFFFFF)           // surfaceLight - pure white
    val onSurface = Color(0xFF1A1A1A)

    val surfaceVariant = Color(0xFFFFFBF7)    // cardLight - sticky headers / elevated surfaces
    val onSurfaceVariant = Color(0xFF5C3320)

    val outline = Color(0xFFA0633E)           // primaryLight as visible outline
    val outlineVariant = Color(0xFFE0D5CA)    // borderLight - subtle dividers

    val inverseOnSurface = Color(0xFFF5F0EB)
    val inverseSurface = Color(0xFF1A1A1A)
    val inversePrimary = Color(0xFFA0633E)

    val shadow = Color.Black
    val surfaceTint = Color(0xFF7C4A2D)
    val scrim = Color.Black
}

object DarkColors {
    val primary = Color(0xFFA0633E)           // primaryLight - softened for dark mode
    val onPrimary = Color(0xFF5C3320)         // primaryDark
    val primaryContainer = Color(0xFF5C3320)  // primaryDark as container
    val onPrimaryContainer = Color(0xFFF5F0EB)

    val secondary = Color(0xFF7C4A2D)         // primary brown
    val onSecondary = Color(0xFFFFFFFF)
    val secondaryContainer = Color(0xFF2E2E2E) // borderDark as container
    val onSecondaryContainer = Color(0xFFF5F0EB)

    val tertiary = Color(0xFFA0633E)           // primaryLight as tertiary
    val onTertiary = Color(0xFF1A1A1A)
    val tertiaryContainer = Color(0xFF222222)  // cardDark
    val onTertiaryContainer = Color(0xFFF5F0EB)

    val error = Color(0xFFFFB4AB)
    val errorContainer = Color(0xFF93000A)
    val onError = Color(0xFF690005)
    val onErrorContainer = Color(0xFFFFDAD6)

    val background = Color(0xFF111111)        // bgDark - near-black
    val onBackground = Color(0xFFF5F0EB)

    val surface = Color(0xFF1A1A1A)           // surfaceDark
    val onSurface = Color(0xFFF5F0EB)

    val surfaceVariant = Color(0xFF222222)    // cardDark - elevated surfaces
    val onSurfaceVariant = Color(0xFFE0D5CA)

    val outline = Color(0xFF7C4A2D)           // primary brown as outline
    val outlineVariant = Color(0xFF2E2E2E)    // borderDark - subtle dividers

    val inverseOnSurface = Color(0xFF1A1A1A)
    val inverseSurface = Color(0xFFF5F0EB)
    val inversePrimary = Color(0xFF7C4A2D)

    val shadow = Color.Black
    val surfaceTint = Color(0xFFA0633E)
    val scrim = Color.Black
}