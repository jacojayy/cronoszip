package com.cronoszip.app.ui.background

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cronoszip.app.ui.theme.CronosBlue
import com.cronoszip.app.ui.theme.CronosCyan
import com.cronoszip.app.ui.theme.Ink
import com.cronoszip.app.ui.theme.InkBlue
import com.cronoszip.app.ui.theme.NightBlue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val drift = if (animate) {
        val transition = rememberInfiniteTransition(label = "liquid-drift")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(26000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "drift",
        ).value
    } else {
        0f
    }

    val wave = if (animate) {
        val transition = rememberInfiniteTransition(label = "liquid-wave")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(11500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "wave",
        ).value
    } else {
        0f
    }

    val pulse = if (animate) {
        val transition = rememberInfiniteTransition(label = "liquid-pulse")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(7200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse",
        ).value
    } else {
        0.35f
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(NightBlue, InkBlue, Color(0xFF01040A), Ink),
                startY = 0f,
                endY = size.height,
            ),
        )

        val s = size.minDimension
        val tau = (PI * 2.0).toFloat()

        val x1 = size.width * (0.18f + 0.10f * sin((drift * tau).toDouble()).toFloat())
        val y1 = size.height * (0.16f + 0.06f * cos((drift * tau * 0.8).toDouble()).toFloat())
        softGlow(
            center = Offset(x1, y1),
            radius = s * (0.62f + 0.05f * pulse),
            color = CronosBlue.copy(alpha = 0.30f),
        )

        val x2 = size.width * (0.86f + 0.06f * cos((drift * tau * 0.7).toDouble()).toFloat())
        val y2 = size.height * (0.42f + 0.07f * sin((drift * tau * 0.9).toDouble()).toFloat())
        softGlow(
            center = Offset(x2, y2),
            radius = s * 0.48f,
            color = CronosCyan.copy(alpha = 0.16f),
        )

        val x3 = size.width * (0.28f + 0.12f * cos((drift * tau * 0.6).toDouble()).toFloat())
        val y3 = size.height * (0.82f + 0.04f * sin((drift * tau * 0.5).toDouble()).toFloat())
        softGlow(
            center = Offset(x3, y3),
            radius = s * 0.62f,
            color = Color(0xFF0B4DD8).copy(alpha = 0.20f),
        )

        val center = Offset(size.width / 2f, size.height * 0.13f)
        val ringBase = s * (0.20f + 0.008f * pulse)
        repeat(4) { index ->
            drawCircle(
                color = Color.White.copy(alpha = 0.045f - index * 0.008f),
                radius = ringBase + index * s * 0.085f,
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CronosCyan.copy(alpha = 0.12f), Color.Transparent),
                center = center,
                radius = s * 0.36f,
            ),
            radius = s * 0.36f,
            center = center,
        )

        drawWave(
            yBase = size.height * 0.81f,
            amplitude = s * 0.035f,
            wavelength = size.width * 0.72f,
            phase = wave * tau,
            color = CronosBlue.copy(alpha = 0.16f),
        )
        drawWave(
            yBase = size.height * 0.87f,
            amplitude = s * 0.027f,
            wavelength = size.width * 0.54f,
            phase = -wave * tau * 1.35f + 1.3f,
            color = CronosCyan.copy(alpha = 0.10f),
        )
        drawWave(
            yBase = size.height * 0.94f,
            amplitude = s * 0.020f,
            wavelength = size.width * 0.86f,
            phase = wave * tau * 0.75f + 2.6f,
            color = Color.White.copy(alpha = 0.055f),
        )

        val dots = listOf(
            Offset(size.width * 0.12f, size.height * 0.36f) to 2.2f,
            Offset(size.width * 0.89f, size.height * 0.24f) to 1.6f,
            Offset(size.width * 0.78f, size.height * 0.67f) to 2.6f,
            Offset(size.width * 0.05f, size.height * 0.70f) to 1.5f,
            Offset(size.width * 0.52f, size.height * 0.08f) to 1.3f,
        )
        dots.forEachIndexed { index, (point, radius) ->
            val alpha = 0.10f + 0.06f * sin((drift * tau + index).toDouble()).toFloat()
            drawCircle(
                color = Color.White.copy(alpha = alpha.coerceAtLeast(0.02f)),
                radius = radius.dp.toPx(),
                center = point,
            )
        }

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                center = Offset(size.width / 2f, size.height * 0.48f),
                radius = size.maxDimension * 0.78f,
            ),
        )
    }
}

private fun DrawScope.softGlow(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = color.alpha * 0.35f), Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

private fun DrawScope.drawWave(
    yBase: Float,
    amplitude: Float,
    wavelength: Float,
    phase: Float,
    color: Color,
) {
    val path = Path()
    path.moveTo(0f, yBase)
    var x = 0f
    val step = 6.dp.toPx().coerceAtLeast(2f)
    while (x <= size.width + step) {
        val y = yBase + sin((x / wavelength) * PI * 2 + phase).toFloat() * amplitude
        path.lineTo(x, y)
        x += step
    }
    path.lineTo(size.width, size.height)
    path.lineTo(0f, size.height)
    path.close()
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(color, Color.Transparent),
            startY = yBase - amplitude,
            endY = size.height,
        ),
    )
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.06f),
        style = Stroke(width = 1.dp.toPx()),
    )
}
