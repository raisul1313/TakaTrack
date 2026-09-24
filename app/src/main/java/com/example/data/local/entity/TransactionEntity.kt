package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "transactions",
  indices = [
    Index(value = ["date"]),
    Index(value = ["categoryId"]),
    Index(value = ["accountId"]),
    Index(value = ["type"])
  ]
)
data class TransactionEntity(
  @PrimaryKey val id: String,
  val type: TransactionType,
  val amount: Double,
  val categoryId: String,
  val accountId: String,
  val toAccountId: String? = null,
  val merchant: String,
  val date: Long, // timestamp in millis
  val notes: String = "",
  val paymentMethod: String = "Cash",
  val attachmentUri: String? = null,
  val isRecurring: Boolean = false,
  val recurringInterval: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
