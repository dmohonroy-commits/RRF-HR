package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PastelAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE), // Soft Sky Blue top
                        Color(0xFFE6F4FA), // Light Pastel Cyan
                        Color(0xFFD1FAE5), // Soft Mint Green middle/bottom
                        Color(0xFFE0F7FA)  // Soft Aqua
                    )
                )
            )
    ) {
        // Floating Top-Right Soft Mint Orb
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF86EFAC).copy(alpha = 0.45f),
                            Color(0xFFA7F3D0).copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Floating Center-Left Soft Cyan Orb
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-80).dp, y = (-20).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7DD3FC).copy(alpha = 0.50f),
                            Color(0xFFBAE6FD).copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Floating Bottom-Right Mint/Teal Orb
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6EE7B7).copy(alpha = 0.40f),
                            Color(0xFF99F6E4).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

@Composable
fun GlassOrb3DContent(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    orbBaseColor: Color = Color.White.copy(alpha = 0.70f),
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        shadowElevation = 6.dp,
        modifier = modifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.98f),
                            orbBaseColor,
                            Color(0xFFCBD5E1).copy(alpha = 0.50f)
                        ),
                        center = Offset(size.value * 0.35f, size.value * 0.35f)
                    )
                )
                .border(
                    width = 1.6.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.85f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Specular Reflection Highlight Spot (Top-Left)
            Box(
                modifier = Modifier
                    .size(size * 0.32f)
                    .align(Alignment.TopStart)
                    .offset(x = size * 0.16f, y = size * 0.16f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.0f)
                            )
                        )
                    )
            )

            content()
        }
    }
}

@Composable
fun GlassOrb3DIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    iconSize: Dp = 26.dp,
    iconTint: Color = Color(0xFF0F3A5D),
    orbBaseColor: Color = Color.White.copy(alpha = 0.70f)
) {
    GlassOrb3DContent(
        modifier = modifier,
        size = size,
        orbBaseColor = orbBaseColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun GlassOrb3DPdfIcon(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    orbBaseColor: Color = Color.White.copy(alpha = 0.70f)
) {
    GlassOrb3DContent(
        modifier = modifier,
        size = size,
        orbBaseColor = orbBaseColor
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF0284C7),
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = "PDF",
                fontSize = (size.value * 0.22f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerShape: RoundedCornerShape = RoundedCornerShape(22.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = cornerShape,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.72f)),
        border = BorderStroke(
            1.8.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.80f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.82f),
                            Color.White.copy(alpha = 0.42f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}
