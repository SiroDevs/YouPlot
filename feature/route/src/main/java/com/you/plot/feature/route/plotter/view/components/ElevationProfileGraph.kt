package com.you.plot.feature.route.plotter.view.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.ElevationPoint
import kotlin.collections.forEach

@Composable
fun ElevationProfileGraph(profile: List<ElevationPoint>, modifier: Modifier = Modifier) {
    if (profile.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val minElev = profile.minOf { it.elevationMeters }
        val maxElev = profile.maxOf { it.elevationMeters }
        val elevRange = (maxElev - minElev).coerceAtLeast(1.0)
        val maxDist = profile.maxOf { it.distanceKm }.coerceAtLeast(0.001)

        val w = size.width
        val h = size.height

        val gridCount = 3
        for (i in 0..gridCount) {
            val y = h * i / gridCount
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        val fillPath = Path().apply {
            moveTo(0f, h)
            profile.forEach { pt ->
                val x = (pt.distanceKm / maxDist * w).toFloat()
                val y = (h - (pt.elevationMeters - minElev) / elevRange * h).toFloat()
                lineTo(x, y)
            }
            lineTo(w, h)
            close()
        }
        drawPath(fillPath, fillColor)

        val linePath = Path()
        profile.forEachIndexed { i, pt ->
            val x = (pt.distanceKm / maxDist * w).toFloat()
            val y = (h - (pt.elevationMeters - minElev) / elevRange * h).toFloat()
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(linePath, lineColor, style = Stroke(width = 3f))

        val paint = Paint().apply {
            color = labelColor.toArgb()
            textSize = 28f
        }
        drawContext.canvas.nativeCanvas.apply {
            drawText("${"%.0f".format(maxElev)}m", 4f, 28f, paint)
            drawText("${"%.0f".format(minElev)}m", 4f, h - 4f, paint)
            drawText("${"%.1f".format(maxDist)} km", w - 80f, h - 4f, paint)
        }
    }
}