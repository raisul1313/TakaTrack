package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionType
import com.example.domain.model.BudgetStatus
import com.example.domain.model.FinancialSummary
import com.example.domain.model.SpendingTrendPoint
import com.example.domain.model.TransactionItem
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.EmptyState
import com.example.ui.components.FintechCard
import com.example.ui.components.SpendingBarChart
import com.example.ui.components.parseColorHex
import com.example.ui.navigation.Screen
import com.example.ui.theme.CardAccent
import com.example.ui.theme.CardGradientEnd
import com.example.ui.theme.CardGradientStart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: ExpenseViewModel,
  onNavigate: (String) -> Unit,
  onOpenAddTransaction: () -> Unit,
  onSelectTransaction: (TransactionItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val summary by viewModel.financialSummary.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
  val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val trendTimeframe by viewModel.dashboardTrendTimeframe.collectAsStateWithLifecycle()

  // Greeting
  val greeting = remember {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    when {
      hour < 12 -> "Good morning"
      hour < 17 -> "Good afternoon"
      else -> "Good evening"
    }
  }

  val currentMonthDisplay = remember {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
    sdf.format(Date())
  }

  // Calculate chart trend points for the selected timeframe
  val trendPoints = remember(trendTimeframe, transactions) {
    calculateTrend(trendTimeframe, transactions)
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onOpenAddTransaction,
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
        text = { Text("Add", fontWeight = FontWeight.SemiBold) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_add_transaction")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Top Greeting Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = greeting,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = currentMonthDisplay,
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { onNavigate(Screen.Settings.route) },
              modifier = Modifier.testTag("btn_settings")
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Settings,
                  contentDescription = "Settings",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
          }
        }
      }

      // 2. Main Balance Hero Card
      item {
        MainBalanceCard(
          summary = summary,
          currencySymbol = currencySymbol
        )
      }

      // Quick Action Buttons
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .clickable { onNavigate(Screen.AddExpense.route) }
              .testTag("btn_quick_add_expense"),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp)
          ) {
            Row(
              modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Add Expense",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .clickable { onNavigate(Screen.Accounts.route) }
              .testTag("btn_quick_accounts"),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(16.dp)
          ) {
            Row(
              modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Wallets",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      // 3. Spending Overview Chart
      item {
        SpendingOverviewCard(
          timeframe = trendTimeframe,
          onTimeframeChange = { viewModel.setDashboardTrendTimeframe(it) },
          points = trendPoints,
          currencySymbol = currencySymbol
        )
      }

      // 4. Monthly Budget Summary
      item {
        MonthlyBudgetSummaryCard(
          summary = summary,
          currencySymbol = currencySymbol,
          onViewBudgets = { onNavigate(Screen.Budgets.route) }
        )
      }

      // 5. Top Categories
      if (summary.topCategories.isNotEmpty()) {
        item {
          TopCategoriesCard(
            summary = summary,
            currencySymbol = currencySymbol,
            onViewAll = { onNavigate(Screen.Insights.route) }
          )
        }
      }

      // 6. Recent Transactions
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          TextButton(
            onClick = { onNavigate(Screen.Transactions.route) },
            modifier = Modifier.testTag("btn_see_all_transactions")
          ) {
            Text(
              text = "See all",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      val recentTxs = transactions.take(5)
      if (recentTxs.isEmpty()) {
        item {
          FintechCard(modifier = Modifier.fillMaxWidth()) {
            EmptyState(
              icon = Icons.Default.ReceiptLong,
              title = "No transactions yet",
              message = "Tap '+ Add' to record your first expense or income.",
              actionLabel = "Add Transaction",
              onActionClick = onOpenAddTransaction
            )
          }
        }
      } else {
        items(recentTxs, key = { it.id }) { tx ->
          HomeTransactionRow(
            tx = tx,
            currencySymbol = currencySymbol,
            onClick = { onSelectTransaction(tx) }
          )
        }
      }
    }
  }
}

@Composable
fun MainBalanceCard(
  summary: FinancialSummary,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("hero_balance_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0xFF0F766E), // Deep Teal
              Color(0xFF0D9488), // Teal
              Color(0xFF042F2E)  // Rich Dark Emerald
            )
          )
        )
        .padding(20.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Total Balance",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f)
          )
          Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF5EEAD4),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${String.format(Locale.US, "%.0f", summary.savingsRatePercent)}% saved",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF5EEAD4),
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Big Balance
        Text(
          text = CurrencyFormatter.format(summary.totalBalance, currencySymbol),
          style = MaterialTheme.typography.displayMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mini metrics row: Income, Expenses, Savings
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.18f))
            .padding(vertical = 12.dp, horizontal = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Income
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(18.dp)
                  .clip(CircleShape)
                  .background(IncomeGreen.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDownward,
                  contentDescription = null,
                  tint = IncomeGreen,
                  modifier = Modifier.size(12.dp)
                )
              }
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Income",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyFormatter.format(summary.incomeThisMonth, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Box(
            modifier = Modifier
              .width(1.dp)
              .height(34.dp)
              .background(Color.White.copy(alpha = 0.15f))
              .align(Alignment.CenterVertically)
          )

          // Expenses
          Column(
            modifier = Modifier
              .weight(1f)
              .padding(start = 12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(18.dp)
                  .clip(CircleShape)
                  .background(ExpenseRed.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowUpward,
                  contentDescription = null,
                  tint = ExpenseRed,
                  modifier = Modifier.size(12.dp)
                )
              }
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Expenses",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyFormatter.format(summary.expensesThisMonth, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Box(
            modifier = Modifier
              .width(1.dp)
              .height(34.dp)
              .background(Color.White.copy(alpha = 0.15f))
              .align(Alignment.CenterVertically)
          )

          // Savings
          Column(
            modifier = Modifier
              .weight(1f)
              .padding(start = 12.dp)
          ) {
            Text(
              text = "Savings",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyFormatter.format(summary.savingsThisMonth, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF5EEAD4)
            )
          }
        }
      }
    }
  }
}

