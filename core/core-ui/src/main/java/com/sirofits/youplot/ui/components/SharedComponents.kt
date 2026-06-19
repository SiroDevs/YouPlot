package com.sirofits.youplot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sirofits.youplot.domain.entity.ElevationPoint
import com.sirofits.youplot.domain.entity.SportType

// ─── Sport Type Chip ─────────────────────────────────────────────────────────

@Composable
fun SportChip(sportType: SportType, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(sportType.displayName(), style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Text(sportType.emoji(), fontSize = 14.sp)
        },
    )
}

fun SportType.displayName() = when (this) {
    SportType.RUNNING -> "Running"
    SportType.CYCLING -> "Cycling"
    SportType.HIKING -> "Hiking"
    SportType.WALKING -> "Walking"
}

fun SportType.emoji() = when (this) {
    SportType.RUNNING -> "🏃"
    SportType.CYCLING -> "🚴"
    SportType.HIKING -> "🥾"
    SportType.WALKING -> "🚶"
}

// ─── Route Stat Row ──────────────────────────────────────────────────────────

@Composable
fun RouteStatRow(
    distanceKm: Double,
    elevationGainM: Double,
    elevationLossM: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatChip(
            label = "Distance",
            value = "%.1f km".format(distanceKm),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            label = "↑ Gain",
            value = "${elevationGainM.toInt()} m",
            modifier = Modifier.weight(1f),
            valueColor = MaterialTheme.colorScheme.primary,
        )
        StatChip(
            label = "↓ Loss",
            value = "${elevationLossM.toInt()} m",
            modifier = Modifier.weight(1f),
            valueColor = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Elevation Profile Graph ─────────────────────────────────────────────────

@Composable
fun ElevationProfileGraph(
    points: List<ElevationPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.tertiary,
    fillColor: Color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
) {
    if (points.isEmpty()) return

    val minElev = points.minOf { it.elevationMeters }
    val maxElev = points.maxOf { it.elevationMeters }
    val maxDist = points.maxOf { it.distanceKm }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(100.dp)
    ) {
        val w = size.width
        val h = size.height
        val elevRange = (maxElev - minElev).coerceAtLeast(1.0)

        fun xFor(dist: Double) = (dist / maxDist * w).toFloat()
        fun yFor(elev: Double) = (h - ((elev - minElev) / elevRange * h * 0.85f)).toFloat()

        val linePath = Path()
        val fillPath = Path()

        points.forEachIndexed { i, point ->
            val x = xFor(point.distanceKm)
            val y = yFor(point.elevationMeters)
            if (i == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(xFor(maxDist), h)
        fillPath.close()

        drawPath(fillPath, fillColor)
        drawPath(linePath, lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        // Waypoint markers along x axis
        points.filter { it.distanceKm > 0 && it.distanceKm < maxDist }.take(5).forEach { pt ->
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(xFor(pt.distanceKm), yFor(pt.elevationMeters)),
            )
        }
    }
}

// ─── Loading shimmer placeholder ─────────────────────────────────────────────

@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
    )
}

// ─── Empty state ─────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─── Common TopAppBar ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouPlotTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Text("←", fontSize = 20.sp)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}
