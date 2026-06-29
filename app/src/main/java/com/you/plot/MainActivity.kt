package com.you.plot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.you.plot.app.navigation.AppNavHost
import com.you.plot.core.designsystem.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import com.you.plot.core.data.repos.ThemeRepo
import androidx.hilt.navigation.compose.hiltViewModel
import com.you.plot.core.data.repos.PreferencesRepo
import com.you.plot.core.data.repos.ThemeMode
import javax.inject.Inject
import io.sentry.Sentry

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefsRepo: PreferencesRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
    // waiting for view to draw to better represent a captured error with a screenshot
    findViewById<android.view.View>(android.R.id.content).viewTreeObserver.addOnGlobalLayoutListener {
      try {
        throw Exception("This app uses Sentry! :)")
      } catch (e: Exception) {
        Sentry.captureException(e)
      }
    }


        setContent {
            val themeRepo: ThemeRepo = hiltViewModel()

            val themeMode = themeRepo.selectedTheme
            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            AppTheme(useDarkTheme = isDarkTheme) {
                AppNavHost(themeRepo = themeRepo)
            }
        }
    }
}
