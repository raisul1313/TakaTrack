package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BudgetPeriod
import com.example.data.local.entity.RecurringFrequency
import com.example.data.local.entity.TransactionType

class AppTypeConverters {
  @TypeConverter
  fun fromTransactionType(type: TransactionType): String = type.name

  @TypeConverter
  fun toTransactionType(value: String): TransactionType =
    runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.EXPENSE)

  @TypeConverter
  fun fromAccountType(type: AccountType): String = type.name

  @TypeConverter
  fun toAccountType(value: String): AccountType =
    runCatching { AccountType.valueOf(value) }.getOrDefault(AccountType.CASH)

  @TypeConverter
  fun fromBudgetPeriod(period: BudgetPeriod): String = period.name

  @TypeConverter
  fun toBudgetPeriod(value: String): BudgetPeriod =
    runCatching { BudgetPeriod.valueOf(value) }.getOrDefault(BudgetPeriod.MONTHLY)

  @TypeConverter
  fun fromRecurringFrequency(freq: RecurringFrequency): String = freq.name

  @TypeConverter
  fun toRecurringFrequency(value: String): RecurringFrequency =
    runCatching { RecurringFrequency.valueOf(value) }.getOrDefault(RecurringFrequency.MONTHLY)
}
