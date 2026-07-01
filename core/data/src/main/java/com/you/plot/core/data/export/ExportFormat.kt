package com.you.plot.core.data.export

/** Formats a route can be exported to. Kept separate from plan formats since the
 *  two share very little content-model overlap. */
enum class RouteExportFormat(val extension: String, val mime: String, val display: String) {
    GPX("gpx", "application/gpx+xml", "GPX"),
    TCX("tcx", "application/vnd.garmin.tcx+xml", "TCX"),
    PDF("pdf", "application/pdf", "PDF"),
    FIT("fit", "application/vnd.ant.fit", "FIT"),
    IMAGE("png", "image/png", "Image"),
}

enum class PlanExportFormat(val extension: String, val mime: String, val display: String) {
    PDF("pdf", "application/pdf", "PDF"),
    TXT("txt", "text/plain", "Text"),
    IMAGE("png", "image/png", "Image"),
}

/** Something a callable exporter can throw when the requested format is stubbed. */
class UnsupportedExportFormatException(format: String) :
    UnsupportedOperationException("Export to $format is not yet implemented.")
