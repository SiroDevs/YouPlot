package com.you.plot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.you.plot.app.navigation.AppNavHost
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.designsystem.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import com.you.plot.core.data.repos.ThemeRepo
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val themeRepo: ThemeRepo = hiltViewModel()
            val themeMode = themeRepo.selectedTheme
            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                else -> true
            }

            AppTheme(useDarkTheme = isDarkTheme) {
                AppNavHost()
            }
        }
    }
}
