package com.willykez.dialer.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * One UI-style layered hero backdrop: the subject's photo scaled up and heavily blurred,
 * sitting behind a dark scrim so foreground text stays legible. Falls back to a flat
 * gradient when there's no photo, or on API levels below 31 where Modifier.blur is a no-op.
 * Shared by the in-call screen and the contact detail hero header.
 */
@Composable
fun BlurredPhotoBackdrop(
    photoUri: String?,
    modifier: Modifier = Modifier,
    height: Dp? = null,
    scrimStrength: Float = 1f
) {
    val context = LocalContext.current
    var bitmap by remember(photoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        bitmap = null
        if (!photoUri.isNullOrBlank()) {
            bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(android.net.Uri.parse(photoUri))
                        ?.use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }
        }
    }

    val sizeModifier = if (height != null) Modifier.fillMaxSize().height(height) else Modifier.fillMaxSize()

    Box(modifier = modifier.then(sizeModifier).background(MaterialTheme.colorScheme.background)) {
        val loaded = bitmap
        if (loaded != null) {
            Image(
                bitmap = loaded.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(60.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.45f * scrimStrength),
                            Color.Black.copy(alpha = 0.7f * scrimStrength),
                            Color.Black.copy(alpha = 0.92f * scrimStrength)
                        )
                    )
                )
        )
    }
}
