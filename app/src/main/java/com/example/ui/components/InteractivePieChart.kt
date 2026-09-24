package com.example.ui.components

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CategorySpending
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class ChartStyle {
  PIE,
  DONUT
}

/**
 * Recharts & D3-inspired Interactive Pie and Donut Chart for Android Jetpack Compose.
 *
 * Features:
 * - D3/Recharts style slice-popout (active shape radial displacement) upon selection
 * - Smooth entrance and transition animations using Compose Animatable
 * - Interactive touch gesture detection: tap any slice directly to inspect
 * - Toggleable between solid Pie Chart and Donut Chart
 * - Center data callout with category name, formatted amount, and percentage share
 * - Interactive legend with swatches, values, and direct touch selection
 * - Slice separation stroke lines mirroring Recharts styling
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractivePieChart(
  categories: List<CategorySpending>,
  currencySymbol: String,
  modifier: Modifier = Modifier,
  selectedCategory: CategorySpending? = null,
  onCategorySelected: (CategorySpending?) -> Unit = {},
  chartSize: Dp = 230.dp,
  chartStyle: ChartStyle = ChartStyle.DONUT,
  showLegend: Boolean = true,
  allowTouchSelection: Boolean = true
) {
  if (categories.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(chartSize),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No spending data available for this period",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    return
  }

  val totalAmount = remember(categories) { categories.sumOf { it.totalSpent } }
  val activeCategory = selectedCategory ?: categories.firstOrNull()

  // Entrance animation for sweep angles
  val animationProgress = remember { Animatable(0f) }
  LaunchedEffect(categories) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
    )
  }

  val surfaceColor = MaterialTheme.colorScheme.surface

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Canvas Container
    Box(
      modifier = Modifier
        .size(chartSize)
        .testTag("interactive_pie_chart_box"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(
        modifier = Modifier
          .size(chartSize)
          .pointerInput(categories, chartStyle, allowTouchSelection) {
            if (!allowTouchSelection) return@pointerInput
            detectTapGestures { tapOffset ->
              val centerX = size.width / 2f
              val centerY = size.height / 2f
              val dx = tapOffset.x - centerX
              val dy = tapOffset.y - centerY
              val distance = sqrt(dx * dx + dy * dy)
              val minDim = minOf(size.width, size.height).toFloat()
              val outerRadius = (minDim / 2f) - 10f
              val innerRadius = if (chartStyle == ChartStyle.DONUT) outerRadius * 0.58f else 0f

              if (distance in innerRadius..outerRadius + 20f) {
                // Angle in radians converted to degrees [-180, 180]
                var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                // Normalize so -90 deg (12 o'clock) is 0 deg [0, 360)
                angleDeg = (angleDeg + 90f + 360f) % 360f

                var accumulatedAngle = 0f
                var tappedCategory: CategorySpending? = null

                for (cat in categories) {
                  val sweep = if (totalAmount > 0) {
                    ((cat.totalSpent / totalAmount) * 360f).toFloat()
                  } else 0f

                  if (angleDeg >= accumulatedAngle && angleDeg < accumulatedAngle + sweep) {
                    tappedCategory = cat
                    break
                  }
                  accumulatedAngle += sweep
                }

                if (tappedCategory != null) {
                  if (activeCategory?.categoryId == tappedCategory.categoryId) {
                    onCategorySelected(null) // toggle deselect
                  } else {
                    onCategorySelected(tappedCategory)
                  }
                }
              } else if (distance < innerRadius && chartStyle == ChartStyle.DONUT) {
                // Tapped inside donut hole -> reset selection
                onCategorySelected(null)
              }
            }
          }
      ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val chartRadius = (size.minDimension / 2f) - 14.dp.toPx()
        val innerHoleRadius = if (chartStyle == ChartStyle.DONUT) chartRadius * 0.60f else 0f

        var currentStartAngle = -90f
        val currentProgress = animationProgress.value

        for (item in categories) {
          val sweepAngle = if (totalAmount > 0) {
            ((item.totalSpent / totalAmount) * 360f).toFloat() * currentProgress
          } else 0f

          if (sweepAngle <= 0.05f) continue

          val isSelected = activeCategory?.categoryId == item.categoryId
          val catColor = parseColorHex(item.categoryColorHex)

          // D3/Recharts active shape displacement:
          // Slices translate outwards along their mid-angle bisector
          val midAngleDeg = currentStartAngle + (sweepAngle / 2f)
          val midAngleRad = Math.toRadians(midAngleDeg.toDouble())
          val popoutDistance = if (isSelected) 10.dp.toPx() else 0f

          val sliceCenterX = centerX + (cos(midAngleRad) * popoutDistance).toFloat()
          val sliceCenterY = centerY + (sin(midAngleRad) * popoutDistance).toFloat()

          val sliceRadius = if (isSelected) chartRadius + 4.dp.toPx() else chartRadius

          if (chartStyle == ChartStyle.PIE) {
            // Solid Pie Slice
            val path = Path().apply {
              moveTo(sliceCenterX, sliceCenterY)
              arcTo(
                rect = Rect(
                  left = sliceCenterX - sliceRadius,
                  top = sliceCenterY - sliceRadius,
                  right = sliceCenterX + sliceRadius,
                  bottom = sliceCenterY + sliceRadius
                ),
                startAngleDegrees = currentStartAngle,
                sweepAngleDegrees = sweepAngle,
                forceMoveTo = false
              )
              close()
            }

            // Fill slice
            drawPath(path = path, color = catColor, style = Fill)

            // D3/Recharts slice boundary gap
            drawPath(
              path = path,
              color = surfaceColor,
              style = Stroke(width = 2.dp.toPx())
            )
          } else {
            // Donut Slice with inner cut
            val sliceInnerRadius = if (isSelected) innerHoleRadius - 2.dp.toPx() else innerHoleRadius
            val strokeThickness = sliceRadius - sliceInnerRadius
            val ringMidRadius = sliceInnerRadius + (strokeThickness / 2f)

            drawArc(
              color = catColor,
              startAngle = currentStartAngle,
              sweepAngle = (sweepAngle - 1.5f).coerceAtLeast(0.5f),
              useCenter = false,
              topLeft = Offset(sliceCenterX - ringMidRadius, sliceCenterY - ringMidRadius),
              size = Size(ringMidRadius * 2f, ringMidRadius * 2f),
              style = Stroke(
                width = strokeThickness,
                cap = StrokeCap.Round
              )
            )
          }

          currentStartAngle += sweepAngle
        }
      }

      // Center Information for Donut Style
      if (chartStyle == ChartStyle.DONUT && activeCategory != null) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(20.dp)
        ) {
          Text(
            text = activeCategory.categoryName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = CurrencyFormatter.formatCompact(activeCategory.totalSpent, currencySymbol),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Surface(
            shape = CircleShape,
            color = parseColorHex(activeCategory.categoryColorHex).copy(alpha = 0.16f)
          ) {
            Text(
              text = String.format(Locale.US, "%.1f%%", activeCategory.percentage),
              style = MaterialTheme.typography.labelSmall,
              color = parseColorHex(activeCategory.categoryColorHex),
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }
      }
    }

    // Callout card if in solid PIE mode
    if (chartStyle == ChartStyle.PIE && activeCategory != null) {
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = parseColorHex(activeCategory.categoryColorHex).copy(alpha = 0.12f),
        modifier = Modifier.testTag("pie_active_slice_callout")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(parseColorHex(activeCategory.categoryColorHex))
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = activeCategory.categoryName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = CurrencyFormatter.format(activeCategory.totalSpent, currencySymbol),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = parseColorHex(activeCategory.categoryColorHex)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "(${String.format(Locale.US, "%.1f%%", activeCategory.percentage)})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Interactive Legend
    if (showLegend) {
      Spacer(modifier = Modifier.height(16.dp))

      FlowRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { cat ->
          val isSelected = activeCategory?.categoryId == cat.categoryId
          val catColor = parseColorHex(cat.categoryColorHex)

          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable {
                if (isSelected) {
                  onCategorySelected(null)
                } else {
                  onCategorySelected(cat)
                }
              }
              .testTag("legend_item_${cat.categoryId}"),
            color = if (isSelected) catColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            shape = RoundedCornerShape(20.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(catColor)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = cat.categoryName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${String.format(Locale.US, "%.1f", cat.percentage)}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}
