package com.willykez.dialer.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.dialer.ui.theme.EmberGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * The app's one recurring, unmistakable identity mark: every avatar — with or without a
 * photo — sits inside a rotated Ember sweep-gradient ring, the rotation deterministically
 * seeded per-person so everyone reads as a distinct little "badge" rather than a flat gray
 * circle. Appears identically on Recents, Contacts, Contact Detail, and the call screen.
 */
@Composable
fun ContactAvatar(
    photoUri: String?,
    initials: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    ringSeed: String = initials,
    showRing: Boolean = true
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

    val rotationDegrees = remember(ringSeed) { (abs(ringSeed.hashCode()) % 360).toFloat() }
    val ringWidth = size * 0.065f
    val innerSize = size - (ringWidth * 2) - 4.dp

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (showRing) {
            Canvas(modifier = Modifier.size(size)) {
                rotate(rotationDegrees) {
                    drawCircle(
                        brush = Brush.sweepGradient(EmberGradient),
                        radius = (size.toPx() / 2f) - (ringWidth.toPx() / 2f),
                        style = Stroke(width = ringWidth.toPx())
                    )
                }
            }
        }

        val loadedBitmap = bitmap
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            if (loadedBitmap != null) {
                Image(
                    bitmap = loadedBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(innerSize).clip(CircleShape)
                )
            } else {
                Text(
                    text = initials.take(2).uppercase().ifBlank { "?" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black,
                    fontSize = (innerSize.value / 2.6f).sp
                )
            }
        }
    }
}
