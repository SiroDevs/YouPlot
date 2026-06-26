/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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