package com.splicr.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.splicr.app.data.CanvasItemData

@Composable
fun ThumbnailImage(
    thumbnailBitmap: Bitmap?, canvasItemData: CanvasItemData, modifier: Modifier = Modifier
) {
    if (thumbnailBitmap != null) {
        Image(
            modifier = modifier,
            bitmap = thumbnailBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            modifier = modifier,
            model = canvasItemData.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}