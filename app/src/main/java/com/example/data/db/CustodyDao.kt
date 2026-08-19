package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustodyPersonEntity
import com.example.data.model.CustodyTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustodyDao {
    @Query("SELECT * FROM custody_people ORDER BY name ASC")
    fun getAllPeople(): Flow<List<CustodyPersonEntity>>

    @Query("SELECT * FROM custody_people ORDER BY name ASC")
    suspend fun getAllPeopleSnapshot(): List<CustodyPersonEntity>

    @Query("SELECT * FROM custody_people WHERE id = :id")
    fun getPersonById(id: Long): Flow<CustodyPersonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: CustodyPersonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPeople(people: List<CustodyPersonEntity>)

    @Update
    suspend fun updatePerson(person: CustodyPersonEntity)

    @Delete
    suspend fun deletePerson(person: CustodyPersonEntity)

    @Query("DELETE FROM custody_transactions WHERE personId = :personId")
    suspend fun deleteTransactionsForPerson(personId: Long)

    @Query("SELECT * FROM custody_transactions ORDER BY dateMillis DESC, id DESC")
    fun getAllCustodyTransactions(): Flow<List<CustodyTransactionEntity>>

    @Query("SELECT * FROM custody_transactions ORDER BY dateMillis DESC, id DESC")
    suspend fun getAllCustodyTransactionsSnapshot(): List<CustodyTransactionEntity>

    @Query("SELECT * FROM custody_transactions WHERE personId = :personId ORDER BY dateMillis DESC, id DESC")
    fun getCustodyTransactionsForPerson(personId: Long): Flow<List<CustodyTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustodyTransaction(transaction: CustodyTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustodyTransactions(transactions: List<CustodyTransactionEntity>)

    @Delete
    suspend fun deleteCustodyTransaction(transaction: CustodyTransactionEntity)

    @Query("DELETE FROM custody_people")
    suspend fun clearPeople()

    @Query("DELETE FROM custody_transactions")
    suspend fun clearCustodyTransactions()
}
