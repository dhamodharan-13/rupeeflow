package com.example.data.repository

import com.example.data.db.CustodyDao
import com.example.data.db.TransactionDao
import com.example.data.model.CustodyPersonEntity
import com.example.data.model.CustodyTransactionEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val custodyDao: CustodyDao
) {
    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsByMethod(method: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByMethod(method)

    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(startMillis, endMillis)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)

    // Custody
    val allPeople: Flow<List<CustodyPersonEntity>> = custodyDao.getAllPeople()
    val allCustodyTransactions: Flow<List<CustodyTransactionEntity>> =
        custodyDao.getAllCustodyTransactions()

    suspend fun insertPerson(person: CustodyPersonEntity): Long =
        custodyDao.insertPerson(person)

    suspend fun updatePerson(person: CustodyPersonEntity) =
        custodyDao.updatePerson(person)

    suspend fun deletePerson(person: CustodyPersonEntity) {
        custodyDao.deleteTransactionsForPerson(person.id)
        custodyDao.deletePerson(person)
    }

    suspend fun insertCustodyTransaction(transaction: CustodyTransactionEntity): Long =
        custodyDao.insertCustodyTransaction(transaction)

    suspend fun deleteCustodyTransaction(transaction: CustodyTransactionEntity) =
        custodyDao.deleteCustodyTransaction(transaction)

    // Backup Export to JSON string
    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // 1. Transactions
        val txs = transactionDao.getAllTransactionsSnapshot()
        val txArray = JSONArray()
        for (t in txs) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("dateMillis", t.dateMillis)
            obj.put("amount", t.amount)
            obj.put("type", t.type)
            obj.put("method", t.method)
            obj.put("reason", t.reason)
            obj.put("category", t.category)
            obj.put("createdAt", t.createdAt)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // 2. People
        val people = custodyDao.getAllPeopleSnapshot()
        val peopleArray = JSONArray()
        for (p in people) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("note", p.note)
            obj.put("createdAt", p.createdAt)
            peopleArray.put(obj)
        }
        root.put("people", peopleArray)

        // 3. Custody Transactions
        val ctxs = custodyDao.getAllCustodyTransactionsSnapshot()
        val ctxArray = JSONArray()
        for (c in ctxs) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("personId", c.personId)
            obj.put("personName", c.personName)
            obj.put("amount", c.amount)
            obj.put("type", c.type)
            obj.put("dateMillis", c.dateMillis)
            obj.put("note", c.note)
            obj.put("createdAt", c.createdAt)
            ctxArray.put(obj)
        }
        root.put("custodyTransactions", ctxArray)

        return root.toString(2)
    }

    // Restore Backup from JSON string
    suspend fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            val txList = mutableListOf<TransactionEntity>()
            if (root.has("transactions")) {
                val txArray = root.getJSONArray("transactions")
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    txList.add(
                        TransactionEntity(
                            id = obj.optLong("id", 0),
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            amount = obj.optDouble("amount", 0.0),
                            type = obj.optString("type", "EXPENSE"),
                            method = obj.optString("method", "UPI"),
                            reason = obj.optString("reason", "Expense"),
                            category = obj.optString("category", "General / Other"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val peopleList = mutableListOf<CustodyPersonEntity>()
            if (root.has("people")) {
                val peopleArray = root.getJSONArray("people")
                for (i in 0 until peopleArray.length()) {
                    val obj = peopleArray.getJSONObject(i)
                    peopleList.add(
                        CustodyPersonEntity(
                            id = obj.optLong("id", 0),
                            name = obj.optString("name", "Unknown"),
                            note = obj.optString("note", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val ctxList = mutableListOf<CustodyTransactionEntity>()
            if (root.has("custodyTransactions")) {
                val ctxArray = root.getJSONArray("custodyTransactions")
                for (i in 0 until ctxArray.length()) {
                    val obj = ctxArray.getJSONObject(i)
                    ctxList.add(
                        CustodyTransactionEntity(
                            id = obj.optLong("id", 0),
                            personId = obj.optLong("personId", 0),
                            personName = obj.optString("personName", "Unknown"),
                            amount = obj.optDouble("amount", 0.0),
                            type = obj.optString("type", "RECEIVED"),
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            note = obj.optString("note", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (txList.isNotEmpty()) {
                transactionDao.insertAll(txList)
            }
            if (peopleList.isNotEmpty()) {
                custodyDao.insertAllPeople(peopleList)
            }
            if (ctxList.isNotEmpty()) {
                custodyDao.insertAllCustodyTransactions(ctxList)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
