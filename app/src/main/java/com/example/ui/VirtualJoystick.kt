package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Joystick analógico virtual táctil para movimiento 3D en smartphones
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    onMove: (x: Float, y: Float) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(size)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                val radius = (size.toPx() / 2f) * 0.75f

                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                        val delta = offset - center
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y)
                        val clampedOffset = if (dist > radius) {
                            val angle = atan2(delta.y, delta.x)
                            Offset(cos(angle) * radius, sin(angle) * radius)
                        } else {
                            delta
                        }
                        thumbOffset = clampedOffset
                        val normalizedX = (clampedOffset.x / radius).coerceIn(-1f, 1f)
                        val normalizedY = (clampedOffset.y / radius).coerceIn(-1f, 1f)
                        onMove(normalizedX, normalizedY)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)

                        val clampedOffset = if (dist > radius) {
                            val angle = atan2(newOffset.y, newOffset.x)
                            Offset(cos(angle) * radius, sin(angle) * radius)
                        } else {
                            newOffset
                        }

                        thumbOffset = clampedOffset
                        val normalizedX = (clampedOffset.x / radius).coerceIn(-1f, 1f)
                        val normalizedY = (clampedOffset.y / radius).coerceIn(-1f, 1f)
                        onMove(normalizedX, normalizedY)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = (this.size.width / 2f) * 0.85f
            val thumbRadius = (this.size.width / 2f) * 0.32f

            // Anillo exterior base
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3300E5FF), Color(0x15003366)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )

            // Borde exterior neon
            drawCircle(
                color = Color(0x8800E5FF),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Líneas guía cardinales del joystick
            drawLine(
                color = Color(0x4400E5FF),
                start = Offset(center.x - outerRadius * 0.6f, center.y),
                end = Offset(center.x + outerRadius * 0.6f, center.y),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color(0x4400E5FF),
                start = Offset(center.x, center.y - outerRadius * 0.6f),
                end = Offset(center.x, center.y + outerRadius * 0.6f),
                strokeWidth = 1.5.dp.toPx()
            )

            // Knob interior (la palanca que se mueve con el dedo)
            val knobCenter = center + thumbOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF007799)),
                    center = knobCenter,
                    radius = thumbRadius
                ),
                radius = thumbRadius,
                center = knobCenter
            )

            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = knobCenter,
                style = Stroke(width = 2.dp.toPx())
            )

            // Punto central del pomo
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = knobCenter
            )
        }
    }
}
