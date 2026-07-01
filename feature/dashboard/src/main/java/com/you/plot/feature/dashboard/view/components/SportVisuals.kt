package com.you.plot.feature.dashboard.view.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SportType

/**
 * Maps a [SportType] to an icon. Exhaustive over the real enum (RUNNING,
 * CYCLING, HIKING, WALKING) so adding a new sport is a compile error here
 * until it's given an icon, rather than silently falling back.
 */
fun sportTypeIcon(sportType: SportType): ImageVector = when (sportType) {
    SportType.RUNNING -> Icons.Default.DirectionsRun
    SportType.CYCLING -> Icons.Default.DirectionsBike
    SportType.HIKING -> Icons.Default.Terrain
    SportType.WALKING -> Icons.Default.DirectionsWalk
}

fun sportTypeLabel(sportType: SportType): String =
    sportType.name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
fun SportTypeChip(sportType: SportType, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Icon(
                sportTypeIcon(sportType),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                sportTypeLabel(sportType),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}
