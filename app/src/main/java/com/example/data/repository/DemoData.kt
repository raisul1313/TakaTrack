package com.example.data.repository

import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetPeriod
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.RecurringFrequency
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

object DemoData {

  fun getCurrentMonthYear(): String {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
    return sdf.format(cal.time)
  }

  val defaultCategories = listOf(
    // Expenses
    CategoryEntity("cat_food", "Food & Dining", TransactionType.EXPENSE, "restaurant", "#F97316", true),
    CategoryEntity("cat_groceries", "Groceries", TransactionType.EXPENSE, "shopping_cart", "#10B981", true),
    CategoryEntity("cat_transport", "Transport", TransactionType.EXPENSE, "directions_car", "#3B82F6", true),
    CategoryEntity("cat_shopping", "Shopping", TransactionType.EXPENSE, "shopping_bag", "#EC4899", true),
    CategoryEntity("cat_bills", "Bills & Utilities", TransactionType.EXPENSE, "receipt_long", "#EAB308", true),
    CategoryEntity("cat_rent", "Rent & Housing", TransactionType.EXPENSE, "home", "#6366F1", true),
    CategoryEntity("cat_health", "Healthcare", TransactionType.EXPENSE, "medical_services", "#EF4444", true),
    CategoryEntity("cat_education", "Education", TransactionType.EXPENSE, "school", "#14B8A6", true),
    CategoryEntity("cat_entertainment", "Entertainment", TransactionType.EXPENSE, "movie", "#8B5CF6", true),
    CategoryEntity("cat_travel", "Travel", TransactionType.EXPENSE, "flight", "#06B6D4", true),
    CategoryEntity("cat_subs", "Subscriptions", TransactionType.EXPENSE, "subscriptions", "#A855F7", true),
    CategoryEntity("cat_personal", "Personal Care", TransactionType.EXPENSE, "face", "#F43F5E", true),
    CategoryEntity("cat_other_exp", "Other Expense", TransactionType.EXPENSE, "more_horiz", "#64748B", true),

    // Income
    CategoryEntity("cat_salary", "Salary", TransactionType.INCOME, "account_balance_wallet", "#10B981", true),
    CategoryEntity("cat_freelance", "Freelance", TransactionType.INCOME, "laptop_mac", "#0D9488", true),
    CategoryEntity("cat_business", "Business", TransactionType.INCOME, "business_center", "#2563EB", true),
    CategoryEntity("cat_investment", "Investment", TransactionType.INCOME, "trending_up", "#8B5CF6", true),
    CategoryEntity("cat_bonus", "Bonus", TransactionType.INCOME, "card_giftcard", "#F59E0B", true),
    CategoryEntity("cat_gift", "Gift", TransactionType.INCOME, "redeem", "#EC4899", true),
    CategoryEntity("cat_other_inc", "Other Income", TransactionType.INCOME, "attach_money", "#64748B", true)
  )

  val defaultAccounts = listOf(
    AccountEntity("acc_cash", "Cash Wallet", AccountType.CASH, 12500.0, "#10B981", "payments"),
    AccountEntity("acc_bank", "Bank Account", AccountType.BANK, 45000.0, "#2563EB", "account_balance"),
    AccountEntity("acc_bkash", "bKash", AccountType.MOBILE_WALLET, 8500.0, "#E11D48", "smartphone"),
    AccountEntity("acc_nagad", "Nagad", AccountType.MOBILE_WALLET, 4200.0, "#EA580C", "phone_android"),
    AccountEntity("acc_credit", "Credit Card", AccountType.CREDIT_CARD, -2500.0, "#7C3AED", "credit_card"),
    AccountEntity("acc_savings", "Savings Vault", AccountType.SAVINGS, 30000.0, "#059669", "savings")
  )

  fun getSampleBudgets(monthYear: String = getCurrentMonthYear()): List<BudgetEntity> = listOf(
    BudgetEntity("b_overall", null, 50000.0, BudgetPeriod.MONTHLY, monthYear, 80.0),
    BudgetEntity("b_food", "cat_food", 10000.0, BudgetPeriod.MONTHLY, monthYear, 80.0),
    BudgetEntity("b_groceries", "cat_groceries", 8000.0, BudgetPeriod.MONTHLY, monthYear, 80.0),
    BudgetEntity("b_transport", "cat_transport", 4500.0, BudgetPeriod.MONTHLY, monthYear, 80.0),
    BudgetEntity("b_shopping", "cat_shopping", 6000.0, BudgetPeriod.MONTHLY, monthYear, 80.0),
    BudgetEntity("b_bills", "cat_bills", 4000.0, BudgetPeriod.MONTHLY, monthYear, 80.0),
    BudgetEntity("b_entertainment", "cat_entertainment", 3000.0, BudgetPeriod.MONTHLY, monthYear, 80.0)
  )

