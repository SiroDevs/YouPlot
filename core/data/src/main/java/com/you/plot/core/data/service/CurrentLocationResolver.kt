package com.you.plot.core.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.you.plot.core.common.entity.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Wraps the LocationManager one-shot location fix flow that PlotterViewModel and
 * StartPointFormViewModel both used to inline. Prefers a cached-but-fresh fix
 * (< 2 minutes old); otherwise requests a live update for up to 5 seconds
 * before giving up.
 */
@Singleton
class CurrentLocationResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @SuppressLint("MissingPermission")
    suspend fun resolve(): LatLng? = withContext(Dispatchers.IO) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = enabledProviders(lm)
        if (providers.isEmpty()) return@withContext null

        cachedFreshLocation(lm, providers)?.let { return@withContext it.toLatLng() }
        liveLocation(lm, providers)?.toLatLng()
            ?: providers
                .firstNotNullOfOrNull { lm.getLastKnownLocation(it) }
                ?.toLatLng()
    }

    private fun enabledProviders(lm: LocationManager): List<String> =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }

    @SuppressLint("MissingPermission")
    private fun cachedFreshLocation(lm: LocationManager, providers: List<String>): Location? {
        val twoMinutesMs = 2 * 60 * 1000L
        return providers.firstNotNullOfOrNull { p ->
            lm.getLastKnownLocation(p)
                ?.takeIf { System.currentTimeMillis() - it.time < twoMinutesMs }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun liveLocation(
        lm: LocationManager,
        providers: List<String>,
    ): Location? = suspendCancellableCoroutine { cont ->
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                lm.removeUpdates(this); if (cont.isActive) cont.resume(loc)
            }

            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}
        }
        try {
            lm.requestLocationUpdates(
                providers.first(), 0L, 0f, listener, android.os.Looper.getMainLooper(),
            )
            cont.invokeOnCancellation { lm.removeUpdates(listener) }
            @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
            GlobalScope.launch {
                delay(5_000)
                lm.removeUpdates(listener)
                if (cont.isActive) cont.resume(null)
            }
        } catch (_: SecurityException) {
            cont.resume(null)
        }
    }

    private fun Location.toLatLng() = LatLng(latitude, longitude)
}
