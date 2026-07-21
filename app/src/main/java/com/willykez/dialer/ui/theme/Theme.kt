package com.willykez.dialer.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
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
 * Rounder-than-default shape scale, echoing the liquid-glass pill nav and One UI-style call
 * surfaces used throughout the app. Uses only the stable 5-role Shapes constructor — the
 * expanded Material 3 Expressive shape tokens (largeIncreased, extraExtraLarge, etc.) are
 * still internal-only in the resolved material3 artifact, so they aren't available yet.
 */
val DialerShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DialerTypography,
        shapes = DialerShapes,
        content = content
    )
}
