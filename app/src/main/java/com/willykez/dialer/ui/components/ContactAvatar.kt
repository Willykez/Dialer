package com.willykez.dialer.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ContactAvatar(
    photoUri: String?,
    initials: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmapState = produceState<android.graphics.Bitmap?>(initialValue = null, key1 = photoUri) {
        value = if (photoUri.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(android.net.Uri.parse(photoUri))
                        ?.use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = bitmapState.value
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(size).clip(CircleShape)
            )
        } else {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = (size.value / 2.6f).sp
            )
        }
    }
}
