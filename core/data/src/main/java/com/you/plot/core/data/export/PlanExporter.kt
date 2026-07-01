package com.you.plot.core.data.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import android.net.Uri
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.domain.entity.Route
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Emits a share-ready file for a plan. The plan's parent [Route] is passed so we
 * can include distance / elevation stats in the exported summary.
 */
@Singleton
class PlanExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun export(
        plan: ActivityPlan,
        route: Route?,
        format: PlanExportFormat,
    ): Uri {
        val safeName = plan.name.replace("[^A-Za-z0-9._-]".toRegex(), "_")
        val file = File(context.cacheDir, "exports").apply { mkdirs() }
            .resolve("$safeName.${format.extension}")
        when (format) {
            PlanExportFormat.TXT -> file.writeText(buildText(plan, route))
            PlanExportFormat.PDF -> writePlanPdf(plan, route, file)
            PlanExportFormat.IMAGE -> throw UnsupportedExportFormatException("Image")
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    private fun buildText(plan: ActivityPlan, route: Route?): String = buildString {
        val fmt = SimpleDateFormat("EEE dd MMM yyyy HH:mm", Locale.getDefault())
        appendLine("YouPlot – ${plan.name}")
        appendLine("=".repeat(40))
        appendLine("Sport:         ${plan.sportType.name}")
        appendLine("Start:         ${fmt.format(Date(plan.startDate))}")
        appendLine("Duration:      ${plan.numberOfDays} day${if (plan.numberOfDays == 1) "" else "s"}")
        appendLine("Avg speed:     %.1f km/h".format(plan.avgSpeed))
        appendLine("Daily distance:%.1f km".format(plan.avgDailyDist))
        if (route != null) {
            appendLine("Route:         ${route.name}")
            appendLine("Total dist:    %.1f km".format(route.totalDist))
            appendLine("Elev. gain:    %.0f m".format(route.elevationGain))
            appendLine("Elev. loss:    %.0f m".format(route.elevationLoss))
        }
        if (plan.description.isNotBlank()) {
            appendLine()
            appendLine("Description")
            appendLine("-".repeat(11))
            appendLine(plan.description)
        }
        if (plan.events.isNotEmpty()) {
            appendLine()
            appendLine("Schedule")
            appendLine("-".repeat(8))
            plan.events.sortedWith(compareBy({ it.dayNumber }, { it.plannedTime })).forEach { e ->
                appendLine(
                    "Day ${e.dayNumber}  ${SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(e.plannedTime))}  ${e.name} " +
                        "(%.1f km)".format(e.distCovered),
                )
            }
        }
    }

    private fun writePlanPdf(plan: ActivityPlan, route: Route?, file: File) {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val title = Paint().apply { textSize = 22f; isAntiAlias = true; isFakeBoldText = true }
        val heading = Paint().apply { textSize = 14f; isAntiAlias = true; isFakeBoldText = true }
        val body = Paint().apply { textSize = 12f; isAntiAlias = true }
        val muted = Paint().apply { textSize = 11f; isAntiAlias = true; color = 0xFF666666.toInt() }

        val fmt = SimpleDateFormat("EEE dd MMM yyyy HH:mm", Locale.getDefault())
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

        var y = 60f
        canvas.drawText(plan.name, 40f, y, title); y += 34
        canvas.drawText("Sport: ${plan.sportType.name}", 40f, y, body); y += 18
        canvas.drawText("Start: ${fmt.format(Date(plan.startDate))}", 40f, y, body); y += 18
        canvas.drawText(
            "Duration: ${plan.numberOfDays} day${if (plan.numberOfDays == 1) "" else "s"}",
            40f, y, body,
        )
        y += 18
        canvas.drawText("Avg speed: %.1f km/h".format(plan.avgSpeed), 40f, y, body); y += 18
        canvas.drawText("Daily distance: %.1f km".format(plan.avgDailyDist), 40f, y, body); y += 22

        if (route != null) {
            canvas.drawText("Route: ${route.name}", 40f, y, body); y += 18
            canvas.drawText(
                "Route stats: %.1f km · ↑ %.0f m · ↓ %.0f m".format(
                    route.totalDist, route.elevationGain, route.elevationLoss,
                ),
                40f, y, muted,
            )
            y += 24
        }

        if (plan.events.isNotEmpty()) {
            canvas.drawText("Schedule", 40f, y, heading); y += 20
            plan.events
                .sortedWith(compareBy({ it.dayNumber }, { it.plannedTime }))
                .forEach { e ->
                    if (y > 800f) return@forEach
                    canvas.drawText(
                        "Day ${e.dayNumber}  ${timeFmt.format(Date(e.plannedTime))}  ${e.name}",
                        40f, y, body,
                    )
                    y += 16
                }
        }

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
    }
}
