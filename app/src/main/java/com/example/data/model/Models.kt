package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 3 Saving & Spending methods:
 * 1. UPI (Online banking)
 * 2. Wallet (Cash)
 * 3. Treasure Safe (Physical safe savings)
 */
enum class PaymentMethod(val id: String, val displayName: String, val shortName: String) {
    UPI("UPI", "UPI (Bank)", "UPI"),
    WALLET("WALLET", "Wallet (Cash)", "Wallet"),
    SAFE("SAFE", "Treasure Safe", "Safe");

    companion object {
        fun fromId(id: String): PaymentMethod {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: UPI
        }
    }
}

/**
 * Transaction type for personal tracking
 */
enum class TransactionType(val id: String, val displayName: String) {
    EXPENSE("EXPENSE", "Spending"),
    INCOME("INCOME", "Income / Add");

    companion object {
        fun fromId(id: String): TransactionType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: EXPENSE
        }
    }
}

/**
 * Standard spending categories for analysis & pie charts
 */
object Categories {
    const val FOOD = "Food & Dining"
    const val GROCERIES = "Groceries"
    const val SHOPPING = "Shopping"
    const val BILLS = "Bills & Utilities"
    const val TRANSPORT = "Transport & Fuel"
    const val ENTERTAINMENT = "Entertainment"
    const val HEALTH = "Health & Medical"
    const val SAFE_DEPOSIT = "Safe Stash"
    const val GENERAL = "General / Other"

    val list = listOf(
        FOOD,
        GROCERIES,
        SHOPPING,
        BILLS,
        TRANSPORT,
        ENTERTAINMENT,
        HEALTH,
        SAFE_DEPOSIT,
        GENERAL
    )
}

/**
 * Room entity representing a personal spending or income transaction
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long,
    val amount: Double,
    val type: String, // EXPENSE, INCOME
    val method: String, // UPI, WALLET, SAFE
    val reason: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val transactionType: TransactionType
        get() = TransactionType.fromId(type)

    val paymentMethod: PaymentMethod
        get() = PaymentMethod.fromId(method)
}

/**
 * Person for whom the user holds/saves money in UPI bank account
 */
@Entity(tableName = "custody_people")
data class CustodyPersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Type of custody transaction:
 * - RECEIVED_TO_SAVE: Person gave money to save
 * - RETURNED_TO_PERSON: User sent money back
 */
enum class CustodyType(val id: String, val displayName: String) {
    RECEIVED_TO_SAVE("RECEIVED", "Received"),
    RETURNED_TO_PERSON("RETURNED", "Returned");

    companion object {
        fun fromId(id: String): CustodyType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: RECEIVED_TO_SAVE
        }
    }
}

/**
 * Record of savings received or returned for other people
 */
@Entity(tableName = "custody_transactions")
data class CustodyTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val personName: String,
    val amount: Double,
    val type: String, // RECEIVED, RETURNED
    val dateMillis: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val custodyType: CustodyType
        get() = CustodyType.fromId(type)
}
