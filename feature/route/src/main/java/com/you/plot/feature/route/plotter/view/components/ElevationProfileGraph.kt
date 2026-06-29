package com.you.plot.feature.route.plotter.view.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.ElevationPoint
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * A real elevation graph with:
 *  - Y-axis: elevation in metres (or feet), with nicely-rounded tick intervals
 *  - X-axis: distance in km (or miles), with nicely-rounded tick intervals
 *  - Labelled ticks on both axes
 *  - Filled area under the profile curve
 */
@Composable
fun ElevationProfileGraph(
    profile: List<ElevationPoint>,
    modifier: Modifier = Modifier,
    useMetric: Boolean = true,   // false → feet / miles
) {
    if (profile.isEmpty()) return

    val lineColor   = MaterialTheme.colorScheme.primary
    val fillColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val gridColor   = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val axisColor   = MaterialTheme.colorScheme.outline
    val labelColor  = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
    ) {
        val W = size.width
        val H = size.height

        // ── Margins that leave room for axis labels ──────────────────────────
        val leftMargin   = 108f   // wide enough for "1234 m" labels
        val bottomMargin = 52f
        val topMargin    = 16f
        val rightMargin  = 16f

        val plotW = W - leftMargin - rightMargin
        val plotH = H - bottomMargin - topMargin

        // ── Convert units ───────────────────────────────────────────────────
        val elevFactor  = if (useMetric) 1.0 else 3.28084   // metres → feet
        val distFactor  = if (useMetric) 1.0 else 0.621371  // km → miles
        val elevUnit    = if (useMetric) "m"  else "ft"
        val distUnit    = if (useMetric) "km" else "mi"

        val elevations = profile.map { it.elevationMeters * elevFactor }
        val distances  = profile.map { it.distanceKm   * distFactor  }

        val minElev = elevations.min()
        val maxElev = elevations.max()
        val maxDist = distances.max().coerceAtLeast(0.001)

        // ── Nice tick calculation ────────────────────────────────────────────
        val yTicks = niceTicks(minElev, maxElev, targetCount = 4)
        val xTicks = niceTicks(0.0, maxDist, targetCount = 5)

        val yMin = yTicks.first()
        val yMax = yTicks.last()
        val yRange = (yMax - yMin).coerceAtLeast(1.0)

        fun elevToY(e: Double) = (topMargin + plotH * (1.0 - (e - yMin) / yRange)).toFloat()
        fun distToX(d: Double) = (leftMargin + plotW * (d / maxDist)).toFloat()

        // ── Grid lines + Y-axis tick labels ─────────────────────────────────
        val labelPaint = Paint().apply {
            isAntiAlias = true
            color       = labelColor.toArgb()
            textSize    = 28f
            typeface    = Typeface.MONOSPACE
            textAlign   = Paint.Align.RIGHT
        }
        val axisPaint = Paint().apply {
            isAntiAlias = true
            color       = axisColor.toArgb()
            textSize    = 28f
            typeface    = Typeface.MONOSPACE
            textAlign   = Paint.Align.CENTER
        }

        for (tick in yTicks) {
            val y = elevToY(tick)
            // grid line
            drawLine(gridColor, Offset(leftMargin, y), Offset(leftMargin + plotW, y), strokeWidth = 1.5f)
            // label — right-aligned, centred on tick line
            val label = if (tick % 1.0 == 0.0) "${tick.toInt()} $elevUnit" else "%.1f $elevUnit".format(tick)
            drawContext.canvas.nativeCanvas.drawText(label, leftMargin - 8f, y + labelPaint.textSize / 3f, labelPaint)
        }

        // ── X-axis tick labels ───────────────────────────────────────────────
        for (tick in xTicks) {
            val x = distToX(tick)
            // vertical grid line (light)
            drawLine(gridColor, Offset(x, topMargin), Offset(x, topMargin + plotH), strokeWidth = 1f)
            val label = if (tick % 1.0 == 0.0) "${tick.toInt()} $distUnit" else "%.1f $distUnit".format(tick)
            drawContext.canvas.nativeCanvas.drawText(label, x, H - 6f, axisPaint)
        }

        // ── Axis lines ───────────────────────────────────────────────────────
        drawLine(axisColor, Offset(leftMargin, topMargin), Offset(leftMargin, topMargin + plotH), strokeWidth = 2f)
        drawLine(axisColor, Offset(leftMargin, topMargin + plotH), Offset(leftMargin + plotW, topMargin + plotH), strokeWidth = 2f)

        // ── Filled area under the profile ────────────────────────────────────
        val fillPath = Path().apply {
            val firstX = distToX(distances[0])
            val firstY = elevToY(elevations[0])
            moveTo(firstX, topMargin + plotH)
            lineTo(firstX, firstY)
            for (i in 1 until profile.size) {
                lineTo(distToX(distances[i]), elevToY(elevations[i]))
            }
            lineTo(distToX(distances.last()), topMargin + plotH)
            close()
        }
        drawPath(fillPath, fillColor)

        // ── Profile line ────────────────────────────────────────────────────
        val linePath = Path()
        profile.forEachIndexed { i, _ ->
            val x = distToX(distances[i])
            val y = elevToY(elevations[i])
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(linePath, lineColor, style = Stroke(width = 3.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    }
}

/** Returns a list of "nice" tick values covering [dataMin, dataMax]. */
private fun niceTicks(dataMin: Double, dataMax: Double, targetCount: Int = 5): List<Double> {
    val range = (dataMax - dataMin).coerceAtLeast(1.0)
    val roughStep = range / (targetCount - 1)
    val magnitude = 10.0.pow(floor(log10(roughStep)))
    val normalised = roughStep / magnitude
    val niceStep = when {
        normalised < 1.5 -> 1.0
        normalised < 3.0 -> 2.0
        normalised < 7.0 -> 5.0
        else             -> 10.0
    } * magnitude
    val start = floor(dataMin / niceStep) * niceStep
    val end   = ceil (dataMax / niceStep) * niceStep
    val ticks = mutableListOf<Double>()
    var t = start
    while (t <= end + niceStep * 0.01) {
        ticks += t
        t += niceStep
    }
    return ticks
}