  fun getSampleRecurring(): List<RecurringTransactionEntity> {
    val now = System.currentTimeMillis()
    val dayMillis = 24L * 60 * 60 * 1000
    return listOf(
      RecurringTransactionEntity(
        id = "rec_rent",
        type = TransactionType.EXPENSE,
        amount = 20000.0,
        categoryId = "cat_rent",
        accountId = "acc_bank",
        merchant = "Apartment Rent",
        notes = "Monthly flat rental",
        frequency = RecurringFrequency.MONTHLY,
        nextDueDate = now + (5 * dayMillis)
      ),
      RecurringTransactionEntity(
        id = "rec_netflix",
        type = TransactionType.EXPENSE,
        amount = 650.0,
        categoryId = "cat_subs",
        accountId = "acc_credit",
        merchant = "Netflix Subscription",
        notes = "Standard HD plan",
        frequency = RecurringFrequency.MONTHLY,
        nextDueDate = now + (12 * dayMillis)
      ),
      RecurringTransactionEntity(
        id = "rec_internet",
        type = TransactionType.EXPENSE,
        amount = 1200.0,
        categoryId = "cat_bills",
        accountId = "acc_bkash",
        merchant = "Fiber Broadband",
        notes = "50Mbps home Wi-Fi",
        frequency = RecurringFrequency.MONTHLY,
        nextDueDate = now + (8 * dayMillis)
      ),
      RecurringTransactionEntity(
        id = "rec_salary",
        type = TransactionType.INCOME,
        amount = 72000.0,
        categoryId = "cat_salary",
        accountId = "acc_bank",
        merchant = "TechCorp Ltd",
        notes = "Monthly payroll direct deposit",
        frequency = RecurringFrequency.MONTHLY,
        nextDueDate = now + (20 * dayMillis)
      )
    )
  }

  fun getSampleTransactions(): List<TransactionEntity> {
    val now = System.currentTimeMillis()
    val dayMillis = 24L * 60 * 60 * 1000

    return listOf(
      // Today
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 450.0,
        categoryId = "cat_food",
        accountId = "acc_cash",
        merchant = "Sultan's Dine",
        date = now - (2 * 3600 * 1000), // 2 hours ago
        notes = "Lunch with colleagues",
        paymentMethod = "Cash"
      ),
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 320.0,
        categoryId = "cat_transport",
        accountId = "acc_bkash",
        merchant = "Uber Ride",
        date = now - (5 * 3600 * 1000),
        notes = "Office commute",
        paymentMethod = "bKash"
      ),

      // Yesterday
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 2450.0,
        categoryId = "cat_groceries",
        accountId = "acc_credit",
        merchant = "Agora Superstore",
        date = now - dayMillis,
        notes = "Weekly fresh vegetables, milk & fruits",
        paymentMethod = "Credit Card"
      ),
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 650.0,
        categoryId = "cat_subs",
        accountId = "acc_credit",
        merchant = "Netflix",
        date = now - (dayMillis + 4 * 3600 * 1000),
        notes = "Monthly streaming",
        paymentMethod = "Credit Card"
      ),

      // 2 days ago
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 2100.0,
        categoryId = "cat_food",
        accountId = "acc_bank",
        merchant = "Madchef Gourmet Burgers",
        date = now - (2 * dayMillis),
        notes = "Dinner celebration",
        paymentMethod = "Debit Card"
      ),
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 1200.0,
        categoryId = "cat_bills",
        accountId = "acc_bkash",
        merchant = "Link3 Internet",
        date = now - (2 * dayMillis + 6 * 3600 * 1000),
        notes = "Fiber optic internet bill",
        paymentMethod = "bKash"
      ),

      // 4 days ago
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 4800.0,
        categoryId = "cat_shopping",
        accountId = "acc_credit",
        merchant = "Aarong Fashion",
        date = now - (4 * dayMillis),
        notes = "Formal cotton shirts & pants",
        paymentMethod = "Credit Card"
      ),
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 1500.0,
        categoryId = "cat_health",
        accountId = "acc_nagad",
        merchant = "Popular Diagnostic Centre",
        date = now - (4 * dayMillis + 3 * 3600 * 1000),
        notes = "Routine doctor consultation",
        paymentMethod = "Nagad"
      ),

      // 6 days ago - Freelance income
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.INCOME,
        amount = 15000.0,
        categoryId = "cat_freelance",
        accountId = "acc_bank",
        merchant = "Client UX Wireframe Project",
        date = now - (6 * dayMillis),
        notes = "Milestone 2 payment received",
        paymentMethod = "Bank Transfer"
      ),
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 3250.0,
        categoryId = "cat_transport",
        accountId = "acc_cash",
        merchant = "Fuel Refill Station",
        date = now - (6 * dayMillis + 5 * 3600 * 1000),
        notes = "Full tank Octane",
        paymentMethod = "Cash"
      ),

      // 10 days ago - Rent
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 20000.0,
        categoryId = "cat_rent",
        accountId = "acc_bank",
        merchant = "Apartment Rent",
        date = now - (10 * dayMillis),
        notes = "House rent for current month",
        paymentMethod = "Bank Transfer"
      ),

      // 12 days ago - Transfer Bank to Cash
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.TRANSFER,
        amount = 5000.0,
        categoryId = "cat_other_exp",
        accountId = "acc_bank",
        toAccountId = "acc_cash",
        merchant = "ATM Cash Withdrawal",
        date = now - (12 * dayMillis),
        notes = "Weekly pocket cash withdrawal",
        paymentMethod = "ATM"
      ),

      // 15 days ago - Salary
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.INCOME,
        amount = 72000.0,
        categoryId = "cat_salary",
        accountId = "acc_bank",
        merchant = "TechCorp Global Ltd",
        date = now - (15 * dayMillis),
        notes = "Monthly salary direct credit",
        paymentMethod = "Direct Deposit"
      ),

      // 18 days ago
      TransactionEntity(
        id = UUID.randomUUID().toString(),
        type = TransactionType.EXPENSE,
        amount = 2950.0,
        categoryId = "cat_groceries",
        accountId = "acc_cash",
        merchant = "Unimart Supermarket",
        date = now - (18 * dayMillis),
        notes = "Monthly household cleaning & supplies",
        paymentMethod = "Cash"
      )
    )
  }
}
