package com.example

import com.example.data.local.entity.TransactionType
import com.example.domain.model.CategorySpending
import com.example.domain.model.TransactionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpendingDistributionTest {

  private fun createExpense(
    id: String,
    categoryId: String,
    categoryName: String,
    amount: Double
  ): TransactionItem {
    return TransactionItem(
      id = id,
      type = TransactionType.EXPENSE,
      amount = amount,
      categoryId = categoryId,
      categoryName = categoryName,
      categoryIcon = "shopping_bag",
      categoryColorHex = "#3B82F6",
      accountId = "acc_1",
      accountName = "Cash",
      merchant = "Merchant $id",
      date = System.currentTimeMillis(),
      notes = ""
    )
  }

  @Test
  fun testCategorySpendingAggregation_calculatesCorrectTotalsAndPercentages() {
    val txs = listOf(
      createExpense("1", "cat_food", "Food", 500.0),
      createExpense("2", "cat_food", "Food", 300.0),
      createExpense("3", "cat_rent", "Rent", 2000.0),
      createExpense("4", "cat_transport", "Transport", 1200.0)
    )

    val totalSpent = txs.sumOf { it.amount }
    assertEquals(4000.0, totalSpent, 0.001)

    val categorySpendingList = txs.groupBy { it.categoryId }
      .map { (catId, items) ->
        val spent = items.sumOf { it.amount }
        val pct = (spent / totalSpent) * 100.0
        CategorySpending(
          categoryId = catId,
          categoryName = items.first().categoryName,
          categoryIcon = "icon",
          categoryColorHex = "#000",
          totalSpent = spent,
          percentage = pct
        )
      }
      .sortedByDescending { it.totalSpent }

    // Rent should be #1 with 2000.0 (50.0%)
    assertEquals("cat_rent", categorySpendingList[0].categoryId)
    assertEquals(2000.0, categorySpendingList[0].totalSpent, 0.001)
    assertEquals(50.0, categorySpendingList[0].percentage, 0.001)

    // Transport should be #2 with 1200.0 (30.0%)
    assertEquals("cat_transport", categorySpendingList[1].categoryId)
    assertEquals(1200.0, categorySpendingList[1].totalSpent, 0.001)
    assertEquals(30.0, categorySpendingList[1].percentage, 0.001)

    // Food should be #3 with 800.0 (20.0%)
    assertEquals("cat_food", categorySpendingList[2].categoryId)
    assertEquals(800.0, categorySpendingList[2].totalSpent, 0.001)
    assertEquals(20.0, categorySpendingList[2].percentage, 0.001)

    // Sum of percentages must equal 100%
    val totalPct = categorySpendingList.sumOf { it.percentage }
    assertEquals(100.0, totalPct, 0.001)
  }

  @Test
  fun testSweepAngleCalculation_equals360Degrees() {
    val percentages = listOf(50.0, 30.0, 20.0)
    val sweepAngles = percentages.map { (it / 100.0 * 360f).toFloat() }

    val totalSweep = sweepAngles.sum()
    assertEquals(360f, totalSweep, 0.001f)
    assertEquals(180f, sweepAngles[0], 0.001f)
    assertEquals(108f, sweepAngles[1], 0.001f)
    assertEquals(72f, sweepAngles[2], 0.001f)
  }

  @Test
  fun testEmptyTransactions_handlesGracefully() {
    val txs = emptyList<TransactionItem>()
    val totalSpent = txs.sumOf { it.amount }
    val categorySpendingList = txs.groupBy { it.categoryId }.map { (_, _) -> }

    assertEquals(0.0, totalSpent, 0.001)
    assertTrue(categorySpendingList.isEmpty())
  }
}
