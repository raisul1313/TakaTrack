package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
  @PrimaryKey val id: String,
  val type: TransactionType,
  val amount: Double,
  val categoryId: String,
  val accountId: String,
  val merchant: String,
  val notes: String = "",
  val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
  val nextDueDate: Long,
  val isActive: Boolean = true
)
