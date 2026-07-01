package com.you.plot.core.ui.maps

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.ElevationPoint
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
@Composable
fun ElevationProfile(
    points: List<ElevationPoint>,
    modifier: Modifier = Modifier,
    useMetric: Boolean = true,
) {
    if (points.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val axisColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier.background(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            RoundedCornerShape(10.dp)
        ).padding(10.dp)
    ) {
        val W = size.width
        val H = size.height

        val titleSpace = 34f
        val leftMargin = titleSpace + 84f
        val bottomMargin = 64f
        val topMargin = 16f
        val rightMargin = 16f

        val plotW = W - leftMargin - rightMargin
        val plotH = H - bottomMargin - topMargin

        val elevFactor = if (useMetric) 1.0 else 3.28084
        val distFactor = if (useMetric) 1.0 else 0.621371
        val elevUnit = if (useMetric) "M" else "FT"
        val distUnit = if (useMetric) "KM" else "MI"

        val elevations = points.map { it.elevation * elevFactor }
        val distances = points.map { it.dist * distFactor }

        val minElev = elevations.min()
        val maxElev = elevations.max()
        val maxDist = distances.max().coerceAtLeast(0.001)

        val yTicks = niceTicks(minElev, maxElev, targetCount = 7)
        val xTicks = niceTicks(0.0, maxDist, targetCount = 8)

        val yMin = yTicks.first()
        val yMax = yTicks.last()
        val yRange = (yMax - yMin).coerceAtLeast(1.0)

        fun elevToY(e: Double) = (topMargin + plotH * (1.0 - (e - yMin) / yRange)).toFloat()
        fun distToX(d: Double) = (leftMargin + plotW * (d / maxDist)).toFloat()

        val labelPaint = Paint().apply {
            isAntiAlias = true
            color = labelColor.toArgb()
            textSize = 28f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.RIGHT
        }
        val axisPaint = Paint().apply {
            isAntiAlias = true
            color = axisColor.toArgb()
            textSize = 28f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = labelColor.toArgb()
            textSize = 26f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }

        for (tick in yTicks) {
            val y = elevToY(tick)
            drawLine(
                gridColor,
                Offset(leftMargin, y),
                Offset(leftMargin + plotW, y),
                strokeWidth = 1.5f
            )
            val label = if (tick % 1.0 == 0.0) "${tick.toInt()}" else "%.1f".format(tick)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                leftMargin - 8f,
                y + labelPaint.textSize / 3f,
                labelPaint
            )
        }

        for (tick in xTicks) {
            val x = distToX(tick)
            drawLine(
                gridColor,
                Offset(x, topMargin),
                Offset(x, topMargin + plotH),
                strokeWidth = 1f
            )
            val label = if (tick % 1.0 == 0.0) "${tick.toInt()}" else "%.1f".format(tick)
            drawContext.canvas.nativeCanvas.drawText(label, x, topMargin + plotH + 28f, axisPaint)
        }

        drawLine(
            axisColor,
            Offset(leftMargin, topMargin),
            Offset(leftMargin, topMargin + plotH),
            strokeWidth = 2f
        )
        drawLine(
            axisColor,
            Offset(leftMargin, topMargin + plotH),
            Offset(leftMargin + plotW, topMargin + plotH),
            strokeWidth = 2f
        )

        drawContext.canvas.nativeCanvas.drawText(
            "DISTANCE ($distUnit)",
            leftMargin + plotW / 2f,
            H - 6f,
            titlePaint
        )

        drawContext.canvas.nativeCanvas.apply {
            save()
            val titleX = titleSpace / 2f + 6f
            val titleY = topMargin + plotH / 2f
            rotate(-90f, titleX, titleY)
            drawText("ELEVATION ($elevUnit)", titleX, titleY, titlePaint)
            restore()
        }

        val fillPath = Path().apply {
            val firstX = distToX(distances[0])
            val firstY = elevToY(elevations[0])
            moveTo(firstX, topMargin + plotH)
            lineTo(firstX, firstY)
            for (i in 1 until points.size) {
                lineTo(distToX(distances[i]), elevToY(elevations[i]))
            }
            lineTo(distToX(distances.last()), topMargin + plotH)
            close()
        }
        drawPath(fillPath, fillColor)

        val linePath = Path()
        points.forEachIndexed { i, _ ->
            val x = distToX(distances[i])
            val y = elevToY(elevations[i])
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(
            linePath,
            lineColor,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
    }
}

private fun niceTicks(dataMin: Double, dataMax: Double, targetCount: Int = 5): List<Double> {
    val range = (dataMax - dataMin).coerceAtLeast(1.0)
    val roughStep = range / (targetCount - 1)
    val magnitude = 10.0.pow(floor(log10(roughStep)))
    val normalised = roughStep / magnitude
    val niceStep = when {
        normalised < 1.5 -> 1.0
        normalised < 2.25 -> 2.0
        normalised < 3.75 -> 2.5
        normalised < 7.0 -> 5.0
        else -> 10.0
    } * magnitude
    val start = floor(dataMin / niceStep) * niceStep
    val end = ceil(dataMax / niceStep) * niceStep
    val ticks = mutableListOf<Double>()
    var t = start
    while (t <= end + niceStep * 0.01) {
        ticks += t
        t += niceStep
    }
    return ticks
}
