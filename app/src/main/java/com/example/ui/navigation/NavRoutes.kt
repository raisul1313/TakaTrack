package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
  data object Home : Screen("home", "Home")
  data object Transactions : Screen("transactions", "Transactions")
  data object Budgets : Screen("budgets", "Budgets")
  data object Insights : Screen("insights", "Insights")
  data object Accounts : Screen("accounts", "Accounts")
  data object Recurring : Screen("recurring", "Recurring")
  data object Settings : Screen("settings", "Settings")
  data object Onboarding : Screen("onboarding", "Onboarding")
  data object AddExpense : Screen("add_expense", "Add Expense")
  data object SpendingDistribution : Screen("spending_distribution", "Spending Distribution")
}

val BottomNavScreens = listOf(
  Screen.Home,
  Screen.Transactions,
  Screen.Budgets,
  Screen.Insights
)
