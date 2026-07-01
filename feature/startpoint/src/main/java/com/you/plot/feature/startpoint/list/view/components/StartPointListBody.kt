package com.you.plot.feature.startpoint.list.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.StartPoint

@Composable
fun StartPointListBody(
    isLoading: Boolean,
    startPoints: List<StartPoint>,
    menuTargetId: Long?,
    onOpenMenu: (Long) -> Unit,
    onDismissMenu: () -> Unit,
    onStartRoute: (StartPoint) -> Unit,
    onEdit: (Long) -> Unit,
    onToggleFavorite: (StartPoint) -> Unit,
    onDelete: (Long) -> Unit,
) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        startPoints.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No start points here yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        else -> LazyColumn(Modifier.fillMaxSize()) {
            item { Spacer(Modifier.height(8.dp)) }
            items(startPoints, key = { it.id }) { sp ->
                StartPointRow(
                    sp = sp,
                    menuOpen = menuTargetId == sp.id,
                    onOpenMenu = { onOpenMenu(sp.id) },
                    onDismissMenu = onDismissMenu,
                    onStartRoute = { onStartRoute(sp) },
                    onEdit = { onEdit(sp.id) },
                    onToggleFavorite = { onToggleFavorite(sp) },
                    onDelete = { onDelete(sp.id) },
                )
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}