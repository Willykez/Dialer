package com.willykez.dialer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Static AMOLED fallback palette, used on devices below Android 12 (no dynamic color / Monet)
 * when the app is in dark mode.
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
 * Static light-mode fallback, used below Android 12 when the app is in light mode — a soft
 * lavender-white surface set (matching the gentle tint Google's Phone app uses) rather than
 * a stark, clinical white.
 */
private val EmberLightScheme = lightColorScheme(
    primary = EmberOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCCB),
    onPrimaryContainer = Color(0xFF3A1200),
    secondary = SurfaceLightContainerHighest,
    onSecondary = TextHighOnLight,
    secondaryContainer = SurfaceLightContainerHigh,
    onSecondaryContainer = TextHighOnLight,
    tertiary = EmberTeal,
    onTertiary = Color.White,
    background = SurfaceLightBackground,
    onBackground = TextHighOnLight,
    surface = SurfaceLightBackground,
    onSurface = TextHighOnLight,
    surfaceVariant = SurfaceLightContainerHigh,
    onSurfaceVariant = TextMediumOnLight,
    surfaceContainer = SurfaceLightContainer,
    surfaceContainerLow = SurfaceLightBackground,
    surfaceContainerLowest = Color.White,
    surfaceContainerHigh = SurfaceLightContainerHigh,
    surfaceContainerHighest = SurfaceLightContainerHighest,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
    error = AccentRed,
    onError = Color.White
)

/**
 * Rebuilds a Monet-derived dynamic color scheme onto a true-black (AMOLED) surface set,
 * the way One UI / Pixel dialers blend "dynamic color" with an OLED-friendly background:
 * hue & tone come from the user's wallpaper, but background/surface stay near-black to
 * save battery in dark mode.
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
 * Rebuilds a Monet-derived dynamic light scheme onto the app's soft lavender-white surface
 * set, so light mode still feels like *this* app and not a generic Material palette.
 */
private fun ColorScheme.pinnedToLightSurfaces(): ColorScheme = copy(
    background = SurfaceLightBackground,
    onBackground = TextHighOnLight,
    surface = SurfaceLightBackground,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SurfaceLightBackground,
    surfaceContainer = SurfaceLightContainer,
    surfaceContainerHigh = SurfaceLightContainerHigh,
    surfaceContainerHighest = SurfaceLightContainerHighest
)

/**
 * Rounder-than-default shape scale, echoing the liquid-glass pill nav and One UI-style call
 * surfaces used throughout the app. Uses the stable 5-role Shapes constructor.
 */
val DialerShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

/**
 * Follows the system's light/dark setting by default (like Google's Phone app), applying
 * dynamic wallpaper color when available on Android 12+, pinned onto this app's own surface
 * palette for either mode so it never reads as a stock, generic Material theme.
 */
@Composable
fun DialerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = remember(darkTheme, useDynamicColor) {
        val supportsDynamicColor = useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        when {
            darkTheme && supportsDynamicColor -> dynamicDarkColorScheme(context).pinnedToAmoled()
            darkTheme -> AmoledDarkScheme
            supportsDynamicColor -> dynamicLightColorScheme(context).pinnedToLightSurfaces()
            else -> EmberLightScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DialerTypography,
        shapes = DialerShapes,
        content = content
    )
}
