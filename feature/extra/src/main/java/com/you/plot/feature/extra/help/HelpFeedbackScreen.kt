package com.you.plot.feature.extra.help

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.AppConstants
import androidx.core.net.toUri
import com.you.plot.core.designsystem.theme.AppTheme
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.core.ui.components.general.InfoDivider
import com.you.plot.core.ui.components.general.InfoNavItem
import com.you.plot.core.ui.components.general.InfoSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpFeedbackScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    fun sendEmail(subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:${AppConstants.SUPPORT_EMAIL}".toUri()
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(Intent.createChooser(intent, "Send email"))
    }

    HelpFeedbackContent(
        onBack = onBack,
        onSendEmail = ::sendEmail,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpFeedbackContent(
    onBack: () -> Unit,
    onSendEmail: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Help & Feedback",
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
        ) {
            InfoSection("Help") {
                InfoNavItem(
                    icon     = Icons.AutoMirrored.Filled.Help,
                    title    = "How to Plot a Route",
                    subtitle = "Step-by-step guide to creating your first route",
                    onClick  = {
                        onSendEmail("YouPlot — How to Plot a Route")
                    },
                )
                InfoDivider()
                InfoNavItem(
                    icon     = Icons.AutoMirrored.Filled.Help,
                    title    = "How to Create a Plan",
                    subtitle = "Learn how to plan multi-day activities",
                    onClick  = {
                        onSendEmail("YouPlot — How to Create a Plan")
                    },
                )
                InfoDivider()
                InfoNavItem(
                    icon     = Icons.AutoMirrored.Filled.Help,
                    title    = "Activity Tracking Guide",
                    subtitle = "Understand how live tracking works",
                    onClick  = {
                        onSendEmail("YouPlot — Activity Tracking Guide")
                    },
                )
            }

            InfoSection("Feedback") {
                InfoNavItem(
                    icon     = Icons.Default.BugReport,
                    title    = "Report a Bug",
                    subtitle = "Tell us about something that isn't working",
                    onClick  = { onSendEmail("YouPlot Bug Report") },
                )
                InfoDivider()
                InfoNavItem(
                    icon     = Icons.Default.RateReview,
                    title    = "Send Feedback",
                    subtitle = "Share ideas or suggestions",
                    onClick  = { onSendEmail("YouPlot Feedback") },
                )
                InfoDivider()
                InfoNavItem(
                    icon     = Icons.Default.Email,
                    title    = "Contact Support",
                    subtitle = AppConstants.SUPPORT_EMAIL,
                    onClick  = { onSendEmail("YouPlot Support") },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HelpFeedbackScreenPreview() {
    AppTheme {
        HelpFeedbackContent(
            onBack = {},
            onSendEmail = {},
        )
    }
}
