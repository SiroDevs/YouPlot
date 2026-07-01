package com.you.plot.core.common.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("ConstantLocale")
val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

@SuppressLint("ConstantLocale")
val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

@SuppressLint("ConstantLocale")
val dateTimeFmt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

object AppSpecs {
    val CORNER = 16.dp
    val TOP_SHAPE = RoundedCornerShape(topStart = CORNER, topEnd = CORNER)
    val BOTTOM_SHAPE = RoundedCornerShape(bottomStart = CORNER, bottomEnd = CORNER)
    val FULL_SHAPE = RoundedCornerShape(CORNER)
}