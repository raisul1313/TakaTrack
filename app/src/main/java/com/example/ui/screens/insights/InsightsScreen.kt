package com.example.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionType
import com.example.domain.model.CategorySpending
import com.example.domain.model.SpendingTrendPoint
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.ComparisonBarItem
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.DonutChart
import com.example.ui.components.FintechCard
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.SpendingLineChart
import com.example.ui.components.parseColorHex
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier
) {
  val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val categories by viewModel.allCategories.collectAsStateWithLifecycle()
  val summary by viewModel.financialSummary.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  var selectedPeriod by remember { mutableStateOf("Month") }

  // Filtered transactions for the selected period
  val periodTransactions = remember(transactions, selectedPeriod) {
    val cal = Calendar.getInstance()
    when (selectedPeriod) {
      "Week" -> {
        cal.add(Calendar.DAY_OF_YEAR, -7)
        transactions.filter { it.date >= cal.timeInMillis }
      }
      "Month" -> {
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        transactions.filter { it.date >= cal.timeInMillis }
      }
      else -> {
        // Year
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        transactions.filter { it.date >= cal.timeInMillis }
      }
    }
  }

  // Category breakdown for donut chart
  val categorySpendingList = remember(periodTransactions, categories) {
    val expenseTxs = periodTransactions.filter { it.type == TransactionType.EXPENSE }
    val totalExpense = expenseTxs.sumOf { it.amount }
    val catMap = categories.associateBy { it.id }

    expenseTxs.groupBy { it.categoryId }
      .map { (catId, txs) ->
        val spent = txs.sumOf { it.amount }
        val cat = catMap[catId]
        val pct = if (totalExpense > 0) (spent / totalExpense) * 100.0 else 0.0
        CategorySpending(
          categoryId = catId,
          categoryName = cat?.name ?: "Other",
          categoryIcon = cat?.iconName ?: "receipt",
          categoryColorHex = cat?.colorHex ?: "#64748B",
          totalSpent = spent,
          percentage = pct
        )
      }
      .sortedByDescending { it.totalSpent }
  }

  // Monthly comparison items (Income vs Expense)
  val comparisonItems = remember(transactions) {
    val list = mutableListOf<ComparisonBarItem>()
    val sdf = SimpleDateFormat("MMM", Locale.US)
    for (m in 3 downTo 0) {
      val cal = Calendar.getInstance()
      cal.add(Calendar.MONTH, -m)
      cal.set(Calendar.DAY_OF_MONTH, 1)
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      val start = cal.timeInMillis
      cal.add(Calendar.MONTH, 1)
      val end = cal.timeInMillis - 1

      val monthTxs = transactions.filter { it.date in start..end }
      val inc = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
      val exp = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
      list.add(ComparisonBarItem(label = sdf.format(Date(start)), income = inc, expense = exp))
    }
    list
  }

  // Trend line points
  val trendPoints = remember(periodTransactions, selectedPeriod) {
    calculateTrendPoints(periodTransactions, selectedPeriod)
  }

  // Financial Health Metrics
  val totalSpentInPeriod = remember(periodTransactions) {
    periodTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
  }
  val totalIncomeInPeriod = remember(periodTransactions) {
    periodTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
  }

  val avgDailySpend = remember(totalSpentInPeriod, selectedPeriod) {
    val days = when (selectedPeriod) {
      "Week" -> 7
      "Month" -> Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
      else -> Calendar.getInstance().get(Calendar.DAY_OF_YEAR).coerceAtLeast(1)
    }
    totalSpentInPeriod / days
  }

  val highestSpendingDay = remember(periodTransactions) {
    val expenseTxs = periodTransactions.filter { it.type == TransactionType.EXPENSE }
    val sdf = SimpleDateFormat("MMM d", Locale.US)
    val dayMap = expenseTxs.groupBy { sdf.format(Date(it.date)) }
    dayMap.maxByOrNull { entry -> entry.value.sumOf { it.amount } }?.key ?: "N/A"
  }

  val topExpenseCategoryName = remember(categorySpendingList) {
    categorySpendingList.firstOrNull()?.categoryName ?: "N/A"
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Title & Period Switcher
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Analytics & Insights",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Understand your spending habits",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Period switcher [Week | Month | Year]
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp)
        ) {
          listOf("Week", "Month", "Year").forEach { p ->
            val isSelected = selectedPeriod == p
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedPeriod = p }
            ) {
              Text(
                text = p,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    }

    // Savings Rate Banner
    item {
      FintechCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Savings,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = "${String.format(Locale.US, "%.1f", summary.savingsRatePercent)}% Savings Rate",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (summary.savingsRatePercent >= 50.0) {
                "Outstanding financial discipline! You are saving most of your income."
              } else if (summary.savingsRatePercent >= 20.0) {
                "Healthy balance between spending and savings this period."
              } else {
                "Consider setting tighter category limits to increase your monthly savings."
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Spending by Category Donut Chart
    item {
      FintechCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          Text(
            text = "Spending by Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Total: ${CurrencyFormatter.format(totalSpentInPeriod, currencySymbol)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          DonutChart(
            categories = categorySpendingList,
            currencySymbol = currencySymbol
          )
        }
      }
    }

    // Quick Stats Grid
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricTile(
          icon = Icons.Default.TrendingDown,
          title = "Daily Average",
          value = CurrencyFormatter.format(avgDailySpend, currencySymbol),
          modifier = Modifier.weight(1f)
        )
        MetricTile(
          icon = Icons.Default.CalendarMonth,
          title = "Top Spend Day",
          value = highestSpendingDay,
          modifier = Modifier.weight(1f)
        )
        MetricTile(
          icon = Icons.Default.Insights,
          title = "Top Category",
          value = topExpenseCategoryName,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Income vs Expense Comparison Bar Chart
    item {
      FintechCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          Text(
            text = "Cash Flow Comparison",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Monthly income vs expense breakdown",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          IncomeExpenseBarChart(
            items = comparisonItems,
            currencySymbol = currencySymbol
          )
        }
      }
    }

    // Spending Trend Line Chart
    if (trendPoints.size >= 2) {
      item {
        FintechCard(modifier = Modifier.fillMaxWidth()) {
          Column {
            Text(
              text = "Spending Trend Curve",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Day-to-day spending trajectory",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            SpendingLineChart(
              points = trendPoints,
              currencySymbol = currencySymbol
            )
          }
        }
      }
    }

    // Ranked Categories Breakdown List
    item {
      Text(
        text = "Category Breakdown",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    items(categorySpendingList, key = { it.categoryId }) { cat ->
      val catColor = parseColorHex(cat.categoryColorHex)

      FintechCard(modifier = Modifier.fillMaxWidth(), elevation = 0.5.dp) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              CategoryIconBadge(
                iconName = cat.categoryIcon,
                colorHex = cat.categoryColorHex,
                size = 36.dp,
                iconSize = 18.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = cat.categoryName,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${String.format(Locale.US, "%.1f", cat.percentage)}% of total",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Text(
              text = CurrencyFormatter.format(cat.totalSpent, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          LinearProgressIndicator(
            progress = { (cat.percentage / 100f).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(CircleShape),
            color = catColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
          )
        }
      }
    }
  }
}

@Composable
private fun MetricTile(
  icon: ImageVector,
  title: String,
  value: String,
  modifier: Modifier = Modifier
) {
  FintechCard(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    elevation = 0.5.dp
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.Start
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1
      )
    }
  }
}

