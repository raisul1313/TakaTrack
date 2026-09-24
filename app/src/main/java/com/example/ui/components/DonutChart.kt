package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CategorySpending
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonutChart(
  categories: List<CategorySpending>,
  currencySymbol: String = "৳",
  modifier: Modifier = Modifier,
  chartSize: Dp = 190.dp,
  strokeWidth: Dp = 26.dp
) {
  if (categories.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(chartSize),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No spending data available",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    return
  }

  val totalAmount = remember(categories) { categories.sumOf { it.totalSpent } }
  var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull()) }

  val progress = remember { Animatable(0f) }
  LaunchedEffect(categories) {
    progress.snapTo(0f)
    progress.animateTo(1f, animationSpec = tween(durationMillis = 800))
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier.size(chartSize),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(chartSize)) {
        val strokePx = strokeWidth.toPx()
        val diameter = size.minDimension - strokePx
        val radius = diameter / 2f
        val topLeft = Offset(strokePx / 2f, strokePx / 2f)
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f
        val currentProgress = progress.value

        for (item in categories) {
          val sweepAngle = if (totalAmount > 0) {
            ((item.totalSpent / totalAmount) * 360f).toFloat() * currentProgress
          } else 0f

          val isSelected = selectedCategory?.categoryId == item.categoryId
          val color = parseColorHex(item.categoryColorHex)
          val actualStroke = if (isSelected) strokePx + 4.dp.toPx() else strokePx

          drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = (sweepAngle - 2f).coerceAtLeast(0.5f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = actualStroke, cap = StrokeCap.Round)
          )
          startAngle += sweepAngle
        }
      }

      // Center Information
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        val displayItem = selectedCategory ?: categories.first()
        Text(
          text = displayItem.categoryName,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
        Text(
          text = CurrencyFormatter.formatCompact(displayItem.totalSpent, currencySymbol),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = String.format(Locale.US, "%.1f%%", displayItem.percentage),
          style = MaterialTheme.typography.labelSmall,
          color = parseColorHex(displayItem.categoryColorHex),
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive Legend
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.take(6).forEach { cat ->
        val isSelected = selectedCategory?.categoryId == cat.categoryId
        val catColor = parseColorHex(cat.categoryColorHex)

        Surface(
          modifier = Modifier
            .clip(CircleShape)
            .clickable { selectedCategory = cat }
            .padding(horizontal = 4.dp, vertical = 2.dp),
          color = if (isSelected) catColor.copy(alpha = 0.15f) else Color.Transparent,
          shape = CircleShape
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(catColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = cat.categoryName,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}
