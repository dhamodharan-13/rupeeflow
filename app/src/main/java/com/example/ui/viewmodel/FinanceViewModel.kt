package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.FinanceDatabase
import com.example.data.model.CustodyPersonEntity
import com.example.data.model.CustodyTransactionEntity
import com.example.data.model.CustodyType
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class OverviewBalances(
    val myUpiBalance: Double = 0.0,
    val walletBalance: Double = 0.0,
    val treasureSafeBalance: Double = 0.0,
    val myTotalNetWorth: Double = 0.0,
    val otherPeopleTotalHeld: Double = 0.0,
    val totalBankUpiBalance: Double = 0.0
)

data class PersonSummary(
    val person: CustodyPersonEntity,
    val totalReceived: Double,
    val totalReturned: Double,
    val currentHeldBalance: Double,
    val transactions: List<CustodyTransactionEntity>
)

data class CategorySpending(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class MethodSpending(
    val method: PaymentMethod,
    val amount: Double,
    val percentage: Float
)

data class DaySpending(
    val dayLabel: String,
    val dateMillis: Long,
    val amount: Double
)

enum class AnalysisMode {
    WEEKLY,
    MONTHLY
}

data class AnalysisSettings(
    val mode: AnalysisMode = AnalysisMode.WEEKLY,
    val weekOffset: Int = 0,
    val monthOffset: Int = 0
)

data class AnalysisState(
    val mode: AnalysisMode = AnalysisMode.WEEKLY,
    val totalSpentInPeriod: Double = 0.0,
    val totalIncomeInPeriod: Double = 0.0,
    val totalTransactionsInPeriod: Int = 0,
    val topSpendingCategory: String = "None",
    val majorSpendings: List<TransactionEntity> = emptyList(),
    val categorySpendings: List<CategorySpending> = emptyList(),
    val methodSpendings: List<MethodSpending> = emptyList(),
    val dailyTrend: List<DaySpending> = emptyList(),
    val periodLabel: String = ""
)

enum class HistoryDateFilterType(val label: String) {
    ALL_TIME("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom Range")
}

data class HistoryFilterState(
    val dateFilterType: HistoryDateFilterType = HistoryDateFilterType.ALL_TIME,
    val customStartMillis: Long? = null,
    val customEndMillis: Long? = null,
    val selectedMethod: String? = null // null means All Methods
) {
    val isFilterActive: Boolean
        get() = dateFilterType != HistoryDateFilterType.ALL_TIME || selectedMethod != null

    val filterSummaryLabel: String
        get() {
            val methodLabel = selectedMethod?.let { PaymentMethod.fromId(it).shortName } ?: "All Methods"
            val dateLabel = when (dateFilterType) {
                HistoryDateFilterType.ALL_TIME -> "All Time"
                HistoryDateFilterType.TODAY -> "Today"
                HistoryDateFilterType.THIS_WEEK -> "This Week"
                HistoryDateFilterType.THIS_MONTH -> "This Month"
                HistoryDateFilterType.CUSTOM -> {
                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                    val start = customStartMillis?.let { sdf.format(Date(it)) } ?: "?"
                    val end = customEndMillis?.let { sdf.format(Date(it)) } ?: "?"
                    "$start - $end"
                }
            }
            return if (selectedMethod != null && dateFilterType != HistoryDateFilterType.ALL_TIME) {
                "$methodLabel • $dateLabel"
            } else if (selectedMethod != null) {
                methodLabel
            } else {
                dateLabel
            }
        }
}

data class FinanceUiState(
    val balances: OverviewBalances = OverviewBalances(),
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val historyFilter: HistoryFilterState = HistoryFilterState(),
    val people: List<CustodyPersonEntity> = emptyList(),
    val custodyTransactions: List<CustodyTransactionEntity> = emptyList(),
    val personSummaries: List<PersonSummary> = emptyList(),
    val analysisState: AnalysisState = AnalysisState(),
    val isLoading: Boolean = false
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db.transactionDao(), db.custodyDao())
    }

    private val _historyFilter = MutableStateFlow(HistoryFilterState())
    private val _analysisSettings = MutableStateFlow(AnalysisSettings())

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.allTransactions,
        repository.allPeople,
        repository.allCustodyTransactions,
        _historyFilter,
        _analysisSettings
    ) { rawTransactions, peopleList, rawCustodyTransactions, filter, settings ->

        // 1. Calculate Balances for the 3 buckets: UPI, Wallet, Treasure Safe
        var upiIn = 0.0
        var upiOut = 0.0
        var walletIn = 0.0
        var walletOut = 0.0
        var safeIn = 0.0
        var safeOut = 0.0

        for (tx in rawTransactions) {
            val isPlus = tx.type == TransactionType.INCOME.id
            when (PaymentMethod.fromId(tx.method)) {
                PaymentMethod.UPI -> if (isPlus) upiIn += tx.amount else upiOut += tx.amount
                PaymentMethod.WALLET -> if (isPlus) walletIn += tx.amount else walletOut += tx.amount
                PaymentMethod.SAFE -> if (isPlus) safeIn += tx.amount else safeOut += tx.amount
            }
        }

        val myUpi = upiIn - upiOut
        val myWallet = walletIn - walletOut
        val mySafe = safeIn - safeOut

        // Total Net Worth = My UPI + My Wallet + My Treasure Safe
        val myNetWorth = myUpi + myWallet + mySafe

        // 2. Calculate People's Custody Summary
        var totalPeopleReceived = 0.0
        var totalPeopleReturned = 0.0

        val personSummariesList = peopleList.map { person ->
            val personTxs = rawCustodyTransactions.filter { it.personId == person.id }
            val received = personTxs.filter { it.type == CustodyType.RECEIVED_TO_SAVE.id }.sumOf { it.amount }
            val returned = personTxs.filter { it.type == CustodyType.RETURNED_TO_PERSON.id }.sumOf { it.amount }
            totalPeopleReceived += received
            totalPeopleReturned += returned
            PersonSummary(
                person = person,
                totalReceived = received,
                totalReturned = returned,
                currentHeldBalance = (received - returned),
                transactions = personTxs
            )
        }

        val otherPeopleTotalHeld = totalPeopleReceived - totalPeopleReturned
        // Bank UPI total = My UPI Money + Custody Money
        val totalBankUpiBalance = myUpi + otherPeopleTotalHeld

        val balances = OverviewBalances(
            myUpiBalance = myUpi,
            walletBalance = myWallet,
            treasureSafeBalance = mySafe,
            myTotalNetWorth = myNetWorth,
            otherPeopleTotalHeld = otherPeopleTotalHeld,
            totalBankUpiBalance = totalBankUpiBalance
        )

        // 3. Filtered transactions according to Method and Date Filters
        val filtered = rawTransactions.filter { tx ->
            // Check Method Filter
            val matchesMethod = if (filter.selectedMethod != null) {
                tx.method.equals(filter.selectedMethod, ignoreCase = true)
            } else true

            // Check Date Filter
            val matchesDate = when (filter.dateFilterType) {
                HistoryDateFilterType.ALL_TIME -> true
                HistoryDateFilterType.TODAY -> {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    tx.dateMillis in start..end
                }
                HistoryDateFilterType.THIS_WEEK -> {
                    val cal = Calendar.getInstance()
                    cal.firstDayOfWeek = Calendar.MONDAY
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_WEEK, 6)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    tx.dateMillis in start..end
                }
                HistoryDateFilterType.THIS_MONTH -> {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    tx.dateMillis in start..end
                }
                HistoryDateFilterType.CUSTOM -> {
                    val start = filter.customStartMillis ?: 0L
                    val end = filter.customEndMillis ?: Long.MAX_VALUE
                    tx.dateMillis in start..end
                }
            }

            matchesMethod && matchesDate
        }

        // 4. Calculate Analysis
        val analysis = calculateAnalysis(
            transactions = rawTransactions,
            mode = settings.mode,
            weekOffset = settings.weekOffset,
            monthOffset = settings.monthOffset
        )

        FinanceUiState(
            balances = balances,
            transactions = rawTransactions,
            filteredTransactions = filtered,
            historyFilter = filter,
            people = peopleList,
            custodyTransactions = rawCustodyTransactions,
            personSummaries = personSummariesList,
            analysisState = analysis,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceUiState(isLoading = true)
    )

    fun setHistoryFilter(filter: HistoryFilterState) {
        _historyFilter.value = filter
    }

    fun resetHistoryFilter() {
        _historyFilter.value = HistoryFilterState()
    }

    fun setAnalysisMode(mode: AnalysisMode) {
        _analysisSettings.value = _analysisSettings.value.copy(mode = mode)
    }

    fun nextAnalysisPeriod() {
        val current = _analysisSettings.value
        _analysisSettings.value = when (current.mode) {
            AnalysisMode.WEEKLY -> current.copy(weekOffset = current.weekOffset + 1)
            AnalysisMode.MONTHLY -> current.copy(monthOffset = current.monthOffset + 1)
        }
    }

    fun prevAnalysisPeriod() {
        val current = _analysisSettings.value
        _analysisSettings.value = when (current.mode) {
            AnalysisMode.WEEKLY -> current.copy(weekOffset = current.weekOffset - 1)
            AnalysisMode.MONTHLY -> current.copy(monthOffset = current.monthOffset - 1)
        }
    }

    fun resetAnalysisPeriod() {
        val current = _analysisSettings.value
        _analysisSettings.value = current.copy(weekOffset = 0, monthOffset = 0)
    }

    // --- Personal Transactions Actions ---
    fun addTransaction(
        amount: Double,
        type: TransactionType,
        method: PaymentMethod,
        reason: String,
        category: String,
        dateMillis: Long
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                dateMillis = dateMillis,
                amount = amount,
                type = type.id,
                method = method.id,
                reason = reason.trim().ifEmpty { "Expense" },
                category = category.trim().ifEmpty { reason.trim() }
            )
            repository.insertTransaction(entity)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // --- Custody (Other People's Money in UPI) Actions ---
    fun addPerson(name: String, note: String = "") {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertPerson(
                    CustodyPersonEntity(
                        name = name.trim(),
                        note = note.trim()
                    )
                )
            }
        }
    }

    fun deletePerson(person: CustodyPersonEntity) {
        viewModelScope.launch {
            repository.deletePerson(person)
        }
    }

    fun addCustodyTransaction(
        personId: Long,
        personName: String,
        amount: Double,
        type: CustodyType,
        dateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.insertCustodyTransaction(
                CustodyTransactionEntity(
                    personId = personId,
                    personName = personName,
                    amount = amount,
                    type = type.id,
                    dateMillis = dateMillis,
                    note = note.trim()
                )
            )
        }
    }

    fun deleteCustodyTransaction(transaction: CustodyTransactionEntity) {
        viewModelScope.launch {
            repository.deleteCustodyTransaction(transaction)
        }
    }

    // --- Backup & Restore ---
    suspend fun getBackupJson(): String {
        return repository.exportBackupJson()
    }

    suspend fun restoreFromJson(jsonString: String): Boolean {
        return repository.restoreBackupJson(jsonString)
    }

    // --- Analytics Helper ---
    private fun calculateAnalysis(
        transactions: List<TransactionEntity>,
        mode: AnalysisMode,
        weekOffset: Int,
        monthOffset: Int
    ): AnalysisState {
        val calendar = Calendar.getInstance()

        val (startMillis, endMillis, periodLabel) = when (mode) {
            AnalysisMode.WEEKLY -> {
                calendar.firstDayOfWeek = Calendar.MONDAY
                calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                val startFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                val startStr = startFormat.format(Date(start))

                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis

                val endFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                val endStr = endFormat.format(Date(end))

                val label = if (weekOffset == 0) "This Week ($startStr - $endStr)"
                else "$startStr - $endStr"

                Triple(start, end, label)
            }
            AnalysisMode.MONTHLY -> {
                calendar.add(Calendar.MONTH, monthOffset)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, maxDay)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis

                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                val label = if (monthOffset == 0) "This Month (${monthFormat.format(Date(start))})"
                else monthFormat.format(Date(start))

                Triple(start, end, label)
            }
        }

        // Filter transactions within time window
        val periodTransactions = transactions.filter { it.dateMillis in startMillis..endMillis }
        val expenses = periodTransactions.filter { it.type == TransactionType.EXPENSE.id }
        val incomes = periodTransactions.filter { it.type == TransactionType.INCOME.id }

        val totalSpent = expenses.sumOf { it.amount }
        val totalIncome = incomes.sumOf { it.amount }

        // Method Breakdown
        val methodMap = mutableMapOf<PaymentMethod, Double>()
        for (m in PaymentMethod.entries) methodMap[m] = 0.0
        for (exp in expenses) {
            val m = PaymentMethod.fromId(exp.method)
            methodMap[m] = (methodMap[m] ?: 0.0) + exp.amount
        }
        val methodSpendings = methodMap.entries
            .filter { it.value > 0 }
            .map {
                MethodSpending(
                    method = it.key,
                    amount = it.value,
                    percentage = if (totalSpent > 0) ((it.value / totalSpent) * 100).toFloat() else 0f
                )
            }
            .sortedByDescending { it.amount }

        // Daily trend
        val dailyTrend = mutableListOf<DaySpending>()
        val dayFormat = if (mode == AnalysisMode.WEEKLY) SimpleDateFormat("EEE", Locale.getDefault())
        else SimpleDateFormat("dd", Locale.getDefault())

        val tempCal = Calendar.getInstance()
        tempCal.timeInMillis = startMillis

        while (tempCal.timeInMillis <= endMillis) {
            val dayStart = tempCal.timeInMillis
            val dayLabel = dayFormat.format(Date(dayStart))

            tempCal.set(Calendar.HOUR_OF_DAY, 23)
            tempCal.set(Calendar.MINUTE, 59)
            tempCal.set(Calendar.SECOND, 59)
            val dayEnd = tempCal.timeInMillis

            val daySpent = expenses
                .filter { it.dateMillis in dayStart..dayEnd }
                .sumOf { it.amount }

            dailyTrend.add(
                DaySpending(
                    dayLabel = dayLabel,
                    dateMillis = dayStart,
                    amount = daySpent
                )
            )

            // advance 1 day
            tempCal.add(Calendar.DAY_OF_MONTH, 1)
            tempCal.set(Calendar.HOUR_OF_DAY, 0)
            tempCal.set(Calendar.MINUTE, 0)
            tempCal.set(Calendar.SECOND, 0)
            tempCal.set(Calendar.MILLISECOND, 0)
        }

        // Major spendings ranked
        val majorSpendings = expenses.sortedByDescending { it.amount }.take(10)

        return AnalysisState(
            mode = mode,
            totalSpentInPeriod = totalSpent,
            totalIncomeInPeriod = totalIncome,
            totalTransactionsInPeriod = periodTransactions.size,
            topSpendingCategory = "None",
            majorSpendings = majorSpendings,
            categorySpendings = emptyList(),
            methodSpendings = methodSpendings,
            dailyTrend = dailyTrend,
            periodLabel = periodLabel
        )
    }
}
