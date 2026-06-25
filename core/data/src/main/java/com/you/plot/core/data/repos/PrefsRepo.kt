package com.you.plot.core.data.repos

import android.content.Context
import androidx.core.content.edit
import com.you.plot.core.common.utils.PrefConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsRepo @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs =
        context.getSharedPreferences(PrefConstants.PREFERENCE_FILE, Context.MODE_PRIVATE)

    var appThemeMode: ThemeMode
        get() = ThemeMode.valueOf(
            prefs.getString(PrefConstants.THEME_MODE, ThemeMode.SYSTEM.name)
                ?: ThemeMode.SYSTEM.name
        )
        set(value) = prefs.edit { putString(PrefConstants.THEME_MODE, value.name) }

    var installDate: Long
        get() = prefs.getLong(PrefConstants.INSTALL_DATE, 0L)
        set(value) = prefs.edit { putLong(PrefConstants.INSTALL_DATE, value) }

    var lastAppOpenTime: Long
        get() = prefs.getLong(PrefConstants.LAST_APP_OPEN_TIME, 0L)
        set(value) = prefs.edit { putLong(PrefConstants.LAST_APP_OPEN_TIME, value) }

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(PrefConstants.NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit { putBoolean(PrefConstants.NOTIFICATIONS_ENABLED, value) }

    var distanceUnitMetric: Boolean
        get() = prefs.getBoolean(PrefConstants.DISTANCE_UNIT_METRIC, true)
        set(value) = prefs.edit { putBoolean(PrefConstants.DISTANCE_UNIT_METRIC, value) }

    var defaultSport: String
        get() = prefs.getString(PrefConstants.DEFAULT_SPORT, "RUNNING") ?: "RUNNING"
        set(value) = prefs.edit { putString(PrefConstants.DEFAULT_SPORT, value) }
}
