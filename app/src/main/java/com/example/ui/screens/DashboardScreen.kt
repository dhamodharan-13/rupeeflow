package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.HistoryFilterDialog
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.AppleRed
import com.example.ui.viewmodel.FinanceUiState
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    onNavigateToWealth: () -> Unit,
    onNavigateToCustody: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var isBalanceHidden by remember { mutableStateOf(true) }
    var dialogInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var dialogInitialMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val balances = uiState.balances
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. My Wealth Card (Click takes you to Wealth Details Page)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToWealth() }
                    .testTag("total_networth_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "MY WEALTH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            IconButton(
                                onClick = { isBalanceHidden = !isBalanceHidden },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Balance Privacy",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppleBlue.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "3 Accounts",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleBlue
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = AppleBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBalanceHidden) "₹ ••••••••"
                        else String.format(Locale.getDefault(), "₹%,.2f", balances.myTotalNetWorth),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("net_worth_value")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multi-Segment Allocation Bar
                    val totalPositive = maxOf(
                        1.0,
                        maxOf(0.0, balances.myUpiBalance) + maxOf(0.0, balances.walletBalance) + maxOf(0.0, balances.treasureSafeBalance)
                    )
                    val upiWeight = (maxOf(0.0, balances.myUpiBalance) / totalPositive).toFloat().coerceAtLeast(0.05f)
                    val walletWeight = (maxOf(0.0, balances.walletBalance) / totalPositive).toFloat().coerceAtLeast(0.05f)
                    val safeWeight = (maxOf(0.0, balances.treasureSafeBalance) / totalPositive).toFloat().coerceAtLeast(0.05f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(upiWeight)
                                .fillMaxHeight()
                                .background(AppleBlue)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .weight(walletWeight)
                                .fillMaxHeight()
                                .background(AppleGreen)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .weight(safeWeight)
                                .fillMaxHeight()
                                .background(AppleOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AllocationLegendItem(label = "UPI", color = AppleBlue, amount = balances.myUpiBalance, isHidden = isBalanceHidden)
                        AllocationLegendItem(label = "Wallet", color = AppleGreen, amount = balances.walletBalance, isHidden = isBalanceHidden)
                        AllocationLegendItem(label = "Safe", color = AppleOrange, amount = balances.treasureSafeBalance, isHidden = isBalanceHidden)
                    }
                }
            }
        }

        // 2. Transactions History Header & Filters
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "RECENT ACTIVITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "(${uiState.filteredTransactions.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Active filter chip
                    if (uiState.historyFilter.isFilterActive) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AppleBlue.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { showFilterDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = uiState.historyFilter.filterSummaryLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleBlue
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear filter",
                                    tint = AppleBlue,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { viewModel.resetHistoryFilter() }
                                )
                            }
                        }
                    }

                    // Filter Button
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_filter_history")
                    ) {
                        if (uiState.historyFilter.isFilterActive) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = AppleBlue,
                                        modifier = Modifier.size(6.dp)
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter History",
                                    tint = AppleBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Grouped Transaction Items Container
        if (uiState.filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (uiState.historyFilter.isFilterActive) "No transactions match your filter" else "No transactions recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.historyFilter.isFilterActive) {
                            TextButton(onClick = { viewModel.resetHistoryFilter() }) {
                                Text("Reset Filters", color = AppleBlue)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        uiState.filteredTransactions.forEachIndexed { index, tx ->
                            AppleTransactionRow(
                                transaction = tx,
                                dateFormat = dateFormat,
                                isHidden = isBalanceHidden,
                                onDelete = { transactionToDelete = tx }
                            )
                            if (index < uiState.filteredTransactions.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 58.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        AddTransactionDialog(
            initialType = dialogInitialType,
            initialMethod = dialogInitialMethod,
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, type, method, reason, category, dateMillis ->
                viewModel.addTransaction(
                    amount = amount,
                    type = type,
                    method = method,
                    reason = reason,
                    category = category,
                    dateMillis = dateMillis
                )
                showAddDialog = false
            }
        )
    }

    // History Filter Dialog
    if (showFilterDialog) {
        HistoryFilterDialog(
            initialFilter = uiState.historyFilter,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { newFilter ->
                viewModel.setHistoryFilter(newFilter)
                showFilterDialog = false
            },
            onResetFilter = {
                viewModel.resetHistoryFilter()
                showFilterDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Delete Entry", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${tx.reason}' for ₹${tx.amount}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { transactionToDelete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AllocationLegendItem(
    label: String,
    color: Color,
    amount: Double,
    isHidden: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (isHidden) "₹••••" else "₹${String.format(Locale.getDefault(), "%,.0f", amount)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AppleTransactionRow(
    transaction: TransactionEntity,
    dateFormat: SimpleDateFormat,
    isHidden: Boolean,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE.id
    val method = transaction.paymentMethod
    val methodColor = when (method) {
        PaymentMethod.UPI -> AppleBlue
        PaymentMethod.WALLET -> AppleGreen
        PaymentMethod.SAFE -> AppleOrange
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("tx_row_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isExpense) AppleRed.copy(alpha = 0.12f)
                        else AppleGreen.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpense) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = if (isExpense) AppleRed else AppleGreen,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = methodColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = method.shortName,
                            style = MaterialTheme.typography.labelSmall,
                            color = methodColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = dateFormat.format(Date(transaction.dateMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isHidden) "₹••••"
                else "${if (isExpense) "-" else "+"}₹${String.format(Locale.getDefault(), "%,.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) AppleRed else AppleGreen
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
