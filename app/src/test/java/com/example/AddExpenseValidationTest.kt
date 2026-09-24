package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AddExpenseValidationTest {

  // Validation helper mimicking the AddExpenseScreen validation rules
  private fun validateExpense(
    amountText: String,
    categoryId: String,
    description: String
  ): Triple<String?, String?, String?> {
    val amountError = when {
      amountText.isBlank() -> "Please enter an amount greater than 0"
      (amountText.toDoubleOrNull() ?: 0.0) <= 0.0 -> "Please enter an amount greater than 0"
      else -> null
    }

    val categoryError = if (categoryId.isBlank()) {
      "Please select a category"
    } else {
      null
    }

    val descriptionError = if (description.trim().isBlank()) {
      "Please enter a description"
    } else {
      null
    }

    return Triple(amountError, categoryError, descriptionError)
  }

  @Test
  fun testEmptyInputs_triggersAllValidationErrors() {
    val (amountErr, catErr, descErr) = validateExpense(
      amountText = "",
      categoryId = "",
      description = ""
    )

    assertNotNull(amountErr)
    assertEquals("Please enter an amount greater than 0", amountErr)
    assertNotNull(catErr)
    assertEquals("Please select a category", catErr)
    assertNotNull(descErr)
    assertEquals("Please enter a description", descErr)
  }

  @Test
  fun testZeroOrNegativeAmount_failsValidation() {
    val (zeroErr, _, _) = validateExpense("0", "cat_food", "Lunch")
    assertEquals("Please enter an amount greater than 0", zeroErr)

    val (negErr, _, _) = validateExpense("-50", "cat_food", "Lunch")
    assertEquals("Please enter an amount greater than 0", negErr)
  }

  @Test
  fun testValidInputs_passValidation() {
    val (amountErr, catErr, descErr) = validateExpense(
      amountText = "250.50",
      categoryId = "cat_food",
      description = "Dinner with colleagues"
    )

    assertNull(amountErr)
    assertNull(catErr)
    assertNull(descErr)
  }

  @Test
  fun testWhitespaceDescription_failsValidation() {
    val (_, _, descErr) = validateExpense("100", "cat_groceries", "   ")
    assertNotNull(descErr)
    assertEquals("Please enter a description", descErr)
  }
}
