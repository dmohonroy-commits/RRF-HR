package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dark Navy Denim Ambient Background with subtle wavy dark blue contours
 * matching the skeuomorphic denim 3D design.
 */
@Composable
fun DenimAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Base deep midnight navy gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF081424),
                            Color(0xFF0D1E35),
                            Color(0xFF091628),
                            Color(0xFF060F1C)
                        )
                    )
                )

                // Subtle dark curved wave overlays
                val wavePath1 = Path().apply {
                    moveTo(0f, size.height * 0.15f)
                    cubicTo(
                        size.width * 0.4f, size.height * 0.08f,
                        size.width * 0.7f, size.height * 0.22f,
                        size.width, size.height * 0.12f
                    )
                    lineTo(size.width, 0f)
                    lineTo(0f, 0f)
                    close()
                }
                drawPath(
                    path = wavePath1,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF132A4A).copy(alpha = 0.45f),
                            Color(0xFF0D1E35).copy(alpha = 0.10f)
                        )
                    )
                )

                val wavePath2 = Path().apply {
                    moveTo(0f, size.height * 0.55f)
                    cubicTo(
                        size.width * 0.35f, size.height * 0.48f,
                        size.width * 0.65f, size.height * 0.62f,
                        size.width, size.height * 0.52f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = wavePath2,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0E223D).copy(alpha = 0.50f),
                            Color(0xFF163258).copy(alpha = 0.25f)
                        )
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * Denim Stitched Card with authentic blue jeans twill texture
 * and golden/amber double dashed stitching.
 */
@Composable
fun DenimStitchedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerShape: RoundedCornerShape = RoundedCornerShape(22.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = cornerShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3B60)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = cornerShape,
                spotColor = Color(0x99000000),
                ambientColor = Color(0x66000000)
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val w = size.width
                    val h = size.height

                    // 1. Denim Fabric Base Gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF264C7A), // Top slightly lighter denim
                                Color(0xFF1F416A),
                                Color(0xFF183556)  // Bottom darker indigo
                            )
                        )
                    )

                    // 2. Realistic Denim Twill Weave Lines (Diagonal 45-degree texture)
                    val spacing = 7.dp.toPx()
                    val strokeW = 1.6f
                    val diagonalLimit = w + h
                    var offset = -h
                    while (offset < diagonalLimit) {
                        drawLine(
                            color = Color(0xFF3B679B).copy(alpha = 0.28f),
                            start = Offset(offset, 0f),
                            end = Offset(offset + h, h),
                            strokeWidth = strokeW
                        )
                        drawLine(
                            color = Color(0xFF0E2138).copy(alpha = 0.35f),
                            start = Offset(offset + spacing * 0.5f, 0f),
                            end = Offset(offset + spacing * 0.5f + h, h),
                            strokeWidth = strokeW * 0.8f
                        )
                        offset += spacing
                    }

                    // 3. Subtle Card Edge Bevel Highlight (Top/Left light, Bottom/Right shadow)
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6B9AC9).copy(alpha = 0.35f),
                                Color(0xFF0B1A2C).copy(alpha = 0.60f)
                            )
                        ),
                        cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )


                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}

/**
 * 3D Embossed Convex Button Orb
 * Available in:
 * - Coral/Orange (e.g. Card 1 & Card 3)
 * - Cyan/Sky Blue (e.g. Card 2 & Card 4)
 * - Dark Denim Inset (e.g. Banner card, Helpline, Menu click)
 */
@Composable
fun Denim3DButtonOrb(
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    themeColor: DenimButtonColor = DenimButtonColor.Cyan,
    content: @Composable BoxScope.() -> Unit
) {
    val gradientColors = when (themeColor) {
        DenimButtonColor.Orange -> listOf(
            Color(0xFFFF8A50),
            Color(0xFFF97316),
            Color(0xFFEA580C),
            Color(0xFFC2410C)
        )
        DenimButtonColor.Cyan -> listOf(
            Color(0xFF38BDF8),
            Color(0xFF0EA5E9),
            Color(0xFF0284C7),
            Color(0xFF0369A1)
        )
        DenimButtonColor.DarkInset -> listOf(
            Color(0xFF1B3556),
            Color(0xFF132842),
            Color(0xFF0D1C30),
            Color(0xFF081322)
        )
    }

    val bevelBorderColors = when (themeColor) {
        DenimButtonColor.Orange -> listOf(
            Color(0xFFFFC099),
            Color(0xFF9A3412)
        )
        DenimButtonColor.Cyan -> listOf(
            Color(0xFFBAE6FD),
            Color(0xFF075985)
        )
        DenimButtonColor.DarkInset -> listOf(
            Color(0xFF385E8E).copy(alpha = 0.5f),
            Color(0xFF060F1A)
        )
    }

    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        shadowElevation = 8.dp,
        modifier = modifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = gradientColors,
                        center = Offset(size.value * 0.35f, size.value * 0.35f),
                        radius = size.value * 1.1f
                    )
                )
                .drawBehind {
                    // Outer Bevel Edge
                    drawCircle(
                        brush = Brush.verticalGradient(bevelBorderColors),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Specular highlight spot (Top-Left)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (themeColor == DenimButtonColor.DarkInset) 0.35f else 0.70f),
                                Color.Transparent
                            ),
                            center = Offset(size.toPx() * 0.32f, size.toPx() * 0.30f),
                            radius = size.toPx() * 0.35f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

enum class DenimButtonColor {
    Orange,
    Cyan,
    DarkInset
}

/**
 * Convenience helper to render Denim3DButtonOrb with an icon.
 */
@Composable
fun Denim3DIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    iconSize: Dp = 26.dp,
    iconTint: Color = Color.White,
    themeColor: DenimButtonColor = DenimButtonColor.Cyan
) {
    Denim3DButtonOrb(
        modifier = modifier,
        size = size,
        themeColor = themeColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * 3D PDF Badge inside Denim3DButtonOrb.
 */
@Composable
fun Denim3DPdfButton(
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    themeColor: DenimButtonColor = DenimButtonColor.Cyan
) {
    Denim3DButtonOrb(
        modifier = modifier,
        size = size,
        themeColor = themeColor
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 2.dp,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = "PDF",
                fontSize = (size.value * 0.22f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0284C7),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