@Composable
fun SpendingOverviewCard(
  timeframe: String,
  onTimeframeChange: (String) -> Unit,
  points: List<SpendingTrendPoint>,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  FintechCard(modifier = modifier.fillMaxWidth()) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Spending Overview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          val totalInTrend = points.sumOf { it.amount }
          Text(
            text = "${CurrencyFormatter.format(totalInTrend, currencySymbol)} spent",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          listOf("Week", "Month", "Year").forEach { tf ->
            val selected = timeframe == tf
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onTimeframeChange(tf) }
            ) {
              Text(
                text = tf,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      SpendingBarChart(
        points = points,
        currencySymbol = currencySymbol,
        chartHeight = 130.dp
      )
    }
  }
}

@Composable
fun MonthlyBudgetSummaryCard(
  summary: FinancialSummary,
  currencySymbol: String,
  onViewBudgets: () -> Unit,
  modifier: Modifier = Modifier
) {
  FintechCard(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onViewBudgets)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PieChart,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Monthly Budget",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${CurrencyFormatter.format(summary.monthlyBudgetAmount, currencySymbol)} limit",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        val percentage = summary.monthlyBudgetPercentage
        val statusColor = when {
          percentage >= 100.0 -> ExpenseRed
          percentage >= 80.0 -> WarningAmber
          else -> IncomeGreen
        }

        Surface(
          shape = CircleShape,
          color = statusColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = "${String.format(Locale.US, "%.0f", percentage)}% spent",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Progress bar
      val progressFraction = (summary.monthlyBudgetPercentage / 100.0).toFloat().coerceIn(0f, 1f)
      LinearProgressIndicator(
        progress = { progressFraction },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(CircleShape),
        color = when {
          summary.monthlyBudgetPercentage >= 100.0 -> ExpenseRed
          summary.monthlyBudgetPercentage >= 80.0 -> WarningAmber
          else -> MaterialTheme.colorScheme.primary
        },
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Spent: ${CurrencyFormatter.format(summary.monthlyBudgetSpent, currencySymbol)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Remaining: ${CurrencyFormatter.format(summary.monthlyBudgetRemaining, currencySymbol)}",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = if (summary.monthlyBudgetRemaining > 0) MaterialTheme.colorScheme.onSurface else ExpenseRed
        )
      }
    }
  }
}

@Composable
fun TopCategoriesCard(
  summary: FinancialSummary,
  currencySymbol: String,
  onViewAll: () -> Unit,
  modifier: Modifier = Modifier
) {
  FintechCard(modifier = modifier.fillMaxWidth()) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Top Spending Categories",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onViewAll) {
          Text(
            text = "Details",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      summary.topCategories.forEach { cat ->
        val catColor = parseColorHex(cat.categoryColorHex)
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              CategoryIconBadge(
                iconName = cat.categoryIcon,
                colorHex = cat.categoryColorHex,
                size = 32.dp,
                iconSize = 16.dp,
                shapeCorner = 10.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = cat.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = CurrencyFormatter.format(cat.totalSpent, currencySymbol),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "(${String.format(Locale.US, "%.0f", cat.percentage)}%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          LinearProgressIndicator(
            progress = { (cat.percentage / 100f).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(5.dp)
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
fun HomeTransactionRow(
  tx: TransactionItem,
  currencySymbol: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isExpense = tx.type == TransactionType.EXPENSE
  val isTransfer = tx.type == TransactionType.TRANSFER

  val sdf = remember { SimpleDateFormat("MMM d, h:mm a", Locale.US) }
  val dateFormatted = remember(tx.date) { sdf.format(Date(tx.date)) }

  FintechCard(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    elevation = 0.5.dp,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        CategoryIconBadge(
          iconName = tx.categoryIcon,
          colorHex = tx.categoryColorHex,
          size = 44.dp,
          iconSize = 22.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = tx.merchant,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = tx.categoryName,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = " • ",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline
            )
            Text(
              text = if (isTransfer) "${tx.accountName} → ${tx.toAccountName ?: "Account"}" else tx.accountName,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(horizontalAlignment = Alignment.End) {
        val amountColor = when {
          isExpense -> ExpenseRed
          isTransfer -> MaterialTheme.colorScheme.primary
          else -> IncomeGreen
        }
        val prefix = when {
          isExpense -> "-"
          isTransfer -> ""
          else -> "+"
        }

        Text(
          text = "$prefix${CurrencyFormatter.format(tx.amount, currencySymbol)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = amountColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = dateFormatted,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

private fun calculateTrend(timeframe: String, transactions: List<TransactionItem>): List<SpendingTrendPoint> {
  val expenseTxs = transactions.filter { it.type == TransactionType.EXPENSE }
  val sdfDay = SimpleDateFormat("EEE", Locale.US)
  val sdfMonth = SimpleDateFormat("MMM", Locale.US)

  return when (timeframe) {
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
