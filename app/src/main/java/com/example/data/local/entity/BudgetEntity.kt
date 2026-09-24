package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
  @PrimaryKey val id: String,
  val categoryId: String? = null, // null means overall monthly budget
  val amount: Double,
  val period: BudgetPeriod = BudgetPeriod.MONTHLY,
  val monthYear: String, // e.g. "2026-09"
  val warningThresholdPercent: Double = 80.0
)
