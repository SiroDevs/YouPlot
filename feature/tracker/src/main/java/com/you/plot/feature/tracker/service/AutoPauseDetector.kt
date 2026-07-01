package com.you.plot.feature.tracker.service

/**
 * Tiny state machine that counts consecutive location ticks where the user's speed
 * is below a walking threshold. Once the run of stationary ticks passes
 * [dwellTicks], [shouldPause] returns true so the caller can pause the activity.
 */
class AutoPauseDetector(
    private val pauseSpeedKmh: Double = 1.0,
    private val dwellTicks: Int = 3,
) {
    private var stationaryTicks = 0

    /** Feeds the next observed speed. Returns true when auto-pause should trigger. */
    fun shouldPause(speedKmh: Double): Boolean {
        if (speedKmh < pauseSpeedKmh) {
            stationaryTicks++
            if (stationaryTicks >= dwellTicks) {
                stationaryTicks = 0
                return true
            }
        } else {
            stationaryTicks = 0
        }
        return false
    }

    fun reset() {
        stationaryTicks = 0
    }
}
