package com.ossama.qrvision.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun QrLogo(modifier: Modifier = Modifier, size: Dp = 56.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(Brush.linearGradient(listOf(Color(0xFF12372C), Color(0xFF07110D))))
    ) {
        Canvas(Modifier.size(size)) {
            val unit = this.size.minDimension / 108f
            val stroke = 6.5f * unit
            val corner = 28f * unit
            val inset = 18f * unit
            val points = listOf(
                Offset(inset, inset),
                Offset(this.size.width - inset - corner, inset),
                Offset(inset, this.size.height - inset - corner)
            )
            points.forEach { p ->
                drawRoundRect(
                    color = Color(0xFF00DCA4),
                    topLeft = p,
                    size = Size(corner, corner),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * unit),
                    style = Stroke(width = stroke)
                )
                drawRoundRect(
                    color = Color(0xFF00DCA4),
                    topLeft = Offset(p.x + 8f * unit, p.y + 8f * unit),
                    size = Size(corner - 16f * unit, corner - 16f * unit),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * unit)
                )
            }
            val x = this.size.width
            val y = this.size.height
            val module = 8.2f * unit
            listOf(
                Offset(x * 0.55f, y * 0.52f), Offset(x * 0.70f, y * 0.52f),
                Offset(x * 0.55f, y * 0.68f), Offset(x * 0.70f, y * 0.68f),
                Offset(x * 0.82f, y * 0.80f)
            ).forEach { p ->
                drawRoundRect(Color(0xFF00DCA4), topLeft = p, size = Size(module, module), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f * unit))
            }
        }
    }
}
