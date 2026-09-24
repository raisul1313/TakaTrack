package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionType
import com.example.domain.model.TransactionItem
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.ExpenseRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Connected variant of ExpenseHistoryList:
 * Observes expenses and currency directly from Room database via ExpenseViewModel.
 */
@Composable
fun ExpenseHistoryList(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier,
  onExpenseClick: (TransactionItem) -> Unit = {},
  searchQuery: String = "",
  selectedCategoryId: String? = null,
  selectedAccountId: String? = null,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  emptyStateActionLabel: String? = "Add Expense",
  emptyStateAction: (() -> Unit)? = null
) {
  val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  // Filter only expenses from the Room database stream, applying optional category/account/query filters
  val expenses = remember(allTransactions, searchQuery, selectedCategoryId, selectedAccountId) {
    allTransactions.filter { tx ->
      val isExpense = tx.type == TransactionType.EXPENSE
      val matchesCategory = selectedCategoryId == null || tx.categoryId == selectedCategoryId
      val matchesAccount = selectedAccountId == null || tx.accountId == selectedAccountId
      val matchesQuery = if (searchQuery.isBlank()) true else {
        tx.merchant.contains(searchQuery, ignoreCase = true) ||
          tx.categoryName.contains(searchQuery, ignoreCase = true) ||
          tx.notes.contains(searchQuery, ignoreCase = true) ||
          tx.accountName.contains(searchQuery, ignoreCase = true) ||
          tx.amount.toString().contains(searchQuery)
      }
      isExpense && matchesCategory && matchesAccount && matchesQuery
    }
  }

  ExpenseHistoryList(
    expenses = expenses,
    currencySymbol = currencySymbol,
    modifier = modifier,
    onExpenseClick = onExpenseClick,
    searchQuery = searchQuery,
    contentPadding = contentPadding,
    emptyStateActionLabel = emptyStateActionLabel,
    emptyStateAction = emptyStateAction
  )
}

/**
 * Pure Composable variant of ExpenseHistoryList:
 * Displays a scrollable financial history list grouped by dates with daily subtotals,
 * custom category badges, and smooth scroll behavior.
 */
@Composable
fun ExpenseHistoryList(
  expenses: List<TransactionItem>,
  currencySymbol: String,
  modifier: Modifier = Modifier,
  onExpenseClick: (TransactionItem) -> Unit = {},
  searchQuery: String = "",
  contentPadding: PaddingValues = PaddingValues(16.dp),
  emptyStateTitle: String = "No expenses recorded",
  emptyStateSubtitle: String = "Your financial history will appear here once you log an expense.",
  emptyStateActionLabel: String? = "Add Expense",
  emptyStateAction: (() -> Unit)? = null
) {
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  // Group expenses by date (Today, Yesterday, Date format)
  val groupedExpenses = remember(expenses) {
    val map = linkedMapOf<String, MutableList<TransactionItem>>()
    val sdfDate = SimpleDateFormat("MMMM d, yyyy", Locale.US)
    val now = Calendar.getInstance()
    val todayStr = sdfDate.format(now.time)
    now.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStr = sdfDate.format(now.time)

    for (tx in expenses) {
      val txDate = sdfDate.format(Date(tx.date))
      val header = when (txDate) {
        todayStr -> "Today"
        yesterdayStr -> "Yesterday"
        else -> txDate
      }
      map.getOrPut(header) { mutableListOf() }.add(tx)
    }
    map
  }

  // Total amount spent across the displayed list
  val totalHistoryExpense = remember(expenses) {
    expenses.sumOf { it.amount }
  }

  val showScrollToTop by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 4 }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("expense_history_list_container")
  ) {
    if (expenses.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(contentPadding),
        contentAlignment = Alignment.Center
      ) {
        EmptyState(
          icon = Icons.Default.ReceiptLong,
          title = if (searchQuery.isNotBlank()) "No matching expenses" else emptyStateTitle,
          message = if (searchQuery.isNotBlank()) {
            "No expense history matching \"$searchQuery\" was found."
          } else {
            emptyStateSubtitle
          },
          actionLabel = if (searchQuery.isNotBlank()) "Clear Filter" else emptyStateActionLabel,
          onActionClick = emptyStateAction
        )
      }
    } else {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .testTag("expense_history_scrollable_list"),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Summary Header Item
        item(key = "history_summary_header") {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${expenses.size} ${if (expenses.size == 1) "Expense" else "Expenses"} in history",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "Total: -${CurrencyFormatter.format(totalHistoryExpense, currencySymbol)}",
                style = MaterialTheme.typography.labelLarge,
                color = ExpenseRed,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Date groups with daily subtotals
        groupedExpenses.forEach { (dateHeader, dayExpenses) ->
          val dayTotal = dayExpenses.sumOf { it.amount }

          item(key = "header_$dateHeader") {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = dateHeader,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              Text(
                text = "-${CurrencyFormatter.format(dayTotal, currencySymbol)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          items(dayExpenses, key = { it.id }) { expense ->
            ExpenseHistoryItem(
              expense = expense,
              currencySymbol = currencySymbol,
              onClick = { onExpenseClick(expense) }
            )
          }
        }
      }
    }

    // Scroll to Top FAB for long histories
    AnimatedVisibility(
      visible = showScrollToTop,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
    ) {
      FloatingActionButton(
        onClick = {
          coroutineScope.launch {
            listState.animateScrollToItem(0)
          }
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
        modifier = Modifier
          .size(44.dp)
          .testTag("btn_scroll_to_top")
      ) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowUp,
          contentDescription = "Scroll to top",
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

/**
 * Individual expense card in the financial history list.
 */
@Composable
fun ExpenseHistoryItem(
  expense: TransactionItem,
  currencySymbol: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.US) }
  val timeFormatted = remember(expense.date) { timeFormat.format(Date(expense.date)) }

  FintechCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("expense_item_${expense.id}"),
    shape = RoundedCornerShape(16.dp),
    elevation = 0.5.dp,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left side: Category icon badge & Details
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CategoryIconBadge(
          iconName = expense.categoryIcon,
          colorHex = expense.categoryColorHex,
          size = 46.dp,
          iconSize = 22.dp,
          shapeCorner = 14.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = expense.merchant,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(2.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // Category tag
            Text(
              text = expense.categoryName,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
              text = "•",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.outline
            )

            // Account / Wallet
            Text(
              text = expense.accountName,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.primary
            )

            Text(
              text = "•",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.outline
            )

            // Timestamp
            Text(
              text = timeFormatted,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!expense.attachmentUri.isNullOrBlank()) {
              Spacer(modifier = Modifier.width(2.dp))
              Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "Receipt attached",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
              )
            }
          }

          // Optional notes preview if available and distinct from merchant
          if (expense.notes.isNotBlank() && expense.notes != expense.merchant) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = expense.notes,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Right side: Negative amount formatted in red
      Text(
        text = "-${CurrencyFormatter.format(expense.amount, currencySymbol)}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ExpenseRed
      )
    }
  }
}
