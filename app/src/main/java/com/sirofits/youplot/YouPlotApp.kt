package com.sirofits.youplot

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class YouPlotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Configure OSMDroid user-agent (required to download map tiles)
        Configuration.getInstance().userAgentValue = packageName
    }
}
