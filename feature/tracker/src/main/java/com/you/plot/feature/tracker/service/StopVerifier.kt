package com.you.plot.feature.tracker.service

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.domain.entity.WaypointProgress
import com.you.plot.core.domain.usecase.tracker.distanceTo

/**
 * Once the user acknowledges a "you've arrived at a planned stop" reminder, we watch
 * their subsequent movement. If they've moved a short distance the stop is presumed
 * genuine; if they've barrelled past it altogether the waypoint gets marked as skipped.
 */
class StopVerifier(
    private val verifyKm: Double = 1.0,
    private val skippedKm: Double = 5.0,
) {
    private var activeWaypoint: WaypointProgress? = null
    private var lastLocation: LatLng? = null
    private var distanceSinceStop = 0.0

    /** Begin watching movement from [startLocation] after the user acknowledged [waypoint]. */
    fun begin(waypoint: WaypointProgress, startLocation: LatLng?) {
        activeWaypoint = waypoint
        lastLocation = startLocation
        distanceSinceStop = 0.0
    }

    /** Cancel verification (user ignored the reminder). */
    fun cancel() {
        activeWaypoint = null
        lastLocation = null
        distanceSinceStop = 0.0
    }

    /** Result classifier for [advance]. */
    enum class Outcome { ONGOING, CONFIRMED, SKIPPED }

    /** Returns the effect the caller should apply based on the newest location. */
    fun advance(location: LatLng): Advance {
        val waypoint = activeWaypoint ?: return Advance(Outcome.ONGOING, null)
        distanceSinceStop += (lastLocation?.distanceTo(location) ?: 0.0)
        lastLocation = location
        return when {
            distanceSinceStop >= skippedKm -> {
                val target = waypoint
                cancel()
                Advance(Outcome.SKIPPED, target)
            }
            distanceSinceStop >= verifyKm -> {
                val target = waypoint
                cancel()
                Advance(Outcome.CONFIRMED, target)
            }
            else -> Advance(Outcome.ONGOING, null)
        }
    }

    data class Advance(val outcome: Outcome, val waypoint: WaypointProgress?)
}
