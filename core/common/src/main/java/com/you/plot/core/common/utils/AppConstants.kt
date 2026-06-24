package com.you.plot.core.common.utils

object AppConstants {
    const val APP_TITLE = "YouPlot"
    const val APP_TAGLINE = "Plot Your Next Adventure"
    const val APP_CREDITS = "© Futuristic Ke"
    const val SUPPORT_EMAIL = "futuristicken@gmail.com"
}

object PrefConstants {
    const val PREFERENCE_FILE = "youplot_pref"
    const val THEME_MODE = "theme_mode"
    const val INSTALL_DATE = "install_date"
    const val LAST_APP_OPEN_TIME = "lastAppOpenTime"
}

object NotifConstants {
    const val CHANNEL_TRACKING_ID = "youplot_tracking"
    const val CHANNEL_TRACKING_NAME = "Activity Tracking"
    const val NOTIF_TRACKING_ID = 1001
}

object Routes {
    const val ROUTE_LIST   = "routes"
    const val ROUTE_PLOTTER = "routes/plot"
    const val PLAN_LIST    = "plans"
    const val PLAN_CREATE  = "plans/create"
    const val PLAN_DETAIL  = "plans/{planId}"
    const val TRACKER      = "tracker/{planId}"

    fun planDetail(planId: Long) = "plans/$planId"
    fun tracker(planId: Long) = "tracker/$planId"
}
