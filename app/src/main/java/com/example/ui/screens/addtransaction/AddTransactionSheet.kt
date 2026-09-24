package com.example.ui.screens.addtransaction

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entity.TransactionType
import com.example.domain.model.TransactionItem
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.parseColorHex
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
  viewModel: ExpenseViewModel,
  editingTransaction: TransactionItem? = null,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val categories by viewModel.allCategories.collectAsStateWithLifecycle()
  val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  var transactionType by remember {
    mutableStateOf(editingTransaction?.type ?: TransactionType.EXPENSE)
  }

  var amountText by remember {
    mutableStateOf(
      if (editingTransaction != null) {
        if (editingTransaction.amount % 1.0 == 0.0) {
          editingTransaction.amount.toLong().toString()
        } else {
          editingTransaction.amount.toString()
        }
      } else ""
    )
  }

  val filteredCategories = remember(categories, transactionType) {
    if (transactionType == TransactionType.TRANSFER) {
      categories
    } else {
      categories.filter { it.type == transactionType }
    }
  }

  var selectedCategoryId by remember(editingTransaction, filteredCategories) {
    mutableStateOf(
      editingTransaction?.categoryId
        ?: filteredCategories.firstOrNull()?.id
        ?: ""
    )
  }

  var merchant by remember {
    mutableStateOf(editingTransaction?.merchant ?: "")
  }

  var selectedAccountId by remember(accounts) {
    mutableStateOf(
      editingTransaction?.accountId
        ?: accounts.firstOrNull()?.id
        ?: ""
    )
  }

  var toAccountId by remember(accounts) {
    mutableStateOf(
      editingTransaction?.toAccountId
        ?: accounts.getOrNull(1)?.id
        ?: accounts.firstOrNull()?.id
        ?: ""
    )
  }

  var selectedDateMillis by remember {
    mutableLongStateOf(editingTransaction?.date ?: System.currentTimeMillis())
  }

  var notes by remember {
    mutableStateOf(editingTransaction?.notes ?: "")
  }

  var paymentMethod by remember {
    mutableStateOf(editingTransaction?.paymentMethod ?: "Cash")
  }

  var attachmentUriString by remember {
    mutableStateOf(editingTransaction?.attachmentUri)
  }

  var isRecurring by remember {
    mutableStateOf(editingTransaction?.isRecurring ?: false)
  }

  var showDatePicker by remember { mutableStateOf(false) }
  var accountDropdownExpanded by remember { mutableStateOf(false) }
  var toAccountDropdownExpanded by remember { mutableStateOf(false) }

  // Photo picker for receipt attachment (Zero permission, fully compliant with Play policies)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      runCatching {
        // Copy to internal app files
        val inputStream = context.contentResolver.openInputStream(uri)
        val receiptsDir = File(context.filesDir, "receipts")
        if (!receiptsDir.exists()) receiptsDir.mkdirs()
        val destFile = File(receiptsDir, "receipt_${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(destFile)
        inputStream?.use { input ->
          outputStream.use { output ->
            input.copyTo(output)
          }
        }
        attachmentUriString = destFile.absolutePath
      }
    }
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
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .padding(bottom = 32.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (editingTransaction == null) "New Transaction" else "Edit Transaction",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDismiss) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Segmented Type Selector [Expense | Income | Transfer]
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        listOf(
          TransactionType.EXPENSE to "Expense",
          TransactionType.INCOME to "Income",
          TransactionType.TRANSFER to "Transfer"
        ).forEach { (type, label) ->
          val isSelected = transactionType == type
          val activeColor = when (type) {
            TransactionType.EXPENSE -> ExpenseRed
            TransactionType.INCOME -> IncomeGreen
            TransactionType.TRANSFER -> TransferBlue
          }

          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                transactionType = type
                // update default selected category
                val validCats = if (type == TransactionType.TRANSFER) categories else categories.filter { it.type == type }
                if (validCats.none { it.id == selectedCategoryId }) {
                  selectedCategoryId = validCats.firstOrNull()?.id ?: ""
                }
              },
            color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
            shadowElevation = if (isSelected) 2.dp else 0.dp,
            shape = RoundedCornerShape(12.dp)
          ) {
            Box(
              modifier = Modifier.padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Amount Input (Big display)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = currencySymbol,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = when (transactionType) {
              TransactionType.EXPENSE -> ExpenseRed
              TransactionType.INCOME -> IncomeGreen
              TransactionType.TRANSFER -> TransferBlue
            }
          )
          Spacer(modifier = Modifier.width(8.dp))
          OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
              // Accept digits and at most one decimal point
              if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                amountText = input
              }
            },
            placeholder = {
              Text(
                text = "0",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
              )
            },
            textStyle = MaterialTheme.typography.displayMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Decimal,
              imeAction = ImeAction.Next
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent,
              disabledBorderColor = Color.Transparent
            ),
            modifier = Modifier.testTag("input_amount")
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Category Selection (Only for Expense & Income)
      if (transactionType != TransactionType.TRANSFER) {
        Text(
          text = "Category",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          filteredCategories.forEach { cat ->
            val isSelected = selectedCategoryId == cat.id
            val catColor = parseColorHex(cat.colorHex)

            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable { selectedCategoryId = cat.id }
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) catColor else MaterialTheme.colorScheme.outlineVariant,
                  shape = RoundedCornerShape(14.dp)
                ),
              color = if (isSelected) catColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                CategoryIconBadge(
                  iconName = cat.iconName,
                  colorHex = cat.colorHex,
                  size = 28.dp,
                  iconSize = 16.dp,
                  shapeCorner = 8.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = cat.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // Title / Merchant Field
      Text(
        text = if (transactionType == TransactionType.INCOME) "Source / Payer" else if (transactionType == TransactionType.TRANSFER) "Transfer Note" else "Merchant / Place",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = merchant,
        onValueChange = { merchant = it },
        placeholder = {
          Text(
            text = when (transactionType) {
              TransactionType.EXPENSE -> "e.g. Supermarket, Coffee, Uber"
              TransactionType.INCOME -> "e.g. Salary, Client payment"
              TransactionType.TRANSFER -> "e.g. ATM withdrawal, Card payment"
            }
          )
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_merchant"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Accounts Row (Source Account & Target Account if transfer)
      if (transactionType == TransactionType.TRANSFER) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "From Account",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            AccountSelectorMenu(
              accounts = accounts,
              selectedAccountId = selectedAccountId,
              onSelect = { selectedAccountId = it }
            )
          }

          Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .padding(top = 22.dp)
              .size(24.dp)
          )

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "To Account",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            AccountSelectorMenu(
              accounts = accounts,
              selectedAccountId = toAccountId,
              onSelect = { toAccountId = it }
            )
          }
        }
      } else {
        Text(
          text = "Account / Wallet",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        AccountSelectorMenu(
          accounts = accounts,
          selectedAccountId = selectedAccountId,
          onSelect = { selectedAccountId = it }
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Date Selection
      Text(
        text = "Date",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))

      val sdf = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.US) }
      val formattedDate = remember(selectedDateMillis) { sdf.format(Date(selectedDateMillis)) }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .clickable { showDatePicker = true }
          .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          val now = System.currentTimeMillis()
          val dayMillis = 24L * 3600 * 1000

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clickable { selectedDateMillis = now }
          ) {
            Text(
              text = "Today",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clickable { selectedDateMillis = now - dayMillis }
          ) {
            Text(
              text = "Yesterday",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Payment Method Chips (Cash, Card, Digital, Bank)
      Text(
        text = "Payment Method",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Cash", "Credit Card", "Debit Card", "bKash", "Nagad", "Bank Transfer").forEach { method ->
          val isSelected = paymentMethod == method
          FilterChip(
            selected = isSelected,
            onClick = { paymentMethod = method },
            label = { Text(method) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Notes
      Text(
        text = "Note / Memo (Optional)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        placeholder = { Text("Add any details or tags...") },
        maxLines = 3,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_notes"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Receipt Attachment
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AttachFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Receipt Photo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (attachmentUriString != null) {
          TextButton(onClick = { attachmentUriString = null }) {
            Text("Remove", color = ExpenseRed)
          }
        } else {
          TextButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            }
          ) {
            Text("Attach Receipt")
          }
        }
      }

      if (attachmentUriString != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
          AsyncImage(
            model = File(attachmentUriString!!),
            contentDescription = "Receipt Preview",
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Recurring Toggle
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Repeat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Recurring Monthly",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Repeat this transaction automatically",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Switch(
          checked = isRecurring,
          onCheckedChange = { isRecurring = it }
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Save Button
      val amountVal = amountText.toDoubleOrNull() ?: 0.0
      val isValid = amountVal > 0.0

      Button(
        onClick = {
          if (isValid) {
            val fallbackCat = filteredCategories.firstOrNull()?.id ?: "cat_other_exp"
            viewModel.saveTransaction(
              id = editingTransaction?.id,
              type = transactionType,
              amount = amountVal,
              categoryId = if (transactionType == TransactionType.TRANSFER) "cat_other_exp" else selectedCategoryId.ifBlank { fallbackCat },
              accountId = selectedAccountId,
              toAccountId = if (transactionType == TransactionType.TRANSFER) toAccountId else null,
              merchant = merchant,
              date = selectedDateMillis,
              notes = notes,
              paymentMethod = paymentMethod,
              attachmentUri = attachmentUriString,
              isRecurring = isRecurring,
              recurringInterval = if (isRecurring) "MONTHLY" else null
            )
            onDismiss()
          }
        },
        enabled = isValid,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_save_transaction"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (editingTransaction == null) "Save Transaction" else "Update Transaction",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  // Date Picker Dialog
  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = selectedDateMillis
    )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
            showDatePicker = false
          }
        ) {
          Text("Select")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancel")
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectorMenu(
  accounts: List<com.example.domain.model.AccountItem>,
  selectedAccountId: String,
  onSelect: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId } ?: accounts.firstOrNull()

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = it }
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .menuAnchor()
        .clip(RoundedCornerShape(14.dp))
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant,
          shape = RoundedCornerShape(14.dp)
        )
        .clickable { expanded = true },
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
      shape = RoundedCornerShape(14.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = selectedAccount?.name ?: "Select Account",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
      }
    }

    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      accounts.forEach { acc ->
        DropdownMenuItem(
          text = { Text(acc.name) },
          onClick = {
            onSelect(acc.id)
            expanded = false
          }
        )
      }
    }
  }
}
