package com.example.ui.screens.addexpense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionType
import com.example.ui.ExpenseViewModel
import com.example.ui.components.AccountIconBadge
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.FintechCard
import com.example.ui.components.parseColorHex
import com.example.ui.theme.ExpenseRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
  viewModel: ExpenseViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
  val categories by viewModel.allCategories.collectAsStateWithLifecycle()
  val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()

  // Filter only expense categories
  val expenseCategories = remember(categories) {
    categories.filter { it.type == TransactionType.EXPENSE }
  }

  // Form State
  var amountText by remember { mutableStateOf("") }
  var selectedCategoryId by remember(expenseCategories) {
    mutableStateOf(expenseCategories.firstOrNull()?.id ?: "")
  }
  var selectedAccountId by remember(accounts) {
    mutableStateOf(accounts.firstOrNull()?.id ?: "")
  }
  var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
  var description by remember { mutableStateOf("") }

  // Validation Errors State
  var amountError by remember { mutableStateOf<String?>(null) }
  var categoryError by remember { mutableStateOf<String?>(null) }
  var descriptionError by remember { mutableStateOf<String?>(null) }

  // Date Picker Dialog State
  var showDatePicker by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val scrollState = rememberScrollState()

  val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US) }
  val quickDateFormatter = remember { SimpleDateFormat("yyyyMMdd", Locale.US) }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .imePadding(),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Add Expense",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("btn_back_add_expense")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding(),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
      ) {
        Box(modifier = Modifier.padding(16.dp)) {
          Button(
            onClick = {
              // Validate inputs
              var hasError = false

              val parsedAmount = amountText.toDoubleOrNull()
              if (parsedAmount == null || parsedAmount <= 0.0) {
                amountError = "Please enter an amount greater than 0"
                hasError = true
              } else {
                amountError = null
              }

              if (selectedCategoryId.isBlank()) {
                categoryError = "Please select a category"
                hasError = true
              } else {
                categoryError = null
              }

              if (description.trim().isBlank()) {
                descriptionError = "Please enter a description"
                hasError = true
              } else {
                descriptionError = null
              }

              if (!hasError && parsedAmount != null) {
                val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }
                val paymentMethod = selectedAccount?.name ?: "Cash"

                viewModel.saveTransaction(
                  id = null,
                  type = TransactionType.EXPENSE,
                  amount = parsedAmount,
                  categoryId = selectedCategoryId,
                  accountId = selectedAccountId,
                  merchant = description.trim(),
                  date = selectedDateMillis,
                  notes = description.trim(),
                  paymentMethod = paymentMethod
                )

                coroutineScope.launch {
                  snackbarHostState.showSnackbar("Expense saved successfully!")
                  delay(250)
                  onNavigateBack()
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("btn_save_expense"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            )
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Save Expense",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(scrollState)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. AMOUNT SECTION
      FintechCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (amountError != null) ExpenseRed else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Expense Amount",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
              if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                amountText = input
                if (amountError != null) amountError = null
              }
            },
            placeholder = {
              Text(
                text = "0.00",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontSize = 32.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
              )
            },
            leadingIcon = {
              Text(
                text = currencySymbol,
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontSize = 28.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
              )
            },
            isError = amountError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(
              fontSize = 32.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              errorContainerColor = ExpenseRed.copy(alpha = 0.08f),
              unfocusedBorderColor = Color.Transparent,
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              errorBorderColor = ExpenseRed
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_expense_amount")
          )

          // Validation Error Message
          AnimatedVisibility(
            visible = amountError != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Row(
              modifier = Modifier.padding(top = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = ExpenseRed,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = amountError.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = ExpenseRed
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Quick Amount Chips
          Text(
            text = "Quick Add:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(100.0, 500.0, 1000.0, 2000.0, 5000.0).forEach { inc ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    val current = amountText.toDoubleOrNull() ?: 0.0
                    val updated = current + inc
                    amountText = if (updated % 1.0 == 0.0) updated.toLong().toString() else String.format(Locale.US, "%.2f", updated)
                    if (amountError != null) amountError = null
                  }
              ) {
                Text(
                  text = "+$currencySymbol${inc.toInt()}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      // 2. CATEGORY SECTION
      FintechCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (categoryError != null) ExpenseRed else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Category",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val selectedCat = expenseCategories.firstOrNull { it.id == selectedCategoryId }
            if (selectedCat != null) {
              Text(
                text = selectedCat.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Categories Grid
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            expenseCategories.forEach { cat ->
              val isSelected = cat.id == selectedCategoryId
              val catColor = parseColorHex(cat.colorHex)

              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) catColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                  .clip(RoundedCornerShape(14.dp))
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) catColor else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                  )
                  .clickable {
                    selectedCategoryId = cat.id
                    if (categoryError != null) categoryError = null
                  }
                  .testTag("category_item_${cat.id}")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  CategoryIconBadge(
                    iconName = cat.iconName,
                    colorHex = cat.colorHex,
                    size = 32.dp,
                    iconSize = 16.dp
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = cat.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                      modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(catColor),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          // Validation Error Message
          AnimatedVisibility(
            visible = categoryError != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Row(
              modifier = Modifier.padding(top = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = ExpenseRed,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = categoryError.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = ExpenseRed
              )
            }
          }
        }
      }

      // 3. DATE SECTION
      FintechCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Expense Date",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Clickable Date Field
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable { showDatePicker = true }
              .testTag("btn_expense_date")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = dateFormatter.format(Date(selectedDateMillis)),
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Text(
                text = "Change",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Quick Date Chips: Today, Yesterday
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val now = Calendar.getInstance()
            val todayStr = quickDateFormatter.format(now.time)
            now.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = quickDateFormatter.format(now.time)
            val selectedStr = quickDateFormatter.format(Date(selectedDateMillis))

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selectedStr == todayStr) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  selectedDateMillis = System.currentTimeMillis()
                }
            ) {
              Text(
                text = "Today",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selectedStr == todayStr) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedStr == todayStr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selectedStr == yesterdayStr) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  val cal = Calendar.getInstance()
                  cal.add(Calendar.DAY_OF_YEAR, -1)
                  selectedDateMillis = cal.timeInMillis
                }
            ) {
              Text(
                text = "Yesterday",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selectedStr == yesterdayStr) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedStr == yesterdayStr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // 4. DESCRIPTION & NOTES SECTION
      FintechCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (descriptionError != null) ExpenseRed else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Description",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = description,
            onValueChange = { input ->
              description = input
              if (descriptionError != null) descriptionError = null
            },
            placeholder = {
              Text(
                text = "e.g. Grocery shopping, Starbucks, Taxi ride",
                style = MaterialTheme.typography.bodyMedium
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            isError = descriptionError != null,
            singleLine = false,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              errorContainerColor = ExpenseRed.copy(alpha = 0.08f),
              unfocusedBorderColor = Color.Transparent,
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              errorBorderColor = ExpenseRed
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_expense_description")
          )

          // Validation Error Message
          AnimatedVisibility(
            visible = descriptionError != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Row(
              modifier = Modifier.padding(top = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = ExpenseRed,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = descriptionError.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = ExpenseRed
              )
            }
          }
        }
      }

      // 5. ACCOUNT / WALLET PICKER
      FintechCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Payment Account",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            accounts.forEach { acc ->
              val isSelected = acc.id == selectedAccountId
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .border(
                    width = if (isSelected) 1.5.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                  )
                  .clickable { selectedAccountId = acc.id }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  AccountIconBadge(
                    iconName = acc.iconName,
                    colorHex = acc.colorHex,
                    size = 28.dp,
                    iconSize = 14.dp
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = acc.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // DatePickerDialog
  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = selectedDateMillis
    )

    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let {
              selectedDateMillis = it
            }
            showDatePicker = false
          }
        ) {
          Text("OK")
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
