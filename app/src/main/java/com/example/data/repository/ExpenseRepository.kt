package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetPeriod
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.domain.model.AccountItem
import com.example.domain.model.BudgetItem
import com.example.domain.model.BudgetStatus
import com.example.domain.model.CategoryItem
import com.example.domain.model.CategorySpending
import com.example.domain.model.FinancialSummary
import com.example.domain.model.RecurringItem
import com.example.domain.model.SpendingTrendPoint
import com.example.domain.model.TransactionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseRepository(private val database: AppDatabase) {

  private val transactionDao = database.transactionDao()
  private val categoryDao = database.categoryDao()
  private val accountDao = database.accountDao()
  private val budgetDao = database.budgetDao()
  private val recurringDao = database.recurringTransactionDao()

  suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
    val existingCategories = categoryDao.getAllCategories().first()
    if (existingCategories.isEmpty()) {
      categoryDao.insertCategories(DemoData.defaultCategories)
      accountDao.insertAccounts(DemoData.defaultAccounts)
      budgetDao.insertBudgets(DemoData.getSampleBudgets())
      recurringDao.insertRecurringList(DemoData.getSampleRecurring())
      transactionDao.insertTransactions(DemoData.getSampleTransactions())
    }
  }

  suspend fun resetToDemoData() = withContext(Dispatchers.IO) {
    transactionDao.clearAll()
    categoryDao.clearAll()
    accountDao.clearAll()
    budgetDao.clearAll()
    recurringDao.clearAll()

    categoryDao.insertCategories(DemoData.defaultCategories)
    accountDao.insertAccounts(DemoData.defaultAccounts)
    budgetDao.insertBudgets(DemoData.getSampleBudgets())
    recurringDao.insertRecurringList(DemoData.getSampleRecurring())
    transactionDao.insertTransactions(DemoData.getSampleTransactions())
  }

  suspend fun clearAllData() = withContext(Dispatchers.IO) {
    transactionDao.clearAll()
    categoryDao.clearAll()
    accountDao.clearAll()
    budgetDao.clearAll()
    recurringDao.clearAll()

    // re-insert essential base categories so user can create expenses
    categoryDao.insertCategories(DemoData.defaultCategories)
    accountDao.insertAccounts(listOf(DemoData.defaultAccounts[0]))
  }

  val allCategories: Flow<List<CategoryItem>> = categoryDao.getAllCategories().combine(
    kotlinx.coroutines.flow.flowOf(Unit)
  ) { list, _ ->
    list.map {
      CategoryItem(
        id = it.id,
        name = it.name,
        type = it.type,
        iconName = it.iconName,
        colorHex = it.colorHex,
        isDefault = it.isDefault
      )
    }
  }.flowOn(Dispatchers.Default)

  val allTransactions: Flow<List<TransactionItem>> = combine(
    transactionDao.getAllTransactions(),
    categoryDao.getAllCategories(),
    accountDao.getAllAccounts()
  ) { txList, catList, accList ->
    val catMap = catList.associateBy { it.id }
    val accMap = accList.associateBy { it.id }

    txList.map { tx ->
      val cat = catMap[tx.categoryId]
      val acc = accMap[tx.accountId]
      val toAcc = tx.toAccountId?.let { accMap[it] }

      TransactionItem(
        id = tx.id,
        type = tx.type,
        amount = tx.amount,
        categoryId = tx.categoryId,
        categoryName = cat?.name ?: "Other",
        categoryIcon = cat?.iconName ?: "receipt",
        categoryColorHex = cat?.colorHex ?: "#64748B",
        accountId = tx.accountId,
        accountName = acc?.name ?: "Account",
        toAccountId = tx.toAccountId,
        toAccountName = toAcc?.name,
        merchant = tx.merchant,
        date = tx.date,
        notes = tx.notes,
        paymentMethod = tx.paymentMethod,
        attachmentUri = tx.attachmentUri,
        isRecurring = tx.isRecurring,
        recurringInterval = tx.recurringInterval,
        createdAt = tx.createdAt
      )
    }
  }.flowOn(Dispatchers.Default)

  fun getTransactionById(id: String): Flow<TransactionItem?> = combine(
    transactionDao.getTransactionById(id),
    categoryDao.getAllCategories(),
    accountDao.getAllAccounts()
  ) { tx, catList, accList ->
    if (tx == null) return@combine null
    val catMap = catList.associateBy { it.id }
    val accMap = accList.associateBy { it.id }
    val cat = catMap[tx.categoryId]
    val acc = accMap[tx.accountId]
    val toAcc = tx.toAccountId?.let { accMap[it] }

    TransactionItem(
      id = tx.id,
      type = tx.type,
      amount = tx.amount,
      categoryId = tx.categoryId,
      categoryName = cat?.name ?: "Other",
      categoryIcon = cat?.iconName ?: "receipt",
      categoryColorHex = cat?.colorHex ?: "#64748B",
      accountId = tx.accountId,
      accountName = acc?.name ?: "Account",
      toAccountId = tx.toAccountId,
      toAccountName = toAcc?.name,
      merchant = tx.merchant,
      date = tx.date,
      notes = tx.notes,
      paymentMethod = tx.paymentMethod,
      attachmentUri = tx.attachmentUri,
      isRecurring = tx.isRecurring,
      recurringInterval = tx.recurringInterval,
      createdAt = tx.createdAt
    )
  }.flowOn(Dispatchers.Default)

  // Account Balances dynamically calculated
  // Balance = Initial Balance + Income - Expenses + Transfers In - Transfers Out
  val allAccounts: Flow<List<AccountItem>> = combine(
    accountDao.getAllAccounts(),
    transactionDao.getAllTransactions()
  ) { accounts, transactions ->
    accounts.map { acc ->
      var balance = acc.initialBalance
      for (tx in transactions) {
        when (tx.type) {
          TransactionType.INCOME -> {
            if (tx.accountId == acc.id) {
              balance += tx.amount
            }
          }
          TransactionType.EXPENSE -> {
            if (tx.accountId == acc.id) {
              balance -= tx.amount
            }
          }
          TransactionType.TRANSFER -> {
            if (tx.accountId == acc.id) {
              balance -= tx.amount
            }
            if (tx.toAccountId == acc.id) {
              balance += tx.amount
            }
          }
        }
      }
      AccountItem(
        id = acc.id,
        name = acc.name,
        type = acc.type,
        initialBalance = acc.initialBalance,
        currentBalance = balance,
        colorHex = acc.colorHex,
        iconName = acc.iconName
      )
    }
  }.flowOn(Dispatchers.Default)

  val financialSummary: Flow<FinancialSummary> = combine(
    transactionDao.getAllTransactions(),
    categoryDao.getAllCategories(),
    accountDao.getAllAccounts(),
    budgetDao.getAllBudgets()
  ) { txList, catList, accList, budgetList ->
    val catMap = catList.associateBy { it.id }

    // calculate current month boundaries
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfMonth = cal.timeInMillis

    cal.add(Calendar.MONTH, 1)
    val endOfMonth = cal.timeInMillis - 1

    var totalBalance = accList.sumOf { it.initialBalance }
    for (tx in txList) {
      when (tx.type) {
        TransactionType.INCOME -> totalBalance += tx.amount
        TransactionType.EXPENSE -> totalBalance -= tx.amount
        TransactionType.TRANSFER -> Unit // transfers don't change net worth
      }
    }

    var incomeThisMonth = 0.0
    var expensesThisMonth = 0.0
    val categorySpentMap = mutableMapOf<String, Double>()

    for (tx in txList) {
      if (tx.date in startOfMonth..endOfMonth) {
        when (tx.type) {
          TransactionType.INCOME -> incomeThisMonth += tx.amount
          TransactionType.EXPENSE -> {
            expensesThisMonth += tx.amount
            val current = categorySpentMap.getOrDefault(tx.categoryId, 0.0)
            categorySpentMap[tx.categoryId] = current + tx.amount
          }
          TransactionType.TRANSFER -> Unit
        }
      }
    }

    val savingsThisMonth = incomeThisMonth - expensesThisMonth
    val savingsRate = if (incomeThisMonth > 0) {
      ((savingsThisMonth / incomeThisMonth) * 100.0).coerceAtLeast(0.0)
    } else 0.0

    val topCategories = categorySpentMap.entries
      .sortedByDescending { it.value }
      .take(5)
      .map { (catId, spent) ->
        val cat = catMap[catId]
        val percentage = if (expensesThisMonth > 0) (spent / expensesThisMonth) * 100.0 else 0.0
        CategorySpending(
          categoryId = catId,
          categoryName = cat?.name ?: "Other",
          categoryIcon = cat?.iconName ?: "receipt",
          categoryColorHex = cat?.colorHex ?: "#64748B",
          totalSpent = spent,
          percentage = percentage
        )
      }

    val currentMonthStr = DemoData.getCurrentMonthYear()
    val overallBudget = budgetList.firstOrNull { it.categoryId == null && it.monthYear == currentMonthStr }
      ?: budgetList.firstOrNull { it.categoryId == null }

    val monthlyBudgetAmount = overallBudget?.amount ?: 50000.0
    val monthlyBudgetSpent = expensesThisMonth
    val monthlyBudgetRemaining = (monthlyBudgetAmount - monthlyBudgetSpent).coerceAtLeast(0.0)
    val monthlyBudgetPercentage = if (monthlyBudgetAmount > 0) {
      (monthlyBudgetSpent / monthlyBudgetAmount) * 100.0
    } else 0.0

    FinancialSummary(
      totalBalance = totalBalance,
      incomeThisMonth = incomeThisMonth,
      expensesThisMonth = expensesThisMonth,
      savingsThisMonth = savingsThisMonth,
      savingsRatePercent = savingsRate,
      topCategories = topCategories,
      monthlyBudgetAmount = monthlyBudgetAmount,
      monthlyBudgetSpent = monthlyBudgetSpent,
      monthlyBudgetRemaining = monthlyBudgetRemaining,
      monthlyBudgetPercentage = monthlyBudgetPercentage
    )
  }.flowOn(Dispatchers.Default)

  val allBudgets: Flow<List<BudgetItem>> = combine(
    budgetDao.getAllBudgets(),
    categoryDao.getAllCategories(),
    transactionDao.getAllTransactions()
  ) { budgets, categories, transactions ->
    val catMap = categories.associateBy { it.id }

    val cal = Calendar.getInstance()
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (maxDays - currentDay).coerceAtLeast(1)

    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val startOfMonth = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val endOfMonth = cal.timeInMillis - 1

    val currentMonthTxs = transactions.filter {
      it.type == TransactionType.EXPENSE && it.date in startOfMonth..endOfMonth
    }

    budgets.map { b ->
      val cat = b.categoryId?.let { catMap[it] }
      val spent = if (b.categoryId == null) {
        currentMonthTxs.sumOf { it.amount }
      } else {
        currentMonthTxs.filter { it.categoryId == b.categoryId }.sumOf { it.amount }
      }

      val remaining = (b.amount - spent).coerceAtLeast(0.0)
      val percentage = if (b.amount > 0) (spent / b.amount) * 100.0 else 0.0

      val status = when {
        spent >= b.amount -> BudgetStatus.EXCEEDED
        percentage >= 90.0 -> BudgetStatus.WARNING_90
        percentage >= 70.0 -> BudgetStatus.WARNING_70
        else -> BudgetStatus.SAFE
      }

      BudgetItem(
        id = b.id,
        categoryId = b.categoryId,
        categoryName = if (b.categoryId == null) "Overall Monthly Budget" else cat?.name ?: "Category",
        categoryIcon = if (b.categoryId == null) "account_balance_wallet" else cat?.iconName ?: "pie_chart",
        categoryColorHex = if (b.categoryId == null) "#0D9488" else cat?.colorHex ?: "#64748B",
        budgetAmount = b.amount,
        spentAmount = spent,
        remainingAmount = remaining,
        percentageUsed = percentage,
        period = b.period,
        monthYear = b.monthYear,
        status = status,
        daysRemainingInMonth = daysRemaining
      )
    }
  }.flowOn(Dispatchers.Default)

  val allRecurring: Flow<List<RecurringItem>> = combine(
    recurringDao.getAllRecurring(),
    categoryDao.getAllCategories(),
    accountDao.getAllAccounts()
  ) { recurringList, categories, accounts ->
    val catMap = categories.associateBy { it.id }
    val accMap = accounts.associateBy { it.id }

    recurringList.map { rec ->
      val cat = catMap[rec.categoryId]
      val acc = accMap[rec.accountId]

      RecurringItem(
        id = rec.id,
        type = rec.type,
        amount = rec.amount,
        categoryId = rec.categoryId,
        categoryName = cat?.name ?: "Category",
        categoryIcon = cat?.iconName ?: "receipt",
        categoryColorHex = cat?.colorHex ?: "#64748B",
        accountId = rec.accountId,
        accountName = acc?.name ?: "Account",
        merchant = rec.merchant,
        notes = rec.notes,
        frequency = rec.frequency,
        nextDueDate = rec.nextDueDate,
        isActive = rec.isActive
      )
    }
  }.flowOn(Dispatchers.Default)

  // Insights Data calculation for different timeframes
  suspend fun getSpendingTrend(
    timeframe: String,
    transactions: List<TransactionEntity>
  ): List<SpendingTrendPoint> = withContext(Dispatchers.Default) {
    val expenseTxs = transactions.filter { it.type == TransactionType.EXPENSE }
    val now = Calendar.getInstance()
    val sdfDay = SimpleDateFormat("EEE", Locale.US)
    val sdfDate = SimpleDateFormat("MMM d", Locale.US)
    val sdfMonth = SimpleDateFormat("MMM", Locale.US)

    when (timeframe) {
      "Week" -> {
        // Last 7 days
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
        // 4 weeks
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
          points.add(SpendingTrendPoint(label = "Wk ${4 - w}", amount = weekTotal, timestamp = start))
        }
        points
      }
      else -> {
        // Last 6 months
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

  // CRUD
  suspend fun insertTransaction(tx: TransactionEntity) = withContext(Dispatchers.IO) {
    transactionDao.insertTransaction(tx)
  }

  suspend fun updateTransaction(tx: TransactionEntity) = withContext(Dispatchers.IO) {
    transactionDao.updateTransaction(tx)
  }

  suspend fun deleteTransaction(id: String) = withContext(Dispatchers.IO) {
    transactionDao.deleteTransactionById(id)
  }

  suspend fun insertCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
    categoryDao.insertCategory(category)
  }

  suspend fun deleteCategory(id: String) = withContext(Dispatchers.IO) {
    categoryDao.deleteCategoryById(id)
  }

  suspend fun insertAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
    accountDao.insertAccount(account)
  }

  suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
    accountDao.updateAccount(account)
  }

  suspend fun deleteAccount(id: String) = withContext(Dispatchers.IO) {
    accountDao.deleteAccountById(id)
  }

  suspend fun insertBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
    budgetDao.insertBudget(budget)
  }

  suspend fun deleteBudget(id: String) = withContext(Dispatchers.IO) {
    budgetDao.deleteBudgetById(id)
  }

  suspend fun insertRecurring(item: RecurringTransactionEntity) = withContext(Dispatchers.IO) {
    recurringDao.insertRecurring(item)
  }

  suspend fun updateRecurring(item: RecurringTransactionEntity) = withContext(Dispatchers.IO) {
    recurringDao.updateRecurring(item)
  }

  suspend fun deleteRecurring(id: String) = withContext(Dispatchers.IO) {
    recurringDao.deleteRecurringById(id)
  }
}
