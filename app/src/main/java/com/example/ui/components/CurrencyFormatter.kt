package com.example.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

  fun format(amount: Double, symbol: String = "৳", showDecimals: Boolean = false): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val pattern = if (showDecimals && (amount % 1.0 != 0.0)) "#,##0.00" else "#,##0"
    val formatter = DecimalFormat(pattern, symbols)
    return "$symbol${formatter.format(amount)}"
  }

  fun formatSigned(
    amount: Double,
    isExpense: Boolean,
    symbol: String = "৳"
  ): String {
    val sign = if (isExpense) "-" else "+"
    return "$sign${format(amount, symbol)}"
  }

  fun formatCompact(amount: Double, symbol: String = "৳"): String {
    return when {
      amount >= 1_000_000 -> "$symbol${String.format(Locale.US, "%.1fM", amount / 1_000_000)}"
      amount >= 1_000 -> "$symbol${String.format(Locale.US, "%.1fk", amount / 1_000)}"
      else -> format(amount, symbol)
    }
  }
}
