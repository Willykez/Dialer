package com.willykez.dialer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * "Ember" editorial type scale: oversized, black-weight, tightly tracked display sizes for
 * screen headers (the "Recents" / "Contacts" titles read like a magazine masthead rather than
 * a generic app-bar label), stepping down to plainer body/label weights for content.
 */
val DialerTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1.2).sp
        ),
        displayMedium = base.displayMedium.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.8).sp
        ),
        headlineLarge = base.headlineLarge.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Black,
            fontSize = 40.sp,
            letterSpacing = (-1).sp
        ),
        headlineMedium = base.headlineMedium.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.4).sp
        ),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.Default),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.Default),
        labelLarge = base.labelLarge.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        ),
        labelMedium = base.labelMedium.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    )
}

val DialpadDigitStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Light,
    fontSize = 40.sp
)
