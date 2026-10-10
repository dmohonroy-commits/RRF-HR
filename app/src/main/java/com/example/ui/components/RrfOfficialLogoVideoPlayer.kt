package com.example.ui.components

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * RrfOfficialLogoVideoPlayer
 *
 * Renders the official Rural Reconstruction Foundation (RRF) 3D emblem logo
 * with 100% transparent background (dark background completely removed),
 * zero cropping, and authentic brand fidelity matching the video:
 * - 3D white bevelled coin with metallic rim
 * - Central official RRF green thatched cottage/house symbol
 * - Twin green laurel / wheat wreath branches
 * - Inner green boundary ring
 * - Outer navy boundary ring
 * - Circular curved text "RURAL RECONSTRUCTION FOUNDATION" along upper arc
 * - Green star ★ at bottom center
 * - Continuous glossy 3D specular shine sweep across the emblem surface
 */
@Composable
fun RrfOfficialLogoVideoPlayer(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    alwaysFormed: Boolean = true
) {
    // Continuous infinite sheen shine transition every 2.8 seconds
    val infiniteTransition = rememberInfiniteTransition(label = "rrf_logo_3d_sheen")
    val shineProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine_pos"
    )

    // Smooth 3D floating perspective tilt matching video
    val tiltAngle by infiniteTransition.animateFloat(
        initialValue = -9f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt_angle"
    )

    // Subtle 3D floating elevation bob
    val floatElevation by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_elev"
    )

    Box(
        modifier = modifier
            .size(size)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = tiltAngle
                    translationY = floatElevation * density
                    cameraDistance = 16f * density
                }
        ) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val radius = (minOf(canvasWidth, canvasHeight) / 2f) * 0.94f

            if (radius <= 0f) return@Canvas

            // =========================================================
            // 1. DROP SHADOW & 3D BEVELLED WHITE COIN (TRANSPARENT BG)
            // =========================================================
            // Soft drop shadow
            drawCircle(
                color = Color(0x66000000),
                radius = radius * 0.98f,
                center = Offset(center.x + radius * 0.04f, center.y + radius * 0.06f)
            )

            // Outer metallic bevel ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF1F5F9),
                        Color(0xFFCBD5E1),
                        Color(0xFF94A3B8)
                    ),
                    center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                    radius = radius * 1.15f
                ),
                radius = radius,
                center = center
            )

            // Inner coin face (White porcelain coin disc)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF8FAFC),
                        Color(0xFFEEF2F6)
                    ),
                    center = Offset(center.x - radius * 0.15f, center.y - radius * 0.15f),
                    radius = radius * 0.95f
                ),
                radius = radius * 0.94f,
                center = center
            )

            // Outer Navy Ring Line
            val navyColor = Color(0xFF1B365D) // Official RRF Navy Blue
            drawCircle(
                color = navyColor,
                radius = radius * 0.92f,
                center = center,
                style = Stroke(width = radius * 0.022f)
            )

            // Inner Green Ring Line
            val rrfGreen = Color(0xFF15803D) // Official RRF Green
            drawCircle(
                color = rrfGreen,
                radius = radius * 0.67f,
                center = center,
                style = Stroke(width = radius * 0.026f)
            )

            // =========================================================
            // 2. CENTRAL GREEN RRF COTTAGE / HOUSE SYMBOL
            // =========================================================
            val hWidth = radius * 0.44f
            val hHeight = radius * 0.40f
            val hTopY = center.y - radius * 0.22f
            val hBottomY = hTopY + hHeight

            // Curved Thatched Roof
            val roofPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, hTopY)
                cubicTo(
                    center.x - hWidth * 0.35f, hTopY + hHeight * 0.12f,
                    center.x - hWidth * 0.52f, hTopY + hHeight * 0.42f,
                    center.x - hWidth * 0.58f, hTopY + hHeight * 0.52f
                )
                lineTo(center.x + hWidth * 0.58f, hTopY + hHeight * 0.52f)
                cubicTo(
                    center.x + hWidth * 0.52f, hTopY + hHeight * 0.42f,
                    center.x + hWidth * 0.35f, hTopY + hHeight * 0.12f,
                    center.x, hTopY
                )
                close()
            }
            drawPath(roofPath, color = rrfGreen)

            // Eaves extension rim
            val eavePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x - hWidth * 0.64f, hTopY + hHeight * 0.52f)
                quadraticTo(
                    center.x, hTopY + hHeight * 0.42f,
                    center.x + hWidth * 0.64f, hTopY + hHeight * 0.52f
                )
                lineTo(center.x + hWidth * 0.54f, hTopY + hHeight * 0.59f)
                quadraticTo(
                    center.x, hTopY + hHeight * 0.49f,
                    center.x - hWidth * 0.54f, hTopY + hHeight * 0.59f
                )
                close()
            }
            drawPath(eavePath, color = rrfGreen)

            // Wall outline
            val wallPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x - hWidth * 0.44f, hTopY + hHeight * 0.56f)
                lineTo(center.x - hWidth * 0.44f, hBottomY)
                lineTo(center.x + hWidth * 0.44f, hBottomY)
                lineTo(center.x + hWidth * 0.44f, hTopY + hHeight * 0.56f)
                close()
            }
            drawPath(wallPath, color = rrfGreen, style = Stroke(width = radius * 0.042f))

            // Door arch cutout
            val doorPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x - hWidth * 0.15f, hBottomY)
                lineTo(center.x - hWidth * 0.15f, hBottomY - hHeight * 0.28f)
                quadraticTo(
                    center.x, hBottomY - hHeight * 0.42f,
                    center.x + hWidth * 0.15f, hBottomY - hHeight * 0.28f
                )
                lineTo(center.x + hWidth * 0.15f, hBottomY)
                close()
            }
            drawPath(doorPath, color = rrfGreen)

            // =========================================================
            // 3. TWIN LAUREL / WHEAT WREATH BRANCHES
            // =========================================================
            val leafCount = 8
            for (i in 0 until leafCount) {
                // Left wreath branch
                val leftAngleDeg = 115f - i * 15f
                val leftAngleRad = Math.toRadians(leftAngleDeg.toDouble()).toFloat()
                val leftLeafR = radius * 0.53f
                val lx = center.x + leftLeafR * cos(leftAngleRad)
                val ly = center.y + leftLeafR * sin(leftAngleRad)

                drawCircle(
                    color = rrfGreen,
                    radius = radius * (0.052f - i * 0.003f).coerceAtLeast(0.024f),
                    center = Offset(lx, ly)
                )

                // Right wreath branch
                val rightAngleDeg = 65f + i * 15f
                val rightAngleRad = Math.toRadians(rightAngleDeg.toDouble()).toFloat()
                val rightLeafR = radius * 0.53f
                val rx = center.x + rightLeafR * cos(rightAngleRad)
                val ry = center.y + rightLeafR * sin(rightAngleRad)

                drawCircle(
                    color = rrfGreen,
                    radius = radius * (0.052f - i * 0.003f).coerceAtLeast(0.024f),
                    center = Offset(rx, ry)
                )
            }

            // =========================================================
            // 4. CURVED TEXT: "RURAL RECONSTRUCTION FOUNDATION"
            // =========================================================
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas

                val textPath = Path()
                val textRadius = radius * 0.795f
                val textOval = RectF(
                    center.x - textRadius,
                    center.y - textRadius,
                    center.x + textRadius,
                    center.y + textRadius
                )
                val sweep = 220f
                textPath.addArc(textOval, 160f, sweep)

                val paint = Paint().apply {
                    color = android.graphics.Color.parseColor("#1B365D")
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textSize = radius * 0.138f
                    isAntiAlias = true
                    letterSpacing = 0.035f
                }

                val titleText = "RURAL RECONSTRUCTION FOUNDATION"
                val textLen = paint.measureText(titleText)
                val arcLength = (2 * Math.PI.toFloat() * textRadius) * (sweep / 360f)
                val hOffset = ((arcLength - textLen) / 2f).coerceAtLeast(0f)

                nativeCanvas.drawTextOnPath(titleText, textPath, hOffset, 0f, paint)
            }

            // =========================================================
            // 5. GREEN STAR ★ AT BOTTOM CENTER
            // =========================================================
            val starRadius = radius * 0.082f
            val starCenter = Offset(center.x, center.y + radius * 0.80f)

            val starPath = androidx.compose.ui.graphics.Path().apply {
                for (i in 0 until 5) {
                    val outerAngle = Math.toRadians((-90 + i * 72).toDouble())
                    val innerAngle = Math.toRadians((-90 + i * 72 + 36).toDouble())

                    val ox = starCenter.x + starRadius * cos(outerAngle).toFloat()
                    val oy = starCenter.y + starRadius * sin(outerAngle).toFloat()
                    val ix = starCenter.x + (starRadius * 0.42f) * cos(innerAngle).toFloat()
                    val iy = starCenter.y + (starRadius * 0.42f) * sin(innerAngle).toFloat()

                    if (i == 0) moveTo(ox, oy) else lineTo(ox, oy)
                    lineTo(ix, iy)
                }
                close()
            }
            drawPath(starPath, color = rrfGreen)

            // =========================================================
            // 6. 3D GLOSSY SPECULAR SHINE SWEEP ACROSS COIN SURFACE
            // =========================================================
            val shineBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.65f),
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                start = Offset(canvasWidth * (shineProgress - 0.35f), 0f),
                end = Offset(canvasWidth * (shineProgress + 0.35f), canvasHeight)
            )

            drawCircle(
                brush = shineBrush,
                radius = radius * 0.94f,
                center = center
            )
        }
    }
}
