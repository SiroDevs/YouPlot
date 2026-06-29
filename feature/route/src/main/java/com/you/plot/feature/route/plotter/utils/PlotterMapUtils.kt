package com.you.plot.feature.route.plotter.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import com.you.plot.core.common.entity.DestinationMode
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.WaypointSearchResult

private const val COLOR_MARKER_TEXT = Color.WHITE

private const val PIN_W = 72;
private const val PIN_H = 90
private const val CIRCLE_R = 28f

data class PlotterUiState(
    val stage: PlotterStage = PlotterStage.STAGE_1,
    val searchQuery: String = "",
    val searchResults: List<WaypointSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val isReverseGeocoding: Boolean = false,
    val selectedCountryCode: String = "ke",
    val startPoint: LatLng? = null,
    val startPointName: String = "",
    val destinationMode: DestinationMode = DestinationMode.PICK_POINT,
    val endPoint: LatLng? = null,
    val endPointName: String = "",
    val targetDistanceKm: Double = 10.0,
    val targetDistanceQuery: String = "10",
    val distanceSuggestions: List<WaypointSearchResult> = emptyList(),
    val manualWaypoints: List<LatLng> = emptyList(),
    val suggestedWaypoints: List<LatLng> = emptyList(),
    val useSuggestedWaypoints: Boolean = false,
    val routeCandidates: List<RouteCandidate> = emptyList(),
    val selectedCandidateId: Int? = null,
    val isRoundTrip: Boolean = false,
    val name: String = "",
    val description: String = "",
    val sportType: SportType = SportType.RUNNING,
    val isSaving: Boolean = false,
    val savedRouteId: Long? = null,
    val needsLocationPermission: Boolean = false,
    val error: String? = null,
) {
    val activeWaypoints: List<LatLng>
        get() = if (useSuggestedWaypoints) suggestedWaypoints else manualWaypoints

    val selectedCandidate: RouteCandidate?
        get() = routeCandidates.firstOrNull { it.id == selectedCandidateId }
            ?: routeCandidates.firstOrNull()
}

fun makePinMarker(context: Context, color: Int, label: String): Bitmap {
    val bmp = createBitmap(PIN_W, PIN_H)
    val canvas = Canvas(bmp)
    val fillPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = COLOR_MARKER_TEXT; textSize = 26f; typeface =
        Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    val cx = PIN_W / 2f;
    val cy = CIRCLE_R + 4f
    canvas.drawCircle(cx, cy, CIRCLE_R, fillPaint)
    canvas.drawCircle(cx, cy, CIRCLE_R, rimPaint)
    val path = Path().apply {
        moveTo(cx - CIRCLE_R * 0.6f, cy + CIRCLE_R * 0.6f)
        lineTo(cx, PIN_H.toFloat() - 4f)
        lineTo(cx + CIRCLE_R * 0.6f, cy + CIRCLE_R * 0.6f)
        close()
    }
    canvas.drawPath(path, fillPaint)
    canvas.drawPath(path, rimPaint)
    val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(label, cx, textY, textPaint)
    return bmp
}

fun makeCircleMarker(context: Context, color: Int, label: String): Bitmap {
    val size = (CIRCLE_R * 2 + 8).toInt()
    val bmp = createBitmap(size, size)
    val canvas = Canvas(bmp)
    val cx = size / 2f;
    val cy = size / 2f;
    val r = CIRCLE_R
    val fillPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = COLOR_MARKER_TEXT; textSize = 22f; typeface =
        Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    canvas.drawCircle(cx, cy, r, fillPaint)
    canvas.drawCircle(cx, cy, r, rimPaint)
    val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(label, cx, textY, textPaint)
    return bmp
}
