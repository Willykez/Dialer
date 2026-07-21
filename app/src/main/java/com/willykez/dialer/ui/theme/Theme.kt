package com.willykez.dialer.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Static AMOLED fallback palette, used on devices below Android 12 (no dynamic color / Monet).
 */
private val AmoledDarkScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    primaryContainer = AccentGreenDark,
    onPrimaryContainer = TextHigh,
    secondary = InteractiveTrack,
    onSecondary = TextHigh,
    secondaryContainer = SurfaceContainerHigh,
    onSecondaryContainer = TextHigh,
    tertiary = AccentAmber,
    onTertiary = Color.Black,
    background = SurfaceBlack,
    onBackground = TextHigh,
    surface = SurfaceBlack,
    onSurface = TextHigh,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextMedium,
    surfaceContainer = SurfaceContainer,
    surfaceContainerLow = SurfaceDim,
    surfaceContainerLowest = SurfaceBlack,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = InteractiveTrack,
    outline = OutlineDim,
    outlineVariant = OutlineDim,
    error = AccentRed,
    onError = TextHigh
)

/**
 * Rebuilds a Monet-derived dynamic color scheme onto a true-black (AMOLED) surface set,
 * the way One UI / Pixel dialers blend "dynamic color" with an OLED-friendly background:
 * hue & tone come from the user's wallpaper, but background/surface stay near-black to
 * save battery and match the rest of this dialer's aesthetic.
 */
private fun ColorScheme.pinnedToAmoled(): ColorScheme = copy(
    background = SurfaceBlack,
    onBackground = TextHigh,
    surface = SurfaceBlack,
    surfaceContainerLowest = SurfaceBlack,
    surfaceContainerLow = SurfaceDim,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = InteractiveTrack
)

/**
 * M3 Expressive shape scale: rounder, more "squircle"-like at rest, echoing the liquid-glass
 * pill navigation and One UI-style call surfaces used throughout the app.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val DialerShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
    largeIncreased = RoundedCornerShape(34.dp),
    extraLarge = RoundedCornerShape(40.dp),
    extraLargeIncreased = RoundedCornerShape(44.dp),
    extraExtraLarge = RoundedCornerShape(56.dp)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DialerTheme(
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = remember(useDynamicColor) {
        if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context).pinnedToAmoled()
        } else {
            AmoledDarkScheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = DialerTypography,
        shapes = DialerShapes,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}
