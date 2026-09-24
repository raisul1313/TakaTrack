package com.example.ui.screens.recurring

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.RecurringFrequency
import com.example.data.local.entity.TransactionType
import com.example.domain.model.RecurringItem
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.EmptyState
import com.example.ui.components.FintechCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
  viewModel: ExpenseViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val recurringList by viewModel.allRecurring.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  var showAddSheet by remember { mutableStateOf(false) }
  var itemToDelete by remember { mutableStateOf<RecurringItem?>(null) }

  // Monthly commitment calculation
  val monthlyCommitment = remember(recurringList) {
    recurringList.filter { it.isActive && it.type == TransactionType.EXPENSE }.sumOf { item ->
      when (item.frequency) {
        RecurringFrequency.DAILY -> item.amount * 30.0
        RecurringFrequency.WEEKLY -> item.amount * 4.33
        RecurringFrequency.MONTHLY -> item.amount
        RecurringFrequency.YEARLY -> item.amount / 12.0
      }
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text("Recurring & Subscriptions", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showAddSheet = true },
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
        text = { Text("Add Recurring") },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_add_recurring")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Monthly Commitment Card
      item {
        FintechCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Estimated Monthly Commitment",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = CurrencyFormatter.format(monthlyCommitment, currencySymbol),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }
      }

      if (recurringList.isEmpty()) {
        item {
          FintechCard(modifier = Modifier.fillMaxWidth()) {
            EmptyState(
              icon = Icons.Default.Repeat,
              title = "No recurring items",
              message = "Add recurring bills like Netflix, Internet, Rent or Salary to never miss a due date.",
              actionLabel = "Add Recurring",
              onActionClick = { showAddSheet = true }
            )
          }
        }
      } else {
        items(recurringList, key = { it.id }) { item ->
          RecurringRowItem(
            item = item,
            currencySymbol = currencySymbol,
            onToggleActive = { viewModel.toggleRecurringActive(item) },
            onSkipNext = { viewModel.skipNextRecurring(item) },
            onDelete = { itemToDelete = item }
          )
        }
      }
    }
  }

  // Add Recurring Sheet
  if (showAddSheet) {
    AddRecurringSheet(
      viewModel = viewModel,
      onDismiss = { showAddSheet = false }
    )
  }

  // Delete Confirmation Dialog
  if (itemToDelete != null) {
    AlertDialog(
      onDismissRequest = { itemToDelete = null },
      title = { Text("Delete Recurring Item") },
      text = { Text("Are you sure you want to stop tracking ${itemToDelete?.merchant}?") },
      confirmButton = {
        Button(
          onClick = {
            itemToDelete?.let { viewModel.deleteRecurring(it.id) }
            itemToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun RecurringRowItem(
  item: RecurringItem,
  currencySymbol: String,
  onToggleActive: () -> Unit,
  onSkipNext: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sdf = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
  val nextDueFormatted = remember(item.nextDueDate) { sdf.format(Date(item.nextDueDate)) }

  FintechCard(modifier = modifier.fillMaxWidth()) {
    Column {
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
            iconName = item.categoryIcon,
            colorHex = item.categoryColorHex,
            size = 42.dp,
            iconSize = 22.dp
          )

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = item.merchant,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              ) {
                Text(
                  text = item.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Next: $nextDueFormatted",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          val isExpense = item.type == TransactionType.EXPENSE
          Text(
            text = "${if (isExpense) "-" else "+"}${CurrencyFormatter.format(item.amount, currencySymbol)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isExpense) ExpenseRed else IncomeGreen
          )
          Spacer(modifier = Modifier.height(4.dp))
          Switch(
            checked = item.isActive,
            onCheckedChange = { onToggleActive() }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Bottom Row actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Paid from: ${item.accountName}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row {
          TextButton(onClick = onSkipNext) {
            Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Skip Next", style = MaterialTheme.typography.labelSmall)
          }

          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringSheet(
  viewModel: ExpenseViewModel,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val categories by viewModel.allCategories.collectAsStateWithLifecycle()
  val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()

  var merchant by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var type by remember { mutableStateOf(TransactionType.EXPENSE) }
  var frequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
  var selectedCategoryId by remember(categories) {
    mutableStateOf(categories.firstOrNull { it.type == type }?.id ?: "")
  }
  var selectedAccountId by remember(accounts) {
    mutableStateOf(accounts.firstOrNull()?.id ?: "")
  }

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
        text = "Add Recurring Subscription / Bill",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Name / Service",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = merchant,
        onValueChange = { merchant = it },
        placeholder = { Text("e.g. Netflix, Wifi, House Rent, Gym") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_recurring_name"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Amount",
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
        placeholder = { Text("0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_recurring_amount"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Frequency Selector
      Text(
        text = "Frequency",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      var freqExpanded by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(
        expanded = freqExpanded,
        onExpandedChange = { freqExpanded = it }
      ) {
        OutlinedTextField(
          value = frequency.name,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(),
          shape = RoundedCornerShape(14.dp)
        )
        ExposedDropdownMenu(
          expanded = freqExpanded,
          onDismissRequest = { freqExpanded = false }
        ) {
          RecurringFrequency.entries.forEach { f ->
            DropdownMenuItem(
              text = { Text(f.name) },
              onClick = {
                frequency = f
                freqExpanded = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      val amountVal = amountText.toDoubleOrNull() ?: 0.0
      val isValid = merchant.isNotBlank() && amountVal > 0.0

      Button(
        onClick = {
          if (isValid) {
            val nextDue = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
            viewModel.addRecurring(
              type = type,
              amount = amountVal,
              categoryId = selectedCategoryId,
              accountId = selectedAccountId,
              merchant = merchant,
              notes = "Recurring ${frequency.name}",
              frequency = frequency,
              nextDueDate = nextDue
            )
            onDismiss()
          }
        },
        enabled = isValid,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("btn_save_recurring"),
        shape = RoundedCornerShape(16.dp)
      ) {
        Text("Save Recurring", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      }
    }
  }
}
