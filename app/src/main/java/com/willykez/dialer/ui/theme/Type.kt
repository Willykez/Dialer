package com.willykez.dialer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DialerTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold),
        headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.Default),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.Default),
        labelLarge = base.labelLarge.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium)
    )
}

val DialpadDigitStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Light,
    fontSize = 40.sp
)
