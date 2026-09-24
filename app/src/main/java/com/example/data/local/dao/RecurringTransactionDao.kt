package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
  @Query("SELECT * FROM recurring_transactions ORDER BY nextDueDate ASC")
  fun getAllRecurring(): Flow<List<RecurringTransactionEntity>>

  @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextDueDate ASC")
  fun getActiveRecurring(): Flow<List<RecurringTransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecurring(item: RecurringTransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecurringList(items: List<RecurringTransactionEntity>)

  @Update
  suspend fun updateRecurring(item: RecurringTransactionEntity)

  @Delete
  suspend fun deleteRecurring(item: RecurringTransactionEntity)

  @Query("DELETE FROM recurring_transactions WHERE id = :id")
  suspend fun deleteRecurringById(id: String)

  @Query("DELETE FROM recurring_transactions")
  suspend fun clearAll()
}
