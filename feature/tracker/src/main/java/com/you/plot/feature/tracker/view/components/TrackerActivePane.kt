package com.you.plot.feature.tracker.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.ActivityActivity

/**
 * Live tracking pane: map on top, stats grid in the middle, controls at the
 * bottom. All the branching lives in the small children — this composable only
 * wires them together.
 */
@Composable
fun TrackerActivePane(
    activity: ActivityActivity,
    nextEta: Long?,
    doneEta: Long?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        TrackerMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp)),
            currentLocation = activity.currentLocation,
            waypoints = activity.waypointProgress,
        )

        TrackerStatsGrid(
            activity = activity,
            nextEta = nextEta,
            doneEta = doneEta,
            modifier = Modifier.fillMaxWidth(),
        )

        TrackerControls(
            status = activity.status,
            onPause = onPause,
            onResume = onResume,
            onFinish = onFinish,
        )

        Spacer(Modifier.height(170.dp))
    }
}
