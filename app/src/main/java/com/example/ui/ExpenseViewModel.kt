package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppCurrency
import com.example.data.local.DataStoreManager
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetPeriod
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.RecurringFrequency
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.repository.DemoData
import com.example.data.repository.ExpenseRepository
import com.example.domain.model.AccountItem
import com.example.domain.model.BudgetItem
import com.example.domain.model.CategoryItem
import com.example.domain.model.FinancialSummary
import com.example.domain.model.RecurringItem
import com.example.domain.model.SpendingTrendPoint
import com.example.domain.model.TransactionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class ExpenseViewModel(
  private val repository: ExpenseRepository,
  private val dataStoreManager: DataStoreManager
) : ViewModel() {

  init {
    viewModelScope.launch {
      repository.initializeDefaultDataIfNeeded()
    }
  }

  val currencySymbol: StateFlow<String> = dataStoreManager.currencySymbol
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "৳")

  val currencyCode: StateFlow<String> = dataStoreManager.currencyCode
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BDT")

  val appTheme: StateFlow<String> = dataStoreManager.appTheme
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

  val isOnboardingCompleted: StateFlow<Boolean> = dataStoreManager.isOnboardingCompleted
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val dailyReminderEnabled: StateFlow<Boolean> = dataStoreManager.dailyReminderEnabled
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  val budgetAlertsEnabled: StateFlow<Boolean> = dataStoreManager.budgetAlertsEnabled
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  val financialSummary: StateFlow<FinancialSummary> = repository.financialSummary
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      FinancialSummary(
        totalBalance = 0.0,
        incomeThisMonth = 0.0,
        expensesThisMonth = 0.0,
        savingsThisMonth = 0.0,
        savingsRatePercent = 0.0,
        topCategories = emptyList(),
        monthlyBudgetAmount = 50000.0,
        monthlyBudgetSpent = 0.0,
        monthlyBudgetRemaining = 50000.0,
        monthlyBudgetPercentage = 0.0
      )
    )

  val allTransactions: StateFlow<List<TransactionItem>> = repository.allTransactions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allCategories: StateFlow<List<CategoryItem>> = repository.allCategories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allAccounts: StateFlow<List<AccountItem>> = repository.allAccounts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allBudgets: StateFlow<List<BudgetItem>> = repository.allBudgets
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allRecurring: StateFlow<List<RecurringItem>> = repository.allRecurring
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Trend timeframe for Dashboard: "Week", "Month", "Year"
  private val _dashboardTrendTimeframe = MutableStateFlow("Week")
  val dashboardTrendTimeframe: StateFlow<String> = _dashboardTrendTimeframe.asStateFlow()

  fun setDashboardTrendTimeframe(timeframe: String) {
    _dashboardTrendTimeframe.value = timeframe
  }

  // Add or edit transaction
  fun saveTransaction(
    id: String? = null,
    type: TransactionType,
    amount: Double,
    categoryId: String,
    accountId: String,
    toAccountId: String? = null,
    merchant: String,
    date: Long,
    notes: String,
    paymentMethod: String,
    attachmentUri: String? = null,
    isRecurring: Boolean = false,
    recurringInterval: String? = null
  ) {
    viewModelScope.launch {
      val entity = TransactionEntity(
        id = id ?: UUID.randomUUID().toString(),
        type = type,
        amount = amount,
        categoryId = categoryId,
        accountId = accountId,
        toAccountId = toAccountId,
        merchant = merchant.ifBlank {
          when (type) {
            TransactionType.EXPENSE -> "Expense"
            TransactionType.INCOME -> "Income"
            TransactionType.TRANSFER -> "Transfer"
          }
        },
        date = date,
        notes = notes,
        paymentMethod = paymentMethod,
        attachmentUri = attachmentUri,
        isRecurring = isRecurring,
        recurringInterval = recurringInterval,
        updatedAt = System.currentTimeMillis()
      )
      repository.insertTransaction(entity)

      // If user toggled recurring on a new expense, create recurring entry too
      if (isRecurring && id == null) {
        val nextDue = date + (30L * 24 * 60 * 60 * 1000)
        repository.insertRecurring(
          RecurringTransactionEntity(
            id = UUID.randomUUID().toString(),
            type = type,
            amount = amount,
            categoryId = categoryId,
            accountId = accountId,
            merchant = merchant,
            notes = notes,
            frequency = RecurringFrequency.MONTHLY,
            nextDueDate = nextDue,
            isActive = true
          )
        )
      }
    }
  }

  fun deleteTransaction(id: String) {
    viewModelScope.launch {
      repository.deleteTransaction(id)
    }
  }

  fun duplicateTransaction(tx: TransactionItem) {
    viewModelScope.launch {
      val duplicate = TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = tx.type,
        amount = tx.amount,
        categoryId = tx.categoryId,
        accountId = tx.accountId,
        toAccountId = tx.toAccountId,
        merchant = tx.merchant,
        date = System.currentTimeMillis(),
        notes = tx.notes,
        paymentMethod = tx.paymentMethod,
        attachmentUri = tx.attachmentUri,
        isRecurring = false,
        recurringInterval = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
      repository.insertTransaction(duplicate)
    }
  }

  fun addCategory(
    name: String,
    type: TransactionType,
    iconName: String,
    colorHex: String
  ) {
    viewModelScope.launch {
      val entity = CategoryEntity(
        id = "cat_" + UUID.randomUUID().toString().take(8),
        name = name,
        type = type,
        iconName = iconName,
        colorHex = colorHex,
        isDefault = false
      )
      repository.insertCategory(entity)
    }
  }

  fun deleteCategory(id: String) {
    viewModelScope.launch {
      repository.deleteCategory(id)
    }
  }

  fun addAccount(
    name: String,
    type: AccountType,
    initialBalance: Double,
    colorHex: String,
    iconName: String
  ) {
    viewModelScope.launch {
      val entity = AccountEntity(
        id = "acc_" + UUID.randomUUID().toString().take(8),
        name = name,
        type = type,
        initialBalance = initialBalance,
        colorHex = colorHex,
        iconName = iconName
      )
      repository.insertAccount(entity)
    }
  }

  fun deleteAccount(id: String) {
    viewModelScope.launch {
      repository.deleteAccount(id)
    }
  }

  fun setBudget(
    categoryId: String?,
    amount: Double,
    period: BudgetPeriod = BudgetPeriod.MONTHLY
  ) {
    viewModelScope.launch {
      val currentMonth = DemoData.getCurrentMonthYear()
      val id = if (categoryId == null) "b_overall" else "b_$categoryId"
      val entity = BudgetEntity(
        id = id,
        categoryId = categoryId,
        amount = amount,
        period = period,
        monthYear = currentMonth,
        warningThresholdPercent = 80.0
      )
      repository.insertBudget(entity)
    }
  }

  fun deleteBudget(id: String) {
    viewModelScope.launch {
      repository.deleteBudget(id)
    }
  }

  fun addRecurring(
    type: TransactionType,
    amount: Double,
    categoryId: String,
    accountId: String,
    merchant: String,
    notes: String,
    frequency: RecurringFrequency,
    nextDueDate: Long
  ) {
    viewModelScope.launch {
      val entity = RecurringTransactionEntity(
        id = "rec_" + UUID.randomUUID().toString().take(8),
        type = type,
        amount = amount,
        categoryId = categoryId,
        accountId = accountId,
        merchant = merchant,
        notes = notes,
        frequency = frequency,
        nextDueDate = nextDueDate,
        isActive = true
      )
      repository.insertRecurring(entity)
    }
  }

  fun toggleRecurringActive(item: RecurringItem) {
    viewModelScope.launch {
      val updated = RecurringTransactionEntity(
        id = item.id,
        type = item.type,
        amount = item.amount,
        categoryId = item.categoryId,
        accountId = item.accountId,
        merchant = item.merchant,
        notes = item.notes,
        frequency = item.frequency,
        nextDueDate = item.nextDueDate,
        isActive = !item.isActive
      )
      repository.updateRecurring(updated)
    }
  }

  fun skipNextRecurring(item: RecurringItem) {
    viewModelScope.launch {
      val dayMillis = 24L * 60 * 60 * 1000
      val increment = when (item.frequency) {
        RecurringFrequency.DAILY -> dayMillis
        RecurringFrequency.WEEKLY -> 7 * dayMillis
        RecurringFrequency.MONTHLY -> 30 * dayMillis
        RecurringFrequency.YEARLY -> 365 * dayMillis
      }
      val updated = RecurringTransactionEntity(
        id = item.id,
        type = item.type,
        amount = item.amount,
        categoryId = item.categoryId,
        accountId = item.accountId,
        merchant = item.merchant,
        notes = item.notes,
        frequency = item.frequency,
        nextDueDate = item.nextDueDate + increment,
        isActive = item.isActive
      )
      repository.updateRecurring(updated)
    }
  }

  fun deleteRecurring(id: String) {
    viewModelScope.launch {
      repository.deleteRecurring(id)
    }
  }

  fun setCurrency(currency: AppCurrency) {
    viewModelScope.launch {
      dataStoreManager.setCurrency(currency.code, currency.symbol)
    }
  }

  fun setAppTheme(theme: String) {
    viewModelScope.launch {
      dataStoreManager.setAppTheme(theme)
    }
  }

  fun setDailyReminder(enabled: Boolean) {
    viewModelScope.launch {
      dataStoreManager.setDailyReminder(enabled)
    }
  }

  fun setBudgetAlerts(enabled: Boolean) {
    viewModelScope.launch {
      dataStoreManager.setBudgetAlerts(enabled)
    }
  }

  fun completeOnboarding(currency: AppCurrency, startingBalance: Double, monthlyBudget: Double) {
    viewModelScope.launch {
      dataStoreManager.setCurrency(currency.code, currency.symbol)
      dataStoreManager.setOnboardingCompleted(true)
      if (monthlyBudget > 0) {
        setBudget(null, monthlyBudget)
      }
      if (startingBalance > 0) {
        val accounts = repository.allAccounts.first()
        val cashAcc = accounts.firstOrNull { it.type == AccountType.CASH }
        if (cashAcc != null) {
          repository.updateAccount(
            AccountEntity(
              id = cashAcc.id,
              name = cashAcc.name,
              type = cashAcc.type,
              initialBalance = startingBalance,
              colorHex = cashAcc.colorHex,
              iconName = cashAcc.iconName
            )
          )
        }
      }
    }
  }

  fun resetToDemoData() {
    viewModelScope.launch {
      repository.resetToDemoData()
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData()
    }
  }

  fun exportCsv(context: Context) {
    viewModelScope.launch {
      val txs = repository.allTransactions.first()
      val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
      val csvBuilder = StringBuilder()
      csvBuilder.append("Date,Type,Amount,Category,Merchant,Account,ToAccount,PaymentMethod,Notes\n")

      for (tx in txs) {
        val dateStr = sdf.format(Date(tx.date))
        val typeStr = tx.type.name
        val amountStr = tx.amount.toString()
        val categoryStr = "\"${tx.categoryName.replace("\"", "\"\"")}\""
        val merchantStr = "\"${tx.merchant.replace("\"", "\"\"")}\""
        val accountStr = "\"${tx.accountName.replace("\"", "\"\"")}\""
        val toAccountStr = "\"${(tx.toAccountName ?: "").replace("\"", "\"\"")}\""
        val paymentMethodStr = "\"${tx.paymentMethod.replace("\"", "\"\"")}\""
        val notesStr = "\"${tx.notes.replace("\"", "\"\"")}\""

        csvBuilder.append("$dateStr,$typeStr,$amountStr,$categoryStr,$merchantStr,$accountStr,$toAccountStr,$paymentMethodStr,$notesStr\n")
      }

      val file = File(context.cacheDir, "takatrack_export.csv")
      file.writeText(csvBuilder.toString())

      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "TakaTrack Financial Export")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Export Transactions CSV"))
    }
  }

  fun exportJson(context: Context) {
    viewModelScope.launch {
      val txs = repository.allTransactions.first()
      val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
      val jsonList = txs.map { tx ->
        """  {
    "id": "${tx.id}",
    "date": "${sdf.format(Date(tx.date))}",
    "type": "${tx.type.name}",
    "amount": ${tx.amount},
    "category": "${tx.categoryName}",
    "merchant": "${tx.merchant}",
    "account": "${tx.accountName}",
    "notes": "${tx.notes}"
  }"""
      }
      val jsonContent = "[\n${jsonList.joinToString(",\n")}\n]"
      val file = File(context.cacheDir, "takatrack_export.json")
      file.writeText(jsonContent)

      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "TakaTrack Financial Export JSON")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Export Transactions JSON"))
    }
  }
}

class ExpenseViewModelFactory(
  private val repository: ExpenseRepository,
  private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
      return ExpenseViewModel(repository, dataStoreManager) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
