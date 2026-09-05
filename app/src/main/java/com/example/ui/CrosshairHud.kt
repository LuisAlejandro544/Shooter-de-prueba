package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Retícula de puntería sci-fi central con hit marker reactivo
 */
@Composable
fun CrosshairHud(
    modifier: Modifier = Modifier,
    isHit: Boolean,
    isFiring: Boolean
) {
    val spread by animateFloatAsState(
        targetValue = if (isFiring) 22f else 12f,
        animationSpec = tween(durationMillis = 80),
        label = "crosshairSpread"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(90.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidth = 2.dp.toPx()
            val color = if (isHit) Color(0xFFFF2A4B) else Color(0xCC00E5FF)

            // Punto central
            drawCircle(
                color = if (isHit) Color(0xFFFF2A4B) else Color(0xFFFFFFFF),
                radius = 2.dp.toPx(),
                center = center
            )

            // Líneas cardinales de la retícula
            val lineLength = 10.dp.toPx()

            // Superior
            drawLine(
                color = color,
                start = Offset(center.x, center.y - spread - lineLength),
                end = Offset(center.x, center.y - spread),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Inferior
            drawLine(
                color = color,
                start = Offset(center.x, center.y + spread),
                end = Offset(center.x, center.y + spread + lineLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Izquierda
            drawLine(
                color = color,
                start = Offset(center.x - spread - lineLength, center.y),
                end = Offset(center.x - spread, center.y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Derecha
            drawLine(
                color = color,
                start = Offset(center.x + spread, center.y),
                end = Offset(center.x + spread + lineLength, center.y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Hit Marker en forma de "X" cuando se impacta un objetivo
            if (isHit) {
                val hitSize = 14.dp.toPx()
                val hitStroke = 3.dp.toPx()
                val hitColor = Color(0xFFFF1744)

                // Diagonal 1 (\)
                drawLine(
                    color = hitColor,
                    start = Offset(center.x - hitSize, center.y - hitSize),
                    end = Offset(center.x + hitSize, center.y + hitSize),
                    strokeWidth = hitStroke,
                    cap = StrokeCap.Round
                )
                // Diagonal 2 (/)
                drawLine(
                    color = hitColor,
                    start = Offset(center.x + hitSize, center.y - hitSize),
                    end = Offset(center.x - hitSize, center.y + hitSize),
                    strokeWidth = hitStroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
