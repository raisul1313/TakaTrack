package com.example.data.local.entity

enum class TransactionType {
  EXPENSE,
  INCOME,
  TRANSFER
}

enum class AccountType {
  CASH,
  BANK,
  CREDIT_CARD,
  MOBILE_WALLET,
  SAVINGS,
  INVESTMENT
}

enum class BudgetPeriod {
  MONTHLY,
  WEEKLY
}

enum class RecurringFrequency {
  DAILY,
  WEEKLY,
  MONTHLY,
  YEARLY
}
