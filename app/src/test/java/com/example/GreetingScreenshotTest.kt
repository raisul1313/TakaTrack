package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.FinancialSummary
import com.example.ui.screens.home.MainBalanceCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleSummary = FinancialSummary(
      totalBalance = 48250.0,
      incomeThisMonth = 72000.0,
      expensesThisMonth = 23750.0,
      savingsThisMonth = 48250.0,
      savingsRatePercent = 67.0,
      topCategories = emptyList(),
      monthlyBudgetAmount = 50000.0,
      monthlyBudgetSpent = 23750.0,
      monthlyBudgetRemaining = 26250.0,
      monthlyBudgetPercentage = 47.5
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        MainBalanceCard(
          summary = sampleSummary,
          currencySymbol = "৳"
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
