package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.SpendingTrendPoint
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryLight

@Composable
fun SpendingBarChart(
  points: List<SpendingTrendPoint>,
  currencySymbol: String = "৳",
  modifier: Modifier = Modifier,
  chartHeight: Dp = 140.dp
) {
  if (points.isEmpty()) return

  val maxAmount = remember(points) {
    val max = points.maxOfOrNull { it.amount } ?: 1.0
    if (max <= 0.0) 1.0 else max
  }

  val progress = remember { Animatable(0f) }
  LaunchedEffect(points) {
    progress.snapTo(0f)
    progress.animateTo(1f, animationSpec = tween(600))
  }

  val barColor = MaterialTheme.colorScheme.primary
  val emptyTrackColor = MaterialTheme.colorScheme.surfaceVariant

  Column(modifier = modifier.fillMaxWidth()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(chartHeight)
        .padding(horizontal = 4.dp)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val totalBars = points.size
        val availableWidth = size.width
        val barSpacing = 12.dp.toPx()
        val barWidth = ((availableWidth - (barSpacing * (totalBars - 1))) / totalBars).coerceAtLeast(8.dp.toPx())
        val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

        points.forEachIndexed { index, pt ->
          val left = index * (barWidth + barSpacing)
          val barHeightNormalized = ((pt.amount / maxAmount) * size.height).toFloat() * progress.value
          val barHeight = barHeightNormalized.coerceAtLeast(4.dp.toPx())

          // Background track
          drawRoundRect(
            color = emptyTrackColor,
            topLeft = Offset(left, 0f),
            size = Size(barWidth, size.height),
            cornerRadius = cornerRadius
          )

          // Filled bar
          drawRoundRect(
            color = if (pt.amount > 0) barColor else Color.Transparent,
            topLeft = Offset(left, size.height - barHeight),
            size = Size(barWidth, barHeight),
            cornerRadius = cornerRadius
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Labels row
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

data class ComparisonBarItem(
  val label: String,
  val income: Double,
  val expense: Double
)

@Composable
fun IncomeExpenseBarChart(
  items: List<ComparisonBarItem>,
  currencySymbol: String = "৳",
  modifier: Modifier = Modifier,
  chartHeight: Dp = 150.dp
) {
  if (items.isEmpty()) return

  val maxVal = remember(items) {
    items.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0
  }

  val anim = remember { Animatable(0f) }
  LaunchedEffect(items) {
    anim.snapTo(0f)
    anim.animateTo(1f, animationSpec = tween(700))
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(chartHeight),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.Bottom
    ) {
      items.forEach { item ->
        val incomeHeightFraction = ((item.income / maxVal).toFloat() * anim.value).coerceIn(0.04f, 1f)
        val expenseHeightFraction = ((item.expense / maxVal).toFloat() * anim.value).coerceIn(0.04f, 1f)

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxHeight(),
          verticalArrangement = Arrangement.Bottom
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.weight(1f, fill = false)
          ) {
            // Income bar
            Box(
              modifier = Modifier
                .width(14.dp)
                .fillMaxHeight(incomeHeightFraction)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(IncomeGreen)
            )

            // Expense bar
            Box(
              modifier = Modifier
                .width(14.dp)
                .fillMaxHeight(expenseHeightFraction)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(ExpenseRed)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Legend
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .width(12.dp)
          .height(12.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(IncomeGreen)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Income",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.width(16.dp))

      Box(
        modifier = Modifier
          .width(12.dp)
          .height(12.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(ExpenseRed)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Expense",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
