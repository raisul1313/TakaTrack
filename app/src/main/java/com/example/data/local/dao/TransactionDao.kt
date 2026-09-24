package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
  fun getTransactionById(id: String): Flow<TransactionEntity?>

  @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC LIMIT :limit")
  fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC")
  fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE accountId = :accountId OR toAccountId = :accountId ORDER BY date DESC")
  fun getTransactionsByAccount(accountId: String): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransactions(transactions: List<TransactionEntity>)

  @Update
  suspend fun updateTransaction(transaction: TransactionEntity)

  @Delete
  suspend fun deleteTransaction(transaction: TransactionEntity)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransactionById(id: String)

  @Query("DELETE FROM transactions")
  suspend fun clearAll()
}
