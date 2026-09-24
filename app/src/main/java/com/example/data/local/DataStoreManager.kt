package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "takatrack_prefs")

val PopularCurrencies: List<AppCurrency> = AppCurrency.SUPPORTED

data class AppCurrency(
  val code: String,
  val symbol: String,
  val name: String
) {
  companion object {
    val SUPPORTED = listOf(
      AppCurrency("BDT", "৳", "Bangladeshi Taka"),
      AppCurrency("USD", "$", "US Dollar"),
      AppCurrency("EUR", "€", "Euro"),
      AppCurrency("GBP", "£", "British Pound"),
      AppCurrency("INR", "₹", "Indian Rupee"),
      AppCurrency("CAD", "C$", "Canadian Dollar"),
      AppCurrency("AUD", "A$", "Australian Dollar"),
      AppCurrency("JPY", "¥", "Japanese Yen"),
      AppCurrency("SGD", "S$", "Singapore Dollar"),
      AppCurrency("MYR", "RM", "Malaysian Ringgit"),
      AppCurrency("AED", "AED", "UAE Dirham"),
      AppCurrency("SAR", "SAR", "Saudi Riyal")
    )
    val DEFAULT = SUPPORTED[0]
  }
}

class DataStoreManager(private val context: Context) {
  companion object {
    val KEY_CURRENCY_CODE = stringPreferencesKey("currency_code")
    val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
    val KEY_THEME = stringPreferencesKey("app_theme")
    val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val KEY_DAILY_REMINDER = booleanPreferencesKey("daily_reminder")
    val KEY_BUDGET_ALERTS = booleanPreferencesKey("budget_alerts")
    val KEY_MONTHLY_SUMMARY = booleanPreferencesKey("monthly_summary")
  }

  val currencySymbol: Flow<String> = context.dataStore.data.map { prefs ->
    prefs[KEY_CURRENCY_SYMBOL] ?: "৳"
  }

  val currencyCode: Flow<String> = context.dataStore.data.map { prefs ->
    prefs[KEY_CURRENCY_CODE] ?: "BDT"
  }

  val appTheme: Flow<String> = context.dataStore.data.map { prefs ->
    prefs[KEY_THEME] ?: "SYSTEM"
  }

  val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_ONBOARDING_COMPLETED] ?: false
  }

  val dailyReminderEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_DAILY_REMINDER] ?: true
  }

  val budgetAlertsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_BUDGET_ALERTS] ?: true
  }

  val monthlySummaryEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_MONTHLY_SUMMARY] ?: true
  }

  suspend fun setCurrency(code: String, symbol: String) {
    context.dataStore.edit { prefs ->
      prefs[KEY_CURRENCY_CODE] = code
      prefs[KEY_CURRENCY_SYMBOL] = symbol
    }
  }

  suspend fun setAppTheme(theme: String) {
    context.dataStore.edit { prefs ->
      prefs[KEY_THEME] = theme
    }
  }

  suspend fun setOnboardingCompleted(completed: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_ONBOARDING_COMPLETED] = completed
    }
  }

  suspend fun setDailyReminder(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_DAILY_REMINDER] = enabled
    }
  }

  suspend fun setBudgetAlerts(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_BUDGET_ALERTS] = enabled
    }
  }

  suspend fun setMonthlySummary(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_MONTHLY_SUMMARY] = enabled
    }
  }
}
