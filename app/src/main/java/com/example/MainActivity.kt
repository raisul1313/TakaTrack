package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.repository.ExpenseRepository
import com.example.domain.model.TransactionItem
import com.example.ui.ExpenseViewModel
import com.example.ui.ExpenseViewModelFactory
import com.example.ui.navigation.AppBottomNavBar
import com.example.ui.navigation.BottomNavScreens
import com.example.ui.navigation.Screen
import com.example.ui.screens.accounts.AccountsScreen
import com.example.ui.screens.addexpense.AddExpenseScreen
import com.example.ui.screens.addtransaction.AddTransactionSheet
import com.example.ui.screens.budgets.BudgetsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.insights.InsightsScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.recurring.RecurringScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.transactions.TransactionDetailSheet
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private lateinit var viewModel: ExpenseViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getInstance(applicationContext)
    val dataStoreManager = DataStoreManager(applicationContext)
    val repository = ExpenseRepository(database)
    val factory = ExpenseViewModelFactory(repository, dataStoreManager)
    viewModel = ViewModelProvider(this, factory)[ExpenseViewModel::class.java]

    setContent {
      val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
      val darkTheme = when (appTheme) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
      }

      MyApplicationTheme(darkTheme = darkTheme) {
        TakaTrackApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun TakaTrackApp(viewModel: ExpenseViewModel) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

  val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
  val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

  var showAddTransactionSheet by remember { mutableStateOf(false) }
  var editingTransaction by remember { mutableStateOf<TransactionItem?>(null) }
  var selectedDetailTransaction by remember { mutableStateOf<TransactionItem?>(null) }

  val shouldShowBottomNav = remember(currentRoute) {
    BottomNavScreens.any { it.route == currentRoute }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      if (shouldShowBottomNav && isOnboardingCompleted) {
        AppBottomNavBar(
          currentRoute = currentRoute,
          onNavigate = { route ->
            navController.navigate(route) {
              popUpTo(Screen.Home.route) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          },
          modifier = Modifier.navigationBarsPadding()
        )
      }
    }
  ) { innerPadding ->
    val startDest = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    NavHost(
      navController = navController,
      startDestination = startDest,
      modifier = Modifier.padding(innerPadding)
    ) {
      composable(Screen.Onboarding.route) {
        OnboardingScreen(
          viewModel = viewModel,
          onComplete = {
            navController.navigate(Screen.Home.route) {
              popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
          }
        )
      }

      composable(Screen.Home.route) {
        HomeScreen(
          viewModel = viewModel,
          onNavigate = { route -> navController.navigate(route) },
          onOpenAddTransaction = {
            editingTransaction = null
            showAddTransactionSheet = true
          },
          onSelectTransaction = { tx ->
            selectedDetailTransaction = tx
          }
        )
      }

      composable(Screen.Transactions.route) {
        TransactionsScreen(
          viewModel = viewModel,
          onOpenAddTransaction = {
            editingTransaction = null
            showAddTransactionSheet = true
          },
          onSelectTransaction = { tx ->
            selectedDetailTransaction = tx
          }
        )
      }

      composable(Screen.Budgets.route) {
        BudgetsScreen(viewModel = viewModel)
      }

      composable(Screen.Insights.route) {
        InsightsScreen(viewModel = viewModel)
      }

      composable(Screen.Accounts.route) {
        AccountsScreen(
          viewModel = viewModel,
          onNavigateBack = { navController.popBackStack() },
          onOpenTransfer = {
            editingTransaction = null
            showAddTransactionSheet = true
          }
        )
      }

      composable(Screen.Recurring.route) {
        RecurringScreen(
          viewModel = viewModel,
          onNavigateBack = { navController.popBackStack() }
        )
      }

      composable(Screen.Settings.route) {
        SettingsScreen(
          viewModel = viewModel,
          onNavigateBack = { navController.popBackStack() },
          onNavigate = { route -> navController.navigate(route) }
        )
      }

      composable(Screen.AddExpense.route) {
        AddExpenseScreen(
          viewModel = viewModel,
          onNavigateBack = { navController.popBackStack() }
        )
      }
    }
  }

  // Add / Edit Transaction Bottom Sheet
  if (showAddTransactionSheet) {
    AddTransactionSheet(
      viewModel = viewModel,
      editingTransaction = editingTransaction,
      onDismiss = {
        showAddTransactionSheet = false
        editingTransaction = null
      }
    )
  }

  // Transaction Detail Bottom Sheet
  selectedDetailTransaction?.let { tx ->
    TransactionDetailSheet(
      transaction = tx,
      currencySymbol = currencySymbol,
      onDismiss = { selectedDetailTransaction = null },
      onEdit = { toEdit ->
        selectedDetailTransaction = null
        editingTransaction = toEdit
        showAddTransactionSheet = true
      },
      onDuplicate = { toDuplicate ->
        viewModel.duplicateTransaction(toDuplicate)
      },
      onDelete = { id ->
        viewModel.deleteTransaction(id)
      }
    )
  }
}
