package com.bookend.reflection.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val defaults = Typography()

/** Serif for the things you read, sans for the things you tap. */
val BookendTypography = defaults.copy(
    displaySmall = defaults.displaySmall.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 42.sp,
    ),
    headlineLarge = defaults.headlineLarge.copy(fontFamily = FontFamily.Serif),
    headlineMedium = defaults.headlineMedium.copy(
        fontFamily = FontFamily.Serif,
        fontSize = 27.sp,
        lineHeight = 34.sp,
    ),
    headlineSmall = defaults.headlineSmall.copy(
        fontFamily = FontFamily.Serif,
        fontSize = 21.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = defaults.titleLarge.copy(
        fontFamily = FontFamily.Serif,
        fontSize = 20.sp,
    ),
    titleMedium = defaults.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = defaults.bodyLarge.copy(
        fontSize = 17.sp,
        lineHeight = 26.sp,
    ),
    labelLarge = defaults.labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = defaults.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
    ),
)

val BookendShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
