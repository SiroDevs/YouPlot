package com.you.plot.feature.extra.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.AppConstants
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.core.ui.components.general.InfoDivider
import com.you.plot.core.ui.components.general.InfoItem
import com.you.plot.core.ui.components.general.InfoSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "About YouPlot",
                showGoBack = true,
                onNavIconClick = onBack
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    AppConstants.APP_TITLE,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    AppConstants.APP_TAGLINE,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Version ${AppConstants.APP_VERSION}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(32.dp))

            InfoSection("About") {
                InfoItem(
                    icon  = Icons.Default.Info,
                    title = "App Version",
                    value = AppConstants.APP_VERSION,
                )
                InfoDivider()
                InfoItem(
                    icon  = Icons.Default.Person,
                    title = "Developer",
                    value = "Futuristic Ke",
                )
                InfoDivider()
                InfoItem(
                    icon  = Icons.Default.Email,
                    title = "Contact",
                    value = AppConstants.SUPPORT_EMAIL,
                )
                InfoDivider()
                InfoItem(
                    icon  = Icons.Default.Code,
                    title = "Credits",
                    value = AppConstants.APP_CREDITS,
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "YouPlot helps you plot routes, plan outdoor activities,\nand track your progress in real time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
