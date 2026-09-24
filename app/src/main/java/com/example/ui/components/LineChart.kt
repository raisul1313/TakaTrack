package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.SpendingTrendPoint

@Composable
fun SpendingLineChart(
  points: List<SpendingTrendPoint>,
  currencySymbol: String = "৳",
  modifier: Modifier = Modifier,
  chartHeight: Dp = 150.dp,
  lineColor: Color = MaterialTheme.colorScheme.primary
) {
  if (points.size < 2) return

  val maxVal = remember(points) {
    val max = points.maxOfOrNull { it.amount } ?: 1.0
    if (max <= 0.0) 1.0 else max
  }

  val anim = remember { Animatable(0f) }
  LaunchedEffect(points) {
    anim.snapTo(0f)
    anim.animateTo(1f, animationSpec = tween(700))
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(chartHeight)
        .padding(horizontal = 8.dp)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val stepX = w / (points.size - 1)
        val progressVal = anim.value

        val strokePath = Path()
        val fillPath = Path()

        val coordinates = points.mapIndexed { idx, pt ->
          val norm = ((pt.amount / maxVal) * (h * 0.8f)).toFloat() * progressVal
          val x = idx * stepX
          val y = h - norm - (h * 0.1f)
          Offset(x, y)
        }

        strokePath.moveTo(coordinates[0].x, coordinates[0].y)
        fillPath.moveTo(coordinates[0].x, h)
        fillPath.lineTo(coordinates[0].x, coordinates[0].y)

        for (i in 0 until coordinates.size - 1) {
          val p0 = coordinates[i]
          val p1 = coordinates[i + 1]
          val cpx = (p0.x + p1.x) / 2
          strokePath.cubicTo(cpx, p0.y, cpx, p1.y, p1.x, p1.y)
          fillPath.cubicTo(cpx, p0.y, cpx, p1.y, p1.x, p1.y)
        }

        fillPath.lineTo(coordinates.last().x, h)
        fillPath.close()

        // Draw under-fill gradient
        drawPath(
          path = fillPath,
          brush = Brush.verticalGradient(
            colors = listOf(
              lineColor.copy(alpha = 0.35f),
              lineColor.copy(alpha = 0.0f)
            ),
            startY = 0f,
            endY = h
          )
        )

        // Draw line
        drawPath(
          path = strokePath,
          color = lineColor,
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw dot points
        coordinates.forEach { coord ->
          drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = coord
          )
          drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = coord
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Labels
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      points.forEach { pt ->
        Text(
          text = pt.label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
