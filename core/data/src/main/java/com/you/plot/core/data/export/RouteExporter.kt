package com.you.plot.core.data.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import android.net.Uri
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
 * Generates a share-ready file for a [Route] in the requested format and returns a
 * FileProvider URI the caller can drop into an ACTION_SEND intent.
 *
 * GPX and TCX are proper XML formats; PDF renders a plain text summary via
 * [PdfDocument]. FIT and IMAGE are stubbed — see [UnsupportedExportFormatException].
 */
@Singleton
class RouteExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun export(route: Route, format: RouteExportFormat): Uri {
        val safeName = route.name.replace("[^A-Za-z0-9._-]".toRegex(), "_")
        val file = File(context.cacheDir, "exports").apply { mkdirs() }
            .resolve("$safeName.${format.extension}")
        when (format) {
            RouteExportFormat.GPX -> file.writeText(buildGpx(route))
            RouteExportFormat.TCX -> file.writeText(buildTcx(route))
            RouteExportFormat.PDF -> writeRoutePdf(route, file)
            RouteExportFormat.FIT -> throw UnsupportedExportFormatException("FIT")
            RouteExportFormat.IMAGE -> throw UnsupportedExportFormatException("Image")
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    private fun buildGpx(route: Route): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine(
            """<gpx version="1.1" creator="YouPlot" xmlns="http://www.topografix.com/GPX/1/1">"""
        )
        appendLine("  <metadata>")
        appendLine("    <name>${route.name.xmlEscaped()}</name>")
        if (route.description.isNotBlank())
            appendLine("    <desc>${route.description.xmlEscaped()}</desc>")
        appendLine("  </metadata>")

        // Named routepoints — one per waypoint the user placed on the plotter.
        appendLine("""  <rte>""")
        appendLine("    <name>${route.name.xmlEscaped()}</name>")
        route.waypoints.sortedBy { it.orderIndex }.forEach { wp ->
            appendLine("    <rtept lat=\"${wp.position.latitude}\" lon=\"${wp.position.longitude}\">")
            appendLine("      <name>${wp.name.xmlEscaped()}</name>")
            appendLine("    </rtept>")
        }
        appendLine("  </rte>")

        // Full geometry track — the OSRM decoded polyline is what actually navigates.
        val geometry = route.polyline.ifEmpty { route.waypoints.map { it.position } }
        if (geometry.isNotEmpty()) {
            appendLine("  <trk>")
            appendLine("    <name>${route.name.xmlEscaped()}</name>")
            appendLine("    <trkseg>")
            geometry.forEach { pt ->
                appendLine("      <trkpt lat=\"${pt.latitude}\" lon=\"${pt.longitude}\"/>")
            }
            appendLine("    </trkseg>")
            appendLine("  </trk>")
        }
        appendLine("</gpx>")
    }

    private fun buildTcx(route: Route): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine(
            """<TrainingCenterDatabase xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2">"""
        )
        appendLine("  <Courses>")
        appendLine("    <Course>")
        appendLine("      <Name>${route.name.take(15).xmlEscaped()}</Name>")
        appendLine("      <Lap>")
        appendLine("        <TotalTimeSeconds>0</TotalTimeSeconds>")
        appendLine("        <DistanceMeters>${(route.totalDist * 1000).toInt()}</DistanceMeters>")
        appendLine("      </Lap>")

        val geometry = route.polyline.ifEmpty { route.waypoints.map { it.position } }
        if (geometry.isNotEmpty()) {
            appendLine("      <Track>")
            val start = Date(route.createdAt)
            val stamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            geometry.forEachIndexed { i, pt ->
                appendLine("        <Trackpoint>")
                appendLine("          <Time>${stamp.format(Date(start.time + i * 1000L))}</Time>")
                appendLine("          <Position>")
                appendLine("            <LatitudeDegrees>${pt.latitude}</LatitudeDegrees>")
                appendLine("            <LongitudeDegrees>${pt.longitude}</LongitudeDegrees>")
                appendLine("          </Position>")
                appendLine("        </Trackpoint>")
            }
            appendLine("      </Track>")
        }

        route.waypoints
            .filter { it.orderIndex > 0 && it.orderIndex < route.waypoints.size - 1 }
            .forEach { wp ->
                appendLine("      <CoursePoint>")
                appendLine("        <Name>${wp.name.take(10).xmlEscaped()}</Name>")
                appendLine(
                    "        <Time>${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                        .format(Date(route.createdAt))}</Time>"
                )
                appendLine("        <Position>")
                appendLine("          <LatitudeDegrees>${wp.position.latitude}</LatitudeDegrees>")
                appendLine("          <LongitudeDegrees>${wp.position.longitude}</LongitudeDegrees>")
                appendLine("        </Position>")
                appendLine("        <PointType>Generic</PointType>")
                appendLine("      </CoursePoint>")
            }

        appendLine("    </Course>")
        appendLine("  </Courses>")
        appendLine("</TrainingCenterDatabase>")
    }

    private fun writeRoutePdf(route: Route, file: File) {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val title = Paint().apply { textSize = 22f; isAntiAlias = true; isFakeBoldText = true }
        val body = Paint().apply { textSize = 13f; isAntiAlias = true }
        val muted = Paint().apply { textSize = 11f; isAntiAlias = true; color = 0xFF666666.toInt() }

        var y = 60f
        canvas.drawText(route.name, 40f, y, title); y += 34
        canvas.drawText("Sport: ${route.sportType.name}", 40f, y, body); y += 20
        canvas.drawText("Distance: %.2f km".format(route.totalDist), 40f, y, body); y += 20
        canvas.drawText("Elevation gain: %.0f m".format(route.elevationGain), 40f, y, body); y += 20
        canvas.drawText("Elevation loss: %.0f m".format(route.elevationLoss), 40f, y, body); y += 20
        canvas.drawText(
            "Round trip: ${if (route.isRoundTrip) "yes" else "no"}", 40f, y, body,
        )
        y += 30
        if (route.description.isNotBlank()) {
            canvas.drawText("Description:", 40f, y, body); y += 18
            route.description.chunked(80).take(6).forEach {
                canvas.drawText(it, 40f, y, muted); y += 16
            }
            y += 8
        }
        canvas.drawText("Waypoints:", 40f, y, body); y += 20
        route.waypoints.sortedBy { it.orderIndex }.forEach { wp ->
            if (y > 780f) return@forEach
            canvas.drawText(
                "${wp.orderIndex + 1}. ${wp.name} — %.1f km".format(wp.distFromStart),
                40f, y, muted,
            )
            y += 16
        }

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
    }
}

private fun String.xmlEscaped(): String = this
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&apos;")
