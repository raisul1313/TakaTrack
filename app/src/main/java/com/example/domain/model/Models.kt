package com.example.domain.model

import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BudgetPeriod
import com.example.data.local.entity.RecurringFrequency
import com.example.data.local.entity.TransactionType

data class TransactionItem(
  val id: String,
  val type: TransactionType,
  val amount: Double,
  val categoryId: String,
  val categoryName: String,
  val categoryIcon: String,
  val categoryColorHex: String,
  val accountId: String,
  val accountName: String,
  val toAccountId: String? = null,
  val toAccountName: String? = null,
  val merchant: String,
  val date: Long,
  val notes: String = "",
  val paymentMethod: String = "Cash",
  val attachmentUri: String? = null,
  val isRecurring: Boolean = false,
  val recurringInterval: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

data class CategoryItem(
  val id: String,
  val name: String,
  val type: TransactionType,
  val iconName: String,
  val colorHex: String,
  val isDefault: Boolean = false
)

data class AccountItem(
  val id: String,
  val name: String,
  val type: AccountType,
  val initialBalance: Double,
  val currentBalance: Double,
  val colorHex: String,
  val iconName: String
)

enum class BudgetStatus {
  SAFE,
  WARNING_70,
  WARNING_90,
  EXCEEDED
}

data class BudgetItem(
  val id: String,
  val categoryId: String?,
  val categoryName: String = "Overall Monthly",
  val categoryIcon: String = "pie_chart",
  val categoryColorHex: String = "#0D9488",
  val budgetAmount: Double,
  val spentAmount: Double,
  val remainingAmount: Double,
  val percentageUsed: Double,
  val period: BudgetPeriod,
  val monthYear: String,
  val status: BudgetStatus,
  val daysRemainingInMonth: Int
)

data class RecurringItem(
  val id: String,
  val type: TransactionType,
  val amount: Double,
  val categoryId: String,
  val categoryName: String,
  val categoryIcon: String,
  val categoryColorHex: String,
  val accountId: String,
  val accountName: String,
  val merchant: String,
  val notes: String,
  val frequency: RecurringFrequency,
  val nextDueDate: Long,
  val isActive: Boolean
)

data class CategorySpending(
  val categoryId: String,
  val categoryName: String,
  val categoryIcon: String,
  val categoryColorHex: String,
  val totalSpent: Double,
  val percentage: Double
)

data class SpendingTrendPoint(
  val label: String,
  val amount: Double,
  val timestamp: Long = 0L
)

data class FinancialSummary(
  val totalBalance: Double,
  val incomeThisMonth: Double,
  val expensesThisMonth: Double,
  val savingsThisMonth: Double,
  val savingsRatePercent: Double,
  val topCategories: List<CategorySpending>,
  val monthlyBudgetAmount: Double,
  val monthlyBudgetSpent: Double,
  val monthlyBudgetRemaining: Double,
  val monthlyBudgetPercentage: Double
)
