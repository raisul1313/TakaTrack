package com.example.ui.screens.distribution

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionType
import com.example.domain.model.CategorySpending
import com.example.domain.model.TransactionItem
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.ChartStyle
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.EmptyState
import com.example.ui.components.FintechCard
import com.example.ui.components.InteractivePieChart
import com.example.ui.components.parseColorHex
import com.example.ui.theme.ExpenseRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DistributionPeriod(val label: String) {
  THIS_MONTH("This Month"),
  LAST_MONTH("Last Month"),
  LAST_3_MONTHS("Last 3 Months"),
  THIS_YEAR("This Year"),
  ALL_TIME("All Time")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingDistributionScreen(
  viewModel: ExpenseViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  onTransactionClick: (TransactionItem) -> Unit = {}
) {
  val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val categories by viewModel.allCategories.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  var selectedPeriod by remember { mutableStateOf(DistributionPeriod.THIS_MONTH) }
  var chartStyle by remember { mutableStateOf(ChartStyle.DONUT) }
  var selectedCategory by remember { mutableStateOf<CategorySpending?>(null) }

  // Filter transactions for chosen period
  val periodExpenseTransactions = remember(allTransactions, selectedPeriod) {
    val expenseTxs = allTransactions.filter { it.type == TransactionType.EXPENSE }
    val now = Calendar.getInstance()

    when (selectedPeriod) {
      DistributionPeriod.THIS_MONTH -> {
        val startCal = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        expenseTxs.filter { it.date >= startCal.timeInMillis }
      }
      DistributionPeriod.LAST_MONTH -> {
        val startCal = Calendar.getInstance().apply {
          add(Calendar.MONTH, -1)
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
          add(Calendar.MILLISECOND, -1)
        }
        expenseTxs.filter { it.date in startCal.timeInMillis..endCal.timeInMillis }
      }
      DistributionPeriod.LAST_3_MONTHS -> {
        val startCal = Calendar.getInstance().apply {
          add(Calendar.MONTH, -3)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
        }
        expenseTxs.filter { it.date >= startCal.timeInMillis }
      }
      DistributionPeriod.THIS_YEAR -> {
        val startCal = Calendar.getInstance().apply {
          set(Calendar.DAY_OF_YEAR, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
        }
        expenseTxs.filter { it.date >= startCal.timeInMillis }
      }
      DistributionPeriod.ALL_TIME -> expenseTxs
    }
  }

  val totalSpentInPeriod = remember(periodExpenseTransactions) {
    periodExpenseTransactions.sumOf { it.amount }
  }

  // Calculate category distribution list
  val categorySpendingList = remember(periodExpenseTransactions, categories, totalSpentInPeriod) {
    val catMap = categories.associateBy { it.id }

    periodExpenseTransactions.groupBy { it.categoryId }
      .map { (catId, txList) ->
        val spent = txList.sumOf { it.amount }
        val cat = catMap[catId]
        val pct = if (totalSpentInPeriod > 0) (spent / totalSpentInPeriod) * 100.0 else 0.0
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

  // Transactions specific to currently selected category (for drilldown)
  val drillDownTransactions = remember(periodExpenseTransactions, selectedCategory) {
    if (selectedCategory == null) emptyList()
    else periodExpenseTransactions.filter { it.categoryId == selectedCategory?.categoryId }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Spending Distribution",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Category breakdown & insights",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("btn_back_distribution")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        actions = {
          // Toggle Pie vs Donut chart mode
          Surface(
            modifier = Modifier
              .padding(end = 8.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .testTag("btn_toggle_chart_mode"),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(modifier = Modifier.padding(2.dp)) {
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { chartStyle = ChartStyle.DONUT },
                color = if (chartStyle == ChartStyle.DONUT) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DonutLarge,
                  contentDescription = "Donut Mode",
                  tint = if (chartStyle == ChartStyle.DONUT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier
                    .padding(6.dp)
                    .size(20.dp)
                )
              }

              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { chartStyle = ChartStyle.PIE },
                color = if (chartStyle == ChartStyle.PIE) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.PieChart,
                  contentDescription = "Pie Mode",
                  tint = if (chartStyle == ChartStyle.PIE) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier
                    .padding(6.dp)
                    .size(20.dp)
                )
              }
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("spending_distribution_scroll"),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Time Period Selector Filter Chips
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(DistributionPeriod.entries) { period ->
            val isSelected = selectedPeriod == period
            FilterChip(
              selected = isSelected,
              onClick = {
                selectedPeriod = period
                selectedCategory = null // reset drill-down on period change
              },
              label = {
                Text(
                  text = period.label,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              leadingIcon = if (isSelected) {
                {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                }
              } else null,
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
              ),
              modifier = Modifier.testTag("chip_period_${period.name}")
            )
          }
        }
      }

      // 2. High-level metric stats banner
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Total Spending
          FintechCard(
            modifier = Modifier.weight(1.2f),
            elevation = 1.dp
          ) {
            Column {
              Text(
                text = "Total Expenses",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "-${CurrencyFormatter.format(totalSpentInPeriod, currencySymbol)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ExpenseRed
              )
              Text(
                text = "${periodExpenseTransactions.size} transactions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Top Category
          FintechCard(
            modifier = Modifier.weight(1f),
            elevation = 1.dp
          ) {
            Column {
              Text(
                text = "Top Spending",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              val topCat = categorySpendingList.firstOrNull()
              Text(
                text = topCat?.categoryName ?: "None",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = if (topCat != null) "${String.format(Locale.US, "%.1f", topCat.percentage)}% of total" else "0%",
                style = MaterialTheme.typography.labelSmall,
                color = if (topCat != null) parseColorHex(topCat.categoryColorHex) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // 3. Interactive Pie / Donut Chart Hero Card
      item {
        FintechCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("pie_chart_card"),
          elevation = 2.dp
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Distribution by Category",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Tap any slice or legend item to filter",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              if (selectedCategory != null) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier
                    .clip(CircleShape)
                    .clickable { selectedCategory = null }
                    .testTag("btn_clear_slice_selection")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "Reset",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Clear",
                      modifier = Modifier.size(14.dp),
                      tint = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (categorySpendingList.isEmpty()) {
              EmptyState(
                icon = Icons.Default.PieChart,
                title = "No expenses in this period",
                message = "Switch to another time period or log an expense to see the pie chart distribution.",
                modifier = Modifier.padding(vertical = 24.dp)
              )
            } else {
              InteractivePieChart(
                categories = categorySpendingList,
                currencySymbol = currencySymbol,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                chartStyle = chartStyle,
                chartSize = 240.dp
              )
            }
          }
        }
      }

      // 4. Drill-Down Category Transactions Section (Animated visibility on selection)
      if (selectedCategory != null && drillDownTransactions.isNotEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = parseColorHex(selectedCategory!!.categoryColorHex).copy(alpha = 0.08f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("category_drilldown_section")
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CategoryIconBadge(
                    iconName = selectedCategory!!.categoryIcon,
                    colorHex = selectedCategory!!.categoryColorHex,
                    size = 36.dp,
                    iconSize = 18.dp
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "${selectedCategory!!.categoryName} Breakdown",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${drillDownTransactions.size} transactions • ${String.format(Locale.US, "%.1f", selectedCategory!!.percentage)}% of total",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Text(
                  text = "-${CurrencyFormatter.format(selectedCategory!!.totalSpent, currencySymbol)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = parseColorHex(selectedCategory!!.categoryColorHex)
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              // List of individual transactions
              val sdfTime = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US) }
              drillDownTransactions.take(5).forEach { tx ->
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = MaterialTheme.colorScheme.surface,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onTransactionClick(tx) }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = tx.merchant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "${sdfTime.format(Date(tx.date))} • ${tx.accountName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    Text(
                      text = "-${CurrencyFormatter.format(tx.amount, currencySymbol)}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = ExpenseRed
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 5. Category Breakdown Detailed List Header
      if (categorySpendingList.isNotEmpty()) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Category Breakdown (${categorySpendingList.size})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Ranked by spend",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // 6. Category Breakdown items
        itemsIndexed(categorySpendingList, key = { _, item -> item.categoryId }) { index, item ->
          val isSelected = selectedCategory?.categoryId == item.categoryId
          val catColor = parseColorHex(item.categoryColorHex)
          val txCount = periodExpenseTransactions.count { it.categoryId == item.categoryId }

          FintechCard(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("category_breakdown_item_${item.categoryId}"),
            elevation = if (isSelected) 2.dp else 0.5.dp,
            onClick = {
              if (isSelected) {
                selectedCategory = null
              } else {
                selectedCategory = item
              }
            }
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  // Rank Pill
                  Surface(
                    shape = CircleShape,
                    color = if (index < 3) catColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(24.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = "#${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (index < 3) catColor else MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  Spacer(modifier = Modifier.width(10.dp))

                  CategoryIconBadge(
                    iconName = item.categoryIcon,
                    colorHex = item.categoryColorHex,
                    size = 40.dp,
                    iconSize = 20.dp
                  )

                  Spacer(modifier = Modifier.width(12.dp))

                  Column {
                    Text(
                      text = item.categoryName,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "$txCount ${if (txCount == 1) "transaction" else "transactions"}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "-${CurrencyFormatter.format(item.totalSpent, currencySymbol)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                  )
                  Text(
                    text = "${String.format(Locale.US, "%.1f", item.percentage)}% of total",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = catColor
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Percentage Progress Bar
              LinearProgressIndicator(
                progress = { (item.percentage / 100.0).toFloat().coerceIn(0.01f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = catColor,
                trackColor = catColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
              )
            }
          }
        }
      }
    }
  }
}
