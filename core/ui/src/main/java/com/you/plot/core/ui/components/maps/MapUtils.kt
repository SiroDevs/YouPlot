package com.you.plot.core.ui.components.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.graphics.createBitmap

private const val COLOR_MARKER_TEXT = Color.WHITE

private const val PIN_W = 72
private const val PIN_H = 90
private const val CIRCLE_R = 28f

/**
 * Draws a droplet-style pin bitmap the map layer can render as a marker icon.
 * Shared by every screen that composes [PlotterMap] — sits in core:ui because
 * PlotterMap itself does, and features can't be depended on from a core module.
 */
fun makePinMarker(context: Context, color: Int, label: String): Bitmap {
    val bmp = createBitmap(PIN_W, PIN_H)
    val canvas = Canvas(bmp)
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; style = Paint.Style.FILL
    }
    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = COLOR_MARKER_TEXT; textSize = 26f
        typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    val cx = PIN_W / 2f
    val cy = CIRCLE_R + 4f
    canvas.drawCircle(cx, cy, CIRCLE_R, fillPaint)
    canvas.drawCircle(cx, cy, CIRCLE_R, rimPaint)
    val path = Path().apply {
        moveTo(cx - CIRCLE_R * 0.6f, cy + CIRCLE_R * 0.6f)
        lineTo(cx, PIN_H.toFloat() - 4f)
        lineTo(cx + CIRCLE_R * 0.6f, cy + CIRCLE_R * 0.6f)
        close()
    }
    canvas.drawPath(path, fillPaint)
    canvas.drawPath(path, rimPaint)
    val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(label, cx, textY, textPaint)
    return bmp
}

fun makeCircleMarker(context: Context, color: Int, label: String): Bitmap {
    val size = (CIRCLE_R * 2 + 8).toInt()
    val bmp = createBitmap(size, size)
    val canvas = Canvas(bmp)
    val cx = size / 2f
    val cy = size / 2f
    val r = CIRCLE_R
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; style = Paint.Style.FILL
    }
    val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = COLOR_MARKER_TEXT; textSize = 22f
        typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    canvas.drawCircle(cx, cy, r, fillPaint)
    canvas.drawCircle(cx, cy, r, rimPaint)
    val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(label, cx, textY, textPaint)
    return bmp
}
