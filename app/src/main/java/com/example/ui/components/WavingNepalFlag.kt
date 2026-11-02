package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated waving/fluttering National Flag of Nepal (नेपालको झन्डा फरफराएको जस्तो)
 * Accurately models Nepal's unique double-pennant geometry with deep Nepal blue border,
 * crimson red body, white crescent moon with rays, and 12-pointed sun, with smooth sinusoidal waving.
 */
@Composable
fun WavingNepalFlag(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "nepal_flag_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flag_phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val poleWidth = 3.5.dp.toPx()
        val poleX = 3.dp.toPx()

        // 1. Draw Flag Pole
        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(poleX, 0f),
            end = Offset(poleX, h),
            strokeWidth = poleWidth,
            cap = StrokeCap.Round
        )

        // Golden Pole Top Finial (कलश/गोलो)
        drawCircle(
            color = Color(0xFFFFD700),
            radius = 3.5.dp.toPx(),
            center = Offset(poleX, 2.dp.toPx())
        )

        val flagLeft = poleX + poleWidth / 2f
        val flagW = w - flagLeft
        val flagH = h * 0.94f

        // Sinusoidal wave flutter displacements on pennant tips
        val waveAmp = 3.5.dp.toPx()
        val waveTopTip = (sin(phase.toDouble()) * waveAmp).toFloat()
        val waveNotch = (sin(phase.toDouble() + 1.6) * (waveAmp * 0.7f)).toFloat()
        val waveBottomTip = (sin(phase.toDouble() + 3.1) * waveAmp).toFloat()

        // Outer coordinates for the blue border
        val p0 = Offset(flagLeft, 3.dp.toPx())
        val p1 = Offset(flagLeft + flagW * 0.88f + waveTopTip, 3.dp.toPx() + flagH * 0.44f + waveTopTip * 0.3f)
        val p2 = Offset(flagLeft + flagW * 0.36f + waveNotch * 0.5f, 3.dp.toPx() + flagH * 0.48f)
        val p3 = Offset(flagLeft + flagW * 0.94f + waveBottomTip, 3.dp.toPx() + flagH * 0.95f + waveBottomTip * 0.3f)
        val p4 = Offset(flagLeft, 3.dp.toPx() + flagH)

        val bluePath = Path().apply {
            moveTo(p0.x, p0.y)
            lineTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y)
            close()
        }

        // Draw Deep Nepal Blue Border
        drawPath(
            path = bluePath,
            color = Color(0xFF003893), // Official Deep Nepal Blue
            style = Fill
        )

        // Inner Crimson Red Field (Inset from blue boundary)
        val inset = 2.4.dp.toPx()
        val r0 = Offset(p0.x + inset * 0.6f, p0.y + inset)
        val r1 = Offset(p1.x - inset * 1.5f, p1.y - inset * 0.3f)
        val r2 = Offset(p2.x - inset * 0.5f, p2.y)
        val r3 = Offset(p3.x - inset * 1.5f, p3.y - inset * 0.3f)
        val r4 = Offset(p4.x + inset * 0.6f, p4.y - inset)

        val redPath = Path().apply {
            moveTo(r0.x, r0.y)
            lineTo(r1.x, r1.y)
            lineTo(r2.x, r2.y)
            lineTo(r3.x, r3.y)
            lineTo(r4.x, r4.y)
            close()
        }

        drawPath(
            path = redPath,
            color = Color(0xFFDC143C), // Crimson Red
            style = Fill
        )

        // White subtle shading ripple to enhance realistic fabric wave
        val rippleColor = Color.White.copy(alpha = 0.15f + 0.10f * sin(phase.toDouble()).toFloat().coerceIn(-0.1f, 0.1f))
        drawLine(
            color = rippleColor,
            start = Offset(flagLeft + flagW * 0.45f + waveTopTip * 0.5f, 4.dp.toPx()),
            end = Offset(flagLeft + flagW * 0.45f + waveBottomTip * 0.5f, 3.dp.toPx() + flagH),
            strokeWidth = 3.dp.toPx()
        )

        // 3. Upper Pennant Emblem: White Crescent Moon with 8 rays (चन्द्रमा)
        val moonX = flagLeft + flagW * 0.28f + waveTopTip * 0.25f
        val moonY = 3.dp.toPx() + flagH * 0.26f
        val moonRadius = 4.2.dp.toPx()

        drawCircle(
            color = Color.White,
            radius = moonRadius,
            center = Offset(moonX, moonY)
        )
        // Red circle overlay creating crescent arc
        drawCircle(
            color = Color(0xFFDC143C),
            radius = moonRadius * 0.78f,
            center = Offset(moonX, moonY - moonRadius * 0.42f)
        )

        // 4. Lower Pennant Emblem: White 12-pointed Sun (सूर्य)
        val sunX = flagLeft + flagW * 0.29f + waveBottomTip * 0.25f
        val sunY = 3.dp.toPx() + flagH * 0.73f
        val sunRadius = 4.8.dp.toPx()

        // Central sun disc
        drawCircle(
            color = Color.White,
            radius = sunRadius * 0.55f,
            center = Offset(sunX, sunY)
        )

        // 12 Sun rays
        for (i in 0 until 12) {
            val angle = (i * 30.0 * PI / 180.0)
            val rStart = sunRadius * 0.5f
            val rEnd = sunRadius
            val x1 = (sunX + cos(angle) * rStart).toFloat()
            val y1 = (sunY + sin(angle) * rStart).toFloat()
            val x2 = (sunX + cos(angle) * rEnd).toFloat()
            val y2 = (sunY + sin(angle) * rEnd).toFloat()

            drawLine(
                color = Color.White,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 1.3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