private fun calculateTrendPoints(
  transactions: List<com.example.domain.model.TransactionItem>,
  period: String
): List<SpendingTrendPoint> {
  val expenseTxs = transactions.filter { it.type == TransactionType.EXPENSE }
  val sdfDay = SimpleDateFormat("EEE", Locale.US)
  val sdfMonth = SimpleDateFormat("MMM", Locale.US)

  return when (period) {
    "Week" -> {
      val points = mutableListOf<SpendingTrendPoint>()
      for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val end = cal.timeInMillis - 1

        val dayTotal = expenseTxs.filter { it.date in start..end }.sumOf { it.amount }
        val label = if (i == 0) "Today" else sdfDay.format(Date(start))
        points.add(SpendingTrendPoint(label = label, amount = dayTotal, timestamp = start))
      }
      points
    }
    "Month" -> {
      val points = mutableListOf<SpendingTrendPoint>()
      for (w in 3 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, -w)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val end = cal.timeInMillis - 1

        val weekTotal = expenseTxs.filter { it.date in start..end }.sumOf { it.amount }
        points.add(SpendingTrendPoint(label = "W${4 - w}", amount = weekTotal, timestamp = start))
      }
      points
    }
    else -> {
      val points = mutableListOf<SpendingTrendPoint>()
      for (m in 5 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -m)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1

        val monthTotal = expenseTxs.filter { it.date in start..end }.sumOf { it.amount }
        points.add(SpendingTrendPoint(label = sdfMonth.format(Date(start)), amount = monthTotal, timestamp = start))
      }
      points
    }
  }
}
