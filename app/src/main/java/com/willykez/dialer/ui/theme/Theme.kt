package com.willykez.dialer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    secondary = AccentBlue,
    tertiary = AccentAmber,
    background = SurfaceBlack,
    onBackground = TextHigh,
    surface = SurfaceDim,
    onSurface = TextHigh,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = TextMedium,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHigh,
    outline = OutlineDim,
    error = AccentRed,
    onError = Color.Black
)

private val LightColors = lightColorScheme(
    primary = AccentGreenDark,
    onPrimary = LightSurface,
    secondary = AccentBlue,
    tertiary = AccentAmber,
    background = LightSurface,
    onBackground = LightTextHigh,
    surface = LightSurface,
    onSurface = LightTextHigh,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightTextMedium,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHigh,
    outline = LightTextMedium,
    error = AccentRed,
    onError = LightSurface
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DialerTypography,
        shapes = DialerShapes,
        content = content
    )
}
