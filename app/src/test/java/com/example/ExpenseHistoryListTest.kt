package com.example

import com.example.data.local.entity.TransactionType
import com.example.domain.model.TransactionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseHistoryListTest {

  private fun createSampleExpense(
    id: String,
    merchant: String,
    amount: Double,
    dateMillis: Long,
    categoryId: String = "cat_food"
  ): TransactionItem {
    return TransactionItem(
      id = id,
      type = TransactionType.EXPENSE,
      amount = amount,
      categoryId = categoryId,
      categoryName = "Food & Dining",
      categoryIcon = "restaurant",
      categoryColorHex = "#EF4444",
      accountId = "acc_cash",
      accountName = "Cash",
      merchant = merchant,
      date = dateMillis,
      notes = "Lunch"
    )
  }

  @Test
  fun testDateGrouping_groupsByDayCorrectly() {
    val now = Calendar.getInstance()
    val todayMillis = now.timeInMillis

    now.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayMillis = now.timeInMillis

    val expense1 = createSampleExpense("1", "Starbucks", 150.0, todayMillis)
    val expense2 = createSampleExpense("2", "KFC", 450.0, todayMillis)
    val expense3 = createSampleExpense("3", "Groceries", 1200.0, yesterdayMillis)

    val list = listOf(expense1, expense2, expense3)

    val sdfDate = SimpleDateFormat("MMMM d, yyyy", Locale.US)
    val nowCheck = Calendar.getInstance()
    val todayStr = sdfDate.format(nowCheck.time)
    nowCheck.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStr = sdfDate.format(nowCheck.time)

    val grouped = linkedMapOf<String, MutableList<TransactionItem>>()
    for (tx in list) {
      val txDate = sdfDate.format(Date(tx.date))
      val header = when (txDate) {
        todayStr -> "Today"
        yesterdayStr -> "Yesterday"
        else -> txDate
      }
      grouped.getOrPut(header) { mutableListOf() }.add(tx)
    }

    assertEquals(2, grouped.keys.size)
    assertTrue(grouped.containsKey("Today"))
    assertTrue(grouped.containsKey("Yesterday"))
    assertEquals(2, grouped["Today"]?.size)
    assertEquals(1, grouped["Yesterday"]?.size)

    val todayTotal = grouped["Today"]?.sumOf { it.amount } ?: 0.0
    assertEquals(600.0, todayTotal, 0.001)

    val yesterdayTotal = grouped["Yesterday"]?.sumOf { it.amount } ?: 0.0
    assertEquals(1200.0, yesterdayTotal, 0.001)
  }

  @Test
  fun testSearchFilter_filtersExpenseHistory() {
    val expense1 = createSampleExpense("1", "Burger King", 250.0, System.currentTimeMillis())
    val expense2 = createSampleExpense("2", "Pharmacy", 100.0, System.currentTimeMillis())
    val list = listOf(expense1, expense2)

    val query = "Burger"
    val filtered = list.filter { it.merchant.contains(query, ignoreCase = true) }

    assertEquals(1, filtered.size)
    assertEquals("Burger King", filtered[0].merchant)
  }
}
