// com/thirtyhelens/ActiveDispatch/maps/IncidentPinBitmap.kt
package com.thirtyhelens.ActiveDispatch.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path            // <-- Compose Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.max

@Composable
fun buildIncidentMarkerDescriptor(
    context: Context,
    icon: ImageVector,
    fillColor: Color,
    label: String?,
    labelColor: Color,
    circleDiameter: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    tailWidth: Dp = 10.dp,
    tailHeight: Dp = 8.dp,
    strokeWidth: Dp = 1.dp,
    textSizeSp: Float = 12f,
    verticalGap: Dp = 6.dp
): BitmapDescriptor {
    // Ensure Maps is initialized so BitmapDescriptorFactory is ready
    MapsInitializer.initialize(context.applicationContext)

    val density = LocalDensity.current
    val circlePx = with(density) { circleDiameter.roundToPx() }
    val iconPx   = with(density) { iconSize.roundToPx() }
    val tailWpx  = with(density) { tailWidth.roundToPx() }
    val tailHpx  = with(density) { tailHeight.roundToPx() }
    val strokePx = with(density) { strokeWidth.toPx() } // Float
    val gapPx    = with(density) { verticalGap.roundToPx() }

    // Measure text with Android paint (baseline control)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = labelColor.toArgb()
        textSize = with(density) { textSizeSp.sp.toPx() }
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textWidth  = if (label != null) textPaint.measureText(label) else 0f
    val fm         = if (label != null) textPaint.fontMetrics else Paint.FontMetrics()
    val textHeight = if (label != null) (fm.bottom - fm.top) else 0f

    val bmpWidth  = max(circlePx.toFloat(), textWidth) + 16f
    val extraText = if (label != null) (tailHpx + gapPx + textHeight) else tailHpx
    val bmpHeight = circlePx.toFloat() + extraText.toFloat()

    val imageBitmap = ImageBitmap(bmpWidth.toInt(), bmpHeight.toInt())
    val androidCanvas = AndroidCanvas(imageBitmap.asAndroidBitmap())

    val composeCanvas = androidx.compose.ui.graphics.Canvas(androidCanvas)
    val scope = CanvasDrawScope()

    scope.draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = composeCanvas,
        size = Size(bmpWidth, bmpHeight)
    ) {
        val centerX = size.width / 2f
        val circleCY = circlePx / 2f
        val circleBottom = circleCY + circlePx / 2f

        // Shadow
        drawCircle(
            color = Color(0x40000000),
            radius = circlePx / 2f + 2f,
            center = Offset(centerX, circleCY + 1.5f)
        )

        // Circle fill
        drawCircle(
            color = fillColor,
            radius = circlePx / 2f,
            center = Offset(centerX, circleCY)
        )

        // Circle stroke
        drawCircle(
            color = fillColor.darken(0.8f),
            radius = circlePx / 2f - strokePx / 2f,
            center = Offset(centerX, circleCY),
            style = Stroke(width = strokePx)
        )

        // Tail (Compose Path) – small overlap to avoid seam
        val tailTopY = circleBottom - 1f
        val tailPath = Path().apply {
            moveTo(centerX - tailWpx / 2f, tailTopY)             // left top
            lineTo(centerX + tailWpx / 2f, tailTopY)             // right top
            lineTo(centerX, tailTopY + tailHpx)                  // tip
            close()
        }

        // Tail fill
        drawPath(path = tailPath, color = fillColor.darken(0.8f))
        // Tail stroke
        drawPath(path = tailPath, color = fillColor.darken(0.8f), style = Stroke(width = strokePx))

        // Icon (white) centered in circle
        val painter = rememberVectorPainter(image = icon)
        val iconLeft = centerX - iconPx / 2f
        val iconTop  = circleCY - iconPx / 2f
        withTransform({ translate(iconLeft, iconTop) }) {
            with(painter) {
                draw(
                    size = Size(iconPx.toFloat(), iconPx.toFloat()),
                    alpha = 1f,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                )
            }
        }
    }

    // Label centered under tail (Android canvas for baseline)
    if (label != null) {
        val textX = (bmpWidth - textWidth) / 2f
        val labelTopY = circlePx + tailHpx + gapPx
        val baseline = labelTopY - fm.top
        androidCanvas.drawText(label, textX, baseline, textPaint)
    }

    return BitmapDescriptorFactory.fromBitmap(imageBitmap.asAndroidBitmap())
}

fun Color.darken(factor: Float = 0.8f): Color {
    // factor < 1 makes it darker, e.g. 0.8 = 20% darker
    return Color(
        red = max(0f, this.red * factor),
        green = max(0f, this.green * factor),
        blue = max(0f, this.blue * factor),
        alpha = this.alpha
    )
}