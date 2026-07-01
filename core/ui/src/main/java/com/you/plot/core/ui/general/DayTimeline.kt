package com.you.plot.core.ui.general

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.timeFmt
import com.you.plot.core.domain.entity.Event
import java.util.Date

@Composable
fun DayTimeline(events: List<Event>, modifier: Modifier = Modifier) {
    if (events.isEmpty()) return

    val scrollState = rememberScrollState()
    val minTime = events.minOf { it.plannedTime }
    val maxTime = events.maxOf { it.plannedTime }.coerceAtLeast(minTime + 1)

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(modifier.horizontalScroll(scrollState)) {
        Row(
            Modifier
                .width(900.dp)
                .height(88.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            events.forEachIndexed { index, event ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(min = 64.dp, max = 96.dp),
                ) {
                    Text(
                        timeFmt.format(Date(event.plannedTime)),
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.65f),
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier.size(10.dp).clip(CircleShape).background(primaryColor),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        event.name.take(14),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (event.distCovered > 0) {
                        Text(
                            "${"%.1f".format(event.distCovered)} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurface.copy(alpha = 0.5f),
                        )
                    }
                }

                if (index < events.lastIndex) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(primaryColor.copy(alpha = 0.35f)),
                    )
                }
            }
        }
    }
}
