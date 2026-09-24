package com.example.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionType
import com.example.domain.model.TransactionItem
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.EmptyState
import com.example.ui.components.ExpenseHistoryList
import com.example.ui.components.FintechCard
import com.example.ui.screens.home.HomeTransactionRow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
  viewModel: ExpenseViewModel,
  onOpenAddTransaction: () -> Unit,
  onSelectTransaction: (TransactionItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val categories by viewModel.allCategories.collectAsStateWithLifecycle()
  val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
  var selectedCategoryId by remember { mutableStateOf<String?>(null) }
  var selectedAccountId by remember { mutableStateOf<String?>(null) }
  var showAccountMenu by remember { mutableStateOf(false) }

  // Filter transactions
  val filteredTransactions = remember(
    transactions,
    searchQuery,
    selectedTypeFilter,
    selectedCategoryId,
    selectedAccountId
  ) {
    transactions.filter { tx ->
      val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter
      val matchesCategory = selectedCategoryId == null || tx.categoryId == selectedCategoryId
      val matchesAccount = selectedAccountId == null || tx.accountId == selectedAccountId || tx.toAccountId == selectedAccountId
      val matchesQuery = if (searchQuery.isBlank()) true else {
        tx.merchant.contains(searchQuery, ignoreCase = true) ||
          tx.categoryName.contains(searchQuery, ignoreCase = true) ||
          tx.notes.contains(searchQuery, ignoreCase = true) ||
          tx.accountName.contains(searchQuery, ignoreCase = true) ||
          tx.amount.toString().contains(searchQuery)
      }
      matchesType && matchesCategory && matchesAccount && matchesQuery
    }
  }

  // Calculate total expense & income for filtered view
  val filteredExpense = remember(filteredTransactions) {
    filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
  }
  val filteredIncome = remember(filteredTransactions) {
    filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
  }

  // Group by day
  val groupedTransactions = remember(filteredTransactions) {
    val map = linkedMapOf<String, MutableList<TransactionItem>>()
    val sdfDate = SimpleDateFormat("MMMM d, yyyy", Locale.US)
    val now = Calendar.getInstance()
    val todayStr = sdfDate.format(now.time)
    now.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStr = sdfDate.format(now.time)

    for (tx in filteredTransactions) {
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

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onOpenAddTransaction,
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
        text = { Text("Add") },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_add_from_transactions")
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Top header & Search Bar
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "Transactions",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by merchant, note, category...") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_transactions_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FilterChip(
            selected = selectedTypeFilter == null,
            onClick = { selectedTypeFilter = null },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          FilterChip(
            selected = selectedTypeFilter == TransactionType.EXPENSE,
            onClick = { selectedTypeFilter = TransactionType.EXPENSE },
            label = { Text("Expenses") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          FilterChip(
            selected = selectedTypeFilter == TransactionType.INCOME,
            onClick = { selectedTypeFilter = TransactionType.INCOME },
            label = { Text("Income") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          FilterChip(
            selected = selectedTypeFilter == TransactionType.TRANSFER,
            onClick = { selectedTypeFilter = TransactionType.TRANSFER },
            label = { Text("Transfers") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          // Filter by Account dropdown
          Box {
            FilterChip(
              selected = selectedAccountId != null,
              onClick = { showAccountMenu = true },
              label = {
                val acc = accounts.firstOrNull { it.id == selectedAccountId }
                Text(acc?.name ?: "Account")
              },
              trailingIcon = {
                Icon(
                  imageVector = Icons.Default.FilterList,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
              },
              shape = RoundedCornerShape(10.dp)
            )

            DropdownMenu(
              expanded = showAccountMenu,
              onDismissRequest = { showAccountMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text("All Accounts") },
                onClick = {
                  selectedAccountId = null
                  showAccountMenu = false
                }
              )
              accounts.forEach { acc ->
                DropdownMenuItem(
                  text = { Text(acc.name) },
                  onClick = {
                    selectedAccountId = acc.id
                    showAccountMenu = false
                  }
                )
              }
            }
          }
        }
      }

      // Summary subheader for filtered transactions
      if (filteredTransactions.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${filteredTransactions.size} transactions",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (filteredExpense > 0) {
              Text(
                text = "Spent: -${CurrencyFormatter.format(filteredExpense, currencySymbol)}",
                style = MaterialTheme.typography.labelSmall,
                color = com.example.ui.theme.ExpenseRed,
                fontWeight = FontWeight.SemiBold
              )
            }
            if (filteredIncome > 0) {
              Text(
                text = "Income: +${CurrencyFormatter.format(filteredIncome, currencySymbol)}",
                style = MaterialTheme.typography.labelSmall,
                color = com.example.ui.theme.IncomeGreen,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // List of transactions grouped by date
      if (selectedTypeFilter == TransactionType.EXPENSE) {
        ExpenseHistoryList(
          expenses = filteredTransactions,
          currencySymbol = currencySymbol,
          onExpenseClick = onSelectTransaction,
          searchQuery = searchQuery,
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
          emptyStateAction = {
            if (searchQuery.isNotBlank() || selectedAccountId != null) {
              searchQuery = ""
              selectedAccountId = null
            } else {
              onOpenAddTransaction()
            }
          }
        )
      } else if (filteredTransactions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
          contentAlignment = Alignment.Center
        ) {
          EmptyState(
            icon = Icons.Default.ReceiptLong,
            title = "No transactions found",
            message = if (searchQuery.isNotBlank() || selectedTypeFilter != null || selectedAccountId != null) {
              "Try changing your search keywords or active filters."
            } else {
              "You haven't recorded any transactions yet."
            },
            actionLabel = if (searchQuery.isNotBlank() || selectedTypeFilter != null || selectedAccountId != null) "Clear Filters" else "Add Transaction",
            onActionClick = {
              if (searchQuery.isNotBlank() || selectedTypeFilter != null || selectedAccountId != null) {
                searchQuery = ""
                selectedTypeFilter = null
                selectedAccountId = null
              } else {
                onOpenAddTransaction()
              }
            }
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          groupedTransactions.forEach { (dateHeader, txList) ->
            item(key = "header_$dateHeader") {
              Text(
                text = dateHeader,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
              )
            }

            items(txList, key = { it.id }) { tx ->
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
  }
}
