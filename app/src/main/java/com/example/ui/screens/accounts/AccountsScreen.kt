package com.example.ui.screens.accounts

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
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.TransactionType
import com.example.domain.model.AccountItem
import com.example.ui.ExpenseViewModel
import com.example.ui.components.AccountIconBadge
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.FintechCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
  viewModel: ExpenseViewModel,
  onNavigateBack: () -> Unit,
  onOpenTransfer: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  var showAddAccountSheet by remember { mutableStateOf(false) }
  var accountToDelete by remember { mutableStateOf<AccountItem?>(null) }

  val totalNetWorth = remember(accounts) {
    accounts.sumOf { it.currentBalance }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text("Accounts & Wallets", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = onOpenTransfer) {
            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Transfer")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showAddAccountSheet = true },
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
        text = { Text("Add Account") },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("fab_add_account")
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
      // Net Worth Summary Card
      item {
        FintechCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Total Combined Balance",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = CurrencyFormatter.format(totalNetWorth, currencySymbol),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Button(
              onClick = onOpenTransfer,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Transfer")
            }
          }
        }
      }

      items(accounts, key = { it.id }) { acc ->
        AccountRowItem(
          account = acc,
          currencySymbol = currencySymbol,
          onDelete = {
            if (accounts.size > 1) {
              accountToDelete = acc
            }
          }
        )
      }
    }
  }

  // Add Account Sheet
  if (showAddAccountSheet) {
    AddAccountSheet(
      viewModel = viewModel,
      onDismiss = { showAddAccountSheet = false }
    )
  }

  // Delete Confirmation Dialog
  if (accountToDelete != null) {
    AlertDialog(
      onDismissRequest = { accountToDelete = null },
      title = { Text("Delete Account") },
      text = { Text("Are you sure you want to delete ${accountToDelete?.name}? Transactions linked to this account may lose account details.") },
      confirmButton = {
        Button(
          onClick = {
            accountToDelete?.let { viewModel.deleteAccount(it.id) }
            accountToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { accountToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun AccountRowItem(
  account: AccountItem,
  currencySymbol: String,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  FintechCard(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        AccountIconBadge(
          iconName = account.iconName,
          colorHex = account.colorHex,
          size = 46.dp,
          iconSize = 22.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
          Text(
            text = account.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${account.type.name} • Initial: ${CurrencyFormatter.format(account.initialBalance, currencySymbol)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        val balanceColor = if (account.currentBalance >= 0) MaterialTheme.colorScheme.onSurface else ExpenseRed
        Text(
          text = CurrencyFormatter.format(account.currentBalance, currencySymbol),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = balanceColor
        )
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Account",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(
  viewModel: ExpenseViewModel,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var name by remember { mutableStateOf("") }
  var accountType by remember { mutableStateOf(AccountType.CASH) }
  var initialBalanceText by remember { mutableStateOf("") }
  var selectedColorHex by remember { mutableStateOf("#0D9488") }
  var selectedIconName by remember { mutableStateOf("payments") }

  val colorsList = listOf(
    "#0D9488", "#2563EB", "#7C3AED", "#E11D48", "#EA580C", "#059669", "#475569"
  )

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
        text = "Add New Account",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Account Name",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        placeholder = { Text("e.g. BRAC Bank, Upay, Savings") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_account_name"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Account Type Selector
      Text(
        text = "Account Type",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      var typeExpanded by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(
        expanded = typeExpanded,
        onExpandedChange = { typeExpanded = it }
      ) {
        OutlinedTextField(
          value = accountType.name,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(),
          shape = RoundedCornerShape(14.dp)
        )
        ExposedDropdownMenu(
          expanded = typeExpanded,
          onDismissRequest = { typeExpanded = false }
        ) {
          AccountType.entries.forEach { type ->
            DropdownMenuItem(
              text = { Text(type.name) },
              onClick = {
                accountType = type
                selectedIconName = when (type) {
                  AccountType.CASH -> "payments"
                  AccountType.BANK -> "account_balance"
                  AccountType.MOBILE_WALLET -> "smartphone"
                  AccountType.CREDIT_CARD -> "credit_card"
                  AccountType.SAVINGS -> "savings"
                  AccountType.INVESTMENT -> "trending_up"
                }
                typeExpanded = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Initial Balance
      Text(
        text = "Initial Balance",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = initialBalanceText,
        onValueChange = { input ->
          if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            initialBalanceText = input
          }
        },
        placeholder = { Text("0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_initial_balance"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Color Palette Row
      Text(
        text = "Accent Color",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        colorsList.forEach { colorHex ->
          val isSelected = selectedColorHex == colorHex
          val color = Color(android.graphics.Color.parseColor(colorHex))
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(color)
              .clickable { selectedColorHex = colorHex },
            contentAlignment = Alignment.Center
          ) {
            if (isSelected) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(Color.White)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      val isValid = name.isNotBlank()
      Button(
        onClick = {
          if (isValid) {
            val balance = initialBalanceText.toDoubleOrNull() ?: 0.0
            viewModel.addAccount(
              name = name,
              type = accountType,
              initialBalance = balance,
              colorHex = selectedColorHex,
              iconName = selectedIconName
            )
            onDismiss()
          }
        },
        enabled = isValid,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("btn_save_account"),
        shape = RoundedCornerShape(16.dp)
      ) {
        Text("Save Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      }
    }
  }
}
