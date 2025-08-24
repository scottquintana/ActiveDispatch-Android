// com/thirtyhelens/ActiveDispatch/maps/IconUtils.kt
package com.thirtyhelens.ActiveDispatch.maps

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.geometry.Size

@Composable
fun bitmapDescriptorFromVector(
    context: Context,
    imageVector: ImageVector,
    tint: Color,
    size: Dp = 20.dp // good for map pins
): BitmapDescriptor {
    val painter = rememberVectorPainter(image = imageVector)
    val density = LocalDensity.current
    val px = with(density) { size.roundToPx() }

    val img = ImageBitmap(px, px)
    val androidCanvas = AndroidCanvas(img.asAndroidBitmap())
    val composeCanvas = ComposeCanvas(androidCanvas)

    val drawScope = CanvasDrawScope()
    drawScope.draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = composeCanvas,
        size = Size(px.toFloat(), px.toFloat())
    ) {
        with(painter) {
            draw(
                size = Size(px.toFloat(), px.toFloat()),
                colorFilter = ColorFilter.tint(tint)
            )
        }
    }

    return BitmapDescriptorFactory.fromBitmap(img.asAndroidBitmap())
}