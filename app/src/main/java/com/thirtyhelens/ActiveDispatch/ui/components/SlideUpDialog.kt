package com.thirtyhelens.ActiveDispatch.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@Composable
fun SlideUpDialog(
    onRequestClose: () -> Unit,
    content: @Composable () -> Unit
) {
    // Full-screen dialog (so it truly overlays the screen)
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onRequestClose,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false // full width
        )
    ) {
        // Animate vertical offset from screen bottom to 0
        val density = LocalDensity.current
        val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.roundToPx() }
        var visible by remember { mutableStateOf(false) }
        val offsetY by animateIntAsState(
            targetValue = if (visible) 0 else screenHeightPx,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            label = "sheet-offset"
        )

        LaunchedEffect(Unit) { visible = true }

        // Scrim + sheet container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)) // scrim
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onRequestClose() } // tap scrim to close
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(x = 0, y = offsetY) } // slide up
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {} // consume clicks so content doesn’t close
            ) {
                // Your custom gradient/modal content
                content()
            }
        }
    }
}
