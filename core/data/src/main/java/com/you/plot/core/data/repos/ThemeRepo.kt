package com.you.plot.core.data.repos

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.you.plot.core.common.utils.PrefConstants
import com.you.plot.core.designsystem.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ThemeRepo @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val prefs = context.getSharedPreferences(PrefConstants.PREFERENCE_FILE, Context.MODE_PRIVATE)

    var selectedTheme: ThemeMode by mutableStateOf(
        ThemeMode.valueOf(prefs.getString(PrefConstants.THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
        private set

    fun setTheme(mode: ThemeMode) {
        selectedTheme = mode
        prefs.edit().putString(PrefConstants.THEME_MODE, mode.name).apply()
    }
}
