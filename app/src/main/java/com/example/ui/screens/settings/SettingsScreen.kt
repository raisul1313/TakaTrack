package com.example.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppCurrency
import com.example.data.local.PopularCurrencies
import com.example.data.local.entity.TransactionType
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.FintechCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.ExpenseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: ExpenseViewModel,
  onNavigateBack: () -> Unit,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
  val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
  val dailyReminder by viewModel.dailyReminderEnabled.collectAsStateWithLifecycle()
  val budgetAlerts by viewModel.budgetAlertsEnabled.collectAsStateWithLifecycle()

  var showCurrencyDialog by remember { mutableStateOf(false) }
  var showThemeDialog by remember { mutableStateOf(false) }
  var showManageCategoriesSheet by remember { mutableStateOf(false) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text("Settings & Preferences", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 48.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. General Preferences
      item {
        Text(
          text = "Preferences",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      item {
        FintechCard(modifier = Modifier.fillMaxWidth()) {
          Column {
            SettingsNavigationRow(
              icon = Icons.Default.MonetizationOn,
              title = "Currency",
              subtitle = "$currencyCode ($currencySymbol)",
              onClick = { showCurrencyDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SettingsNavigationRow(
              icon = Icons.Default.DarkMode,
              title = "Theme",
              subtitle = when (appTheme) {
                "LIGHT" -> "Light Mode"
                "DARK" -> "Dark Mode"
                else -> "System Default"
              },
              onClick = { showThemeDialog = true }
            )
          }
        }
      }

      // 2. Data & Management
      item {
        Text(
          text = "Manage Features",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      item {
        FintechCard(modifier = Modifier.fillMaxWidth()) {
          Column {
            SettingsNavigationRow(
              icon = Icons.Default.AccountBalanceWallet,
              title = "Accounts & Wallets",
              subtitle = "Configure Cash, Bank, and Mobile Wallets",
              onClick = { onNavigate(Screen.Accounts.route) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SettingsNavigationRow(
              icon = Icons.Default.Repeat,
              title = "Recurring & Subscriptions",
              subtitle = "Manage recurring bills, due dates, and frequencies",
              onClick = { onNavigate(Screen.Recurring.route) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SettingsNavigationRow(
              icon = Icons.Default.Category,
              title = "Custom Categories",
              subtitle = "Add and organize custom income & expense categories",
              onClick = { showManageCategoriesSheet = true }
            )
          }
        }
      }

      // 3. Notifications & Reminders
      item {
        Text(
          text = "Reminders & Alerts",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      item {
        FintechCard(modifier = Modifier.fillMaxWidth()) {
          Column {
            SettingsSwitchRow(
              icon = Icons.Default.Notifications,
              title = "Daily Evening Review",
              subtitle = "Gentle reminder to record today's spending",
              checked = dailyReminder,
              onCheckedChange = { viewModel.setDailyReminder(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SettingsSwitchRow(
              icon = Icons.Default.Notifications,
              title = "Budget Threshold Warning",
              subtitle = "Alert when spending crosses 80% of budget limit",
              checked = budgetAlerts,
              onCheckedChange = { viewModel.setBudgetAlerts(it) }
            )
          }
        }
      }

      // 4. Data Backup & Export
      item {
        Text(
          text = "Export & Backup",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      item {
        FintechCard(modifier = Modifier.fillMaxWidth()) {
          Column {
            SettingsNavigationRow(
              icon = Icons.Default.FileDownload,
              title = "Export to CSV",
              subtitle = "Export transactions spreadsheet compatible with Excel",
              onClick = { viewModel.exportCsv(context) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SettingsNavigationRow(
              icon = Icons.Default.FileDownload,
              title = "Export to JSON",
              subtitle = "Structured JSON backup of financial records",
              onClick = { viewModel.exportJson(context) }
            )
          }
        }
      }

      // 5. Reset & Danger Zone
      item {
        Text(
          text = "Database Management",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      item {
        FintechCard(modifier = Modifier.fillMaxWidth()) {
          Column {
            SettingsNavigationRow(
              icon = Icons.Default.Refresh,
              title = "Reset Demo Data",
              subtitle = "Reload sample transactions, accounts, and budgets",
              onClick = { showResetConfirmDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SettingsNavigationRow(
              icon = Icons.Default.DeleteForever,
              title = "Clear All Transactions",
              subtitle = "Permanently delete all transaction history",
              titleColor = ExpenseRed,
              onClick = { showClearConfirmDialog = true }
            )
          }
        }
      }

      // About footer
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "TakaTrack v1.0.0",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Offline-first Personal Finance & Expense Tracker",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
        }
      }
    }
  }

  // Currency Selection Dialog
  if (showCurrencyDialog) {
    AlertDialog(
      onDismissRequest = { showCurrencyDialog = false },
      title = { Text("Select Currency") },
      text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          items(AppCurrency.SUPPORTED, key = { it.code }) { curr ->
            val isSelected = curr.code == currencyCode
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                  viewModel.setCurrency(curr)
                  showCurrencyDialog = false
                },
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "${curr.name} (${curr.symbol})",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                  text = curr.code,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showCurrencyDialog = false }) {
          Text("Done")
        }
      }
    )
  }

  // Theme Selection Dialog
  if (showThemeDialog) {
    AlertDialog(
      onDismissRequest = { showThemeDialog = false },
      title = { Text("Select Theme") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(
            "SYSTEM" to "System Default",
            "LIGHT" to "Light Mode",
            "DARK" to "Dark Mode"
          ).forEach { (mode, label) ->
            val isSelected = appTheme == mode
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                  viewModel.setAppTheme(mode)
                  showThemeDialog = false
                },
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showThemeDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Manage Categories Sheet
  if (showManageCategoriesSheet) {
    ManageCategoriesSheet(
      viewModel = viewModel,
      onDismiss = { showManageCategoriesSheet = false }
    )
  }

  // Reset Confirmation Dialog
  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      title = { Text("Reset to Demo Data?") },
      text = { Text("This will replace current entries with realistic sample transactions, budgets, and accounts.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetToDemoData()
            showResetConfirmDialog = false
          }
        ) {
          Text("Reset")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Clear Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Clear All Data?") },
      text = { Text("This will permanently remove all transactions, budgets, and custom accounts.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllData()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
        ) {
          Text("Clear All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun SettingsNavigationRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  titleColor: Color = MaterialTheme.colorScheme.onSurface,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = titleColor
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForward,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
fun SettingsSwitchRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesSheet(
  viewModel: ExpenseViewModel,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val categories by viewModel.allCategories.collectAsStateWithLifecycle()

  var newCatName by remember { mutableStateOf("") }
  var newCatType by remember { mutableStateOf(TransactionType.EXPENSE) }
  var newCatColor by remember { mutableStateOf("#0D9488") }
  var newCatIcon by remember { mutableStateOf("receipt") }

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
        text = "Manage Categories",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Add category row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = newCatName,
          onValueChange = { newCatName = it },
          placeholder = { Text("Category name") },
          singleLine = true,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        )

        Button(
          onClick = {
            if (newCatName.isNotBlank()) {
              viewModel.addCategory(
                name = newCatName.trim(),
                type = newCatType,
                iconName = newCatIcon,
                colorHex = newCatColor
              )
              newCatName = ""
            }
          },
          enabled = newCatName.isNotBlank(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Add")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(350.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(categories, key = { it.id }) { cat ->
          FintechCard(modifier = Modifier.fillMaxWidth(), elevation = 0.5.dp) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(
                  iconName = cat.iconName,
                  colorHex = cat.colorHex,
                  size = 36.dp,
                  iconSize = 18.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = cat.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = cat.type.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              if (!cat.isDefault) {
                IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ExpenseRed,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
