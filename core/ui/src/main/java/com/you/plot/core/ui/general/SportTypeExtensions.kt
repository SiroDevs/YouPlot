package com.you.plot.core.ui.general

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.NordicWalking
import androidx.compose.ui.graphics.vector.ImageVector
import com.you.plot.core.common.entity.SportType

val SportType.displayLabel: String
    get() = when (this) {
        SportType.RUNNING -> "Running"
        SportType.CYCLING -> "Cycling"
        SportType.HIKING  -> "Hiking"
        SportType.WALKING -> "Walking"
    }

val SportType.icon: ImageVector
    get() = when (this) {
        SportType.RUNNING -> Icons.Outlined.DirectionsRun
        SportType.CYCLING -> Icons.Outlined.DirectionsBike
        SportType.HIKING  -> Icons.Outlined.NordicWalking
        SportType.WALKING -> Icons.Outlined.DirectionsWalk
    }
