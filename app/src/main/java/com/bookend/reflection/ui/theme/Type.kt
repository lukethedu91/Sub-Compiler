package com.bookend.reflection.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val defaults = Typography()

val BookendTypography = defaults.copy(
    displaySmall = defaults.displaySmall.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
    ),
    headlineMedium = defaults.headlineMedium.copy(
        fontFamily = FontFamily.Serif,
    ),
    headlineSmall = defaults.headlineSmall.copy(
        fontFamily = FontFamily.Serif,
    ),
    titleLarge = defaults.titleLarge.copy(
        fontFamily = FontFamily.Serif,
    ),
    bodyLarge = defaults.bodyLarge.copy(
        fontSize = 17.sp,
        lineHeight = 26.sp,
    ),
)
