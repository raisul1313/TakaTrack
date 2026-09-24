package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
  @Query("SELECT * FROM accounts ORDER BY name ASC")
  fun getAllAccounts(): Flow<List<AccountEntity>>

  @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
  fun getAccountById(id: String): Flow<AccountEntity?>

  @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
  suspend fun getAccountByIdSync(id: String): AccountEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAccount(account: AccountEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAccounts(accounts: List<AccountEntity>)

  @Update
  suspend fun updateAccount(account: AccountEntity)

  @Delete
  suspend fun deleteAccount(account: AccountEntity)

  @Query("DELETE FROM accounts WHERE id = :id")
  suspend fun deleteAccountById(id: String)

  @Query("DELETE FROM accounts")
  suspend fun clearAll()
}
