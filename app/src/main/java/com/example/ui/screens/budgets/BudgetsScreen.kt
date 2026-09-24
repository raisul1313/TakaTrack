package com.example.ui.screens.budgets

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BudgetPeriod
import com.example.domain.model.BudgetItem
import com.example.domain.model.BudgetStatus
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.EmptyState
import com.example.ui.components.FintechCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier
) {
  val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
  val categories by viewModel.allCategories.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  var showAddBudgetSheet by remember { mutableStateOf(false) }
  var editingBudget by remember { mutableStateOf<BudgetItem?>(null) }
  var budgetToDelete by remember { mutableStateOf<BudgetItem?>(null) }

  val overallBudget = remember(budgets) {
    budgets.firstOrNull { it.categoryId == null }
  }

  val categoryBudgets = remember(budgets) {
    budgets.filter { it.categoryId != null }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = {
          editingBudget = null
          showAddBudgetSheet = true
        },
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
        text = { Text("Set Budget") },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_add_budget")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Monthly Budgets",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Track your limits and avoid overspending",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Overall Budget Card
      if (overallBudget != null) {
        item {
          BudgetCard(
            budget = overallBudget,
            currencySymbol = currencySymbol,
            isOverall = true,
            onEdit = {
              editingBudget = overallBudget
              showAddBudgetSheet = true
            },
            onDelete = { budgetToDelete = overallBudget }
          )
        }
      }

      item {
        Text(
          text = "Category Budgets",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(top = 8.dp)
        )
      }

      if (categoryBudgets.isEmpty()) {
        item {
          FintechCard(modifier = Modifier.fillMaxWidth()) {
            EmptyState(
              icon = Icons.Default.PieChart,
              title = "No category budgets set",
              message = "Add individual budgets for categories like Food, Transport, and Shopping to stay disciplined.",
              actionLabel = "Add Category Budget",
              onActionClick = {
                editingBudget = null
                showAddBudgetSheet = true
              }
            )
          }
        }
      } else {
        items(categoryBudgets, key = { it.id }) { item ->
          BudgetCard(
            budget = item,
            currencySymbol = currencySymbol,
            isOverall = false,
            onEdit = {
              editingBudget = item
              showAddBudgetSheet = true
            },
            onDelete = { budgetToDelete = item }
          )
        }
      }
    }
  }

  // Set / Edit Budget Bottom Sheet
  if (showAddBudgetSheet) {
    SetBudgetSheet(
      viewModel = viewModel,
      editingBudget = editingBudget,
      onDismiss = {
        showAddBudgetSheet = false
        editingBudget = null
      }
    )
  }

  // Delete Confirmation Dialog
  if (budgetToDelete != null) {
    AlertDialog(
      onDismissRequest = { budgetToDelete = null },
      title = { Text("Delete Budget") },
      text = { Text("Are you sure you want to remove this budget?") },
      confirmButton = {
        Button(
          onClick = {
            budgetToDelete?.let { viewModel.deleteBudget(it.id) }
            budgetToDelete = null
          },
          colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ExpenseRed)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { budgetToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun BudgetCard(
  budget: BudgetItem,
  currencySymbol: String,
  isOverall: Boolean,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val statusColor = when (budget.status) {
    BudgetStatus.EXCEEDED -> ExpenseRed
    BudgetStatus.WARNING_90 -> ExpenseRed
    BudgetStatus.WARNING_70 -> WarningAmber
    BudgetStatus.SAFE -> IncomeGreen
  }

  val progressFraction = (budget.percentageUsed / 100.0).toFloat().coerceIn(0f, 1f)
  val dailyAllowance = if (budget.daysRemainingInMonth > 0) {
    budget.remainingAmount / budget.daysRemainingInMonth
  } else 0.0

  FintechCard(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    borderColor = if (budget.status == BudgetStatus.EXCEEDED) ExpenseRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
  ) {
    Column {
      // Header
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
            iconName = budget.categoryIcon,
            colorHex = budget.categoryColorHex,
            size = 40.dp,
            iconSize = 20.dp
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = budget.categoryName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Limit: ${CurrencyFormatter.format(budget.budgetAmount, currencySymbol)}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Progress bar
      LinearProgressIndicator(
        progress = { progressFraction },
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .clip(CircleShape),
        color = statusColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Spent and Remaining stats
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Spent",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = CurrencyFormatter.format(budget.spentAmount, currencySymbol),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Used",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${String.format(Locale.US, "%.0f", budget.percentageUsed)}%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = statusColor
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Remaining",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = CurrencyFormatter.format(budget.remainingAmount, currencySymbol),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (budget.remainingAmount > 0) MaterialTheme.colorScheme.onSurface else ExpenseRed
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Daily allowance pill
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Daily spending allowance:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${CurrencyFormatter.format(dailyAllowance, currencySymbol)} / day (${budget.daysRemainingInMonth}d left)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetSheet(
  viewModel: ExpenseViewModel,
  editingBudget: BudgetItem?,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val categories by viewModel.allCategories.collectAsStateWithLifecycle()
  val expenseCategories = remember(categories) {
    categories.filter { it.type == com.example.data.local.entity.TransactionType.EXPENSE }
  }

  var isOverall by remember {
    mutableStateOf(editingBudget?.categoryId == null)
  }

  var selectedCategoryId by remember {
    mutableStateOf(editingBudget?.categoryId ?: expenseCategories.firstOrNull()?.id ?: "")
  }

  var amountText by remember {
    mutableStateOf(
      if (editingBudget != null) {
        if (editingBudget.budgetAmount % 1.0 == 0.0) editingBudget.budgetAmount.toLong().toString()
        else editingBudget.budgetAmount.toString()
      } else "10000"
    )
  }

  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .padding(bottom = 32.dp)
    ) {
      Text(
        text = if (editingBudget == null) "Set Budget Limit" else "Edit Budget",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Budget Scope Selector
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(4.dp)
      ) {
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable { isOverall = true },
          color = if (isOverall) MaterialTheme.colorScheme.surface else Color.Transparent,
          shape = RoundedCornerShape(10.dp)
        ) {
          Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Overall Monthly",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = if (isOverall) FontWeight.Bold else FontWeight.Normal,
              color = if (isOverall) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable { isOverall = false },
          color = if (!isOverall) MaterialTheme.colorScheme.surface else Color.Transparent,
          shape = RoundedCornerShape(10.dp)
        ) {
          Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Specific Category",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = if (!isOverall) FontWeight.Bold else FontWeight.Normal,
              color = if (!isOverall) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (!isOverall) {
        Text(
          text = "Select Category",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        var menuExpanded by remember { mutableStateOf(false) }
        val currentCat = expenseCategories.firstOrNull { it.id == selectedCategoryId }

        ExposedDropdownMenuBox(
          expanded = menuExpanded,
          onExpandedChange = { menuExpanded = it }
        ) {
          OutlinedTextField(
            value = currentCat?.name ?: "Select category",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
            shape = RoundedCornerShape(14.dp)
          )
          ExposedDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
          ) {
            expenseCategories.forEach { cat ->
              DropdownMenuItem(
                text = { Text(cat.name) },
                onClick = {
                  selectedCategoryId = cat.id
                  menuExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // Budget Amount
      Text(
        text = "Budget Amount ($currencySymbol)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = amountText,
        onValueChange = { input ->
          if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            amountText = input
          }
        },
        placeholder = { Text("e.g. 20000") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_budget_amount"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      val amountVal = amountText.toDoubleOrNull() ?: 0.0
      val isValid = amountVal > 0.0

      Button(
        onClick = {
          if (isValid) {
            viewModel.setBudget(
              categoryId = if (isOverall) null else selectedCategoryId,
              amount = amountVal
            )
            onDismiss()
          }
        },
        enabled = isValid,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("btn_save_budget"),
        shape = RoundedCornerShape(16.dp)
      ) {
        Text("Save Budget Limit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      }
    }
  }
}
