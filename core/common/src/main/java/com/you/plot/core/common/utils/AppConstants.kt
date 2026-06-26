package com.you.plot.core.common.utils

import org.osmdroid.util.GeoPoint

object AppConstants {
    const val APP_TITLE = "YouPlot"
    const val APP_TAGLINE = "Plot Your Next Adventure"
    const val APP_CREDITS = "© Siro Fits Ke"
    const val SUPPORT_EMAIL = "sirofits@gmail.com"
    const val APP_VERSION = "1.0.2"
}

object MapConstants{
    val KENYA_CENTER = GeoPoint(-0.0236, 37.9062)
    const val COUNTRY_ZOOM = 7.5
    const val CITY_ZOOM = 13.0
    const val WAYPOINT_ZOOM = 12.0
    const val COLOR_START = 0xFF43A047.toInt()
    const val COLOR_END = 0xFFE53935.toInt()
    const val COLOR_WAYPOINT = 0xFF1E88E5.toInt()
    const val COLOR_TURN = 0xFFFF8F00.toInt()

    val CANDIDATE_COLORS = listOf(0xFF2196F3L, 0xFFE91E63L, 0xFF4CAF50L, 0xFFFF9800L)

    const val OSRM_BASE = "https://nominatim.openstreetmap.org/"
    const val OSRM_ROUTER = "https://router.project-osrm.org/route/v1/driving"
}

object PrefConstants {
    const val PREFERENCE_FILE = "youplot_pref"
    const val THEME_MODE = "theme_mode"
    const val INSTALL_DATE = "install_date"
    const val LAST_APP_OPEN_TIME = "lastAppOpenTime"
    const val NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val DISTANCE_UNIT_METRIC = "distance_unit_metric"
    const val DEFAULT_SPORT = "default_sport"
}

object NotifConstants {
    const val CHANNEL_TRACKING_ID = "youplot_tracking"
    const val CHANNEL_TRACKING_NAME = "Activity Tracking"
    const val NOTIF_TRACKING_ID = 1001
}

object Routes {
    const val DASHBOARD = "dashboard"
    const val ROUTE_LIST = "routes"
    const val ROUTE_PLOTTER = "routes/plot"
    const val PLAN_LIST = "plans"
    const val PLAN_CREATE = "plans/create"
    const val PLAN_DETAIL = "plans/{planId}"
    const val TRACKER = "tracker/{planId}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val HELP_FEEDBACK = "help_feedback"

    fun planDetail(planId: Long) = "plans/$planId"
    fun tracker(planId: Long) = "tracker/$planId"
}
