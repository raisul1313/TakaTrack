package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
  @PrimaryKey val id: String,
  val name: String,
  val type: AccountType,
  val initialBalance: Double = 0.0,
  val colorHex: String = "#0D9488",
  val iconName: String = "wallet"
)
