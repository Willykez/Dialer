package com.willykez.dialer.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AmoledDarkScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    secondary = InteractiveTrack,
    onSecondary = TextHigh,
    tertiary = AccentAmber,
    onTertiary = Color.Black,
    background = SurfaceBlack,
    onBackground = TextHigh,
    surface = SurfaceDim,
    onSurface = TextHigh,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextMedium,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = InteractiveTrack,
    outline = OutlineDim,
    error = AccentRed,
    onError = TextHigh
)

val DialerShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun DialerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AmoledDarkScheme,
        typography = DialerTypography,
        shapes = DialerShapes,
        content = content
    )
}
